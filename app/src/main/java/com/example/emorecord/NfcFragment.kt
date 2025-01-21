package com.example.emorecord

import android.app.PendingIntent
import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.example.emorecord.R
import com.example.emorecord.databinding.FragmentNfcBinding
import com.example.emorecord.utils.ViewModelFactory
import com.example.emorecord.viewmodel.CalendarViewModel
import com.example.emorecord.viewmodel.MoodType
import com.example.emorecord.viewmodel.MoodViewModel
import com.example.emorecord.viewmodel.NfcStatus
import com.example.emorecord.viewmodel.NfcViewModel
import org.threeten.bp.LocalDate  // 使用这个导入而不是 java.time.LocalDate


class NfcFragment : Fragment() {
    private lateinit var binding: FragmentNfcBinding
    private lateinit var nfcAdapter: NfcAdapter
    private val viewModel: NfcViewModel by viewModels()
    private val calendarViewModel: CalendarViewModel by activityViewModels {
        ViewModelFactory(requireActivity().application)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNfcBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initNfc()
        setupButtons()
        observeViewModel()
    }

    private fun initNfc() {
        nfcAdapter = NfcAdapter.getDefaultAdapter(requireContext())
    }

    private fun setupButtons() {
        binding.writeSadButton.setOnClickListener {
            viewModel.writeSadMood()
        }

        binding.writeHappyButton.setOnClickListener {
            viewModel.writeHappyMood()
        }
    }

    private fun observeViewModel() {
        viewModel.nfcStatus.observe(viewLifecycleOwner) { status ->
            when (status) {
                is NfcStatus.Writing -> {
                    binding.instructionText.visibility = View.VISIBLE
                    enableNfcForegroundDispatch()
                }
                else -> {
                    binding.instructionText.visibility = View.GONE
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun handleNfcIntent(intent: Intent) {
        if (intent.action == NfcAdapter.ACTION_NDEF_DISCOVERED ||
            intent.action == NfcAdapter.ACTION_TECH_DISCOVERED ||
            intent.action == NfcAdapter.ACTION_TAG_DISCOVERED) {

            val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
            val ndef = Ndef.get(tag)

            try {
                ndef?.connect()
                when (viewModel.nfcStatus.value) {
                    is NfcStatus.Writing -> {
                        // 写入NFC标签
                        val moodType = if (viewModel.isWritingHappy) MoodType.HAPPY else MoodType.SAD

                        // 修复NdefMessage构造
                        val record = NdefRecord.createMime(
                            "application/com.example.mood",
                            moodType.name.toByteArray()
                        )
                        val message = NdefMessage(arrayOf(record))

                        ndef?.writeNdefMessage(message)

                        // 更新心情计数
                        val today = LocalDate.now()
                        val currentMood = calendarViewModel.selectedDateMood.value ?: MoodData(today, 0, 0)
                        if (moodType == MoodType.HAPPY) {
                            calendarViewModel.saveMoodForDate(today, currentMood.happyCount + 1, currentMood.sadCount)
                        } else {
                            calendarViewModel.saveMoodForDate(today, currentMood.happyCount, currentMood.sadCount + 1)
                        }
                        viewModel.onNfcOperationComplete()
                    }
                    else -> {
                        // 读取NFC标签
                        val ndefMessage = ndef?.ndefMessage
                        if (ndefMessage != null) {
                            val record = ndefMessage.records.firstOrNull()
                            if (record != null) {
                                val payload = record.payload
                                val moodTypeString = String(payload)
                                try {
                                    val moodType = MoodType.valueOf(moodTypeString)

                                    val today = LocalDate.now()
                                    val currentMood = calendarViewModel.selectedDateMood.value ?: MoodData(today, 0, 0)
                                    when (moodType) {
                                        MoodType.SAD -> calendarViewModel.saveMoodForDate(today, currentMood.happyCount, currentMood.sadCount + 1)
                                        MoodType.HAPPY -> calendarViewModel.saveMoodForDate(today, currentMood.happyCount + 1, currentMood.sadCount)
                                        else -> {} // 忽略其他情况
                                    }
                                    viewModel.onNfcOperationComplete()
                                } catch (e: IllegalArgumentException) {
                                    viewModel.onNfcError("Invalid mood type")
                                }
                            }
                        }
                    }
                }
                ndef?.close()
            } catch (e: Exception) {
                viewModel.onNfcError(e.message ?: "Unknown error")
            }
        }
    }


    fun enableNfcForegroundDispatch() {
        val intent = Intent(context, activity?.javaClass).apply {
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        nfcAdapter.enableForegroundDispatch(
            requireActivity(),
            pendingIntent,
            null,
            null
        )
    }

    fun disableNfcForegroundDispatch() {
        nfcAdapter.disableForegroundDispatch(requireActivity())
    }

    override fun onPause() {
        super.onPause()
        disableNfcForegroundDispatch()
    }
}



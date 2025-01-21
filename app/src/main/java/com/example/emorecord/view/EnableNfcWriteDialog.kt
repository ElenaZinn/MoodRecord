package com.example.emorecord.view

import android.app.Activity
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.os.Bundle
import com.example.emorecord.R

// EnableNfcWriteDialog.kt
class EnableNfcWriteDialog(
    context: Context,
    private val onTagReceived: (Tag?) -> Unit
) : AlertDialog(context) {

    private val nfcAdapter: NfcAdapter? by lazy {
        NfcAdapter.getDefaultAdapter(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_enable_nfc_write)
        setCancelable(true)

        // 启用前台调度系统
        nfcAdapter?.enableForegroundDispatch(
            context as Activity,
            getPendingIntent(),
            getIntentFiltersArray(),
            getTechListsArray()
        )
    }

    private fun getPendingIntent(): PendingIntent {
        val intent = Intent(context, context.javaClass).apply {
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        return PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun getIntentFiltersArray(): Array<IntentFilter> {
        return arrayOf(IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED))
    }

    private fun getTechListsArray(): Array<Array<String>> {
        return arrayOf(arrayOf(Ndef::class.java.name))
    }

    fun onNewIntent(intent: Intent) {
        if (intent.action == NfcAdapter.ACTION_TAG_DISCOVERED) {
            onTagReceived(intent.getParcelableExtra(NfcAdapter.EXTRA_TAG))
            dismiss()
        }
    }

    override fun onStop() {
        super.onStop()
        nfcAdapter?.disableForegroundDispatch(context as Activity)
    }
}
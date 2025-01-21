package com.example.emorecord

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CalendarView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.lifecycle.lifecycleScope
import com.example.emorecord.databinding.FragmentCalendarBinding
import com.example.emorecord.utils.ViewModelFactory
import com.example.emorecord.viewmodel.CalendarViewModel
import com.google.android.material.snackbar.Snackbar
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.spans.DotSpan
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate  // 使用这个导入而不是 java.time.LocalDate
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.CalendarDay
import java.util.Calendar

// CalendarFragment.kt
class CalendarFragment : Fragment() {
    private lateinit var binding: FragmentCalendarBinding
    private val viewModel: CalendarViewModel by viewModels {
        ViewModelFactory(requireActivity().application)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCalendarBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupCalendar()
        observeViewModel()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupCalendar() {
        binding.calendarView.apply {
            // 使用 LocalDate 设置日期范围
            state().edit()
                .setMinimumDate(CalendarDay.from(
                    LocalDate.now().minusYears(1)
                ))
                .setMaximumDate(CalendarDay.from(LocalDate.now()))
                .commit()

            // 设置日期选择监听器
            setOnDateChangedListener { _, date, _ ->
                viewModel.loadMoodForDate(date.year, date.month, date.day)
            }
        }

        // 自定义日期装饰器
        binding.calendarView.addDecorator(object : DayViewDecorator {
            override fun shouldDecorate(day: CalendarDay): Boolean {
                return viewModel.hasMoodRecord(day.date)
            }

            override fun decorate(view: DayViewFacade) {
                view.addSpan(DotSpan(5f, ContextCompat.getColor(requireContext(), R.color.accent)))
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.selectedDateMood.collect { mood ->
                showMoodDetails(mood)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.errorMessage.collect { message ->
                message?.let {
                    showError(it)
                }
            }
        }
    }

    private fun showMoodDetails(mood: MoodData?) {
        binding.moodDetailsCard.isVisible = mood != null

        mood?.let {
            binding.apply {
                // 更新心情统计
                happyCount.text = getString(R.string.happy_count, it.happyCount)
                sadCount.text = getString(R.string.sad_count, it.sadCount)

                // 计算主导情绪
                val dominantMood = if (it.happyCount > it.sadCount) {
                    R.drawable.ic_mood_happy
                } else if (it.sadCount > it.happyCount) {
                    R.drawable.ic_mood_sad
                } else {
                    R.drawable.ic_mood_neutral
                }

                // 设置主导情绪图标
                dominantMoodIcon.setImageResource(dominantMood)

                // 显示动画
                moodDetailsCard.animateVisible()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setAction("重试") {
                viewModel.retryLastOperation()
            }
            .show()
    }
}


// MoodData.kt
data class MoodData(
    val date: LocalDate,
    val happyCount: Int,
    val sadCount: Int
)

// Extensions.kt
fun View.animateVisible() {
    alpha = 0f
    isVisible = true
    animate()
        .alpha(1f)
        .setDuration(300)
        .setInterpolator(FastOutSlowInInterpolator())
        .start()
}

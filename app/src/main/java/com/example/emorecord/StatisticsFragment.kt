package com.example.emorecord

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.emorecord.R
import com.example.emorecord.databinding.FragmentStatisticsBinding
import com.example.emorecord.viewmodel.MoodViewModel
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import kotlinx.coroutines.launch

// StatisticsFragment.kt
class StatisticsFragment : Fragment() {
    private lateinit var binding: FragmentStatisticsBinding
    private val moodViewModel: MoodViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStatisticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 获取所有历史记录
        val allMoods = moodViewModel.getAllMoods()
        updateStatistics(allMoods)
        setupPieChart()
        observeStatistics()
        setupWeeklyChart()
    }

    private fun updateStatistics(moods: Map<String, Pair<Int, Int>>) {
        // 处理统计数据
        val totalHappy = moods.values.sumOf { it.first }
        val totalSad = moods.values.sumOf { it.second }
    }

    private fun setupPieChart() {
        binding.pieChart.apply {
            description.isEnabled = false
            isDrawHoleEnabled = true
            setHoleColor(Color.WHITE)
            legend.isEnabled = true
            legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
            setUsePercentValues(true)
        }
    }

    private fun observeStatistics() {
        viewLifecycleOwner.lifecycleScope.launch {
            moodViewModel.statistics.collect { stats ->
                updateCharts(stats)
            }

            moodViewModel.currentStreak.collect { streak ->
                binding.streakText.text = "连续记录：$streak 天"
            }
        }
    }

    private fun updateCharts(stats: Statistics) {
        // 更新饼图
        binding.pieChart.apply {
            val entries = listOf(
                PieEntry(stats.happyPercentage, "Happy"),
                PieEntry(stats.sadPercentage, "Sad")
            )

            val dataSet = PieDataSet(entries, "Mood Distribution").apply {
                colors = listOf(
                    ContextCompat.getColor(requireContext(), R.color.happy_pink),
                    ContextCompat.getColor(requireContext(), R.color.sad_blue)
                )
                valueTextSize = 14f
                valueTextColor = Color.WHITE
            }
            this?.let {pieChart ->
                data = PieData(dataSet).apply {
                    setValueFormatter(PercentFormatter(pieChart))
                }
            }


            animateY(1000)
            invalidate()
        }

        // 更新文本
        binding.streakText.text = "Current streak: ${stats.streakDays} days"
        binding.totalCount.text = "Total records: ${stats.totalCount}"
    }

    private fun setupWeeklyChart() {
        binding.weeklyLineChart.apply {
            description.isEnabled = false
            legend.isEnabled = true
            setTouchEnabled(true)
            setScaleEnabled(false)

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                valueFormatter = DayAxisValueFormatter()
                setDrawGridLines(false)
            }

            axisLeft.apply {
                setDrawGridLines(true)
                axisMinimum = 0f
            }

            axisRight.isEnabled = false

            // 设置图例
            legend.apply {
                orientation = Legend.LegendOrientation.HORIZONTAL
                horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
            }
        }

        updateWeeklyChart()
    }

    private fun updateWeeklyChart() {
        val weeklyData = moodViewModel.getWeeklyMoodData()

        // 创建快乐情绪数据集
        val happyEntries = weeklyData.mapIndexed { index, pair ->
            Entry(index.toFloat(), pair.first.toFloat())
        }

        // 创建悲伤情绪数据集
        val sadEntries = weeklyData.mapIndexed { index, pair ->
            Entry(index.toFloat(), pair.second.toFloat())
        }

        val happyDataSet = LineDataSet(happyEntries, "Happy").apply {
            color = ContextCompat.getColor(requireContext(), R.color.happy_pink)
            setCircleColor(ContextCompat.getColor(requireContext(), R.color.happy_pink))
            lineWidth = 2f
            circleRadius = 4f
            setDrawValues(true)
            mode = LineDataSet.Mode.LINEAR
        }

        val sadDataSet = LineDataSet(sadEntries, "Sad").apply {
            color = ContextCompat.getColor(requireContext(), R.color.sad_blue)
            setCircleColor(ContextCompat.getColor(requireContext(), R.color.sad_blue))
            lineWidth = 2f
            circleRadius = 4f
            setDrawValues(true)
            mode = LineDataSet.Mode.LINEAR
        }

        binding.weeklyLineChart.data = LineData(happyDataSet, sadDataSet)
        binding.weeklyLineChart.invalidate()
    }
}

// X轴日期格式化
class DayAxisValueFormatter : ValueFormatter() {
    private val days = arrayOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")

    override fun getAxisLabel(value: Float, axis: AxisBase?): String {
        return days.getOrNull(value.toInt()) ?: ""
    }
}


data class Statistics(
    val happyPercentage: Float = 0f,
    val sadPercentage: Float = 0f,
    val streakDays: Int = 0,
    val totalCount: Int = 0
)

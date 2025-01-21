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
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
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
        setupPieChart()
        observeStatistics()
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
}


data class Statistics(
    val happyPercentage: Float = 0f,
    val sadPercentage: Float = 0f,
    val streakDays: Int = 0,
    val totalCount: Int = 0
)

package com.example.emorecord.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.emorecord.Statistics
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.ln
import kotlin.math.pow

class MoodViewModel : ViewModel() {
    private val _sadCount = MutableStateFlow(0)
    val sadCount: StateFlow<Int> = _sadCount.asStateFlow()

    private val _happyCount = MutableStateFlow(0)
    val happyCount: StateFlow<Int> = _happyCount.asStateFlow()

    private val _progressPercent = MutableStateFlow(50)
    val progressPercent: StateFlow<Int> = _progressPercent

    private val _statistics = MutableStateFlow(Statistics())
    val statistics: StateFlow<Statistics> = _statistics.asStateFlow()


    init {
        updateStatistics()
    }

    private fun updateStatistics() {
        val total = _sadCount.value + _happyCount.value
        if (total > 0) {
            _statistics.value = Statistics(
                happyPercentage = (_happyCount.value.toFloat() / total) * 100f,
                sadPercentage = (_sadCount.value.toFloat() / total) * 100f,
                streakDays = calculateStreak(),
                totalCount = total
            )
        }
    }

    private fun calculateStreak(): Int {
        // TODO: 实现连续记录天数的计算逻辑
        return 0
    }


    fun onSadClick() {
        _sadCount.value = (_sadCount.value ?: 0) + 1
        updateProgress()
    }

    fun onHappyClick() {
        _happyCount.value = (_happyCount.value ?: 0) + 1
        updateProgress()
    }

    private fun updateProgress() {
        val happy = _happyCount.value
        val sad = _sadCount.value
        val total = happy + sad

        if (total > 0) {
            // 使用对数比例计算
            val happyLog = ln(happy.toDouble() + 1)
            val sadLog = ln(sad.toDouble() + 1)
            val totalLog = happyLog + sadLog

            _progressPercent.value = if (totalLog > 0) {
                (sadLog / totalLog * 100).toInt()
            } else {
                50
            }
        }
    }

}
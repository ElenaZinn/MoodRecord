package com.example.emorecord.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.emorecord.Statistics
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

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
        val total = (_sadCount.value ?: 0) + (_happyCount.value ?: 0)
        if (total > 0) {
            _progressPercent.value = (_happyCount.value ?: 0) * 100 / total
        }
    }

}
package com.example.emorecord.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.emorecord.Statistics
import com.example.emorecord.utils.MoodPrefsManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MoodViewModel(application: Application) :  AndroidViewModel(application) {
    private val prefsManager = MoodPrefsManager(application)

    private val _sadCount = MutableStateFlow(0)
    val sadCount: StateFlow<Int> = _sadCount.asStateFlow()

    private val _happyCount = MutableStateFlow(0)
    val happyCount: StateFlow<Int> = _happyCount.asStateFlow()

    private val _progressPercent = MutableStateFlow(50)
    val progressPercent: StateFlow<Int> = _progressPercent

    private val _statistics = MutableStateFlow(Statistics())
    val statistics: StateFlow<Statistics> = _statistics.asStateFlow()

    private val _currentStreak = MutableStateFlow(0)
    val currentStreak: StateFlow<Int> = _currentStreak.asStateFlow()

    private var resetJob: Job? = null


    init {
        loadTodayMood()
        updateStreak()
        setupDailyReset()
    }

    private fun loadTodayMood() {
        val today = getCurrentDate()
        val (happy, sad) = prefsManager.getMoodCounts(today)
        _happyCount.value = happy
        _sadCount.value = sad
        updateProgress()
    }

    private fun updateStatistics() {
        val total = _sadCount.value + _happyCount.value
        if (total > 0) {
            _statistics.value = Statistics(
                happyPercentage = (_happyCount.value.toFloat() / total) * 100f,
                sadPercentage = (_sadCount.value.toFloat() / total) * 100f,
                streakDays = prefsManager.calculateStreak(),
                totalCount = total
            )
        }
    }


    fun onSadClick() {
        _sadCount.value = (_sadCount.value ?: 0) + 1
        saveMood()
        updateProgress()
    }

    fun onHappyClick() {
        _happyCount.value = (_happyCount.value ?: 0) + 1
        saveMood()
        updateProgress()
    }

    private fun saveMood() {
        val currentDate = getCurrentDate()
        prefsManager.saveMoodCounts(
            date = currentDate,
            happyCount = _happyCount.value,
            sadCount = _sadCount.value
        )
        prefsManager.saveLastRecordDate(currentDate)
        updateStreak()
    }

    private fun updateProgress() {
        val happy = _happyCount.value
        val sad = _sadCount.value
        val total = happy + sad

        _progressPercent.value = if (total > 0) {
            (sad.toFloat() / total * 100).toInt()
        } else {
            50
        }
        updateStatistics()
    }

    fun getMoodForDate(date: String): Pair<Int, Int> {
        return prefsManager.getMoodCounts(date)
    }

    fun getAllMoods(): Map<String, Pair<Int, Int>> {
        return prefsManager.getAllDates().associateWith { date ->
            prefsManager.getMoodCounts(date)
        }
    }

    private fun getCurrentDate(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    private fun updateStreak() {
        _currentStreak.value = prefsManager.calculateStreak()
    }

    private fun setupDailyReset() {
        resetJob?.cancel()
        resetJob = viewModelScope.launch {
            while (isActive) {
                val now = Calendar.getInstance()
                val tomorrow = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                // 计算到明天0点的延迟时间
                val delayMillis = tomorrow.timeInMillis - now.timeInMillis

                // 等待到明天0点
                delay(delayMillis)

                // 只重置显示的数据
                resetDisplayCounts()
            }
        }
    }

    private fun resetDisplayCounts() {
        _happyCount.value = 0
        _sadCount.value = 0
        updateProgress()
    }

    override fun onCleared() {
        super.onCleared()
        resetJob?.cancel()
    }

    // 应用进入前台时检查是否需要重置显示数据
    fun checkAndResetIfNeeded() {
        val currentDate = getCurrentDate()
        val lastRecordDate = prefsManager.getLastRecordDate()

        if (currentDate != lastRecordDate) {
            resetDisplayCounts()
            prefsManager.saveLastRecordDate(currentDate)
        }
    }

    fun getWeeklyMoodData(): List<Pair<Int, Int>> {
        val calendar = Calendar.getInstance()
        val today = calendar.time
        val weekData = mutableListOf<Pair<Int, Int>>()

        // 获取过去7天的数据
        for (i in 6 downTo 0) {
            calendar.time = today
            calendar.add(Calendar.DAY_OF_YEAR, -i)

            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
            val (happy, sad) = getMoodForDate(date)
            weekData.add(Pair(happy, sad))
        }

        return weekData
    }


}
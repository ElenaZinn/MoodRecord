package com.example.emorecord.viewmodel

import android.app.Application
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.emorecord.MoodData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter

class CalendarViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val sharedPreferences = application.getSharedPreferences(
        "mood_records",
        Context.MODE_PRIVATE
    )

    private val _selectedDateMood = MutableStateFlow<MoodData?>(null)
    val selectedDateMood: StateFlow<MoodData?> = _selectedDateMood.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var lastLoadedDate: Triple<Int, Int, Int>? = null

    @RequiresApi(Build.VERSION_CODES.O)
    fun loadMoodForDate(year: Int, month: Int, day: Int) {
        lastLoadedDate = Triple(year, month, day)
        viewModelScope.launch {
            try {
                val date = LocalDate.of(year, month , day)
                val dateString = formatDate(date)

                // 从SharedPreferences获取数据
                val moodString = sharedPreferences.getString(dateString, null)
                val moodData = if (moodString != null) {
                    // 解析存储的数据
                    val (happy, sad) = moodString.split(",").map { it.toInt() }
                    MoodData(date, happy, sad)
                } else {
                    MoodData(date, 0, 0)
                }

                _selectedDateMood.value = moodData
            } catch (e: Exception) {
                _errorMessage.value = "加载数据失败: ${e.localizedMessage}"
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun hasMoodRecord(date: LocalDate): Boolean {
        val dateString = formatDate(date)
        return sharedPreferences.contains(dateString)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun retryLastOperation() {
        lastLoadedDate?.let { (year, month, day) ->
            loadMoodForDate(year, month, day)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun saveMoodForDate(date: LocalDate, happyCount: Int, sadCount: Int) {
        viewModelScope.launch {
            try {
                val dateString = formatDate(date)
                // 将数据存储为字符串
                val moodString = "$happyCount,$sadCount"
                sharedPreferences.edit().putString(dateString, moodString).apply()

                // 更新当前显示的数据
                if (date == selectedDateMood.value?.date) {
                    _selectedDateMood.value = MoodData(date, happyCount, sadCount)
                }
            } catch (e: Exception) {
                _errorMessage.value = "保存数据失败: ${e.localizedMessage}"
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun formatDate(date: LocalDate): String {
        return date.format(DateTimeFormatter.ISO_LOCAL_DATE)
    }

    // 清除所有数据的方法（如果需要）
    fun clearAllData() {
        sharedPreferences.edit().clear().apply()
        _selectedDateMood.value = null
    }

    // 获取所有记录的日期（如果需要）
    @RequiresApi(Build.VERSION_CODES.O)
    fun getAllRecordedDates(): List<LocalDate> {
        return sharedPreferences.all.keys.mapNotNull { dateString ->
            try {
                LocalDate.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE)
            } catch (e: Exception) {
                null
            }
        }
    }
}

// MoodData.kt (保持不变)
data class MoodData(
    val date: LocalDate,
    val happyCount: Int,
    val sadCount: Int
) {
    val totalCount: Int
        get() = happyCount + sadCount

    val dominantMood: MoodType
        get() = when {
            happyCount > sadCount -> MoodType.HAPPY
            sadCount > happyCount -> MoodType.SAD
            else -> MoodType.NEUTRAL
        }
}

enum class MoodType {
    HAPPY,
    SAD,
    NEUTRAL
}

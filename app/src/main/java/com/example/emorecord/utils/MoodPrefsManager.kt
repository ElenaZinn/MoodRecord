package com.example.emorecord.utils

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MoodPrefsManager(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun saveMoodCounts(date: String, happyCount: Int, sadCount: Int) {
        prefs.edit().apply {
            putInt("${HAPPY_KEY}_$date", happyCount)
            putInt("${SAD_KEY}_$date", sadCount)
            apply()
        }
    }

    fun getMoodCounts(date: String): Pair<Int, Int> {
        val happyCount = prefs.getInt("${HAPPY_KEY}_$date", 0)
        val sadCount = prefs.getInt("${SAD_KEY}_$date", 0)
        return Pair(happyCount, sadCount)
    }

    companion object {
        private const val PREFS_NAME = "mood_prefs"
        private const val HAPPY_KEY = "happy"
        private const val SAD_KEY = "sad"
        private const val LAST_RECORD_DATE = "last_record_date"
    }

    fun calculateStreak(): Int {
        return try {
            calculateStreakInternal()
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }

    private fun calculateStreakInternal(): Int {
        // 获取所有记录的日期并解析成 Date 对象
        val allDates = getAllDates()
            .mapNotNull { dateStr -> parseDate(dateStr) }
            .sortedDescending()

        if (allDates.isEmpty()) return 0

        // 获取今天的日期（去除时分秒）
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time

        // 获取昨天的日期
        val yesterday = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time

        // 检查最近的记录是否是今天或昨天
        val mostRecent = allDates.first()
        if (mostRecent.before(yesterday)) {
            return 0
        }

        var streak = 1
        var currentDate = if (mostRecent == today) yesterday else today

        // 计算连续天数
        for (date in allDates) {
            if (date == currentDate) {
                streak++
                currentDate = Calendar.getInstance().apply {
                    time = currentDate
                    add(Calendar.DAY_OF_YEAR, -1)
                }.time
            } else if (date.before(currentDate)) {
                break
            }
        }

        return streak
    }

    private fun parseDate(dateStr: String): Date? {
        return try {
            dateFormat.parse(dateStr)
        } catch (e: Exception) {
            null
        }
    }

    fun getAllDates(): Set<String> {
        return prefs.all.keys
            .filter { it.startsWith(HAPPY_KEY) }
            .map { it.removePrefix("${HAPPY_KEY}_") }
            .toSet()
    }

    private fun formatDate(date: Date): String {
        return dateFormat.format(date)
    }

    fun saveLastRecordDate(date: String) {
        prefs.edit().putString(LAST_RECORD_DATE, date).apply()
    }

    fun getLastRecordDate(): String {
        return prefs.getString(LAST_RECORD_DATE, dateFormat.format(Date())) ?: dateFormat.format(Date())
    }


}

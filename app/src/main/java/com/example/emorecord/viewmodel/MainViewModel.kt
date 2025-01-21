package com.example.emorecord.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.emorecord.R

// MainViewModel.kt
class MainViewModel : ViewModel() {
    private val _currentNavigation = MutableLiveData<Int>()
    val currentNavigation: LiveData<Int> = _currentNavigation

    init {
        // 设置初始导航项
        _currentNavigation.value = R.id.navigation_mood
    }

    fun updateNavigation(navigationId: Int) {
        _currentNavigation.value = navigationId
    }
}
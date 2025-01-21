package com.example.emorecord

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.emorecord.R
import com.example.emorecord.view.EnableNfcWriteDialog
import com.example.emorecord.viewmodel.MainViewModel
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import com.example.emorecord.databinding.ActivityMainBinding

// MainActivity.kt
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var nfcWriteDialog: EnableNfcWriteDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        setupBottomNavigation()
        setupNavigation()
    }

    private fun setupBottomNavigation() {
        binding.bottomNav.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navigation_mood -> {
                    viewModel.updateNavigation(R.id.navigation_mood)
                    true
                }
                R.id.navigation_calendar -> {
                    viewModel.updateNavigation(R.id.navigation_calendar)
                    true
                }
                R.id.navigation_nfc -> {
                    viewModel.updateNavigation(R.id.navigation_nfc)
                    true
                }
                R.id.navigation_statistics -> {
                    viewModel.updateNavigation(R.id.navigation_statistics)
                    true
                }
                else -> false
            }
        }
    }

    private fun setupNavigation() {
        viewModel.currentNavigation.observe(this) { navigationId ->
            val fragment = when(navigationId) {
                R.id.navigation_mood -> MoodFragment()
                R.id.navigation_calendar -> CalendarFragment()
                R.id.navigation_nfc -> NfcFragment()
                R.id.navigation_statistics -> StatisticsFragment()
                else -> MoodFragment()
            }
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (::nfcWriteDialog.isInitialized && nfcWriteDialog.isShowing) {
            nfcWriteDialog.onNewIntent(intent)
        }
    }
}

package com.example.emorecord

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.example.emorecord.viewmodel.MoodViewModel

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                TODO("Not yet implemented")
            }

            override fun onActivityStarted(activity: Activity) {
                TODO("Not yet implemented")
            }

            override fun onActivityResumed(activity: Activity) {
                // 如果是 MainActivity，检查是否需要重置显示数据
                if (activity is MainActivity) {
                    val viewModel = ViewModelProvider(activity)[MoodViewModel::class.java]
                    viewModel.checkAndResetIfNeeded()
                }
            }

            override fun onActivityPaused(activity: Activity) {
            }

            override fun onActivityStopped(activity: Activity) {
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
            }

            override fun onActivityDestroyed(activity: Activity) {

            }

        })
    }
}

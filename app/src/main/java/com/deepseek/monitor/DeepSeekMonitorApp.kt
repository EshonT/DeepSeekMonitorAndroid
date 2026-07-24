@file:Suppress("DEPRECATION")

package com.deepseek.monitor

import android.app.Application
import com.deepseek.monitor.background.NotificationHelper
import dagger.hilt.android.HiltAndroidApp

/**
 * DeepSeek Monitor Android 应用入口。
 * Hilt 依赖注入容器在此初始化。
 */
@HiltAndroidApp
class DeepSeekMonitorApp : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createChannel(this)
    }
}

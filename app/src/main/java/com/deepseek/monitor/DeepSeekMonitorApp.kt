@file:Suppress("DEPRECATION")

package com.deepseek.monitor

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.deepseek.monitor.background.NotificationHelper
import com.deepseek.monitor.background.RefreshScheduler
import com.deepseek.monitor.data.local.datastore.ConfigDataStore
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * DeepSeek Monitor Android 应用入口。
 * Hilt 依赖注入容器在此初始化。
 *
 * 实现 [Configuration.Provider] 以接入 [HiltWorkerFactory]，
 * 使 [androidx.hilt.work.HiltWorker] 注解的 Worker 能通过 Hilt 注入依赖。
 */
@HiltAndroidApp
class DeepSeekMonitorApp : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var configDataStore: ConfigDataStore
    @Inject lateinit var refreshScheduler: RefreshScheduler

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createChannel(this)

        // 防御性恢复定时刷新（WorkManager 持久化任务不会丢失，此处作为双保险）
        appScope.launch {
            val enabled = configDataStore.autoRefreshFlow.first()
            if (enabled) {
                val interval = configDataStore.refreshIntervalFlow.first()
                refreshScheduler.schedule(interval)
            }
        }
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}

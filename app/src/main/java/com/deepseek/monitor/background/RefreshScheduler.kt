package com.deepseek.monitor.background

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * WorkManager 调度器。
 * 负责启动和停止后台周期性刷新任务。
 */
@Singleton
class RefreshScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val WORK_NAME = "deepseek_periodic_refresh"
    }

    /**
     * 启动周期性刷新。
     *
     * @param intervalSeconds 间隔: 60/300/1800/3600 秒。
     *   WorkManager 系统限制最小间隔 15 分钟——短于 15 分钟的配置实际执行间隔由系统自行调节。
     */
    fun schedule(intervalSeconds: Int) {
        val interval = intervalSeconds.coerceAtLeast(15 * 60) // 最低 15 分
        val workRequest = PeriodicWorkRequestBuilder<RefreshWorker>(
            interval.toLong(), TimeUnit.SECONDS
        ).setConstraints(
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
        ).build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }

    /**
     * 取消周期性刷新。
     */
    fun cancel() {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }
}

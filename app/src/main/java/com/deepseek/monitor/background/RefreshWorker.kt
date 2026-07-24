package com.deepseek.monitor.background

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.deepseek.monitor.domain.usecase.RefreshAllUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * 后台周期性刷新 Worker。
 *
 * 由 WorkManager [androidx.work.PeriodicWorkRequest] 调度，
 * 调用 [RefreshAllUseCase] 拉取最新余额和用量数据。
 *
 * 结果通过 [NotificationHelper] 通知用户。
 */
@HiltWorker
class RefreshWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val refreshAllUseCase: RefreshAllUseCase
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            Log.d(TAG, "后台刷新开始...")
            val result = refreshAllUseCase()

            // 余额不足通知
            val balance = result.balance
            if (balance != null) {
                val total = balance.totalBalance.toDoubleOrNull() ?: 0.0
                if (total < 10.0) {
                    NotificationHelper.showLowBalance(
                        applicationContext,
                        total,
                        balance.currency
                    )
                }
            }

            Log.d(TAG, "后台刷新完成: balance=${result.balance != null}, usage=${result.usage != null}")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "后台刷新失败: ${e.message}", e)
            Result.retry()
        }
    }

    companion object {
        private const val TAG = "RefreshWorker"
    }
}

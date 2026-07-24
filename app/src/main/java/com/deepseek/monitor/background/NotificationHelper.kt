package com.deepseek.monitor.background

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

/**
 * 通知工具类。
 *
 * 处理后台刷新的通知提醒：余额不足、刷新结果等。
 */
object NotificationHelper {

    private const val CHANNEL_ID = "deepseek_monitor"
    private const val CHANNEL_NAME = "DeepSeek Monitor 提醒"
    private const val NOTIFICATION_ID_BALANCE = 1001

    /**
     * 创建通知渠道（Android 8.0+ 必需）。
     * 在 Application.onCreate() 中调用一次。
     */
    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "余额不足提醒与用量同步通知"
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    /**
     * 显示余额不足通知。
     */
    fun showLowBalance(context: Context, balance: Double, currency: String) {
        // 检查通知权限（Android 13+）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return  // 未授权，静默跳过
            }
        }

        // 点击通知打开主 Activity
        val intent = context.packageManager
            .getLaunchIntentForPackage(context.packageName)
            ?.apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP }

        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("DeepSeek 余额不足")
            .setContentText("当前余额 $balance $currency，请及时充值")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context)
            .notify(NOTIFICATION_ID_BALANCE, notification)
    }
}

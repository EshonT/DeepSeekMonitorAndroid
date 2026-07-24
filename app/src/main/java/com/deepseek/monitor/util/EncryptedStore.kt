package com.deepseek.monitor.util

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * 加密存储封装。
 * 使用 Android KeyStore + AES-256-GCM 加密 SharedPreferences，
 * 保护 API Key、用量 Token 等敏感数据。
 */
object EncryptedStore {

    private const val PREF_FILE_NAME = "deepseek_monitor_secure_prefs"

    /**
     * 创建加密 SharedPreferences 实例。
     * 密钥由 Android KeyStore 硬件安全模块（TEE / StrongBox）保护。
     */
    fun create(context: Context): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return EncryptedSharedPreferences.create(
            context,
            PREF_FILE_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
}

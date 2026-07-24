package com.deepseek.monitor.data.local.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.deepseek.monitor.util.EncryptedStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/** DataStore 实例扩展（单例） */
private val Context.dataStore by preferencesDataStore(name = "config")

/**
 * 应用配置存储。
 *
 * | 数据项            | 存储方式                                  | 安全性      |
 * |-------------------|-------------------------------------------|-------------|
 * | API Key           | EncryptedSharedPreferences（KeyStore AES） | 加密        |
 * | 用量 Token        | EncryptedSharedPreferences（KeyStore AES） | 加密        |
 * | API Key 预览      | EncryptedSharedPreferences                 | 加密        |
 * | 主题模式          | DataStore Preferences                      | 非敏感      |
 * | 刷新间隔          | DataStore Preferences                      | 非敏感      |
 * | 自动刷新开关      | DataStore Preferences                      | 非敏感      |
 * | 设备类型          | DataStore Preferences                      | 非敏感      |
 */
@Singleton
class ConfigDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    /** 加密存储（API Key、用量 Token） */
    private val securePrefs = EncryptedStore.create(context)

    // ── DataStore Keys ──
    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")          // "light" | "dark" | "eink"
        val REFRESH_INTERVAL = intPreferencesKey("refresh_interval") // 60/300/1800/3600 秒
        val AUTO_REFRESH = booleanPreferencesKey("auto_refresh")
        val DEVICE_TYPE = stringPreferencesKey("device_type")
    }

    // ── 非敏感 Flow ──
    val themeModeFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.THEME_MODE] ?: "light"
    }

    val refreshIntervalFlow: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[Keys.REFRESH_INTERVAL] ?: 60
    }

    val autoRefreshFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.AUTO_REFRESH] ?: false
    }

    // ── 敏感 Flow（加密存储） ──
    val apiKeyFlow: Flow<String?> = flowFromSecure("api_key")
    val apiKeyPreviewFlow: Flow<String?> = flowFromSecure("api_key_preview")
    val usageTokenFlow: Flow<String?> = flowFromSecure("usage_token")

    // ── 配置读写 ──

    suspend fun setThemeMode(mode: String) {
        context.dataStore.updateData { prefs ->
            prefs.toMutablePreferences().apply {
                set(Keys.THEME_MODE, mode)
            }
        }
    }

    suspend fun setRefreshInterval(seconds: Int) {
        val normalized = when (seconds) {
            60, 300, 1800, 3600 -> seconds
            else -> 60
        }
        context.dataStore.updateData { prefs ->
            prefs.toMutablePreferences().apply {
                set(Keys.REFRESH_INTERVAL, normalized)
            }
        }
    }

    suspend fun setAutoRefresh(enabled: Boolean) {
        context.dataStore.updateData { prefs ->
            prefs.toMutablePreferences().apply {
                set(Keys.AUTO_REFRESH, enabled)
            }
        }
    }

    // ── 敏感数据读写（加密） ──

    fun saveApiKey(apiKey: String) {
        securePrefs.edit()
            .putString("api_key", apiKey)
            .putString("api_key_preview", maskApiKey(apiKey))
            .commit()  // 同步写入，确保 AuthInterceptor 立即可读
    }

    fun clearApiKey() {
        securePrefs.edit()
            .remove("api_key")
            .remove("api_key_preview")
            .commit()
    }

    fun saveUsageToken(token: String) {
        securePrefs.edit().putString("usage_token", token).commit()
    }

    fun clearUsageToken() {
        securePrefs.edit().remove("usage_token").commit()
    }

    // ── 同步读取（供 Interceptor 使用） ──

    fun getApiKeySync(): String? = securePrefs.getString("api_key", null)
    fun getUsageTokenSync(): String? = securePrefs.getString("usage_token", null)

    // ── 内部方法 ──

    /**
     * 从加密存储读取并转为 Flow。
     * 注意：EncryptedSharedPreferences 不支持 Flow，这里提供轮询访问方式。
     */
    private fun flowFromSecure(key: String): Flow<String?> {
        // EncryptedSharedPreferences 本身不支持 Flow 监听，
        // 此处 flow 仅提供初始值。变更由 ViewModel 层 push 触发。
        return kotlinx.coroutines.flow.flow {
            emit(securePrefs.getString(key, null))
        }
    }

    /**
     * API Key 预览脱敏：展示前7位 + "..." + 后4位。
     * 与 Windows 版 api_key_preview 逻辑一致。
     */
    private fun maskApiKey(key: String): String {
        if (key.length <= 12) return "已保存"
        val start = key.take(7)
        val end = key.takeLast(4)
        return "$start...$end"
    }
}

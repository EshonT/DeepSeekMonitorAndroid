package com.deepseek.monitor.data.repository

import com.deepseek.monitor.data.local.datastore.ConfigDataStore
import com.deepseek.monitor.domain.model.AppConfig
import com.deepseek.monitor.domain.repository.ConfigRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

/**
 * 配置数据仓库实现。
 */
class ConfigRepositoryImpl @Inject constructor(
    private val configDataStore: ConfigDataStore
) : ConfigRepository {

    override val config: Flow<AppConfig> = combine(
        combine(
            configDataStore.apiKeyFlow,
            configDataStore.apiKeyPreviewFlow,
            configDataStore.usageTokenFlow,
            configDataStore.themeModeFlow,
            configDataStore.refreshIntervalFlow,
        ) { apiKey, preview, usageToken, themeMode, interval ->
            arrayOf(apiKey, preview, usageToken, themeMode, interval)
        },
        configDataStore.autoRefreshFlow,
    ) { arr, autoRefresh ->
        @Suppress("UNCHECKED_CAST")
        val apiKey = arr[0] as String?
        val preview = arr[1] as String?
        val usageToken = arr[2] as String?
        val themeMode = arr[3] as String
        val interval = arr[4] as Int
        AppConfig(
            apiKeyConfigured = !apiKey.isNullOrEmpty(),
            apiKeyPreview = preview,
            usageTokenConfigured = !usageToken.isNullOrEmpty(),
            themeMode = themeMode,
            refreshIntervalSeconds = interval,
            autoRefreshEnabled = autoRefresh
        )
    }

    override suspend fun saveApiKey(apiKey: String) {
        configDataStore.saveApiKey(apiKey)
    }

    override suspend fun clearApiKey() {
        configDataStore.clearApiKey()
    }

    override suspend fun saveUsageToken(token: String) {
        configDataStore.saveUsageToken(token)
    }

    override suspend fun clearUsageToken() {
        configDataStore.clearUsageToken()
    }

    override suspend fun setThemeMode(mode: String) {
        configDataStore.setThemeMode(mode)
    }

    override suspend fun setRefreshInterval(seconds: Int) {
        configDataStore.setRefreshInterval(seconds)
    }

    override suspend fun setAutoRefreshEnabled(enabled: Boolean) {
        configDataStore.setAutoRefresh(enabled)
    }
}

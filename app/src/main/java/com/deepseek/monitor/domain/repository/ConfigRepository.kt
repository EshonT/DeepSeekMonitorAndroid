package com.deepseek.monitor.domain.repository

import com.deepseek.monitor.domain.model.AppConfig
import kotlinx.coroutines.flow.Flow

/**
 * 配置数据仓库接口。
 */
interface ConfigRepository {

    /** 获取配置聚合模型的 Flow */
    val config: Flow<AppConfig>

    // ── API Key ──

    suspend fun saveApiKey(apiKey: String)
    suspend fun clearApiKey()

    // ── 用量 Token ──

    suspend fun saveUsageToken(token: String)
    suspend fun clearUsageToken()

    // ── 主题 ──

    suspend fun setThemeMode(mode: String)

    // ── 刷新配置 ──

    suspend fun setRefreshInterval(seconds: Int)
    suspend fun setAutoRefreshEnabled(enabled: Boolean)
}

package com.deepseek.monitor.domain.model

/**
 * 应用配置聚合模型。
 * 由 [com.deepseek.monitor.data.local.datastore.ConfigDataStore] 映射而来，
 * 用于 ViewModel 与 UI 层传递。
 */
data class AppConfig(
    /** API Key 是否已配置 */
    val apiKeyConfigured: Boolean,

    /** API Key 脱敏预览（前7位...后4位） */
    val apiKeyPreview: String?,

    /** 用量 Token 是否已配置 */
    val usageTokenConfigured: Boolean,

    /** 自动刷新间隔（秒）：60/300/1800/3600 */
    val refreshIntervalSeconds: Int,

    /** 自动刷新是否启用 */
    val autoRefreshEnabled: Boolean
)

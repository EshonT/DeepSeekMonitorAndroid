package com.deepseek.monitor.domain.usecase

import com.deepseek.monitor.data.local.datastore.ConfigDataStore
import com.deepseek.monitor.domain.repository.ConfigRepository
import com.deepseek.monitor.domain.repository.UsageRepository
import java.time.LocalDate
import javax.inject.Inject

/**
 * 保存并验证用量 Token 用例。
 *
 * 策略：
 * 1. 备份旧 Token
 * 2. 静默写入新 Token（不推送 Flow）→ AuthInterceptor 自动注入
 * 3. 调 getUsage() 验证
 * 4. 成功 → 推送 Flow，触发 UI 刷新
 * 5. 失败 → 恢复旧 Token，保证已配置的有效凭据不被覆盖
 */
class SaveUsageTokenUseCase @Inject constructor(
    private val configDataStore: ConfigDataStore,
    private val configRepository: ConfigRepository,
    private val usageRepository: UsageRepository
) {
    suspend operator fun invoke(token: String) {
        // 备份旧 Token
        val oldToken = configDataStore.getUsageTokenSync()

        // 静默写入新 Token（不推流，不触发 Dashboard 刷新）
        configRepository.saveUsageTokenSilent(token)

        try {
            val now = LocalDate.now()
            usageRepository.getUsage(now.monthValue, now.year)
            // 验证通过 → 推送 Flow，触发 Dashboard 刷新
            configRepository.notifyUsageTokenChanged()
        } catch (e: Exception) {
            // 验证失败：恢复旧 Token（如果有），否则清除
            if (!oldToken.isNullOrEmpty()) {
                configRepository.saveUsageTokenSilent(oldToken)
            } else {
                configRepository.clearUsageToken()
            }
            throw IllegalStateException(
                "用量 Token 验证失败: ${e.message ?: "未知错误"}"
            )
        }
    }
}

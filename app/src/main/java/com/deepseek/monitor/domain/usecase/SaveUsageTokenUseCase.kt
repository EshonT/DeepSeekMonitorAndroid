package com.deepseek.monitor.domain.usecase

import com.deepseek.monitor.data.local.datastore.ConfigDataStore
import com.deepseek.monitor.domain.repository.UsageRepository
import java.time.LocalDate
import javax.inject.Inject

/**
 * 保存并验证用量 Token 用例。
 *
 * 策略：
 * 1. 备份旧 Token
 * 2. 存储新 Token → AuthInterceptor 自动注入
 * 3. 调 getUsage() 验证
 * 4. 失败则恢复旧 Token，保证已配置的有效凭据不被覆盖
 */
class SaveUsageTokenUseCase @Inject constructor(
    private val configDataStore: ConfigDataStore,
    private val usageRepository: UsageRepository
) {
    suspend operator fun invoke(token: String) {
        // 备份旧 Token
        val oldToken = configDataStore.getUsageTokenSync()

        // 存入新 Token
        configDataStore.saveUsageToken(token)

        try {
            val now = LocalDate.now()
            usageRepository.getUsage(now.monthValue, now.year)
        } catch (e: Exception) {
            // 验证失败：恢复旧 Token（如果有），否则清除
            if (!oldToken.isNullOrEmpty()) {
                configDataStore.saveUsageToken(oldToken)
            } else {
                configDataStore.clearUsageToken()
            }
            throw IllegalStateException(
                "用量 Token 验证失败: ${e.message ?: "未知错误"}"
            )
        }
    }
}

package com.deepseek.monitor.domain.usecase

import com.deepseek.monitor.domain.model.ApiKeyInvalidException
import com.deepseek.monitor.domain.repository.BalanceRepository
import com.deepseek.monitor.domain.repository.ConfigRepository
import javax.inject.Inject

/**
 * 保存并验证 API Key 用例。
 *
 * 1. 加密存储 API Key
 * 2. 调用余额接口验证 Key 有效性
 * 3. 验证失败时回滚（不删除已存储的 Key，仅标记为未验证）
 */
class SaveApiKeyUseCase @Inject constructor(
    private val configRepository: ConfigRepository,
    private val balanceRepository: BalanceRepository
) {
    /**
     * @param apiKey 用户输入的 API Key
     * @throws ApiKeyInvalidException 当 Key 无效时
     */
    suspend operator fun invoke(apiKey: String) {
        // 先加密存储
        configRepository.saveApiKey(apiKey)

        // 验证有效性（抛出异常则外层 UI 提示用户）
        balanceRepository.getBalance()
    }
}

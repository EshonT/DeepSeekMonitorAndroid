package com.deepseek.monitor.data.repository

import com.deepseek.monitor.data.local.datastore.ConfigDataStore
import com.deepseek.monitor.data.remote.api.DeepSeekApiService
import com.deepseek.monitor.domain.model.Balance
import com.deepseek.monitor.domain.model.CredentialNotConfiguredException
import com.deepseek.monitor.domain.repository.BalanceRepository
import javax.inject.Inject

/**
 * 余额数据仓库实现。
 * 通过 [DeepSeekApiService] 调用 DeepSeek 官方余额接口。
 */
class BalanceRepositoryImpl @Inject constructor(
    private val apiService: DeepSeekApiService,
    private val configDataStore: ConfigDataStore
) : BalanceRepository {

    override suspend fun getBalance(): Balance {
        // 检查 API Key 是否已配置
        val apiKey = configDataStore.getApiKeySync()
        if (apiKey.isNullOrEmpty()) {
            throw CredentialNotConfiguredException("API Key")
        }

        val response = apiService.getBalance()

        val info = response.balanceInfos.firstOrNull()
            ?: throw IllegalStateException("余额信息为空")

        return Balance(
            isAvailable = response.isAvailable,
            currency = info.currency,
            totalBalance = info.totalBalance,
            grantedBalance = info.grantedBalance,
            toppedUpBalance = info.toppedUpBalance
        )
    }
}

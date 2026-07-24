package com.deepseek.monitor.domain.usecase

import com.deepseek.monitor.domain.model.Balance
import com.deepseek.monitor.domain.model.UsageResult
import javax.inject.Inject

/**
 * 全量刷新用例。
 * 余额和用量独立获取：一方失败不影响另一方。
 */
class RefreshAllUseCase @Inject constructor(
    private val getBalanceUseCase: GetBalanceUseCase,
    private val getUsageUseCase: GetUsageUseCase
) {
    data class RefreshResult(
        val balance: Balance? = null,
        val usage: UsageResult? = null,
        val balanceError: String? = null,
        val usageError: String? = null
    )

    suspend operator fun invoke(): RefreshResult {
        var balance: Balance? = null
        var balanceError: String? = null
        var usage: UsageResult? = null
        var usageError: String? = null

        // 余额
        try {
            balance = getBalanceUseCase()
        } catch (e: Exception) {
            balanceError = e.message ?: "余额查询失败"
        }

        // 用量
        try {
            usage = getUsageUseCase()
        } catch (e: Exception) {
            usageError = e.message ?: "用量查询失败"
        }

        return RefreshResult(balance, usage, balanceError, usageError)
    }
}

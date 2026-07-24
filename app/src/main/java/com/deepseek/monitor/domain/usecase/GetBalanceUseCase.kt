package com.deepseek.monitor.domain.usecase

import com.deepseek.monitor.domain.model.Balance
import com.deepseek.monitor.domain.repository.BalanceRepository
import javax.inject.Inject

/**
 * 获取账户余额用例。
 */
class GetBalanceUseCase @Inject constructor(
    private val balanceRepository: BalanceRepository
) {
    suspend operator fun invoke(): Balance {
        return balanceRepository.getBalance()
    }
}

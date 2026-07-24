package com.deepseek.monitor.domain.model

/**
 * 账户余额领域模型。
 * 由 [BalanceResponseDto] 映射而来。
 */
data class Balance(
    val isAvailable: Boolean,

    /** "CNY" | "USD" */
    val currency: String,

    /** 总余额 */
    val totalBalance: String,

    /** 赠送余额 */
    val grantedBalance: String,

    /** 充值余额 */
    val toppedUpBalance: String
)

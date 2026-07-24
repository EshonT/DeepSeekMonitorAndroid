package com.deepseek.monitor.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * 余额查询响应。
 *
 * API: GET https://api.deepseek.com/user/balance
 * 示例:
 * {
 *   "is_available": true,
 *   "balance_infos": [{
 *     "currency": "CNY",
 *     "total_balance": "98.50",
 *     "granted_balance": "10.00",
 *     "topped_up_balance": "88.50"
 *   }]
 * }
 */
data class BalanceResponseDto(
    @SerializedName("is_available")
    val isAvailable: Boolean,

    @SerializedName("balance_infos")
    val balanceInfos: List<BalanceInfoDto>
)

data class BalanceInfoDto(
    val currency: String,

    @SerializedName("total_balance")
    val totalBalance: String,

    @SerializedName("granted_balance")
    val grantedBalance: String,

    @SerializedName("topped_up_balance")
    val toppedUpBalance: String
)

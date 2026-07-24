package com.deepseek.monitor.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * 费用查询响应。
 *
 * API: GET https://platform.deepseek.com/api/v0/usage/cost?month={m}&year={y}
 *
 * 响应结构与用量接口类似但 biz_data 是 List:
 * {
 *   "data": {
 *     "biz_data": [{
 *       "total": [ ... ],
 *       "days": [ ... ]
 *     }]
 *   }
 * }
 */
data class UsageCostResponseDto(
    val data: UsageCostDataDto
)

data class UsageCostDataDto(
    @SerializedName("biz_data")
    val bizData: List<UsageCostBizDto>
)

data class UsageCostBizDto(
    val total: List<ModelUsageDto>,
    val days: List<DayUsageDto>
)

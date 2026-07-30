package com.deepseek.monitor.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * 按 API Key 拆分的每日费用响应。
 *
 * 接口：GET /api/v0/usage/by_api_key/cost?month=&year=
 * 按 API Key + 模型维度返回每日费用 buckets。
 *
 * 响应 JSON 结构：
 * ```json
 * {
 *   "code": 0, "msg": "",
 *   "data": {
 *     "biz_code": 0, "biz_msg": "",
 *     "biz_data": {
 *       "models": ["deepseek-v4-flash", "deepseek-v4-pro"],
 *       "data": [{
 *         "currency": "CNY",
 *         "series": [{
 *           "api_key": {...},
 *           "model": "deepseek-v4-flash",
 *           "buckets": [{"time": 1784851200, "cost": "0.64428484"}]
 *         }]
 *       }]
 *     }
 *   }
 * }
 * ```
 */
data class ByApiKeyCostResponseDto(
    val code: Int,
    val msg: String,
    val data: ByApiKeyCostDataDto?
)

data class ByApiKeyCostDataDto(
    @SerializedName("biz_code") val bizCode: Int,
    @SerializedName("biz_msg") val bizMsg: String,
    @SerializedName("biz_data") val bizData: ByApiKeyCostBizDataDto?
)

data class ByApiKeyCostBizDataDto(
    val models: List<String>?,
    val data: List<ByApiKeyCostCurrencyDto>?
)

data class ByApiKeyCostCurrencyDto(
    val currency: String?,
    val series: List<ByApiKeyCostSeriesDto>?
)

data class ByApiKeyCostSeriesDto(
    @SerializedName("api_key") val apiKey: ApiKeyInfoDto?,
    val model: String?,
    val buckets: List<ByApiKeyCostBucketDto>?
)

data class ApiKeyInfoDto(
    @SerializedName("tracking_id") val trackingId: String?,
    val name: String?,
    @SerializedName("sensitive_id") val sensitiveId: String?,
    val valid: Boolean?
)

data class ByApiKeyCostBucketDto(
    /** Unix 时间戳（秒） */
    val time: Long,
    /** 费用金额字符串 */
    val cost: String?
)

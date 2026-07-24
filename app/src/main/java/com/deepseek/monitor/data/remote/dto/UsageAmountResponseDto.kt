package com.deepseek.monitor.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * 用量查询响应。
 *
 * API: GET https://platform.deepseek.com/api/v0/usage/amount?month={m}&year={y}
 *
 * 响应结构（与 Windows 版 lib.rs 反序列化结构完全对齐）:
 * {
 *   "data": {
 *     "biz_data": {
 *       "total": [{ "model": "deepseek-v4-flash", "usage": [...] }, ...],
 *       "days": [{ "date": "2026-07-24", "data": [ ... ] }, ...]
 *     }
 *   }
 * }
 */
data class UsageAmountResponseDto(
    val data: UsageAmountDataDto
)

data class UsageAmountDataDto(
    @SerializedName("biz_data")
    val bizData: UsageBizDataDto
)

data class UsageBizDataDto(
    val total: List<ModelUsageDto>,
    val days: List<DayUsageDto>
)

data class ModelUsageDto(
    val model: String,
    val usage: List<UsageEntryDto>
)

data class UsageEntryDto(
    /**
     * 用量类型:
     * - "REQUEST"          → 请求次数
     * - "PROMPT_CACHE_HIT_TOKEN"  → 缓存命中 Token
     * - "PROMPT_CACHE_MISS_TOKEN" → 缓存未命中 Token
     * - "RESPONSE_TOKEN"    → 输出 Token
     * - "PROMPT_TOKEN"      → 输入 Token（合并到总 Token）
     */
    @SerializedName("type")
    val kind: String,

    /** 数字字符串，例如 "1234.56" */
    val amount: String
)

data class DayUsageDto(
    /** 日期，格式 "2026-07-24" */
    val date: String,

    /** 该日各模型用量明细 */
    val data: List<ModelUsageDto>
)

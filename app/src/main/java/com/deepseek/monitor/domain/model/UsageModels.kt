package com.deepseek.monitor.domain.model

/**
 * 用量聚合结果。
 * 由 [com.deepseek.monitor.data.remote.dto.UsageAmountResponseDto] +
 * [com.deepseek.monitor.data.remote.dto.UsageCostResponseDto] 合并映射而来。
 */
data class UsageResult(
    /** 各模型用量摘要 */
    val models: List<UsageModel>,

    /** 按日用量明细（最近7天） */
    val days: List<UsageDay>,

    /** 当月总费用（CNY） */
    val monthCost: Double
)

/**
 * 单个模型的用量摘要。
 */
data class UsageModel(
    /** "flash" | "pro" */
    val key: String,

    /** "V4 Flash" | "V4 Pro" */
    val name: String,

    /** 总 Token 数 */
    val totalTokens: Long,

    /** API 请求次数 */
    val requestCount: Long,

    /** 缓存命中 Token 数 */
    val cacheHitTokens: Long,

    /** 缓存未命中 Token 数 */
    val cacheMissTokens: Long,

    /** 输出 Token 数 */
    val responseTokens: Long,

    /** 当月费用（CNY） */
    val cost: Double
)

/**
 * 单日用量明细。
 */
data class UsageDay(
    /** 日期，格式 "2026-07-24" */
    val date: String,

    // ── V4 Flash ──
    val flashTokens: Long,
    val flashCacheHit: Long,
    val flashCacheMiss: Long,
    val flashResponse: Long,

    // ── V4 Pro ──
    val proTokens: Long,
    val proCacheHit: Long,
    val proCacheMiss: Long,
    val proResponse: Long,

    /** 当日总 Token */
    val totalTokens: Long,

    /** 当日总费用 */
    val totalCost: Double
)

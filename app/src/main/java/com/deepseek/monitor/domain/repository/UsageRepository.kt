package com.deepseek.monitor.domain.repository

import com.deepseek.monitor.domain.model.UsageResult

/**
 * 用量数据仓库接口。
 */
interface UsageRepository {

    /**
     * 获取指定月份的用量与费用数据。
     *
     * @param month 月份 (1-12)
     * @param year  年份 (如 2026)
     * @throws TokenInvalidException 当 Token 无效或过期时
     */
    suspend fun getUsage(month: Int, year: Int): UsageResult

    /**
     * 验证用量 Token 是否有效。
     *
     * @return true 表示 Token 有效
     */
    suspend fun verifyUsageToken(token: String, month: Int, year: Int): Boolean
}

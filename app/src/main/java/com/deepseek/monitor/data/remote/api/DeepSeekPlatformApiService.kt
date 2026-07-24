package com.deepseek.monitor.data.remote.api

import com.deepseek.monitor.data.remote.dto.UsageAmountResponseDto
import com.deepseek.monitor.data.remote.dto.UsageCostResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * DeepSeek 平台内部 API 接口（platform.deepseek.com）。
 * 认证方式：Bearer Web Token（通过 WebView 登录获取，非 API Key）。
 *
 * 注意：这些接口非 DeepSeek 官方公开 API，未来可能变更。
 */
interface DeepSeekPlatformApiService {

    /**
     * 查询指定月份的用量明细。
     *
     * @param month 月份 (1-12)
     * @param year  年份 (如 2026)
     */
    @GET("api/v0/usage/amount")
    suspend fun getUsageAmount(
        @Query("month") month: Int,
        @Query("year") year: Int
    ): UsageAmountResponseDto

    /**
     * 查询指定月份的费用明细。
     *
     * @param month 月份 (1-12)
     * @param year  年份 (如 2026)
     */
    @GET("api/v0/usage/cost")
    suspend fun getUsageCost(
        @Query("month") month: Int,
        @Query("year") year: Int
    ): UsageCostResponseDto
}

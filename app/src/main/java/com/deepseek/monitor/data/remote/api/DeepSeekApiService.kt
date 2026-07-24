package com.deepseek.monitor.data.remote.api

import com.deepseek.monitor.data.remote.dto.BalanceResponseDto
import retrofit2.http.GET

/**
 * DeepSeek 官方 API 接口（api.deepseek.com）。
 * 认证方式：Bearer API Key（sk-xxx）。
 */
interface DeepSeekApiService {

    /**
     * 查询账户余额。
     * 这是 DeepSeek 官方提供的唯一账户相关公开接口。
     */
    @GET("user/balance")
    suspend fun getBalance(): BalanceResponseDto
}

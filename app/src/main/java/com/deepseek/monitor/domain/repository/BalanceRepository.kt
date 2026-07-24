package com.deepseek.monitor.domain.repository

import com.deepseek.monitor.domain.model.Balance

/**
 * 余额数据仓库接口。
 */
interface BalanceRepository {

    /**
     * 获取 DeepSeek 账户余额。
     *
     * @throws ApiKeyInvalidException 当 API Key 无效时
     * @throws RateLimitException     当请求过于频繁时
     * @throws ServerException        当服务端错误时
     */
    suspend fun getBalance(): Balance
}

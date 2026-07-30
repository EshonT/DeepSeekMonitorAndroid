package com.deepseek.monitor.data.repository

import android.util.Log
import com.deepseek.monitor.data.local.datastore.ConfigDataStore
import com.deepseek.monitor.data.remote.api.DeepSeekPlatformApiService
import com.deepseek.monitor.data.remote.dto.ByApiKeyCostResponseDto
import com.deepseek.monitor.data.remote.dto.UsageAmountResponseDto
import com.deepseek.monitor.data.remote.dto.UsageEntryDto
import com.deepseek.monitor.domain.model.CredentialNotConfiguredException
import com.deepseek.monitor.domain.model.UsageDay
import com.deepseek.monitor.domain.model.UsageModel
import com.deepseek.monitor.domain.model.UsageResult
import com.deepseek.monitor.domain.repository.UsageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

/**
 * 用量数据仓库实现。
 * 通过 [DeepSeekPlatformApiService] 并发调用用量 + 费用两个接口，
 * 合并响应构建 [UsageResult]。
 *
 * Token 验证、模型识别、用量拆解逻辑与 Windows 版 lib.rs 完全对齐。
 */
class UsageRepositoryImpl @Inject constructor(
    private val platformApi: DeepSeekPlatformApiService,
    private val configDataStore: ConfigDataStore
) : UsageRepository {

    override suspend fun getUsage(month: Int, year: Int): UsageResult = coroutineScope {
        // 检查用量 Token 是否已配置
        val token = configDataStore.getUsageTokenSync()
        if (token.isNullOrEmpty()) {
            throw CredentialNotConfiguredException("用量 Token")
        }

        // 计算当月起始和结束 Unix 时间戳（UTC 秒）
        val startOfMonth = java.time.LocalDate.of(year, month, 1)
        val endOfMonth = startOfMonth.plusMonths(1)
        val startTs = startOfMonth.atStartOfDay(java.time.ZoneOffset.UTC).toEpochSecond()
        val endTs = endOfMonth.atStartOfDay(java.time.ZoneOffset.UTC).toEpochSecond()

        val amountDeferred = async { platformApi.getUsageAmount(month, year) }
        val costDeferred = async { platformApi.getUsageCost(month, year) }
        val costByApiKeyDeferred = async {
            runCatching { platformApi.getUsageCostByApiKey(startTs, endTs) }
        }

        val amount = amountDeferred.await()
        val cost = costDeferred.await()
        val costByApiKeyResult = costByApiKeyDeferred.await()
        val costByApiKey = costByApiKeyResult.getOrNull()
        costByApiKeyResult.exceptionOrNull()?.let {
            Log.w("UsageRepo", "by_api_key/cost 接口失败，回退到总费用", it)
        }
        buildUsageResult(amount, cost, costByApiKey)
    }

    override suspend fun verifyUsageToken(token: String, month: Int, year: Int): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // 新建干净 OkHttpClient：只带平台请求头和超时，不带 AuthInterceptor
                val verifyClient = OkHttpClient.Builder()
                    .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                    .addInterceptor { chain ->
                        val request = chain.request().newBuilder()
                            .header("Authorization", "Bearer $token")
                            .header("x-client-platform", "android")
                            .header("Accept", "*/*")
                            .build()
                        chain.proceed(request)
                    }
                    .build()

                val verifyApi = Retrofit.Builder()
                    .baseUrl("https://platform.deepseek.com/")
                    .client(verifyClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(DeepSeekPlatformApiService::class.java)

                verifyApi.getUsageAmount(month, year)
                true
            } catch (_: Exception) {
                false
            }
        }
    }

    // ── 工具方法（与 Windows 版 lib.rs 完全对齐） ──

    private fun buildUsageResult(
        amount: UsageAmountResponseDto,
        cost: com.deepseek.monitor.data.remote.dto.UsageCostResponseDto,
        costByApiKey: ByApiKeyCostResponseDto?
    ): UsageResult {
        val costTotal = cost.data.bizData.firstOrNull()

        fun costForModel(model: String): Double {
            return costTotal
                ?.total
                ?.find { it.model == model }
                ?.let { costSum(it.usage) }
                ?: 0.0
        }

        val models = amount.data.bizData.total.mapNotNull { modelUsage ->
            val label = when (modelUsage.model) {
                "deepseek-v4-flash" -> "flash" to "V4 Flash"
                "deepseek-v4-pro" -> "pro" to "V4 Pro"
                else -> null
            }
            label?.let { (key, name) ->
                val (total, request, hit, miss, response) = tokenBreakdown(modelUsage.usage)
                UsageModel(
                    key = key, name = name,
                    totalTokens = total, requestCount = request,
                    cacheHitTokens = hit, cacheMissTokens = miss,
                    responseTokens = response, cost = costForModel(modelUsage.model)
                )
            }
        }

        val costByDate = costTotal
            ?.days
            ?.associate { day -> day.date to day.data.sumOf { m -> costSum(m.usage) } }
            ?: emptyMap()

        // 从 by_api_key/cost 接口构建按模型拆分的每日费用映射
        // 结构：Map<modelKey, Map<dateStr, costSum>>
        val modelCostByDate = buildModelCostByDate(costByApiKey)

        val days = amount.data.bizData.days.map { day ->
            var flash = 0L; var flashHit = 0L; var flashMiss = 0L; var flashResp = 0L
            var pro = 0L; var proHit = 0L; var proMiss = 0L; var proResp = 0L
            var total = 0L

            for (modelUsage in day.data) {
                val (tokens, _, hit, miss, response) = tokenBreakdown(modelUsage.usage)
                total += tokens
                when (modelUsage.model) {
                    "deepseek-v4-flash" -> {
                        flash += tokens; flashHit += hit
                        flashMiss += miss; flashResp += response
                    }
                    "deepseek-v4-pro" -> {
                        pro += tokens; proHit += hit
                        proMiss += miss; proResp += response
                    }
                }
            }

            UsageDay(
                date = day.date,
                flashTokens = flash, flashCacheHit = flashHit,
                flashCacheMiss = flashMiss, flashResponse = flashResp,
                proTokens = pro, proCacheHit = proHit,
                proCacheMiss = proMiss, proResponse = proResp,
                flashCost = modelCostByDate["flash"]?.get(day.date) ?: 0.0,
                proCost = modelCostByDate["pro"]?.get(day.date) ?: 0.0,
                totalTokens = total,
                totalCost = costByDate[day.date] ?: 0.0
            )
        }

        val monthCost = costTotal
            ?.total
            ?.sumOf { costSum(it.usage) }
            ?: 0.0

        return UsageResult(models = models, days = days, monthCost = monthCost)
    }

    /**
     * 从 by_api_key/cost 接口构建按模型 + 日期拆分的费用映射。
     *
     * 遍历所有 API Key、所有模型 series 中的 buckets，
     * 汇总同一模型同一天的费用。
     *
     * @return Map<modelKey, Map<dateStr, totalCost>>
     */
    private fun buildModelCostByDate(response: ByApiKeyCostResponseDto?): Map<String, Map<String, Double>> {
        if (response == null) return emptyMap()

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val result = mutableMapOf<String, MutableMap<String, Double>>()

        val currencyData = response.data?.bizData?.data ?: return result

        for (currency in currencyData) {
            for (series in currency.series ?: emptyList()) {
                val modelKey = when (series.model) {
                    "deepseek-v4-flash" -> "flash"
                    "deepseek-v4-pro" -> "pro"
                    else -> continue  // 忽略 "deepseek-chat & deepseek-reasoner" 等不支持的模型
                }
                val dateCostMap = result.getOrPut(modelKey) { mutableMapOf() }
                for (bucket in series.buckets ?: emptyList()) {
                    val dateStr = dateFormat.format(Date(bucket.time * 1000))
                    val costValue = bucket.cost?.toDoubleOrNull() ?: 0.0
                    dateCostMap[dateStr] = (dateCostMap[dateStr] ?: 0.0) + costValue
                }
            }
        }
        return result
    }

    private fun tokenBreakdown(usage: List<UsageEntryDto>): TokenBreakdown {
        var total = 0L; var request = 0L; var hit = 0L; var miss = 0L; var response = 0L
        for (entry in usage) {
            val value = entry.amount.toDoubleOrNull()?.toLong() ?: 0L
            when (entry.kind) {
                "REQUEST" -> request = value
                "PROMPT_CACHE_HIT_TOKEN" -> { hit = value; total += value }
                "PROMPT_CACHE_MISS_TOKEN" -> { miss = value; total += value }
                "RESPONSE_TOKEN" -> { response = value; total += value }
                "PROMPT_TOKEN" -> total += value
            }
        }
        return TokenBreakdown(total, request, hit, miss, response)
    }

    private fun costSum(usage: List<UsageEntryDto>): Double {
        return usage
            .filter { it.kind != "REQUEST" }
            .sumOf { it.amount.toDoubleOrNull() ?: 0.0 }
    }
}

private data class TokenBreakdown(
    val total: Long,
    val request: Long,
    val hit: Long,
    val miss: Long,
    val response: Long
)

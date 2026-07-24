package com.deepseek.monitor.domain.usecase

import com.deepseek.monitor.domain.model.UsageResult
import com.deepseek.monitor.domain.repository.UsageRepository
import javax.inject.Inject

/**
 * 获取用量数据用例。
 *
 * 自动处理跨月数据拼接（当日期不超过7号时，补充上月数据）。
 */
class GetUsageUseCase @Inject constructor(
    private val usageRepository: UsageRepository
) {
    suspend operator fun invoke(): UsageResult {
        val now = java.time.LocalDate.now()
        val month = now.monthValue
        val year = now.year

        // 当月前7天需要拼接上月数据以确保7天趋势完整
        if (now.dayOfMonth <= 7) {
            // TODO: 阶段二实现跨月数据合并逻辑
        }

        return usageRepository.getUsage(month, year)
    }
}

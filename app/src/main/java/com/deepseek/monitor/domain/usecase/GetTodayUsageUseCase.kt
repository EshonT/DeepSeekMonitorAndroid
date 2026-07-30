package com.deepseek.monitor.domain.usecase

import com.deepseek.monitor.domain.model.UsageDay
import com.deepseek.monitor.domain.repository.UsageRepository
import java.time.LocalDate
import javax.inject.Inject

/**
 * 获取今日用量数据。
 *
 * 调用 [UsageRepository.getUsage] 获取当月用量，
 * 从中提取当天的 [UsageDay]。
 * 用于每日详情页展示两个模型当日 Token 用量和费用对比。
 */
class GetTodayUsageUseCase @Inject constructor(
    private val usageRepository: UsageRepository
) {
    /**
     * @return 今日的 [UsageDay]，包含按模型拆分的 Token 明细和费用。
     *         如果当月用量数据中不包含今天（如跨月边界），返回 null。
     */
    suspend operator fun invoke(): UsageDay? {
        val now = LocalDate.now()
        val result = usageRepository.getUsage(
            month = now.monthValue,
            year = now.year
        )
        val today = now.toString() // "yyyy-MM-dd"
        return result.days.find { it.date == today }
    }
}

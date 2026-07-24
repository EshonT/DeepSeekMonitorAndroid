package com.deepseek.monitor.util

import java.text.DecimalFormat
import java.util.Locale

/**
 * 数值格式化工具。
 * 与 Windows 版 main.tsx 中的 fmtInt / fmtTokensShort 逻辑保持一致。
 */
object TokenFormatter {

    private val moneyFormat = DecimalFormat("0.00")

    /**
     * 带千分位分隔的整数格式。
     * 例：1234567 → "1,234,567"
     */
    fun fmtInt(value: Long): String {
        return String.format(Locale.ENGLISH, "%,d", value)
    }

    /**
     * Token 数量缩写格式。
     * 规则：>=1亿 → "120M" | >=100万 → "1.2M" | >=1000 → "1.2K" | 其他 → "0"
     */
    fun fmtTokensShort(value: Long): String {
        return when {
            value >= 1_0000_0000L -> "${value / 100_0000}M"      // 1亿+
            value >= 100_0000L -> "${value / 100_0000.0}M"        // 100万+
            value >= 1000L -> "${value / 1000.0}K"                 // 1K+
            else -> value.toString()
        }
    }

    /**
     * 金额格式化，保留两位小数。
     * 例：12.5 → "¥12.50"
     */
    fun fmtMoney(value: Double): String {
        return "¥${moneyFormat.format(value)}"
    }

    /**
     * 金额格式化（无货币符号）。
     * 例：12.5 → "12.50"
     */
    fun fmtDecimal(value: Double): String {
        return moneyFormat.format(value)
    }

    /**
     * 百分比格式化。
     * 例：0.85 → "85%"
     */
    fun fmtPercent(ratio: Double): String {
        return "${(ratio * 100).toInt()}%"
    }

    /**
     * 缓存命中率计算。
     * 命中率 = 缓存命中 / (缓存命中 + 缓存未命中)，分母为 0 时返回 0%
     */
    fun cacheHitRatio(hit: Long, miss: Long): Double {
        val total = hit + miss
        return if (total == 0L) 0.0 else hit.toDouble() / total.toDouble()
    }
}

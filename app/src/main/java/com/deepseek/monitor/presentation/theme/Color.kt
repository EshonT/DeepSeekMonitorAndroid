package com.deepseek.monitor.presentation.theme

import androidx.compose.ui.graphics.Color

/**
 * 应用全局颜色 Token。
 * 三套主题共享同一套颜色语义键，值切换由 [AppTheme] 控制。
 */

// ── Light 主题 ──
object LightColors {
    val background = Color(0xFFF5F7FA)
    val surface = Color(0xFFFFFFFF)
    val primary = Color(0xFF4D6BFE)
    val onPrimary = Color.White
    val textStrong = Color(0xFF1A2A40)
    val textMuted = Color(0xFF6B7280)
    val textFaint = Color(0xFF9CA3AF)

    // 模型颜色
    val flash = Color(0xFF4091FF)      // V4 Flash 蓝
    val pro = Color(0xFFDA38F0)        // V4 Pro 紫

    // 图表颜色
    val chartHit = Color(0xFF34D399)    // 缓存命中 绿
    val chartMiss = Color(0xFFFF9C2B)   // 缓存未命中 橙
    val chartResponse = Color(0xFFA78BFA) // 输出 Token 紫

    // 语义色
    val error = Color(0xFFDC2626)
    val success = Color(0xFF16A34A)
    val warning = Color(0xFFFF9C2B)
    val divider = Color(0xFFE5E7EB)
}

// ── Dark 主题 ──
object DarkColors {
    val background = Color(0xFF1A1B1E)
    val surface = Color(0xFF2D2E32)
    val primary = Color(0xFF6B85FF)
    val onPrimary = Color.White
    val textStrong = Color(0xFFF0F0F0)
    val textMuted = Color(0xFF9CA3AF)
    val textFaint = Color(0xFF6B7280)

    val flash = Color(0xFF5BA0FF)
    val pro = Color(0xFFE45CFF)

    val chartHit = Color(0xFF34D399)
    val chartMiss = Color(0xFFFF9C2B)
    val chartResponse = Color(0xFFA78BFA)

    val error = Color(0xFFEF4444)
    val success = Color(0xFF22C55E)
    val warning = Color(0xFFFF9C2B)
    val divider = Color(0xFF374151)
}

// ── E-Ink 主题：严格 4 级灰阶 ──
object EInkColors {
    // 4 级灰阶：纯黑 #000000 / 深灰 #555555 / 中灰 #999999 / 纯白 #FFFFFF
    val black = Color(0xFF000000)
    val darkGray = Color(0xFF555555)
    val midGray = Color(0xFF999999)
    val white = Color(0xFFFFFFFF)

    val background = white
    val surface = white
    val primary = black
    val onPrimary = white
    val textStrong = black
    val textMuted = darkGray
    val textFaint = midGray

    val flash = black
    val pro = black

    val chartHit = darkGray
    val chartMiss = midGray
    val chartResponse = black

    val error = black
    val success = black
    val warning = darkGray
    val divider = midGray
}

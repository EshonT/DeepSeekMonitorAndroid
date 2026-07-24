package com.deepseek.monitor.presentation.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * 应用字体排版定义。
 * E-Ink 模式下所有字号增大 2sp 并加粗处理，提升墨水屏可读性。
 */
object AppTypography {

    /** 标准排版（Light / Dark 主题） */
    val standard = Typography(
        displayLarge = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold, lineHeight = 36.sp),
        headlineLarge = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold, lineHeight = 32.sp),
        headlineMedium = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.SemiBold, lineHeight = 28.sp),
        titleLarge = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.SemiBold, lineHeight = 26.sp),
        titleMedium = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium, lineHeight = 24.sp),
        bodyLarge = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Normal, lineHeight = 24.sp),
        bodyMedium = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal, lineHeight = 20.sp),
        bodySmall = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Normal, lineHeight = 16.sp),
        labelLarge = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium, lineHeight = 20.sp),
        labelMedium = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium, lineHeight = 16.sp),
        labelSmall = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Medium, lineHeight = 16.sp),
    )

    /** E-Ink 排版：统一 +2sp，强化粗体 */
    val eInk = Typography(
        displayLarge = TextStyle(fontSize = 30.sp, fontWeight = FontWeight.Bold, lineHeight = 38.sp),
        headlineLarge = TextStyle(fontSize = 26.sp, fontWeight = FontWeight.Bold, lineHeight = 34.sp),
        headlineMedium = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold, lineHeight = 30.sp),
        titleLarge = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, lineHeight = 28.sp),
        titleMedium = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, lineHeight = 26.sp),
        bodyLarge = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Medium, lineHeight = 26.sp),
        bodyMedium = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium, lineHeight = 22.sp),
        bodySmall = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium, lineHeight = 18.sp),
        labelLarge = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, lineHeight = 22.sp),
        labelMedium = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, lineHeight = 18.sp),
        labelSmall = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Bold, lineHeight = 18.sp),
    )
}

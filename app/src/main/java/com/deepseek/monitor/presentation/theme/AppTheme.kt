package com.deepseek.monitor.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * 主题模式枚举。
 * 与 legado [Theme.EInk] 设计一致。
 */
enum class ThemeMode {
    LIGHT,
    DARK,
    EINK
}

/**
 * 全局动画开关。E-Ink 模式下为 false，确保所有过渡动画被禁用。
 */
val LocalAnimationsEnabled = staticCompositionLocalOf { true }

/**
 * 全局阴影开关。E-Ink 模式下为 false（elevation = 0）。
 */
val LocalElevationEnabled = staticCompositionLocalOf { true }

/**
 * 全局 E-Ink 模式标记。
 */
val LocalEInkMode = staticCompositionLocalOf { false }

/**
 * 应用主题根节点。
 *
 * @param themeMode 当前主题模式，由顶层 ViewModel/偏好控制。
 */
@Composable
fun AppTheme(
    themeMode: ThemeMode = ThemeMode.LIGHT,
    content: @Composable () -> Unit
) {
    val isEInk = themeMode == ThemeMode.EINK
    val useDark = themeMode == ThemeMode.DARK || (themeMode != ThemeMode.EINK && isSystemInDarkTheme())

    val colorScheme = when {
        isEInk -> lightColorScheme(
            primary = EInkColors.primary,
            onPrimary = EInkColors.onPrimary,
            background = EInkColors.background,
            surface = EInkColors.surface,
            onBackground = EInkColors.textStrong,
            onSurface = EInkColors.textStrong,
            error = EInkColors.error,
            onError = EInkColors.white,
            outline = EInkColors.divider,
        )
        useDark -> darkColorScheme(
            primary = DarkColors.primary,
            onPrimary = DarkColors.onPrimary,
            background = DarkColors.background,
            surface = DarkColors.surface,
            onBackground = DarkColors.textStrong,
            onSurface = DarkColors.textStrong,
            error = DarkColors.error,
            onError = Color.White,
            outline = DarkColors.divider,
        )
        else -> lightColorScheme(
            primary = LightColors.primary,
            onPrimary = LightColors.onPrimary,
            background = LightColors.background,
            surface = LightColors.surface,
            onBackground = LightColors.textStrong,
            onSurface = LightColors.textStrong,
            error = LightColors.error,
            onError = Color.White,
            outline = LightColors.divider,
        )
    }

    val typography = if (isEInk) AppTypography.eInk else AppTypography.standard

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
    ) {
        CompositionLocalProvider(
            LocalAnimationsEnabled provides !isEInk,
            LocalElevationEnabled provides !isEInk,
            LocalEInkMode provides isEInk,
        ) {
            content()
        }
    }
}

package com.deepseek.monitor.presentation.navigation

/**
 * 应用页面路由定义。
 * 三个页面：仪表盘、设置、模型详情。
 */
sealed class Screen(val route: String) {
    data object Dashboard : Screen("dashboard")
    data object Settings : Screen("settings")

    /** detail/{model} — 参数 model 取 "flash" 或 "pro" */
    data object Detail : Screen("detail/{model}") {
        fun createRoute(model: String) = "detail/$model"
    }

    companion object {
        const val ARG_MODEL = "model"
    }
}

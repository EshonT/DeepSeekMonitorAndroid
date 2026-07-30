package com.deepseek.monitor.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.deepseek.monitor.presentation.daily.DailyOverviewScreen
import com.deepseek.monitor.presentation.dashboard.DashboardScreen
import com.deepseek.monitor.presentation.detail.DetailScreen
import com.deepseek.monitor.presentation.settings.SettingsScreen

/**
 * 应用导航图。
 * 单 Activity 内三页面路由。
 */
@Composable
fun DeepSeekNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route,
        modifier = modifier
    ) {
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                onNavigateToDetail = { model ->
                    navController.navigate(Screen.Detail.createRoute(model))
                },
                onNavigateToDaily = {
                    navController.navigate(Screen.DailyOverview.route)
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.Detail.route,
            arguments = listOf(
                navArgument(Screen.ARG_MODEL) { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val model = backStackEntry.arguments?.getString(Screen.ARG_MODEL) ?: "flash"
            DetailScreen(
                model = model,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.DailyOverview.route) {
            DailyOverviewScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}

package com.deepseek.monitor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.deepseek.monitor.presentation.navigation.DeepSeekNavGraph
import com.deepseek.monitor.presentation.theme.AppTheme
import com.deepseek.monitor.presentation.theme.ThemeMode
import dagger.hilt.android.AndroidEntryPoint

/**
 * 单 Activity 架构入口。
 * 通过 NavGraph 管理三个页面：仪表盘 / 设置 / 详情。
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AppTheme(themeMode = ThemeMode.LIGHT) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    DeepSeekNavGraph(navController = navController)
                }
            }
        }
    }
}

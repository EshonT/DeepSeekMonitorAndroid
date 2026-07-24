package com.deepseek.monitor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.deepseek.monitor.data.local.datastore.ConfigDataStore
import com.deepseek.monitor.presentation.navigation.DeepSeekNavGraph
import com.deepseek.monitor.presentation.theme.AppTheme
import com.deepseek.monitor.presentation.theme.ThemeMode
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var configDataStore: ConfigDataStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val raw by configDataStore.themeModeFlow.collectAsState(initial = "auto")
            val resolved = when (raw) {
                "eink" -> ThemeMode.EINK
                "dark" -> ThemeMode.DARK
                "light" -> ThemeMode.LIGHT
                else -> if (isSystemInDarkTheme()) ThemeMode.DARK else ThemeMode.LIGHT
            }

            AppTheme(themeMode = resolved) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    DeepSeekNavGraph(navController = navController)
                }
            }
        }
    }
}

package com.deepseek.monitor

import android.content.ComponentName
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
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
        // FLAG_KEEP_SCREEN_ON 必须在 enableEdgeToEdge() 之后，否则被覆盖
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContent {
            // Compose 层常亮，防止 OEM ROM 清除 Window flag
            val rootView = LocalView.current
            DisposableEffect(Unit) {
                rootView.keepScreenOn = true
                onDispose { rootView.keepScreenOn = false }
            }

            val raw by configDataStore.themeModeFlow.collectAsState(initial = "auto")
            val resolved = when (raw) {
                "eink" -> ThemeMode.EINK
                "dark" -> ThemeMode.DARK
                "light" -> ThemeMode.LIGHT
                else -> if (isSystemInDarkTheme()) ThemeMode.DARK else ThemeMode.LIGHT
            }

            // 主题切换到/离开水墨屏时动态切换启动图标。
            // 跳过初始 "auto" 避免 DataStore 加载前的错误状态。
            LaunchedEffect(raw) {
                if (raw != "auto") updateLauncherIcon(raw == "eink")
            }

            AppTheme(themeMode = resolved) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    DeepSeekNavGraph(navController = navController)
                }
            }
        }
    }

    /**
     * 动态切换启动图标：水墨屏模式显示高对比度黑白图标，
     * 其他模式显示品牌彩色图标。
     *
     * 通过 PackageManager 启用/禁用两个 activity-alias 实现，
     * 同一时间只有一个启动入口处于启用状态。
     */
    private fun updateLauncherIcon(eink: Boolean) {
        // 从类全名提取包名，不受 applicationIdSuffix 影响
        val pkg = MainActivity::class.java.name.removeSuffix(".MainActivity")
        val defaultAlias = ComponentName(pkg, "$pkg.MainActivityDefault")
        val einkAlias = ComponentName(pkg, "$pkg.MainActivityEink")

        val state = if (eink) {
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        } else {
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        }
        val oppositeState = if (eink) {
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        } else {
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        }

        packageManager.setComponentEnabledSetting(
            einkAlias, state, PackageManager.DONT_KILL_APP
        )
        packageManager.setComponentEnabledSetting(
            defaultAlias, oppositeState, PackageManager.DONT_KILL_APP
        )
    }
}

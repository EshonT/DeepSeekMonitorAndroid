package com.deepseek.monitor.presentation.eink

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * E-Ink 残影管理：每 6 次局部 UI 更新触发一次全屏刷新。
 * 参考 legado 项目的 "6-local + 1-global" 策略。
 */
class EInkRefreshManager {
    private val _counter = MutableStateFlow(0)
    val counter: StateFlow<Int> = _counter.asStateFlow()

    private val _needsFullRefresh = MutableStateFlow(false)
    val needsFullRefresh: StateFlow<Boolean> = _needsFullRefresh.asStateFlow()

    /** 记录一次局部更新。返回是否应立即触发全屏刷新。 */
    fun onLocalUpdate(): Boolean {
        val next = _counter.value + 1
        _counter.value = if (next >= 6) 0 else next
        val shouldRefresh = next >= 6
        _needsFullRefresh.value = shouldRefresh
        return shouldRefresh
    }

    /** 全屏刷新执行完毕后调用。 */
    fun onFullRefreshDone() {
        _needsFullRefresh.value = false
    }

    /** 强制立即全屏刷新（余额大幅变化等关键场景）。 */
    fun forceFullRefresh() {
        _counter.value = 0
        _needsFullRefresh.value = true
    }
}

@Composable
fun rememberEInkRefreshManager(): EInkRefreshManager {
    return remember { EInkRefreshManager() }
}

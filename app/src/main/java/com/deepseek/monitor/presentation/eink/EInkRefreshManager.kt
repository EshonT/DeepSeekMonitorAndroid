package com.deepseek.monitor.presentation.eink

import javax.inject.Inject
import javax.inject.Singleton

/**
 * E-Ink 残影管理：每 6 次局部 UI 更新触发一次全屏刷新。
 * 参考 legado 项目的 "6-local + 1-global" 策略。
 */
@Singleton
class EInkRefreshManager @Inject constructor() {
    private var _counter = 0

    /** 全屏刷新触发器 key。Compose 消费后重置。 */
    var fullRefreshKey = 0
        private set

    /** 记录一次局部更新。返回是否应触发全屏刷新。 */
    fun onLocalUpdate(): Boolean {
        _counter++
        return if (_counter >= 6) {
            _counter = 0
            fullRefreshKey++
            true
        } else {
            false
        }
    }

    /** 强制立即全屏刷新（余额大幅变化等关键场景）。 */
    fun forceFullRefresh() {
        _counter = 0
        fullRefreshKey++
    }
}

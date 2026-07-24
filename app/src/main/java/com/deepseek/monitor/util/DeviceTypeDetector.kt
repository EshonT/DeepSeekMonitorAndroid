package com.deepseek.monitor.util

import android.content.Context
import android.content.pm.PackageManager

/**
 * 设备类型检测器。
 * 采用 smallestWidth + 硬件特征双维度识别策略。
 *
 * 参考 legado AppConfig.isEInkMode 的检测方式。
 */
enum class DeviceType {
    /** 手机（sw < 600dp） */
    PHONE,

    /** 折叠屏展开态 / 小型平板（600dp ≤ sw < 840dp） */
    FOLDABLE,

    /** 大型平板（sw ≥ 840dp） */
    TABLET,

    /** 墨水屏设备 */
    EINK
}

object DeviceTypeDetector {

    /**
     * 已知 E-Ink 设备厂商的系统特征标识。
     * - com.hmct.eink：华为墨水屏平板（MatePad Paper）
     * - com.onyx.eink：文石 BOOX 系列
     * - com.boox.eink：文石部分型号
     * - com.hisense.eink：海信墨水屏手机
     */
    private val EINK_FEATURES = setOf(
        "com.hmct.eink",
        "com.onyx.eink",
        "com.boox.eink",
        "com.hisense.eink",
    )

    /**
     * 检测当前设备类型。
     * E-Ink 特性优先检测，确保墨水屏设备不被误判为平板。
     */
    fun detect(context: Context): DeviceType {
        // 优先检测 E-Ink 设备
        for (feature in EINK_FEATURES) {
            if (context.packageManager.hasSystemFeature(feature)) {
                return DeviceType.EINK
            }
        }

        // 按 smallestWidth 分级
        val swDp = context.resources.configuration.smallestScreenWidthDp
        return when {
            swDp >= 840 -> DeviceType.TABLET
            swDp >= 600 -> DeviceType.FOLDABLE
            else -> DeviceType.PHONE
        }
    }

    /**
     * 快速判断是否为 E-Ink 设备。
     */
    fun isEInk(context: Context): Boolean {
        return detect(context) == DeviceType.EINK
    }
}

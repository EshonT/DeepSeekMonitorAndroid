package com.deepseek.monitor.presentation.eink

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint

/**
 * 彩色图片转黑白灰阶 Bitmap。
 * 参考 legado EpaperTransformation：
 * 1. 灰度化（去饱和度）
 * 2. 阈值二值化（threshold 以上 → 纯白，以下 → 纯黑）
 *
 * @param threshold 二值化阈值 0-255，默认 150
 */
class ImageGrayscaleTransform(private val threshold: Int = 150) {

    fun transform(source: Bitmap): Bitmap {
        val width = source.width
        val height = source.height
        val result = Bitmap.createBitmap(width, height, source.config ?: Bitmap.Config.ARGB_8888)

        // 步骤一：灰度化
        val canvas = Canvas(result)
        val paint = Paint().apply {
            val colorMatrix = ColorMatrix().apply { setSaturation(0f) }
            colorFilter = ColorMatrixColorFilter(colorMatrix)
        }
        canvas.drawBitmap(source, 0f, 0f, paint)

        // 步骤二：阈值二值化
        val pixels = IntArray(width * height)
        result.getPixels(pixels, 0, width, 0, 0, width, height)
        for (i in pixels.indices) {
            val gray = android.graphics.Color.red(pixels[i])
            pixels[i] = if (gray < threshold) android.graphics.Color.BLACK else android.graphics.Color.WHITE
        }
        result.setPixels(pixels, 0, width, 0, 0, width, height)

        return result
    }
}

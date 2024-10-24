package com.github.lnstow.utils.ext

import android.graphics.Color
import android.view.Window
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.FloatRange
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.graphics.ColorUtils
import kotlin.math.roundToInt

fun @receiver:ColorInt Int.alpha(@FloatRange(from = 0.0, to = 1.0) alpha: Float): Int =
    ColorUtils.setAlphaComponent(this, alpha.toAlphaInt())

fun Int.toAlphaFloat() = this / 255f
fun Float.toAlphaInt() = (this * 255).roundToInt()

fun @receiver:ColorInt Int.mixColorAbove(@ColorInt target: Int) =
    ColorUtils.compositeColors(this, target)

fun @receiver:ColorInt Int.mixColorBelow(@ColorInt target: Int) =
    ColorUtils.compositeColors(target, this)

/** 如果颜色是透明的，会抛出异常 */
val @receiver:ColorInt Int.isLight: Boolean
    get() {
        // https://stackoverflow.com/a/60912606
        val whiteContrast = ColorUtils.calculateContrast(Color.WHITE, this)
        val blackContrast = ColorUtils.calculateContrast(Color.BLACK, this)
        return blackContrast >= whiteContrast
    }

/** 判断颜色亮度，原本的[Int.luminance]需要api26，[Color.luminance]需要api24 */
val @receiver:ColorInt Int.luminanceCompat: Float
    get() {
        checkTransparent(this)
        return ColorUtils.calculateLuminance(this).toFloat()
    }

val @receiver:ColorInt Int.luminanceHsv: Float
    get() {
        checkTransparent(this)
        val hsv = FloatArray(3)
        Color.colorToHSV(this, hsv)
        return hsv[2]
    }

val @receiver:ColorInt Int.luminanceHsl: Float
    get() {
        checkTransparent(this)
        val hsl = FloatArray(3)
        ColorUtils.colorToHSL(this, hsl)
        return hsl[2]
    }

private fun checkTransparent(@ColorInt color: Int) {
    if (color.alpha(0f) == Color.TRANSPARENT) {
        myApp.showToast("color is transparent", false)
        throw IllegalArgumentException("color is transparent")
    }
}

@Deprecated("Android 15 之后废弃的属性", ReplaceWith("ComponentActivity.enableEdgeToEdge()"))
fun setStatusBarColor(@ColorRes colorId: Int, window: Window?) {
    setStatusBarColorCode(getColorById(window?.context ?: return, colorId), window)
}

@Deprecated("Android 15 之后废弃的属性", ReplaceWith("ComponentActivity.enableEdgeToEdge()"))
fun setStatusBarColorCode(@ColorInt color: Int, window: Window?) {
    window ?: return
    window.statusBarColor = color
    window.navigationBarColor = color
    window.wi.lightBars(
        isLight = runCatching { window.statusBarColor.isLight }.getOrDefault(
            AppCompatDelegate.getDefaultNightMode() != AppCompatDelegate.MODE_NIGHT_YES
        )
    )
}
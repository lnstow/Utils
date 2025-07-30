package com.github.lnstow.utils.ext

import android.view.View
import android.view.Window
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsCompat.Type.InsetsType
import androidx.core.view.WindowInsetsControllerCompat

//  https://blog.csdn.net/StjunF/article/details/121840122
//  https://juejin.cn/post/7395866692772085800#heading-4
fun View.addWindowInsetsPadding(
    consumed: Boolean = false,
    @InsetsType barType: Int = WIB.systemBars(),
    doOnWindowChange: ((bars: androidx.core.graphics.Insets) -> Unit)? = null,
) {
    ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
        val bars = insets.getInsets(
            barType or WindowInsetsCompat.Type.displayCutout()
        )
        doOnWindowChange?.invoke(bars) ?: v.setPadding(
            bars.left, bars.top, bars.right, bars.bottom
        )
        if (consumed) WindowInsetsCompat.CONSUMED
        else insets
    }
}

private typealias WI = WindowInsetsControllerCompat
typealias WIB = WindowInsetsCompat.Type

fun WI.showSystemBars(@InsetsType barType: Int = WIB.systemBars()) {
    systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    show(barType)
}

fun WI.hideSystemBars(@InsetsType barType: Int = WIB.systemBars()) {
    systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    hide(barType)
}

fun WI.lightBars(
    isLight: Boolean = AppCompatDelegate.getDefaultNightMode() != AppCompatDelegate.MODE_NIGHT_YES,
    @InsetsType barType: Int = WIB.systemBars(),
) {
    if (barType and WIB.statusBars() != 0) isAppearanceLightStatusBars = isLight
    if (barType and WIB.navigationBars() != 0) isAppearanceLightNavigationBars = isLight
}

val Window.wi: WI get() = WindowInsetsControllerCompat(this, decorView)
inline fun Window.behavior(block: WI.() -> Unit) = wi.block()

fun Window.getStatusBarsHeight(): Int {
    return ViewCompat.getRootWindowInsets(this.decorView)
        ?.getInsets(WIB.statusBars())?.top ?: 0
}

fun Window.getNavigationBarsHeight(): Int {
    return ViewCompat.getRootWindowInsets(this.decorView)
        ?.getInsets(WIB.navigationBars())?.bottom ?: 0
}
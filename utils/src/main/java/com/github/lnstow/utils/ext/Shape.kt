package com.github.lnstow.utils.ext

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Outline
import android.graphics.Path
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.InsetDrawable
import android.graphics.drawable.ScaleDrawable
import android.os.Build
import android.view.Gravity
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.FloatRange
import androidx.annotation.Px
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.graphics.drawable.toDrawable
import com.github.lnstow.utils.ui.BaseAct

@Retention(AnnotationRetention.SOURCE)
annotation class Dp

/** 用泛型是为了 让返回值类型 等于调用者 */
fun <T : View> T.clearShadow() = this.apply { outlineProvider = null }
fun <T : View> T.round(@Dp radius: Int) =
    this.apply { outlineProvider = RoundCorner(radius.toPx()) }

private class RoundCorner(val radius: Int) : ViewOutlineProvider() {
    override fun getOutline(view: View?, outline: Outline?) {
        // outline默认alpha是1，会遮挡边框附近的背景色
        outline?.alpha = 0f
        // 如果不设置，不会裁剪背景色，圆角无效
        view?.clipToOutline = true
        view?.run { outline?.setRoundRect(0, 0, width, height, radius.toFloat()) }
    }
}

@RequiresApi(Build.VERSION_CODES.R)
fun <T : View> T.roundPath(path: View.() -> Path) = this.apply { outlineProvider = RoundPath(path) }

@RequiresApi(Build.VERSION_CODES.R)
fun <T : View> T.round(@Dp vararg radii: Int) = roundPath {
    Path().apply {
        addRoundRect(
            0f, 0f, width.f, height.f,
            FloatArray(radii.size) { radii[it].toPx().f },
            Path.Direction.CW,
        )
    }
}

@RequiresApi(Build.VERSION_CODES.R)
private class RoundPath(val path: View.() -> Path) : ViewOutlineProvider() {
    override fun getOutline(view: View?, outline: Outline?) {
        // outline默认alpha是1，会遮挡边框附近的背景色
        outline?.alpha = 0f
        // 如果不设置，不会裁剪背景色，圆角无效
        view?.clipToOutline = true
        view?.run { outline?.setPath(path()) }
    }
}

fun getColorById(context: Context, @ColorRes id: Int) = ContextCompat.getColor(context, id)
fun <T : TextView> T.ftColor(@ColorRes id: Int) = ftColorCode(getColorById(context, id))
fun <T : TextView> T.ftColorCode(@ColorInt color: Int) = this.apply { setTextColor(color) }
fun <T : View> T.bgColor(@ColorRes id: Int) = bgColorCode(getColorById(context, id))
fun <T : View> T.bgColorCode(@ColorInt color: Int) = this.apply { setBackgroundColor(color) }

inline fun <T : View> T.bgColor(@ColorRes id: Int, @Dp radius: Int, block: Style = {}) =
    bgColorCode(getColorById(context, id), radius, block)

inline fun <T : View> T.bgColorCode(@ColorInt color: Int, @Dp radius: Int, block: Style = {}) =
    background {
        colorRadius(color, radius, block)
    }

inline fun <T : View> T.background(block: Style) = apply { background = background.style(block) }

private typealias Style = GradientDrawable.() -> Unit

inline fun Drawable?.style(block: Style) =
    (if (this == null || this !is GradientDrawable) GradientDrawable() else this).apply {
        mutate()
        block()
    }


fun <T : Drawable?> T.tintId(@ColorRes id: Int, context: Context = BaseAct.top) =
    tintCode(getColorById(context, id))

fun <T : Drawable?> T.tintCode(@ColorInt color: Int) =
    this?.apply { mutate(); DrawableCompat.setTint(this, color) } ?: this

fun <T : Drawable?> T.alpha(@FloatRange(from = 0.0, to = 1.0) alpha: Float) =
    this?.apply { mutate(); setAlpha(alpha.toAlphaInt()) } ?: this

fun <T : Drawable?> T.setBounds(
    @Px width: Int = this?.intrinsicWidth ?: 0,
    @Px height: Int = this?.intrinsicHeight ?: 0
) = this?.apply { setBounds(0, 0, width, height) } ?: this

fun Drawable?.limitByteIfBitmap(context: Context, maxMB: Int = 55): Drawable? {
    val resource = this
    if (resource !is BitmapDrawable) return resource
    val bmp = resource.bitmap
    val scale = NumUnits.MB * maxMB * 1f / bmp.allocationByteCount
    return if (scale < 1f) {
        Bitmap.createScaledBitmap(
            bmp,
            (bmp.width * scale).toInt(),
            (bmp.height * scale).toInt(),
            true
        ).toDrawable(context.resources)
    } else resource
}

inline fun Context.newDrById(@ColorRes id: Int, @Dp radius: Int, block: Style = {}) =
    newDrByCode(getColorById(this, id), radius, block)

inline fun newDrByCode(@ColorInt color: Int, @Dp radius: Int, block: Style = {}) =
    GradientDrawable().colorRadius(color, radius, block)

inline fun GradientDrawable.colorRadius(@ColorInt color: Int, @Dp radius: Int, block: Style = {}) =
    this.apply {
        setColor(color)
        cornerRadius = radius.toPx().f
        block()
    }

fun GradientDrawable.setStrokeById(@ColorRes colorId: Int, @Dp width: Int = 1) {
    setStroke(width.toPx(), getColorById(myApp, colorId))
}

fun GradientDrawable.gradient(
    @ColorInt c: Int,
    vararg alphaArr: Float = floatArrayOf(0f, 1f),
    orientation: GradientDrawable.Orientation? = null
): GradientDrawable {
    this.colors = IntArray(alphaArr.size) { c.alpha(alphaArr[it]) }
    if (orientation != null) this.orientation = orientation
    return this
}

fun <T : View> T.gradient(
    @ColorInt c: Int,
    vararg alphaArr: Float = floatArrayOf(0f, 1f),
    orientation: GradientDrawable.Orientation? = null
) = background { gradient(c, alphaArr = alphaArr, orientation = orientation) }

/**
 * @param scale 表示缩放后dr，相对于（view宽高）的百分比。
 * @param minW 表示缩放后dr的最小宽度百分比（相对于view宽度），
 * 如 [minW] = 0.2f ，表示最终显示的dr，最小宽度是view宽度的 20%。
 * @since 对于[ScaleDrawable]，level表示缩放后dr相对于（view宽高）的百分比，
 * 构造函数中的scaleWidth为0.2f，表示缩放后的dr，最小宽度是view宽度的80%
 */
fun Drawable?.scale(scale: Float, minW: Float = 0f, minH: Float = 0f) =
    ScaleDrawable(this, Gravity.CENTER, 1 - minW, 1 - minH)
        .also { it.level = (10000 * scale).toInt() }

fun Drawable?.inset(left: Int, top: Int, right: Int = left, bottom: Int = top) =
    InsetDrawable(this, left, top, right, bottom)

package com.github.lnstow.utils.ext

import android.app.Activity
import android.app.ActivityOptions
import android.app.Service
import android.content.Context
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.text.Editable
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.util.Pair
import android.util.TypedValue
import android.view.GestureDetector
import android.view.Gravity
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.TouchDelegate
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.webkit.WebView
import android.widget.Checkable
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Space
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.annotation.IntRange
import androidx.annotation.MainThread
import androidx.annotation.StringRes
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.text.buildSpannedString
import androidx.core.view.postDelayed
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.github.lnstow.utils.LnUtils.resId
import com.github.lnstow.utils.ui.BaseAct
import com.github.lnstow.utils.ui.ClickEv
import com.google.android.material.tabs.TabLayout
import com.google.gson.internal.bind.util.ISO8601Utils
import java.util.Date
import kotlin.math.abs

fun View.expandTouchArea(size: Int) {
    expandTouchArea(Rect(size, size, size, size))
}

fun View.expandTouchArea(rect: Rect) = postDelayed(600) {
    val curRect = Rect()
    getHitRect(curRect)
    curRect.top -= rect.top
    curRect.bottom += rect.bottom
    curRect.left -= rect.left
    curRect.right += rect.right
    val parentView = parent as View
    when (val d = parentView.touchDelegate) {
        null -> parentView.touchDelegate = TouchDelegate(curRect, this)
        is MultiTouchDelegate -> d.addDelegate(TouchDelegate(curRect, this))
        else -> parentView.touchDelegate = MultiTouchDelegate(curRect, this).also {
            it.addDelegate(d)
        }
    }
}

private class MultiTouchDelegate(rect: Rect, view: View) : TouchDelegate(rect, view) {
    private val delegates = mutableListOf<TouchDelegate>()

    fun addDelegate(delegate: TouchDelegate) {
        delegates.add(delegate)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (super.onTouchEvent(event)) return true
        var res = false
        val x = event.x
        val y = event.y
        for (delegate in delegates) {
            event.setLocation(x, y)
            res = delegate.onTouchEvent(event)
            if (res) break
        }
        return res
    }
}

/**
 * 触发设备震动
 * @param milliseconds 震动持续时间（毫秒）
 * @param amplitude 震动力度(1..255)
 */
@MainThread
private fun View.startVibrate(
    @IntRange(from = 1) milliseconds: Long = 100,
    @IntRange(from = 1, to = 255) amplitude: Int? = null,
) {
//    if (!CommonModule.vibrateEffect) return
//    performHapticFeedback(
//        HapticFeedbackConstants.LONG_PRESS, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
//    )
//    return
    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        context.getSystemService(Service.VIBRATOR_MANAGER_SERVICE)
            .as2<VibratorManager>().defaultVibrator
    } else context.getSystemService(Service.VIBRATOR_SERVICE) as Vibrator

    if (!vibrator.hasVibrator()) return
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) vibrator.vibrate(
        VibrationEffect.createOneShot(
            milliseconds.coerceAtLeast(50),
            amplitude ?: VibrationEffect.DEFAULT_AMPLITUDE
        )
    ) else vibrator.vibrate(milliseconds)
}

fun View.startVibrateLv1() = startVibrate(10, 10)
fun View.startVibrateLv2() = startVibrate(50, 50)
fun View.startVibrateLv3() = startVibrate(100, 1)

/** 布局gravity */
fun <T : LinearLayout> T.gravityCenter() = apply { gravity = Gravity.CENTER }
fun <T : LinearLayout> T.gravityCenterVertical() = apply { gravity = Gravity.CENTER_VERTICAL }
fun <T : LinearLayout> T.gravityCenterHorizontal() = apply { gravity = Gravity.CENTER_HORIZONTAL }

fun <T : LinearLayout.LayoutParams> T.gravityCenter() = apply { gravity = Gravity.CENTER }
fun <T : LinearLayout.LayoutParams> T.gravityCenterVertical() =
    apply { gravity = Gravity.CENTER_VERTICAL }

fun <T : LinearLayout.LayoutParams> T.gravityCenterHorizontal() =
    apply { gravity = Gravity.CENTER_HORIZONTAL }

fun <T : TextView> T.gravityCenter() = apply { gravity = Gravity.CENTER }
fun <T : TextView> T.gravityCenterVertical() = apply { gravity = Gravity.CENTER_VERTICAL }
fun <T : TextView> T.gravityCenterHorizontal() = apply { gravity = Gravity.CENTER_HORIZONTAL }


/** 单位转换 */
fun Int.toPx(): Int = ((this * density) + 0.5F).toInt()
fun Int.toDp(): Int = ((this / density) + 0.5F).toInt()

fun Long.toDate(): Date = Date(this)
fun Long.toISO8601(): String = ISO8601Utils.format(Date(this), true)
private val density get() = myApp.resources.displayMetrics.density

fun View.getUsableWidth() = width - paddingLeft - paddingRight
fun View.getUsableHeight() = height - paddingTop - paddingBottom

fun TextView.updateCompoundDrawablesWithIntrinsicBounds(
    left: Drawable? = compoundDrawables[0],
    top: Drawable? = compoundDrawables[1],
    right: Drawable? = compoundDrawables[2],
    bottom: Drawable? = compoundDrawables[3],
) = setCompoundDrawablesWithIntrinsicBounds(left, top, right, bottom)

fun TextView.setLineHeightDp(lineHeight: Int) {
    val fontHeight = paint.getFontMetricsInt(null)
    if (lineHeight != fontHeight) {
        setLineSpacing(lineHeight.toPx() - fontHeight + 0f, 1f)
    }
}

fun TextView.singleLine(
    ellipsize: TextUtils.TruncateAt = TextUtils.TruncateAt.END,
    maxEms: Int? = null,
    maxWidth: Int? = null,
) {
    setSingleLine()
    setEllipsize(ellipsize)
    if (maxEms != null) setMaxEms(maxEms)
    if (maxWidth != null) setMaxWidth(maxWidth)
    isSelected = true
}

fun EditText.setTextAndSel(text: CharSequence) {
    setText(text)
    /** 文本输入过滤器会导致实际长度不等于预期长度，有超出索引异常，所以要取实际长度 */
    setSelection(this.text.length)
}

inline fun <T : View> T.showSoftInput(
    delay: Long = 150,
    crossinline block: T.() -> Unit = {},
) = postDelayed(delay) {
    requestFocus()
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
    block(this)
}

inline fun <T : View> T.hideSoftInput(
    delay: Long = 150,
    crossinline block: T.() -> Unit = {},
) = postDelayed(delay) {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(this.windowToken, 0)
    block(this)
}

val TextView.isNotEmpty get() = !text.isNullOrEmpty()
val TextView.isBlank get() = text.isNullOrBlank()
val SearchView.searchEt: EditText get() = findViewById(androidx.appcompat.R.id.search_src_text)

/**
 * 监听键盘点击完成事件
 */
fun EditText.setOnEditorDoneActionListener(done: () -> Unit) {
    setOnEditorActionListener({ v: TextView?, actionId: Int, event: KeyEvent? ->
        if (actionId == EditorInfo.IME_ACTION_DONE || isConfirmEvent(event)) {
            done.invoke()
            return@setOnEditorActionListener true
        }
        false
    })
}

private fun isConfirmEvent(event: KeyEvent?): Boolean {
    return event != null &&
            (event.keyCode == KeyEvent.KEYCODE_ENTER || event.keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER)
}

fun RecyclerView.getEmptyView(@StringRes id: Int): View {
    val emptyView = TextView(context)
    emptyView.layoutParams = VGLP(MATCH, MATCH)
    emptyView.setText(id)
    emptyView.gravityCenter()
    return emptyView
}

fun RecyclerView.Adapter<*>.notifyItemAllChanged(payload: Any? = null) {
    notifyItemRangeChanged(0, itemCount, payload)
}

fun WebView.loadHtml(html: String, baseUrl: String? = null) {
    loadDataWithBaseURL(baseUrl, html, "text/html", "UTF-8", null)
}

@Suppress("FunctionNaming")
inline fun Context.TextView(block: TextView.() -> Unit) = TextView(this).apply(block)

@Suppress("FunctionNaming")
inline fun Context.LinearLayout(block: LinearLayout.() -> Unit) = LinearLayout(this).apply(block)
inline fun <T : ViewGroup, R : View> T.addView(view: Context.() -> R, block: R.() -> Unit = {}) =
    context.view().apply { block();addView(this) }

fun getDimension(context: Context, @DimenRes id: Int): Float {
    return context.resources.getDimension(id)
}

fun <T : TextView> T.bold() = apply { setTypeface(typeface, Typeface.BOLD) }
fun <T : TextView> T.unBold() = apply { setTypeface(null, Typeface.NORMAL) }
fun <T : TextView> T.ftSize(@DimenRes id: Int) =
    apply { setTextSize(TypedValue.COMPLEX_UNIT_PX, getDimension(context, id)) }

fun <T : TextView> T.ftSizeCode(sp: Int) =
    apply { setTextSize(TypedValue.COMPLEX_UNIT_SP, sp.f) }

fun View.getStringById(@StringRes id: Int) = context.getString(id)
fun View.getDimensionById(@DimenRes id: Int) = getDimension(context, id)
fun View.getColorById(@ColorRes id: Int) = getColorById(context, id)
fun View.getDrawableById(@DrawableRes id: Int) = ContextCompat.getDrawable(context, id)


@Suppress("SpreadOperator")
fun sharedElementBundle(ctx: Context, vararg views: View) =
    ActivityOptions.makeSceneTransitionAnimation(
        ctx.activity(),
        *(views.map { Pair(it, it.transitionName) }.toTypedArray())
    ).toBundle()


/** 绑定输入框文本和按钮的样式
 * @param btnUseMain 按钮是否使用主题色
 * @param btnStyle 传入 按钮 和 输入框是否为空
 * */
inline fun bindTextAndBtnStyle(
    et: EditText,
    btn: View,
    btnUseMain: Boolean = true,
    crossinline etIsEmpty: (Editable) -> Boolean = { false },
    crossinline btnStyle: View.(disable: Boolean) -> Unit = {
        if (this is TextView) ftColor(
            if (it) resId.ft
            else if (btnUseMain) resId.main
            else resId.ftAcc
        )
    },
) = et.doAfterTextChanged {
    val empty = it.isNullOrBlank() || etIsEmpty(it)
    btn.btnStyle(empty)
    btn.isClickable = !empty
}

inline fun <T> bindCheckAndBtnStyle(
    cb: Array<T>,
    btn: TextView,
    btnUseMain: Boolean = true,
    crossinline disable: (T) -> Boolean = { false },
    crossinline btnStyle: TextView.(disable: Boolean) -> Unit = {
        ftColor(
            if (it) resId.ft
            else if (btnUseMain) resId.main
            else resId.ftAcc
        )
    },
) where T : Checkable, T : View {
    val isDisable = cb.any { !it.isChecked || disable(it) }
    btn.btnStyle(isDisable)
    btn.isClickable = !isDisable
    cb.forEach {
        it.setOnClickListener {
            val empty = cb.any { !it.isChecked || disable(it) }
            btn.btnStyle(empty)
            btn.isClickable = !empty
        }
    }
}

@Suppress("FunctionNaming")
fun <T : View> T.styleBtn(btnStyle: BtnStyle, @Dp radius: Int = 5): T {
    bgColor(btnStyle.bgColorId, radius) { this.alpha = 255 }
    if (this is TextView) ftColor(btnStyle.ftColorId).gravityCenter()
    return this
}

enum class BtnStyle(@ColorRes val bgColorId: Int, @ColorRes val ftColorId: Int) {

}


fun LinearLayout.addDivider(horizontal: Boolean = true, @Dp marginDp: Int = 0) =
    addView(::View) {
        layoutParams = if (horizontal) LLLP(MATCH, 1.toPx()).apply {
            setMargins(marginDp.toPx(), 0, marginDp.toPx(), 0)
        } else LLLP(1.toPx(), MATCH).apply {
            setMargins(0, marginDp.toPx(), 0, marginDp.toPx())
        }
        bgColor(resId.divider)
    }

fun LinearLayout.addSpace(@Dp dp: Int, horizontal: Boolean = true) =
    addView(::Space) {
        layoutParams = if (horizontal) LLLP(MATCH, dp.toPx()) else LLLP(dp.toPx(), MATCH)
    }

fun LinearLayout.addSpace() = addView(::Space) {
    layoutParams = LLLP(0, 0).apply { weight = 1f }
}

inline fun <VG : ViewGroup, T : View> T.attachTo(
    vg: VG,
    doAddView: VG.(View) -> Unit = { addView(it) },
): T = this.also { vg.doAddView(it) }

inline fun <VG : ViewGroup, T : View> T.attachTo(
    vg: VG,
    @Dp height: Int,
    @Dp width: Int? = null,
): T = this.also { vg.addView(it, width?.toPx() ?: MATCH, height.toPx()) }

fun View.setPaddingHorizontal(@Dp paddingDp: Int) {
    setPadding(paddingDp.toPx(), paddingTop, paddingDp.toPx(), paddingBottom)
}

fun View.setPaddingDp(@Dp horizontalDp: Int, @Dp verticalDp: Int) {
    setPadding(horizontalDp.toPx(), verticalDp.toPx(), horizontalDp.toPx(), verticalDp.toPx())
}

val View.globalRect: Rect
    get() {
        val rect = Rect()
        getGlobalVisibleRect(rect)
        return rect
    }

val View.localRect: Rect
    get() {
        val rect = Rect()
        getLocalVisibleRect(rect)
        return rect
    }


fun Activity.getColorById(@ColorRes id: Int) = ContextCompat.getColor(this, id)
fun Fragment.getColorById(@ColorRes id: Int) = ContextCompat.getColor(requireContext(), id)
fun Activity.getDrawableById(@DrawableRes id: Int) = ContextCompat.getDrawable(this, id)
fun Fragment.getDrawableById(@DrawableRes id: Int) = ContextCompat.getDrawable(requireContext(), id)

fun Context.getHighlightMsg(
    @StringRes startStr: Int,
    highlightStr: String,
    @StringRes endStr: Int,
) = buildSpannedString {
    append(getString(startStr))
    append(
        " $highlightStr ",
        ForegroundColorSpan(getColorById(this@getHighlightMsg, resId.main)),
        EX_EX
    )
    append(getString(endStr))
}

inline fun View.doOnEachNextLayout(crossinline action: (view: View) -> Unit) {
    addOnLayoutChangeListener { view, _, _, _, _, _, _, _, _ ->
        action(view)
    }
}

fun TabLayout.setOnTabReselect(tabIdx: Int? = null, block: (TabLayout.Tab?) -> Unit) {
    addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
        override fun onTabReselected(tab: TabLayout.Tab?) {
            if (tabIdx == null || tab?.position == tabIdx) block(tab)
        }

        override fun onTabUnselected(tab: TabLayout.Tab?) {}
        override fun onTabSelected(tab: TabLayout.Tab?) {}
    })
}

val @receiver:StringRes Int.s get() = BaseAct.top.getString(this)
val @receiver:ColorRes Int.c get() = BaseAct.top.getColor(this)
val @receiver:ColorInt Int.hc get() = this or 0xff000000.toInt()

@Deprecated("Use RVH2", ReplaceWith("RVH2(view)"))
open class RVH(view: View) : RecyclerView.ViewHolder(view)

open class RVH2<T : View>(val view: T) : RecyclerView.ViewHolder(view)

open class VbVh<T : ViewBinding>(val vb: T) : RecyclerView.ViewHolder(vb.root) {
    constructor(
        create: (
            inflater: LayoutInflater, parent: ViewGroup, attach: Boolean,
        ) -> T,
        vg: ViewGroup,
    ) : this(create(LayoutInflater.from(vg.context), vg, false))
}

fun RecyclerView.setOnClick(block: ClickEv) {
    val gestureDetector = GestureDetector(
        context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapUp(e: MotionEvent): Boolean {
                block(this@setOnClick)
                return false
            }
        })
    setOnTouchListener { v, event ->
        gestureDetector.onTouchEvent(event)
    }
}

val View.parentView: ViewGroup? get() = this.parent as? ViewGroup
fun View.removeFromParent() = parentView?.removeView(this)

val ViewPager2.rv get() = getChildAt(0) as RecyclerView
fun ViewPager2.addCarouselEffect(
    @Dp eachItemSpace: Int,
    @Dp nextItemWidth: Int,
    endView: View? = this.parentView?.parentView,
    nextItemScaleY: Float = 0.8f,
) {
    var par: ViewGroup? = this
    while (par != null && par !== rootView && par !== endView) {
        par.clipChildren = false    // No clipping the left and right items
        par.clipToPadding = false   // Show in full width without clipping the padding
        par = par.parentView
    }

    offscreenPageLimit = 5  // Render the left and right items
    setPaddingHorizontal(eachItemSpace + nextItemWidth)
    rv.overScrollMode = RecyclerView.OVER_SCROLL_NEVER // Remove the scroll effect

    val compositePageTransformer = CompositePageTransformer()
    compositePageTransformer.addTransformer(MarginPageTransformer(eachItemSpace.toPx()))

    compositePageTransformer.addTransformer { page, position ->
        val r = 1 - abs(position)
        page.scaleY = nextItemScaleY + r * (1 - nextItemScaleY)
    }
    setPageTransformer(compositePageTransformer)
}

fun View.getRectInWindow(): Rect {
    val location = IntArray(2)
    this.getLocationInWindow(location)
    return Rect(
        location[0],
        location[1],
        location[0] + this.width,
        location[1] + this.height
    )
}

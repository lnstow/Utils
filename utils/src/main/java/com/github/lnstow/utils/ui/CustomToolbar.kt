package com.github.lnstow.utils.ui

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.view.updatePadding
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import com.github.lnstow.utils.LnUtils
import com.github.lnstow.utils.databinding.CustomToolbarBinding
import com.github.lnstow.utils.ext.Dp
import com.github.lnstow.utils.ext.LLLP
import com.github.lnstow.utils.ext.MATCH
import com.github.lnstow.utils.ext.WRAP
import com.github.lnstow.utils.ext.activity
import com.github.lnstow.utils.ext.ftColorCode
import com.github.lnstow.utils.ext.ftSize
import com.github.lnstow.utils.ext.getColorById
import com.github.lnstow.utils.ext.getDrawableById
import com.github.lnstow.utils.ext.getStringById
import com.github.lnstow.utils.ext.onBackPressed2
import com.github.lnstow.utils.ext.singleLine
import com.github.lnstow.utils.ext.toPx

abstract class CustomToolbar(
    context: Context,
    @Dp val heightDp: Int = LnUtils.tbConfig.heightDp
) {
    protected open val iconWidth = 35.toPx()
    protected open val tbPadding = 15.toPx()
    protected val primaryColor = getColorById(context, LnUtils.resId.main)
    protected val textColor = getColorById(context, LnUtils.tbConfig.text)
    protected val titleColor = getColorById(context, LnUtils.tbConfig.title)

    lateinit var title: View protected set
    val titleTv get() = title as TextView
    lateinit var back: View protected set
    protected val backIv get() = back as ImageView

    val tbView by lazy {
        CustomToolbarBinding.inflate(LayoutInflater.from(context)).apply {
            toolbarLeftLl.updatePadding(left = tbPadding)
            toolbarRightLl.updatePadding(right = tbPadding)
            root.layoutParams = LLLP(MATCH, MATCH)
            onLayout()
        }.root
    }

    protected abstract fun CustomToolbarBinding.onLayout()

    protected fun CustomToolbarBinding.addIcon(
        @DrawableRes resId: Int,
        toRight: Boolean = true
    ) = addIcon(root.getDrawableById(resId), toRight)

    protected fun CustomToolbarBinding.addIcon(
        drawable: Drawable?,
        toRight: Boolean = true
    ) = newView(::ImageView) {
        layoutParams = LLLP(iconWidth, MATCH)
        scaleType = ImageView.ScaleType.CENTER_INSIDE
        setImageDrawable(drawable)
        (if (toRight) toolbarRightLl else toolbarLeftLl).addView(this)
    }

    protected fun CustomToolbarBinding.addText(
        @StringRes textId: Int,
        toRight: Boolean,
        highlight: Boolean
    ) = addText(root.getStringById(textId), toRight, highlight)

    protected fun CustomToolbarBinding.addText(
        text: CharSequence,
        toRight: Boolean,
        highlight: Boolean
    ) = newView(::TextView) {
        layoutParams = LLLP(WRAP, MATCH)
        gravity = Gravity.CENTER
        ftColorCode(if (highlight) primaryColor else textColor).ftSize(LnUtils.tbConfig.textSize)
        this.text = text
        (if (toRight) toolbarRightLl else toolbarLeftLl).addView(this)
    }

    protected fun CustomToolbarBinding.addTitle(
        @StringRes textId: Int,
        toCenter: Boolean,
        highlight: Boolean
    ) = addTitle(root.getStringById(textId), toCenter, highlight)

    protected fun CustomToolbarBinding.addTitle(
        text: CharSequence,
        toCenter: Boolean,
        highlight: Boolean
    ) = newView(::TextView) {
        layoutParams = LLLP(WRAP, MATCH)
        gravity = Gravity.CENTER
        titleWidthManuallyLimit()
        ftColorCode(if (highlight) primaryColor else titleColor).ftSize(LnUtils.tbConfig.titleSize)
        this.text = text
        title = this
        (if (toCenter) toolbarCenterLl else toolbarLeftLl).addView(this)
    }
}

/** 带有返回箭头的toolbar，父类提供onLayout2给子类实现布局 */
abstract class BackArrowTb(context: Context) : CustomToolbar(context) {
    private val backArrow = VectorDrawableCompat.create(
        context.resources, LnUtils.tbConfig.backIcon, context.theme
    )

    final override fun CustomToolbarBinding.onLayout() {
        back = addIcon(backArrow, toRight = false)
        back.setOnClickListener { it.context.activity()?.onBackPressed2() }
        onLayout2()
    }

    protected abstract fun CustomToolbarBinding.onLayout2()
}

inline fun <T : View> CustomToolbarBinding.newView(
    view: Context.() -> T,
    block: T.() -> Unit
) = root.context.view().apply(block)

inline fun <VG : ViewGroup, TB : CustomToolbar> TB.attachTo(
    vg: VG,
    doAddView: VG.(View) -> Unit
): TB = this.also { vg.doAddView(tbView) }

fun <VG : ViewGroup, TB : CustomToolbar> TB.attachTo(vg: VG): TB =
    this.also { vg.addView(tbView, MATCH, heightDp.toPx()) }

fun TextView.titleWidthManuallyLimit() = singleLine(maxWidth = 180.toPx())

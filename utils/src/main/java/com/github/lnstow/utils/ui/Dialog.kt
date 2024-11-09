package com.github.lnstow.utils.ui

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.PopupWindow
import androidx.appcompat.app.AlertDialog

val AlertDialog.parentLayout get() = findViewById<ViewGroup>(androidx.appcompat.R.id.parentPanel)
val AlertDialog.customLayout get() = findViewById<ViewGroup>(android.R.id.custom)

private val transparent by lazy { ColorDrawable(Color.TRANSPARENT) }

/** 此方法需要在[AlertDialog.setOnShowListener]中调用 */
fun AlertDialog.updateSize(
    width: Int? = null,
    height: Int? = null,
//    cornerRadius: Int? = null
) {
    /** 对话框宽高 */
    window?.setBackgroundDrawable(transparent)
    window?.setLayout(
        width ?: window!!.attributes.width,
        height ?: window!!.attributes.height
    )

    /** 对话框圆角 */
//    val parent = parentLayout
//    if (cornerRadius != null) parent?.round(cornerRadius)
//    parent?.updateLayoutParams<FrameLayout.LayoutParams> {
//        this.gravity = Gravity.CENTER
//        this.width = width ?: this.width
//        this.height = height ?: this.height
//    }

    /** 设置对话框透明布局，因为上层自定义view到下层根布局中间有很多层布局，大多都有白色背景，
     * 如果不设置为透明，则会看不出上层布局设置的圆角 */
//    var view: ViewParent? = customLayout
//    while (true) {
//        if (view is ViewGroup) {
//            if (view != parent) view.background = transparent
//            view = view.parent
//        } else {
//            break
//        }
//    }

    /** 以下代码是为了显示对话框的周围阴影，如果没有，则对话框周围看不出一圈高度阴影 */
//    window?.decorView?.background = GradientDrawable().apply {
//        setColor(Color.RED)
//        if (cornerRadius != null) {
//            this.cornerRadius = cornerRadius.toFloat() + 10
//        }
//    }
}

fun PopupWindow.dimBehind(dimAlpha: Float = 0.7f) {
    val container = contentView.rootView
    val context = contentView.context
    val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val p = container.layoutParams as WindowManager.LayoutParams
    p.flags = p.flags or WindowManager.LayoutParams.FLAG_DIM_BEHIND
    p.dimAmount = dimAlpha
    wm.updateViewLayout(container, p)
}
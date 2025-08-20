package com.github.lnstow.utils.ext

import android.app.ProgressDialog
import android.content.Context
import android.view.View
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.github.lnstow.utils.LnUtils
import com.github.lnstow.utils.ui.BaseAct
import com.google.android.material.dialog.MaterialAlertDialogBuilder

inline fun Context.showDialog(
    themeId: Int = LnUtils.resId.showDialogTheme,
    setDialogListener: AlertDialog.() -> Unit = {},
    block: MaterialAlertDialogBuilder.() -> Unit,
): AlertDialog = MaterialAlertDialogBuilder(this, themeId).run {
    block()
    val d = create()
    d.setDialogListener()
    d.show()
    d
}

inline fun Fragment.showDialog(
    themeId: Int = LnUtils.resId.showDialogTheme,
    setDialogListener: AlertDialog.() -> Unit = {},
    block: MaterialAlertDialogBuilder.() -> Unit,
) = requireContext().showDialog(themeId, setDialogListener, block)

private fun AlertDialog.btn(
    which: Int, text: String, click: (View) -> Unit,
) = getButton(which).apply {
    setText(text)
    setOnClickListener(click)
}

fun AlertDialog.positiveBtn(text: String, click: (View) -> Unit) =
    btn(AlertDialog.BUTTON_POSITIVE, text, click)

fun AlertDialog.negativeBtn(text: String, click: (View) -> Unit) =
    btn(AlertDialog.BUTTON_NEGATIVE, text, click)

fun AlertDialog.neutralBtn(text: String, click: (View) -> Unit) =
    btn(AlertDialog.BUTTON_NEUTRAL, text, click)


fun Context.showToast(msg: String, showShort: Boolean = true) = Toast.makeText(
    this, msg, if (showShort) Toast.LENGTH_SHORT else Toast.LENGTH_LONG
).show()

fun Fragment.showToast(msg: String, shortShow: Boolean = true) =
    requireContext().showToast(msg, shortShow)

fun Context.showLoading(msg: String) = ProgressDialog(this).apply {
    setMessage(msg)
    setCancelable(false)
    show()
}

fun Fragment.showLoading(msg: String) = requireContext().showLoading(msg)

interface ToastInfo {
    fun showToast(ctx: AccessCtx = BaseAct.top as BaseAct)
}

class ToastDef private constructor(
    private val msg: String? = null,
    @StringRes private val msgId: Int = 0,
    private val showShort: Boolean = true,
) : ToastInfo {
    constructor(msg: String, showShort: Boolean = true) : this(msg, 0, showShort)
    constructor(msg: Int, showShort: Boolean = true) : this(null, msg, showShort)

    override fun showToast(ctx: AccessCtx) {
        val c = ctx.ctx()
        c.showToast(msg ?: c.getString(msgId), showShort)
    }
}

interface LoadingInfo {
    fun createAndShow(ctx: AccessCtx = BaseAct.top as BaseAct): UiObj

    interface UiObj {
        fun show(ctx: AccessCtx = BaseAct.top as BaseAct)
        fun hide(ctx: AccessCtx = BaseAct.top as BaseAct)
    }
}

class LoadingDef private constructor(
    private val msg: String? = null,
    @StringRes private val msgId: Int = 0,
) : LoadingInfo {
    constructor(msg: String) : this(msg, 0)
    constructor(msg: Int) : this(null, msg)

    override fun createAndShow(ctx: AccessCtx): LoadingInfo.UiObj {
        return LoadingDefDialog(msg, msgId).also { it.show(ctx) }
    }
}

class LoadingDefDialog(
    private val msg: String? = null,
    @StringRes private val msgId: Int = 0,
) : LoadingInfo.UiObj {
    private var p: ProgressDialog? = null
    override fun show(ctx: AccessCtx) {
        if (p != null) p?.show()
        else {
            val c = ctx.ctx()
            p = c.showLoading(msg ?: c.getString(msgId))
        }
    }

    override fun hide(ctx: AccessCtx) {
        p?.dismiss()
        p = null
    }
}
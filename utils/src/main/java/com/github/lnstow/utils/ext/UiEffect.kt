package com.github.lnstow.utils.ext

import android.app.ProgressDialog
import android.content.Context
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder

inline fun Context.showDialog(
    setDialogListener: AlertDialog.() -> Unit = {},
    block: MaterialAlertDialogBuilder.() -> Unit
): AlertDialog = MaterialAlertDialogBuilder(this).run {
    block()
    val d = create()
    d.setDialogListener()
    d.show()
    d
}

inline fun Fragment.showDialog(
    setDialogListener: AlertDialog.() -> Unit = {},
    block: MaterialAlertDialogBuilder.() -> Unit
) = requireContext().showDialog(setDialogListener, block)

private fun AlertDialog.btn(
    which: Int, text: String, click: (View) -> Unit
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



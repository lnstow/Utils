package com.github.lnstow.utils.ui

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.annotation.ColorRes
import androidx.annotation.LayoutRes
import androidx.core.graphics.Insets
import androidx.core.view.updatePadding
import androidx.fragment.app.FragmentActivity
import com.github.lnstow.utils.R
import com.github.lnstow.utils.ext.AccessAct
import com.github.lnstow.utils.ext.AccessCtx
import com.github.lnstow.utils.ext.Dp
import com.github.lnstow.utils.ext.WIB
import com.github.lnstow.utils.ext.addWindowInsetsPadding
import com.github.lnstow.utils.ext.bgColor
import com.github.lnstow.utils.ext.f
import com.github.lnstow.utils.ext.lightBars
import com.github.lnstow.utils.ext.toPx
import com.github.lnstow.utils.ext.wi
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

abstract class BaseBottomDialog(
    @LayoutRes layoutId: Int = 0,
    @ColorRes private val bgColorId: Int = dialogBehavior.bgColorId,
) : BottomSheetDialogFragment(layoutId), AccessCtx, AccessAct, HandlerHolder {
    override fun ctx(): Context = requireContext()
    override fun act(): FragmentActivity = requireActivity()
    override val hd: PageEventHandler by lazy { FragmentPageEventHandler(this) }

    protected open val draggable = true
    var dismissListener: () -> Unit = {}
    protected open val softInputMode = WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN or
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setStyle(STYLE_NORMAL, dialogBehavior.themeId)
//    }

    override fun getTheme(): Int {
        return dialogBehavior.themeId
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val d = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        initBehavior(d.behavior)
        d.window?.setSoftInputMode(softInputMode)
        if (dialogBehavior.isLightBar != null) setLightBar(dialogBehavior.isLightBar!!)
        edgeToEdgeSetListener(d.window?.decorView)
        return d
    }

    protected open fun edgeToEdgeSetListener(rootView: View?) {
        rootView?.addWindowInsetsPadding(
            consumed = false, barType = WIB.navigationBars() or WIB.ime()
        ) {
            edgeToEdgeUpdateLayout(view?.parent as? View, insets = it)
        }
    }

    protected open fun edgeToEdgeUpdateLayout(parentView: View?, insets: Insets) {
        parentView?.bgBottomDialog()
        parentView?.updatePadding(bottom = insets.bottom + 12.toPx())
    }

    protected open fun initBehavior(behavior: BottomSheetBehavior<FrameLayout>) {
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        behavior.skipCollapsed = true
        behavior.isDraggable = draggable
    }

    override fun onDismiss(dialog: DialogInterface) {
        dismissListener()
        super.onDismiss(dialog)
    }

    protected fun View.bgBottomDialog(
        @ColorRes id: Int = bgColorId,
        @Dp radius: Int = dialogBehavior.bgRadius,
    ) = bgColor(id, 0) {
        val r = radius.toPx().f
        cornerRadii = floatArrayOf(r, r, r, r, 0f, 0f, 0f, 0f)
    }

    protected fun setDim(dim: Float) {
        dialog?.window?.setDimAmount(dim)
    }

    protected fun setDraggable(draggable: Boolean) {
        (dialog as? BottomSheetDialog)?.behavior?.isDraggable = draggable
    }

    protected fun setLightBar(lightBar: Boolean) {
        dialog?.window?.wi?.lightBars(isLight = lightBar)
    }

    companion object {
        lateinit var dialogBehavior: BottomDialogBehavior
    }

    class BottomDialogBehavior(
        @ColorRes val bgColorId: Int,
        @Dp val bgRadius: Int = 24,
        val themeId: Int = R.style.EdgeToEdgeBottomSheetDialog,
        val isLightBar: Boolean? = null,
    )
}
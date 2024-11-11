package com.github.lnstow.utils.ui

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.annotation.LayoutRes
import androidx.fragment.app.FragmentActivity
import com.github.lnstow.utils.R
import com.github.lnstow.utils.ext.AccessAct
import com.github.lnstow.utils.ext.AccessCtx
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

abstract class BaseBottomDialog(@LayoutRes layoutId: Int = 0) :
    BottomSheetDialogFragment(layoutId), AccessCtx, AccessAct, HandlerHolder {
    override fun ctx(): Context = requireContext()
    override fun act(): FragmentActivity = requireActivity()
    override val hd: PageEventHandler by lazy { FragmentPageEventHandler(this) }

    protected open val draggable = true
    var dismissListener: () -> Unit = {}
    protected open val softInputMode = WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN or
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.TransparentBottomSheetDialog)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val d = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        initBehavior(d.behavior)
        d.window?.setSoftInputMode(softInputMode)
        return d
    }

    open fun initBehavior(behavior: BottomSheetBehavior<FrameLayout>) {
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        behavior.skipCollapsed = true
        behavior.isDraggable = draggable
    }

    override fun onDismiss(dialog: DialogInterface) {
        dismissListener()
        super.onDismiss(dialog)
    }
}
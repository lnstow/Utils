package com.github.lnstow.utils.ui

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.github.lnstow.utils.ext.AccessAct
import com.github.lnstow.utils.ext.AccessCtx

abstract class BaseFrag(@LayoutRes layoutId: Int = 0) : Fragment(layoutId),
    AccessCtx, AccessAct, HandlerHolder {
    override fun ctx(): Context = requireContext()
    override fun act(): FragmentActivity = requireActivity()
    override val hd: PageEventHandler by lazy { FragmentPageEventHandler(this) }
    open val tb: CustomToolbar? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    abstract fun initView()
}
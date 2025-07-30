package com.github.lnstow.utils.ui

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.github.lnstow.utils.ext.AccessAct
import com.github.lnstow.utils.ext.AccessCtx
import kotlinx.coroutines.CoroutineScope

abstract class BaseFrag(@LayoutRes layoutId: Int = 0) : Fragment(layoutId),
    AccessCtx, AccessAct, HandlerHolder {
    override fun ctx(): Context = requireContext()
    override fun act(): FragmentActivity = requireActivity()
    override val hd: PageEventHandler by lazy { FragmentPageEventHandler(this) }
    protected val scope: CoroutineScope get() = viewLifecycleOwner.lifecycleScope

    open val tb: CustomToolbar? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        view.isClickable = true // 让最上层fragment拦截点击事件
        initView()
    }

    abstract fun initView()
}
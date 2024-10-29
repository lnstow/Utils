package com.github.lnstow.utils.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.github.lnstow.utils.ext.AccessAct
import com.github.lnstow.utils.ext.AccessCtx
import com.github.lnstow.utils.ext.MATCH
import com.github.lnstow.utils.ext.VGLP
import com.github.lnstow.utils.ext.toPx

abstract class BaseFrag(@LayoutRes private val layoutId: Int = 0) : Fragment(layoutId),
    AccessCtx, AccessAct, HandlerHolder {
    override fun ctx(): Context = requireContext()
    override fun act(): FragmentActivity = requireActivity()
    override val hd: PageEventHandler by lazy { FragmentPageEventHandler(this) }
    open val toolbar: CustomToolbar? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (toolbar == null || layoutId == 0)
            return super.onCreateView(inflater, container, savedInstanceState)
        return FrameLayout(inflater.context).apply {
            layoutParams = VGLP(MATCH, MATCH)
            toolbar!!.attachTo(this)
            addView(
                inflater.inflate(layoutId, container, false),
                FrameLayout.LayoutParams(MATCH, MATCH).apply {
                    topMargin = toolbar!!.heightDp.toPx()
                }
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    abstract fun initView()
}
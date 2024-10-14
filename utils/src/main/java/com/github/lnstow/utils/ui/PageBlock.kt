package com.github.lnstow.utils.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.annotation.LayoutRes
import androidx.viewbinding.ViewBinding
import com.github.lnstow.utils.ext.Dp
import com.github.lnstow.utils.ext.MATCH
import com.github.lnstow.utils.ext.WRAP
import com.github.lnstow.utils.ext.toPx

typealias ClickEv = (View) -> Unit
typealias ClickEv2 = () -> Unit

//interface PageBlock
// object PBM {
//
// //    fun attachTo
// }

@Suppress("FunctionNaming")
fun PageBlockContainerLayout(ctx: Context) = LinearLayout(ctx).apply {
    fitsSystemWindows = true
    layoutParams = com.github.lnstow.utils.ext.VGLP(MATCH, MATCH)
    orientation = LinearLayout.VERTICAL
    isClickable = true
//    bgColor(R.color.tkc_content_bg_white)
}

@Suppress("FunctionNaming")
fun PageBlockContainerLayoutForBottomDialog(ctx: Context, @Dp heightDp: Int = WRAP) =
    LinearLayout(ctx).apply {
        layoutParams =
            com.github.lnstow.utils.ext.VGLP(MATCH, if (heightDp == WRAP) WRAP else heightDp.toPx())
        orientation = LinearLayout.VERTICAL
        isClickable = true
//        background = getDrawableById(R.drawable.bg_dialog_bottom)
    }

abstract class PageBlockFragment : BaseFrag() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
//        return super.onCreateView(inflater, container, savedInstanceState)
        return PageBlockContainerLayout(requireContext())
    }

    override fun initView() {
        (requireView() as LinearLayout).initPageBlock()
    }

    abstract fun LinearLayout.initPageBlock()
}

abstract class ViewBindingFragment(@LayoutRes layoutId: Int) : BaseFrag(layoutId) {
    protected abstract val vb: ViewBinding
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
////        return super.onCreateView(inflater, container, savedInstanceState)
//        return vb.root
//    }
}
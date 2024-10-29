package com.github.lnstow.utils.ui

import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.github.lnstow.utils.LnUtils
import com.github.lnstow.utils.ext.LaunchParams
import com.github.lnstow.utils.ext.collectEvent
import com.github.lnstow.utils.ext.finish
import com.github.lnstow.utils.ext.log
import com.github.lnstow.utils.ext.showToast
import kotlinx.coroutines.flow.SharedFlow

/** 用法
 * 1. 在[PageEvent]添加事件
 * 2. 在[PageEventHandler]添加事件回调
 * 3. 在[HandlerHolder.bindEvent]绑定事件 并委托[HandlerHolder.hd]实现新回调
 */
interface PageEvent {
    val peBackPressed: SharedFlow<Unit>
    val peNavigate: SharedFlow<LaunchParams>
    val peFinish: SharedFlow<Unit>
}

interface PageEventHandler {
    fun <T> bindFlow(flow: SharedFlow<T>, cb: (T) -> Unit)

    fun peError(e: Throwable)
    fun peBackPressed(u: Unit = Unit)
    fun peNavigate(lp: LaunchParams, startFrag: (Fragment) -> Unit = this::peStartFrag)
    fun peStartFrag(frag: Fragment) {
        frag.showToast("${this::class.simpleName} 需要实现peStartFrag方法", shortShow = false)
    }

    fun peFinish(u: Unit = Unit)
}

/** 注意！
 * 1. 在[PageEventHandler]添加新方法后，建议在此接口中实现方法（委托[hd]）。
 * 2. 在使用共享vm时，由于[HandlerHolder.bindEvent]可能是 通过frag的父级act调用的，
 * 所以如果frag需要特殊处理某个方法，先在act中重写对应方法，并在方法中分派给frag处理 */
interface HandlerHolder : PageEventHandler {
    val hd: PageEventHandler
    override fun <T> bindFlow(flow: SharedFlow<T>, cb: (T) -> Unit) = hd.bindFlow(flow, cb)

    fun bindEvent(pe: PageEvent) {
        bindFlow(pe.peBackPressed, this::peBackPressed)
        bindFlow(pe.peNavigate, this::peNavigate)
        bindFlow(pe.peFinish, this::peFinish)
    }

    override fun peError(e: Throwable) = hd.peError(e)
    override fun peBackPressed(u: Unit) = hd.peBackPressed(u)
    override fun peNavigate(lp: LaunchParams, startFrag: (Fragment) -> Unit) =
        hd.peNavigate(lp, this::peStartFrag)

    override fun peStartFrag(frag: Fragment) = hd.peStartFrag(frag)

    override fun peFinish(u: Unit) = hd.peFinish(u)
}

class ActivityPageEventHandler(private val act: FragmentActivity) : PageEventHandler {
    override fun <T> bindFlow(flow: SharedFlow<T>, cb: (T) -> Unit) {
        act.collectEvent(flow, cb)
    }

    override fun peError(e: Throwable) {
        e.stackTraceToString().log("peError")
        TODO("Not yet implemented")
    }

    override fun peBackPressed(u: Unit) {
        act.onBackPressedDispatcher.onBackPressed()
    }

    override fun peNavigate(lp: LaunchParams, startFrag: (Fragment) -> Unit) {
        val frag = LnUtils.nav.navigate(lp, act) ?: return
        if (frag is DialogFragment) frag.show(act.supportFragmentManager, frag.tag)
        else startFrag(frag)
    }

    override fun peFinish(u: Unit) {
        act.finishAfterTransition()
    }

    override fun peStartFrag(frag: Fragment) {
        if (act is HasFragContainer) act.add(frag)
        else super.peStartFrag(frag)
    }
}

class FragmentPageEventHandler(
    private val frag: Fragment,
    private val del: PageEventHandler = ActivityPageEventHandler(frag.requireActivity())
) : PageEventHandler by del {
    override fun <T> bindFlow(flow: SharedFlow<T>, cb: (T) -> Unit) {
        frag.collectEvent(flow, cb)
    }

    override fun peNavigate(lp: LaunchParams, startFrag: (Fragment) -> Unit) {
        val frag = LnUtils.nav.navigate(lp, frag) ?: return
        if (frag is DialogFragment) frag.show(this.frag.childFragmentManager, frag.tag)
        else startFrag(frag)
    }

    override fun peFinish(u: Unit) {
        frag.finish()
    }

    override fun peStartFrag(frag: Fragment) {
        if (this.frag is HasFragContainer) this.frag.add(frag)
        else del.peStartFrag(frag)
    }
}

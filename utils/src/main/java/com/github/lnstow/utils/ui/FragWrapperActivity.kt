package com.github.lnstow.utils.ui

import android.os.Bundle
import android.view.View
import androidx.annotation.AnimRes
import androidx.annotation.AnimatorRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.FragmentManager
import com.github.lnstow.utils.LnUtils
import com.github.lnstow.utils.R

interface HasFragContainer {
    val containerId: Int
    fun getFragManager(): FragmentManager
    fun add(frag: Fragment) {
        getFragManager().addFragWithAnimInOut(frag, containerId)
    }
}

abstract class FragWrapperActivity : BaseAct(), HasFragContainer {
    /** usually R.id.container */
    override val containerId: Int = R.id.frag_container
    override fun getFragManager(): FragmentManager = supportFragmentManager

    final override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onCreateContainer()
    }

    open fun onCreateContainer(): View {
        return FragmentContainerView(this).apply {
            id = containerId
            setContentView(this)
            if (getFragManager().fragments.isEmpty())
                add(initFrag())
        }
    }

    abstract fun initFrag(): Fragment
    final override fun initView() {}
}

fun FragmentManager.addFragWithAnimInOut(
    frag: Fragment,
    containerId: Int,
    isAddStack: Boolean = true,
    @AnimatorRes @AnimRes topEnter: Int = LnUtils.pageAnim.topEnter,
    @AnimatorRes @AnimRes bottomExit: Int = LnUtils.pageAnim.bottomExit,
    @AnimatorRes @AnimRes bottomEnter: Int = LnUtils.pageAnim.bottomEnter,
    @AnimatorRes @AnimRes topExit: Int = LnUtils.pageAnim.topExit,
) {
    val tr = beginTransaction()
    val f = fragments.lastOrNull()
    tr.setCustomAnimations(topEnter, bottomExit, bottomEnter, topExit)
    tr.add(containerId, frag, frag.hashCode().toString())
    if (f != null) tr.hide(f)
    if (isAddStack && f != null) tr.addToBackStack(null)
    tr.commitAllowingStateLoss()
}

fun FragmentManager.replaceFragWithAnimInOut(
    frag: Fragment,
    containerId: Int,
    @AnimatorRes @AnimRes topEnter: Int = LnUtils.pageAnim.topEnter,
    @AnimatorRes @AnimRes bottomExit: Int = LnUtils.pageAnim.bottomExit,
    @AnimatorRes @AnimRes bottomEnter: Int = LnUtils.pageAnim.bottomEnter,
    @AnimatorRes @AnimRes topExit: Int = LnUtils.pageAnim.topExit,
) {
    val tr = beginTransaction()
    tr.setCustomAnimations(topEnter, bottomExit, bottomEnter, topExit)
    tr.replace(containerId, frag, frag.hashCode().toString())
    tr.commitAllowingStateLoss()
}


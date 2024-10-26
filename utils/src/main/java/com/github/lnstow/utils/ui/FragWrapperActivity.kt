package com.github.lnstow.utils.ui

import android.os.Bundle
import androidx.annotation.AnimRes
import androidx.annotation.AnimatorRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.FragmentManager
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

    open fun onCreateContainer() {
        FragmentContainerView(this).apply {
            id = containerId
            setContentView(this)
            if (getFragManager().fragments.isEmpty())
                add(initFrag())
        }
    }

    abstract fun initFrag(): Fragment
    override fun initView() {}
}

@Suppress("FunctionNaming")
fun FragmentManager.addFragWithAnimInOut(
    frag: Fragment,
    containerId: Int,
    isAddStack: Boolean = true,
    @AnimatorRes @AnimRes enter: Int = R.anim.frag_slide_in_right_anim,
    @AnimatorRes @AnimRes exit: Int = R.anim.frag_slide_out_right_anim,
    @AnimatorRes @AnimRes popEnter: Int = enter,
    @AnimatorRes @AnimRes popExit: Int = exit,
) {
    val tr = beginTransaction()
    tr.setCustomAnimations(enter, exit, popEnter, popExit)
    tr.add(containerId, frag, frag.hashCode().toString())
    if (isAddStack && fragments.isNotEmpty()) tr.addToBackStack(null)
    tr.commitAllowingStateLoss()
}

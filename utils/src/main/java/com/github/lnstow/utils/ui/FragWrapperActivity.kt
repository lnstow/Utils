package com.github.lnstow.utils.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.FragmentManager
import com.github.lnstow.utils.LnUtils

interface HasFragContainer {
    val containerId: Int
    fun getFragManager(): FragmentManager
    fun add(frag: Fragment) {
        getFragManager().addFragAnimFadeInOut(frag, containerId)
    }
}

abstract class FragWrapperActivity : BaseAct(), HasFragContainer {
    /** usually R.id.container */
    override val containerId: Int = LnUtils.resId.container
    override fun getFragManager(): FragmentManager = supportFragmentManager

    final override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onCreateContainer()
    }

    open fun onCreateContainer() {
        FragmentContainerView(this).apply {
            id = containerId
            setContentView(this)
            add(initFrag())
        }
    }

    abstract fun initFrag(): Fragment
    override fun initView() {}
}

@Suppress("FunctionNaming")
fun FragmentManager.addFragAnimFadeInOut(
    frag: Fragment,
    containerId: Int,
    isAddStack: Boolean = true
) {
    val tr = beginTransaction()
    tr.add(containerId, frag, frag.hashCode().toString())
    if (isAddStack && fragments.isNotEmpty()) tr.addToBackStack(null)
    tr.commitAllowingStateLoss()
//    FragmentUtils.add(
//        this, add, containerId, isAddStack,
//        R.anim.activity_fade_in, R.anim.activity_fade_out,
//        R.anim.activity_fade_in, R.anim.activity_fade_out
//    )
}

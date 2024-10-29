package com.github.lnstow.utils.ext

import android.content.Context
import androidx.activity.OnBackPressedCallback
import androidx.core.view.postDelayed
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

/**
 * 监听返回键事件
 *
 * @param enable 返回布尔值表示 是否处理返回事件，阻止事件继续分发
 * @param onBackPressed 当处理返回时，需要执行的操作
 * @return
 */
inline fun Fragment.backPress(
    crossinline enable: () -> Boolean = { true },
    crossinline onBackPressed: OnBackPressedCallback.() -> Unit
): OnBackPressedCallback {
    val callback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (enable()) onBackPressed()
            else {
                isEnabled = false
                requireActivity().onBackPressedDispatcher.onBackPressed()
                isEnabled = true
            }
        }
    }
    /** 注意！
     * 下面一行[addCallback]不能使用[viewLifecycleOwner]，必须使用[activity]的生命周期。
     * 因为[addCallback]内部用 生命周期观察者包装了回调，应用切换到后台时，回调都被取消，
     * 应用重回前台时，回调都被重新添加。！！！但是 act和frag 生命周期的回调顺序不一样，
     * 这里如果使用frag的生命周期，重新添加时，所有frag回调都会被放置在所有act回调之前，
     * 导致在反向遍历返回栈时，act的回调先于frag触发，打乱了原本代码中设置的回调顺序，
     * 只有使用相同的生命周期 才能保证 应用切换后 重新添加的回调 保持原顺序 */
    requireActivity().onBackPressedDispatcher.addCallback(requireActivity(), callback)

    viewLifecycleOwner.lifecycle.addObserver(LifecycleEventObserver { _, event ->
        if (event == Lifecycle.Event.ON_DESTROY) callback.remove()
    })
    return callback
}

inline fun FragmentActivity.backPress(
    crossinline enable: () -> Boolean = { true },
    crossinline onBackPressed: OnBackPressedCallback.() -> Unit
): OnBackPressedCallback {
    val callback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (enable()) onBackPressed()
            else {
                isEnabled = false
                onBackPressedDispatcher.onBackPressed()
                isEnabled = true
            }
        }
    }
    onBackPressedDispatcher.addCallback(this, callback)
    return callback
}

fun Fragment.onBackPressed() = requireActivity().onBackPressedDispatcher.onBackPressed()
fun Fragment.finish() =
    if (parentFragmentManager.backStackEntryCount == 0) onBackPressed()
    else parentFragmentManager.popBackStack()

fun FragmentActivity.onBackPressed2() = onBackPressedDispatcher.onBackPressed()
fun FragmentActivity.doubleBackToExit(toastStrId: Int, timeout: Long = 2000) {
    var exit = false
    backPress(enable = { !exit }) {
        exit = true
        showToast(getString(toastStrId))
        window.decorView.postDelayed(timeout) { exit = false }
    }
}

interface AccessCtx {
    fun ctx(): Context
}

interface AccessAct {
    fun act(): FragmentActivity
}
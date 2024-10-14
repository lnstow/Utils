package com.github.lnstow.utils.ui

import android.content.Context
import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import com.github.lnstow.utils.ext.ApiError
import com.github.lnstow.utils.ext.defaultCatch
import com.github.lnstow.utils.ext.myApp
import com.github.lnstow.utils.ext.showDialog
import com.github.lnstow.utils.ext.showToast
import com.github.lnstow.utils.util.CrashHandler
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

abstract class BaseAct(@LayoutRes layoutId: Int = 0) : AppCompatActivity(layoutId) {
    override fun onCreate(savedInstanceState: Bundle?) {
        actList.add(this)
        super.onCreate(savedInstanceState)
        initView()
    }

    override fun onStart() {
        super.onStart()
        // FIXME: 2024/10/14 调整代码位置
//        UserShelf.checkUpdate()
    }

    override fun onResumeFragments() {
        super.onResumeFragments()
        CrashHandler.checkCrash()
    }

    override fun onDestroy() {
        actList.remove(this)
        super.onDestroy()
    }

    abstract fun initView()

    open fun onCatchException(err: Throwable) {
        if (err is ApiError) showDialog { setMessage(err.message) }
        else err.defaultCatch()
    }

    companion object {
        private val actList = mutableListOf<BaseAct>()
        val top: Context get() = actList.lastOrNull() ?: myApp
        val bottom: Context get() = actList.firstOrNull() ?: myApp
        inline fun topUi(crossinline block: BaseAct.() -> Unit) {
            val act = top as? BaseAct ?: return
            act.runOnUiThread { act.block() }
        }

        init {
            GlobalScope.launch {
                BaseVm.toast.collect {
                    topUi {
                        showToast(it)
                    }
                }
            }
            GlobalScope.launch {
                BaseVm.err.collect {
                    topUi {
                        onCatchException(it)
                    }
                }
            }
        }
    }
}
package com.github.lnstow.utils.ui

import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.github.lnstow.utils.ext.AccessAct
import com.github.lnstow.utils.ext.AccessCtx
import com.github.lnstow.utils.ext.ApiError
import com.github.lnstow.utils.ext.LoadingDef
import com.github.lnstow.utils.ext.LoadingInfo
import com.github.lnstow.utils.ext.ToastInfo
import com.github.lnstow.utils.ext.addWindowInsetsPadding
import com.github.lnstow.utils.ext.defaultCatch
import com.github.lnstow.utils.ext.lightBars
import com.github.lnstow.utils.ext.showDialog
import com.github.lnstow.utils.ext.wi
import com.github.lnstow.utils.util.CrashHandler
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

abstract class BaseAct(@LayoutRes layoutId: Int = 0) : AppCompatActivity(layoutId),
    AccessCtx, AccessAct, HandlerHolder {
    override fun ctx(): Context = this
    override fun act(): FragmentActivity = this
    override val hd: PageEventHandler by lazy { ActivityPageEventHandler(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        actList.add(this)
        if (actBehavior.enableEdgeToEdge) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                window.isNavigationBarContrastEnforced = false
            setEdgeToEdge()
        }
        super.onCreate(savedInstanceState)
        initView()
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
        if (err is ApiError) onCatchApiError(err)
        else onCatchOtherException(err)
    }

    open fun onCatchApiError(err: ApiError) {
        actBehavior.onCatchApiError(this, err)
    }

    open fun onCatchOtherException(err: Throwable) {
        actBehavior.onCatchOtherException(this, err)
    }

    open fun onShowToast(toast: ToastInfo) {
        lifecycleScope.launchWhenResumed { toast.showToast(this@BaseAct) }
    }

    open fun setEdgeToEdge() {
        actBehavior.setEdgeToEdge(this)
    }

    protected var loadingUi: LoadingInfo.UiObj? = null
    fun showLoading(info: LoadingInfo) {
        hideLoading()
        loadingUi = info.createAndShow(this)
    }

    fun hideLoading() {
        actList.forEach {
            it.loadingUi?.hide(it)
            it.loadingUi = null
        }
    }

    @OptIn(FlowPreview::class)
    companion object : HandlerHolder {
        val actList = mutableListOf<BaseAct>()
        val top: BaseAct get() = actList.last()
        val bottom: BaseAct get() = actList.first()
        inline fun topUi(crossinline block: BaseAct.() -> Unit) {
            val act = top
            act.runOnUiThread { act.block() }
        }

        fun clearOtherActs(currAct: BaseAct?) {
            actList.toList().forEach {
                if (it != currAct) it.finish()
            }
        }

        fun finishPreviousAct() {
            actList[actList.lastIndex - 1].finish()
        }

        init {
            GlobalScope.launch {
                BaseVm.toast.debounce(200).collect {
                    topUi {
                        onShowToast(it)
                    }
                }
            }
            GlobalScope.launch {
                BaseVm.err.debounce(300).collect {
                    topUi {
                        onCatchException(it)
                    }
                }
            }
            GlobalScope.launch {
                BaseVm.loading.debounce(500).collect {
                    topUi {
                        if (it == null) hideLoading()
                        else showLoading(it)
                    }
                }
            }
//            TODO("refactor err and toast")
            bindEvent(BaseVm.Companion)
        }

        lateinit var actBehavior: ActBehavior

        override val hd: PageEventHandler
            get() = ActivityPageEventHandler(top as BaseAct)

        override fun <T> bindFlow(flow: SharedFlow<T>, cb: (T) -> Unit) {
            GlobalScope.launch {
                flow.collect { topUi { cb(it) } }
            }
        }
    }

    class ActBehavior(
        val enableEdgeToEdge: Boolean,
        val setEdgeToEdge: (BaseAct) -> Unit = {
            it.enableEdgeToEdge()
            it.window.wi.lightBars()
            it.window.decorView.addWindowInsetsPadding()
        },
        val loadingInfo: LoadingInfo? = LoadingDef("Loading..."),
        val onCatchApiError: (BaseAct, ApiError) -> Unit = { act, err ->
            act.showDialog { setMessage(err.message) }
        },
        val onCatchOtherException: (BaseAct, Throwable) -> Unit = { _, err ->
            err.defaultCatch()
        },
    )
}
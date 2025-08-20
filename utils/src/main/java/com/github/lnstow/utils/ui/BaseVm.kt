package com.github.lnstow.utils.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.lnstow.utils.ext.DSP_IO
import com.github.lnstow.utils.ext.LaunchParams
import com.github.lnstow.utils.ext.LoadingInfo
import com.github.lnstow.utils.ext.ToastInfo
import com.github.lnstow.utils.ui.BaseVm.Companion.emit2
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

abstract class BaseVm : ViewModel(), StateHolder {
    override val scope: CoroutineScope get() = viewModelScope

    protected inline fun launchIn(
        loadingInfo: LoadingInfo? = BaseAct.actBehavior.loadingInfo,
        noinline onError: (Exception) -> Unit = onError2,
        crossinline block: suspend CoroutineScope.() -> Unit,
    ) = viewModelScope.launchInLoading(loadingInfo, onError, block)

    //    protected inline fun <T> LiveData<T>.update(v: T.() -> T) = set(valueNN.v())
    //    protected fun <T> LiveData<T>.repost() = post(valueNN)

    /** 返回true表示子类需要启动观察者 */
//    protected open fun MediatorLiveData<*>.observeOther() = false
//    private val observeOther = viewModelScope.launch {
//        delay(300)
//        MediatorLiveData<Unit>().apply {
//            if (observeOther()) asFlow().launchIn(this@launch)
//        }
//    }

    protected fun <T> collectEvent(
        flow: Flow<T>,
        dsp: CoroutineDispatcher = Dispatchers.Default,
        block: suspend (T) -> Unit,
    ) = viewModelScope.launch(dsp) { flow.collect(block) }

    companion object : StateHolder, PageEvent {
        val loading = asStateFlow<LoadingInfo?>(null)
        val toast = asEventFlowMultiPage<ToastInfo>(onlyLastEvent = true)
        val err = asEventFlowMultiPage<Throwable>(onlyLastEvent = false)
        override val peBackPressed: SharedFlow<Unit> = asEventFlow()
        override val peNavigate: SharedFlow<LaunchParams> = asEventFlow()
        override val peFinish: SharedFlow<Unit> = asEventFlow()
    }
}

val appScope = CoroutineScope(SupervisorJob() + DSP_IO)
val onError2 = { e: Throwable -> BaseVm.err.emit2(e) }

inline fun CoroutineScope.launchInLoading(
    loadingInfo: LoadingInfo? = BaseAct.actBehavior.loadingInfo,
    noinline onError: (Exception) -> Unit = onError2,
    crossinline block: suspend CoroutineScope.() -> Unit,
) = launch(DSP_IO) {
    try {
        if (loadingInfo != null) BaseVm.loading.emit(loadingInfo)
        block()
    } catch (e: Exception) {
        this.ensureActive()
        onError(e)
    } finally {
        if (loadingInfo != null) BaseVm.loading.emit(null)
    }
}

interface StateHolder {
//    fun <T> LiveData<T>.post(v: T) = (this as MutableLiveData).postValue(v)
//    fun <T> LiveData<T>.set(v: T) = (this as MutableLiveData).setValue(v)
//    fun <T> asLiveData(v: T): LiveData<T> = MutableLiveData(v)
//    fun <T> asLiveData(): LiveData<T> = MutableLiveData()

    val scope: CoroutineScope get() = appScope

    fun <T> SharedFlow<T>.emit2(value: T) {
        this as MutableSharedFlow
        if (!tryEmit(value)) scope.launch { emit(value) }
    }

    fun <T> StateFlow<T>.emit2(value: T) {
        (this as MutableStateFlow).tryEmit(value)
    }

    fun <T> asStateFlow(value: T): MutableStateFlow<T> = MutableStateFlow(value)

    // sharedFlow参数详解 https://itnext.io/mutablesharedflow-is-kind-of-complicated-61af68011eae
    /** 将[SharedFlow]配置为一个事件流，默认没有粘性事件，且存在收集者时，每个事件都被处理，不丢弃事件
     * 此方法创建的事件流，只能在单个页面（一个activity和他的多个子fragment）使用，解释见下面另一个方法 */
    fun <T> asEventFlow(
        replay: Int = 0,
        extraBufferCapacity: Int = 0,
        onBufferOverflow: BufferOverflow = BufferOverflow.SUSPEND,
    ): MutableSharedFlow<T> = MutableSharedFlow(replay, extraBufferCapacity, onBufferOverflow)

    /** 创建一个可在多个页面观察的事件流。当多个页面使用[launchWhenStarted]收集事件流时，
     * 使用[BufferOverflow.SUSPEND]，缓冲区溢出后，需要等待所有收集者都处理完事件 才会发出下一个事件，
     * 前一个页面处于onStop状态，收集者被挂起，无法处理完事件，导致当前页面的收集者也被卡住收不到新事件，
     * 使用[BufferOverflow.DROP_OLDEST]，缓冲区溢出后，当前页面收集者仍可处理新事件，
     * 前一个页面的收集者会在前一个页面重新显示后，收到缓冲区中的所有事件并处理。
     * @param onlyLastEvent 收集者从挂起恢复后，是否只接收处理最后一次事件 */
    fun <T> asEventFlowMultiPage(
        onlyLastEvent: Boolean,
    ): MutableSharedFlow<T> = MutableSharedFlow(
        0, if (onlyLastEvent) 1 else 500, BufferOverflow.DROP_OLDEST
    )
}

//@JvmInline
//value class VmStr(val s: String) {
//    constructor(@StringRes id: Int) : this(id.s)
//}
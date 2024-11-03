package com.github.lnstow.utils.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import com.github.lnstow.utils.ext.DSP_IO
import com.github.lnstow.utils.ext.LaunchParams
import com.github.lnstow.utils.ext.ToastInfo
import com.github.lnstow.utils.ext.valueNN
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch

abstract class BaseVm : ViewModel(), StateHolder {

    protected inline fun launchIn(
        crossinline onError: (Throwable) -> Unit = { (err as MutableSharedFlow).tryEmit(it) },
        crossinline block: suspend CoroutineScope.() -> Unit
    ) = viewModelScope.launch(DSP_IO) {
        try {
            block()
        } catch (e: Throwable) {
            onError(e)
        }
    }

    protected fun <T> SharedFlow<T>.emit(value: T) {
        this as MutableSharedFlow
        if (!tryEmit(value)) viewModelScope.launch { emit(value) }
    }

    protected inline fun <T> LiveData<T>.update(v: T.() -> T) = set(valueNN.v())
    protected fun <T> LiveData<T>.repost() = post(valueNN)
    protected inline fun <T> StateFlow<T>.update(v: T.() -> T) {
        (this as MutableStateFlow).updateAndGet(v)
    }

    /** 返回true表示子类需要启动观察者 */
    protected open fun MediatorLiveData<*>.observeOther() = false
    private val observeOther = viewModelScope.launch {
        delay(300)
        MediatorLiveData<Unit>().apply {
            if (observeOther()) asFlow().launchIn(this@launch)
        }
    }

    companion object : StateHolder, PageEvent {
        val toast = asEventFlowMultiPage<ToastInfo>(onlyLastEvent = true)
        val err = asEventFlowMultiPage<Throwable>(onlyLastEvent = false)
        override val peBackPressed: SharedFlow<Unit> = asEventFlow()
        override val peNavigate: SharedFlow<LaunchParams> = asEventFlow()
        override val peFinish: SharedFlow<Unit> = asEventFlow()
    }
}

interface StateHolder {
    fun <T> LiveData<T>.post(v: T) = (this as MutableLiveData).postValue(v)
    fun <T> LiveData<T>.set(v: T) = (this as MutableLiveData).setValue(v)
    fun <T> asLiveData(v: T): LiveData<T> = MutableLiveData(v)
    fun <T> asLiveData(): LiveData<T> = MutableLiveData()

    fun <T> StateFlow<T>.emit(value: T) {
        (this as MutableStateFlow).tryEmit(value)
    }

    fun <T> asStateFlow(value: T): StateFlow<T> = MutableStateFlow(value)

    // sharedFlow参数详解 https://itnext.io/mutablesharedflow-is-kind-of-complicated-61af68011eae
    /** 将[SharedFlow]配置为一个事件流，默认没有粘性事件，且存在收集者时，每个事件都被处理，不丢弃事件
     * 此方法创建的事件流，只能在单个页面（一个activity和他的多个子fragment）使用，解释见下面另一个方法 */
    fun <T> asEventFlow(
        replay: Int = 0,
        extraBufferCapacity: Int = 0,
        onBufferOverflow: BufferOverflow = BufferOverflow.SUSPEND
    ): SharedFlow<T> = MutableSharedFlow(replay, extraBufferCapacity, onBufferOverflow)

    /** 创建一个可在多个页面观察的事件流。当多个页面使用[launchWhenStarted]收集事件流时，
     * 使用[BufferOverflow.SUSPEND]，缓冲区溢出后，需要等待所有收集者都处理完事件 才会发出下一个事件，
     * 前一个页面处于onStop状态，收集者被挂起，无法处理完事件，导致当前页面的收集者也被卡住收不到新事件，
     * 使用[BufferOverflow.DROP_OLDEST]，缓冲区溢出后，当前页面收集者仍可处理新事件，
     * 前一个页面的收集者会在前一个页面重新显示后，收到缓冲区中的所有事件并处理。
     * @param onlyLastEvent 收集者从挂起恢复后，是否只接收处理最后一次事件 */
    fun <T> asEventFlowMultiPage(
        onlyLastEvent: Boolean,
    ): SharedFlow<T> = MutableSharedFlow(
        0, if (onlyLastEvent) 1 else 500, BufferOverflow.DROP_OLDEST
    )
}

//@JvmInline
//value class VmStr(val s: String) {
//    constructor(@StringRes id: Int) : this(id.s)
//}
package com.github.lnstow.utils.ext

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Created by jason on 2020/12/23
 */

/** 获取非空值，valueNonNull */
val <T> LiveData<T>.valueNN get() = value!!

@Suppress("FunctionNaming")
inline fun <T> MediatorLiveData(
    init: T?,
    vararg source: LiveData<*>,
    crossinline onEach: MediatorLiveData<T>.() -> Unit
) = MediatorLiveData<T>().apply {
    if (init != null) value = init
    addSources(*source, onEach = onEach)
}

inline fun <T> MediatorLiveData<T>.addSources(
    vararg source: LiveData<*>,
    crossinline onEach: MediatorLiveData<T>.() -> Unit
) {
    val onChange = { _: Any -> onEach() }
    source.forEach { addSource(it, onChange) }
}

/**
 * 避免在 Fragment 使用 this 来观察 Livedata
 */
fun <T> Fragment.observe(liveData: LiveData<T>, observer: Observer<T>) {
    liveData.observe(viewLifecycleOwner, observer)
}

fun <T> FragmentActivity.observe(liveData: LiveData<T>, observer: Observer<T>) {
    liveData.observe(this, observer)
}

inline fun LifecycleOwner.observeLifecycle(crossinline state: (Lifecycle.Event) -> Unit) =
    lifecycle.addObserver(LifecycleEventObserver { _, ev -> state(ev) })

inline fun Lifecycle.Event.onDestroy(block: () -> Unit): Lifecycle.Event {
    if (this == Lifecycle.Event.ON_DESTROY) block()
    return this
}

fun LifecycleOwner.launch(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> Unit
) = lifecycleScope.launch(context, start, block)

fun LifecycleOwner.repeatOnLifecycle(
    state: Lifecycle.State = Lifecycle.State.RESUMED,
    block: suspend CoroutineScope.() -> Unit
) = launch { lifecycle.repeatOnLifecycle(state, block) }

/** 收集事件流时，建议使用[launchWhenStarted]，因为[repeatOnLifecycle]会在应用被切换到后台时，
 * 取消收集者，如果 事件流是一个网络请求后的延迟事件，没有观察者的情况下发射新值，
 * 收集者重新观察时 已经丢失事件。而[launchWhenStarted]则是在后台挂起收集者，保证存在收集者
 * 并且可以在切换回前台后 收到事件流缓冲区的值或者挂起的emit值 */
inline fun <T> LifecycleOwner.collectEvent(
    flow: SharedFlow<T>,
    noinline action: suspend (T) -> Unit
) = lifecycleScope.launchWhenStarted { flow.collect(action) }

fun Fragment.launch(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> Unit
) = viewLifecycleOwner.launch(context, start, block)

fun Fragment.repeatOnLifecycle(
    state: Lifecycle.State = Lifecycle.State.RESUMED,
    block: suspend CoroutineScope.() -> Unit
) = viewLifecycleOwner.repeatOnLifecycle(state, block)

inline fun <T> Fragment.collectEvent(
    flow: SharedFlow<T>,
    noinline action: suspend (T) -> Unit
) = viewLifecycleOwner.collectEvent(flow, action)

inline fun <T> LifecycleOwner.observe(
    flow: StateFlow<T>,
    noinline action: suspend (T) -> Unit
) = repeatOnLifecycle(Lifecycle.State.STARTED) { flow.collect(action) }

inline fun <T> Fragment.observe(
    flow: StateFlow<T>,
    noinline action: suspend (T) -> Unit
) = viewLifecycleOwner.observe(flow, action)

@Suppress("ConstructorParameterNaming")
@JvmInline
value class FlowUIWrapper(val __private_: CoroutineScope) {
    inline fun <T> collect(
        flow: StateFlow<T>,
        noinline action: suspend (T) -> Unit
    ) = __private_.launch { flow.collect(action) }

    inline fun <T> StateFlow<T>.collectIn(
        noinline action: suspend (T) -> Unit
    ) = collect(this, action)
}

inline fun LifecycleOwner.flowUI(crossinline block: FlowUIWrapper.() -> Unit) =
    repeatOnLifecycle(Lifecycle.State.STARTED) {
        FlowUIWrapper(this).block()
    }

inline fun Fragment.flowUI(crossinline block: FlowUIWrapper.() -> Unit) =
    viewLifecycleOwner.flowUI(block)

inline fun <T> LifecycleOwner.debounce(
    crossinline debounceAttach: ((T) -> Unit) -> Any,
    delay: Long = 300,
    crossinline debounceBlock: (T) -> Unit,
) {
    var job: Job? = null
    debounceAttach {
        job?.cancel()
        job = launch {
            delay(delay)
            debounceBlock(it)
        }
    }
}

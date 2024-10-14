@file:Suppress("CommentSpacing")

package com.github.lnstow.utils.ext

import com.github.lnstow.utils.ui.BaseAct
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*

/**
 * 用来定义repo中方法（网络请求，数据库加载）的返回值，
 * 会有三种状态（加载中，成功，失败），
 * 在viewModel中根据状态取出结果
 */
sealed class Result<out T> {
    object Loading : Result<Nothing>()

    /**
     * 首先因为 多态 可以 父类引用 指向 子类对象，
     * 所以当 返回值定义为 Result<T> 时，可以返回 Result<T> 的子类型。
     *
     * 使用 out T 泛型协变，假设 R ：T ，则有 Result<R> ：Result<T>，
     * 由于 Nothing 是底部类型（所有类型的子类），即 Nothing : T，
     * 所以 Result<Nothing> 也是 Result<T> 的子类型。
     */
    data class Success<out T>(val data: T) : Result<T>()
    data class Failure(val err: Throwable) : Result<Nothing>()
}

//private val list = arrayListOf<Pair<KClass<out Throwable>, (Throwable) -> Unit>>()
//inline fun <reified T : Throwable> whenThrow(noinline block: (T) -> Unit) {
//    whenThrowable(T::class, block)
//}
//
//fun <T : Throwable> whenThrowable(clz: KClass<T>, block: (T) -> Unit) =
//    list.add(clz to (block as (Throwable) -> Unit))
//
//fun onCatch(throwable: Throwable) {
//    list.filter { throwable::class == it.first }.forEach {
//        it.second(throwable)
//    }
//}
//
//fun aa() {
//    whenThrow<NetworkException> {
//        println("NetworkException")
//    }
//}

/**
 * 用于在 网络请求 的 flow 构建器中，捕获 网络请求 抛出的异常，
 * 处理 服务器返回 的错误信息，并用 emit 发出错误信息
 * @param apiFun 网络请求api
 * @param successData 成功接收响应后的回调（需要返回值）
 */
//inline fun <T, R> sendRequest(
//    emitLoading: Boolean = true,
//    crossinline getErrorBody: (String) -> Throwable = { Error(it.fromJson<String>()) },
//    crossinline successData: (responseBody: R) -> T = { it as T },
//    crossinline apiFun: suspend () -> Response<R>
//) = flowBlock(emitLoading) {
//    val response = apiFun()
//    if (response.isSuccessful) successData(response.body()!!)
//    else throw getErrorBody(response.errorBody()!!.string())
//}

/** 使用此方法，发射单个成功值，[block]需要一个返回值代表类型，然后返回值被发射 */
inline fun <T> flowBlock(
    emitLoading: Boolean = true,
    crossinline block: suspend () -> T,
) = flowMulti<T>(emitLoading) {
    emit(Result.Success(block()))
}

/** 使用此方法，可在[block]中发射多个[Result.Success]值 */
inline fun <T> flowMulti(
    emitLoading: Boolean = true,
    crossinline block: suspend FlowCollector<Result<T>>.() -> Unit,
) = flow<Result<T>> {
    if (emitLoading) emit(Result.Loading)
    block()
}.catch { emit(Result.Failure(it)); if (debug) it.printStackTrace() }.flowOn(Dispatchers.IO)

//inline fun <T> Flow<Result<T>>.onResult(
//    crossinline loading: () -> Unit = {},
//    crossinline failure: (Throwable) -> Unit = { it.stackTraceToString().log() },
//    crossinline success: suspend (T) -> Unit,
//) = onEach {
//    when (it) {
//        Result.Loading -> loading()
//        is Result.Failure -> failure(it.err)
//        is Result.Success -> success(it.data)
//    }
//}

fun Throwable.defaultCatch() {
    val s = this.stackTraceToString()
    if (debug) s.logSt()
    BaseAct.top.showDialog { setMessage(s) }
}

//inline fun <T> Result<T>.on(
//    crossinline loading: () -> Unit = {},
//    crossinline failure: (Throwable) -> Unit = { it.defaultCatch() },
//    crossinline success: (T) -> Unit,
//) = when (this) {
//    Result.Loading -> loading()
//    is Result.Failure -> failure(err)
//    is Result.Success -> success(data)
//}


// inline fun <T> Flow<Result<T>>.onLoading(
//    crossinline loading: () -> Unit,
// ) = onEach { if (it is Result.Loading) loading() }
//
// inline fun <T> Flow<Result<T>>.onFailure(
//    crossinline failure: (Throwable) -> Unit,
// ) = onEach { if (it is Result.Failure) failure(it.err) }
//
// inline fun <T> Flow<Result<T>>.onSuccess(
//    crossinline success: (T) -> Unit,
// ) = onEach { if (it is Result.Success) success(it.data) }
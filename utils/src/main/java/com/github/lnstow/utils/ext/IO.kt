package com.github.lnstow.utils.ext

import android.widget.ImageView
import androidx.annotation.Keep
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.InputStream
import java.io.Reader
import java.util.Collections

fun <T> MutableList<T>.move(from: Int, to: Int) = apply {
    if (from != to) add(to.coerceIn(0, size - 1), removeAt(from))
}

fun <T> MutableList<T>.move(item: T, to: Int) = apply {
    val i = indexOf(item)
    if (i != -1) move(i, to)
}

fun <T> MutableList<T>.swap(pos1: Int, pos2: Int) = apply {
    if (pos1 != pos2) Collections.swap(this, pos1, pos2)
}

/** 返回from到to子列表，不是复制一个新列表，包含from，排除to */
fun <T> List<T>.subList(from: Int = 0, to: Int = size) = subList(from, to)

val gson by lazy { Gson() }
fun Any?.toJson(): String = gson.toJson(this)
inline fun <reified T> String.fromJson(): T =
    gson.fromJson(this, object : TypeToken<T>() {}.type)

inline fun <reified T> File.fromJson(): T = bufferedReader().fromJson()
inline fun <reified T> InputStream.fromJson(): T = bufferedReader().fromJson()
inline fun <reified T> Reader.fromJson(): T =
    gson.fromJson(this, object : TypeToken<T>() {}.type)

val fileDir: File by lazy { myApp.filesDir }

fun File.createFile(): File {
    if (exists()) return this
    parentFile?.createDir()
    createNewFile()
    return this
}

fun File.createDir(): File {
    if (exists()) return this
    mkdirs()
    return this
}

infix fun File.writeJson(dataObj: Any) = createFile().writeText(dataObj.toJson())

fun ImageView.loadUrl(url: String?) {
    Glide.with(context.activity()!!)
        .load(url)
//        .load(if (url.isNullOrBlank()) "TEST_BOOK_COVER" else url)
//        .centerInside()
        .transition(DrawableTransitionOptions.withCrossFade(300))
        .into(this)
}

@Keep
interface IApiResp<T> {
    val code: Any
    val message: String?
    val data: T?
    fun isOk(): Boolean
    fun msgNonNull() = if (message.isNullOrBlank()) "no message" else message
}

class ApiError(val resp: IApiResp<*>) : Exception(resp.msgNonNull())

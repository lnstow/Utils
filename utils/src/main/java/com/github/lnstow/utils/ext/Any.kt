package com.github.lnstow.utils.ext

import android.app.Application
import android.text.Spanned
import android.util.Log
import android.view.ViewGroup
import android.widget.LinearLayout
import com.github.lnstow.utils.BuildConfig
import kotlinx.coroutines.Dispatchers
import java.util.Date

/** 使用[logSt]输出当前代码的调用堆栈 */
private val st get() = Throwable("print stack trace").stackTraceToString()
fun <T> T.log(prefix: String = "") = this.also { Log.d("ln", "log debug $prefix $it") }
fun <T> T.logSt(prefix: String = "") = this.also { Log.d("ln", "log debug $prefix $it\n$st") }

inline fun <reified T> Any?.as2(block: T.() -> Unit = { }): T = (this as T).apply(block)

lateinit var myApp: Application
var debug = BuildConfig.DEBUG
val Int.f get() = this.toFloat()
val Float.i get() = this.toInt()
val Long.i get() = this.toInt()

typealias VGLP = ViewGroup.LayoutParams
typealias LLLP = LinearLayout.LayoutParams
typealias MLP = ViewGroup.MarginLayoutParams

const val MATCH = VGLP.MATCH_PARENT
const val WRAP = VGLP.WRAP_CONTENT
const val EX_EX = Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
const val IN_IN = Spanned.SPAN_INCLUSIVE_INCLUSIVE
const val EX_IN = Spanned.SPAN_EXCLUSIVE_INCLUSIVE
const val IN_EX = Spanned.SPAN_INCLUSIVE_EXCLUSIVE
val DSP_IO = Dispatchers.IO
val DSP_MAIN = Dispatchers.Main

val Date.yearFriendly get() = year + 1900
val Date.monthFriendly get() = month + 1

object NumUnits {
    const val KB = 1024L
    const val MB = KB * 1024L
    const val GB = MB * 1024L
    const val TB = GB * 1024L
}
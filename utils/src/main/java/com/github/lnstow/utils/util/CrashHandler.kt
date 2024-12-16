package com.github.lnstow.utils.util

import android.os.Handler
import android.os.Looper
import com.github.lnstow.utils.ext.createFile
import com.github.lnstow.utils.ext.debug
import com.github.lnstow.utils.ext.defaultCatch
import com.github.lnstow.utils.ext.fileDir
import com.github.lnstow.utils.ext.logSt
import com.github.lnstow.utils.ext.showDialog
import com.github.lnstow.utils.ui.BaseAct
import java.io.File
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.nio.channels.UnresolvedAddressException
import kotlin.system.exitProcess

object CrashHandler : Thread.UncaughtExceptionHandler {

    override fun uncaughtException(t: Thread, e: Throwable) {
        if (isUIThread()) {
            val s = e.stackTraceToString()
            if (debug) s.logSt()
            crashFile.createFile()
            crashFile.writeText(s)
            exitProcess(1)
        } else {
            Handler(Looper.getMainLooper()).post { e.defaultCatch() }
        }
    }

    private fun isUIThread(): Boolean {
        return Looper.getMainLooper().thread === Thread.currentThread()
    }

    fun checkCrash() {
        if (!crashFile.canWrite()) return
        val text = crashFile.readText()
        BaseAct.top.showDialog { setMessage(text) }
        crashFile.delete()
    }

    private val crashFile by lazy { File(fileDir, "crash") }
}

fun Throwable.isNetworkError() =
    this is SocketTimeoutException || this is UnknownHostException || this is ConnectException
            || this is UnresolvedAddressException
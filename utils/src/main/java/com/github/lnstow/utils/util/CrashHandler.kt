package com.github.lnstow.utils.util

import android.os.Handler
import android.os.Looper
import com.github.lnstow.utils.ext.createFile
import com.github.lnstow.utils.ext.debug
import com.github.lnstow.utils.ext.fileDir
import com.github.lnstow.utils.ext.log
import com.github.lnstow.utils.ext.showDialog
import com.github.lnstow.utils.ui.BaseAct
import java.io.File
import kotlin.system.exitProcess

object CrashHandler : Thread.UncaughtExceptionHandler {

    override fun uncaughtException(t: Thread, e: Throwable) {
        val s = e.stackTraceToString()
        if (debug) s.log()
        crashFile.createFile()
        crashFile.writeText(s)

        if (isUIThread()) {
            exitProcess(1)
        } else {
            Handler(Looper.getMainLooper()).post { checkCrash() }
        }
    }

    private fun isUIThread(): Boolean {
        return Looper.getMainLooper().thread === Thread.currentThread()
    }

    fun checkCrash() {
        if (!crashFile.canWrite()) return
        val text = crashFile.readText()
        BaseAct.top.showDialog {
            setMessage(text)
        }
        crashFile.delete()
    }

    private val crashFile by lazy { File(fileDir, "crash") }
}
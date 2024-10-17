package com.github.lnstow.utils

import android.app.Application
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import com.github.lnstow.utils.ext.debug
import com.github.lnstow.utils.ext.myApp
import com.github.lnstow.utils.util.CrashHandler

object LnUtils {

    fun init(app: Application, isDebug: Boolean, resId: ResId) {
        myApp = app
        debug = isDebug
        if (isDebug) Thread.setDefaultUncaughtExceptionHandler(CrashHandler)
        this.resId = resId
    }

    lateinit var resId: ResId

    class ResId(
        @ColorRes val main: Int,
        @ColorRes val divider: Int,
        @ColorRes val ft: Int,
        @ColorRes val ftAcc: Int,
        @StringRes val copyTextOk: Int = TODO(),
    )
}
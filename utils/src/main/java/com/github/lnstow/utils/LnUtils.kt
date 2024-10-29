package com.github.lnstow.utils

import android.app.Application
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import com.github.lnstow.utils.ext.debug
import com.github.lnstow.utils.ext.myApp
import com.github.lnstow.utils.ui.BaseAct
import com.github.lnstow.utils.ui.NavigateManager
import com.github.lnstow.utils.util.CrashHandler

object LnUtils {
    lateinit var nav: NavigateManager
    lateinit var resId: ResId

    fun init(
        app: Application,
        isDebug: Boolean,
        resId: ResId,
        behavior: BaseAct.ActBehavior,
        nav: NavigateManager,
    ) {
        myApp = app
        debug = isDebug
        if (isDebug) Thread.setDefaultUncaughtExceptionHandler(CrashHandler)
        this.resId = resId
        BaseAct.actBehavior = behavior
        this.nav = nav
    }

    class ResId(
        @ColorRes val main: Int,
        @ColorRes val divider: Int,
        @ColorRes val ft: Int,
        @ColorRes val ftAcc: Int,
        @StringRes val copyTextOk: Int = TODO(),
    )
}
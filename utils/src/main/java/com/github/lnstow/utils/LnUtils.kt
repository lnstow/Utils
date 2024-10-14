package com.github.lnstow.utils

import android.app.Application
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import com.github.lnstow.utils.ext.myApp

object LnUtils {

    fun init(app: Application, resId: ResId) {
        myApp = app
        this.resId = resId
    }

    lateinit var resId: ResId

    class ResId(
        @ColorRes val main: Int,
        @ColorRes val divider: Int,
        @ColorRes val ft: Int,
        @ColorRes val ftAcc: Int,
        val container: Int,
        @StringRes val copyTextOk: Int = TODO(),
    )
}
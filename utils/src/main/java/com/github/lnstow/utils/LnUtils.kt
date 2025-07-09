package com.github.lnstow.utils

import android.app.Application
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.github.lnstow.utils.ext.Dp
import com.github.lnstow.utils.ext.debug
import com.github.lnstow.utils.ext.myApp
import com.github.lnstow.utils.ui.BaseAct
import com.github.lnstow.utils.ui.BaseBottomDialog
import com.github.lnstow.utils.ui.NavigateManager
import com.github.lnstow.utils.util.CrashHandler

object LnUtils {
    lateinit var resId: ResId
    lateinit var nav: NavigateManager
    lateinit var tbConfig: TbConfig

    fun init(
        app: Application,
        isDebug: Boolean,
        resId: ResId,
        behavior: BaseAct.ActBehavior,
        dialogBehavior: BaseBottomDialog.BottomDialogBehavior,
        nav: NavigateManager,
        tbConfig: TbConfig,
    ) {
        myApp = app
        debug = isDebug
        if (isDebug) Thread.setDefaultUncaughtExceptionHandler(CrashHandler)
        this.resId = resId
        BaseAct.actBehavior = behavior
        BaseBottomDialog.dialogBehavior = dialogBehavior
        this.nav = nav
        this.tbConfig = tbConfig
    }

    class ResId(
        @ColorRes val main: Int,
        @ColorRes val divider: Int,
        @ColorRes val ft: Int,
        @ColorRes val ftAcc: Int,
        @StringRes val copyTextOk: Int = TODO(),
    )

    class TbConfig(
        @ColorRes val text: Int,
        @ColorRes val title: Int,
        @DimenRes val textSize: Int,
        @DimenRes val titleSize: Int,
        @DrawableRes val backIcon: Int,
        @Dp val heightDp: Int,
    )
}
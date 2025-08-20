package com.github.lnstow.utils

import android.app.Application
import androidx.annotation.AnimRes
import androidx.annotation.AnimatorRes
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.annotation.StyleRes
import com.github.lnstow.utils.ext.Dp
import com.github.lnstow.utils.ext.debug
import com.github.lnstow.utils.ext.myApp
import com.github.lnstow.utils.ui.BaseAct
import com.github.lnstow.utils.ui.BaseBottomDialog
import com.github.lnstow.utils.ui.NavigateManager
import com.github.lnstow.utils.util.CrashHandler

object LnUtils {
    lateinit var resId: ResId private set
    lateinit var nav: NavigateManager private set
    lateinit var tbConfig: TbConfig private set
    lateinit var pageAnim: PageAnim private set

    fun init(
        app: Application,
        isDebug: Boolean,
        resId: ResId,
        behavior: BaseAct.ActBehavior,
        dialogBehavior: BaseBottomDialog.BottomDialogBehavior,
        nav: NavigateManager,
        tbConfig: TbConfig,
        pageAnim: PageAnim,
    ) {
        myApp = app
        debug = isDebug
        if (isDebug) Thread.setDefaultUncaughtExceptionHandler(CrashHandler)
        this.resId = resId
        BaseAct.actBehavior = behavior
        BaseBottomDialog.dialogBehavior = dialogBehavior
        this.nav = nav
        this.tbConfig = tbConfig
        this.pageAnim = pageAnim
    }

    class ResId(
        @ColorRes val main: Int,
        @ColorRes val divider: Int,
        @ColorRes val ft: Int,
        @ColorRes val ftAcc: Int,
        @StringRes val copyTextOk: Int = 0,
        @StyleRes val showDialogTheme: Int = 0,
    )

    class TbConfig(
        @ColorRes val text: Int,
        @ColorRes val title: Int,
        @DimenRes val textSize: Int,
        @DimenRes val titleSize: Int,
        @DrawableRes val backIcon: Int,
        @Dp val heightDp: Int,
    )

    class PageAnim(
        @AnimatorRes @AnimRes val topEnter: Int = R.anim.frag_slide_in_right,
        @AnimatorRes @AnimRes val bottomExit: Int = R.anim.frag_fade_out,
        @AnimatorRes @AnimRes val bottomEnter: Int = R.anim.frag_fade_in,
        @AnimatorRes @AnimRes val topExit: Int = R.anim.frag_slide_out_right,
    )
}
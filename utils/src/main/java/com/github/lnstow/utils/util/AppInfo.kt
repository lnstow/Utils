package com.github.lnstow.utils.util

import android.os.Build
import com.github.lnstow.utils.ui.BaseAct
import java.util.Locale

object AppInfo {
    private val ctx get() = BaseAct.top
    private val pkgInfo get() = ctx.packageManager.getPackageInfo(ctx.packageName, 0)
    val verCode: Int get() = pkgInfo.versionCode
    val locale: Locale get() = Locale.getDefault()

    private val dm get() = ctx.resources.displayMetrics
    private fun getUserAgent(): String =
        "Android/${Build.VERSION.RELEASE}_${Build.MODEL}" +
                " App/${verCode}_${pkgInfo.versionName}" +
                " Screen/${dm.widthPixels}x${dm.heightPixels}"

    @Deprecated("建议在 调用处直接 拼接map")
    fun setHeader(header: MutableMap<String, String>) {
        header["user-agent"] = getUserAgent()
        header["app-ver"] = verCode.toString()
        header["app-la"] = locale.toLanguageTag()
        // TODO: 2023/4/15 服务器接受语言、发送时间，拒绝低版本 ，检查request-header信息正确性
    }
}
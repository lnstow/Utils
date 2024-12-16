package com.github.lnstow.utils.util

import android.os.Build
import android.provider.Settings
import com.github.lnstow.utils.ui.BaseAct
import java.util.Locale

object AppInfo {
    private val ctx get() = BaseAct.top
    private val pkgInfo get() = ctx.packageManager.getPackageInfo(ctx.packageName, 0)
    private val dm get() = ctx.resources.displayMetrics

    val verCode: Int
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
            pkgInfo.longVersionCode.toInt()
        else pkgInfo.versionCode
    val locale: Locale get() = Locale.getDefault()
    val verName: String get() = pkgInfo.versionName!!
    val dw: Int get() = dm.widthPixels
    val dh: Int get() = dm.heightPixels

    /** 机型名称（e.g. iPhone 13） */
    val deviceName: String get() = Build.MODEL

    /** 系统版本（e.g. "ios 17.1") */
    val systemVer: String get() = Build.VERSION.RELEASE
    val androidId: String
        get() = Settings.Secure.getString(
            ctx.contentResolver, Settings.Secure.ANDROID_ID
        )

    private fun getUserAgent(): String =
        "Android/${systemVer}_${deviceName}" +
                " App/${verCode}_${verName}" +
                " Screen/${dw}x${dh}"

    @Deprecated("建议在 调用处直接 拼接map")
    fun setHeader(header: MutableMap<String, String>) {
        header["user-agent"] = getUserAgent()
        header["app-ver"] = verCode.toString()
        header["app-la"] = locale.toLanguageTag()
        // TODO: 2023/4/15 服务器接受语言、发送时间，拒绝低版本 ，检查request-header信息正确性
    }
}
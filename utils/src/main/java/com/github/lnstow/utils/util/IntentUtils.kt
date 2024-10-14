package com.github.lnstow.utils.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.net.toUri
import com.github.lnstow.utils.LnUtils
import com.github.lnstow.utils.ext.s
import com.github.lnstow.utils.ext.showToast
import com.github.lnstow.utils.ui.BaseAct

object IntentUtils {
    fun openLink(url: String, ctx: Context = BaseAct.top) =
        openLink(url.toUri(), ctx)

    fun openLink(url: Uri, ctx: Context = BaseAct.top) {
        val intent = Intent(Intent.ACTION_VIEW, url)
//        if (intent.isValid(ctx))
        runCatching {
            ctx.startActivity(intent)
        }.onFailure {
            ctx.showToast("app not found")
        }
    }
}

fun String.openLink(ctx: Context = BaseAct.top) = IntentUtils.openLink(this, ctx)
fun Intent?.isValid(ctx: Context? = null) =
    this?.resolveActivity(ctx?.packageManager ?: BaseAct.top.packageManager) != null

fun String.copyText(ctx: Context = BaseAct.top) {
    val clipboard = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("label", this))
    ctx.showToast(LnUtils.resId.copyTextOk.s)
}
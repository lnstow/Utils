package com.github.lnstow.utils.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Parcelable
import android.provider.MediaStore
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import com.github.lnstow.utils.LnUtils
import com.github.lnstow.utils.ext.activity
import com.github.lnstow.utils.ext.s
import com.github.lnstow.utils.ext.showToast
import com.github.lnstow.utils.ui.BaseAct
import java.io.File

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

    const val REQUEST_OPEN_CAMERA = 1231
    const val REQUEST_OPEN_ALBUM = 1232
    const val REQUEST_OPEN_CAMERA_OR_ALBUM = 1233
    fun createAlbumIntent(): Intent {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.setType("image/*")
        return intent
    }

    fun createCameraIntent(fileTarget: File, context: Context): Pair<Intent, Uri> {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        val uri = fileTarget.toContentUri(context)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
        return intent to uri
    }

    fun startIntentForResult(
        intent: Intent,
        requestCode: Int,
        fragment: Fragment? = null,
        context: Context? = null
    ) {
        when {
            fragment != null -> fragment.startActivityForResult(intent, requestCode)
            else -> context?.activity()?.startActivityForResult(intent, requestCode)
        }
    }

    fun openAlbum(frag: Fragment? = null, context: Context = BaseAct.top) {
        startIntentForResult(createAlbumIntent(), REQUEST_OPEN_ALBUM, frag, context)
    }

    //    https://medium.com/@appgeniuz08/capture-photo-via-intent-in-android-66749be91b9d
    //    https://developer.android.com/media/camera/camera-deprecated/photobasics?hl=en
    fun openCamera(fileTarget: File, frag: Fragment? = null, context: Context = BaseAct.top): Uri {
        val (intent, uri) = createCameraIntent(fileTarget, context)
        startIntentForResult(intent, REQUEST_OPEN_CAMERA, frag, context)
        return uri
    }

    //    https://juejin.cn/post/6844903544324096007
    fun openCameraOrAlbum(
        fileTarget: File,
        frag: Fragment? = null,
        context: Context = BaseAct.top
    ): Uri {
        val (take, uri) = createCameraIntent(fileTarget, context)
        take.addCategory(Intent.CATEGORY_DEFAULT)
        val chose = Intent.createChooser(createAlbumIntent(), "select photo")
        chose.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf<Parcelable>(take))
        startIntentForResult(chose, REQUEST_OPEN_CAMERA_OR_ALBUM, frag, context)
        return uri
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

fun Context.hasCamera(): Boolean {
    return packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
}

fun Fragment.hasCamera(): Boolean = requireContext().hasCamera()

private const val PROVIDER_NAME = "com.github.lnstow.utils.provider"
fun File.toContentUri(ctx: Context = BaseAct.top): Uri {
    return FileProvider.getUriForFile(ctx, PROVIDER_NAME, this)
}
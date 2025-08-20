package com.github.lnstow.utils.util

import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.github.lnstow.utils.ext.showToast

class RequestPermissionFlow(
    act: FragmentActivity,
    private val permission: String,
    var onRejected: () -> Unit = { act.showToast("此功能需要权限才能使用") },
) {
    private val launcher =
        act.registerForActivityResult(ActivityResultContracts.RequestPermission(), ::onResult)
//    val grantedFlow = permissionGranted.mapNotNull { it[permission] }
//        .shareIn(act.lifecycleScope, started = SharingStarted.Eagerly)

    private var onResolve: (() -> Unit)? = null
    fun checkPermission(act: FragmentActivity, onResolve: (() -> Unit)? = null) {
        this.onResolve = onResolve
        when {
            hasPermission(act) -> onResult(true)

            act.shouldShowRequestPermissionRationale(permission) -> {
                act.showToast("此功能需要权限才能使用")
//                onRejected.invoke()
                launcher.launch(permission)
            }

            else -> launcher.launch(permission)
        }
    }

    fun hasPermission(act: FragmentActivity) =
        ContextCompat.checkSelfPermission(act, permission) == PackageManager.PERMISSION_GRANTED

    private fun onResult(ok: Boolean) {
//        permissionGranted.tryEmit(mapOf(permission to ok))
        if (ok) onResolve?.invoke()
        else onRejected.invoke()
        onResolve = null
    }

    companion object {
//        private val permissionGranted by lazy {
//            MutableSharedFlow<Map<String, Boolean>>(
//                replay = 0,
//                extraBufferCapacity = 10,
//                BufferOverflow.DROP_OLDEST
//            )
//        }

        fun openAppSettings(act: FragmentActivity) {
            val intent = android.content.Intent().apply {
                action = android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                data = android.net.Uri.fromParts("package", act.packageName, null)
            }
            act.startActivity(intent)
        }
    }
}

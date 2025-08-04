package com.github.lnstow.utils.util

import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.github.lnstow.utils.ext.showToast
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.shareIn

class RequestPermissionFlow(
    act: FragmentActivity,
    private val permission: String,
) {
    private val launcher =
        act.registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            permissionGranted.tryEmit(mapOf(permission to it))
        }
    val grantedFlow = permissionGranted.mapNotNull { it[permission] }
        .shareIn(act.lifecycleScope, started = SharingStarted.Eagerly)

    fun checkPermission(act: FragmentActivity) {
        when {
            ContextCompat.checkSelfPermission(act, permission)
                    == PackageManager.PERMISSION_GRANTED -> {
                permissionGranted.tryEmit(mapOf(permission to true))
            }

            act.shouldShowRequestPermissionRationale(permission) -> {
                act.showToast("此功能需要录音权限才能使用")
                launcher.launch(permission)
            }

            else -> launcher.launch(permission)
        }
    }

    companion object {
        private val permissionGranted by lazy {
            MutableSharedFlow<Map<String, Boolean>>(
                replay = 0,
                extraBufferCapacity = 10,
                BufferOverflow.DROP_OLDEST
            )
        }

        fun openAppSettings(act: FragmentActivity) {
            val intent = android.content.Intent().apply {
                action = android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                data = android.net.Uri.fromParts("package", act.packageName, null)
            }
            act.startActivity(intent)
        }
    }
}

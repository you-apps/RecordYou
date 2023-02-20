package com.bnyro.recorder.util

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat

object PermissionHelper {
    fun checkPermissions(context: Context, permissions: Array<String>): Boolean {
        if (!hasPermission(context, permissions.first())) {
            ActivityCompat.requestPermissions(
                context as Activity,
                permissions,
                1
            )
            return false
        }
        return true
    }

    private fun hasPermission(context: Context, permission: String): Boolean {
        return ActivityCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }
}

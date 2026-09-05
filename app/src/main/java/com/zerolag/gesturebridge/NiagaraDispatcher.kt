package com.zerolag.gesturebridge

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log

object NiagaraDispatcher {
    private const val TAG = "NiagaraDispatcher"
    const val NIAGARA_PACKAGE_NAME = "bitpit.launcher"

    /**
     * Instantly brings Niagara Launcher to the foreground without any animation or transition delay.
     * This bypasses the OnePlus/ColorOS Quickstep InputDispatcher freeze.
     */
    fun launchNiagaraInstantly(context: Context, directToPackage: Boolean = true) {
        try {
            if (directToPackage && isPackageInstalled(context, NIAGARA_PACKAGE_NAME)) {
                val launchIntent = context.packageManager.getLaunchIntentForPackage(NIAGARA_PACKAGE_NAME)?.apply {
                    addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED or
                        Intent.FLAG_ACTIVITY_NO_ANIMATION or
                        Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    )
                }
                if (launchIntent != null) {
                    context.startActivity(launchIntent)
                    return
                }
            }

            // Fallback to standard Home category intent
            val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED or
                    Intent.FLAG_ACTIVITY_NO_ANIMATION
                )
            }
            context.startActivity(homeIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch home activity", e)
            // If direct intent fails, request AccessibilityService to trigger home
            GestureAccessibilityService.instance?.triggerHome()
        }
    }

    private fun isPackageInstalled(context: Context, packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }
}

package com.zerolag.gesturebridge

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.core.content.ContextCompat

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == "android.intent.action.QUICKBOOT_POWERON") {
            val prefs = context.getSharedPreferences(GestureOverlayService.PREFS_NAME, Context.MODE_PRIVATE)
            val autoStart = prefs.getBoolean("auto_start", true)

            if (autoStart && Settings.canDrawOverlays(context)) {
                val serviceIntent = Intent(context, GestureOverlayService::class.java).apply {
                    action = GestureOverlayService.ACTION_START
                }
                ContextCompat.startForegroundService(context, serviceIntent)
            }
        }
    }
}

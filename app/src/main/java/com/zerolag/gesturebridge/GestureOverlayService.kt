package com.zerolag.gesturebridge

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import androidx.core.app.NotificationCompat

class GestureOverlayService : Service() {

    companion object {
        const val ACTION_START = "com.zerolag.gesturebridge.ACTION_START"
        const val ACTION_STOP = "com.zerolag.gesturebridge.ACTION_STOP"
        const val ACTION_UPDATE_PREFS = "com.zerolag.gesturebridge.ACTION_UPDATE_PREFS"
        const val PREFS_NAME = "zerolag_prefs"
        const val KEY_BAR_HEIGHT = "bar_height"
        const val KEY_SHOW_PILL = "show_pill"
        const val KEY_DIRECT_NIAGARA = "direct_niagara"
        const val KEY_HAPTICS = "enable_haptics"

        var isRunning = false
            private set
    }

    private var windowManager: WindowManager? = null
    private var overlayView: GestureOverlayView? = null
    private lateinit var prefs: SharedPreferences

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_UPDATE_PREFS -> {
                updateOverlayAppearance()
                return START_STICKY
            }
        }

        startForeground(1001, buildNotification())
        setupOverlay()
        isRunning = true

        return START_STICKY
    }

    private fun setupOverlay() {
        if (overlayView != null) {
            updateOverlayAppearance()
            return
        }

        val directNiagara = prefs.getBoolean(KEY_DIRECT_NIAGARA, true)

        overlayView = GestureOverlayView(
            context = this,
            onSwipeHome = {
                if (directNiagara) {
                    NiagaraDispatcher.launchNiagaraInstantly(this, directToPackage = true)
                } else {
                    val acc = GestureAccessibilityService.instance
                    if (acc != null) {
                        acc.triggerHome()
                    } else {
                        NiagaraDispatcher.launchNiagaraInstantly(this, directToPackage = false)
                    }
                }
            },
            onSwipeRecents = {
                GestureAccessibilityService.instance?.triggerRecents()
            },
            onSwipeBack = {
                GestureAccessibilityService.instance?.triggerBack()
            }
        )

        updateOverlayAppearance()

        val heightDp = prefs.getInt(KEY_BAR_HEIGHT, 28)
        val heightPx = (heightDp * resources.displayMetrics.density).toInt()

        val layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            heightPx,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
        }

        try {
            windowManager?.addView(overlayView, layoutParams)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateOverlayAppearance() {
        overlayView?.let { view ->
            view.showVisualPill = prefs.getBoolean(KEY_SHOW_PILL, true)
            view.enableHaptics = prefs.getBoolean(KEY_HAPTICS, true)

            val heightDp = prefs.getInt(KEY_BAR_HEIGHT, 28)
            val heightPx = (heightDp * resources.displayMetrics.density).toInt()

            val params = view.layoutParams as? WindowManager.LayoutParams
            if (params != null && params.height != heightPx) {
                params.height = heightPx
                windowManager?.updateViewLayout(view, params)
            }
        }
    }

    private fun removeOverlay() {
        if (overlayView != null) {
            try {
                windowManager?.removeView(overlayView)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            overlayView = null
        }
    }

    override fun onDestroy() {
        removeOverlay()
        isRunning = false
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "zerolag_channel",
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_MIN
            ).apply {
                description = "ZeroLag Gesture Bar background service"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        val openIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, openIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, "zerolag_channel")
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_text))
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .build()
    }
}

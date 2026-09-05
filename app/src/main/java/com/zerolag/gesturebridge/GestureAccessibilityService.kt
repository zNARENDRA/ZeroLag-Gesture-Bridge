package com.zerolag.gesturebridge

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.provider.Settings
import android.text.TextUtils
import android.view.accessibility.AccessibilityEvent

class GestureAccessibilityService : AccessibilityService() {

    companion object {
        var instance: GestureAccessibilityService? = null
            private set

        fun isAccessibilitySettingsOn(context: Context): Boolean {
            var accessibilityEnabled = 0
            val service = context.packageName + "/" + GestureAccessibilityService::class.java.canonicalName
            try {
                accessibilityEnabled = Settings.Secure.getInt(
                    context.applicationContext.contentResolver,
                    Settings.Secure.ACCESSIBILITY_ENABLED
                )
            } catch (e: Settings.SettingNotFoundException) {
                return false
            }
            val mStringColonSplitter = TextUtils.SimpleStringSplitter(':')

            if (accessibilityEnabled == 1) {
                val settingValue = Settings.Secure.getString(
                    context.applicationContext.contentResolver,
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
                )
                if (settingValue != null) {
                    mStringColonSplitter.setString(settingValue)
                    while (mStringColonSplitter.hasNext()) {
                        val accessibilityService = mStringColonSplitter.next()
                        if (accessibilityService.equals(service, ignoreCase = true)) {
                            return true
                        }
                    }
                }
            }
            return false
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // No event inspection needed; used purely for global actions
    }

    override fun onInterrupt() {
        instance = null
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
    }

    fun triggerHome(): Boolean {
        return performGlobalAction(GLOBAL_ACTION_HOME)
    }

    fun triggerRecents(): Boolean {
        return performGlobalAction(GLOBAL_ACTION_RECENTS)
    }

    fun triggerBack(): Boolean {
        return performGlobalAction(GLOBAL_ACTION_BACK)
    }
}

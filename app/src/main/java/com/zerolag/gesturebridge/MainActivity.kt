package com.zerolag.gesturebridge

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.zerolag.gesturebridge.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = getSharedPreferences(GestureOverlayService.PREFS_NAME, Context.MODE_PRIVATE)

        setupListeners()
        loadPreferences()
    }

    override fun onResume() {
        super.onResume()
        updatePermissionStates()
        updateServiceState()
    }

    private fun setupListeners() {
        binding.btnGrantOverlay.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                startActivity(intent)
            }
        }

        binding.btnGrantAccessibility.setOnClickListener {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            startActivity(intent)
            Toast.makeText(this, "Enable 'ZeroLag Gestures' in Accessibility", Toast.LENGTH_LONG).show()
        }

        binding.btnToggleService.setOnClickListener {
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "Please grant Overlay permission first!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (GestureOverlayService.isRunning) {
                stopGestureService()
            } else {
                startGestureService()
            }
            updateServiceState()
        }

        binding.sbBarHeight.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val height = if (progress < 15) 15 else progress
                binding.tvHeightLabel.text = "Touch Trigger Zone Height: $height dp"
                prefs.edit().putInt(GestureOverlayService.KEY_BAR_HEIGHT, height).apply()
                notifyServiceUpdate()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        binding.switchShowPill.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean(GestureOverlayService.KEY_SHOW_PILL, isChecked).apply()
            notifyServiceUpdate()
        }

        binding.switchDirectNiagara.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean(GestureOverlayService.KEY_DIRECT_NIAGARA, isChecked).apply()
        }

        binding.switchHaptics.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean(GestureOverlayService.KEY_HAPTICS, isChecked).apply()
            notifyServiceUpdate()
        }
    }

    private fun loadPreferences() {
        val height = prefs.getInt(GestureOverlayService.KEY_BAR_HEIGHT, 28)
        binding.sbBarHeight.progress = height
        binding.tvHeightLabel.text = "Touch Trigger Zone Height: $height dp"

        binding.switchShowPill.isChecked = prefs.getBoolean(GestureOverlayService.KEY_SHOW_PILL, true)
        binding.switchDirectNiagara.isChecked = prefs.getBoolean(GestureOverlayService.KEY_DIRECT_NIAGARA, true)
        binding.switchHaptics.isChecked = prefs.getBoolean(GestureOverlayService.KEY_HAPTICS, true)
    }

    private fun updatePermissionStates() {
        val hasOverlay = Settings.canDrawOverlays(this)
        val hasAccessibility = GestureAccessibilityService.isAccessibilitySettingsOn(this)

        if (hasOverlay) {
            binding.btnGrantOverlay.text = "Granted"
            binding.btnGrantOverlay.isEnabled = false
        } else {
            binding.btnGrantOverlay.text = "Grant"
            binding.btnGrantOverlay.isEnabled = true
        }

        if (hasAccessibility) {
            binding.btnGrantAccessibility.text = "Enabled"
            binding.btnGrantAccessibility.isEnabled = false
        } else {
            binding.btnGrantAccessibility.text = "Enable"
            binding.btnGrantAccessibility.isEnabled = true
        }
    }

    private fun updateServiceState() {
        if (GestureOverlayService.isRunning) {
            binding.tvStatusBadge.text = "RUNNING"
            binding.tvStatusBadge.setTextColor(Color.parseColor("#00E676"))
            binding.btnToggleService.text = getString(R.string.action_stop_service)
        } else {
            binding.tvStatusBadge.text = "STOPPED"
            binding.tvStatusBadge.setTextColor(Color.parseColor("#FF5252"))
            binding.btnToggleService.text = getString(R.string.action_toggle_service)
        }
    }

    private fun startGestureService() {
        val intent = Intent(this, GestureOverlayService::class.java).apply {
            action = GestureOverlayService.ACTION_START
        }
        ContextCompat.startForegroundService(this, intent)
        prefs.edit().putBoolean("auto_start", true).apply()
        updateServiceState()
    }

    private fun stopGestureService() {
        val intent = Intent(this, GestureOverlayService::class.java).apply {
            action = GestureOverlayService.ACTION_STOP
        }
        startService(intent)
        prefs.edit().putBoolean("auto_start", false).apply()
        updateServiceState()
    }

    private fun notifyServiceUpdate() {
        if (GestureOverlayService.isRunning) {
            val intent = Intent(this, GestureOverlayService::class.java).apply {
                action = GestureOverlayService.ACTION_UPDATE_PREFS
            }
            startService(intent)
        }
    }
}

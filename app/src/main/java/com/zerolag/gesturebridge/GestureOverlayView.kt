package com.zerolag.gesturebridge

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs

@SuppressLint("ViewConstructor")
class GestureOverlayView(
    context: Context,
    private val onSwipeHome: () -> Unit,
    private val onSwipeRecents: () -> Unit,
    private val onSwipeBack: () -> Unit
) : View(context) {

    var showVisualPill: Boolean = true
        set(value) {
            field = value
            invalidate()
        }

    var enableHaptics: Boolean = true

    private val pillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0x80FFFFFF.toInt()
        style = Paint.Style.FILL
    }

    private val pillRect = RectF()

    private var startX = 0f
    private var startY = 0f
    private var isHolding = false
    private var gestureTriggered = false

    private val handler = Handler(Looper.getMainLooper())
    private val holdRunnable = Runnable {
        if (!gestureTriggered && isHolding) {
            gestureTriggered = true
            triggerHaptic(HapticFeedbackConstants.LONG_PRESS)
            onSwipeRecents()
        }
    }

    private val vibrator: Vibrator? by lazy {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (showVisualPill) {
            val pillWidth = width * 0.35f
            val pillHeight = 4f * resources.displayMetrics.density
            val left = (width - pillWidth) / 2f
            val top = (height - pillHeight) / 2f
            pillRect.set(left, top, left + pillWidth, top + pillHeight)
            val cornerRadius = pillHeight / 2f
            canvas.drawRoundRect(pillRect, cornerRadius, cornerRadius, pillPaint)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                startX = event.rawX
                startY = event.rawY
                isHolding = true
                gestureTriggered = false

                // Schedule long-press for Recent apps gesture
                handler.postDelayed(holdRunnable, 350)
                pillPaint.color = 0xCC3D7BFD.toInt()
                invalidate()
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                val deltaY = startY - event.rawY
                val deltaX = event.rawX - startX

                // If dragged significantly upward before timer fires, cancel hold runnable
                if (deltaY > 60 && !gestureTriggered) {
                    // Ready for swipe up
                }
                return true
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                handler.removeCallbacks(holdRunnable)
                isHolding = false
                pillPaint.color = 0x80FFFFFF.toInt()
                invalidate()

                if (gestureTriggered) {
                    return true
                }

                val deltaY = startY - event.rawY
                val deltaX = event.rawX - startX
                val swipeThreshold = 40 * resources.displayMetrics.density

                if (deltaY > swipeThreshold) {
                    // Upward swipe -> Instant Home to Niagara
                    triggerHaptic(HapticFeedbackConstants.KEYBOARD_TAP)
                    onSwipeHome()
                    return true
                } else if (abs(deltaX) > swipeThreshold * 1.5f && abs(deltaX) > abs(deltaY)) {
                    // Horizontal swipe -> Back
                    triggerHaptic(HapticFeedbackConstants.KEYBOARD_TAP)
                    onSwipeBack()
                    return true
                } else if (abs(deltaX) < 15 && abs(deltaY) < 15) {
                    // Short tap on the pill -> Quick Home
                    triggerHaptic(HapticFeedbackConstants.KEYBOARD_TAP)
                    onSwipeHome()
                    return true
                }
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun triggerHaptic(feedbackConstant: Int) {
        if (!enableHaptics) return
        performHapticFeedback(feedbackConstant)
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(20)
            }
        } catch (_: Exception) { }
    }
}

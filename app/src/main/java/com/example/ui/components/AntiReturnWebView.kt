package com.example.ui.components

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import android.webkit.WebView

/**
 * Hardened WebView that overrides low-level Android View visibility and focus callbacks.
 * Configured with isFocusable = true, isFocusableInTouchMode = true,
 * ensuring touch keyboard (IME) input works seamlessly when tapping inputs, while keeping
 * background protection active.
 */
class AntiReturnWebView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : WebView(context, attrs, defStyleAttr) {

    var isShieldActive: Boolean = true

    init {
        isFocusable = true
        isFocusableInTouchMode = true
    }

    override fun onCheckIsTextEditor(): Boolean {
        return true
    }

    override fun requestFocus(direction: Int, previouslyFocusedRect: Rect?): Boolean {
        return super.requestFocus(direction, previouslyFocusedRect)
    }

    override fun onWindowVisibilityChanged(visibility: Int) {
        if (isShieldActive) {
            // Always feed View.VISIBLE to Chromium's native engine
            super.onWindowVisibilityChanged(View.VISIBLE)
        } else {
            super.onWindowVisibilityChanged(visibility)
        }
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        if (isShieldActive) {
            super.onVisibilityChanged(changedView, View.VISIBLE)
        } else {
            super.onVisibilityChanged(changedView, visibility)
        }
    }

    override fun dispatchWindowVisibilityChanged(visibility: Int) {
        if (isShieldActive) {
            super.dispatchWindowVisibilityChanged(View.VISIBLE)
        } else {
            super.dispatchWindowVisibilityChanged(visibility)
        }
    }

    override fun onWindowFocusChanged(hasWindowFocus: Boolean) {
        if (isShieldActive) {
            // Always report window focus as true so window.blur is never triggered natively
            super.onWindowFocusChanged(true)
        } else {
            super.onWindowFocusChanged(hasWindowFocus)
        }
    }

    override fun hasWindowFocus(): Boolean {
        return if (isShieldActive) true else super.hasWindowFocus()
    }

    override fun isShown(): Boolean {
        return if (isShieldActive) true else super.isShown()
    }

    override fun onPause() {
        if (!isShieldActive) {
            super.onPause()
        }
        // When shield is active, do NOT pause JavaScript execution or background timers
    }

    override fun pauseTimers() {
        if (!isShieldActive) {
            super.pauseTimers()
        }
        // Keep timers running continuously
    }
}

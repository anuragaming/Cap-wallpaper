package com.example.ui.components

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.webkit.WebView

/**
 * Hardened WebView that overrides low-level Android View visibility and focus callbacks.
 * When isShieldActive is true, it prevents the underlying Chromium C++ engine from ever
 * learning that the window was minimized, hidden, or lost focus.
 * This ensures Chromium never fires native visibilitychange or blur events, and never pauses timers.
 */
class AntiReturnWebView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : WebView(context, attrs, defStyleAttr) {

    var isShieldActive: Boolean = true

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

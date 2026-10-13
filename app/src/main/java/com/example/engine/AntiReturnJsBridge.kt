package com.example.engine

import android.webkit.JavascriptInterface

class AntiReturnJsBridge(
    private val onBlocked: (type: String, domain: String, detail: String) -> Unit
) {
    @JavascriptInterface
    fun onDetectionBlocked(type: String, domain: String, detail: String) {
        onBlocked(type, domain, detail)
    }
}

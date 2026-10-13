package com.example.model

data class ShieldConfig(
    val isMasterShieldEnabled: Boolean = true,
    val blockVisibilityDetection: Boolean = true,
    val blockFocusBlurDetection: Boolean = true,
    val blockPageHideAndFreeze: Boolean = true,
    val blockHistoryReturnTrap: Boolean = true,
    val blockExitIntentAlerts: Boolean = true,
    val blockVisitorReturnFingerprint: Boolean = true,
    val keepBackgroundTimersActive: Boolean = true,
    val isDesktopMode: Boolean = false
)

data class ShieldStats(
    val totalBlocked: Int = 0,
    val visibilityBlocked: Int = 0,
    val blurBlocked: Int = 0,
    val historyTrapBlocked: Int = 0,
    val fingerprintBlocked: Int = 0,
    val pageHideBlocked: Int = 0
)

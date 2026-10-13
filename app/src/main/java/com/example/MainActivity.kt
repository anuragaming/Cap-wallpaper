package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.BrowserScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.ShieldDashboardScreen
import com.example.ui.theme.AntiReturnTheme
import com.example.ui.theme.CyberDarkNavy
import com.example.util.ScreenCaptureHelper
import com.example.viewmodel.AntiReturnViewModel
import com.example.viewmodel.AppScreen

class MainActivity : ComponentActivity() {

    companion object {
        const val EXTRA_OPEN_GEMINI = "extra_open_gemini"
        const val EXTRA_TRIGGER_LENS_CAPTURE = "extra_trigger_lens_capture"
    }

    private val viewModel: AntiReturnViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        handleIntent(intent)

        setContent {
            AntiReturnTheme {
                val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
                val shieldConfig by viewModel.shieldConfig.collectAsStateWithLifecycle()
                val shieldStats by viewModel.shieldStats.collectAsStateWithLifecycle()
                val logs by viewModel.logs.collectAsStateWithLifecycle()
                val currentUrl by viewModel.currentLoadedUrl.collectAsStateWithLifecycle()
                val urlInput by viewModel.urlInput.collectAsStateWithLifecycle()
                val pageTitle by viewModel.pageTitle.collectAsStateWithLifecycle()
                val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
                val canGoBack by viewModel.canGoBack.collectAsStateWithLifecycle()
                val canGoForward by viewModel.canGoForward.collectAsStateWithLifecycle()
                val blockedOnPage by viewModel.activePageBlockedCount.collectAsStateWithLifecycle()

                // Tabs state
                val tabs by viewModel.tabs.collectAsStateWithLifecycle()
                val activeTabId by viewModel.activeTabId.collectAsStateWithLifecycle()
                val isTabSwitcherVisible by viewModel.isTabSwitcherVisible.collectAsStateWithLifecycle()

                // DevTools state
                val isDevToolsOpen by viewModel.isDevToolsOpen.collectAsStateWithLifecycle()
                val consoleLogs by viewModel.consoleLogs.collectAsStateWithLifecycle()
                val networkLogs by viewModel.networkLogs.collectAsStateWithLifecycle()

                // Gemini state
                val isGeminiOverlayOpen by viewModel.isGeminiOverlayOpen.collectAsStateWithLifecycle()
                val geminiPrompt by viewModel.geminiPrompt.collectAsStateWithLifecycle()
                val geminiSolution by viewModel.geminiSolution.collectAsStateWithLifecycle()
                val isGeminiLoading by viewModel.isGeminiLoading.collectAsStateWithLifecycle()
                val geminiError by viewModel.geminiError.collectAsStateWithLifecycle()

                // Google Lens state
                val capturedScreenshot by viewModel.capturedScreenshot.collectAsStateWithLifecycle()
                val isGoogleLensOpen by viewModel.isGoogleLensOpen.collectAsStateWithLifecycle()
                val googleLensActiveTab by viewModel.googleLensActiveTab.collectAsStateWithLifecycle()
                val lensAiSolution by viewModel.lensAiSolution.collectAsStateWithLifecycle()
                val lensOcrText by viewModel.lensOcrText.collectAsStateWithLifecycle()
                val isLensLoading by viewModel.isLensLoading.collectAsStateWithLifecycle()
                val lensError by viewModel.lensError.collectAsStateWithLifecycle()
                val triggerLensCapture by viewModel.triggerLensCapture.collectAsStateWithLifecycle()

                // System Back button handling
                BackHandler(enabled = currentScreen != AppScreen.BROWSER) {
                    viewModel.navigateToScreen(AppScreen.BROWSER)
                }

                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.safeDrawing),
                    containerColor = CyberDarkNavy
                ) { innerPadding ->
                    Box(modifier = Modifier.fillMaxSize()) {
                        when (currentScreen) {
                            AppScreen.BROWSER -> BrowserScreen(
                                currentUrl = currentUrl,
                                urlInput = urlInput,
                                pageTitle = pageTitle,
                                isLoading = isLoading,
                                canGoBack = canGoBack,
                                canGoForward = canGoForward,
                                blockedOnThisPage = blockedOnPage,
                                config = shieldConfig,
                                tabs = tabs,
                                activeTabId = activeTabId,
                                isTabSwitcherVisible = isTabSwitcherVisible,
                                isDevToolsOpen = isDevToolsOpen,
                                consoleLogs = consoleLogs,
                                networkLogs = networkLogs,
                                isGeminiOverlayOpen = isGeminiOverlayOpen,
                                geminiPrompt = geminiPrompt,
                                geminiSolution = geminiSolution,
                                isGeminiLoading = isGeminiLoading,
                                geminiError = geminiError,
                                capturedScreenshot = capturedScreenshot,
                                isGoogleLensOpen = isGoogleLensOpen,
                                googleLensActiveTab = googleLensActiveTab,
                                lensAiSolution = lensAiSolution,
                                lensOcrText = lensOcrText,
                                isLensLoading = isLensLoading,
                                lensError = lensError,
                                triggerLensCapture = triggerLensCapture,
                                onUrlInputChanged = viewModel::updateUrlInput,
                                onNavigate = viewModel::navigateToUrl,
                                onPageStarted = viewModel::onPageLoadStarted,
                                onPageFinished = viewModel::onPageLoadFinished,
                                onBlockedEvent = viewModel::recordBlockedEvent,
                                onToggleMaster = viewModel::setMasterShield,
                                onToggleDesktopMode = viewModel::toggleDesktopMode,
                                onOpenDashboard = { viewModel.navigateToScreen(AppScreen.SHIELD_DASHBOARD) },
                                onOpenSettings = { viewModel.navigateToScreen(AppScreen.SETTINGS) },
                                onNewTab = { isIncognito -> viewModel.openNewTab("https://anhdev.io.vn", isIncognito) },
                                onSelectTab = viewModel::switchTab,
                                onCloseTab = viewModel::closeTab,
                                onCloseAllTabs = viewModel::closeAllTabs,
                                onSetTabSwitcherVisible = viewModel::setTabSwitcherVisible,
                                onSetDevToolsOpen = viewModel::setDevToolsOpen,
                                onClearDevToolsLogs = viewModel::clearDevToolsLogs,
                                onAddConsoleLog = viewModel::addConsoleLog,
                                onAddNetworkLog = viewModel::addNetworkRequest,
                                onSetGeminiOverlayOpen = { open ->
                                    if (open) viewModel.openGeminiOverlay() else viewModel.closeGeminiOverlay()
                                },
                                onGeminiPromptChanged = viewModel::setGeminiPrompt,
                                onExecuteGeminiSolve = { ctx, mode -> viewModel.executeGeminiSolve(ctx, mode) },
                                onScreenshotCaptured = viewModel::onScreenshotCaptured,
                                onCloseGoogleLens = viewModel::closeGoogleLens,
                                onGoogleLensTabSelected = viewModel::setGoogleLensTab,
                                onExecuteLensAction = { ctx, mode -> viewModel.executeLensAction(ctx, mode) }
                            )

                            AppScreen.SHIELD_DASHBOARD -> ShieldDashboardScreen(
                                config = shieldConfig,
                                stats = shieldStats,
                                logs = logs,
                                onToggleMaster = viewModel::setMasterShield,
                                onClearLogs = viewModel::clearLogs,
                                onBackToBrowser = { viewModel.navigateToScreen(AppScreen.BROWSER) }
                            )

                            AppScreen.SETTINGS -> SettingsScreen(
                                config = shieldConfig,
                                onToggleDesktopMode = viewModel::toggleDesktopMode,
                                onResetStats = viewModel::resetStats,
                                onClearLogs = viewModel::clearLogs,
                                onBackToBrowser = { viewModel.navigateToScreen(AppScreen.BROWSER) }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        intent?.data?.let { uri ->
            viewModel.navigateToUrl(uri.toString())
            viewModel.navigateToScreen(AppScreen.BROWSER)
        }

        if (intent?.getBooleanExtra(EXTRA_OPEN_GEMINI, false) == true) {
            viewModel.navigateToScreen(AppScreen.BROWSER)
            viewModel.openGeminiOverlay()
        }

        if (intent?.getBooleanExtra(EXTRA_TRIGGER_LENS_CAPTURE, false) == true) {
            viewModel.navigateToScreen(AppScreen.BROWSER)
            window.decorView.post {
                ScreenCaptureHelper.captureActivityScreenshot(this) { bmp ->
                    viewModel.onScreenshotCaptured(bmp, true)
                }
            }
        }
    }
}

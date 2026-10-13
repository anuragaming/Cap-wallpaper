package com.example.viewmodel

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.engine.GeminiSolverService
import com.example.model.BrowserTab
import com.example.model.ConsoleLogItem
import com.example.model.InterceptionLog
import com.example.model.NetworkRequestItem
import com.example.model.ShieldConfig
import com.example.model.ShieldStats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

enum class AppScreen {
    BROWSER,
    SHIELD_DASHBOARD,
    SETTINGS
}

class AntiReturnViewModel : ViewModel() {

    private val _shieldConfig = MutableStateFlow(ShieldConfig())
    val shieldConfig: StateFlow<ShieldConfig> = _shieldConfig.asStateFlow()

    private val _shieldStats = MutableStateFlow(ShieldStats())
    val shieldStats: StateFlow<ShieldStats> = _shieldStats.asStateFlow()

    private val _logs = MutableStateFlow<List<InterceptionLog>>(emptyList())
    val logs: StateFlow<List<InterceptionLog>> = _logs.asStateFlow()

    private val _currentScreen = MutableStateFlow(AppScreen.BROWSER)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    // -----------------------------------------------------------------
    // Multi-Tab Management (Chrome-style Normal & Incognito)
    // -----------------------------------------------------------------
    private val defaultTab = BrowserTab(
        id = "default-tab",
        url = "https://anhdev.io.vn",
        title = "Nhật Anh DEV",
        isIncognito = false
    )

    private val _tabs = MutableStateFlow<List<BrowserTab>>(listOf(defaultTab))
    val tabs: StateFlow<List<BrowserTab>> = _tabs.asStateFlow()

    private val _activeTabId = MutableStateFlow("default-tab")
    val activeTabId: StateFlow<String> = _activeTabId.asStateFlow()

    private val _isTabSwitcherVisible = MutableStateFlow(false)
    val isTabSwitcherVisible: StateFlow<Boolean> = _isTabSwitcherVisible.asStateFlow()

    private val _urlInput = MutableStateFlow("https://anhdev.io.vn")
    val urlInput: StateFlow<String> = _urlInput.asStateFlow()

    private val _currentLoadedUrl = MutableStateFlow("https://anhdev.io.vn")
    val currentLoadedUrl: StateFlow<String> = _currentLoadedUrl.asStateFlow()

    private val _pageTitle = MutableStateFlow("Nhật Anh DEV")
    val pageTitle: StateFlow<String> = _pageTitle.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _canGoBack = MutableStateFlow(false)
    val canGoBack: StateFlow<Boolean> = _canGoBack.asStateFlow()

    private val _canGoForward = MutableStateFlow(false)
    val canGoForward: StateFlow<Boolean> = _canGoForward.asStateFlow()

    private val _activePageBlockedCount = MutableStateFlow(0)
    val activePageBlockedCount: StateFlow<Int> = _activePageBlockedCount.asStateFlow()

    private val _whitelist = MutableStateFlow<Set<String>>(emptySet())
    val whitelist: StateFlow<Set<String>> = _whitelist.asStateFlow()

    // -----------------------------------------------------------------
    // DevTools State (Console & Network)
    // -----------------------------------------------------------------
    private val _isDevToolsOpen = MutableStateFlow(false)
    val isDevToolsOpen: StateFlow<Boolean> = _isDevToolsOpen.asStateFlow()

    private val _consoleLogs = MutableStateFlow<List<ConsoleLogItem>>(emptyList())
    val consoleLogs: StateFlow<List<ConsoleLogItem>> = _consoleLogs.asStateFlow()

    private val _networkLogs = MutableStateFlow<List<NetworkRequestItem>>(emptyList())
    val networkLogs: StateFlow<List<NetworkRequestItem>> = _networkLogs.asStateFlow()

    // -----------------------------------------------------------------
    // Gemini AI Assistant & Solver State
    // -----------------------------------------------------------------
    private val _isGeminiOverlayOpen = MutableStateFlow(false)
    val isGeminiOverlayOpen: StateFlow<Boolean> = _isGeminiOverlayOpen.asStateFlow()

    private val _geminiPrompt = MutableStateFlow("")
    val geminiPrompt: StateFlow<String> = _geminiPrompt.asStateFlow()

    private val _geminiSolution = MutableStateFlow("")
    val geminiSolution: StateFlow<String> = _geminiSolution.asStateFlow()

    private val _isGeminiLoading = MutableStateFlow(false)
    val isGeminiLoading: StateFlow<Boolean> = _isGeminiLoading.asStateFlow()

    private val _geminiError = MutableStateFlow<String?>(null)
    val geminiError: StateFlow<String?> = _geminiError.asStateFlow()

    private val _pageExtractedContext = MutableStateFlow<String?>(null)
    val pageExtractedContext: StateFlow<String?> = _pageExtractedContext.asStateFlow()

    // -----------------------------------------------------------------
    // Google Lens In-Browser State (Screenshot, OCR, Solver & Search)
    // Lưu trong bộ nhớ trình duyệt, KHÔNG lưu ra thư viện ảnh của máy
    // -----------------------------------------------------------------
    private val _capturedScreenshot = MutableStateFlow<Bitmap?>(null)
    val capturedScreenshot: StateFlow<Bitmap?> = _capturedScreenshot.asStateFlow()

    private val _isGoogleLensOpen = MutableStateFlow(false)
    val isGoogleLensOpen: StateFlow<Boolean> = _isGoogleLensOpen.asStateFlow()

    private val _googleLensActiveTab = MutableStateFlow("SOLVE") // "SOLVE", "OCR", "SEARCH"
    val googleLensActiveTab: StateFlow<String> = _googleLensActiveTab.asStateFlow()

    private val _lensCustomPrompt = MutableStateFlow("")
    val lensCustomPrompt: StateFlow<String> = _lensCustomPrompt.asStateFlow()

    private val _lensAiSolution = MutableStateFlow("")
    val lensAiSolution: StateFlow<String> = _lensAiSolution.asStateFlow()

    private val _lensOcrText = MutableStateFlow("")
    val lensOcrText: StateFlow<String> = _lensOcrText.asStateFlow()

    private val _isLensLoading = MutableStateFlow(false)
    val isLensLoading: StateFlow<Boolean> = _isLensLoading.asStateFlow()

    private val _lensError = MutableStateFlow<String?>(null)
    val lensError: StateFlow<String?> = _lensError.asStateFlow()

    private val _triggerLensCapture = MutableStateFlow(false)
    val triggerLensCapture: StateFlow<Boolean> = _triggerLensCapture.asStateFlow()

    fun navigateToScreen(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun setMasterShield(enabled: Boolean) {
        _shieldConfig.update { it.copy(isMasterShieldEnabled = enabled) }
    }

    fun toggleVisibilityDetection(enabled: Boolean) {
        _shieldConfig.update { it.copy(blockVisibilityDetection = enabled) }
    }

    fun toggleFocusBlurDetection(enabled: Boolean) {
        _shieldConfig.update { it.copy(blockFocusBlurDetection = enabled) }
    }

    fun togglePageHideAndFreeze(enabled: Boolean) {
        _shieldConfig.update { it.copy(blockPageHideAndFreeze = enabled) }
    }

    fun toggleHistoryReturnTrap(enabled: Boolean) {
        _shieldConfig.update { it.copy(blockHistoryReturnTrap = enabled) }
    }

    fun toggleExitIntentAlerts(enabled: Boolean) {
        _shieldConfig.update { it.copy(blockExitIntentAlerts = enabled) }
    }

    fun toggleVisitorReturnFingerprint(enabled: Boolean) {
        _shieldConfig.update { it.copy(blockVisitorReturnFingerprint = enabled) }
    }

    fun toggleKeepBackgroundTimers(enabled: Boolean) {
        _shieldConfig.update { it.copy(keepBackgroundTimersActive = enabled) }
    }

    fun toggleDesktopMode() {
        _shieldConfig.update { it.copy(isDesktopMode = !it.isDesktopMode) }
    }

    fun updateUrlInput(url: String) {
        _urlInput.value = url
    }

    fun navigateToUrl(rawUrl: String) {
        val target = when {
            rawUrl.isBlank() -> "https://anhdev.io.vn"
            rawUrl.startsWith("http://") || rawUrl.startsWith("https://") -> rawUrl
            rawUrl.contains(".") && !rawUrl.contains(" ") -> "https://$rawUrl"
            else -> "https://www.google.com/search?q=${rawUrl.trim()}"
        }
        _currentLoadedUrl.value = target
        _urlInput.value = target
        _activePageBlockedCount.value = 0

        // Update active tab URL
        val activeId = _activeTabId.value
        _tabs.update { currentTabs ->
            currentTabs.map { tab ->
                if (tab.id == activeId) tab.copy(url = target, lastAccessed = System.currentTimeMillis())
                else tab
            }
        }
    }

    // -----------------------------------------------------------------
    // Tab operations
    // -----------------------------------------------------------------
    fun openNewTab(url: String = "https://anhdev.io.vn", isIncognito: Boolean = false) {
        val newTab = BrowserTab(
            url = url,
            title = if (isIncognito) "Tab ẩn danh" else "Tab mới",
            isIncognito = isIncognito
        )
        _tabs.update { it + newTab }
        _activeTabId.value = newTab.id
        _currentLoadedUrl.value = url
        _urlInput.value = url
        _pageTitle.value = newTab.title
        _activePageBlockedCount.value = 0
        _isTabSwitcherVisible.value = false
        _currentScreen.value = AppScreen.BROWSER
    }

    fun switchTab(tabId: String) {
        val tab = _tabs.value.find { it.id == tabId } ?: return
        _activeTabId.value = tab.id
        _currentLoadedUrl.value = tab.url
        _urlInput.value = tab.url
        _pageTitle.value = tab.title
        _activePageBlockedCount.value = 0
        _isTabSwitcherVisible.value = false
        _currentScreen.value = AppScreen.BROWSER
    }

    fun closeTab(tabId: String) {
        val currentList = _tabs.value
        if (currentList.size <= 1) {
            // If last tab closed, open fresh home tab
            val freshTab = BrowserTab(url = "https://anhdev.io.vn", title = "Nhật Anh DEV", isIncognito = false)
            _tabs.value = listOf(freshTab)
            _activeTabId.value = freshTab.id
            _currentLoadedUrl.value = freshTab.url
            _urlInput.value = freshTab.url
            _pageTitle.value = freshTab.title
            return
        }

        val filtered = currentList.filter { it.id != tabId }
        _tabs.value = filtered

        if (_activeTabId.value == tabId) {
            val nextTab = filtered.last()
            _activeTabId.value = nextTab.id
            _currentLoadedUrl.value = nextTab.url
            _urlInput.value = nextTab.url
            _pageTitle.value = nextTab.title
        }
    }

    fun closeAllTabs(isIncognitoOnly: Boolean? = null) {
        val currentList = _tabs.value
        val remaining = when (isIncognitoOnly) {
            true -> currentList.filterNot { it.isIncognito }
            false -> currentList.filter { it.isIncognito }
            null -> emptyList()
        }

        if (remaining.isEmpty()) {
            val fresh = BrowserTab(url = "https://anhdev.io.vn", title = "Nhật Anh DEV", isIncognito = false)
            _tabs.value = listOf(fresh)
            _activeTabId.value = fresh.id
            _currentLoadedUrl.value = fresh.url
            _urlInput.value = fresh.url
            _pageTitle.value = fresh.title
        } else {
            _tabs.value = remaining
            val nextTab = remaining.last()
            _activeTabId.value = nextTab.id
            _currentLoadedUrl.value = nextTab.url
            _urlInput.value = nextTab.url
            _pageTitle.value = nextTab.title
        }
    }

    fun setTabSwitcherVisible(visible: Boolean) {
        _isTabSwitcherVisible.value = visible
    }

    // -----------------------------------------------------------------
    // Page lifecycle
    // -----------------------------------------------------------------
    fun onPageLoadStarted(url: String) {
        _isLoading.value = true
        _currentLoadedUrl.value = url
        _urlInput.value = url
        _activePageBlockedCount.value = 0
    }

    fun onPageLoadFinished(url: String, title: String?, canBack: Boolean, canForward: Boolean) {
        _isLoading.value = false
        _currentLoadedUrl.value = url
        _urlInput.value = url
        val cleanTitle = title?.takeIf { it.isNotBlank() } ?: url
        _pageTitle.value = cleanTitle
        _canGoBack.value = canBack
        _canGoForward.value = canForward

        // Update active tab title & url
        val activeId = _activeTabId.value
        _tabs.update { currentTabs ->
            currentTabs.map { tab ->
                if (tab.id == activeId) tab.copy(url = url, title = cleanTitle)
                else tab
            }
        }
    }

    // -----------------------------------------------------------------
    // Anti-Return Shield event logging
    // -----------------------------------------------------------------
    fun recordBlockedEvent(type: String, domain: String, detail: String) {
        if (!_shieldConfig.value.isMasterShieldEnabled) return

        viewModelScope.launch {
            if (type == "SHIELD_INIT") return@launch

            val activeTab = _tabs.value.find { it.id == _activeTabId.value }
            // If active tab is incognito, do not record persistent history logs
            val isIncognito = activeTab?.isIncognito ?: false

            val readableAction = when (type) {
                "VISIBILITY_CHANGE" -> "Khóa visibilityState = 'visible' (Ngăn phát hiện chuyển tab)"
                "WINDOW_BLUR" -> "Khóa document.hasFocus() = true (Giữ tiêu điểm liên tục)"
                "PAGE_HIDE" -> "Chặn sự kiện pagehide / freeze khi ẩn ứng dụng"
                "HISTORY_TRAP" -> "Chặn spam history.pushState (Bảo vệ nút Back / Return)"
                "EXIT_INTERCEPT" -> "Chặn bẫy giữ chân window.onbeforeunload"
                "FINGERPRINT_PROBE" -> "Làm nhiễu dấu vết Canvas để ẩn danh lượt quay lại"
                else -> detail
            }

            if (!isIncognito) {
                val newLog = InterceptionLog(
                    id = UUID.randomUUID().toString(),
                    eventType = type,
                    domain = domain.ifBlank { "local" },
                    actionTaken = readableAction,
                    details = detail
                )
                _logs.update { listOf(newLog) + it.take(99) }
            }

            _activePageBlockedCount.update { it + 1 }

            _shieldStats.update { current ->
                current.copy(
                    totalBlocked = current.totalBlocked + 1,
                    visibilityBlocked = if (type == "VISIBILITY_CHANGE") current.visibilityBlocked + 1 else current.visibilityBlocked,
                    blurBlocked = if (type == "WINDOW_BLUR") current.blurBlocked + 1 else current.blurBlocked,
                    historyTrapBlocked = if (type == "HISTORY_TRAP" || type == "EXIT_INTERCEPT") current.historyTrapBlocked + 1 else current.historyTrapBlocked,
                    fingerprintBlocked = if (type == "FINGERPRINT_PROBE") current.fingerprintBlocked + 1 else current.fingerprintBlocked,
                    pageHideBlocked = if (type == "PAGE_HIDE") current.pageHideBlocked + 1 else current.pageHideBlocked
                )
            }
        }
    }

    fun clearLogs() {
        _logs.value = emptyList()
    }

    fun resetStats() {
        _shieldStats.value = ShieldStats()
        _activePageBlockedCount.value = 0
    }

    fun toggleWhitelist(domain: String) {
        _whitelist.update { set ->
            if (set.contains(domain)) set - domain else set + domain
        }
    }

    // -----------------------------------------------------------------
    // DevTools methods
    // -----------------------------------------------------------------
    fun setDevToolsOpen(open: Boolean) {
        _isDevToolsOpen.value = open
    }

    fun addConsoleLog(level: String, message: String, source: String = "") {
        // Only log if DevTools is active to conserve UI performance
        if (_isDevToolsOpen.value) {
            val item = ConsoleLogItem(level = level, message = message, source = source)
            _consoleLogs.update { (listOf(item) + it).take(100) }
        }
    }

    fun addNetworkRequest(url: String, method: String = "GET", type: String = "RESOURCE") {
        // Critical performance optimization: only collect resource logs if DevTools is actively open
        // This prevents hundreds of recompositions per second during web loading
        if (_isDevToolsOpen.value) {
            val item = NetworkRequestItem(url = url, method = method, type = type)
            _networkLogs.update { (listOf(item) + it).take(100) }
        }
    }

    fun clearDevToolsLogs() {
        _consoleLogs.value = emptyList()
        _networkLogs.value = emptyList()
    }

    // -----------------------------------------------------------------
    // Gemini Solver & Assistant methods
    // -----------------------------------------------------------------
    fun openGeminiOverlay(initialPrompt: String = "", pageContext: String? = null) {
        if (initialPrompt.isNotBlank()) {
            _geminiPrompt.value = initialPrompt
        }
        if (pageContext != null) {
            _pageExtractedContext.value = pageContext
        }
        _geminiError.value = null
        _isGeminiOverlayOpen.value = true
    }

    fun closeGeminiOverlay() {
        _isGeminiOverlayOpen.value = false
    }

    fun setGeminiPrompt(text: String) {
        _geminiPrompt.value = text
    }

    fun setPageExtractedContext(contextText: String?) {
        _pageExtractedContext.value = contextText
    }

    fun executeGeminiSolve(context: Context, mode: String = "SOLVE") {
        val prompt = _geminiPrompt.value.trim()
        if (prompt.isBlank()) {
            _geminiError.value = "Vui lòng nhập câu hỏi hoặc trích xuất đề bài từ trang web."
            return
        }

        _isGeminiLoading.value = true
        _geminiError.value = null
        _geminiSolution.value = ""

        viewModelScope.launch {
            val result = GeminiSolverService.solveExerciseOrSearch(
                context = context,
                prompt = prompt,
                pageContext = _pageExtractedContext.value,
                mode = mode
            )
            _isGeminiLoading.value = false
            result.onSuccess { solution ->
                _geminiSolution.value = solution
            }.onFailure { err ->
                _geminiError.value = err.message ?: "Đã xảy ra lỗi khi gọi Gemini."
            }
        }
    }

    // -----------------------------------------------------------------
    // Google Lens In-Browser Methods (Capture, OCR, Solver & Search)
    // -----------------------------------------------------------------
    fun requestLensCapture() {
        _triggerLensCapture.value = true
    }

    fun onScreenshotCaptured(bitmap: Bitmap?, openLens: Boolean = true) {
        _triggerLensCapture.value = false
        _capturedScreenshot.value = bitmap
        if (openLens && bitmap != null) {
            _isGoogleLensOpen.value = true
            _lensError.value = null
        }
    }

    fun openGoogleLens() {
        _lensError.value = null
        _isGoogleLensOpen.value = true
    }

    fun closeGoogleLens() {
        _isGoogleLensOpen.value = false
    }

    fun setGoogleLensTab(tab: String) {
        _googleLensActiveTab.value = tab
    }

    fun setLensCustomPrompt(prompt: String) {
        _lensCustomPrompt.value = prompt
    }

    fun clearLensError() {
        _lensError.value = null
    }

    fun executeLensAction(context: Context, mode: String = _googleLensActiveTab.value) {
        val screenshot = _capturedScreenshot.value
        if (screenshot == null) {
            _lensError.value = "Chưa có ảnh chụp màn hình trình duyệt. Vui lòng bấm 'Chụp màn hình' để bắt đầu."
            return
        }

        _isLensLoading.value = true
        _lensError.value = null

        viewModelScope.launch {
            val result = GeminiSolverService.analyzeImageWithGemini(
                context = context,
                bitmap = screenshot,
                prompt = _lensCustomPrompt.value,
                mode = mode
            )
            _isLensLoading.value = false
            result.onSuccess { text ->
                when (mode) {
                    "OCR" -> _lensOcrText.value = text
                    else -> _lensAiSolution.value = text
                }
            }.onFailure { err ->
                _lensError.value = err.message ?: "Lỗi khi xử lý hình ảnh với Google Lens."
            }
        }
    }
}

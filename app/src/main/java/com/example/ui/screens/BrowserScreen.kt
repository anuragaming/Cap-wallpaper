package com.example.ui.screens

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.view.View
import com.example.util.ScreenCaptureHelper
import android.view.ViewGroup
import android.webkit.ConsoleMessage
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebChromeClient
import android.os.Message
import android.view.MotionEvent
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import com.example.data.local.HistoryEntry
import com.example.ui.components.HistorySheet
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.R
import com.example.engine.AntiReturnJsBridge
import com.example.engine.AntiReturnScriptEngine
import com.example.model.BrowserTab
import com.example.model.ConsoleLogItem
import com.example.model.NetworkRequestItem
import com.example.model.ShieldConfig
import com.example.ui.components.AntiReturnWebView
import com.example.ui.components.ChromeTabSwitcher
import com.example.ui.components.DevToolsSheet
import com.example.ui.components.GeminiAssistantDialog
import com.example.ui.components.GoogleLensSheet
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberDarkNavy
import com.example.ui.theme.CyberGreen
import com.example.ui.theme.CyberRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.net.URI

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun BrowserScreen(
    currentUrl: String,
    urlInput: String,
    pageTitle: String,
    isLoading: Boolean,
    canGoBack: Boolean,
    canGoForward: Boolean,
    blockedOnThisPage: Int,
    config: ShieldConfig,
    tabs: List<BrowserTab>,
    activeTabId: String,
    isTabSwitcherVisible: Boolean,
    isDevToolsOpen: Boolean,
    consoleLogs: List<ConsoleLogItem>,
    networkLogs: List<NetworkRequestItem>,
    isGeminiOverlayOpen: Boolean,
    geminiPrompt: String,
    geminiSolution: String,
    isGeminiLoading: Boolean,
    geminiError: String?,
    capturedScreenshot: Bitmap? = null,
    isGoogleLensOpen: Boolean = false,
    googleLensActiveTab: String = "SOLVE",
    lensAiSolution: String = "",
    lensOcrText: String = "",
    isLensLoading: Boolean = false,
    lensError: String? = null,
    triggerLensCapture: Boolean = false,
    onUrlInputChanged: (String) -> Unit,
    onNavigate: (String) -> Unit,
    onPageStarted: (String) -> Unit,
    onPageFinished: (String, String?, Boolean, Boolean) -> Unit,
    onBlockedEvent: (type: String, domain: String, detail: String) -> Unit,
    onToggleMaster: (Boolean) -> Unit,
    onToggleDesktopMode: () -> Unit,
    onOpenDashboard: () -> Unit,
    onOpenSettings: () -> Unit,
    onNewTab: (isIncognito: Boolean) -> Unit,
    onSelectTab: (String) -> Unit,
    onCloseTab: (String) -> Unit,
    onCloseAllTabs: (Boolean?) -> Unit,
    onSetTabSwitcherVisible: (Boolean) -> Unit,
    onSetDevToolsOpen: (Boolean) -> Unit,
    onClearDevToolsLogs: () -> Unit,
    onAddConsoleLog: (String, String, String) -> Unit,
    onAddNetworkLog: (String, String, String) -> Unit,
    onSetGeminiOverlayOpen: (Boolean) -> Unit,
    onGeminiPromptChanged: (String) -> Unit,
    onExecuteGeminiSolve: (Context, String) -> Unit,
    onScreenshotCaptured: (Bitmap?, Boolean) -> Unit = { _, _ -> },
    onCloseGoogleLens: () -> Unit = {},
    onGoogleLensTabSelected: (String) -> Unit = {},
    onExecuteLensAction: (Context, String) -> Unit = { _, _ -> },
    isHistoryOpen: Boolean = false,
    historyList: List<HistoryEntry> = emptyList(),
    onOpenHistory: () -> Unit = {},
    onCloseHistory: () -> Unit = {},
    onDeleteHistoryEntry: (HistoryEntry) -> Unit = {},
    onClearAllHistory: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var showShieldSheet by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var isEditingUrl by remember { mutableStateOf(false) }

    val activity = context as? Activity
    val captureNormalScreen: () -> Unit = {
        if (activity != null) {
            ScreenCaptureHelper.captureActivityScreenshot(activity) { bmp ->
                val finalBmp = bmp ?: captureWebView(webViewRef)
                onScreenshotCaptured(finalBmp, true)
            }
        } else {
            val bmp = captureWebView(webViewRef)
            onScreenshotCaptured(bmp, true)
        }
    }

    // Auto-capture standard screenshot when triggered (e.g. from Floating Shield button)
    LaunchedEffect(triggerLensCapture) {
        if (triggerLensCapture) {
            captureNormalScreen()
        }
    }

    val sheetState = rememberModalBottomSheetState()
    val activeTab = remember(tabs, activeTabId) {
        tabs.find { it.id == activeTabId } ?: tabs.firstOrNull()
    }
    val isIncognito = activeTab?.isIncognito ?: false

    // System Back Handler
    BackHandler(enabled = isTabSwitcherVisible || isDevToolsOpen || isGeminiOverlayOpen || canGoBack) {
        when {
            isGeminiOverlayOpen -> onSetGeminiOverlayOpen(false)
            isDevToolsOpen -> onSetDevToolsOpen(false)
            isTabSwitcherVisible -> onSetTabSwitcherVisible(false)
            canGoBack -> {
                webViewRef?.let { wv ->
                    if (wv.canGoBack()) wv.goBack()
                }
            }
        }
    }

    // Hostname for display
    val displayHost = remember(currentUrl) {
        try {
            val uri = URI(currentUrl)
            uri.host?.takeIf { it.isNotBlank() } ?: currentUrl
        } catch (e: Exception) {
            currentUrl
        }
    }

    val topBarBg = if (isIncognito) Color(0xFF141324) else Color(0xFF111827)
    val pillBg = if (isIncognito) Color(0xFF1F1B2E) else Color(0xFF1F2937)

    // Render Tab Switcher Grid if user opened it
    if (isTabSwitcherVisible) {
        ChromeTabSwitcher(
            tabs = tabs,
            activeTabId = activeTabId,
            onSelectTab = onSelectTab,
            onCloseTab = onCloseTab,
            onNewTab = onNewTab,
            onCloseAllTabs = onCloseAllTabs,
            onDismiss = { onSetTabSwitcherVisible(false) }
        )
    } else {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(if (isIncognito) Color(0xFF0F0E17) else Color(0xFF1F2937))
        ) {
            // ==========================================
            // 1. CHROME OMNIBOX TOP BAR
            // ==========================================
            Surface(
                color = topBarBg,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Home Button (Go to default page https://anhdev.io.vn)
                    IconButton(
                        onClick = { onNavigate("https://anhdev.io.vn") },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("chrome_home_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Trang chủ",
                            tint = if (isIncognito) Color(0xFFC084FC) else TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(2.dp))

                    // Chrome Address Bar (Pill Shape)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .clip(RoundedCornerShape(21.dp))
                            .background(pillBg)
                            .border(
                                1.dp,
                                if (isEditingUrl) (if (isIncognito) Color(0xFFC084FC) else CyberCyan) else Color(0xFF374151),
                                RoundedCornerShape(21.dp)
                            )
                            .padding(horizontal = 10.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Lock / Security / Incognito Icon
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable { showShieldSheet = true }
                                    .padding(3.dp)
                                    .testTag("chrome_lock_icon"),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isIncognito) {
                                    Icon(
                                        imageVector = Icons.Default.VisibilityOff,
                                        contentDescription = "Ẩn danh",
                                        tint = Color(0xFFC084FC),
                                        modifier = Modifier.size(16.dp)
                                    )
                                } else {
                                    Icon(
                                        imageVector = if (config.isMasterShieldEnabled) Icons.Default.Lock else Icons.Default.Shield,
                                        contentDescription = "Bảo mật",
                                        tint = if (config.isMasterShieldEnabled) CyberGreen else CyberRed,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            // URL Input / Display
                            BasicTextField(
                                value = if (isEditingUrl) urlInput else displayHost,
                                onValueChange = {
                                    isEditingUrl = true
                                    onUrlInputChanged(it)
                                },
                                singleLine = true,
                                textStyle = TextStyle(
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = if (isEditingUrl) FontWeight.Normal else FontWeight.Medium
                                ),
                                cursorBrush = SolidColor(if (isIncognito) Color(0xFFC084FC) else CyberCyan),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                                keyboardActions = KeyboardActions(
                                    onGo = {
                                        isEditingUrl = false
                                        onNavigate(urlInput)
                                    }
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .onFocusChanged { focusState ->
                                        if (focusState.isFocused) {
                                            isEditingUrl = true
                                            onUrlInputChanged(currentUrl)
                                        }
                                    }
                                    .testTag("chrome_url_input")
                            )

                            // Clear button if editing, Reload button if viewing
                            if (isEditingUrl && urlInput.isNotEmpty()) {
                                IconButton(
                                    onClick = { onUrlInputChanged("") },
                                    modifier = Modifier.size(26.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Xóa",
                                        tint = TextSecondary,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            } else {
                                IconButton(
                                    onClick = { webViewRef?.reload() },
                                    modifier = Modifier.size(26.dp).testTag("chrome_reload_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Tải lại",
                                        tint = TextSecondary,
                                        modifier = Modifier.size(17.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // 🔍 Google Lens Button (One-click capture & OCR / Solve / Search)
                    IconButton(
                        onClick = { captureNormalScreen() },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("chrome_google_lens_button")
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_google_lens),
                            contentDescription = "Google Lens & OCR",
                            tint = Color.Unspecified,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(2.dp))

                    // ✨ Gemini AI Quick Solver Button (Solves exercises directly on the page!)
                    IconButton(
                        onClick = { onSetGeminiOverlayOpen(true) },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("chrome_gemini_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Gemini giải bài tập",
                            tint = CyberCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(2.dp))

                    // Chrome Tab Count Box Button
                    val tabCount = if (isIncognito) tabs.count { it.isIncognito } else tabs.count { !it.isIncognito }
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .border(
                                1.5.dp,
                                if (isIncognito) Color(0xFFC084FC) else TextSecondary,
                                RoundedCornerShape(6.dp)
                            )
                            .clickable { onSetTabSwitcherVisible(true) }
                            .testTag("chrome_tab_switcher_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$tabCount",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isIncognito) Color(0xFFC084FC) else TextPrimary,
                            fontFamily = FontFamily.SansSerif
                        )
                    }

                    Spacer(modifier = Modifier.width(2.dp))

                    // Chrome 3-Dots Menu Button
                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("chrome_menu_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Menu tùy chọn",
                                tint = TextPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Chrome Dropdown Menu
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            modifier = Modifier
                                .background(Color(0xFF1F2937))
                                .border(1.dp, Color(0xFF374151), RoundedCornerShape(8.dp))
                        ) {
                            DropdownMenuItem(
                                text = { Text("Tab mới", color = TextPrimary) },
                                leadingIcon = { Icon(Icons.Default.Add, null, tint = TextSecondary) },
                                onClick = {
                                    showMenu = false
                                    onNewTab(false)
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("Tab ẩn danh mới 🕶️", color = Color(0xFFC084FC)) },
                                leadingIcon = { Icon(Icons.Default.VisibilityOff, null, tint = Color(0xFFC084FC)) },
                                onClick = {
                                    showMenu = false
                                    onNewTab(true)
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("🕒 Lịch sử web", color = TextPrimary) },
                                leadingIcon = { Icon(Icons.Default.History, null, tint = TextSecondary) },
                                onClick = {
                                    showMenu = false
                                    onOpenHistory()
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("👤 Đăng nhập Google", color = Color(0xFF60A5FA), fontWeight = FontWeight.SemiBold) },
                                leadingIcon = { Icon(Icons.Default.AccountCircle, null, tint = Color(0xFF60A5FA)) },
                                onClick = {
                                    showMenu = false
                                    onNavigate("https://accounts.google.com")
                                }
                            )

                            HorizontalDivider(color = Color(0xFF374151), modifier = Modifier.padding(vertical = 4.dp))

                            DropdownMenuItem(
                                text = { Text("🔍 Google Lens (Chụp & Giải / OCR)", color = Color(0xFF38BDF8), fontWeight = FontWeight.SemiBold) },
                                leadingIcon = {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_google_lens),
                                        contentDescription = null,
                                        tint = Color.Unspecified,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    captureNormalScreen()
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("✨ Trợ lý Gemini (Giải bài text)", color = CyberCyan, fontWeight = FontWeight.SemiBold) },
                                leadingIcon = { Icon(Icons.Default.AutoAwesome, null, tint = CyberCyan) },
                                onClick = {
                                    showMenu = false
                                    onSetGeminiOverlayOpen(true)
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("🛠️ DevTools (Kiểm tra phần tử & Console)", color = Color(0xFF38BDF8)) },
                                leadingIcon = { Icon(Icons.Default.Terminal, null, tint = Color(0xFF38BDF8)) },
                                onClick = {
                                    showMenu = false
                                    onSetDevToolsOpen(true)
                                }
                            )

                            HorizontalDivider(color = Color(0xFF374151), modifier = Modifier.padding(vertical = 4.dp))

                            DropdownMenuItem(
                                text = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("🛡️ Lá Chắn Nhật Anh browser", color = CyberCyan, fontWeight = FontWeight.SemiBold)
                                        if (blockedOnThisPage > 0) {
                                            Text("($blockedOnThisPage)", color = CyberGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                },
                                leadingIcon = { Icon(Icons.Default.Security, null, tint = CyberCyan) },
                                onClick = {
                                    showMenu = false
                                    onOpenDashboard()
                                }
                            )

                            DropdownMenuItem(
                                text = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Trang web cho máy tính", color = TextPrimary)
                                        Checkbox(
                                            checked = config.isDesktopMode,
                                            onCheckedChange = {
                                                onToggleDesktopMode()
                                                showMenu = false
                                            },
                                            colors = CheckboxDefaults.colors(
                                                checkedColor = CyberCyan,
                                                checkmarkColor = Color.Black
                                            )
                                        )
                                    }
                                },
                                leadingIcon = { Icon(Icons.Default.Laptop, null, tint = TextSecondary) },
                                onClick = {
                                    onToggleDesktopMode()
                                    showMenu = false
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("Sao chép liên kết", color = TextPrimary) },
                                leadingIcon = { Icon(Icons.Default.ContentCopy, null, tint = TextSecondary) },
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("URL", currentUrl)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "Đã sao chép liên kết!", Toast.LENGTH_SHORT).show()
                                    showMenu = false
                                }
                            )

                            HorizontalDivider(color = Color(0xFF374151), modifier = Modifier.padding(vertical = 4.dp))

                            DropdownMenuItem(
                                text = { Text("Cài đặt", color = TextPrimary) },
                                leadingIcon = { Icon(Icons.Default.Settings, null, tint = TextSecondary) },
                                onClick = {
                                    showMenu = false
                                    onOpenSettings()
                                }
                            )
                        }
                    }
                }
            }

            // Chrome Loading Progress Bar
            AnimatedVisibility(visible = isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.5.dp),
                    color = if (isIncognito) Color(0xFFC084FC) else Color(0xFF38BDF8),
                    trackColor = Color(0xFF1F2937)
                )
            }

            // ==========================================
            // 2. FULLSCREEN CHROME WEBVIEW
            // ==========================================
            Box(modifier = Modifier.fillMaxSize()) {
                AndroidView(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("chrome_web_view"),
                    factory = { ctx ->
                        AntiReturnWebView(ctx).apply {
                            isShieldActive = config.isMasterShieldEnabled
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )

                            // Essential for soft keyboard (IME) input when user taps web input fields
                            isFocusable = true
                            isFocusableInTouchMode = true
                            setOnTouchListener { v, event ->
                                when (event.action) {
                                    MotionEvent.ACTION_DOWN, MotionEvent.ACTION_UP -> {
                                        if (!v.hasFocus()) {
                                            v.requestFocus()
                                        }
                                    }
                                }
                                false
                            }

                            // Hardware GPU acceleration enabled for 60-120fps smooth scrolling
                            setLayerType(View.LAYER_TYPE_NONE, null)

                            // Clean Chrome User-Agent (stripping ; wv and Version/X to allow Google login without disallowed_useragent)
                            val baseUa = WebSettings.getDefaultUserAgent(ctx)
                            val cleanChromeUa = baseUa
                                .replace("; wv", "")
                                .replace(Regex("Version/[0-9.]+ "), "")

                            settings.apply {
                                javaScriptEnabled = true
                                domStorageEnabled = true
                                databaseEnabled = true
                                useWideViewPort = true
                                loadWithOverviewMode = true
                                builtInZoomControls = true
                                displayZoomControls = false
                                mediaPlaybackRequiresUserGesture = false
                                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                                cacheMode = if (isIncognito) WebSettings.LOAD_NO_CACHE else WebSettings.LOAD_DEFAULT
                                javaScriptCanOpenWindowsAutomatically = true
                                setSupportMultipleWindows(true)
                                userAgentString = cleanChromeUa
                            }

                            // Enable & synchronize cookies like Chrome
                            val cookieManager = CookieManager.getInstance()
                            cookieManager.setAcceptCookie(true)
                            cookieManager.setAcceptThirdPartyCookies(this, true)

                            // Anti-Return monitoring JS interface
                            addJavascriptInterface(
                                AntiReturnJsBridge { type, domain, detail ->
                                    onBlockedEvent(type, domain, detail)
                                },
                                "AntiReturnBridge"
                            )

                            webViewClient = object : WebViewClient() {
                                override fun onRenderProcessGone(view: WebView?, detail: RenderProcessGoneDetail?): Boolean {
                                    view?.let { wv ->
                                        val parent = wv.parent as? ViewGroup
                                        parent?.removeView(wv)
                                        try {
                                            wv.destroy()
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }
                                    webViewRef = null
                                    return true
                                }

                                override fun onLoadResource(view: WebView?, url: String?) {
                                    super.onLoadResource(view, url)
                                    // Only log network resources when DevTools is actually open to prevent continuous recompositions
                                    if (isDevToolsOpen && url != null) {
                                        onAddNetworkLog(url, "GET", "RESOURCE")
                                    }
                                }

                                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                    super.onPageStarted(view, url, favicon)
                                    url?.let { onPageStarted(it) }

                                    if (config.isMasterShieldEnabled) {
                                        val script = AntiReturnScriptEngine.buildInjectionScript(config)
                                        view?.evaluateJavascript(script, null)
                                    }
                                }

                                override fun onPageCommitVisible(view: WebView?, url: String?) {
                                    super.onPageCommitVisible(view, url)
                                    if (config.isMasterShieldEnabled) {
                                        val script = AntiReturnScriptEngine.buildInjectionScript(config)
                                        view?.evaluateJavascript(script, null)
                                    }
                                }

                                override fun onPageFinished(view: WebView?, url: String?) {
                                    super.onPageFinished(view, url)
                                    val title = view?.title
                                    onPageFinished(
                                        url ?: "",
                                        title,
                                        view?.canGoBack() == true,
                                        view?.canGoForward() == true
                                    )

                                    // Persist cookies to disk
                                    CookieManager.getInstance().flush()

                                    if (config.isMasterShieldEnabled) {
                                        val script = AntiReturnScriptEngine.buildInjectionScript(config)
                                        view?.evaluateJavascript(script, null)
                                    }
                                }

                                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                                    val reqUrl = request?.url?.toString() ?: return false
                                    return if (reqUrl.startsWith("http://") || reqUrl.startsWith("https://")) {
                                        false
                                    } else {
                                        true
                                    }
                                }
                            }

                            webChromeClient = object : WebChromeClient() {
                                override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                                    consoleMessage?.let { msg ->
                                        val level = when (msg.messageLevel()) {
                                            ConsoleMessage.MessageLevel.ERROR -> "ERROR"
                                            ConsoleMessage.MessageLevel.WARNING -> "WARN"
                                            ConsoleMessage.MessageLevel.TIP -> "INFO"
                                            else -> "LOG"
                                         }
                                        onAddConsoleLog(level, msg.message(), msg.sourceId() ?: "")
                                    }
                                    return super.onConsoleMessage(consoleMessage)
                                }

                                override fun onReceivedTitle(view: WebView?, title: String?) {
                                    super.onReceivedTitle(view, title)
                                }

                                // Handle Google login popup windows & multiple window popups seamlessly in tab
                                override fun onCreateWindow(
                                    view: WebView?,
                                    isDialog: Boolean,
                                    isUserGesture: Boolean,
                                    resultMsg: Message?
                                ): Boolean {
                                    val transport = resultMsg?.obj as? WebView.WebViewTransport ?: return false
                                    val tempWv = WebView(ctx).apply {
                                        webViewClient = object : WebViewClient() {
                                            override fun shouldOverrideUrlLoading(v: WebView?, request: WebResourceRequest?): Boolean {
                                                request?.url?.toString()?.let { url ->
                                                    view?.loadUrl(url)
                                                }
                                                return true
                                            }

                                            override fun onPageStarted(v: WebView?, url: String?, favicon: Bitmap?) {
                                                if (url != null) {
                                                    view?.loadUrl(url)
                                                }
                                            }
                                        }
                                    }
                                    transport.webView = tempWv
                                    resultMsg.sendToTarget()
                                    return true
                                }
                            }

                            webViewRef = this
                            loadUrl(currentUrl)
                        }
                    },
                    update = { webView ->
                        webViewRef = webView
                        (webView as? AntiReturnWebView)?.isShieldActive = config.isMasterShieldEnabled

                        val baseUa = WebSettings.getDefaultUserAgent(context)
                        val cleanChromeUa = baseUa
                            .replace("; wv", "")
                            .replace(Regex("Version/[0-9.]+ "), "")

                        if (config.isDesktopMode) {
                            webView.settings.userAgentString =
                                "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
                        } else {
                            webView.settings.userAgentString = cleanChromeUa
                        }

                        if (webView.url != currentUrl && !isLoading) {
                            webView.loadUrl(currentUrl)
                        }
                    }
                )
            }
        }
    }

    // ==========================================
    // 3. CHROME PAGE INFO & SHIELD BOTTOM SHEET
    // ==========================================
    if (showShieldSheet) {
        ModalBottomSheet(
            onDismissRequest = { showShieldSheet = false },
            sheetState = sheetState,
            containerColor = Color(0xFF1F2937)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isIncognito) Icons.Default.VisibilityOff else Icons.Default.Lock,
                            contentDescription = null,
                            tint = if (isIncognito) Color(0xFFC084FC) else (if (config.isMasterShieldEnabled) CyberGreen else CyberRed),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isIncognito) "Tab ẩn danh đang hoạt động" else "Kết nối an toàn",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Nhật Anh browser Shield: ${if (config.isMasterShieldEnabled) "Đang hoạt động" else "Tắt"}",
                                fontSize = 12.sp,
                                color = if (config.isMasterShieldEnabled) CyberGreen else CyberRed
                            )
                        }
                    }

                    Switch(
                        checked = config.isMasterShieldEnabled,
                        onCheckedChange = {
                            onToggleMaster(it)
                            webViewRef?.reload()
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = CyberGreen
                        )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF111827))
                        .padding(14.dp)
                ) {
                    Column {
                        Text(
                            text = pageTitle.ifBlank { displayHost },
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (blockedOnThisPage > 0)
                                "Đã ngăn chặn $blockedOnThisPage lượt phát hiện rời tab/quay lại trên trang này!"
                            else
                                "Đang bảo vệ: Web không thể phát hiện khi bạn chuyển tab hay rời trang.",
                            fontSize = 12.sp,
                            color = CyberCyan
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "• visibilityState được giữ cố định là 'visible'\n• document.hasFocus() luôn trả về true\n• Bẫy nút quay lại (history back hijack) đã bị vô hiệu hóa",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "Mở Bảng Điều Khiển Chi Tiết ->",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberCyan,
                        modifier = Modifier
                            .clickable {
                                showShieldSheet = false
                                onOpenDashboard()
                            }
                            .padding(8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // ==========================================
    // 4. DEVTOOLS INSPECTOR SHEET
    // ==========================================
    if (isDevToolsOpen) {
        DevToolsSheet(
            webView = webViewRef,
            currentUrl = currentUrl,
            config = config,
            consoleLogs = consoleLogs,
            networkLogs = networkLogs,
            onClearLogs = onClearDevToolsLogs,
            onAddConsoleLog = onAddConsoleLog,
            onDismiss = { onSetDevToolsOpen(false) }
        )
    }

    // ==========================================
    // 5. GEMINI AI HOMEWORK SOLVER & OVERLAY DIALOG
    // ==========================================
    if (isGeminiOverlayOpen) {
        GeminiAssistantDialog(
            prompt = geminiPrompt,
            solution = geminiSolution,
            isLoading = isGeminiLoading,
            errorMessage = geminiError,
            webView = webViewRef,
            onPromptChanged = onGeminiPromptChanged,
            onSolve = { ctx, mode -> onExecuteGeminiSolve(ctx, mode) },
            onDismiss = { onSetGeminiOverlayOpen(false) }
        )
    }

    // ==========================================
    // 6. GOOGLE LENS IN-BROWSER SHEET
    // ==========================================
    GoogleLensSheet(
        isOpen = isGoogleLensOpen,
        screenshot = capturedScreenshot,
        activeTab = googleLensActiveTab,
        solution = lensAiSolution,
        ocrText = lensOcrText,
        isLoading = isLensLoading,
        errorMessage = lensError,
        onDismiss = onCloseGoogleLens,
        onTabSelected = onGoogleLensTabSelected,
        onRetakeScreenshot = {
            captureNormalScreen()
        },
        onExecuteAction = onExecuteLensAction,
        onOpenInBrowserTab = { url ->
            onNewTab(false)
            onNavigate(url)
        },
        webView = webViewRef,
        onOpenGeminiWithPrompt = { text ->
            onGeminiPromptChanged(text)
            onSetGeminiOverlayOpen(true)
        }
    )

    // ==========================================
    // 7. CHROME WEB BROWSING HISTORY SHEET
    // ==========================================
    HistorySheet(
        isOpen = isHistoryOpen,
        historyList = historyList,
        onDismiss = onCloseHistory,
        onNavigateToUrl = onNavigate,
        onDeleteEntry = onDeleteHistoryEntry,
        onClearAllHistory = onClearAllHistory
    )
}

/**
 * Capture visible WebView area into an in-memory Bitmap.
 * Saved in browser internal memory only, NOT saved to device gallery/external photos.
 */
private fun captureWebView(webView: WebView?): Bitmap? {
    if (webView == null || webView.width <= 0 || webView.height <= 0) return null
    return try {
        val bitmap = Bitmap.createBitmap(webView.width, webView.height, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        webView.draw(canvas)
        bitmap
    } catch (e: Throwable) {
        e.printStackTrace()
        null
    }
}


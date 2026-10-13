package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.webkit.CookieManager
import android.webkit.WebView
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeviceHub
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ConsoleLogItem
import com.example.model.NetworkRequestItem
import com.example.model.ShieldConfig
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberDarkNavy
import com.example.ui.theme.CyberGreen
import com.example.ui.theme.CyberRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DevToolsSheet(
    webView: WebView?,
    currentUrl: String,
    config: ShieldConfig,
    consoleLogs: List<ConsoleLogItem>,
    networkLogs: List<NetworkRequestItem>,
    onClearLogs: () -> Unit,
    onAddConsoleLog: (level: String, message: String, source: String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Console", "Elements", "Network", "Storage", "Nhật Anh Anti")

    // Console state
    var jsInput by remember { mutableStateOf("") }
    var consoleFilter by remember { mutableStateOf("ALL") }

    // Elements state
    var domHtml by remember { mutableStateOf("Đang tải mã nguồn DOM...") }
    var isFetchingHtml by remember { mutableStateOf(false) }

    // Storage state
    var cookiesText by remember { mutableStateOf("") }
    var localStorageText by remember { mutableStateOf("") }

    // Fetch DOM HTML
    fun fetchHtml() {
        if (webView == null) return
        isFetchingHtml = true
        webView.evaluateJavascript(
            "(function() { return document.documentElement.outerHTML; })();"
        ) { result ->
            isFetchingHtml = false
            if (result != null) {
                // unescape quotes
                val unescaped = result
                    .removePrefix("\"")
                    .removeSuffix("\"")
                    .replace("\\\"", "\"")
                    .replace("\\n", "\n")
                    .replace("\\t", "  ")
                    .replace("\\\\", "\\")
                domHtml = unescaped
            } else {
                domHtml = "Không thể lấy nội dung DOM của trang."
            }
        }
    }

    // Fetch Storage & Cookies
    fun fetchStorage() {
        cookiesText = try {
            val c = CookieManager.getInstance().getCookie(currentUrl)
            if (c.isNullOrBlank()) "Không có Cookie nào được lưu cho trang này." else c
        } catch (e: Exception) {
            "Lỗi khi đọc cookie: ${e.message}"
        }

        webView?.evaluateJavascript(
            "(function() { return JSON.stringify(window.localStorage); })();"
        ) { result ->
            localStorageText = result ?: "{}"
        }
    }

    LaunchedEffect(selectedTab) {
        if (selectedTab == 1 && domHtml.startsWith("Đang tải")) {
            fetchHtml()
        } else if (selectedTab == 3) {
            fetchStorage()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF0F172A),
        modifier = Modifier.fillMaxHeight(0.92f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            // Header bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Terminal,
                        contentDescription = null,
                        tint = CyberCyan,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "🛠️ Nhật Anh DevTools (Desktop Inspector)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Đóng",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Navigation Tabs
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color(0xFF1E293B),
                contentColor = CyberCyan,
                edgePadding = 0.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontSize = 13.sp,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == index) CyberCyan else TextSecondary
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Tab Content
            when (selectedTab) {
                0 -> {
                    // ==========================================
                    // 1. CONSOLE TAB
                    // ==========================================
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Console Toolbar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Filter buttons
                            Row(
                                modifier = Modifier
                                    .horizontalScroll(rememberScrollState())
                                    .weight(1f)
                            ) {
                                listOf("ALL", "LOG", "WARN", "ERROR").forEach { filter ->
                                    val isSelected = consoleFilter == filter
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isSelected) CyberCyan.copy(alpha = 0.2f) else Color(0xFF1E293B))
                                            .border(1.dp, if (isSelected) CyberCyan else Color.Transparent, RoundedCornerShape(6.dp))
                                            .clickable { consoleFilter = filter }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = filter,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) CyberCyan else TextSecondary
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                }
                            }

                            // Clear console
                            IconButton(onClick = onClearLogs, modifier = Modifier.size(28.dp)) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Xóa console",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // Quick JS snippets
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(vertical = 4.dp)
                        ) {
                            listOf(
                                "document.title",
                                "location.href",
                                "document.cookie",
                                "navigator.userAgent",
                                "window.innerWidth + 'x' + window.innerHeight",
                                "document.querySelectorAll('a').length + ' links'"
                            ).forEach { snippet ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFF1E293B))
                                        .clickable { jsInput = snippet }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = snippet,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = CyberCyan
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                        }

                        // Console Log Feed
                        val filteredLogs = remember(consoleLogs, consoleFilter) {
                            if (consoleFilter == "ALL") consoleLogs
                            else consoleLogs.filter { it.level.equals(consoleFilter, ignoreCase = true) }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF030712))
                                .border(1.dp, Color(0xFF1F2937), RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            if (filteredLogs.isEmpty()) {
                                Text(
                                    text = "> Console rỗng. Nhập JavaScript bên dưới để thực thi trên trang.",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp,
                                    color = TextMuted
                                )
                            } else {
                                LazyColumn(modifier = Modifier.fillMaxSize()) {
                                    items(filteredLogs) { log ->
                                        val color = when (log.level.uppercase()) {
                                            "ERROR" -> CyberRed
                                            "WARN" -> Color(0xFFF59E0B)
                                            "RESULT" -> CyberCyan
                                            else -> Color(0xFFE2E8F0)
                                        }
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 3.dp)
                                        ) {
                                            Text(
                                                text = if (log.level == "RESULT") "<- " else "> ",
                                                fontFamily = FontFamily.Monospace,
                                                fontSize = 11.sp,
                                                color = color
                                            )
                                            Text(
                                                text = log.message,
                                                fontFamily = FontFamily.Monospace,
                                                fontSize = 11.sp,
                                                color = color,
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                        HorizontalDivider(color = Color(0xFF111827), thickness = 0.5.dp)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Interactive JS Execution Bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF1E293B))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "JS >",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = CyberCyan,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            BasicTextField(
                                value = jsInput,
                                onValueChange = { jsInput = it },
                                singleLine = true,
                                textStyle = TextStyle(
                                    color = TextPrimary,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 13.sp
                                ),
                                cursorBrush = SolidColor(CyberCyan),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("devtools_js_input")
                            )

                            // Execute button
                            IconButton(
                                onClick = {
                                    val code = jsInput.trim()
                                    if (code.isNotEmpty()) {
                                        onAddConsoleLog("LOG", "> $code", "user")
                                        webView?.evaluateJavascript(code) { result ->
                                            val cleanRes = result?.removePrefix("\"")?.removeSuffix("\"") ?: "undefined"
                                            onAddConsoleLog("RESULT", cleanRes, "eval")
                                        }
                                        jsInput = ""
                                    }
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Chạy JS",
                                    tint = CyberCyan,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }

                1 -> {
                    // ==========================================
                    // 2. ELEMENTS (DOM) TAB
                    // ==========================================
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Mã nguồn DOM (document.documentElement.outerHTML):",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                            Row {
                                IconButton(onClick = { fetchHtml() }, modifier = Modifier.size(28.dp)) {
                                    Icon(Icons.Default.Refresh, "Tải lại DOM", tint = CyberCyan, modifier = Modifier.size(18.dp))
                                }
                                IconButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("DOM", domHtml))
                                        Toast.makeText(context, "Đã sao chép mã nguồn HTML!", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, "Sao chép HTML", tint = TextSecondary, modifier = Modifier.size(18.dp))
                                }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF030712))
                                .border(1.dp, Color(0xFF1F2937), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Text(
                                text = domHtml,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = Color(0xFF93C5FD),
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                2 -> {
                    // ==========================================
                    // 3. NETWORK TAB
                    // ==========================================
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Tài nguyên & Yêu cầu mạng (${networkLogs.size}):",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                            IconButton(onClick = onClearLogs, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Default.Delete, "Xóa logs mạng", tint = TextSecondary, modifier = Modifier.size(18.dp))
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF030712))
                                .border(1.dp, Color(0xFF1F2937), RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            if (networkLogs.isEmpty()) {
                                Text(
                                    text = "Chưa ghi nhận yêu cầu mạng nào hoặc trang đã tải xong.",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp,
                                    color = TextMuted
                                )
                            } else {
                                LazyColumn(modifier = Modifier.fillMaxSize()) {
                                    items(networkLogs) { req ->
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(Color(0xFF0284C7))
                                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = req.method,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.White
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = req.type,
                                                    fontSize = 10.sp,
                                                    color = CyberCyan
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = req.url,
                                                fontSize = 11.sp,
                                                fontFamily = FontFamily.Monospace,
                                                color = Color(0xFFE2E8F0),
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        HorizontalDivider(color = Color(0xFF1E293B), thickness = 0.5.dp)
                                    }
                                }
                            }
                        }
                    }
                }

                3 -> {
                    // ==========================================
                    // 4. STORAGE & COOKIES TAB
                    // ==========================================
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Cookies
                        Text(text = "🍪 Cookie (Domain hiện tại):", fontWeight = FontWeight.Bold, color = CyberCyan, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF030712))
                                .padding(8.dp)
                        ) {
                            Text(
                                text = cookiesText.ifBlank { "Không có cookie." },
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = Color(0xFFCBD5E1)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // LocalStorage
                        Text(text = "💾 LocalStorage:", fontWeight = FontWeight.Bold, color = CyberCyan, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF030712))
                                .padding(8.dp)
                        ) {
                            Text(
                                text = localStorageText.ifBlank { "{}" },
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = Color(0xFFCBD5E1)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // User Agent
                        Text(text = "📱 User Agent:", fontWeight = FontWeight.Bold, color = CyberCyan, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF030712))
                                .padding(8.dp)
                        ) {
                            Text(
                                text = webView?.settings?.userAgentString ?: "Chưa xác định",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = Color(0xFFCBD5E1)
                            )
                        }
                    }
                }

                4 -> {
                    // ==========================================
                    // 5. NHẬT ANH ANTI SHIELD DIAGNOSTICS
                    // ==========================================
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF1E293B))
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(
                                    text = "🛡️ Trạng Thái Lá Chắn Nhật Anh DEV Anti",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CyberCyan
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Master Shield: ${if (config.isMasterShieldEnabled) "ĐANG BẢO VỆ 100%" else "ĐÃ TẮT"}",
                                    fontSize = 12.sp,
                                    color = if (config.isMasterShieldEnabled) CyberGreen else CyberRed,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "• Visibility Lock: BẬT (document.visibilityState = 'visible')\n" +
                                            "• Focus & Blur Lock: BẬT (document.hasFocus() = true)\n" +
                                            "• Chặn bẫy History Trap (pushState spam): BẬT\n" +
                                            "• Chặn PageHide & Freeze: BẬT\n" +
                                            "• Chống bẫy Exit Intent (onbeforeunload): BẬT\n" +
                                            "• Giữ background timers chạy liên tục: BẬT\n" +
                                            "• Chống Visitor Fingerprint Canvas: BẬT",
                                    fontSize = 12.sp,
                                    color = TextSecondary,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

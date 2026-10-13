package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.webkit.WebView
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.engine.GeminiSolverService
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberDarkNavy
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.OnDeviceAiHelper

/**
 * Direct In-Browser Google Lens & Gemini AI Solver
 * Solves exercises, extracts OCR text, and performs visual search
 * directly inside the browser WITHOUT EXITING THE WEB APP.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoogleLensSheet(
    isOpen: Boolean,
    screenshot: Bitmap?,
    activeTab: String, // "SOLVE", "OCR", "SEARCH"
    solution: String,
    ocrText: String,
    isLoading: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onTabSelected: (String) -> Unit,
    onRetakeScreenshot: () -> Unit,
    onExecuteAction: (Context, String) -> Unit,
    onOpenInBrowserTab: (String) -> Unit,
    webView: WebView? = null,
    onOpenGeminiWithPrompt: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    if (!isOpen) return

    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    var showApiKeySection by remember { mutableStateOf(false) }
    var apiKeyInput by remember { mutableStateOf(GeminiSolverService.getApiKey(context)) }
    var currentModel by remember { mutableStateOf(GeminiSolverService.getSelectedModel(context)) }
    var showExternalAppsSection by remember { mutableStateOf(false) }

    // Auto-trigger analysis once if screenshot is present and solution/OCR is empty
    var hasAutoTriggered by remember(screenshot) { mutableStateOf(false) }
    LaunchedEffect(screenshot, isOpen) {
        if (isOpen && screenshot != null && !hasAutoTriggered) {
            val key = GeminiSolverService.getApiKey(context)
            if (key.isNotBlank() && key != "MY_GEMINI_API_KEY") {
                hasAutoTriggered = true
                onExecuteAction(context, activeTab)
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF0F172A),
        dragHandle = null,
        modifier = modifier.fillMaxHeight(0.92f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 16.dp)
        ) {
            // ==========================================
            // 1. LENS HEADER BAR
            // ==========================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E293B))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0F172A))
                            .border(1.5.dp, Color(0xFF38BDF8), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_google_lens),
                            contentDescription = "Google Lens",
                            tint = Color.Unspecified,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Google Lens & Gemini Trực Tiếp",
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = Color(0xFF0284C7),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "Trong Web",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Text(
                            text = "Xem kết quả & giải bài ngay trên trang • Không cần thoát web",
                            color = Color(0xFF38BDF8),
                            fontSize = 11.sp
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { showApiKeySection = !showApiKeySection },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = "API Key",
                            tint = if (GeminiSolverService.getApiKey(context).isBlank()) Color(0xFFF59E0B) else TextSecondary,
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Đóng",
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Scrollable Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                // ==========================================
                // 2. MODEL SELECTOR & QUOTA SAVING BAR
                // ==========================================
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1E293B))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ElectricBolt,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Mô hình AI:",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        // Flash 3.5 button
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable {
                                    currentModel = GeminiSolverService.MODEL_FLASH
                                    GeminiSolverService.setSelectedModel(context, GeminiSolverService.MODEL_FLASH)
                                    Toast.makeText(context, "Đã chọn: Gemini 3.5 Flash", Toast.LENGTH_SHORT).show()
                                },
                            color = if (currentModel == GeminiSolverService.MODEL_FLASH) Color(0xFF0284C7) else Color(0xFF0F172A),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "3.5 Flash",
                                color = if (currentModel == GeminiSolverService.MODEL_FLASH) Color.White else TextSecondary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        // Flash Lite 3.1 button (Saves quota)
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable {
                                    currentModel = GeminiSolverService.MODEL_FLASH_LITE
                                    GeminiSolverService.setSelectedModel(context, GeminiSolverService.MODEL_FLASH_LITE)
                                    Toast.makeText(context, "Đã chọn: 3.1 Flash-Lite (Tiết kiệm lượt nhất)", Toast.LENGTH_SHORT).show()
                                },
                            color = if (currentModel == GeminiSolverService.MODEL_FLASH_LITE) Color(0xFF059669) else Color(0xFF0F172A),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "3.1 Flash-Lite (Tiết kiệm)",
                                color = if (currentModel == GeminiSolverService.MODEL_FLASH_LITE) Color.White else TextSecondary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // ==========================================
                // 3. API KEY CONFIGURATION (COLLAPSIBLE)
                // ==========================================
                AnimatedVisibility(visible = showApiKeySection) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF1E293B),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "🔑 Cấu hình Gemini API Key",
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Free từ Google",
                                    color = Color(0xFF38BDF8),
                                    fontSize = 11.sp
                                )
                            }

                            Text(
                                text = "Lấy key hoàn toàn miễn phí tại aistudio.google.com/apikey rồi dán vào đây để sử dụng AI trực tiếp:",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )

                            OutlinedTextField(
                                value = apiKeyInput,
                                onValueChange = { apiKeyInput = it },
                                placeholder = { Text("Dán API Key (AIzaSy...)", color = TextMuted, fontSize = 11.sp) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CyberCyan,
                                    unfocusedBorderColor = Color(0xFF334155),
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                )
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Button(
                                    onClick = {
                                        GeminiSolverService.saveCustomApiKey(context, apiKeyInput.trim())
                                        Toast.makeText(context, "Đã lưu API Key thành công!", Toast.LENGTH_SHORT).show()
                                        showApiKeySection = false
                                        // Execute immediately with new key
                                        onExecuteAction(context, activeTab)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text("Lưu & Dùng Ngay", color = CyberDarkNavy, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // ==========================================
                // 4. CAPTURED SCREENSHOT PREVIEW & THUMBNAIL
                // ==========================================
                screenshot?.let { bmp ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFF334155), RoundedCornerShape(12.dp)),
                        color = Color(0xFF1E293B)
                    ) {
                        Column {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp)
                                    .background(Color(0xFF0F172A)),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    bitmap = bmp.asImageBitmap(),
                                    contentDescription = "Ảnh chụp màn hình web",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.fillMaxSize()
                                )

                                // Retake button overlay
                                Surface(
                                    color = Color(0xDD0F172A),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(8.dp)
                                        .clickable { onRetakeScreenshot() }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CameraAlt,
                                            contentDescription = null,
                                            tint = CyberCyan,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Chụp lại",
                                            color = CyberCyan,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "📸 ${bmp.width}x${bmp.height}px • Lưu tạm trong trình duyệt",
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )

                                Text(
                                    text = "Sẵn sàng phân tích",
                                    color = Color(0xFF10B981),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                } ?: run {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .background(Color(0xFF1E293B), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Chưa có ảnh chụp màn hình", color = TextSecondary, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Button(
                                onClick = onRetakeScreenshot,
                                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text("Chụp màn hình ngay", color = CyberDarkNavy, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // ==========================================
                // 5. DIRECT IN-BROWSER WORKSPACE (3 MAIN TABS)
                // ==========================================
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF1E293B),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.6f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "KẾT QUẢ AI TRỰC TIẾP TRÊN TRÌNH DUYỆT",
                                color = Color(0xFF38BDF8),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // 3 Feature Tabs
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            LensModeTab(
                                title = "📐 Giải bài",
                                icon = Icons.Default.AutoAwesome,
                                selected = activeTab == "SOLVE",
                                color = Color(0xFF38BDF8),
                                onClick = {
                                    onTabSelected("SOLVE")
                                    if (screenshot != null) onExecuteAction(context, "SOLVE")
                                },
                                modifier = Modifier.weight(1f)
                            )

                            LensModeTab(
                                title = "🔤 Chép chữ",
                                icon = Icons.Default.TextFields,
                                selected = activeTab == "OCR",
                                color = Color(0xFF34D399),
                                onClick = {
                                    onTabSelected("OCR")
                                    if (screenshot != null) onExecuteAction(context, "OCR")
                                },
                                modifier = Modifier.weight(1f)
                            )

                            LensModeTab(
                                title = "🔍 Tìm kiếm",
                                icon = Icons.Default.Search,
                                selected = activeTab == "SEARCH",
                                color = Color(0xFFFBBC04),
                                onClick = {
                                    onTabSelected("SEARCH")
                                    if (screenshot != null) onExecuteAction(context, "SEARCH")
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Big Action Button: Run / Re-run
                        Button(
                            onClick = { onExecuteAction(context, activeTab) },
                            enabled = !isLoading && screenshot != null,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = when (activeTab) {
                                    "OCR" -> Color(0xFF059669)
                                    "SEARCH" -> Color(0xFFD97706)
                                    else -> Color(0xFF0284C7)
                                }
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Đang phân tích trực tiếp trên trang...",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                Icon(Icons.Default.Refresh, null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = when (activeTab) {
                                        "OCR" -> "⚡ Trích xuất toàn bộ chữ (OCR) ngay"
                                        "SEARCH" -> "⚡ Nhận diện & Tìm kiếm ảnh ngay"
                                        else -> "⚡ Giải bài tập chi tiết ngay trong web"
                                    },
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        errorMessage?.let { err ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                color = Color(0x33EF4444),
                                shape = RoundedCornerShape(6.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444))
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = "⚠️ $err",
                                        color = Color(0xFFFCA5A5),
                                        fontSize = 11.sp
                                    )
                                    if (err.contains("API Key", ignoreCase = true)) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Button(
                                            onClick = { showApiKeySection = true },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text("Nhập API Key ngay", color = Color.White, fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Result Card Display
                        when (activeTab) {
                            "OCR" -> {
                                LensResultCard(
                                    title = "📝 Văn bản nhận diện được (OCR)",
                                    badge = "Trích Xuất Chữ",
                                    badgeColor = Color(0xFF059669),
                                    content = ocrText,
                                    placeholder = "Đang đọc chữ và số từ màn hình...",
                                    isLoading = isLoading,
                                    onCopy = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("Lens OCR", ocrText)
                                        clipboard.setPrimaryClip(clip)
                                        Toast.makeText(context, "Đã sao chép toàn bộ chữ!", Toast.LENGTH_SHORT).show()
                                    },
                                    onAskGemini = onOpenGeminiWithPrompt?.let { cb ->
                                        {
                                            cb(ocrText)
                                            onDismiss()
                                        }
                                    },
                                    onSearchWeb = {
                                        if (ocrText.isNotBlank()) {
                                            val query = ocrText.take(150).replace("\n", " ")
                                            onOpenInBrowserTab("https://www.google.com/search?q=${java.net.URLEncoder.encode(query, "UTF-8")}")
                                            onDismiss()
                                        }
                                    }
                                )
                            }
                            "SEARCH" -> {
                                LensResultCard(
                                    title = "🔍 Thông tin nhận diện & tìm kiếm",
                                    badge = "Tìm Kiếm Web",
                                    badgeColor = Color(0xFFD97706),
                                    content = solution,
                                    placeholder = "Đang tìm kiếm thông tin liên quan...",
                                    isLoading = isLoading,
                                    onCopy = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("Lens Search", solution)
                                        clipboard.setPrimaryClip(clip)
                                        Toast.makeText(context, "Đã sao chép!", Toast.LENGTH_SHORT).show()
                                    },
                                    onAskGemini = onOpenGeminiWithPrompt?.let { cb ->
                                        {
                                            cb(solution)
                                            onDismiss()
                                        }
                                    },
                                    onSearchWeb = {
                                        if (solution.isNotBlank()) {
                                            val query = solution.take(150).replace("\n", " ")
                                            onOpenInBrowserTab("https://www.google.com/search?q=${java.net.URLEncoder.encode(query, "UTF-8")}")
                                            onDismiss()
                                        }
                                    }
                                )
                            }
                            else -> {
                                LensResultCard(
                                    title = "✨ Lời giải bài tập chi tiết",
                                    badge = currentModel.replace("gemini-", "").replace("-preview", ""),
                                    badgeColor = Color(0xFF0284C7),
                                    content = solution,
                                    placeholder = "Đang đọc đề bài và giải chi tiết từng bước...",
                                    isLoading = isLoading,
                                    onCopy = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("Lời giải", solution)
                                        clipboard.setPrimaryClip(clip)
                                        Toast.makeText(context, "Đã sao chép lời giải!", Toast.LENGTH_SHORT).show()
                                    },
                                    onAskGemini = onOpenGeminiWithPrompt?.let { cb ->
                                        {
                                            cb("Giải thích thêm về bài tập này:\n$solution")
                                            onDismiss()
                                        }
                                    },
                                    onSearchWeb = {
                                        if (solution.isNotBlank()) {
                                            val query = solution.take(150).replace("\n", " ")
                                            onOpenInBrowserTab("https://www.google.com/search?q=${java.net.URLEncoder.encode(query, "UTF-8")}")
                                            onDismiss()
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // ==========================================
                // 6. ZERO-TOKEN / ZERO-API WEB TOOLS
                // ==========================================
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF1E293B),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "🌐 Công cụ miễn phí 100% trong trình duyệt (Không tốn token):",
                            color = TextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Button: Open Google Lens Web inside browser tab
                            Button(
                                onClick = {
                                    onOpenInBrowserTab("https://lens.google.com/")
                                    onDismiss()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)),
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD97706)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.OpenInBrowser, null, tint = Color(0xFFFBBF24), modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Mở Lens Web", color = Color(0xFFFBBF24), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            }

                            // Button: Extract text from DOM JS
                            Button(
                                onClick = {
                                    webView?.evaluateJavascript(
                                        """
                                        (function() {
                                            var sel = window.getSelection().toString().trim();
                                            if (sel.length > 0) return sel;
                                            var h = document.querySelector('h1, h2, .question, .de-bai, article, main');
                                            return h ? h.innerText.trim() : document.body.innerText.substring(0, 500);
                                        })();
                                        """.trimIndent()
                                    ) { res ->
                                        val clean = res?.removePrefix("\"")?.removeSuffix("\"")?.replace("\\n", "\n")?.replace("\\\"", "\"")?.trim()
                                        if (!clean.isNullOrBlank() && clean != "null") {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            clipboard.setPrimaryClip(ClipData.newPlainText("Web Text", clean))
                                            Toast.makeText(context, "Đã trích xuất chữ đề bài từ trang web!", Toast.LENGTH_SHORT).show()
                                            onOpenGeminiWithPrompt?.invoke(clean)
                                            onDismiss()
                                        } else {
                                            Toast.makeText(context, "Không tìm thấy văn bản nổi bật trên trang", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)),
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.TextFields, null, tint = Color(0xFF34D399), modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Lấy chữ từ Web", color = Color(0xFF34D399), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // ==========================================
                // 7. SECONDARY EXPORT: EXTERNAL APPS (OPTIONAL)
                // ==========================================
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF0F172A),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showExternalAppsSection = !showExternalAppsSection },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Smartphone,
                                    contentDescription = null,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Tùy chọn phụ: Chuyển ảnh sang ứng dụng ngoài máy",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }

                            Icon(
                                imageVector = if (showExternalAppsSection) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        AnimatedVisibility(visible = showExternalAppsSection) {
                            Column(modifier = Modifier.padding(top = 8.dp)) {
                                Text(
                                    text = "Lưu ý: Các nút bên dưới sẽ chuyển bạn sang ứng dụng khác trên điện thoại.",
                                    color = TextMuted,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )

                                OnDeviceAiActionButton(
                                    title = "Mở app Gemini trên máy",
                                    description = "Chuyển ảnh sang ứng dụng Gemini ngoài màn hình",
                                    badgeText = "App ngoài",
                                    badgeColor = Color(0xFF0284C7),
                                    icon = Icons.Default.AutoAwesome,
                                    iconTint = Color(0xFF38BDF8),
                                    onClick = {
                                        screenshot?.let { bmp ->
                                            OnDeviceAiHelper.openWithGeminiApp(context, bmp)
                                        } ?: Toast.makeText(context, "Chưa có ảnh chụp", Toast.LENGTH_SHORT).show()
                                    }
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                OnDeviceAiActionButton(
                                    title = "Mở app Google Lens trên máy",
                                    description = "Chuyển ảnh sang ứng dụng Google Lens ngoài màn hình",
                                    badgeText = "App ngoài",
                                    badgeColor = Color(0xFF059669),
                                    icon = Icons.Default.CameraAlt,
                                    iconTint = Color(0xFF34D399),
                                    onClick = {
                                        screenshot?.let { bmp ->
                                            OnDeviceAiHelper.openWithGoogleLensApp(context, bmp)
                                        } ?: Toast.makeText(context, "Chưa có ảnh chụp", Toast.LENGTH_SHORT).show()
                                    }
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                OnDeviceAiActionButton(
                                    title = "Chia sẻ cho AI khác (ChatGPT, Copilot...)",
                                    description = "Mở bảng chia sẻ của hệ điều hành Android",
                                    badgeText = "Chia sẻ",
                                    badgeColor = Color(0xFF7C3AED),
                                    icon = Icons.Default.Share,
                                    iconTint = Color(0xFFC084FC),
                                    onClick = {
                                        screenshot?.let { bmp ->
                                            OnDeviceAiHelper.shareToAnyAiApp(
                                                context,
                                                bmp,
                                                "Hãy giải bài tập này hoặc trích xuất văn bản trong hình"
                                            )
                                        } ?: Toast.makeText(context, "Chưa có ảnh chụp", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OnDeviceAiActionButton(
    title: String,
    description: String,
    badgeText: String,
    badgeColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() },
        color = Color(0xFF1E293B),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFF334155))
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(iconTint.copy(alpha = 0.15f))
                    .border(1.dp, iconTint.copy(alpha = 0.4f), RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = title,
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Surface(
                        color = badgeColor.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(4.dp),
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, badgeColor)
                    ) {
                        Text(
                            text = badgeText,
                            color = badgeColor,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }

                Text(
                    text = description,
                    color = Color(0xFF94A3B8),
                    fontSize = 10.sp,
                    lineHeight = 14.sp
                )
            }
        }
    }
}

@Composable
private fun LensModeTab(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() },
        color = if (selected) color.copy(alpha = 0.25f) else Color(0xFF0F172A),
        border = if (selected) androidx.compose.foundation.BorderStroke(1.dp, color) else androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFF334155)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) color else TextSecondary,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = title,
                color = if (selected) TextPrimary else TextSecondary,
                fontSize = 11.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@Composable
private fun LensResultCard(
    title: String,
    badge: String,
    badgeColor: Color,
    content: String,
    placeholder: String,
    isLoading: Boolean,
    onCopy: () -> Unit,
    onAskGemini: (() -> Unit)? = null,
    onSearchWeb: (() -> Unit)? = null
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .border(1.dp, Color(0xFF334155), RoundedCornerShape(10.dp)),
        color = Color(0xFF0F172A)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        color = badgeColor.copy(alpha = 0.25f),
                        shape = RoundedCornerShape(4.dp),
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, badgeColor)
                    ) {
                        Text(
                            text = badge,
                            color = badgeColor,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }

                if (content.isNotBlank()) {
                    IconButton(
                        onClick = onCopy,
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Sao chép",
                            tint = CyberCyan,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }

            HorizontalDivider(
                color = Color(0xFF334155),
                thickness = 0.5.dp,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            if (isLoading) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 14.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = CyberCyan,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = placeholder,
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            } else if (content.isBlank()) {
                Text(
                    text = "Bấm nút phía trên để bắt đầu phân tích trực tiếp trên trang.",
                    color = TextMuted,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                SelectionContainer {
                    Text(
                        text = content,
                        color = Color(0xFFE2E8F0),
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        fontFamily = FontFamily.Default
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Action buttons on result
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Button(
                        onClick = onCopy,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0369A1)),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.ContentCopy, null, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Sao chép", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }

                    if (onAskGemini != null) {
                        Button(
                            onClick = onAskGemini,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.AutoAwesome, null, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Hỏi thêm AI", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }

                    if (onSearchWeb != null) {
                        Button(
                            onClick = onSearchWeb,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                            shape = RoundedCornerShape(6.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Search, null, tint = CyberCyan, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Tìm Web", color = CyberCyan, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

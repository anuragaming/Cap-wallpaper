package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberDarkNavy
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.OnDeviceAiHelper

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
    modifier: Modifier = Modifier
) {
    if (!isOpen) return

    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showApiFallbackSection by remember { mutableStateOf(false) }

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
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0F172A))
                            .border(1.dp, Color(0xFF38BDF8), CircleShape),
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
                                text = "Google Lens & AI Trên Máy",
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = Color(0xFF059669),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "Free AI",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Text(
                            text = "Dùng app Gemini & Lens trên máy • Không tốn lượt API",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    }
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

            // Scrollable Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // ==========================================
                // 2. CAPTURED SCREENSHOT PREVIEW CARD
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
                                    .height(180.dp)
                                    .background(Color(0xFF0F172A)),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    bitmap = bmp.asImageBitmap(),
                                    contentDescription = "Ảnh chụp màn hình thực tế",
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
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CameraAlt,
                                            contentDescription = null,
                                            tint = CyberCyan,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Chụp lại màn hình",
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
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "📸 Ảnh màn hình chuẩn: ${bmp.width}x${bmp.height}px",
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )

                                Text(
                                    text = "Bộ nhớ cache tạm trình duyệt",
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
                            .height(130.dp)
                            .background(Color(0xFF1E293B), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Chưa có ảnh chụp màn hình", color = TextSecondary, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = onRetakeScreenshot,
                                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan)
                            ) {
                                Text("Chụp màn hình ngay", color = CyberDarkNavy, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // ==========================================
                // 3. ON-DEVICE AI INTEGRATION (ZERO API USAGE!)
                // ==========================================
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF1E293B),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Smartphone,
                                contentDescription = null,
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "DÙNG AI TRÊN MÁY (KHÔNG TỐN LƯỢT API)",
                                color = Color(0xFF38BDF8),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = "Gửi trực tiếp ảnh vừa chụp tới app có sẵn trên điện thoại của bạn để giải bài và tra cứu miễn phí 100%!",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                        )

                        // 3.1 Button: Gemini App on Device
                        OnDeviceAiActionButton(
                            title = "Mở bằng ứng dụng Gemini trên máy",
                            description = "Giải bài tập, làm văn, hỏi đáp chi tiết bằng app Gemini",
                            badgeText = "Khuyên Dùng",
                            badgeColor = Color(0xFF0284C7),
                            icon = Icons.Default.AutoAwesome,
                            iconTint = Color(0xFF38BDF8),
                            onClick = {
                                screenshot?.let { bmp ->
                                    OnDeviceAiHelper.openWithGeminiApp(context, bmp)
                                } ?: Toast.makeText(context, "Vui lòng chụp màn hình trước", Toast.LENGTH_SHORT).show()
                            }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // 3.2 Button: Google Lens App on Device
                        OnDeviceAiActionButton(
                            title = "Mở bằng ứng dụng Google Lens trên máy",
                            description = "Chép chữ OCR, giải toán, tìm kiếm hình ảnh gốc",
                            badgeText = "Tích Hợp Sẵn",
                            badgeColor = Color(0xFF059669),
                            icon = Icons.Default.CameraAlt,
                            iconTint = Color(0xFF34D399),
                            onClick = {
                                screenshot?.let { bmp ->
                                    OnDeviceAiHelper.openWithGoogleLensApp(context, bmp)
                                } ?: Toast.makeText(context, "Vui lòng chụp màn hình trước", Toast.LENGTH_SHORT).show()
                            }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // 3.3 Button: Share to Any AI App (ChatGPT, Copilot, etc.)
                        OnDeviceAiActionButton(
                            title = "Chia sẻ cho AI khác (ChatGPT, Copilot, Zalo...)",
                            description = "Mở bảng chia sẻ để chọn bất kỳ ứng dụng nào bạn thích",
                            badgeText = "Tự Chọn",
                            badgeColor = Color(0xFF7C3AED),
                            icon = Icons.Default.Share,
                            iconTint = Color(0xFFC084FC),
                            onClick = {
                                screenshot?.let { bmp ->
                                    OnDeviceAiHelper.shareToAnyAiApp(
                                        context,
                                        bmp,
                                        "Hãy giải bài tập này hoặc tóm tắt nội dung trong hình ảnh"
                                    )
                                } ?: Toast.makeText(context, "Vui lòng chụp màn hình trước", Toast.LENGTH_SHORT).show()
                            }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // 3.4 Button: Google Lens Web inside browser
                        OnDeviceAiActionButton(
                            title = "Mở Google Lens Web (lens.google.com)",
                            description = "Mở giao diện Lens Web trực tiếp trong tab trình duyệt",
                            badgeText = "Lens Web",
                            badgeColor = Color(0xFFD97706),
                            icon = Icons.Default.OpenInBrowser,
                            iconTint = Color(0xFFFBBF24),
                            onClick = {
                                onOpenInBrowserTab("https://lens.google.com/")
                                onDismiss()
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // ==========================================
                // 4. IN-APP GEMINI API OPTION (BACKUP / ADVANCED)
                // ==========================================
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF111827),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showApiFallbackSection = !showApiFallbackSection },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.ElectricBolt,
                                    contentDescription = null,
                                    tint = Color(0xFFF59E0B),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Dùng Gemini API tích hợp sẵn (Dự phòng)",
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Icon(
                                imageVector = if (showApiFallbackSection) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        AnimatedVisibility(visible = showApiFallbackSection) {
                            Column(modifier = Modifier.padding(top = 10.dp)) {
                                Surface(
                                    color = Color(0x33F59E0B),
                                    shape = RoundedCornerShape(6.dp),
                                    border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFFF59E0B))
                                ) {
                                    Text(
                                        text = "⚠️ Lưu ý: Tùy chọn này dùng API token cá nhân của bạn. Để tránh hết lượt dùng, bạn nên bấm các nút 'Dùng AI trên máy' ở trên.",
                                        color = Color(0xFFFDE68A),
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Feature Tabs
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    LensModeTab(
                                        title = "Giải bài",
                                        icon = Icons.Default.AutoAwesome,
                                        selected = activeTab == "SOLVE",
                                        color = Color(0xFF38BDF8),
                                        onClick = { onTabSelected("SOLVE") },
                                        modifier = Modifier.weight(1f)
                                    )

                                    LensModeTab(
                                        title = "Chép chữ (OCR)",
                                        icon = Icons.Default.TextFields,
                                        selected = activeTab == "OCR",
                                        color = Color(0xFF34D399),
                                        onClick = { onTabSelected("OCR") },
                                        modifier = Modifier.weight(1f)
                                    )

                                    LensModeTab(
                                        title = "Tìm kiếm",
                                        icon = Icons.Default.Search,
                                        selected = activeTab == "SEARCH",
                                        color = Color(0xFFFBBC04),
                                        onClick = { onTabSelected("SEARCH") },
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Execute button
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
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Đang gọi Gemini API...", color = Color.White, fontSize = 12.sp)
                                    } else {
                                        Icon(Icons.Default.Refresh, null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = when (activeTab) {
                                                "OCR" -> "Trích xuất chữ bằng API"
                                                "SEARCH" -> "Tìm kiếm hình ảnh bằng API"
                                                else -> "Giải bài tập bằng API"
                                            },
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                errorMessage?.let { err ->
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(text = "⚠️ $err", color = Color(0xFFFCA5A5), fontSize = 11.sp)
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Result Display
                                when (activeTab) {
                                    "OCR" -> {
                                        LensResultCard(
                                            title = "📝 Văn bản nhận diện được (OCR)",
                                            badge = "OCR Result",
                                            badgeColor = Color(0xFF059669),
                                            content = ocrText,
                                            placeholder = "Đang nhận diện chữ viết...",
                                            isLoading = isLoading,
                                            onCopy = {
                                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                val clip = ClipData.newPlainText("Lens OCR", ocrText)
                                                clipboard.setPrimaryClip(clip)
                                                Toast.makeText(context, "Đã sao chép chữ!", Toast.LENGTH_SHORT).show()
                                            }
                                        )
                                    }
                                    "SEARCH" -> {
                                        LensResultCard(
                                            title = "🔍 Thông tin tìm kiếm hình ảnh",
                                            badge = "Search Result",
                                            badgeColor = Color(0xFFD97706),
                                            content = solution,
                                            placeholder = "Đang phân tích ảnh...",
                                            isLoading = isLoading,
                                            onCopy = {
                                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                val clip = ClipData.newPlainText("Lens Search", solution)
                                                clipboard.setPrimaryClip(clip)
                                                Toast.makeText(context, "Đã sao chép!", Toast.LENGTH_SHORT).show()
                                            }
                                        )
                                    }
                                    else -> {
                                        LensResultCard(
                                            title = "✨ Lời giải bài tập chi tiết",
                                            badge = "Gemini Flash",
                                            badgeColor = Color(0xFF0284C7),
                                            content = solution,
                                            placeholder = "Đang giải bài tập...",
                                            isLoading = isLoading,
                                            onCopy = {
                                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                val clip = ClipData.newPlainText("Lời giải", solution)
                                                clipboard.setPrimaryClip(clip)
                                                Toast.makeText(context, "Đã sao chép lời giải!", Toast.LENGTH_SHORT).show()
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
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() },
        color = Color(0xFF0F172A),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(iconTint.copy(alpha = 0.15f))
                    .border(1.dp, iconTint.copy(alpha = 0.4f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = title,
                        color = TextPrimary,
                        fontSize = 13.sp,
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
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = description,
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp,
                    lineHeight = 15.sp
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
        color = if (selected) color.copy(alpha = 0.2f) else Color(0xFF1E293B),
        border = if (selected) androidx.compose.foundation.BorderStroke(1.dp, color) else null,
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
    onCopy: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .border(1.dp, Color(0xFF334155), RoundedCornerShape(10.dp)),
        color = Color(0xFF1E293B)
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
                        .padding(vertical = 12.dp),
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
                    text = "Bấm nút phía trên để bắt đầu phân tích bằng API.",
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

                Button(
                    onClick = onCopy,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0369A1)),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.ContentCopy, null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Sao chép nội dung này", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }
        }
    }
}

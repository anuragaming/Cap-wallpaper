package com.example.ui.screens

import android.webkit.CookieManager
import android.webkit.WebStorage
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ShieldConfig
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberCard
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberDarkNavy
import com.example.ui.theme.CyberGreen
import com.example.ui.theme.CyberRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun SettingsScreen(
    config: ShieldConfig,
    onToggleDesktopMode: () -> Unit,
    onResetStats: () -> Unit,
    onClearLogs: () -> Unit,
    onBackToBrowser: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CyberDarkNavy)
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackToBrowser,
                modifier = Modifier.testTag("settings_back_to_browser")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Quay lại duyệt web",
                    tint = TextPrimary
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Cài Đặt & Quyền Riêng Tư",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Section: User Agent
        Text(
            text = "CHẾ ĐỘ HIỂN THỊ TRÌNH DUYỆT",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondary,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(CyberCard)
                .border(1.dp, CyberBorder, RoundedCornerShape(16.dp))
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (config.isDesktopMode) Icons.Default.Computer else Icons.Default.PhoneAndroid,
                        contentDescription = null,
                        tint = CyberCyan,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (config.isDesktopMode) "Giao diện Desktop (Máy tính)" else "Giao diện Mobile (Di động)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Text(
                            text = if (config.isDesktopMode) "Giả lập Chrome trên Linux x86_64" else "Chrome Mobile mặc định",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }

                OutlinedButton(
                    onClick = onToggleDesktopMode,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("settings_toggle_ua")
                ) {
                    Text(text = "Đổi Chế Độ", fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Section: External Browser Support (Chrome, Edge, Kiwi, Firefox)
        Text(
            text = "HỖ TRỢ TRÌNH DUYỆT NGOÀI (CHROME, EDGE, KIWI, FIREFOX)",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = CyberCyan,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(CyberCard)
                .border(1.dp, CyberCyan.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.PrivacyTip,
                        contentDescription = null,
                        tint = CyberCyan,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Cách dùng trên Chrome, Edge & Samsung Internet",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Do cơ chế bảo mật Sandbox của Android, không một ứng dụng nào được tự ý can thiệp vào tiến trình của Google Chrome ngoài mà không cần sự tương tác của bạn. Tuy nhiên, bạn hoàn toàn có thể dùng 2 cách cực kỳ tiện lợi sau:",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    lineHeight = 17.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Option A: Bookmarklet
                Text(
                    text = "Cách 1: Bookmarklet 1-chạm (Dành cho Chrome, Edge, Safari)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyberGreen
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "• Bấm nút Copy bên dưới -> Vào Chrome tạo Dấu trang (Bookmark) mới tên '!AntiReturn', dán mã vừa copy vào phần URL.\n• Mỗi khi vào bất kỳ trang web nào có Return Detector, chỉ cần gõ '!AntiReturn' trên thanh URL hoặc bấm Dấu trang đó, tính năng Return Detector của web sẽ bị khóa lập tức!",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                        val clip = android.content.ClipData.newPlainText("Bookmarklet", com.example.engine.ExternalBrowserScriptHelper.getBookmarkletCode())
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Đã copy Bookmarklet! Hãy tạo Dấu trang trong Chrome để dùng.", Toast.LENGTH_LONG).show()
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyberCyan,
                        contentColor = CyberDarkNavy
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("copy_bookmarklet_btn")
                ) {
                    Text("Sao Chép Mã Bookmarklet Cho Chrome", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Option B: Userscript
                Text(
                    text = "Cách 2: Userscript Tự Động (Dành cho Kiwi Browser, Firefox Android)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyberCyan
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "• Nếu bạn dùng Kiwi Browser hoặc Firefox có cài Tampermonkey / Violentmonkey:\n• Copy Userscript này cài vào tiện ích mở rộng, 100% trang web sẽ TỰ ĐỘNG được chặn Return Detector mà bạn không cần phải làm bất kỳ thao tác nào!",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = {
                        val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                        val clip = android.content.ClipData.newPlainText("Userscript", com.example.engine.ExternalBrowserScriptHelper.getUserscriptCode())
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Đã copy Userscript cho Tampermonkey / Violentmonkey!", Toast.LENGTH_LONG).show()
                    },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("copy_userscript_btn")
                ) {
                    Text("Sao Chép Userscript (Kiwi/Firefox)", fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Section: Storage & Data Clean
        Text(
            text = "DỮ LIỆU & LÀM SẠCH TRACKING",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondary,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            ActionCard(
                title = "Xóa Cookie & LocalStorage của Web",
                subtitle = "Xóa sạch mã định danh người dùng cũ để web không nhận diện lượt quay lại.",
                icon = Icons.Default.CleaningServices,
                buttonText = "Xóa Bộ Nhớ Web",
                testTag = "clean_cookies_button",
                onClick = {
                    CookieManager.getInstance().removeAllCookies(null)
                    CookieManager.getInstance().flush()
                    WebStorage.getInstance().deleteAllData()
                    Toast.makeText(context, "Đã xóa sạch Cookie và bộ nhớ đệm web!", Toast.LENGTH_SHORT).show()
                }
            )

            ActionCard(
                title = "Đặt Lại Bộ Đếm Thống Kê",
                subtitle = "Đặt lại toàn bộ số lần chặn Return Detector về 0.",
                icon = Icons.Default.Restore,
                buttonText = "Đặt Lại",
                testTag = "reset_stats_button",
                onClick = {
                    onResetStats()
                    Toast.makeText(context, "Đã đặt lại bộ đếm thống kê!", Toast.LENGTH_SHORT).show()
                }
            )

            ActionCard(
                title = "Xóa Nhật Ký Hoạt Động",
                subtitle = "Xóa danh sách các lời gọi JavaScript đã bị chặn.",
                icon = Icons.Default.DeleteForever,
                buttonText = "Xóa Nhật Ký",
                testTag = "clear_all_logs_button",
                onClick = {
                    onClearLogs()
                    Toast.makeText(context, "Đã xóa toàn bộ nhật ký!", Toast.LENGTH_SHORT).show()
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Section: About & Privacy
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(CyberCard)
                .border(1.dp, CyberBorder, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.PrivacyTip,
                        contentDescription = null,
                        tint = CyberCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Về Nhật Anh browser",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "• Nguyên lý: Can thiệp lớp thực thi DOM & Page Visibility API ngay từ thời điểm document_start.\n" +
                            "• Khi bật tính năng, website vẫn tải đầy đủ giao diện và tính năng bình thường, nhưng không thể biết khi nào bạn chuyển tab, rời ứng dụng hoặc ấn quay lại.\n" +
                            "• Toàn bộ quá trình chặn diễn ra 100% trên thiết bị của bạn, bảo mật tuyệt đối không gửi bất kỳ dữ liệu nào ra máy chủ bên ngoài.",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    lineHeight = 17.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Phiên bản 1.0.0 (Anti-Detection Engine Active)",
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun ActionCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    buttonText: String,
    testTag: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CyberCard)
            .border(1.dp, CyberBorder, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = CyberCyan,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = onClick,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CyberCyan.copy(alpha = 0.2f),
                    contentColor = CyberCyan
                ),
                modifier = Modifier.testTag(testTag)
            ) {
                Text(text = buttonText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

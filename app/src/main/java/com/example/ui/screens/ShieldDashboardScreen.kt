package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.InterceptionLog
import com.example.model.ShieldConfig
import com.example.model.ShieldStats
import com.example.service.FloatingShieldService
import com.example.ui.components.InterceptionFeedView
import com.example.ui.components.ShieldStatusCard
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberCard
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberDarkNavy
import com.example.ui.theme.CyberGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun ShieldDashboardScreen(
    config: ShieldConfig,
    stats: ShieldStats,
    logs: List<InterceptionLog>,
    onToggleMaster: (Boolean) -> Unit,
    onClearLogs: () -> Unit,
    onBackToBrowser: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var isFloatingEnabled by remember {
        mutableStateOf(FloatingShieldService.isServiceRunning)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CyberDarkNavy)
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Top Header with Back button to return to browser
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackToBrowser,
                modifier = Modifier.testTag("dashboard_back_to_browser")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Quay lại duyệt web",
                    tint = TextPrimary
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Nhật Anh browser",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        // Master Shield Radar Card
        ShieldStatusCard(
            isEnabled = config.isMasterShieldEnabled,
            stats = stats,
            onToggleMaster = onToggleMaster
        )

        Spacer(modifier = Modifier.height(16.dp))

        // All Modules Active Summary Card (Clean, un-cluttered design)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(CyberCard)
                .border(1.dp, CyberCyan.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                .padding(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = CyberGreen,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Toàn Bộ 7 Lớp Bảo Vệ Đang Bật",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Tự động kích hoạt: Visibility Lock, Focus Shield, Chống bẫy Back, Page-Hide, Timer Keep-Alive & Anti-Fingerprint.",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        lineHeight = 15.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Persistent Floating Action Button Controller Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(CyberCard)
                .border(1.dp, if (isFloatingEnabled) CyberCyan else CyberBorder, RoundedCornerShape(16.dp))
                .padding(16.dp)
                .testTag("floating_overlay_controller_card")
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isFloatingEnabled) CyberCyan.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PictureInPictureAlt,
                                contentDescription = null,
                                tint = if (isFloatingEnabled) CyberCyan else TextMuted,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "Nút Nổi Trên Ứng Dụng Khác",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Persistent Floating Action Button",
                                fontSize = 11.sp,
                                color = CyberCyan
                            )
                        }
                    }

                    Switch(
                        checked = isFloatingEnabled,
                        onCheckedChange = { enable ->
                            if (enable) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
                                    val intent = Intent(
                                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                        Uri.parse("package:${context.packageName}")
                                    ).apply {
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    }
                                    context.startActivity(intent)
                                } else {
                                    val serviceIntent = Intent(context, FloatingShieldService::class.java).apply {
                                        action = FloatingShieldService.ACTION_START
                                    }
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                        context.startForegroundService(serviceIntent)
                                    } else {
                                        context.startService(serviceIntent)
                                    }
                                    isFloatingEnabled = true
                                }
                            } else {
                                val serviceIntent = Intent(context, FloatingShieldService::class.java).apply {
                                    action = FloatingShieldService.ACTION_STOP
                                }
                                context.startService(serviceIntent)
                                isFloatingEnabled = false
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = CyberCyan,
                            uncheckedThumbColor = TextMuted,
                            uncheckedTrackColor = CyberBorder
                        ),
                        modifier = Modifier.testTag("toggle_floating_service_switch")
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Khi bật, nút tròn Cyber Shield sẽ luôn nổi đè lên màn hình thiết bị và các trình duyệt ngoài (Chrome, Edge, v.v.). Bạn có thể kéo thả nút tới bất kỳ vị trí nào và chạm vào để mở nhanh bảng điều khiển.",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    lineHeight = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Live Feed of Interceptions
        InterceptionFeedView(
            logs = logs,
            onClearLogs = onClearLogs
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

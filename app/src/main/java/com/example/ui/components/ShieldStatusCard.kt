package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ShieldStats
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberCard
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberGreen
import com.example.ui.theme.CyberRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun ShieldStatusCard(
    isEnabled: Boolean,
    stats: ShieldStats,
    onToggleMaster: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val statusColor by animateColorAsState(
        targetValue = if (isEnabled) CyberGreen else CyberRed,
        label = "statusColor"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = if (isEnabled) 0.8f else 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(24.dp))
            .testTag("shield_status_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CyberCard)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            CyberCard,
                            if (isEnabled) CyberGreen.copy(alpha = 0.08f) else CyberRed.copy(alpha = 0.05f)
                        )
                    )
                )
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header: Status Badge + Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isEnabled) "ĐANG BẢO VỆ" else "ĐÃ TẮT BẢO VỆ",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor,
                        letterSpacing = 1.sp
                    )
                }

                Switch(
                    checked = isEnabled,
                    onCheckedChange = onToggleMaster,
                    modifier = Modifier.testTag("master_shield_switch"),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = CyberGreen,
                        uncheckedThumbColor = TextMuted,
                        uncheckedTrackColor = CyberBorder
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Center Radar Shield Graphic
            Box(
                modifier = Modifier.size(120.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(120.dp)) {
                    if (isEnabled) {
                        // Outer pulsating ring
                        drawCircle(
                            color = statusColor.copy(alpha = pulseAlpha * 0.4f),
                            radius = size.minDimension / 2f,
                            style = Stroke(width = 2.dp.toPx())
                        )
                        // Middle wave ring
                        drawCircle(
                            color = statusColor.copy(alpha = pulseAlpha * 0.7f),
                            radius = size.minDimension / 2.7f,
                            style = Stroke(width = 1.5.dp.toPx())
                        )
                    }
                    // Inner solid circle
                    drawCircle(
                        color = if (isEnabled) CyberGreen.copy(alpha = 0.15f) else CyberRed.copy(alpha = 0.1f),
                        radius = size.minDimension / 3.4f
                    )
                }

                Icon(
                    imageVector = if (isEnabled) Icons.Default.Security else Icons.Default.Block,
                    contentDescription = "Shield Icon",
                    tint = statusColor,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = if (isEnabled) "Nhật Anh browser Đang Bảo Vệ" else "Bảo Vệ Đang Bị Tắt",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Text(
                text = if (isEnabled)
                    "Trang web không thể phát hiện bạn chuyển tab, rời màn hình hay ấn nút quay lại."
                else
                    "Trang web có thể kích hoạt Return Detector để theo dõi và dừng đếm giờ của bạn.",
                fontSize = 12.sp,
                color = TextSecondary,
                lineHeight = 17.sp,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Quick Stats Counter
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                    .border(1.dp, CyberBorder, RoundedCornerShape(16.dp))
                    .padding(vertical = 12.dp, horizontal = 14.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                StatItem(
                    count = stats.totalBlocked,
                    label = "Tổng Chặn",
                    color = CyberCyan
                )
                StatItem(
                    count = stats.visibilityBlocked,
                    label = "Chuyển Tab",
                    color = CyberGreen
                )
                StatItem(
                    count = stats.blurBlocked,
                    label = "Mất Focus",
                    color = CyberCyan
                )
                StatItem(
                    count = stats.historyTrapBlocked,
                    label = "Bẫy Back",
                    color = CyberRed
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    count: Int,
    label: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count.toString(),
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            color = color
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = TextSecondary
        )
    }
}

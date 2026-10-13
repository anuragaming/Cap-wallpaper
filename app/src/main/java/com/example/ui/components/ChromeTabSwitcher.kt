package com.example.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BrowserTab
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberDarkNavy
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.net.URI

@Composable
fun ChromeTabSwitcher(
    tabs: List<BrowserTab>,
    activeTabId: String,
    onSelectTab: (String) -> Unit,
    onCloseTab: (String) -> Unit,
    onNewTab: (isIncognito: Boolean) -> Unit,
    onCloseAllTabs: (isIncognitoOnly: Boolean?) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 0: Regular Tabs, 1: Incognito Tabs
    var selectedTabGroup by remember {
        val initialIncognito = tabs.find { it.id == activeTabId }?.isIncognito ?: false
        mutableIntStateOf(if (initialIncognito) 1 else 0)
    }

    var showMenu by remember { mutableStateOf(false) }

    val regularTabs = tabs.filter { !it.isIncognito }
    val incognitoTabs = tabs.filter { it.isIncognito }
    val displayedTabs = if (selectedTabGroup == 0) regularTabs else incognitoTabs

    val backgroundColor = if (selectedTabGroup == 1) Color(0xFF181824) else Color(0xFF1F2937)

    BackHandler {
        onDismiss()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        // Top Action Bar
        Surface(
            color = if (selectedTabGroup == 1) Color(0xFF12121C) else Color(0xFF111827),
            tonalElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tab Mode Selector (Regular Tabs vs Incognito Tabs)
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Regular Tab Icon Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selectedTabGroup == 0) Color(0xFF374151) else Color.Transparent)
                            .clickable { selectedTabGroup = 0 }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Public,
                                contentDescription = "Thẻ thông thường",
                                tint = if (selectedTabGroup == 0) CyberCyan else TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${regularTabs.size}",
                                color = if (selectedTabGroup == 0) Color.White else TextSecondary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Incognito Tab Icon Button (Glasses & Hat style)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selectedTabGroup == 1) Color(0xFF2E263D) else Color.Transparent)
                            .clickable { selectedTabGroup = 1 }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.VisibilityOff,
                                contentDescription = "Thẻ ẩn danh",
                                tint = if (selectedTabGroup == 1) Color(0xFFC084FC) else TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${incognitoTabs.size}",
                                color = if (selectedTabGroup == 1) Color(0xFFE9D5FF) else TextSecondary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                // Add new tab (+) button
                IconButton(
                    onClick = { onNewTab(selectedTabGroup == 1) },
                    modifier = Modifier
                        .size(38.dp)
                        .testTag("tab_switcher_add_tab_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Thêm tab mới",
                        tint = TextPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // More Options Menu
                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Menu tùy chọn",
                            tint = TextPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(Color(0xFF1F2937))
                    ) {
                        DropdownMenuItem(
                            text = { Text("Thẻ mới", color = TextPrimary) },
                            onClick = {
                                showMenu = false
                                onNewTab(false)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Thẻ ẩn danh mới 🕶️", color = Color(0xFFC084FC)) },
                            onClick = {
                                showMenu = false
                                onNewTab(true)
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    if (selectedTabGroup == 1) "Đóng tất cả thẻ ẩn danh" else "Đóng tất cả các thẻ",
                                    color = Color(0xFFEF4444)
                                )
                            },
                            onClick = {
                                showMenu = false
                                onCloseAllTabs(selectedTabGroup == 1)
                            }
                        )
                    }
                }

                // Done / Back to Browser button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onDismiss() }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Xong",
                        color = CyberCyan,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }

        // Incognito Banner Notice when on Incognito tab
        AnimatedVisibility(visible = selectedTabGroup == 1) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF271E3A))
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.VisibilityOff,
                        contentDescription = null,
                        tint = Color(0xFFC084FC),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Chế độ ẩn danh: Lịch sử, cookie và dữ liệu trang web sẽ không được lưu sau khi đóng tab.",
                        color = Color(0xFFE9D5FF),
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // Empty State or Tabs Grid
        if (displayedTabs.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(
                        imageVector = if (selectedTabGroup == 1) Icons.Default.VisibilityOff else Icons.Default.Public,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = if (selectedTabGroup == 1) "Không có thẻ ẩn danh nào" else "Không có thẻ nào",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Nhấn dấu (+) ở góc trên để mở thẻ mới",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            // Chrome Tab Cards 2-Column Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                items(displayedTabs, key = { it.id }) { tab ->
                    val isActive = tab.id == activeTabId
                    ChromeTabCard(
                        tab = tab,
                        isActive = isActive,
                        onSelect = { onSelectTab(tab.id) },
                        onClose = { onCloseTab(tab.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun ChromeTabCard(
    tab: BrowserTab,
    isActive: Boolean,
    onSelect: () -> Unit,
    onClose: () -> Unit
) {
    val borderColor = when {
        isActive && tab.isIncognito -> Color(0xFFC084FC)
        isActive -> CyberCyan
        tab.isIncognito -> Color(0xFF3B2F52)
        else -> Color(0xFF374151)
    }

    val cardBg = if (tab.isIncognito) Color(0xFF221A30) else Color(0xFF1E293B)

    val hostText = remember(tab.url) {
        try {
            val uri = URI(tab.url)
            uri.host ?: tab.url
        } catch (e: Exception) {
            tab.url
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(cardBg)
            .border(
                width = if (isActive) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onSelect() }
            .testTag("tab_card_${tab.id}")
    ) {
        // Tab Card Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(if (tab.isIncognito) Color(0xFF1B1427) else Color(0xFF0F172A))
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (tab.isIncognito) Icons.Default.VisibilityOff else Icons.Default.Language,
                contentDescription = null,
                tint = if (tab.isIncognito) Color(0xFFC084FC) else CyberCyan,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = tab.title.ifBlank { hostText },
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            // Close Tab Button
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .clickable { onClose() }
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Đóng thẻ",
                    tint = TextSecondary,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        // Tab Card Preview Body (Chrome thumbnail style)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Public,
                    contentDescription = null,
                    tint = TextMuted.copy(alpha = 0.5f),
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = hostText,
                    fontSize = 11.sp,
                    color = TextSecondary,
                    maxLines = 2,
                    textAlign = TextAlign.Center,
                    overflow = TextOverflow.Ellipsis
                )
                if (isActive) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (tab.isIncognito) Color(0xFF6B21A8) else CyberCyan.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Đang xem",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (tab.isIncognito) Color(0xFFE9D5FF) else CyberCyan
                        )
                    }
                }
            }
        }
    }
}

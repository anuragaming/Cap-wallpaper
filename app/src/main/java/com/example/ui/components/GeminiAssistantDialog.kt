package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.FindInPage
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.GeminiSolverService
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberDarkNavy
import com.example.ui.theme.CyberGreen
import com.example.ui.theme.CyberRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeminiAssistantDialog(
    prompt: String,
    solution: String,
    isLoading: Boolean,
    errorMessage: String?,
    webView: WebView?,
    onPromptChanged: (String) -> Unit,
    onSolve: (Context, mode: String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var currentMode by remember { mutableStateOf("SOLVE") } // "SOLVE", "SEARCH", "SUMMARIZE", "EXPLAIN"
    var showApiKeyDialog by remember { mutableStateOf(false) }
    var selectedModel by remember { mutableStateOf(GeminiSolverService.getSelectedModel(context)) }
    var apiKeyInput by remember { mutableStateOf(GeminiSolverService.getApiKey(context)) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF0F172A),
        modifier = Modifier.fillMaxHeight(0.92f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF38BDF8), Color(0xFFA855F7), Color(0xFFEC4899))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Trợ Lý Gemini AI",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFF1E293B))
                                    .border(0.5.dp, CyberCyan, RoundedCornerShape(4.dp))
                                    .clickable {
                                        val newModel = if (selectedModel == GeminiSolverService.MODEL_FLASH) {
                                            GeminiSolverService.MODEL_FLASH_LITE
                                        } else {
                                            GeminiSolverService.MODEL_FLASH
                                        }
                                        selectedModel = newModel
                                        GeminiSolverService.setSelectedModel(context, newModel)
                                        Toast.makeText(context, "Đã chọn: $newModel", Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (selectedModel == GeminiSolverService.MODEL_FLASH) "3.5 Flash" else "3.1 Flash-Lite (Tiết kiệm)",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CyberCyan
                                )
                            }
                        }
                        Text(
                            text = "Giải bài tập & Tìm kiếm ngay trên trang mà không cần thoát",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { showApiKeyDialog = true }, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Cấu hình API Key",
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp)
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
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Mode Selector Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    Triple("SOLVE", "📐 Giải bài tập chi tiết", Color(0xFF38BDF8)),
                    Triple("SEARCH", "🔍 Tìm kiếm thông minh", Color(0xFF34D399)),
                    Triple("SUMMARIZE", "💡 Tóm tắt trang này", Color(0xFFFBBF24)),
                    Triple("EXPLAIN", "🧠 Giải thích khái niệm", Color(0xFFA78BFA))
                ).forEach { (mode, label, color) ->
                    val isSelected = currentMode == mode
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) color.copy(alpha = 0.2f) else Color(0xFF1E293B))
                            .border(1.dp, if (isSelected) color else Color.Transparent, RoundedCornerShape(8.dp))
                            .clickable { currentMode = mode }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) color else TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Web Context Extractor Shortcut Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1E293B))
                    .clickable {
                        // Extract selected text or main question text from WebView via JS
                        webView?.evaluateJavascript(
                            """
                            (function() {
                                var sel = window.getSelection().toString().trim();
                                if (sel.length > 0) return sel;
                                var h1 = document.querySelector('h1, h2, .question, .de-bai, article');
                                return h1 ? h1.innerText.trim() : document.title;
                            })();
                            """.trimIndent()
                        ) { result ->
                            val cleanText = result
                                ?.removePrefix("\"")
                                ?.removeSuffix("\"")
                                ?.replace("\\n", "\n")
                                ?.replace("\\\"", "\"")
                                ?.trim()
                            if (!cleanText.isNullOrBlank() && cleanText != "null") {
                                onPromptChanged(cleanText)
                                Toast.makeText(context, "Đã trích xuất nội dung từ trang web!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Không tìm thấy nội dung bôi đen trên trang.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.FindInPage,
                    contentDescription = null,
                    tint = CyberCyan,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Trích xuất câu hỏi/đề bài đang bôi đen trên trang web",
                    fontSize = 12.sp,
                    color = CyberCyan,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Question Input Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF1E293B))
                    .border(1.dp, Color(0xFF334155), RoundedCornerShape(10.dp))
                    .padding(10.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = when (currentMode) {
                                "SOLVE" -> "Đề bài / Câu hỏi cần giải:"
                                "SUMMARIZE" -> "Yêu cầu tóm tắt:"
                                "EXPLAIN" -> "Khái niệm cần giải thích:"
                                else -> "Nội dung tìm kiếm:"
                            },
                            fontSize = 12.sp,
                            color = TextSecondary,
                            fontWeight = FontWeight.Medium
                        )

                        Row {
                            // Paste button
                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clipText = clipboard.primaryClip?.getItemAt(0)?.text?.toString()
                                    if (!clipText.isNullOrBlank()) {
                                        onPromptChanged(clipText)
                                    }
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.ContentPaste, "Dán", tint = TextSecondary, modifier = Modifier.size(14.dp))
                            }
                            // Clear button
                            if (prompt.isNotEmpty()) {
                                Spacer(modifier = Modifier.width(4.dp))
                                IconButton(
                                    onClick = { onPromptChanged("") },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.Close, "Xóa", tint = TextSecondary, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    BasicTextField(
                        value = prompt,
                        onValueChange = onPromptChanged,
                        textStyle = TextStyle(
                            color = TextPrimary,
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        ),
                        cursorBrush = SolidColor(CyberCyan),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(70.dp)
                            .testTag("gemini_prompt_input")
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Solve / Submit Button
            Button(
                onClick = { onSolve(context, currentMode) },
                enabled = !isLoading && prompt.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CyberCyan,
                    disabledContainerColor = Color(0xFF334155)
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("gemini_solve_button")
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.Black,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Gemini đang suy nghĩ & giải...", color = Color.Black, fontWeight = FontWeight.Bold)
                } else {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when (currentMode) {
                            "SOLVE" -> "✨ Giải Bài Tập Ngay"
                            "SUMMARIZE" -> "✨ Tóm Tắt Ngay"
                            "EXPLAIN" -> "✨ Giải Thích Ngay"
                            else -> "✨ Tìm Kiếm Ngay"
                        },
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Error notice
            AnimatedVisibility(visible = !errorMessage.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(CyberRed.copy(alpha = 0.15f))
                        .border(1.dp, CyberRed, RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = errorMessage ?: "",
                        fontSize = 12.sp,
                        color = CyberRed,
                        lineHeight = 16.sp
                    )
                }
            }

            // Solution / Output Result Area
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF030712))
                    .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(10.dp))
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Kết quả từ Gemini:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberCyan
                    )

                    if (solution.isNotBlank()) {
                        Row {
                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Lời giải", solution))
                                    Toast.makeText(context, "Đã sao chép lời giải vào clipboard!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, "Sao chép", tint = CyberCyan, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                HorizontalDivider(color = Color(0xFF1E293B), thickness = 0.5.dp, modifier = Modifier.padding(vertical = 6.dp))

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    if (solution.isBlank()) {
                        Text(
                            text = "Kết quả giải bài tập, đáp án hoặc thông tin tìm kiếm từ Gemini sẽ hiển thị chi tiết tại đây...",
                            fontSize = 13.sp,
                            color = TextMuted,
                            lineHeight = 18.sp
                        )
                    } else {
                        Text(
                            text = solution,
                            fontSize = 14.sp,
                            color = Color(0xFFF1F5F9),
                            lineHeight = 22.sp,
                            fontFamily = FontFamily.SansSerif
                        )
                    }
                }
            }
        }
    }

    // Custom API Key Dialog
    if (showApiKeyDialog) {
        AlertDialog(
            onDismissRequest = { showApiKeyDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Key, null, tint = CyberCyan)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cấu hình Gemini API Key", fontSize = 16.sp, color = TextPrimary)
                }
            },
            text = {
                Column {
                    Text(
                        text = "Nhập API Key Gemini của bạn để giải bài tập và tìm kiếm không giới hạn:",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = apiKeyInput,
                        onValueChange = { apiKeyInput = it },
                        placeholder = { Text("Dán API Key (AIzaSy...)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = Color(0xFF475569)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        GeminiSolverService.saveCustomApiKey(context, apiKeyInput)
                        Toast.makeText(context, "Đã lưu API Key thành công!", Toast.LENGTH_SHORT).show()
                        showApiKeyDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan)
                ) {
                    Text("Lưu", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showApiKeyDialog = false }) {
                    Text("Hủy", color = TextSecondary)
                }
            },
            containerColor = Color(0xFF1E293B)
        )
    }
}

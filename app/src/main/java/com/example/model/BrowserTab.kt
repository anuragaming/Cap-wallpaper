package com.example.model

import java.util.UUID

data class BrowserTab(
    val id: String = UUID.randomUUID().toString(),
    val url: String = "https://anhdev.io.vn",
    val title: String = "Nhật Anh DEV",
    val isIncognito: Boolean = false,
    val lastAccessed: Long = System.currentTimeMillis()
)

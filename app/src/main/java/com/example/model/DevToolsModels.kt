package com.example.model

data class ConsoleLogItem(
    val timestamp: Long = System.currentTimeMillis(),
    val level: String = "LOG", // "LOG", "INFO", "WARN", "ERROR", "RESULT"
    val message: String,
    val source: String = ""
)

data class NetworkRequestItem(
    val timestamp: Long = System.currentTimeMillis(),
    val url: String,
    val method: String = "GET",
    val type: String = "DOCUMENT"
)

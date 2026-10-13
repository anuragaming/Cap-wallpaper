package com.example.model

data class InterceptionLog(
    val id: String,
    val timestamp: Long = System.currentTimeMillis(),
    val eventType: String,
    val domain: String,
    val actionTaken: String,
    val details: String
)

package com.example.data.repository

import com.example.data.local.HistoryDao
import com.example.data.local.HistoryEntry
import kotlinx.coroutines.flow.Flow

class HistoryRepository(private val historyDao: HistoryDao) {
    val allHistory: Flow<List<HistoryEntry>> = historyDao.getAllHistory()

    fun search(query: String): Flow<List<HistoryEntry>> = historyDao.searchHistory(query)

    suspend fun addHistory(url: String, title: String) {
        val cleanUrl = url.trim()
        if (cleanUrl.isBlank() || cleanUrl.startsWith("about:") || cleanUrl.startsWith("chrome:") || cleanUrl.startsWith("data:")) {
            return
        }
        val cleanTitle = title.trim().ifBlank { cleanUrl }
        historyDao.insert(
            HistoryEntry(
                url = cleanUrl,
                title = cleanTitle,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun delete(entry: HistoryEntry) {
        historyDao.delete(entry)
    }

    suspend fun deleteById(id: Long) {
        historyDao.deleteById(id)
    }

    suspend fun clearAll() {
        historyDao.clearAll()
    }
}

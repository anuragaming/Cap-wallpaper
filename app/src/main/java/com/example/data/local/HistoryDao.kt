package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    @Query("SELECT * FROM web_history ORDER BY timestamp DESC LIMIT 300")
    fun getAllHistory(): Flow<List<HistoryEntry>>

    @Query("SELECT * FROM web_history WHERE title LIKE '%' || :query || '%' OR url LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchHistory(query: String): Flow<List<HistoryEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: HistoryEntry): Long

    @Delete
    suspend fun delete(entry: HistoryEntry)

    @Query("DELETE FROM web_history WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM web_history")
    suspend fun clearAll()
}

package com.example.pegasus.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.pegasus.domain.AccessLog
import kotlinx.coroutines.flow.Flow

/**
 * Sprint 03: DAO for the application-access audit log.
 */
@Dao
interface AccessLogDao {

    @Insert
    suspend fun insert(log: AccessLog): Long

    @Query("SELECT * FROM access_logs WHERE userId = :userId ORDER BY timestamp DESC")
    fun observeForUser(userId: String): Flow<List<AccessLog>>

    @Query("SELECT * FROM access_logs ORDER BY timestamp DESC")
    suspend fun getAll(): List<AccessLog>

    @Query("SELECT COUNT(*) FROM access_logs WHERE userId = :userId")
    suspend fun countForUser(userId: String): Int
}

package com.example.pegasus.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.pegasus.domain.Trip
import kotlinx.coroutines.flow.Flow

/**
 * Sprint 03: DAO for Trip CRUD.
 * All read queries are scoped by userId so each account only sees its own trips.
 */
@Dao
interface TripDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(trip: Trip)

    @Update
    suspend fun update(trip: Trip)

    @Delete
    suspend fun delete(trip: Trip)

    @Query("DELETE FROM trips WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM trips WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): Trip?

    @Query("SELECT * FROM trips WHERE userId = :userId ORDER BY startDate ASC")
    suspend fun getAllForUser(userId: String): List<Trip>

    @Query("SELECT * FROM trips WHERE userId = :userId ORDER BY startDate ASC")
    fun observeAllForUser(userId: String): Flow<List<Trip>>

    @Query("SELECT EXISTS(SELECT 1 FROM trips WHERE userId = :userId AND title = :title AND id != :excludeId)")
    suspend fun isTitleTakenByOther(userId: String, title: String, excludeId: String): Boolean
}

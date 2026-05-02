package com.example.pegasus.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.pegasus.domain.Activity
import kotlinx.coroutines.flow.Flow

/**
 * Sprint 03: DAO for Activity CRUD inside a Trip.
 */
@Dao
interface ActivityDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(activity: Activity)

    @Update
    suspend fun update(activity: Activity)

    @Query("DELETE FROM activities WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM activities WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): Activity?

    @Query("SELECT * FROM activities WHERE tripId = :tripId ORDER BY date ASC, time ASC")
    suspend fun getByTripId(tripId: String): List<Activity>

    @Query("SELECT * FROM activities WHERE tripId = :tripId ORDER BY date ASC, time ASC")
    fun observeByTripId(tripId: String): Flow<List<Activity>>
}

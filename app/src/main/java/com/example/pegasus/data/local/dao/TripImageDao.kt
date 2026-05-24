package com.example.pegasus.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.pegasus.domain.TripImage
import kotlinx.coroutines.flow.Flow

@Dao
interface TripImageDao {

    @Query("SELECT * FROM trip_images WHERE tripId = :tripId ORDER BY addedAt DESC")
    fun observeForTrip(tripId: String): Flow<List<TripImage>>

    @Query("SELECT * FROM trip_images WHERE tripId = :tripId ORDER BY addedAt DESC")
    suspend fun listForTrip(tripId: String): List<TripImage>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(image: TripImage)

    @Query("DELETE FROM trip_images WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM trip_images WHERE tripId = :tripId")
    suspend fun deleteForTrip(tripId: String)
}

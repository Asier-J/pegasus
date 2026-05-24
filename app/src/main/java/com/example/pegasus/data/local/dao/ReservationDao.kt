package com.example.pegasus.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.pegasus.domain.Reservation
import kotlinx.coroutines.flow.Flow

@Dao
interface ReservationDao {

    @Query("SELECT * FROM reservations ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<Reservation>>

    @Query("SELECT * FROM reservations WHERE tripId = :tripId ORDER BY createdAt DESC")
    fun observeForTrip(tripId: String): Flow<List<Reservation>>

    @Query("SELECT * FROM reservations WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): Reservation?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reservation: Reservation)

    @Query("DELETE FROM reservations WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM reservations WHERE tripId = :tripId")
    suspend fun deleteForTrip(tripId: String)
}

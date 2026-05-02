package com.example.pegasus.domain

import kotlinx.coroutines.flow.Flow

// ─── TripRepository ───────────────────────────────────────────────────────────
// Sprint 03: now backed by Room. All operations are suspend and scoped to a userId
// so different accounts only see their own trips.
interface TripRepository {
    fun observeTrips(userId: String): Flow<List<Trip>>
    suspend fun getTrips(userId: String): List<Trip>
    suspend fun getTripById(id: String): Trip?
    suspend fun addTrip(trip: Trip)
    suspend fun editTrip(trip: Trip)
    suspend fun deleteTrip(id: String)
    suspend fun isTitleTakenByOther(userId: String, title: String, excludeId: String): Boolean
}

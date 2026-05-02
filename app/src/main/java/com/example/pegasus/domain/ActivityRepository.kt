package com.example.pegasus.domain

import kotlinx.coroutines.flow.Flow

// ─── ActivityRepository ───────────────────────────────────────────────────────
// Sprint 03: Room-backed. Activities live inside a Trip; deleting a Trip cascades.
interface ActivityRepository {
    fun observeActivities(tripId: String): Flow<List<Activity>>
    suspend fun getActivitiesByTripId(tripId: String): List<Activity>
    suspend fun getActivityById(id: String): Activity?
    suspend fun addActivity(activity: Activity)
    suspend fun updateActivity(activity: Activity)
    suspend fun deleteActivity(id: String)
}

package com.example.pegasus.domain

// ─── ActivityRepository ───────────────────────────────────────────────────────
// Interface defining CRUD operations for Activity entities.
// Implementations: ActivityRepositoryImpl (uses FakeTripDataSource).
interface ActivityRepository {
    fun getActivitiesByTripId(tripId: String): List<Activity>
    fun getActivityById(id: String): Activity?
    fun addActivity(activity: Activity)
    fun updateActivity(activity: Activity)
    fun deleteActivity(id: String)
}

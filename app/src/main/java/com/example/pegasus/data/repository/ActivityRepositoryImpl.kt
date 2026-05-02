package com.example.pegasus.data.repository

import android.util.Log
import com.example.pegasus.data.local.dao.ActivityDao
import com.example.pegasus.domain.Activity
import com.example.pegasus.domain.ActivityRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

// ─── ActivityRepositoryImpl ────────────────────────────────────────────────────
// Sprint 03: Room-backed implementation.
@Singleton
class ActivityRepositoryImpl @Inject constructor(
    private val activityDao: ActivityDao
) : ActivityRepository {

    override fun observeActivities(tripId: String): Flow<List<Activity>> =
        activityDao.observeByTripId(tripId)

    override suspend fun getActivitiesByTripId(tripId: String): List<Activity> =
        activityDao.getByTripId(tripId).also {
            Log.d(TAG, "getActivitiesByTripId($tripId): ${it.size} activities")
        }

    override suspend fun getActivityById(id: String): Activity? = activityDao.getById(id)

    override suspend fun addActivity(activity: Activity) {
        activityDao.insert(activity)
        Log.i(TAG, "addActivity: '${activity.title}' on trip ${activity.tripId}")
    }

    override suspend fun updateActivity(activity: Activity) {
        activityDao.update(activity)
        Log.i(TAG, "updateActivity: '${activity.title}' (id=${activity.id})")
    }

    override suspend fun deleteActivity(id: String) {
        activityDao.deleteById(id)
        Log.i(TAG, "deleteActivity: id=$id")
    }

    private companion object {
        const val TAG = "ActivityRepository"
    }
}

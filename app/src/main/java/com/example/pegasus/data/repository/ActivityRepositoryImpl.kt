package com.example.pegasus.data.repository

import com.example.pegasus.data.fakeDB.FakeTripDataSource
import com.example.pegasus.domain.Activity
import com.example.pegasus.domain.ActivityRepository

// ─── ActivityRepositoryImpl ────────────────────────────────────────────────────
// Concrete implementation of ActivityRepository backed by FakeTripDataSource.
// Architecture: UI → ViewModel → ActivityRepository → ActivityRepositoryImpl → FakeTripDataSource
class ActivityRepositoryImpl : ActivityRepository {

    override fun getActivitiesByTripId(tripId: String): List<Activity> =
        FakeTripDataSource.getActivitiesByTripId(tripId)

    override fun getActivityById(id: String): Activity? =
        FakeTripDataSource.getActivityById(id)

    override fun addActivity(activity: Activity) =
        FakeTripDataSource.addActivity(activity)

    override fun updateActivity(activity: Activity) =
        FakeTripDataSource.updateActivity(activity)

    override fun deleteActivity(id: String) =
        FakeTripDataSource.deleteActivity(id)
}

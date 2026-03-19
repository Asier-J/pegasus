package com.example.pegasus.data.fakeDB

import android.util.Log
import com.example.pegasus.domain.Activity
import com.example.pegasus.domain.Trip
import java.time.LocalDate
import java.time.LocalTime

// ─── FakeTripDataSource ───────────────────────────────────────────────────────
// InMemory singleton data source for trips and activities.
// Data exists only while the application is running (no persistence).
// Sprint 02: Preloaded with fake dataset to simplify testing.
object FakeTripDataSource {

    private const val TAG = "FakeTripDataSource"

    // ── Trip 1 fake id (used to link activities) ───────────────────────────────
    private const val TRIP_1_ID = "trip-001"
    private const val TRIP_2_ID = "trip-002"
    private const val TRIP_3_ID = "trip-003"

    // ── InMemory collections ───────────────────────────────────────────────────
    private val trips: MutableList<Trip> = mutableListOf(
        Trip(
            id          = TRIP_1_ID,
            title       = "Paris Getaway",
            startDate   = "10/06/2026",
            endDate     = "17/06/2026",
            description = "A romantic week in the City of Light."
        ),
        Trip(
            id          = TRIP_2_ID,
            title       = "Tokyo Adventure",
            startDate   = "01/08/2026",
            endDate     = "15/08/2026",
            description = "Exploring temples, food, and technology."
        ),
        Trip(
            id          = TRIP_3_ID,
            title       = "Barcelona Weekend",
            startDate   = "05/04/2026",
            endDate     = "07/04/2026",
            description = "Art, tapas and the Mediterranean coast."
        )
    )

    private val activities: MutableList<Activity> = mutableListOf(
        Activity(
            tripId      = TRIP_1_ID,
            title       = "Eiffel Tower visit",
            description = "Sunset visit to the tower.",
            date        = LocalDate.of(2026, 6, 11),
            time        = LocalTime.of(18, 0)
        ),
        Activity(
            tripId      = TRIP_1_ID,
            title       = "Louvre Museum",
            description = "Morning at the Louvre.",
            date        = LocalDate.of(2026, 6, 12),
            time        = LocalTime.of(9, 30)
        ),
        Activity(
            tripId      = TRIP_2_ID,
            title       = "Senso-ji Temple",
            description = "Traditional temple in Asakusa.",
            date        = LocalDate.of(2026, 8, 2),
            time        = LocalTime.of(8, 0)
        )
    )

    // ── Trip CRUD ──────────────────────────────────────────────────────────────

    fun getTrips(): List<Trip> {
        Log.d(TAG, "getTrips: returning ${trips.size} trips")
        return trips.toList()
    }

    fun getTripById(id: String): Trip? {
        val trip = trips.find { it.id == id }
        Log.d(TAG, "getTripById($id): ${if (trip != null) "found" else "not found"}")
        return trip
    }

    fun addTrip(trip: Trip) {
        trips.add(trip)
        Log.i(TAG, "addTrip: added '${trip.title}' (id=${trip.id})")
    }

    fun editTrip(trip: Trip) {
        val index = trips.indexOfFirst { it.id == trip.id }
        if (index >= 0) {
            trips[index] = trip
            Log.i(TAG, "editTrip: updated '${trip.title}' (id=${trip.id})")
        } else {
            Log.e(TAG, "editTrip: trip with id=${trip.id} not found")
        }
    }

    fun deleteTrip(id: String) {
        val removed = trips.removeIf { it.id == id }
        if (removed) {
            // Also delete all activities for this trip
            activities.removeIf { it.tripId == id }
            Log.i(TAG, "deleteTrip: removed trip id=$id and its activities")
        } else {
            Log.e(TAG, "deleteTrip: trip id=$id not found")
        }
    }

    // ── Activity CRUD ──────────────────────────────────────────────────────────

    fun getActivitiesByTripId(tripId: String): List<Activity> {
        val result = activities.filter { it.tripId == tripId }
        Log.d(TAG, "getActivitiesByTripId($tripId): ${result.size} activities")
        return result
    }

    fun getActivityById(id: String): Activity? {
        val activity = activities.find { it.id == id }
        Log.d(TAG, "getActivityById($id): ${if (activity != null) "found" else "not found"}")
        return activity
    }

    fun addActivity(activity: Activity) {
        activities.add(activity)
        Log.i(TAG, "addActivity: added '${activity.title}' to trip ${activity.tripId}")
    }

    fun updateActivity(activity: Activity) {
        val index = activities.indexOfFirst { it.id == activity.id }
        if (index >= 0) {
            activities[index] = activity
            Log.i(TAG, "updateActivity: updated '${activity.title}' (id=${activity.id})")
        } else {
            Log.e(TAG, "updateActivity: activity id=${activity.id} not found")
        }
    }

    fun deleteActivity(id: String) {
        val removed = activities.removeIf { it.id == id }
        if (removed) {
            Log.i(TAG, "deleteActivity: removed activity id=$id")
        } else {
            Log.e(TAG, "deleteActivity: activity id=$id not found")
        }
    }

    // ── Test helper ───────────────────────────────────────────────────────────
    // Clears all data — used only in unit tests.
    fun clearAll() {
        trips.clear()
        activities.clear()
        Log.d(TAG, "clearAll: data source reset")
    }
}

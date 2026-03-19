package com.example.pegasus

import com.example.pegasus.data.fakeDB.FakeTripDataSource
import com.example.pegasus.data.repository.ActivityRepositoryImpl
import com.example.pegasus.data.repository.TripRepositoryImpl
import com.example.pegasus.domain.Activity
import com.example.pegasus.domain.Trip
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

/**
 * Unit tests for Activity CRUD operations.
 * Tests cover: addActivity, updateActivity, deleteActivity, getActivitiesByTripId.
 * Each test uses a clean FakeTripDataSource and a pre-created trip.
 */
class ActivityCrudTest {

    private lateinit var activityRepository: ActivityRepositoryImpl
    private lateinit var tripRepository: TripRepositoryImpl

    private val testTripId = "test-trip-001"

    @Before
    fun setUp() {
        FakeTripDataSource.clearAll()
        tripRepository     = TripRepositoryImpl()
        activityRepository = ActivityRepositoryImpl()

        // Insert a trip to associate activities with
        tripRepository.addTrip(
            Trip(
                id          = testTripId,
                title       = "Test Trip",
                startDate   = "01/06/2026",
                endDate     = "30/06/2026",
                description = "Trip for testing"
            )
        )
    }

    // ── addActivity ────────────────────────────────────────────────────────────

    @Test
    fun addActivity_addsToList() {
        val activity = Activity(
            tripId      = testTripId,
            title       = "Museum visit",
            description = "Local museum",
            date        = LocalDate.of(2026, 6, 10),
            time        = LocalTime.of(10, 0)
        )
        activityRepository.addActivity(activity)
        val result = activityRepository.getActivitiesByTripId(testTripId)
        assertEquals(1, result.size)
        assertEquals("Museum visit", result[0].title)
    }

    @Test
    fun addActivity_multipleActivities_allPresent() {
        val a1 = Activity(tripId = testTripId, title = "A1", description = "", date = LocalDate.of(2026, 6, 5),  time = LocalTime.of(9, 0))
        val a2 = Activity(tripId = testTripId, title = "A2", description = "", date = LocalDate.of(2026, 6, 10), time = LocalTime.of(14, 0))
        activityRepository.addActivity(a1)
        activityRepository.addActivity(a2)
        assertEquals(2, activityRepository.getActivitiesByTripId(testTripId).size)
    }

    // ── getActivitiesByTripId ──────────────────────────────────────────────────

    @Test
    fun getActivitiesByTripId_emptyForNewTrip() {
        assertTrue(activityRepository.getActivitiesByTripId(testTripId).isEmpty())
    }

    @Test
    fun getActivitiesByTripId_onlyReturnsActivitiesForTargetTrip() {
        val otherTripId = "other-trip"
        tripRepository.addTrip(
            Trip(id = otherTripId, title = "Other", startDate = "01/07/2026", endDate = "10/07/2026", description = "Other")
        )
        val a1 = Activity(tripId = testTripId,  title = "A1", description = "", date = LocalDate.of(2026, 6, 5), time = LocalTime.of(9, 0))
        val a2 = Activity(tripId = otherTripId, title = "A2", description = "", date = LocalDate.of(2026, 7, 2), time = LocalTime.of(9, 0))
        activityRepository.addActivity(a1)
        activityRepository.addActivity(a2)

        val result = activityRepository.getActivitiesByTripId(testTripId)
        assertEquals(1, result.size)
        assertEquals("A1", result[0].title)
    }

    // ── getActivityById ────────────────────────────────────────────────────────

    @Test
    fun getActivityById_returnsCorrectActivity() {
        val activity = Activity(
            id          = "act-id-1",
            tripId      = testTripId,
            title       = "Museum",
            description = "",
            date        = LocalDate.of(2026, 6, 10),
            time        = LocalTime.of(10, 0)
        )
        activityRepository.addActivity(activity)
        val found = activityRepository.getActivityById("act-id-1")
        assertNotNull(found)
        assertEquals("Museum", found!!.title)
    }

    @Test
    fun getActivityById_returnsNullForUnknownId() {
        assertNull(activityRepository.getActivityById("nonexistent"))
    }

    // ── updateActivity ─────────────────────────────────────────────────────────

    @Test
    fun updateActivity_updatesCorrectly() {
        val activity = Activity(
            id          = "upd-id",
            tripId      = testTripId,
            title       = "Old title",
            description = "Old desc",
            date        = LocalDate.of(2026, 6, 10),
            time        = LocalTime.of(10, 0)
        )
        activityRepository.addActivity(activity)

        val updated = activity.copy(title = "New title", time = LocalTime.of(14, 30))
        activityRepository.updateActivity(updated)

        val result = activityRepository.getActivityById("upd-id")
        assertNotNull(result)
        assertEquals("New title", result!!.title)
        assertEquals(LocalTime.of(14, 30), result.time)
    }

    @Test
    fun updateActivity_doesNotChangeTotalCount() {
        val activity = Activity(
            id          = "cnt-id",
            tripId      = testTripId,
            title       = "Title",
            description = "",
            date        = LocalDate.of(2026, 6, 10),
            time        = LocalTime.of(10, 0)
        )
        activityRepository.addActivity(activity)
        activityRepository.updateActivity(activity.copy(title = "Updated"))
        assertEquals(1, activityRepository.getActivitiesByTripId(testTripId).size)
    }

    // ── deleteActivity ─────────────────────────────────────────────────────────

    @Test
    fun deleteActivity_removesFromList() {
        val activity = Activity(
            id          = "del-act-id",
            tripId      = testTripId,
            title       = "To delete",
            description = "",
            date        = LocalDate.of(2026, 6, 10),
            time        = LocalTime.of(10, 0)
        )
        activityRepository.addActivity(activity)
        assertEquals(1, activityRepository.getActivitiesByTripId(testTripId).size)

        activityRepository.deleteActivity("del-act-id")
        assertEquals(0, activityRepository.getActivitiesByTripId(testTripId).size)
    }

    @Test
    fun deleteActivity_unknownId_doesNotCrash() {
        activityRepository.deleteActivity("does-not-exist")
        assertEquals(0, activityRepository.getActivitiesByTripId(testTripId).size)
    }

    @Test
    fun deleteActivity_onlyDeletesTargetActivity() {
        val a1 = Activity(id = "a1", tripId = testTripId, title = "A1", description = "", date = LocalDate.of(2026, 6, 5),  time = LocalTime.of(9, 0))
        val a2 = Activity(id = "a2", tripId = testTripId, title = "A2", description = "", date = LocalDate.of(2026, 6, 10), time = LocalTime.of(14, 0))
        activityRepository.addActivity(a1)
        activityRepository.addActivity(a2)

        activityRepository.deleteActivity("a1")

        val result = activityRepository.getActivitiesByTripId(testTripId)
        assertEquals(1, result.size)
        assertEquals("A2", result[0].title)
    }
}

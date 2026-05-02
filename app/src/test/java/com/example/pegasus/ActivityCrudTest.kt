package com.example.pegasus

import android.os.Build
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.pegasus.data.local.AppDatabase
import com.example.pegasus.data.repository.ActivityRepositoryImpl
import com.example.pegasus.data.repository.TripRepositoryImpl
import com.example.pegasus.domain.Activity
import com.example.pegasus.domain.Trip
import com.example.pegasus.domain.User
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate
import java.time.LocalTime

/**
 * Sprint 03 — T5.1: Unit tests for the Room-backed ActivityRepository.
 *
 * Coverage:
 *  - CRUD: add / get / list / update / delete / unknown id
 *  - Ordering: getByTripId sorts by (date asc, time asc)
 *  - Reactive Flow: observeByTripId emits on every Room write (T1.6)
 *  - Cascade: deleting parent Trip cascades to its activities
 *  - LocalDate/LocalTime are correctly persisted via Converters
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P])
class ActivityCrudTest {

    private lateinit var db: AppDatabase
    private lateinit var activityRepository: ActivityRepositoryImpl
    private lateinit var tripRepository: TripRepositoryImpl

    private val userId = "uid-1"
    private val testTripId = "test-trip-001"

    @Before
    fun setUp() = runTest {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()

        tripRepository     = TripRepositoryImpl(db.tripDao())
        activityRepository = ActivityRepositoryImpl(db.activityDao())

        db.userDao().insert(User(uid = userId, email = "u@p.com", username = "user1"))
        tripRepository.addTrip(
            Trip(
                id = testTripId, userId = userId, title = "Test Trip",
                startDate = "01/06/2026", endDate = "30/06/2026", description = "T"
            )
        )
    }

    @After fun tearDown() { db.close() }

    @Test
    fun addActivity_addsToList() = runTest {
        activityRepository.addActivity(
            Activity(tripId = testTripId, title = "Museum", description = "",
                date = LocalDate.of(2026, 6, 10), time = LocalTime.of(10, 0))
        )
        val result = activityRepository.getActivitiesByTripId(testTripId)
        assertEquals(1, result.size)
        assertEquals("Museum", result[0].title)
    }

    @Test
    fun addActivity_persistsLocalDateAndLocalTime() = runTest {
        activityRepository.addActivity(
            Activity(id = "a-1", tripId = testTripId, title = "Concert",
                description = "Stadium",
                date = LocalDate.of(2026, 6, 15), time = LocalTime.of(21, 30))
        )
        val stored = activityRepository.getActivityById("a-1")
        assertEquals(LocalDate.of(2026, 6, 15), stored?.date)
        assertEquals(LocalTime.of(21, 30), stored?.time)
    }

    @Test
    fun addActivity_persistsIntegerAndDatetimeFields() = runTest {
        activityRepository.addActivity(
            Activity(id = "a-int", tripId = testTripId, title = "T",
                description = "", date = LocalDate.of(2026, 6, 10),
                time = LocalTime.of(10, 0), durationMinutes = 90)
        )
        val stored = activityRepository.getActivityById("a-int")
        assertEquals(90, stored?.durationMinutes)
        assertTrue((stored?.createdAt ?: 0) > 0)
    }

    @Test
    fun addActivity_multipleActivities_allPresent() = runTest {
        activityRepository.addActivity(Activity(tripId = testTripId, title = "A1", description = "",
            date = LocalDate.of(2026, 6, 5), time = LocalTime.of(9, 0)))
        activityRepository.addActivity(Activity(tripId = testTripId, title = "A2", description = "",
            date = LocalDate.of(2026, 6, 10), time = LocalTime.of(14, 0)))
        assertEquals(2, activityRepository.getActivitiesByTripId(testTripId).size)
    }

    @Test
    fun getActivitiesByTripId_emptyForNewTrip() = runTest {
        assertTrue(activityRepository.getActivitiesByTripId(testTripId).isEmpty())
    }

    @Test
    fun getActivitiesByTripId_unknownTrip_returnsEmpty() = runTest {
        // No crash for trips that don't exist.
        assertTrue(activityRepository.getActivitiesByTripId("does-not-exist").isEmpty())
    }

    @Test
    fun getActivitiesByTripId_orderedByDateThenTimeAsc() = runTest {
        // Insert in deliberately scrambled order; DAO must return them sorted.
        activityRepository.addActivity(Activity(id = "a-late",  tripId = testTripId, title = "Late",
            description = "", date = LocalDate.of(2026, 6, 20), time = LocalTime.of(8, 0)))
        activityRepository.addActivity(Activity(id = "a-mid-2", tripId = testTripId, title = "MidLater",
            description = "", date = LocalDate.of(2026, 6, 10), time = LocalTime.of(18, 0)))
        activityRepository.addActivity(Activity(id = "a-early", tripId = testTripId, title = "Early",
            description = "", date = LocalDate.of(2026, 6, 5),  time = LocalTime.of(9, 0)))
        activityRepository.addActivity(Activity(id = "a-mid-1", tripId = testTripId, title = "MidEarly",
            description = "", date = LocalDate.of(2026, 6, 10), time = LocalTime.of(9, 0)))

        val result = activityRepository.getActivitiesByTripId(testTripId)
        assertEquals(listOf("Early", "MidEarly", "MidLater", "Late"), result.map { it.title })
    }

    @Test
    fun getActivitiesByTripId_onlyReturnsActivitiesForTargetTrip() = runTest {
        val otherTripId = "other-trip"
        tripRepository.addTrip(Trip(id = otherTripId, userId = userId, title = "Other",
            startDate = "01/07/2026", endDate = "10/07/2026", description = ""))
        activityRepository.addActivity(Activity(tripId = testTripId,  title = "A1", description = "",
            date = LocalDate.of(2026, 6, 5), time = LocalTime.of(9, 0)))
        activityRepository.addActivity(Activity(tripId = otherTripId, title = "A2", description = "",
            date = LocalDate.of(2026, 7, 2), time = LocalTime.of(9, 0)))

        val result = activityRepository.getActivitiesByTripId(testTripId)
        assertEquals(1, result.size)
        assertEquals("A1", result[0].title)
    }

    @Test
    fun getActivityById_returnsCorrectActivity() = runTest {
        activityRepository.addActivity(
            Activity(id = "act-id-1", tripId = testTripId, title = "Museum",
                description = "", date = LocalDate.of(2026, 6, 10), time = LocalTime.of(10, 0))
        )
        val found = activityRepository.getActivityById("act-id-1")
        assertNotNull(found)
        assertEquals("Museum", found!!.title)
    }

    @Test
    fun getActivityById_returnsNullForUnknownId() = runTest {
        assertNull(activityRepository.getActivityById("nope"))
    }

    @Test
    fun updateActivity_updatesCorrectly() = runTest {
        val activity = Activity(id = "u1", tripId = testTripId, title = "Old",
            description = "", date = LocalDate.of(2026, 6, 10), time = LocalTime.of(10, 0))
        activityRepository.addActivity(activity)
        activityRepository.updateActivity(activity.copy(title = "New", time = LocalTime.of(14, 30)))

        val result = activityRepository.getActivityById("u1")
        assertEquals("New", result!!.title)
        assertEquals(LocalTime.of(14, 30), result.time)
    }

    @Test
    fun updateActivity_changesDate() = runTest {
        val activity = Activity(id = "u-date", tripId = testTripId, title = "T",
            description = "", date = LocalDate.of(2026, 6, 10), time = LocalTime.of(10, 0))
        activityRepository.addActivity(activity)
        activityRepository.updateActivity(activity.copy(date = LocalDate.of(2026, 6, 25)))

        val result = activityRepository.getActivityById("u-date")
        assertEquals(LocalDate.of(2026, 6, 25), result?.date)
    }

    @Test
    fun deleteActivity_removesFromList() = runTest {
        activityRepository.addActivity(
            Activity(id = "d1", tripId = testTripId, title = "T", description = "",
                date = LocalDate.of(2026, 6, 10), time = LocalTime.of(10, 0))
        )
        assertEquals(1, activityRepository.getActivitiesByTripId(testTripId).size)
        activityRepository.deleteActivity("d1")
        assertEquals(0, activityRepository.getActivitiesByTripId(testTripId).size)
    }

    @Test
    fun deleteActivity_unknownId_isSilentNoOp() = runTest {
        // No exception on missing id; count stays at 0.
        activityRepository.deleteActivity("ghost")
        assertEquals(0, activityRepository.getActivitiesByTripId(testTripId).size)
    }

    @Test
    fun deleteTrip_cascadesToActivities() = runTest {
        activityRepository.addActivity(Activity(tripId = testTripId, title = "T", description = "",
            date = LocalDate.of(2026, 6, 10), time = LocalTime.of(10, 0)))
        assertEquals(1, activityRepository.getActivitiesByTripId(testTripId).size)
        // Removing the trip should cascade-delete its activities (FK ON DELETE CASCADE).
        tripRepository.deleteTrip(testTripId)
        assertEquals(0, activityRepository.getActivitiesByTripId(testTripId).size)
    }

    // ── Reactive Flow (T1.6) ──────────────────────────────────────────────────
    @Test
    fun observeByTripId_emitsOnInsert() = runTest {
        val initial = activityRepository.observeActivities(testTripId).first()
        assertTrue(initial.isEmpty())

        activityRepository.addActivity(Activity(tripId = testTripId, title = "Reactive",
            description = "", date = LocalDate.of(2026, 6, 10), time = LocalTime.of(12, 0)))

        val after = activityRepository.observeActivities(testTripId).first()
        assertEquals(1, after.size)
        assertEquals("Reactive", after[0].title)
    }
}

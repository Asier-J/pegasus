package com.example.pegasus

import com.example.pegasus.domain.Activity
import com.example.pegasus.domain.ActivityRepository
import com.example.pegasus.domain.Trip
import com.example.pegasus.domain.TripRepository
import com.example.pegasus.ui.viewmodels.ActivityViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.LocalDate
import java.time.LocalTime

/**
 * Sprint 03 — T5.1 / T5.2: Validation paths and CRUD behavior of ActivityViewModel.
 *
 * Coverage:
 *  - Validation: empty title / null date / null time / out-of-range date / unknown trip
 *  - Bounds of the trip date range (inclusive at both ends)
 *  - Happy paths call the activity repository with expected arguments
 *  - Trip lookup failure returns false without crashing
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ActivityViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var activityRepository: ActivityRepository
    private lateinit var tripRepository: TripRepository
    private lateinit var vm: ActivityViewModel

    private val tripId = "trip-1"
    private val parentTrip = Trip(
        id = tripId, userId = "uid", title = "Madrid",
        startDate = "01/06/2026", endDate = "10/06/2026", description = "T"
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        activityRepository = mock {
            onBlocking { observeActivities(any()) } doReturn flowOf(emptyList())
        }
        tripRepository = mock {
            onBlocking { getTripById(tripId) } doReturn parentTrip
        }
        vm = ActivityViewModel(activityRepository, tripRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ── Validation: required fields ───────────────────────────────────────────
    @Test
    fun addActivity_emptyTitle_emitsTitleEmptyError() = runTest(testDispatcher) {
        var captured: Boolean? = null
        vm.addActivity(tripId, "", "desc", LocalDate.of(2026, 6, 5), LocalTime.of(10, 0)) {
            captured = it
        }
        advanceUntilIdle()
        assertEquals(false, captured)
        assertEquals(ActivityViewModel.ERROR_TITLE_EMPTY, vm.errorMessage.value)
        verify(activityRepository, never()).addActivity(any())
    }

    @Test
    fun addActivity_nullDate_emitsDateEmptyError() = runTest(testDispatcher) {
        vm.addActivity(tripId, "T", "desc", null, LocalTime.of(10, 0))
        advanceUntilIdle()
        assertEquals(ActivityViewModel.ERROR_DATE_EMPTY, vm.errorMessage.value)
        verify(activityRepository, never()).addActivity(any())
    }

    @Test
    fun addActivity_nullTime_emitsTimeEmptyError() = runTest(testDispatcher) {
        vm.addActivity(tripId, "T", "desc", LocalDate.of(2026, 6, 5), null)
        advanceUntilIdle()
        assertEquals(ActivityViewModel.ERROR_TIME_EMPTY, vm.errorMessage.value)
        verify(activityRepository, never()).addActivity(any())
    }

    // ── Validation: date inside trip range (T5.2) ─────────────────────────────
    @Test
    fun addActivity_dateBeforeTripStart_emitsOutOfRangeError() = runTest(testDispatcher) {
        var captured: Boolean? = null
        vm.addActivity(
            tripId, "T", "desc",
            LocalDate.of(2026, 5, 31), LocalTime.of(10, 0)
        ) { captured = it }
        advanceUntilIdle()
        assertEquals(false, captured)
        assertEquals(ActivityViewModel.ERROR_DATE_OUT_OF_RANGE, vm.errorMessage.value)
    }

    @Test
    fun addActivity_dateAfterTripEnd_emitsOutOfRangeError() = runTest(testDispatcher) {
        vm.addActivity(
            tripId, "T", "desc",
            LocalDate.of(2026, 6, 11), LocalTime.of(10, 0)
        )
        advanceUntilIdle()
        assertEquals(ActivityViewModel.ERROR_DATE_OUT_OF_RANGE, vm.errorMessage.value)
    }

    @Test
    fun addActivity_dateExactlyAtTripStart_isAllowed() = runTest(testDispatcher) {
        var captured: Boolean? = null
        vm.addActivity(
            tripId, "Arrival", "land",
            LocalDate.of(2026, 6, 1), LocalTime.of(10, 0)
        ) { captured = it }
        advanceUntilIdle()
        assertEquals(true, captured)
        verify(activityRepository, times(1)).addActivity(any())
    }

    @Test
    fun addActivity_dateExactlyAtTripEnd_isAllowed() = runTest(testDispatcher) {
        var captured: Boolean? = null
        vm.addActivity(
            tripId, "Departure", "fly home",
            LocalDate.of(2026, 6, 10), LocalTime.of(18, 0)
        ) { captured = it }
        advanceUntilIdle()
        assertEquals(true, captured)
    }

    // ── Validation: trip lookup ──────────────────────────────────────────────
    @Test
    fun addActivity_unknownTrip_emitsOutOfRangeError() = runTest(testDispatcher) {
        whenever(tripRepository.getTripById("ghost")).thenReturn(null)
        var captured: Boolean? = null
        vm.addActivity(
            "ghost", "T", "",
            LocalDate.of(2026, 6, 5), LocalTime.of(10, 0)
        ) { captured = it }
        advanceUntilIdle()
        // When the parent trip is missing, the date-range check fails closed.
        assertEquals(false, captured)
        assertEquals(ActivityViewModel.ERROR_DATE_OUT_OF_RANGE, vm.errorMessage.value)
    }

    @Test
    fun addActivity_parentTripWithUnparseableDates_failsClosed() = runTest(testDispatcher) {
        whenever(tripRepository.getTripById(tripId)).thenReturn(
            parentTrip.copy(startDate = "not-a-date", endDate = "still-not")
        )
        vm.addActivity(
            tripId, "T", "",
            LocalDate.of(2026, 6, 5), LocalTime.of(10, 0)
        )
        advanceUntilIdle()
        // Defensive: bad parent dates → out-of-range error, no crash.
        assertEquals(ActivityViewModel.ERROR_DATE_OUT_OF_RANGE, vm.errorMessage.value)
    }

    // ── Happy paths ──────────────────────────────────────────────────────────
    @Test
    fun addActivity_validInput_callsRepositoryWithTrimmedFields() = runTest(testDispatcher) {
        vm.addActivity(
            tripId, "  Museum  ", "  visit  ",
            LocalDate.of(2026, 6, 5), LocalTime.of(10, 0)
        )
        advanceUntilIdle()
        argumentCaptor<Activity>().apply {
            verify(activityRepository).addActivity(capture())
            assertEquals("Museum", firstValue.title)
            assertEquals("visit",  firstValue.description)
            assertEquals(tripId,   firstValue.tripId)
            assertEquals(LocalDate.of(2026, 6, 5), firstValue.date)
            assertEquals(LocalTime.of(10, 0),       firstValue.time)
        }
    }

    @Test
    fun updateActivity_validInput_callsRepositoryWithSameId() = runTest(testDispatcher) {
        vm.updateActivity(
            id = "act-1", tripId = tripId, title = "Updated",
            description = "new desc",
            date = LocalDate.of(2026, 6, 5), time = LocalTime.of(11, 0)
        )
        advanceUntilIdle()
        argumentCaptor<Activity>().apply {
            verify(activityRepository).updateActivity(capture())
            assertEquals("act-1", firstValue.id)
            assertEquals("Updated", firstValue.title)
            assertEquals(tripId, firstValue.tripId)
        }
    }

    @Test
    fun updateActivity_validationStillApplies() = runTest(testDispatcher) {
        var captured: Boolean? = null
        vm.updateActivity(
            id = "act-1", tripId = tripId, title = "",
            description = "", date = LocalDate.of(2026, 6, 5),
            time = LocalTime.of(11, 0)
        ) { captured = it }
        advanceUntilIdle()
        assertEquals(false, captured)
        verify(activityRepository, never()).updateActivity(any())
    }

    @Test
    fun deleteActivity_callsRepository() = runTest(testDispatcher) {
        vm.deleteActivity("act-9", tripId)
        advanceUntilIdle()
        verify(activityRepository).deleteActivity("act-9")
    }

    @Test
    fun loadActivities_setsCurrentTripId_andDoesNotCrash() = runTest(testDispatcher) {
        vm.loadActivities(tripId)
        advanceUntilIdle()
        // No exception, list initially empty (mock returned emptyList).
        assertTrue(vm.activities.value.isEmpty())
    }

    // ── State helpers ─────────────────────────────────────────────────────────
    @Test
    fun clearError_emptiesErrorMessage() = runTest(testDispatcher) {
        vm.addActivity(tripId, "", "", null, null)
        advanceUntilIdle()
        assertNotNull(vm.errorMessage.value)
        vm.clearError()
        assertNull(vm.errorMessage.value)
    }
}

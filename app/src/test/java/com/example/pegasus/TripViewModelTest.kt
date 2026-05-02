package com.example.pegasus

import com.example.pegasus.domain.AuthRepository
import com.example.pegasus.domain.Trip
import com.example.pegasus.domain.TripRepository
import com.example.pegasus.ui.viewmodels.TripViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
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
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Sprint 03 — T5.1 / T5.2: Validation paths and CRUD behavior of TripViewModel.
 *
 * Coverage:
 *  - Validation rejects every required-field path with the right error message
 *  - Validation rejects end < start dates
 *  - Validation rejects unparseable dates
 *  - Duplicate-title check uses `excludeId` correctly (self-edit allowed)
 *  - addTrip/editTrip without an authenticated user returns ERROR_NO_USER
 *  - Happy paths call the repository with the expected arguments
 *  - clearError() empties the error stream
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TripViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var tripRepository: TripRepository
    private lateinit var authRepository: AuthRepository
    private lateinit var vm: TripViewModel

    private val uid = "uid-test"

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        tripRepository = mock {
            onBlocking { observeTrips(any()) } doReturn flowOf(emptyList())
            onBlocking { isTitleTakenByOther(any(), any(), any()) } doReturn false
        }
        authRepository = mock {
            on { observeCurrentUser() } doReturn flowOf(AuthRepository.AuthUser(uid, "e@x.com", true))
            on { currentUser() } doReturn AuthRepository.AuthUser(uid, "e@x.com", true)
        }
        vm = TripViewModel(tripRepository, authRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ── Validation: required fields ───────────────────────────────────────────
    @Test
    fun addTrip_emptyTitle_emitsTitleEmptyError() = runTest(testDispatcher) {
        var captured: Boolean? = null
        vm.addTrip("", "01/06/2026", "07/06/2026", "desc") { captured = it }
        advanceUntilIdle()
        assertEquals(false, captured)
        assertEquals(TripViewModel.ERROR_TITLE_EMPTY, vm.errorMessage.value)
        verify(tripRepository, never()).addTrip(any())
    }

    @Test
    fun addTrip_emptyStartDate_emitsStartEmptyError() = runTest(testDispatcher) {
        vm.addTrip("X", "", "07/06/2026", "desc")
        advanceUntilIdle()
        assertEquals(TripViewModel.ERROR_START_DATE_EMPTY, vm.errorMessage.value)
        verify(tripRepository, never()).addTrip(any())
    }

    @Test
    fun addTrip_emptyEndDate_emitsEndEmptyError() = runTest(testDispatcher) {
        vm.addTrip("X", "01/06/2026", "", "desc")
        advanceUntilIdle()
        assertEquals(TripViewModel.ERROR_END_DATE_EMPTY, vm.errorMessage.value)
    }

    @Test
    fun addTrip_emptyDescription_emitsDescriptionEmptyError() = runTest(testDispatcher) {
        vm.addTrip("X", "01/06/2026", "07/06/2026", "")
        advanceUntilIdle()
        assertEquals(TripViewModel.ERROR_DESCRIPTION_EMPTY, vm.errorMessage.value)
    }

    // ── Validation: date logic ────────────────────────────────────────────────
    @Test
    fun addTrip_endBeforeStart_emitsDateOrderError() = runTest(testDispatcher) {
        vm.addTrip("X", "10/06/2026", "01/06/2026", "desc")
        advanceUntilIdle()
        assertEquals(TripViewModel.ERROR_DATE_ORDER, vm.errorMessage.value)
        verify(tripRepository, never()).addTrip(any())
    }

    @Test
    fun addTrip_sameStartAndEndIsAllowed() = runTest(testDispatcher) {
        var captured: Boolean? = null
        vm.addTrip("Daytrip", "10/06/2026", "10/06/2026", "single-day") { captured = it }
        advanceUntilIdle()
        assertEquals(true, captured)
        verify(tripRepository, times(1)).addTrip(any())
    }

    @Test
    fun addTrip_unparseableDate_emitsDateOrderError() = runTest(testDispatcher) {
        // Defensive: when one of the dates can't be parsed, validation should fail
        // (returns false from isStartBeforeOrEqualEnd → reports DATE_ORDER) instead
        // of crashing the ViewModel.
        vm.addTrip("X", "not-a-date", "07/06/2026", "desc")
        advanceUntilIdle()
        assertEquals(TripViewModel.ERROR_DATE_ORDER, vm.errorMessage.value)
    }

    // ── Validation: title uniqueness (T5.2) ───────────────────────────────────
    @Test
    fun addTrip_duplicateTitle_emitsTitleDuplicateError() = runTest(testDispatcher) {
        whenever(tripRepository.isTitleTakenByOther(uid, "Paris", "")).thenReturn(true)
        var captured: Boolean? = null
        vm.addTrip("Paris", "01/06/2026", "07/06/2026", "desc") { captured = it }
        advanceUntilIdle()
        assertEquals(false, captured)
        assertEquals(TripViewModel.ERROR_TITLE_DUPLICATE, vm.errorMessage.value)
        verify(tripRepository, never()).addTrip(any())
    }

    @Test
    fun editTrip_keepingSameTitle_isAllowed() = runTest(testDispatcher) {
        // Self-edit must use excludeId = current trip id, so its own title doesn't
        // count as a collision.
        whenever(tripRepository.isTitleTakenByOther(uid, "Paris", "trip-1"))
            .thenReturn(false)
        var captured: Boolean? = null
        vm.editTrip("trip-1", "Paris", "01/06/2026", "07/06/2026", "desc") { captured = it }
        advanceUntilIdle()
        assertEquals(true, captured)
        verify(tripRepository, times(1)).editTrip(any())
    }

    @Test
    fun editTrip_titleTakenByDifferentTrip_isRejected() = runTest(testDispatcher) {
        whenever(tripRepository.isTitleTakenByOther(uid, "Paris", "trip-1"))
            .thenReturn(true)
        var captured: Boolean? = null
        vm.editTrip("trip-1", "Paris", "01/06/2026", "07/06/2026", "desc") { captured = it }
        advanceUntilIdle()
        assertEquals(false, captured)
        assertEquals(TripViewModel.ERROR_TITLE_DUPLICATE, vm.errorMessage.value)
        verify(tripRepository, never()).editTrip(any())
    }

    // ── Auth-required guard ──────────────────────────────────────────────────
    @Test
    fun addTrip_withoutLoggedUser_emitsNoUserError() = runTest(testDispatcher) {
        whenever(authRepository.currentUser()).thenReturn(null)
        var captured: Boolean? = null
        vm.addTrip("X", "01/06/2026", "07/06/2026", "desc") { captured = it }
        advanceUntilIdle()
        assertEquals(false, captured)
        assertEquals(TripViewModel.ERROR_NO_USER, vm.errorMessage.value)
        verify(tripRepository, never()).addTrip(any())
    }

    @Test
    fun editTrip_withoutLoggedUser_emitsNoUserError() = runTest(testDispatcher) {
        whenever(authRepository.currentUser()).thenReturn(null)
        vm.editTrip("trip-1", "X", "01/06/2026", "07/06/2026", "desc")
        advanceUntilIdle()
        assertEquals(TripViewModel.ERROR_NO_USER, vm.errorMessage.value)
        verify(tripRepository, never()).editTrip(any())
    }

    // ── Happy paths ──────────────────────────────────────────────────────────
    @Test
    fun addTrip_validInput_callsRepositoryAndReportsSuccess() = runTest(testDispatcher) {
        var captured: Boolean? = null
        vm.addTrip("Paris", "01/06/2026", "07/06/2026", "City of Light") { captured = it }
        advanceUntilIdle()

        assertEquals(true, captured)
        // Verify the trip persisted has the trimmed title and the auth uid as owner.
        org.mockito.kotlin.argumentCaptor<Trip>().apply {
            verify(tripRepository).addTrip(capture())
            val saved = firstValue
            assertEquals("Paris", saved.title)
            assertEquals(uid, saved.userId)
            assertEquals("City of Light", saved.description)
        }
    }

    @Test
    fun editTrip_validInput_callsRepositoryWithSameId() = runTest(testDispatcher) {
        vm.editTrip("trip-42", "Tokyo", "01/08/2026", "10/08/2026", "Adventure")
        advanceUntilIdle()
        org.mockito.kotlin.argumentCaptor<Trip>().apply {
            verify(tripRepository).editTrip(capture())
            assertEquals("trip-42", firstValue.id)
            assertEquals("Tokyo", firstValue.title)
            assertEquals(uid, firstValue.userId)
        }
    }

    @Test
    fun deleteTrip_callsRepository() = runTest(testDispatcher) {
        vm.deleteTrip("trip-9")
        advanceUntilIdle()
        verify(tripRepository).deleteTrip("trip-9")
    }

    // ── State helpers ─────────────────────────────────────────────────────────
    @Test
    fun clearError_emptiesErrorMessage() = runTest(testDispatcher) {
        vm.addTrip("", "01/06/2026", "07/06/2026", "desc") // forces an error
        advanceUntilIdle()
        assertNotNull(vm.errorMessage.value)
        vm.clearError()
        assertNull(vm.errorMessage.value)
    }

    @Test
    fun addTrip_trimsTitleAndDescription() = runTest(testDispatcher) {
        vm.addTrip("  Paris  ", "01/06/2026", "07/06/2026", "  desc  ")
        advanceUntilIdle()
        org.mockito.kotlin.argumentCaptor<Trip>().apply {
            verify(tripRepository).addTrip(capture())
            assertEquals("Paris", firstValue.title)
            assertEquals("desc",  firstValue.description)
        }
    }

    // ── T1.6: trips StateFlow reacts to auth-state changes ───────────────────
    @Test
    fun trips_emitsEmptyWhenLoggedOut_andRepoFlowWhenLoggedIn() = runTest(testDispatcher) {
        // Auth state: starts logged out, then logs in.
        val authFlow = MutableStateFlow<AuthRepository.AuthUser?>(null)
        val seededTrip = Trip(
            id = "t1", userId = uid, title = "OnlyMine",
            startDate = "01/06/2026", endDate = "07/06/2026", description = ""
        )
        val freshAuth: AuthRepository = mock {
            on { observeCurrentUser() } doReturn authFlow
            on { currentUser() } doReturn null
        }
        val freshRepo: TripRepository = mock {
            onBlocking { observeTrips(uid) } doReturn flowOf(listOf(seededTrip))
            onBlocking { observeTrips(any()) } doReturn flowOf(listOf(seededTrip))
            onBlocking { isTitleTakenByOther(any(), any(), any()) } doReturn false
        }
        val freshVm = TripViewModel(freshRepo, freshAuth)
        advanceUntilIdle()

        // Logged out → empty list (flatMapLatest hits the null branch).
        assertTrue(freshVm.trips.value.isEmpty())

        // Log in → flow switches to the repository's observeTrips(uid).
        authFlow.value = AuthRepository.AuthUser(uid, "e@x.com", true)
        advanceUntilIdle()
        assertEquals(1, freshVm.trips.value.size)
        assertEquals("OnlyMine", freshVm.trips.value[0].title)

        // Log out again → flow flips back to empty.
        authFlow.value = null
        advanceUntilIdle()
        assertTrue(freshVm.trips.value.isEmpty())
    }
}

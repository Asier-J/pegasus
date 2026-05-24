package com.example.pegasus

import com.example.pegasus.domain.AuthRepository
import com.example.pegasus.domain.HotelRepository
import com.example.pegasus.domain.Reservation
import com.example.pegasus.domain.ReservationRepository
import com.example.pegasus.domain.Trip
import com.example.pegasus.domain.TripImageRepository
import com.example.pegasus.domain.TripRepository
import com.example.pegasus.ui.viewmodels.ReservationViewModel
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
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Sprint 04 T1.4 — Tests for [ReservationViewModel].
 *
 * Coverage:
 *  - reservations StateFlow proxies the repository observe.
 *  - cancel: remote OK + local delete + trip delete (default).
 *  - cancel: remote OK + local delete but trip kept when removeTrip = false.
 *  - cancel: remote failure → still removes locally, surfaces ERROR_REMOTE_CANCEL.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ReservationViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var reservationRepo: ReservationRepository
    private lateinit var hotelRepo: HotelRepository
    private lateinit var tripRepo: TripRepository
    private lateinit var tripImages: TripImageRepository
    private lateinit var auth: AuthRepository
    private lateinit var vm: ReservationViewModel

    private val uid     = "uid-1"
    private val tripA   = "trip-A"
    private val sampleTrip = Trip(
        id = tripA, userId = uid, title = "My Barcelona trip",
        startDate = "01/05/2025", endDate = "03/05/2025"
    )
    private val sample = Reservation(
        id = "R-1", tripId = tripA,
        hotelId = "BCN01", hotelName = "H", hotelAddress = "A", hotelImageUrl = "",
        roomId = "R1", roomType = "single", roomImageUrl = "", pricePerNight = 80.0,
        startDate = "2025-05-01", endDate = "2025-05-03", nights = 2,
        guestName = "Asier", guestEmail = "asier@example.com"
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        reservationRepo = mock {
            on { observeReservations() } doReturn flowOf(listOf(sample))
        }
        hotelRepo = mock()
        tripRepo  = mock {
            on { observeTrips(uid) } doReturn flowOf(listOf(sampleTrip))
        }
        tripImages = mock()
        auth = mock {
            on { observeCurrentUser() } doReturn flowOf(
                AuthRepository.AuthUser(uid, "asier@example.com", true)
            )
        }
        vm = ReservationViewModel(reservationRepo, hotelRepo, tripRepo, tripImages, auth)
    }

    @After fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun `reservations StateFlow exposes repository data`() = runTest {
        advanceUntilIdle()
        assertEquals(1, vm.reservations.value.size)
        assertEquals("R-1", vm.reservations.value[0].id)
    }

    @Test
    fun `tripTitlesById maps tripId to its title for the logged-in user`() = runTest {
        advanceUntilIdle()
        assertEquals("My Barcelona trip", vm.tripTitlesById.value[tripA])
    }

    @Test
    fun `tripTitlesById is empty when no user is logged in`() = runTest {
        val authNoUser: AuthRepository = mock {
            on { observeCurrentUser() } doReturn flowOf(null)
        }
        val vm2 = ReservationViewModel(reservationRepo, hotelRepo, tripRepo, tripImages, authNoUser)
        advanceUntilIdle()
        assertTrue(vm2.tripTitlesById.value.isEmpty())
    }

    @Test
    fun `cancel cleans up trip image files before removing the trip`() = runTest {
        vm.cancel(sample)
        advanceUntilIdle()

        // Order doesn't have to be strict, but both side-effects must happen.
        verify(tripImages).deleteAllForTrip(tripA)
        verify(tripRepo).deleteTrip(tripA)
    }

    @Test
    fun `cancel removeTrip=false skips both trip and trip-image cleanup`() = runTest {
        vm.cancel(sample, removeTrip = false)
        advanceUntilIdle()

        verify(tripImages, never()).deleteAllForTrip(any())
        verify(tripRepo, never()).deleteTrip(any())
    }

    @Test
    fun `cancel deletes remotely, locally and removes the linked trip`() = runTest {
        vm.cancel(sample)
        advanceUntilIdle()

        verify(hotelRepo).cancelReservation("R-1")
        verify(reservationRepo).deleteReservation("R-1")
        verify(tripRepo).deleteTrip("trip-A")
        assertNull(vm.errorMessage.value)
    }

    @Test
    fun `cancel removeTrip=false keeps the local trip`() = runTest {
        vm.cancel(sample, removeTrip = false)
        advanceUntilIdle()

        verify(hotelRepo).cancelReservation("R-1")
        verify(reservationRepo).deleteReservation("R-1")
        verify(tripRepo, never()).deleteTrip(any())
    }

    @Test
    fun `cancel still removes locally and surfaces an error when remote fails`() = runTest {
        whenever(hotelRepo.cancelReservation(eq("R-1"))).thenThrow(RuntimeException("boom"))

        vm.cancel(sample)
        advanceUntilIdle()

        // Local cleanup happens regardless.
        verify(reservationRepo).deleteReservation("R-1")
        verify(tripRepo).deleteTrip("trip-A")
        assertEquals(ReservationViewModel.ERROR_REMOTE_CANCEL, vm.errorMessage.value)
    }

    @Test
    fun `clearError resets the error state`() = runTest {
        whenever(hotelRepo.cancelReservation(any())).thenThrow(RuntimeException("x"))
        vm.cancel(sample)
        advanceUntilIdle()
        assertNotNull(vm.errorMessage.value)

        vm.clearError()
        assertNull(vm.errorMessage.value)
    }
}

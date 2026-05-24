package com.example.pegasus

import com.example.pegasus.domain.AuthRepository
import com.example.pegasus.domain.Hotel
import com.example.pegasus.domain.HotelRepository
import com.example.pegasus.domain.Reservation
import com.example.pegasus.domain.ReservationConfirmation
import com.example.pegasus.domain.ReservationRepository
import com.example.pegasus.domain.Room
import com.example.pegasus.domain.Trip
import com.example.pegasus.domain.TripRepository
import com.example.pegasus.ui.viewmodels.HotelViewModel
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
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Sprint 04 T1.4 — Validates the HotelViewModel orchestration:
 *
 *  - Search validation (missing dates / dates out of order).
 *  - Successful search updates state and clears errors.
 *  - Failed search surfaces a message + empty list.
 *  - bookRoom guards (no user / no email / missing dates).
 *  - Successful booking persists a Trip + Reservation and exposes lastBooking.
 *  - Trip title de-duplication when the hotel was already booked.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HotelViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var hotelRepo: HotelRepository
    private lateinit var reservationRepo: ReservationRepository
    private lateinit var tripRepo: TripRepository
    private lateinit var auth: AuthRepository
    private lateinit var vm: HotelViewModel

    private val uid       = "uid-1"
    private val authUser  = AuthRepository.AuthUser(uid, "asier@example.com", true)

    private val sampleRoom  = Room("R1", "single", 80.0, listOf("https://x/img.png"))
    private val sampleHotel = Hotel(
        id = "BCN01", name = "Hotel Ramblas", address = "La Rambla 33",
        rating = 4, imageUrl = "https://x/BCN01.png",
        rooms = listOf(sampleRoom)
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        hotelRepo       = mock()
        reservationRepo = mock()
        tripRepo        = mock {
            on { observeTrips(any()) } doReturn flowOf(emptyList())
        }
        auth = mock {
            on { observeCurrentUser() } doReturn flowOf(authUser)
            on { currentUser() } doReturn authUser
        }
        vm = HotelViewModel(hotelRepo, reservationRepo, tripRepo, auth)
    }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    // ── search validation ───────────────────────────────────────────────────────

    @Test
    fun `search without dates surfaces ERROR_DATES_REQUIRED and skips the API`() = runTest {
        vm.searchHotels()
        advanceUntilIdle()
        assertEquals(HotelViewModel.ERROR_DATES_REQUIRED, vm.errorMessage.value)
        verify(hotelRepo, never()).checkAvailability(any(), any(), any())
    }

    @Test
    fun `search with end-before-start surfaces ERROR_DATE_ORDER`() = runTest {
        vm.setStartDate("2025-05-10")
        vm.setEndDate("2025-05-01")

        vm.searchHotels()
        advanceUntilIdle()

        assertEquals(HotelViewModel.ERROR_DATE_ORDER, vm.errorMessage.value)
        verify(hotelRepo, never()).checkAvailability(any(), any(), any())
    }

    @Test
    fun `search success replaces the hotel list and clears errors`() = runTest {
        whenever(hotelRepo.checkAvailability("BCN", "2025-05-01", "2025-05-03"))
            .thenReturn(listOf(sampleHotel))

        vm.setStartDate("2025-05-01")
        vm.setEndDate("2025-05-03")
        vm.searchHotels()
        advanceUntilIdle()

        assertEquals(1, vm.hotels.value.size)
        assertEquals("BCN01", vm.hotels.value[0].id)
        assertNull(vm.errorMessage.value)
    }

    @Test
    fun `search failure surfaces error message and clears the list`() = runTest {
        whenever(hotelRepo.checkAvailability(any(), any(), any()))
            .thenThrow(RuntimeException("boom"))

        vm.setStartDate("2025-05-01")
        vm.setEndDate("2025-05-03")
        vm.searchHotels()
        advanceUntilIdle()

        assertEquals("boom", vm.errorMessage.value)
        assertTrue(vm.hotels.value.isEmpty())
    }

    // ── bookRoom guards ────────────────────────────────────────────────────────

    @Test
    fun `book without user surfaces ERROR_NO_USER`() = runTest {
        whenever(auth.currentUser()).thenReturn(null)
        vm.setStartDate("2025-05-01")
        vm.setEndDate("2025-05-03")

        vm.bookRoom(sampleHotel, sampleRoom)
        advanceUntilIdle()

        assertEquals(HotelViewModel.ERROR_NO_USER, vm.errorMessage.value)
        verify(reservationRepo, never()).addReservation(any())
    }

    @Test
    fun `book without dates surfaces ERROR_DATES_REQUIRED`() = runTest {
        vm.bookRoom(sampleHotel, sampleRoom)
        advanceUntilIdle()
        assertEquals(HotelViewModel.ERROR_DATES_REQUIRED, vm.errorMessage.value)
    }

    @Test
    fun `book without email surfaces ERROR_NO_EMAIL`() = runTest {
        whenever(auth.currentUser()).thenReturn(authUser.copy(email = null))
        vm.setStartDate("2025-05-01"); vm.setEndDate("2025-05-03")

        vm.bookRoom(sampleHotel, sampleRoom)
        advanceUntilIdle()

        assertEquals(HotelViewModel.ERROR_NO_EMAIL, vm.errorMessage.value)
    }

    // ── bookRoom success ───────────────────────────────────────────────────────

    @Test
    fun `book success persists Trip + Reservation and sets lastBooking`() = runTest {
        whenever(hotelRepo.reserveRoom(any(), any(), any(), any(), any(), any()))
            .thenReturn(ReservationConfirmation("RID-1", 2, "ok"))
        whenever(tripRepo.isTitleTakenByOther(eq(uid), any(), any())).thenReturn(false)

        vm.setStartDate("2025-05-01"); vm.setEndDate("2025-05-03")
        vm.bookRoom(sampleHotel, sampleRoom)
        advanceUntilIdle()

        val tripCaptor = argumentCaptor<Trip>()
        verify(tripRepo).addTrip(tripCaptor.capture())
        val trip = tripCaptor.firstValue
        assertEquals(uid, trip.userId)
        assertEquals("Hotel Ramblas (BCN01)", trip.title)
        // dates converted to dd/MM/yyyy for local storage
        assertEquals("01/05/2025", trip.startDate)
        assertEquals("03/05/2025", trip.endDate)
        assertEquals(160, trip.budget) // 80 * 2 nights

        val resCaptor = argumentCaptor<Reservation>()
        verify(reservationRepo).addReservation(resCaptor.capture())
        val reservation = resCaptor.firstValue
        assertEquals("RID-1", reservation.id)
        assertEquals(trip.id, reservation.tripId)
        assertEquals("BCN01", reservation.hotelId)
        assertEquals(2, reservation.nights)
        assertEquals(80.0, reservation.pricePerNight, 0.001)

        assertEquals("RID-1", vm.lastBooking.value?.id)
        assertNull(vm.errorMessage.value)
    }

    @Test
    fun `book de-duplicates the trip title when the same hotel was already booked`() = runTest {
        whenever(hotelRepo.reserveRoom(any(), any(), any(), any(), any(), any()))
            .thenReturn(ReservationConfirmation("RID-2", 1, "ok"))
        // base name "Hotel Ramblas (BCN01)" is taken, "#2" is free
        whenever(tripRepo.isTitleTakenByOther(eq(uid), eq("Hotel Ramblas (BCN01)"), any())).thenReturn(true)
        whenever(tripRepo.isTitleTakenByOther(eq(uid), eq("Hotel Ramblas (BCN01) #2"), any())).thenReturn(false)

        vm.setStartDate("2025-05-01"); vm.setEndDate("2025-05-02")
        vm.bookRoom(sampleHotel, sampleRoom)
        advanceUntilIdle()

        val tripCaptor = argumentCaptor<Trip>()
        verify(tripRepo).addTrip(tripCaptor.capture())
        assertEquals("Hotel Ramblas (BCN01) #2", tripCaptor.firstValue.title)
    }

    @Test
    fun `book failure surfaces error and does not persist anything`() = runTest {
        whenever(hotelRepo.reserveRoom(any(), any(), any(), any(), any(), any()))
            .thenThrow(RuntimeException("server down"))

        vm.setStartDate("2025-05-01"); vm.setEndDate("2025-05-03")
        vm.bookRoom(sampleHotel, sampleRoom)
        advanceUntilIdle()

        assertEquals("server down", vm.errorMessage.value)
        verify(tripRepo, never()).addTrip(any())
        verify(reservationRepo, never()).addReservation(any())
        assertNull(vm.lastBooking.value)
    }

    // ── clear helpers ──────────────────────────────────────────────────────────

    @Test
    fun `clearError resets the error state`() = runTest {
        vm.searchHotels()           // triggers ERROR_DATES_REQUIRED
        advanceUntilIdle()
        assertNotNull(vm.errorMessage.value)
        vm.clearError()
        assertNull(vm.errorMessage.value)
    }

    @Test
    fun `clearLastBooking resets the dialog trigger`() = runTest {
        whenever(hotelRepo.reserveRoom(any(), any(), any(), any(), any(), any()))
            .thenReturn(ReservationConfirmation("RID-3", 1, "ok"))
        whenever(tripRepo.isTitleTakenByOther(any(), any(), any())).thenReturn(false)

        vm.setStartDate("2025-05-01"); vm.setEndDate("2025-05-02")
        vm.bookRoom(sampleHotel, sampleRoom)
        advanceUntilIdle()
        assertNotNull(vm.lastBooking.value)

        vm.clearLastBooking()
        assertNull(vm.lastBooking.value)
    }
}

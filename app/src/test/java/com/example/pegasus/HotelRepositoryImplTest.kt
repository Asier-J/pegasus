package com.example.pegasus

import com.example.pegasus.data.remote.api.HotelApiService
import com.example.pegasus.data.remote.dto.ApiMessageDto
import com.example.pegasus.data.remote.dto.AvailabilityResponseDto
import com.example.pegasus.data.remote.dto.HotelDto
import com.example.pegasus.data.remote.dto.ReservationDto
import com.example.pegasus.data.remote.dto.ReservationResponseDto
import com.example.pegasus.data.remote.dto.ReserveRequestDto
import com.example.pegasus.data.remote.dto.RoomDto
import com.example.pegasus.data.remote.mapper.toDomain
import com.example.pegasus.data.repository.HotelRepositoryImpl
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Sprint 04 T1.4 — Unit tests for [HotelRepositoryImpl] using a mocked
 * [HotelApiService] (no MockWebServer needed).
 *
 * Coverage:
 *  - The repository wires the right group_id into every API call.
 *  - DTOs are mapped to domain models and image URLs are made absolute.
 *  - reserveRoom returns a flat [com.example.pegasus.domain.ReservationConfirmation].
 *  - Failures from the API bubble up as exceptions.
 */
class HotelRepositoryImplTest {

    private val baseUrl = "http://example.com/"
    private val groupId = "G10"

    private fun build(api: HotelApiService) =
        HotelRepositoryImpl(api, groupId, baseUrl)

    // ── listAllHotels ───────────────────────────────────────────────────────────

    @Test
    fun `listAllHotels passes group id and maps absolute image URLs`() = runTest {
        val api = mock<HotelApiService>()
        whenever(api.listHotels(eq(groupId))).thenReturn(listOf(
            HotelDto(
                id = "BCN01", name = "X", address = "Y", rating = 5,
                imageUrl = "/images/BCN01.png",
                rooms = listOf(RoomDto("R1", "single", 50.0, listOf("/images/BCN01R1.png")))
            )
        ))

        val hotels = build(api).listAllHotels()

        assertEquals(1, hotels.size)
        assertEquals("BCN01", hotels[0].id)
        assertEquals("http://example.com/images/BCN01.png", hotels[0].imageUrl)
        assertEquals(1, hotels[0].rooms.size)
        assertEquals("http://example.com/images/BCN01R1.png", hotels[0].rooms[0].images[0])
        verify(api).listHotels(groupId)
    }

    // ── checkAvailability ───────────────────────────────────────────────────────

    @Test
    fun `checkAvailability forwards all params and unwraps available_hotels`() = runTest {
        val api = mock<HotelApiService>()
        whenever(api.checkAvailability(
            groupId   = eq(groupId),
            startDate = eq("2025-05-01"),
            endDate   = eq("2025-05-03"),
            city      = eq("BCN"),
            hotelId   = anyOrNull()
        )).thenReturn(AvailabilityResponseDto(
            availableHotels = listOf(
                HotelDto("BCN01", "X", "Y", 5, "/images/BCN01.png", rooms = emptyList())
            )
        ))

        val hotels = build(api).checkAvailability("BCN", "2025-05-01", "2025-05-03")

        assertEquals(1, hotels.size)
        assertEquals("BCN01", hotels[0].id)
        verify(api).checkAvailability(
            groupId   = eq(groupId),
            startDate = eq("2025-05-01"),
            endDate   = eq("2025-05-03"),
            city      = eq("BCN"),
            hotelId   = anyOrNull()
        )
    }

    // ── reserveRoom ─────────────────────────────────────────────────────────────

    @Test
    fun `reserveRoom builds request body and returns confirmation`() = runTest {
        val api = mock<HotelApiService>()
        whenever(api.reserveRoom(eq(groupId), any())).thenReturn(ReservationResponseDto(
            message = "Reserva confirmada",
            nights  = 3,
            reservation = ReservationDto(
                id = "FTGEHP", hotelId = "BCN03", roomId = "R3",
                startDate = "2025-05-01", endDate = "2025-05-04",
                guestName = "asier", guestEmail = "asier@example.com"
            )
        ))

        val confirmation = build(api).reserveRoom(
            hotelId    = "BCN03",
            roomId     = "R3",
            startDate  = "2025-05-01",
            endDate    = "2025-05-04",
            guestName  = "asier",
            guestEmail = "asier@example.com"
        )

        assertEquals("FTGEHP", confirmation.reservationId)
        assertEquals(3, confirmation.nights)
        assertEquals("Reserva confirmada", confirmation.message)

        val bodyCaptor = argumentCaptor<ReserveRequestDto>()
        verify(api).reserveRoom(eq(groupId), bodyCaptor.capture())
        val sent = bodyCaptor.firstValue
        assertEquals("BCN03", sent.hotelId)
        assertEquals("R3",   sent.roomId)
        assertEquals("2025-05-01", sent.startDate)
        assertEquals("2025-05-04", sent.endDate)
        assertEquals("asier@example.com", sent.guestEmail)
    }

    // ── cancelReservation ───────────────────────────────────────────────────────

    @Test
    fun `cancelReservation calls API with the reservation id`() = runTest {
        val api = mock<HotelApiService>()
        whenever(api.cancelReservation(eq("FTGEHP"))).thenReturn(ApiMessageDto("ok", "FTGEHP"))

        build(api).cancelReservation("FTGEHP")

        verify(api).cancelReservation("FTGEHP")
    }

    // ── failures ────────────────────────────────────────────────────────────────

    @Test(expected = RuntimeException::class)
    fun `network exception is propagated to caller`() = runTest {
        val api = mock<HotelApiService>()
        whenever(api.listHotels(eq(groupId))).thenThrow(RuntimeException("boom"))

        build(api).listAllHotels()
    }

    // ── mapper sanity ───────────────────────────────────────────────────────────

    @Test
    fun `mapper keeps absolute URLs untouched`() {
        val dto = HotelDto(
            id = "X", name = "Y", address = "Z", rating = 1,
            imageUrl = "https://cdn.example/photo.png",
            rooms = emptyList()
        )
        val domain = dto.toDomain(baseUrl)
        assertEquals("https://cdn.example/photo.png", domain.imageUrl)
    }

    @Test
    fun `mapper handles null rooms as empty list`() {
        val dto = HotelDto(
            id = "X", name = "Y", address = "Z", rating = 1,
            imageUrl = "/img.png", rooms = null
        )
        val domain = dto.toDomain(baseUrl)
        assertTrue(domain.rooms.isEmpty())
    }
}

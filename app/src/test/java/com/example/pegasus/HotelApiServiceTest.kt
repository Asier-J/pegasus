package com.example.pegasus

import com.example.pegasus.data.remote.api.HotelApiService
import com.example.pegasus.data.remote.dto.ReserveRequestDto
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Sprint 04 T1.4 — Unit tests for [HotelApiService] using MockWebServer.
 *
 * Each test enqueues a JSON response, runs a real Retrofit call through the
 * mock server and asserts both:
 *   - the request the server received (path + body), and
 *   - the deserialised DTO returned to the caller.
 */
class HotelApiServiceTest {

    private lateinit var server: MockWebServer
    private lateinit var api: HotelApiService

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(HotelApiService::class.java)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    // ── listHotels ──────────────────────────────────────────────────────────────

    @Test
    fun `listHotels parses JSON array into HotelDto list`() = runTest {
        server.enqueue(MockResponse().setBody("""
            [
              {
                "id":"BCN01","name":"Hotel Ramblas","address":"La Rambla 33",
                "rating":4,"image_url":"/images/BCN01.png",
                "rooms":[{"id":"R1","room_type":"single","price":80.0,"images":["/images/BCN01R1.png"]}]
              }
            ]
        """.trimIndent()))

        val hotels = api.listHotels("G10")

        assertEquals(1, hotels.size)
        val hotel = hotels[0]
        assertEquals("BCN01", hotel.id)
        assertEquals("Hotel Ramblas", hotel.name)
        assertEquals(4, hotel.rating)
        assertEquals("/images/BCN01.png", hotel.imageUrl)
        assertEquals(1, hotel.rooms?.size)
        assertEquals("R1", hotel.rooms!![0].id)
        assertEquals("single", hotel.rooms[0].roomType)
        assertEquals(80.0, hotel.rooms[0].price, 0.001)

        val request = server.takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/hotels/G10/hotels", request.path)
    }

    // ── checkAvailability ───────────────────────────────────────────────────────

    @Test
    fun `checkAvailability sends start_date, end_date and city as query params`() = runTest {
        server.enqueue(MockResponse().setBody("""
            {"available_hotels":[
              {"id":"BCN01","name":"Hotel Ramblas","address":"La Rambla 33",
               "rating":4,"image_url":"/images/BCN01.png","rooms":[]}
            ]}
        """.trimIndent()))

        val resp = api.checkAvailability(
            groupId   = "G10",
            startDate = "2025-05-01",
            endDate   = "2025-05-03",
            city      = "BCN"
        )

        assertEquals(1, resp.availableHotels.size)
        assertEquals("BCN01", resp.availableHotels[0].id)

        val request = server.takeRequest()
        assertEquals("GET", request.method)
        val path = request.path!!
        assertTrue(path.startsWith("/hotels/G10/availability"))
        assertTrue(path.contains("start_date=2025-05-01"))
        assertTrue(path.contains("end_date=2025-05-03"))
        assertTrue(path.contains("city=BCN"))
    }

    @Test
    fun `checkAvailability omits null query params`() = runTest {
        server.enqueue(MockResponse().setBody("""{"available_hotels":[]}"""))

        api.checkAvailability(
            groupId   = "G10",
            startDate = "2025-05-01",
            endDate   = "2025-05-03"
        )

        val path = server.takeRequest().path!!
        assertTrue(path.contains("start_date=2025-05-01"))
        assertTrue(path.contains("end_date=2025-05-03"))
        assertFalse("city should not be present when null", path.contains("city="))
        assertFalse("hotel_id should not be present when null", path.contains("hotel_id="))
    }

    // ── reserveRoom ─────────────────────────────────────────────────────────────

    @Test
    fun `reserveRoom sends JSON body and parses confirmation`() = runTest {
        server.enqueue(MockResponse().setBody("""
            {
              "message":"Reserva confirmada",
              "nights":3,
              "reservation":{
                "id":"FTGEHP","hotel_id":"BCN03","room_id":"R3",
                "start_date":"2025-05-01","end_date":"2025-05-04",
                "guest_name":"vitor","guest_email":"vitorlui@gmail.com"
              }
            }
        """.trimIndent()))

        val resp = api.reserveRoom(
            groupId = "G10",
            request = ReserveRequestDto(
                hotelId    = "BCN03",
                roomId     = "R3",
                startDate  = "2025-05-01",
                endDate    = "2025-05-04",
                guestName  = "vitor",
                guestEmail = "vitorlui@gmail.com"
            )
        )

        assertEquals(3, resp.nights)
        assertEquals("FTGEHP", resp.reservation.id)
        assertEquals("BCN03", resp.reservation.hotelId)
        assertEquals("R3", resp.reservation.roomId)

        val request = server.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/hotels/G10/reserve", request.path)

        val body = request.body.readUtf8()
        assertTrue(body.contains("\"hotel_id\":\"BCN03\""))
        assertTrue(body.contains("\"room_id\":\"R3\""))
        assertTrue(body.contains("\"start_date\":\"2025-05-01\""))
        assertTrue(body.contains("\"end_date\":\"2025-05-04\""))
        assertTrue(body.contains("\"guest_email\":\"vitorlui@gmail.com\""))
    }

    // ── cancelReservation ───────────────────────────────────────────────────────

    @Test
    fun `cancelReservation issues DELETE on reservations endpoint`() = runTest {
        server.enqueue(MockResponse().setBody("""{"message":"cancelled","id":"FTGEHP"}"""))

        val ack = api.cancelReservation("FTGEHP")

        assertEquals("cancelled", ack.message)
        assertEquals("FTGEHP", ack.id)

        val request = server.takeRequest()
        assertEquals("DELETE", request.method)
        assertEquals("/reservations/FTGEHP", request.path)
    }

    // ── error handling ──────────────────────────────────────────────────────────

    @Test(expected = retrofit2.HttpException::class)
    fun `non-2xx response surfaces as HttpException`() = runTest {
        server.enqueue(MockResponse().setResponseCode(400).setBody(
            """{"detail":"Sólo se permiten reservas en mayo y junio de 2025"}"""
        ))

        api.checkAvailability(
            groupId   = "G10",
            startDate = "2026-05-01",
            endDate   = "2026-05-03",
            city      = "BCN"
        )
    }
}

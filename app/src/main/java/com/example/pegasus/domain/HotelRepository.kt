package com.example.pegasus.domain

/**
 * Sprint 04 — Remote repository for the Hotels Demo REST API.
 *
 * Implementations talk to Retrofit + the `/hotels/{group_id}/...` endpoints
 * and map DTOs into the [Hotel] / [Room] domain models.
 */
interface HotelRepository {

    /** GET /hotels/{group_id}/hotels — every hotel and all its rooms. */
    suspend fun listAllHotels(): List<Hotel>

    /**
     * GET /hotels/{group_id}/availability — hotels with at least one available
     * room in the requested window.
     *
     * @param city       one of "BCN", "PAR", "LON".
     * @param startDate  ISO-8601 (yyyy-MM-dd).
     * @param endDate    ISO-8601 (yyyy-MM-dd).
     */
    suspend fun checkAvailability(city: String, startDate: String, endDate: String): List<Hotel>

    /**
     * POST /hotels/{group_id}/reserve — book a room.
     *
     * Returns the server-generated reservation id and the number of nights so
     * the caller can persist a [Reservation] row locally.
     */
    suspend fun reserveRoom(
        hotelId: String,
        roomId: String,
        startDate: String,
        endDate: String,
        guestName: String,
        guestEmail: String
    ): ReservationConfirmation

    /** DELETE /reservations/{res_id} — cancel server-side. Local row is removed by [ReservationRepository]. */
    suspend fun cancelReservation(reservationId: String)
}

/** Slim DTO returned by [HotelRepository.reserveRoom]. */
data class ReservationConfirmation(
    val reservationId: String,
    val nights: Int,
    val message: String
)

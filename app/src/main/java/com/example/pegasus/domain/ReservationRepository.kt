package com.example.pegasus.domain

import kotlinx.coroutines.flow.Flow

/**
 * Sprint 04 — Local repository for [Reservation] rows.
 *
 * Booking flow:
 *  1. UI asks [HotelRepository.reserveRoom] for the remote booking.
 *  2. On success, the ViewModel builds a [Reservation] (with hotel/room
 *     snapshots) and calls [addReservation] to persist it locally.
 *  3. The reservation list screen observes [observeReservations].
 *
 * Cancel flow mirrors the booking flow: remote DELETE then [deleteReservation].
 */
interface ReservationRepository {

    fun observeReservations(): Flow<List<Reservation>>

    fun observeReservationsForTrip(tripId: String): Flow<List<Reservation>>

    suspend fun getReservationById(id: String): Reservation?

    suspend fun addReservation(reservation: Reservation)

    suspend fun deleteReservation(reservationId: String)

    suspend fun deleteReservationsForTrip(tripId: String)
}

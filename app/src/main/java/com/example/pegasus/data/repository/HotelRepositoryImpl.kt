package com.example.pegasus.data.repository

import android.util.Log
import com.example.pegasus.BuildConfig
import com.example.pegasus.data.remote.api.HotelApiService
import com.example.pegasus.data.remote.dto.ReserveRequestDto
import com.example.pegasus.data.remote.mapper.toDomain
import com.example.pegasus.domain.Hotel
import com.example.pegasus.domain.HotelRepository
import com.example.pegasus.domain.ReservationConfirmation
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/**
 * Sprint 04 — Retrofit-backed implementation of [HotelRepository].
 *
 * Logs every remote call at DEBUG (attempt) / INFO (success) / ERROR (failure)
 * with the chosen [groupId] so misrouting bugs are obvious in Logcat.
 */
@Singleton
class HotelRepositoryImpl @Inject constructor(
    private val api: HotelApiService,
    @Named("groupId")    private val groupId: String,
    @Named("hotelsBase") private val baseUrl: String
) : HotelRepository {

    constructor(api: HotelApiService) : this(
        api      = api,
        groupId  = BuildConfig.GROUP_ID,
        baseUrl  = BuildConfig.HOTELS_API_URL
    )

    override suspend fun listAllHotels(): List<Hotel> {
        Log.d(TAG, "listHotels(group=$groupId)")
        return runCatching { api.listHotels(groupId).map { it.toDomain(baseUrl) } }
            .onSuccess { Log.i(TAG, "listHotels → ${it.size} hotels") }
            .onFailure { Log.e(TAG, "listHotels failed", it) }
            .getOrThrow()
    }

    override suspend fun checkAvailability(
        city: String,
        startDate: String,
        endDate: String
    ): List<Hotel> {
        Log.d(TAG, "availability(group=$groupId, city=$city, $startDate..$endDate)")
        return runCatching {
            api.checkAvailability(
                groupId   = groupId,
                startDate = startDate,
                endDate   = endDate,
                city      = city
            ).availableHotels.map { it.toDomain(baseUrl) }
        }
            .onSuccess { Log.i(TAG, "availability → ${it.size} hotels") }
            .onFailure { Log.e(TAG, "availability failed", it) }
            .getOrThrow()
    }

    override suspend fun reserveRoom(
        hotelId: String,
        roomId: String,
        startDate: String,
        endDate: String,
        guestName: String,
        guestEmail: String
    ): ReservationConfirmation {
        Log.d(TAG, "reserve(group=$groupId, hotel=$hotelId, room=$roomId, $startDate..$endDate)")
        return runCatching {
            val resp = api.reserveRoom(
                groupId = groupId,
                request = ReserveRequestDto(
                    hotelId    = hotelId,
                    roomId     = roomId,
                    startDate  = startDate,
                    endDate    = endDate,
                    guestName  = guestName,
                    guestEmail = guestEmail
                )
            )
            ReservationConfirmation(
                reservationId = resp.reservation.id,
                nights        = resp.nights,
                message       = resp.message
            )
        }
            .onSuccess { Log.i(TAG, "reserve OK → id=${it.reservationId}, nights=${it.nights}") }
            .onFailure { Log.e(TAG, "reserve failed", it) }
            .getOrThrow()
    }

    override suspend fun cancelReservation(reservationId: String) {
        Log.d(TAG, "cancel(reservation=$reservationId)")
        runCatching { api.cancelReservation(reservationId) }
            .onSuccess { Log.i(TAG, "cancel OK → $reservationId") }
            .onFailure { Log.e(TAG, "cancel failed", it) }
            .getOrThrow()
    }

    companion object {
        private const val TAG = "HotelRepository"
    }
}

package com.example.pegasus.data.remote.api

import com.example.pegasus.data.remote.dto.ApiMessageDto
import com.example.pegasus.data.remote.dto.AvailabilityResponseDto
import com.example.pegasus.data.remote.dto.HotelDto
import com.example.pegasus.data.remote.dto.ReservationDto
import com.example.pegasus.data.remote.dto.ReserveRequestDto
import com.example.pegasus.data.remote.dto.ReservationResponseDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Sprint 04 — Retrofit interface for the Hotels Demo REST API.
 *
 * Base URL is `BuildConfig.HOTELS_API_URL` and `groupId` is injected on every
 * call so the same service can be re-used in tests with a different group.
 */
interface HotelApiService {

    @GET("hotels/{group_id}/hotels")
    suspend fun listHotels(
        @Path("group_id") groupId: String
    ): List<HotelDto>

    @GET("hotels/{group_id}/availability")
    suspend fun checkAvailability(
        @Path("group_id") groupId: String,
        @Query("start_date") startDate: String,
        @Query("end_date")   endDate: String,
        @Query("city")       city: String? = null,
        @Query("hotel_id")   hotelId: String? = null
    ): AvailabilityResponseDto

    @POST("hotels/{group_id}/reserve")
    suspend fun reserveRoom(
        @Path("group_id") groupId: String,
        @Body request: ReserveRequestDto
    ): ReservationResponseDto

    @GET("hotels/{group_id}/reservations")
    suspend fun listReservations(
        @Path("group_id") groupId: String,
        @Query("guest_email") guestEmail: String? = null
    ): List<ReservationDto>

    @DELETE("reservations/{res_id}")
    suspend fun cancelReservation(
        @Path("res_id") reservationId: String
    ): ApiMessageDto
}

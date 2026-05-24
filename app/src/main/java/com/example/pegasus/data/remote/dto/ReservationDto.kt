package com.example.pegasus.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Reservation as returned by the server (inside POST /reserve responses and the
 * GET /reservations payloads). It optionally carries denormalised hotel/room
 * snapshots — they're present on GET responses and absent on POST responses.
 */
data class ReservationDto(
    @SerializedName("id")          val id: String,
    @SerializedName("hotel_id")    val hotelId: String,
    @SerializedName("room_id")     val roomId: String,
    @SerializedName("start_date")  val startDate: String,
    @SerializedName("end_date")    val endDate: String,
    @SerializedName("guest_name")  val guestName: String,
    @SerializedName("guest_email") val guestEmail: String,
    @SerializedName("hotel")       val hotel: HotelDto? = null,
    @SerializedName("room")        val room: RoomDto? = null
)

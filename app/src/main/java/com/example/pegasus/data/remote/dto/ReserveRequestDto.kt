package com.example.pegasus.data.remote.dto

import com.google.gson.annotations.SerializedName

/** Request body for POST /hotels/{group_id}/reserve. */
data class ReserveRequestDto(
    @SerializedName("hotel_id")    val hotelId: String,
    @SerializedName("room_id")     val roomId: String,
    @SerializedName("start_date")  val startDate: String,   // YYYY-MM-DD
    @SerializedName("end_date")    val endDate: String,     // YYYY-MM-DD
    @SerializedName("guest_name")  val guestName: String,
    @SerializedName("guest_email") val guestEmail: String
)

package com.example.pegasus.data.remote.dto

import com.google.gson.annotations.SerializedName

/** Response body of POST /hotels/{group_id}/reserve. */
data class ReservationResponseDto(
    @SerializedName("message")     val message: String,
    @SerializedName("nights")      val nights: Int,
    @SerializedName("reservation") val reservation: ReservationDto
)

/** Generic acknowledgement returned by cancel endpoints. */
data class ApiMessageDto(
    @SerializedName("message") val message: String? = null,
    @SerializedName("id")      val id: String? = null
)

package com.example.pegasus.data.remote.dto

import com.google.gson.annotations.SerializedName

/** DTO for a hotel room — embedded inside [HotelDto.rooms]. */
data class RoomDto(
    @SerializedName("id")        val id: String,
    @SerializedName("room_type") val roomType: String,
    @SerializedName("price")     val price: Double,
    @SerializedName("images")    val images: List<String> = emptyList()
)

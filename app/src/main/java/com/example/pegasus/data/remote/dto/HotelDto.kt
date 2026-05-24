package com.example.pegasus.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO returned by GET /hotels/{group_id}/hotels and (nested) /availability.
 *
 * Field names match the JSON payload of the Hotels Demo API exactly.
 */
data class HotelDto(
    @SerializedName("id")        val id: String,
    @SerializedName("name")      val name: String,
    @SerializedName("address")   val address: String,
    @SerializedName("rating")    val rating: Int,
    @SerializedName("image_url") val imageUrl: String,
    @SerializedName("rooms")     val rooms: List<RoomDto>? = null
)

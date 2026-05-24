package com.example.pegasus.data.remote.dto

import com.google.gson.annotations.SerializedName

/** Wrapper returned by GET /hotels/{group_id}/availability. */
data class AvailabilityResponseDto(
    @SerializedName("available_hotels") val availableHotels: List<HotelDto> = emptyList()
)

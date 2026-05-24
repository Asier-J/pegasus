package com.example.pegasus.data.remote.mapper

import com.example.pegasus.data.remote.dto.HotelDto
import com.example.pegasus.data.remote.dto.RoomDto
import com.example.pegasus.domain.Hotel
import com.example.pegasus.domain.Room

/**
 * Sprint 04 — DTO → Domain mappers.
 *
 * Image URLs returned by the API are relative paths like `/images/BCN01.png`.
 * We prepend the base URL here so domain layer + UI can use them as-is.
 */
fun HotelDto.toDomain(baseUrl: String): Hotel = Hotel(
    id        = id,
    name      = name,
    address   = address,
    rating    = rating,
    imageUrl  = absoluteUrl(baseUrl, imageUrl),
    rooms     = rooms.orEmpty().map { it.toDomain(baseUrl) }
)

fun RoomDto.toDomain(baseUrl: String): Room = Room(
    id        = id,
    roomType  = roomType,
    price     = price,
    images    = images.map { absoluteUrl(baseUrl, it) }
)

/**
 * Combines [baseUrl] (e.g. `http://15.224.84.148:8090/`) with an API path
 * (e.g. `/images/BCN01.png`) into a single, well-formed URL.
 */
internal fun absoluteUrl(baseUrl: String, path: String): String {
    if (path.startsWith("http://") || path.startsWith("https://")) return path
    val base = baseUrl.trimEnd('/')
    val tail = path.trimStart('/')
    return "$base/$tail"
}

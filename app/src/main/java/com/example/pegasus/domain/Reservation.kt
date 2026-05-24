package com.example.pegasus.domain

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Sprint 04 — Reservation persisted locally after a successful POST /reserve call.
 *
 * - `id`         — server-generated reservation id (PK).
 * - `tripId`     — local Trip the reservation belongs to (FK, CASCADE).
 * - `hotelId`    — id returned by the API (e.g. "BCN01").
 * - `hotelName`, `hotelAddress`, `hotelImageUrl` — denormalised so the
 *   reservation list survives even if the remote hotel list is offline.
 * - `roomId`, `roomType`, `roomImageUrl`, `pricePerNight` — same idea for the room.
 * - `startDate`, `endDate` — ISO `YYYY-MM-DD` strings (API format).
 * - `nights`     — server-returned number of nights.
 * - `guestName`, `guestEmail` — values used when booking.
 * - `createdAt`  — epoch ms when the reservation was stored locally.
 */
@Entity(
    tableName = "reservations",
    foreignKeys = [
        ForeignKey(
            entity = Trip::class,
            parentColumns = ["id"],
            childColumns = ["tripId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("tripId")]
)
data class Reservation(
    @PrimaryKey
    val id: String,
    val tripId: String,
    val hotelId: String,
    val hotelName: String,
    val hotelAddress: String,
    val hotelImageUrl: String,
    val roomId: String,
    val roomType: String,
    val roomImageUrl: String,
    val pricePerNight: Double,
    val startDate: String,
    val endDate: String,
    val nights: Int,
    val guestName: String,
    val guestEmail: String,
    val createdAt: Long = System.currentTimeMillis()
) {
    val totalPrice: Double get() = pricePerNight * nights
}

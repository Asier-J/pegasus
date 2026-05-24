package com.example.pegasus.domain

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Sprint 04 — A photo attached to a Trip (Sprint 04 T3).
 *
 * The file itself lives in app-internal storage at
 * `filesDir/trip_images/<tripId>/<uuid>`. `localPath` holds the absolute path so
 * Coil can render it directly via `File(localPath)`.
 */
@Entity(
    tableName = "trip_images",
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
data class TripImage(
    @PrimaryKey
    val id: String = java.util.UUID.randomUUID().toString(),
    val tripId: String,
    val localPath: String,
    val addedAt: Long = System.currentTimeMillis()
)

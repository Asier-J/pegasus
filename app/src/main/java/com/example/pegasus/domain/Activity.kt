package com.example.pegasus.domain

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalTime

// ─── Activity ─────────────────────────────────────────────────────────────────
// Represents a single itinerary item inside a Trip (many to 1).
// Sprint 03: Room @Entity. LocalDate/LocalTime stored via TypeConverters.
@Entity(
    tableName = "activities",
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
data class Activity(
    @PrimaryKey
    val id: String = java.util.UUID.randomUUID().toString(),
    val tripId: String,
    val title: String,                   // text
    val description: String = "",        // text
    val date: LocalDate,                 // datetime
    val time: LocalTime,                 // datetime
    val durationMinutes: Int = 60,       // integer (Sprint 03 T1.2 — at least one int field)
    val createdAt: Long = System.currentTimeMillis()  // datetime as INTEGER (epoch ms)
)

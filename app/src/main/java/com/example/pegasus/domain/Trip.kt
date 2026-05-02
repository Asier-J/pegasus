package com.example.pegasus.domain

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// ─── Trip ─────────────────────────────────────────────────────────────────────
// Represents a travel trip owned by a User (many to 1).
// Sprint 02: in-memory storage with String dates (dd/MM/yyyy).
// Sprint 03: now a Room @Entity with userId FK so each user only sees their own trips.
@Entity(
    tableName = "trips",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["uid"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("userId")]
)
data class Trip(
    @PrimaryKey
    val id: String = java.util.UUID.randomUUID().toString(),
    val userId: String,                  // Sprint 03: trip owner (Firebase uid)
    val title: String,                   // text
    val startDate: String,               // text — dd/MM/yyyy
    val endDate: String,                 // text — dd/MM/yyyy
    val description: String = "",        // text
    val budget: Int = 0,                 // integer (Sprint 03 T1.2 — at least one int field)
    val createdAt: Long = System.currentTimeMillis()  // datetime stored as INTEGER (epoch ms)
)

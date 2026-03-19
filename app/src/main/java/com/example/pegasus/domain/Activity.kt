package com.example.pegasus.domain

import java.time.LocalDate
import java.time.LocalTime

// ─── Activity ─────────────────────────────────────────────────────────────────
// Represents a single activity/itinerary item inside a Trip (many to 1).
// Sprint 02: Uses LocalDate and LocalTime for precise scheduling.
data class Activity(
    val id: String = java.util.UUID.randomUUID().toString(),
    val tripId: String,
    val title: String,
    val description: String = "",
    val date: LocalDate,
    val time: LocalTime
)

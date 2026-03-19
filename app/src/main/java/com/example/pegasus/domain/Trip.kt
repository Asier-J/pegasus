package com.example.pegasus.domain

// ─── Trip ─────────────────────────────────────────────────────────────────────
// Represents a travel trip owned by a User (many to 1).
// Sprint 02: Uses String dates in dd/MM/yyyy format for UI simplicity.
data class Trip(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val startDate: String,    // format: dd/MM/yyyy
    val endDate: String,      // format: dd/MM/yyyy
    val description: String = ""
)

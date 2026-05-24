package com.example.pegasus.domain

/**
 * Sprint 04 — Domain model for a hotel room.
 */
data class Room(
    val id: String,
    val roomType: String,
    val price: Double,
    val images: List<String>
)

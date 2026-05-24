package com.example.pegasus.domain

/**
 * Sprint 04 — Domain model for a hotel returned by the Hotels REST API.
 */
data class Hotel(
    val id: String,
    val name: String,
    val address: String,
    val rating: Int,
    val imageUrl: String,
    val rooms: List<Room>
)

package com.example.pegasus.domain

import kotlin.collections.filter

// ─── Trip ─────────────────────────────────────────────────────────────────────
// Represents a travel trip owned by a User (many to 1).
// Contains ItineraryItems (1 to *), stores Images (1 to *),
// and gets AIRecommendations (1 to *).
data class Trip(
    val id: String,
    val userId: String,
    val title: String,
    val destination: String,
    val startDate: Long,
    val endDate: Long,
    val budget: Double = 0.0,
    val status: String = "planned", // "planned", "ongoing", "completed"
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),

    // 1 to *
    val itineraryItems: List<ItineraryItem> = emptyList(),
    val images: List<Image> = emptyList(),
    val recommendations: List<AIRecommendation> = emptyList()
) {
    /**
     * Calculates remaining budget after all itinerary item costs.
     */
    fun getRemainingBudget(): Double {
        val totalActivityCost = itineraryItems.sumOf { it.price }
        return budget - totalActivityCost
    }

    /**
     * Returns the duration of the trip in days.
     */
    fun getDurationDays(): Long {
        val msPerDay = 86_400_000L
        return (endDate - startDate) / msPerDay
    }

    /**
     * Returns itinerary items filtered by type.
     */
    fun getItemsByType(type: String): List<ItineraryItem> {
        return itineraryItems.filter { it.type == type }
    }

    /**
     * Future feature: calculate optimization of daily spending.
     */
    fun optimizeBudgetDistribution() {
        // @TODO Implement smart budget distribution algorithm
    }

    /**
     * Future feature: share trip with other users.
     */
    fun shareTrip(): String {
        // @TODO Generate shareable link or export trip data
        return ""
    }

    /**
     * Future feature: calculate the carbon footprint of the trip.
     */
    fun getCarbonFootprint(): Double {
        // @TODO Implement carbon footprint calculation based on transport types
        return 0.0
    }
}
package com.example.pegasus.domain

// ─── ItineraryItem ────────────────────────────────────────────────────────────
// Represents a single item in a trip's itinerary. Belongs to one Trip (many to 1).
data class ItineraryItem(
    val id: String,
    val tripId: String,
    val title: String,
    val type: String,       // "flight", "hotel", "activity", "restaurant", "transport"
    val location: String,
    val datetime: Long,
    val price: Double = 0.0,
    val notes: String = "",
    val isConfirmed: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    /**
     * Returns a formatted date string for display.
     */
    fun getFormattedDate(): String {
        // @TODO Format datetime using user's locale from Preferences.preferredLanguage
        return datetime.toString()
    }

    /**
     * Returns whether this item is a transport-related type.
     */
    fun isTransport(): Boolean {
        return type == "flight" || type == "transport"
    }

    /**
     * Future feature: fetch real-time price updates for this item.
     */
    fun refreshPrice(): Double {
        // @TODO Call external API to get updated price
        return price
    }

    /**
     * Future feature: add this item to the device calendar.
     */
    fun addToCalendar() {
        // @TODO Implement calendar integration
    }
}
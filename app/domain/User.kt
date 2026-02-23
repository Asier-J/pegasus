package com.example.pegasus.domain

// ─── User ─────────────────────────────────────────────────────────────────────
// Represents the main user of the app.
// Owns multiple Trips (1 to *), has one UserPreferences (1 to 1),
// and manages one UserAuthentication (1 to 1).
data class User(
    val id: String,
    val email: String,
    val displayName: String,
    val photoUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis(),

    // 1 to 1
    val userPreferences: UserPreferences = UserPreferences(userId = id),
    val userAuthentication: UserAuthentication = UserAuthentication(userId = id),

    // 1 to *
    val trips: List<Trip> = emptyList()
) {
    /**
     * Returns a formatted string with the user's basic profile info.
     */
    fun getFullProfile(): String {
        return "$displayName ($email)"
    }

    /**
     * Returns the number of trips the user has planned.
     */
    fun getTripCount(): Int {
        return trips.size
    }

    /**
     * Future feature: aggregate travel statistics (total km, countries visited, etc.)
     */
    fun getTravelStats(): Map<String, Int> {
        // @TODO Implement stats aggregation across all trips
        return emptyMap()
    }

    /**
     * Future feature: export user data to a shareable format.
     */
    fun exportData(): String {
        // @TODO Implement data export (JSON or PDF)
        return ""
    }

    /**
     * Future feature: delete the user account and all associated data.
     */
    fun deleteAccount() {
        // @TODO Implement account deletion with cascade to trips, images, etc.
    }
}
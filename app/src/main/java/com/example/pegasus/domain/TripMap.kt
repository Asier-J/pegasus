package com.example.pegasus.domain

// ─── TripMap ──────────────────────────────────────────────────────────────────
// Handles map display and location logic.
// Shows locations for multiple Trips (* to *).
data class TripMap(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val zoomLevel: Int = 12,
    val markers: List<MapMarker> = emptyList()
) {
    /**
     * Centers the map on the given coordinates.
     */
    fun showLocation(latitude: Double, longitude: Double): TripMap {
        // @TODO Update map camera position to given coordinates
        return this.copy(latitude = latitude, longitude = longitude)
    }

    /**
     * Fetches nearby places of interest around the current map center.
     */
    fun getNearbyPlaces(): List<String> {
        // @TODO Call Places API to get nearby restaurants, hotels, attractions
        return emptyList()
    }

    /**
     * Centers the map on all locations of a given trip's itinerary items.
     */
    fun centerOnTrip(trip: Trip): TripMap {
        // @TODO Calculate bounding box of all trip locations and fit camera
        return this
    }

    /**
     * Adds a marker to the map at the given location.
     */
    fun addMarker(location: String, label: String): TripMap {
        // @TODO Geocode location string and add marker to map
        val newMarker = MapMarker(location = location, label = label)
        return this.copy(markers = markers + newMarker)
    }

    /**
     * Future feature: calculate route between two locations.
     */
    fun getRoute(origin: String, destination: String): List<MapMarker> {
        // @TODO Call Directions API and return list of waypoints
        return emptyList()
    }

    /**
     * Future feature: filter map markers by itinerary item type.
     */
    fun filterMarkersByType(type: String): TripMap {
        // @TODO Filter visible markers to show only given type (e.g. "hotel", "activity")
        return this
    }
}

// ─── MapMarker ────────────────────────────────────────────────────────────────
// Represents a pin on the map.
data class MapMarker(
    val id: String = java.util.UUID.randomUUID().toString(),
    val location: String,
    val label: String,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val type: String = "default"
)
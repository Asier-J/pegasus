package com.example.pegasus.ui.screens

import androidx.compose.ui.graphics.Color
import com.example.pegasus.R

// ─── Data Classes ─────────────────────────────────────────────────────────────
data class TripPhoto(val id: Int, val color: Color)

data class Trip(
    val id: Int,
    val nameResId: Int,
    val destinationResId: Int,
    val photos: List<TripPhoto>
) {
    val coverColor: Color get() = photos.firstOrNull()?.color ?: Color(0xFF1E3A5F)
}

// ─── Mock Data ────────────────────────────────────────────────────────────────
val mockPhotoTrips = listOf(
    Trip(
        id = 1,
        nameResId = R.string.trip_summer_japan,
        destinationResId = R.string.trip_japan_cities,
        photos = listOf(
            TripPhoto(1, Color(0xFF7B3F8C)),
            TripPhoto(2, Color(0xFF2E7D5A)),
            TripPhoto(3, Color(0xFFB85C2A)),
            TripPhoto(4, Color(0xFF3A6EA8)),
        )
    ),
    Trip(
        id = 2,
        nameResId = R.string.trip_balkan_road,
        destinationResId = R.string.trip_balkan_countries,
        photos = listOf(
            TripPhoto(1, Color(0xFF2E7D5A)),
            TripPhoto(2, Color(0xFF7B3F8C)),
        )
    ),
    Trip(
        id = 3,
        nameResId = R.string.trip_pyrenees_hiking,
        destinationResId = R.string.trip_pyrenees_locations,
        photos = listOf(
            TripPhoto(1, Color(0xFF3A6EA8)),
            TripPhoto(2, Color(0xFFB85C2A)),
            TripPhoto(3, Color(0xFF7B3F8C)),
        )
    )
)

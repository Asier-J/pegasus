package com.example.pegasus.domain

import android.net.Uri
import kotlinx.coroutines.flow.Flow

/**
 * Sprint 04 — Repository for trip gallery photos (T3).
 *
 * Adds copy the picked Uri into app-internal storage; deletes remove both the
 * Room row and the underlying file. The repository owns both sides so the
 * ViewModel never touches the file system directly.
 */
interface TripImageRepository {

    fun observeImagesForTrip(tripId: String): Flow<List<TripImage>>

    suspend fun addImage(tripId: String, sourceUri: Uri): TripImage

    suspend fun deleteImage(image: TripImage)

    suspend fun deleteAllForTrip(tripId: String)
}

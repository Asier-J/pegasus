package com.example.pegasus.data.repository

import android.net.Uri
import android.util.Log
import com.example.pegasus.data.local.ImageFileStorage
import com.example.pegasus.data.local.dao.TripImageDao
import com.example.pegasus.domain.TripImage
import com.example.pegasus.domain.TripImageRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Sprint 04 — Coordinates the Room table with internal-storage file ops.
 *
 * `addImage` copies the picked file first and only then inserts a row, so a
 * crash mid-write cannot leave an orphan DB row. `deleteImage` does it in the
 * opposite order (DB then file) so the UI immediately stops showing the photo.
 */
@Singleton
class TripImageRepositoryImpl @Inject constructor(
    private val dao: TripImageDao,
    private val storage: ImageFileStorage
) : TripImageRepository {

    override fun observeImagesForTrip(tripId: String): Flow<List<TripImage>> =
        dao.observeForTrip(tripId)

    override suspend fun addImage(tripId: String, sourceUri: Uri): TripImage {
        Log.d(TAG, "add image trip=$tripId, source=$sourceUri")
        val file  = storage.saveImageForTrip(tripId, sourceUri)
        val image = TripImage(tripId = tripId, localPath = file.absolutePath)
        dao.insert(image)
        Log.i(TAG, "added image id=${image.id} → ${image.localPath}")
        return image
    }

    override suspend fun deleteImage(image: TripImage) {
        Log.i(TAG, "delete image id=${image.id}")
        dao.deleteById(image.id)
        storage.deleteFile(image.localPath)
    }

    override suspend fun deleteAllForTrip(tripId: String) {
        Log.i(TAG, "delete all images for trip=$tripId")
        dao.deleteForTrip(tripId)
        storage.deleteAllForTrip(tripId)
    }

    companion object {
        private const val TAG = "TripImageRepository"
    }
}

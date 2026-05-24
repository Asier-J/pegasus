package com.example.pegasus.data.local

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Sprint 04 — copies images picked from the device into app-internal storage.
 *
 * Layout: `filesDir/trip_images/<tripId>/<random-uuid>.jpg`. Files survive app
 * restarts and are wiped on uninstall, which matches the lifetime users expect
 * for a "trip gallery" feature.
 */
@Singleton
class ImageFileStorage @Inject constructor(
    @ApplicationContext private val context: Context
) {

    /** Copies [sourceUri] into trip-scoped internal storage. Returns the new file. */
    suspend fun saveImageForTrip(tripId: String, sourceUri: Uri): File =
        withContext(Dispatchers.IO) {
            val tripDir = File(context.filesDir, "trip_images/$tripId").apply { mkdirs() }
            val outFile = File(tripDir, "${UUID.randomUUID()}.jpg")
            context.contentResolver.openInputStream(sourceUri).use { input ->
                requireNotNull(input) { "Cannot open $sourceUri" }
                outFile.outputStream().use { output -> input.copyTo(output) }
            }
            outFile
        }

    /** Deletes a previously-stored file by path. Safe to call on a missing file. */
    suspend fun deleteFile(absolutePath: String) = withContext(Dispatchers.IO) {
        runCatching { File(absolutePath).takeIf { it.exists() }?.delete() }
        Unit
    }

    /** Removes every photo for [tripId] (used on trip delete). */
    suspend fun deleteAllForTrip(tripId: String) = withContext(Dispatchers.IO) {
        runCatching {
            File(context.filesDir, "trip_images/$tripId").deleteRecursively()
        }
        Unit
    }
}

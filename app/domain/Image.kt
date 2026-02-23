package com.example.pegasus.domain

// ─── Image ────────────────────────────────────────────────────────────────────
// Represents a photo stored within a Trip (many to 1).
data class Image(
    val id: String,
    val tripId: String,
    val url: String,
    val caption: String = "",
    val location: String = "",
    val uploadedAt: Long = System.currentTimeMillis(),
    val sizeBytes: Long = 0L,
    val isFavorite: Boolean = false
) {
    /**
     * Returns a thumbnail version of the image URL.
     */
    fun getThumbnailUrl(): String {
        // @TODO Implement thumbnail URL generation (e.g. append ?w=200 for cloud storage)
        return url
    }

    /**
     * Future feature: upload image to cloud storage and return the URL.
     */
    fun upload(localPath: String): String {
        // @TODO Implement upload to Firebase Storage or equivalent
        return ""
    }

    /**
     * Future feature: delete image from cloud storage.
     */
    fun delete() {
        // @TODO Implement deletion from cloud storage and database
    }

    /**
     * Future feature: run AI analysis on the image to detect location or landmarks.
     */
    fun analyzeWithAI(): Map<String, String> {
        // @TODO Call vision AI API to extract tags, location, landmarks
        return emptyMap()
    }
}
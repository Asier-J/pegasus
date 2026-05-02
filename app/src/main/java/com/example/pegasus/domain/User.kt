package com.example.pegasus.domain

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey

// ─── User ─────────────────────────────────────────────────────────────────────
// Sprint 03: persisted in Room as the local mirror of the Firebase Auth account.
// `uid`        = Firebase Auth uid (PK).
// `username`   = unique inside the app (enforced via index + repository check).
// Extra profile fields requested in the lab: address, country, phone, acceptEmails.
@Entity(
    tableName = "users",
    indices = [Index(value = ["username"], unique = true)]
)
data class User(
    @PrimaryKey
    val uid: String,
    val email: String,
    val username: String,
    val displayName: String = username,
    val birthdate: String = "",          // dd/MM/yyyy
    val address: String = "",
    val country: String = "",
    val phone: String = "",
    val acceptEmails: Boolean = false,
    val photoUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    /** Display helper for Profile/About screens. */
    @Ignore
    fun getFullProfile(): String = "$displayName ($email)"
}

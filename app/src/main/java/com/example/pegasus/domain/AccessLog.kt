package com.example.pegasus.domain

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Sprint 03: audit log for application access events (login / logout).
 *
 * `userId` is stored as a plain Firebase uid string. We deliberately do NOT add a
 * foreign-key constraint to `users.uid` — audit rows must always be writable
 * regardless of whether the local user mirror has been created yet (e.g. accounts
 * registered directly in the Firebase Console). Otherwise a transient state could
 * crash the whole app on login/logout.
 */
@Entity(
    tableName = "access_logs",
    indices = [Index("userId")]
)
data class AccessLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val userId: String,
    val event: String,                       // "LOGIN" | "LOGOUT"
    val timestamp: Long = System.currentTimeMillis()
) {
    enum class Event { LOGIN, LOGOUT }

    companion object {
        const val EVENT_LOGIN = "LOGIN"
        const val EVENT_LOGOUT = "LOGOUT"
    }
}

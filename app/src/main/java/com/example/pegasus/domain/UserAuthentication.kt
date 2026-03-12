package com.example.pegasus.domain

// ─── Authentication ───────────────────────────────────────────────────────────
// Manages login/logout/password for a User. Belongs to one User (1 to 1).
data class UserAuthentication(
    val userId: String,
    val isLoggedIn: Boolean = false,
    val lastLoginAt: Long? = null,
    val provider: String = "email" // "email", "google", "apple"
) {
    /**
     * Logs the user in with email and password.
     * Returns an updated Authentication with isLoggedIn = true.
     */
    fun login(email: String, password: String): UserAuthentication {
        // @TODO Implement login with backend/Firebase Auth
        return this.copy(isLoggedIn = true, lastLoginAt = System.currentTimeMillis())
    }

    /**
     * Logs the user out.
     */
    fun logout(): UserAuthentication {
        // @TODO Implement logout and clear session token
        return this.copy(isLoggedIn = false)
    }

    /**
     * Sends a password reset email.
     */
    fun resetPassword(email: String) {
        // @TODO Implement password reset via backend/Firebase Auth
    }

    /**
     * Future feature: login with Google OAuth.
     */
    fun loginWithGoogle(): UserAuthentication {
        // @TODO Implement Google Sign-In
        return this.copy(isLoggedIn = true, provider = "google", lastLoginAt = System.currentTimeMillis())
    }

    /**
     * Future feature: update current password.
     */
    fun updatePassword(oldPassword: String, newPassword: String) {
        // @TODO Validate old password and update to new one
    }
}
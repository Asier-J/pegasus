package com.example.pegasus.domain

import kotlinx.coroutines.flow.Flow

/**
 * Sprint 03: Firebase Authentication façade.
 * Repository pattern keeps Firebase out of ViewModels and screens, so the rest of
 * the app stays test-friendly (we can fake the auth backend in unit tests).
 */
interface AuthRepository {

    data class AuthUser(
        val uid: String,
        val email: String?,
        val isEmailVerified: Boolean
    )

    /** Current logged-in user, or null. Hot stream — emits on auth state changes. */
    fun observeCurrentUser(): Flow<AuthUser?>

    fun currentUser(): AuthUser?

    suspend fun login(email: String, password: String): Result<AuthUser>

    /** Registers a new account and triggers the verification email. */
    suspend fun register(email: String, password: String): Result<AuthUser>

    suspend fun sendEmailVerification(): Result<Unit>

    suspend fun sendPasswordResetEmail(email: String): Result<Unit>

    suspend fun reloadCurrentUser(): Result<AuthUser?>

    fun logout()
}

package com.example.pegasus.data.repository

import android.util.Log
import com.example.pegasus.domain.AuthRepository
import com.example.pegasus.domain.AuthRepository.AuthUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Sprint 03: Firebase Authentication implementation.
 *
 * Uses `kotlinx-coroutines-play-services` `Task.await()` so we can write each
 * Firebase call as a clean `suspend` operation. Errors are wrapped in [Result]
 * so callers (the ViewModels) can pattern-match without try/catch noise.
 */
@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : AuthRepository {

    /** Hot stream of the current Firebase user. Emits `null` on logout. */
    override fun observeCurrentUser(): Flow<AuthUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser?.toAuthUser())
        }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }

    override fun currentUser(): AuthUser? = firebaseAuth.currentUser?.toAuthUser()

    override suspend fun login(email: String, password: String): Result<AuthUser> = runCatching {
        Log.d(TAG, "login: attempt for $email")
        val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
        val user = result.user ?: error("Firebase returned no user after sign-in")
        Log.i(TAG, "login: success for uid=${user.uid}")
        user.toAuthUser()
    }.onFailure { Log.e(TAG, "login: failed for $email", it) }

    override suspend fun register(email: String, password: String): Result<AuthUser> = runCatching {
        Log.d(TAG, "register: attempt for $email")
        val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
        val user = result.user ?: error("Firebase returned no user after sign-up")
        // Trigger verification email immediately. The UI can also resend it later.
        user.sendEmailVerification().await()
        Log.i(TAG, "register: success uid=${user.uid} — verification email sent")
        user.toAuthUser()
    }.onFailure { Log.e(TAG, "register: failed for $email", it) }

    override suspend fun sendEmailVerification(): Result<Unit> = runCatching {
        val user = firebaseAuth.currentUser ?: error("No current user")
        user.sendEmailVerification().await()
        Log.i(TAG, "sendEmailVerification: sent to ${user.email}")
        Unit
    }.onFailure { Log.e(TAG, "sendEmailVerification: failed", it) }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> = runCatching {
        firebaseAuth.sendPasswordResetEmail(email).await()
        Log.i(TAG, "sendPasswordResetEmail: sent to $email")
        Unit
    }.onFailure { Log.e(TAG, "sendPasswordResetEmail: failed for $email", it) }

    override suspend fun reloadCurrentUser(): Result<AuthUser?> = runCatching {
        val user = firebaseAuth.currentUser ?: return@runCatching null
        user.reload().await()
        firebaseAuth.currentUser?.toAuthUser()
    }.onFailure { Log.e(TAG, "reloadCurrentUser: failed", it) }

    override fun logout() {
        val uid = firebaseAuth.currentUser?.uid
        firebaseAuth.signOut()
        Log.i(TAG, "logout: signed out (was uid=$uid)")
    }

    // ── helpers ────────────────────────────────────────────────────────────────
    private fun FirebaseUser.toAuthUser(): AuthUser =
        AuthUser(uid = uid, email = email, isEmailVerified = isEmailVerified)

    private companion object { const val TAG = "AuthRepository" }
}

package com.example.pegasus.domain

import kotlinx.coroutines.flow.Flow

/**
 * Sprint 03: User profile persistence.
 * Wraps the local UserDao plus the access-log audit. Auth lives in AuthRepository.
 */
interface UserRepository {
    suspend fun getUser(uid: String): User?
    fun observeUser(uid: String): Flow<User?>
    suspend fun saveUser(user: User): Result<Unit>
    suspend fun updateUser(user: User): Result<Unit>
    suspend fun isUsernameTakenByOther(username: String, excludeUid: String): Boolean

    /** Records an application-access event. */
    suspend fun logAccess(userId: String, event: String)
    fun observeAccessLogs(userId: String): Flow<List<AccessLog>>
}

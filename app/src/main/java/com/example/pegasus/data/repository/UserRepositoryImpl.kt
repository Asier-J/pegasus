package com.example.pegasus.data.repository

import android.util.Log
import com.example.pegasus.data.local.dao.AccessLogDao
import com.example.pegasus.data.local.dao.UserDao
import com.example.pegasus.domain.AccessLog
import com.example.pegasus.domain.User
import com.example.pegasus.domain.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val accessLogDao: AccessLogDao
) : UserRepository {

    override suspend fun getUser(uid: String): User? = userDao.getByUid(uid)

    override fun observeUser(uid: String): Flow<User?> = userDao.observeByUid(uid)

    override suspend fun saveUser(user: User): Result<Unit> = runCatching {
        if (userDao.isUsernameTakenByOther(user.username, user.uid)) {
            Log.w(TAG, "saveUser: username '${user.username}' already taken")
            error("Username '${user.username}' is already in use")
        }
        // Sprint 04 hotfix: `saveUser` now upserts instead of plain insert. The
        // Sprint 04 AuthViewModel `init` block races against the Register flow
        // and may have already inserted a stub row via `ensureLocalProfile`; a
        // second `insert` for the same uid would otherwise crash with
        // SQLITE_CONSTRAINT_PRIMARYKEY (1555).
        val existing = userDao.getByUid(user.uid)
        if (existing != null) {
            userDao.update(user)
            Log.i(TAG, "saveUser: upserted profile for uid=${user.uid}")
        } else {
            userDao.insert(user)
            Log.i(TAG, "saveUser: stored profile for uid=${user.uid}")
        }
    }

    override suspend fun updateUser(user: User): Result<Unit> = runCatching {
        if (userDao.isUsernameTakenByOther(user.username, user.uid)) {
            Log.w(TAG, "updateUser: username '${user.username}' already taken")
            error("Username '${user.username}' is already in use")
        }
        userDao.update(user)
        Log.i(TAG, "updateUser: updated profile for uid=${user.uid}")
    }

    override suspend fun isUsernameTakenByOther(username: String, excludeUid: String): Boolean =
        userDao.isUsernameTakenByOther(username, excludeUid)

    override suspend fun logAccess(userId: String, event: String) {
        // Defensive: any DB error here must NEVER propagate up and crash the app
        // — audit logging is best-effort, and login/logout should always succeed.
        runCatching {
            val id = accessLogDao.insert(AccessLog(userId = userId, event = event))
            Log.i(TAG, "logAccess: [$event] for user=$userId (logId=$id)")
        }.onFailure { Log.e(TAG, "logAccess failed for user=$userId event=$event", it) }
    }

    override fun observeAccessLogs(userId: String): Flow<List<AccessLog>> =
        accessLogDao.observeForUser(userId)

    private companion object {
        const val TAG = "UserRepository"
    }
}

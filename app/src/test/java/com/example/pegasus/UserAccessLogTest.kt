package com.example.pegasus

import android.os.Build
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.pegasus.data.local.AppDatabase
import com.example.pegasus.data.repository.UserRepositoryImpl
import com.example.pegasus.domain.AccessLog
import com.example.pegasus.domain.User
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Sprint 03 — T4.1 / T4.4 / T5.1:
 * Tests user persistence (with unique-username constraint) and the access log.
 *
 * Coverage:
 *  - User CRUD (insert / update / delete / observe)
 *  - Unique username — at insert (saveUser), at update (updateUser),
 *    self-edit allowed, cross-user collision rejected
 *  - findByUsername / getByUid
 *  - Access log: insert, scoped per-user, sorted DESC by timestamp,
 *    countForUser, defensive logAccess (never crashes the caller),
 *    survives missing referenced user (no FK in v2)
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P])
class UserAccessLogTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: UserRepositoryImpl

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        repository = UserRepositoryImpl(db.userDao(), db.accessLogDao())
    }

    @After fun tearDown() { db.close() }

    // ── User persistence ───────────────────────────────────────────────────────
    @Test
    fun saveUser_persistsAllFields() = runTest {
        val user = User(
            uid = "uid-1",
            email = "alice@example.com",
            username = "alice",
            displayName = "Alice",
            birthdate = "01/01/2000",
            address = "Main 123",
            country = "ES",
            phone = "+34 600 000 000",
            acceptEmails = true
        )
        val r = repository.saveUser(user)
        assertTrue(r.isSuccess)
        val stored = repository.getUser("uid-1")
        assertNotNull(stored)
        assertEquals("alice@example.com", stored!!.email)
        assertEquals("alice", stored.username)
        assertEquals("ES", stored.country)
        assertEquals("Alice", stored.displayName)
        assertEquals("01/01/2000", stored.birthdate)
        assertEquals("Main 123", stored.address)
        assertEquals("+34 600 000 000", stored.phone)
        assertTrue(stored.acceptEmails)
        assertTrue(stored.createdAt > 0)
    }

    @Test
    fun saveUser_rejectsDuplicateUsername() = runTest {
        repository.saveUser(User(uid = "u1", email = "a@x.com", username = "alice"))
        val r = repository.saveUser(User(uid = "u2", email = "b@x.com", username = "alice"))
        assertTrue(r.isFailure)
    }

    @Test
    fun getUser_returnsNullForUnknown() = runTest {
        assertNull(repository.getUser("nope"))
    }

    @Test
    fun observeUser_emitsOnInsert() = runTest {
        // Initially null (no row).
        assertNull(repository.observeUser("uid-obs").first())

        repository.saveUser(User(uid = "uid-obs", email = "o@x.com", username = "obs"))

        val observed = repository.observeUser("uid-obs").first()
        assertNotNull(observed)
        assertEquals("obs", observed!!.username)
    }

    // ── Update + uniqueness on edit ───────────────────────────────────────────
    @Test
    fun updateUser_keepingSameUsername_succeeds() = runTest {
        repository.saveUser(User(uid = "u1", email = "a@x.com", username = "alice", displayName = "A"))
        // Edit some other field but keep the same username — must NOT trip the uniqueness check
        // since `excludeUid = self`.
        val r = repository.updateUser(User(uid = "u1", email = "a@x.com", username = "alice", displayName = "Alice Edited"))
        assertTrue(r.isSuccess)
        assertEquals("Alice Edited", repository.getUser("u1")?.displayName)
    }

    @Test
    fun updateUser_changingToOtherUsersUsername_fails() = runTest {
        repository.saveUser(User(uid = "u1", email = "a@x.com", username = "alice"))
        repository.saveUser(User(uid = "u2", email = "b@x.com", username = "bob"))
        val r = repository.updateUser(User(uid = "u2", email = "b@x.com", username = "alice"))
        assertTrue(r.isFailure)
    }

    @Test
    fun isUsernameTakenByOther_isCorrect() = runTest {
        repository.saveUser(User(uid = "u1", email = "a@x.com", username = "alice"))
        assertTrue(repository.isUsernameTakenByOther("alice", excludeUid = "u2"))
        // Same uid editing its own username is allowed.
        assertFalse(repository.isUsernameTakenByOther("alice", excludeUid = "u1"))
        // Unknown username is free.
        assertFalse(repository.isUsernameTakenByOther("totally-new", excludeUid = ""))
    }

    @Test
    fun delete_removesUserRow() = runTest {
        repository.saveUser(User(uid = "u1", email = "a@x.com", username = "alice"))
        // Delete via DAO directly (the repository doesn't expose delete in domain interface).
        db.userDao().delete("u1")
        assertNull(repository.getUser("u1"))
    }

    // ── Access log (T4.4) ─────────────────────────────────────────────────────
    @Test
    fun logAccess_recordsLoginAndLogout() = runTest {
        repository.saveUser(User(uid = "u1", email = "a@x.com", username = "alice"))
        repository.logAccess("u1", AccessLog.EVENT_LOGIN)
        repository.logAccess("u1", AccessLog.EVENT_LOGOUT)

        val logs = repository.observeAccessLogs("u1").first()
        assertEquals(2, logs.size)
        val events = logs.map { it.event }.toSet()
        assertTrue(AccessLog.EVENT_LOGIN in events)
        assertTrue(AccessLog.EVENT_LOGOUT in events)
    }

    @Test
    fun logAccess_isOrderedByTimestampDesc() = runTest {
        repository.saveUser(User(uid = "u1", email = "a@x.com", username = "alice"))
        // Insert with a small delay so timestamps differ at ms precision.
        repository.logAccess("u1", AccessLog.EVENT_LOGIN)
        Thread.sleep(5)
        repository.logAccess("u1", AccessLog.EVENT_LOGOUT)

        val logs = repository.observeAccessLogs("u1").first()
        // Newest first → LOGOUT before LOGIN.
        assertEquals(AccessLog.EVENT_LOGOUT, logs[0].event)
        assertEquals(AccessLog.EVENT_LOGIN,  logs[1].event)
        assertTrue(logs[0].timestamp >= logs[1].timestamp)
    }

    @Test
    fun logAccess_isScopedPerUser() = runTest {
        repository.saveUser(User(uid = "u1", email = "a@x.com", username = "alice"))
        repository.saveUser(User(uid = "u2", email = "b@x.com", username = "bob"))
        repository.logAccess("u1", AccessLog.EVENT_LOGIN)
        repository.logAccess("u1", AccessLog.EVENT_LOGOUT)
        repository.logAccess("u2", AccessLog.EVENT_LOGIN)

        val u1Logs = repository.observeAccessLogs("u1").first()
        val u2Logs = repository.observeAccessLogs("u2").first()
        assertEquals(2, u1Logs.size)
        assertEquals(1, u2Logs.size)
        assertTrue(u1Logs.all { it.userId == "u1" })
        assertTrue(u2Logs.all { it.userId == "u2" })
    }

    @Test
    fun countForUser_isCorrect() = runTest {
        repository.saveUser(User(uid = "u1", email = "a@x.com", username = "alice"))
        repeat(3) { repository.logAccess("u1", AccessLog.EVENT_LOGIN) }
        val count = db.accessLogDao().countForUser("u1")
        assertEquals(3, count)
    }

    @Test
    fun logAccess_worksEvenIfUserMirrorIsMissing() = runTest {
        // T4.4 + Sprint 03 hotfix: AccessLog has NO foreign key to users.uid in v2.
        // So a Firebase account that has not yet been mirrored locally can still log
        // a LOGIN row without crashing.
        repository.logAccess("uid-not-in-users-table", AccessLog.EVENT_LOGIN)
        val logs = repository.observeAccessLogs("uid-not-in-users-table").first()
        assertEquals(1, logs.size)
        assertEquals(AccessLog.EVENT_LOGIN, logs[0].event)
    }

    @Test
    fun logAccess_neverThrows_evenIfDbErrors() = runTest {
        // Repository wraps the DAO insert in runCatching. Calling logAccess
        // after closing the DB MUST NOT propagate the exception.
        db.close()
        // The next line would crash if the defensive runCatching weren't in place.
        repository.logAccess("u1", AccessLog.EVENT_LOGIN)
        // If we reach here, the contract is honored.
        assertTrue(true)
    }

    @Test
    fun observeAccessLogs_emptyForUnknownUser() = runTest {
        val logs = repository.observeAccessLogs("ghost").first()
        assertTrue(logs.isEmpty())
    }

    // ── DAO-level methods not exposed by repository ───────────────────────────
    @Test
    fun findByUsername_returnsRowWhenExists() = runTest {
        repository.saveUser(User(uid = "u1", email = "a@x.com", username = "alice"))
        val found = db.userDao().findByUsername("alice")
        assertNotNull(found)
        assertEquals("u1", found!!.uid)
    }

    @Test
    fun findByUsername_returnsNullWhenAbsent() = runTest {
        assertNull(db.userDao().findByUsername("ghost"))
    }

    @Test
    fun accessLog_getAll_returnsEveryUsersRows() = runTest {
        repository.saveUser(User(uid = "u1", email = "a@x.com", username = "alice"))
        repository.saveUser(User(uid = "u2", email = "b@x.com", username = "bob"))
        repository.logAccess("u1", AccessLog.EVENT_LOGIN)
        repository.logAccess("u2", AccessLog.EVENT_LOGIN)
        repository.logAccess("u2", AccessLog.EVENT_LOGOUT)

        val all = db.accessLogDao().getAll()
        assertEquals(3, all.size)
        // Sorted DESC by timestamp.
        assertTrue(all[0].timestamp >= all[1].timestamp)
        assertTrue(all[1].timestamp >= all[2].timestamp)
    }
}

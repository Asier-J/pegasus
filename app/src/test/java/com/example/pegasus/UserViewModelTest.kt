package com.example.pegasus

import com.example.pegasus.domain.AccessLog
import com.example.pegasus.domain.AuthRepository
import com.example.pegasus.domain.User
import com.example.pegasus.domain.UserRepository
import com.example.pegasus.ui.viewmodels.UserViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Sprint 03 — T4.1 / T4.4: Tests for UserViewModel which exposes:
 *  - `currentProfile`: hot Flow of the logged-in user's local profile
 *  - `accessLogs`:    hot Flow of that user's login/logout audit rows
 *  - `updateProfile()`: writes through the repository, surfacing failures
 *
 * Coverage:
 *  - When logged out, both flows emit null/empty
 *  - When logged in, flows are wired to the corresponding `userRepository` Flow
 *  - updateProfile success → repository called, no error message
 *  - updateProfile failure → error message surfaced
 *  - clearError empties the error stream
 */
@OptIn(ExperimentalCoroutinesApi::class)
class UserViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var users: UserRepository
    private lateinit var auth: AuthRepository

    private val uid = "uid-9"
    private val authUser = AuthRepository.AuthUser(uid, "demo@p.app", true)
    private val sampleProfile = User(
        uid = uid, email = "demo@p.app", username = "demo",
        displayName = "Demo", birthdate = "01/01/2000",
        address = "X 1", country = "ES", phone = "+34", acceptEmails = true
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        users = mock {
            onBlocking { observeUser(any()) } doReturn flowOf(null)
            onBlocking { observeAccessLogs(any()) } doReturn flowOf(emptyList())
            onBlocking { updateUser(any()) } doReturn Result.success(Unit)
        }
        auth = mock {
            on { observeCurrentUser() } doReturn flowOf(null)
        }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ── currentProfile flow ───────────────────────────────────────────────────
    @Test
    fun currentProfile_loggedOut_emitsNull() = runTest(testDispatcher) {
        whenever(auth.observeCurrentUser()).thenReturn(flowOf(null))
        val vm = UserViewModel(users, auth)
        advanceUntilIdle()
        assertNull(vm.currentProfile.value)
    }

    @Test
    fun currentProfile_loggedIn_emitsRepositoryProfile() = runTest(testDispatcher) {
        whenever(auth.observeCurrentUser()).thenReturn(flowOf(authUser))
        whenever(users.observeUser(uid)).thenReturn(flowOf(sampleProfile))

        val vm = UserViewModel(users, auth)
        advanceUntilIdle()

        assertEquals(sampleProfile, vm.currentProfile.value)
    }

    @Test
    fun currentProfile_switchesWhenAuthStateChanges() = runTest(testDispatcher) {
        // Auth state moves: logged out → logged in
        val authFlow = MutableStateFlow<AuthRepository.AuthUser?>(null)
        whenever(auth.observeCurrentUser()).thenReturn(authFlow)
        whenever(users.observeUser(uid)).thenReturn(flowOf(sampleProfile))

        val vm = UserViewModel(users, auth)
        advanceUntilIdle()
        assertNull(vm.currentProfile.value)

        // Now log in
        authFlow.value = authUser
        advanceUntilIdle()
        assertEquals(sampleProfile, vm.currentProfile.value)
    }

    // ── accessLogs flow ───────────────────────────────────────────────────────
    @Test
    fun accessLogs_loggedOut_isEmpty() = runTest(testDispatcher) {
        whenever(auth.observeCurrentUser()).thenReturn(flowOf(null))
        val vm = UserViewModel(users, auth)
        advanceUntilIdle()
        assertTrue(vm.accessLogs.value.isEmpty())
    }

    @Test
    fun accessLogs_loggedIn_emitsRepositoryLogs() = runTest(testDispatcher) {
        val sampleLogs = listOf(
            AccessLog(id = 1, userId = uid, event = AccessLog.EVENT_LOGIN, timestamp = 1_000L),
            AccessLog(id = 2, userId = uid, event = AccessLog.EVENT_LOGOUT, timestamp = 2_000L)
        )
        whenever(auth.observeCurrentUser()).thenReturn(flowOf(authUser))
        whenever(users.observeAccessLogs(uid)).thenReturn(flowOf(sampleLogs))

        val vm = UserViewModel(users, auth)
        // `accessLogs` is `WhileSubscribed(5s)` so we must have an active collector
        // for it to start emitting. Launch one in the runTest scope; cancel after.
        val collected = mutableListOf<List<AccessLog>>()
        val job = launch { vm.accessLogs.collect { collected.add(it) } }
        advanceUntilIdle()

        assertTrue(collected.isNotEmpty())
        assertTrue(collected.last().any { it.event == AccessLog.EVENT_LOGIN })
        assertTrue(collected.last().any { it.event == AccessLog.EVENT_LOGOUT })
        job.cancel()
    }

    // ── updateProfile ────────────────────────────────────────────────────────
    @Test
    fun updateProfile_success_clearsErrorAndCallsRepo() = runTest(testDispatcher) {
        whenever(users.updateUser(sampleProfile)).thenReturn(Result.success(Unit))
        val vm = UserViewModel(users, auth)
        vm.updateProfile(sampleProfile)
        advanceUntilIdle()

        verify(users).updateUser(sampleProfile)
        assertNull(vm.errorMessage.value)
    }

    @Test
    fun updateProfile_failure_emitsErrorMessage() = runTest(testDispatcher) {
        whenever(users.updateUser(any()))
            .thenReturn(Result.failure(IllegalStateException("Username 'x' is already in use")))

        val vm = UserViewModel(users, auth)
        vm.updateProfile(sampleProfile.copy(username = "x"))
        advanceUntilIdle()

        assertEquals("Username 'x' is already in use", vm.errorMessage.value)
    }

    @Test
    fun clearError_emptiesErrorMessage() = runTest(testDispatcher) {
        whenever(users.updateUser(any()))
            .thenReturn(Result.failure(RuntimeException("boom")))
        val vm = UserViewModel(users, auth)
        vm.updateProfile(sampleProfile)
        advanceUntilIdle()
        assertNotNull(vm.errorMessage.value)

        vm.clearError()
        assertNull(vm.errorMessage.value)
    }
}

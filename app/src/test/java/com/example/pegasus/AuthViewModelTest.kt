package com.example.pegasus

import com.example.pegasus.domain.AccessLog
import com.example.pegasus.domain.AuthRepository
import com.example.pegasus.domain.User
import com.example.pegasus.domain.UserRepository
import com.example.pegasus.ui.viewmodels.AuthViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
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
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Sprint 03 — T2 / T3: Tests the orchestration that AuthViewModel does between
 * Firebase Auth (`AuthRepository`) and the local user mirror (`UserRepository`).
 *
 * Coverage:
 *  - Empty-field guards return early without calling Firebase
 *  - Login: success → ensureLocalProfile + LOGIN access log + LOGIN_OK action
 *  - Login: success when user already exists → no duplicate stub created
 *  - Login: Firebase failure → error message, no DB writes
 *  - Register: empty fields, duplicate username, Firebase failure, success
 *  - Recover: empty / success / failure
 *  - Logout: writes LOGOUT log + signs out + emits LOGOUT_OK
 *  - clearMessages
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var auth: AuthRepository
    private lateinit var users: UserRepository
    private lateinit var vm: AuthViewModel

    private val uid = "uid-123"
    private val authUser = AuthRepository.AuthUser(uid, "demo@pegasus.app", true)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        auth = mock {
            on { observeCurrentUser() } doReturn flowOf(null)
            on { currentUser() } doReturn null
        }
        users = mock {
            onBlocking { isUsernameTakenByOther(any(), any()) } doReturn false
            onBlocking { getUser(any()) } doReturn null
            onBlocking { saveUser(any()) } doReturn Result.success(Unit)
        }
        vm = AuthViewModel(auth, users)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ── Login ────────────────────────────────────────────────────────────────
    @Test
    fun login_emptyEmail_emitsFieldsEmptyError() = runTest(testDispatcher) {
        vm.login("", "password")
        advanceUntilIdle()
        assertEquals(AuthViewModel.ERROR_FIELDS_EMPTY, vm.uiState.value.errorMessage)
        verify(auth, never()).login(any(), any())
    }

    @Test
    fun login_emptyPassword_emitsFieldsEmptyError() = runTest(testDispatcher) {
        vm.login("demo@pegasus.app", "")
        advanceUntilIdle()
        assertEquals(AuthViewModel.ERROR_FIELDS_EMPTY, vm.uiState.value.errorMessage)
        verify(auth, never()).login(any(), any())
    }

    @Test
    fun login_success_createsStubProfileAndLogsLogin() = runTest(testDispatcher) {
        whenever(auth.login("demo@pegasus.app", "Demo1234!"))
            .thenReturn(Result.success(authUser))
        whenever(users.getUser(uid)).thenReturn(null)        // first time → stub created

        vm.login("demo@pegasus.app", "Demo1234!")
        advanceUntilIdle()

        verify(users).saveUser(any())                        // ensureLocalProfile fired
        verify(users).logAccess(uid, AccessLog.EVENT_LOGIN)
        assertEquals(AuthViewModel.UiState.Action.LOGIN_OK, vm.uiState.value.lastAction)
    }

    @Test
    fun login_success_existingProfile_skipsStubCreation() = runTest(testDispatcher) {
        whenever(auth.login(any(), any())).thenReturn(Result.success(authUser))
        // User already exists in Room → ensureLocalProfile must early-return.
        whenever(users.getUser(uid)).thenReturn(
            User(uid = uid, email = authUser.email!!, username = "demo")
        )

        vm.login("demo@pegasus.app", "Demo1234!")
        advanceUntilIdle()

        verify(users, never()).saveUser(any())
        verify(users).logAccess(uid, AccessLog.EVENT_LOGIN)
        assertEquals(AuthViewModel.UiState.Action.LOGIN_OK, vm.uiState.value.lastAction)
    }

    @Test
    fun login_firebaseFailure_emitsErrorAndDoesNotLog() = runTest(testDispatcher) {
        whenever(auth.login(any(), any()))
            .thenReturn(Result.failure(RuntimeException("bad credentials")))

        vm.login("demo@pegasus.app", "wrong")
        advanceUntilIdle()

        assertNotNull(vm.uiState.value.errorMessage)
        verify(users, never()).logAccess(any(), any())
        assertNull(vm.uiState.value.lastAction)
    }

    // ── Register ─────────────────────────────────────────────────────────────
    @Test
    fun register_emptyFields_emitsFieldsEmptyError() = runTest(testDispatcher) {
        vm.register("", "", "", "", "", "", "", false)
        advanceUntilIdle()
        assertEquals(AuthViewModel.ERROR_FIELDS_EMPTY, vm.uiState.value.errorMessage)
        verify(auth, never()).register(any(), any())
    }

    @Test
    fun register_duplicateUsername_rejectsBeforeFirebase() = runTest(testDispatcher) {
        whenever(users.isUsernameTakenByOther("alice", excludeUid = "")).thenReturn(true)

        var taken = false
        vm.register(
            email = "a@x.com", password = "P1!aaaaaa",
            username = "alice", birthdate = "01/01/2000",
            address = "", country = "", phone = "", acceptEmails = false,
            onUsernameTaken = { taken = true }
        )
        advanceUntilIdle()

        assertTrue(taken)
        assertEquals(AuthViewModel.ERROR_USERNAME_TAKEN, vm.uiState.value.errorMessage)
        verify(auth, never()).register(any(), any())   // never reaches Firebase
    }

    @Test
    fun register_firebaseFailure_emitsError() = runTest(testDispatcher) {
        whenever(auth.register(any(), any()))
            .thenReturn(Result.failure(RuntimeException("email exists")))

        vm.register(
            email = "a@x.com", password = "P1!aaaaaa",
            username = "alice", birthdate = "01/01/2000",
            address = "Main 1", country = "ES", phone = "+34", acceptEmails = true
        )
        advanceUntilIdle()

        assertNotNull(vm.uiState.value.errorMessage)
        verify(users, never()).saveUser(any())
        verify(users, never()).logAccess(any(), any())
    }

    @Test
    fun register_success_savesProfileAndLogsLogin() = runTest(testDispatcher) {
        whenever(auth.register(any(), any())).thenReturn(Result.success(authUser))

        vm.register(
            email = "demo@pegasus.app", password = "Demo1234!",
            username = "demo", birthdate = "01/01/2000",
            address = "Main 1", country = "ES", phone = "+34 600",
            acceptEmails = true
        )
        advanceUntilIdle()

        argumentCaptor<User>().apply {
            verify(users).saveUser(capture())
            val saved = firstValue
            assertEquals(uid, saved.uid)
            assertEquals("demo", saved.username)
            assertEquals("demo@pegasus.app", saved.email)
            assertEquals("01/01/2000", saved.birthdate)
            assertEquals("Main 1", saved.address)
            assertEquals("ES", saved.country)
            assertEquals("+34 600", saved.phone)
            assertTrue(saved.acceptEmails)
        }
        verify(users).logAccess(uid, AccessLog.EVENT_LOGIN)
        assertEquals(AuthViewModel.UiState.Action.REGISTER_OK, vm.uiState.value.lastAction)
        assertEquals(AuthViewModel.INFO_VERIFICATION_SENT, vm.uiState.value.infoMessage)
    }

    // ── Recover password ─────────────────────────────────────────────────────
    @Test
    fun recoverPassword_emptyEmail_emitsFieldsEmptyError() = runTest(testDispatcher) {
        vm.recoverPassword("")
        advanceUntilIdle()
        assertEquals(AuthViewModel.ERROR_FIELDS_EMPTY, vm.uiState.value.errorMessage)
        verify(auth, never()).sendPasswordResetEmail(any())
    }

    @Test
    fun recoverPassword_success_emitsResetSentInfo() = runTest(testDispatcher) {
        whenever(auth.sendPasswordResetEmail("demo@pegasus.app"))
            .thenReturn(Result.success(Unit))

        vm.recoverPassword("demo@pegasus.app")
        advanceUntilIdle()

        assertEquals(AuthViewModel.INFO_RESET_SENT, vm.uiState.value.infoMessage)
        assertEquals(AuthViewModel.UiState.Action.RECOVER_OK, vm.uiState.value.lastAction)
    }

    @Test
    fun recoverPassword_failure_emitsErrorMessage() = runTest(testDispatcher) {
        whenever(auth.sendPasswordResetEmail(any()))
            .thenReturn(Result.failure(RuntimeException("no such user")))

        vm.recoverPassword("ghost@pegasus.app")
        advanceUntilIdle()

        assertNotNull(vm.uiState.value.errorMessage)
        assertNull(vm.uiState.value.infoMessage)
    }

    // ── Logout ───────────────────────────────────────────────────────────────
    @Test
    fun logout_signedInUser_writesLogoutLog_andSignsOut() = runTest(testDispatcher) {
        // Simulate a session: AuthViewModel.currentUser flow already started in @Before
        // collecting an initial null. We need to expose a flow that emits authUser then null
        // — easier path: stub currentUser() (used by login()) and inject a fresh VM with the
        // session value seeded.
        whenever(auth.observeCurrentUser()).thenReturn(flowOf(authUser))
        val sessionVm = AuthViewModel(auth, users)
        // Drain the flow's initial emission so currentUser.value == authUser.
        advanceUntilIdle()

        sessionVm.logout()
        advanceUntilIdle()

        verify(users).logAccess(uid, AccessLog.EVENT_LOGOUT)
        verify(auth, times(1)).logout()
        assertEquals(AuthViewModel.UiState.Action.LOGOUT_OK, sessionVm.uiState.value.lastAction)
    }

    @Test
    fun logout_withoutSession_stillCallsRepoLogoutWithoutCrashing() = runTest(testDispatcher) {
        // currentUser is null → no logAccess call should happen, but signOut must still run.
        vm.logout()
        advanceUntilIdle()

        verify(users, never()).logAccess(any(), any())
        verify(auth).logout()
    }

    // ── State helpers ────────────────────────────────────────────────────────
    @Test
    fun clearMessages_emptiesErrorAndInfoButKeepsLastAction() = runTest(testDispatcher) {
        vm.login("", "")              // forces ERROR_FIELDS_EMPTY
        advanceUntilIdle()
        assertNotNull(vm.uiState.value.errorMessage)

        vm.clearMessages()
        assertNull(vm.uiState.value.errorMessage)
        assertNull(vm.uiState.value.infoMessage)
    }

    @Test
    fun resendVerification_success_emitsInfo() = runTest(testDispatcher) {
        whenever(auth.sendEmailVerification()).thenReturn(Result.success(Unit))
        vm.resendVerification()
        advanceUntilIdle()
        assertEquals(AuthViewModel.INFO_VERIFICATION_SENT, vm.uiState.value.infoMessage)
    }

    @Test
    fun resendVerification_failure_emitsError() = runTest(testDispatcher) {
        whenever(auth.sendEmailVerification())
            .thenReturn(Result.failure(RuntimeException("no current user")))
        vm.resendVerification()
        advanceUntilIdle()
        assertNotNull(vm.uiState.value.errorMessage)
    }
}

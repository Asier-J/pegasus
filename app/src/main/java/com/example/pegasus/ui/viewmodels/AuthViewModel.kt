package com.example.pegasus.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pegasus.domain.AccessLog
import com.example.pegasus.domain.AuthRepository
import com.example.pegasus.domain.User
import com.example.pegasus.domain.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Sprint 03: Authentication state holder for the UI layer.
 *
 * Coordinates Firebase Auth + the local User table:
 *  - login   → records LOGIN access log
 *  - register→ creates Firebase user, sends verification email, then writes the
 *              extended profile into the local DB
 *  - logout  → records LOGOUT access log, then signs out Firebase
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    /** Hot stream of the current Firebase session (null when logged out). */
    val currentUser: StateFlow<AuthRepository.AuthUser?> =
        authRepository.observeCurrentUser().stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = authRepository.currentUser()
        )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    data class UiState(
        val isLoading: Boolean = false,
        val errorMessage: String? = null,
        val infoMessage: String? = null,
        val lastAction: Action? = null
    ) {
        enum class Action { LOGIN_OK, REGISTER_OK, RECOVER_OK, LOGOUT_OK }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, infoMessage = null)
    }

    // ── Login ──────────────────────────────────────────────────────────────────
    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = ERROR_FIELDS_EMPTY)
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            authRepository.login(email.trim(), password)
                .onSuccess { user ->
                    // Sprint 03 hotfix: ensure a local User row exists before writing
                    // the access log (FK constraint) or any future trip (FK constraint).
                    // This handles Firebase users created outside the app (e.g. via the
                    // Firebase Console) the first time they log in.
                    ensureLocalProfile(user)
                    userRepository.logAccess(user.uid, AccessLog.EVENT_LOGIN)
                    Log.i(TAG, "login OK uid=${user.uid}")
                    _uiState.value = UiState(lastAction = UiState.Action.LOGIN_OK)
                }
                .onFailure {
                    Log.e(TAG, "login failed", it)
                    _uiState.value = UiState(errorMessage = it.localizedMessage ?: ERROR_LOGIN_GENERIC)
                }
        }
    }

    /**
     * If the Firebase user has no row yet in the local Room `users` table, insert a
     * minimal stub so foreign-keyed tables (access_logs, trips) can reference it.
     * Username is derived from the email's local part with a uid suffix to avoid
     * unique-index collisions.
     */
    private suspend fun ensureLocalProfile(user: AuthRepository.AuthUser) {
        val existing = userRepository.getUser(user.uid)
        if (existing != null) return

        val emailPrefix = user.email?.substringBefore("@")?.takeIf { it.isNotBlank() } ?: "user"
        val candidate = emailPrefix
        val finalUsername =
            if (userRepository.isUsernameTakenByOther(candidate, user.uid))
                "${candidate}_${user.uid.take(4)}"
            else candidate

        userRepository.saveUser(
            com.example.pegasus.domain.User(
                uid = user.uid,
                email = user.email ?: "",
                username = finalUsername,
                displayName = emailPrefix
            )
        ).onFailure { Log.w(TAG, "ensureLocalProfile: stub creation failed", it) }
    }

    // ── Register ───────────────────────────────────────────────────────────────
    fun register(
        email: String,
        password: String,
        username: String,
        birthdate: String,
        address: String,
        country: String,
        phone: String,
        acceptEmails: Boolean,
        onUsernameTaken: () -> Unit = {}
    ) {
        if (email.isBlank() || password.isBlank() || username.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = ERROR_FIELDS_EMPTY)
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            // Username uniqueness check before hitting Firebase to avoid orphan accounts.
            if (userRepository.isUsernameTakenByOther(username, excludeUid = "")) {
                _uiState.value = UiState(errorMessage = ERROR_USERNAME_TAKEN)
                onUsernameTaken()
                return@launch
            }

            authRepository.register(email.trim(), password)
                .onSuccess { authUser ->
                    val profile = User(
                        uid = authUser.uid,
                        email = authUser.email ?: email.trim(),
                        username = username.trim(),
                        displayName = username.trim(),
                        birthdate = birthdate,
                        address = address.trim(),
                        country = country.trim(),
                        phone = phone.trim(),
                        acceptEmails = acceptEmails
                    )
                    userRepository.saveUser(profile)
                        .onSuccess {
                            userRepository.logAccess(authUser.uid, AccessLog.EVENT_LOGIN)
                            Log.i(TAG, "register OK uid=${authUser.uid}")
                            _uiState.value = UiState(
                                infoMessage = INFO_VERIFICATION_SENT,
                                lastAction = UiState.Action.REGISTER_OK
                            )
                        }
                        .onFailure {
                            Log.e(TAG, "register: failed to save profile", it)
                            _uiState.value = UiState(errorMessage = it.message ?: ERROR_REGISTER_GENERIC)
                        }
                }
                .onFailure {
                    Log.e(TAG, "register failed", it)
                    _uiState.value = UiState(errorMessage = it.localizedMessage ?: ERROR_REGISTER_GENERIC)
                }
        }
    }

    // ── Password recovery ──────────────────────────────────────────────────────
    fun recoverPassword(email: String) {
        if (email.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = ERROR_FIELDS_EMPTY)
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            authRepository.sendPasswordResetEmail(email.trim())
                .onSuccess {
                    Log.i(TAG, "recoverPassword OK $email")
                    _uiState.value = UiState(
                        infoMessage = INFO_RESET_SENT,
                        lastAction = UiState.Action.RECOVER_OK
                    )
                }
                .onFailure {
                    Log.e(TAG, "recoverPassword failed", it)
                    _uiState.value = UiState(errorMessage = it.localizedMessage ?: ERROR_RECOVER_GENERIC)
                }
        }
    }

    // ── Logout ─────────────────────────────────────────────────────────────────
    fun logout() {
        val uid = currentUser.value?.uid
        viewModelScope.launch {
            if (uid != null) {
                userRepository.logAccess(uid, AccessLog.EVENT_LOGOUT)
            }
            authRepository.logout()
            Log.i(TAG, "logout OK (was uid=$uid)")
            _uiState.value = UiState(lastAction = UiState.Action.LOGOUT_OK)
        }
    }

    fun resendVerification() {
        viewModelScope.launch {
            authRepository.sendEmailVerification()
                .onSuccess { _uiState.value = _uiState.value.copy(infoMessage = INFO_VERIFICATION_SENT) }
                .onFailure { _uiState.value = _uiState.value.copy(errorMessage = it.localizedMessage) }
        }
    }

    companion object {
        private const val TAG = "AuthViewModel"
        const val ERROR_FIELDS_EMPTY     = "Rellena todos los campos obligatorios"
        const val ERROR_USERNAME_TAKEN   = "Ese nombre de usuario ya está en uso"
        const val ERROR_LOGIN_GENERIC    = "No se ha podido iniciar sesión"
        const val ERROR_REGISTER_GENERIC = "No se ha podido crear la cuenta"
        const val ERROR_RECOVER_GENERIC  = "No se ha podido enviar el correo de recuperación"
        const val INFO_VERIFICATION_SENT = "Te hemos enviado un correo para verificar tu cuenta"
        const val INFO_RESET_SENT        = "Te hemos enviado un correo para recuperar la contraseña"
    }
}

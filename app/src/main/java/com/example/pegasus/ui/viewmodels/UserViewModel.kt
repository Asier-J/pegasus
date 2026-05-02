package com.example.pegasus.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pegasus.domain.AccessLog
import com.example.pegasus.domain.AuthRepository
import com.example.pegasus.domain.User
import com.example.pegasus.domain.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Sprint 03: exposes the current user's profile and access-log history.
 * Used by ProfileScreen / PreferencesScreen.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class UserViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    /** The local profile of the logged-in Firebase user (or null if logged out). */
    val currentProfile: StateFlow<User?> =
        authRepository.observeCurrentUser()
            .flatMapLatest { authUser ->
                if (authUser == null) flowOf<User?>(null)
                else userRepository.observeUser(authUser.uid)
            }
            .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val accessLogs: StateFlow<List<AccessLog>> =
        authRepository.observeCurrentUser()
            .flatMapLatest { authUser ->
                if (authUser == null) flowOf(emptyList())
                else userRepository.observeAccessLogs(authUser.uid)
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), emptyList())

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun updateProfile(profile: User) {
        viewModelScope.launch {
            userRepository.updateUser(profile)
                .onFailure {
                    Log.e(TAG, "updateProfile failed", it)
                    _errorMessage.value = it.message
                }
        }
    }

    fun clearError() { _errorMessage.value = null }

    private companion object { const val TAG = "UserViewModel" }
}

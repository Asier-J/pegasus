package com.example.pegasus.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pegasus.domain.AuthRepository
import com.example.pegasus.domain.Trip
import com.example.pegasus.domain.TripRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import javax.inject.Inject

// ─── TripViewModel ─────────────────────────────────────────────────────────────
// Sprint 03: Hilt-injected, Room-backed.
// All trips are scoped to the current Firebase uid (multi-user persistence).
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TripViewModel @Inject constructor(
    private val repository: TripRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    /**
     * The trip list is bound to the auth state: when the uid changes (login/logout)
     * the underlying Flow switches automatically.
     */
    val trips: StateFlow<List<Trip>> =
        authRepository.observeCurrentUser()
            .flatMapLatest { authUser ->
                if (authUser == null) flowOf(emptyList())
                else repository.observeTrips(authUser.uid)
            }
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun clearError() { _errorMessage.value = null }

    /** Synchronous lookup used by edit screens to prefill fields. */
    suspend fun getTripById(id: String): Trip? = repository.getTripById(id)

    // ── Create ─────────────────────────────────────────────────────────────────
    fun addTrip(
        title: String,
        startDate: String,
        endDate: String,
        description: String,
        onResult: (Boolean) -> Unit = {}
    ) {
        viewModelScope.launch {
            val uid = authRepository.currentUser()?.uid ?: run {
                _errorMessage.value = ERROR_NO_USER
                onResult(false); return@launch
            }
            if (!validateTripFields(title, startDate, endDate, description)) {
                onResult(false); return@launch
            }
            // Sprint 03 T5.2: prevent duplicate trip names per-user
            if (repository.isTitleTakenByOther(uid, title.trim(), excludeId = "")) {
                _errorMessage.value = ERROR_TITLE_DUPLICATE
                onResult(false); return@launch
            }
            val trip = Trip(
                userId = uid,
                title = title.trim(),
                startDate = startDate,
                endDate = endDate,
                description = description.trim()
            )
            repository.addTrip(trip)
            Log.i(TAG, "addTrip OK '${trip.title}'")
            onResult(true)
        }
    }

    // ── Update ─────────────────────────────────────────────────────────────────
    fun editTrip(
        id: String,
        title: String,
        startDate: String,
        endDate: String,
        description: String,
        onResult: (Boolean) -> Unit = {}
    ) {
        viewModelScope.launch {
            val uid = authRepository.currentUser()?.uid ?: run {
                _errorMessage.value = ERROR_NO_USER
                onResult(false); return@launch
            }
            if (!validateTripFields(title, startDate, endDate, description)) {
                onResult(false); return@launch
            }
            if (repository.isTitleTakenByOther(uid, title.trim(), excludeId = id)) {
                _errorMessage.value = ERROR_TITLE_DUPLICATE
                onResult(false); return@launch
            }
            val trip = Trip(
                id = id,
                userId = uid,
                title = title.trim(),
                startDate = startDate,
                endDate = endDate,
                description = description.trim()
            )
            repository.editTrip(trip)
            Log.i(TAG, "editTrip OK '${trip.title}'")
            onResult(true)
        }
    }

    // ── Delete ─────────────────────────────────────────────────────────────────
    fun deleteTrip(id: String) {
        viewModelScope.launch {
            repository.deleteTrip(id)
            Log.i(TAG, "deleteTrip OK id=$id")
        }
    }

    // ── Validation ─────────────────────────────────────────────────────────────
    private fun validateTripFields(
        title: String,
        startDate: String,
        endDate: String,
        description: String
    ): Boolean {
        if (title.isBlank())       { _errorMessage.value = ERROR_TITLE_EMPTY;       return false }
        if (startDate.isBlank())   { _errorMessage.value = ERROR_START_DATE_EMPTY;  return false }
        if (endDate.isBlank())     { _errorMessage.value = ERROR_END_DATE_EMPTY;    return false }
        if (description.isBlank()) { _errorMessage.value = ERROR_DESCRIPTION_EMPTY; return false }
        if (!isStartBeforeOrEqualEnd(startDate, endDate)) {
            _errorMessage.value = ERROR_DATE_ORDER
            return false
        }
        return true
    }

    private fun isStartBeforeOrEqualEnd(startDate: String, endDate: String): Boolean = try {
        val start = LocalDate.parse(startDate, formatter)
        val end   = LocalDate.parse(endDate,   formatter)
        !start.isAfter(end)
    } catch (e: DateTimeParseException) {
        Log.e(TAG, "Date parsing error: ${e.message}")
        false
    }

    companion object {
        private const val TAG = "TripViewModel"

        const val ERROR_NO_USER           = "No hay sesión activa"
        const val ERROR_TITLE_EMPTY       = "El título no puede estar vacío"
        const val ERROR_TITLE_DUPLICATE   = "Ya tienes otro viaje con ese título"
        const val ERROR_START_DATE_EMPTY  = "Selecciona una fecha de inicio"
        const val ERROR_END_DATE_EMPTY    = "Selecciona una fecha de fin"
        const val ERROR_DESCRIPTION_EMPTY = "La descripción no puede estar vacía"
        const val ERROR_DATE_ORDER        = "La fecha de inicio debe ser anterior o igual a la fecha de fin"
    }
}

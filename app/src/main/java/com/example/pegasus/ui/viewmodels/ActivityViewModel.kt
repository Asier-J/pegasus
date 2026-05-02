package com.example.pegasus.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pegasus.domain.Activity
import com.example.pegasus.domain.ActivityRepository
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
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import javax.inject.Inject

// ─── ActivityViewModel ─────────────────────────────────────────────────────────
// Sprint 03 — T1.5 / T1.6: Hilt-injected, Room-backed.
// Activities live inside a Trip. The detail screen calls `loadActivities(tripId)`
// once on entry; from there `activities` is a hot Flow tied to the active trip
// id, so any insert/update/delete in Room reaches the UI without a manual reload.
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ActivityViewModel @Inject constructor(
    private val activityRepository: ActivityRepository,
    private val tripRepository: TripRepository
) : ViewModel() {

    private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    /** Currently displayed trip id; null until `loadActivities()` is called. */
    private val _currentTripId = MutableStateFlow<String?>(null)

    /**
     * Reactive list of activities for the current trip. Switches automatically
     * whenever the user navigates to a different trip detail screen, and emits
     * fresh data on every Room write.
     */
    val activities: StateFlow<List<Activity>> =
        _currentTripId
            .flatMapLatest { tripId ->
                if (tripId == null) flowOf(emptyList())
                else activityRepository.observeActivities(tripId)
            }
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun clearError() { _errorMessage.value = null }

    /** Selects which trip's activities to observe. Cheap; no DB read happens here. */
    fun loadActivities(tripId: String) {
        _currentTripId.value = tripId
        Log.d(TAG, "loadActivities: now observing trip=$tripId")
    }

    suspend fun getActivityById(id: String): Activity? = activityRepository.getActivityById(id)

    fun addActivity(
        tripId: String,
        title: String,
        description: String,
        date: LocalDate?,
        time: LocalTime?,
        onResult: (Boolean) -> Unit = {}
    ) {
        viewModelScope.launch {
            if (!validateActivityFields(tripId, title, description, date, time)) {
                onResult(false); return@launch
            }
            val activity = Activity(
                tripId = tripId,
                title = title.trim(),
                description = description.trim(),
                date = date!!,
                time = time!!
            )
            activityRepository.addActivity(activity)
            Log.i(TAG, "addActivity OK '${activity.title}'")
            onResult(true)
        }
    }

    fun updateActivity(
        id: String,
        tripId: String,
        title: String,
        description: String,
        date: LocalDate?,
        time: LocalTime?,
        onResult: (Boolean) -> Unit = {}
    ) {
        viewModelScope.launch {
            if (!validateActivityFields(tripId, title, description, date, time)) {
                onResult(false); return@launch
            }
            val activity = Activity(
                id = id,
                tripId = tripId,
                title = title.trim(),
                description = description.trim(),
                date = date!!,
                time = time!!
            )
            activityRepository.updateActivity(activity)
            Log.i(TAG, "updateActivity OK '${activity.title}'")
            onResult(true)
        }
    }

    fun deleteActivity(id: String, tripId: String) {
        viewModelScope.launch {
            activityRepository.deleteActivity(id)
            Log.i(TAG, "deleteActivity OK id=$id (trip=$tripId)")
        }
    }

    // ── Validation ─────────────────────────────────────────────────────────────
    private suspend fun validateActivityFields(
        tripId: String,
        title: String,
        description: String,
        date: LocalDate?,
        time: LocalTime?
    ): Boolean {
        if (title.isBlank())  { _errorMessage.value = ERROR_TITLE_EMPTY; return false }
        if (date == null)     { _errorMessage.value = ERROR_DATE_EMPTY;  return false }
        if (time == null)     { _errorMessage.value = ERROR_TIME_EMPTY;  return false }
        if (!isDateWithinTripRange(tripId, date)) {
            _errorMessage.value = ERROR_DATE_OUT_OF_RANGE
            return false
        }
        return true
    }

    private suspend fun isDateWithinTripRange(tripId: String, date: LocalDate): Boolean {
        val trip = tripRepository.getTripById(tripId) ?: run {
            Log.e(TAG, "isDateWithinTripRange: trip $tripId not found"); return false
        }
        return try {
            val tripStart = LocalDate.parse(trip.startDate, dateFormatter)
            val tripEnd   = LocalDate.parse(trip.endDate,   dateFormatter)
            !date.isBefore(tripStart) && !date.isAfter(tripEnd)
        } catch (e: DateTimeParseException) {
            Log.e(TAG, "Date parse error: ${e.message}")
            false
        }
    }

    companion object {
        private const val TAG = "ActivityViewModel"

        const val ERROR_TITLE_EMPTY       = "El título de la actividad no puede estar vacío"
        const val ERROR_DATE_EMPTY        = "Selecciona una fecha para la actividad"
        const val ERROR_TIME_EMPTY        = "Selecciona una hora para la actividad"
        const val ERROR_DATE_OUT_OF_RANGE = "La fecha debe estar dentro del rango del viaje"
    }
}

package com.example.pegasus.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.pegasus.data.repository.ActivityRepositoryImpl
import com.example.pegasus.data.repository.TripRepositoryImpl
import com.example.pegasus.domain.Activity
import com.example.pegasus.domain.ActivityRepository
import com.example.pegasus.domain.TripRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

// ─── ActivityViewModel ─────────────────────────────────────────────────────────
// Manages activity state for a specific trip and exposes CRUD operations.
// Validates that activity dates fall within the trip's date range.
// Architecture: UI → ActivityViewModel → ActivityRepository → FakeTripDataSource
class ActivityViewModel : ViewModel() {

    private val activityRepository: ActivityRepository = ActivityRepositoryImpl()
    private val tripRepository: TripRepository = TripRepositoryImpl()
    private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    // ── State ──────────────────────────────────────────────────────────────────
    private val _activities = MutableStateFlow<List<Activity>>(emptyList())
    val activities: StateFlow<List<Activity>> = _activities.asStateFlow()

    /** Holds validation or operation error messages to display in the UI. */
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // ── Read ───────────────────────────────────────────────────────────────────

    /** Loads activities for the given trip id. */
    fun loadActivities(tripId: String) {
        _activities.value = activityRepository.getActivitiesByTripId(tripId)
        Log.d(TAG, "loadActivities($tripId): ${_activities.value.size} activities")
    }

    /** Returns a single activity by its id, or null if not found. */
    fun getActivityById(id: String): Activity? = activityRepository.getActivityById(id)

    // ── Create ─────────────────────────────────────────────────────────────────

    /**
     * Validates input and adds a new activity to the trip.
     * @return true if added successfully, false on validation error.
     */
    fun addActivity(
        tripId: String,
        title: String,
        description: String,
        date: LocalDate?,
        time: LocalTime?
    ): Boolean {
        if (!validateActivityFields(tripId, title, description, date, time)) return false
        val activity = Activity(
            tripId      = tripId,
            title       = title.trim(),
            description = description.trim(),
            date        = date!!,
            time        = time!!
        )
        activityRepository.addActivity(activity)
        loadActivities(tripId)
        Log.i(TAG, "addActivity: '${activity.title}' added to trip $tripId")
        return true
    }

    // ── Update ─────────────────────────────────────────────────────────────────

    /**
     * Validates input and updates an existing activity.
     * @return true if updated successfully, false on validation error.
     */
    fun updateActivity(
        id: String,
        tripId: String,
        title: String,
        description: String,
        date: LocalDate?,
        time: LocalTime?
    ): Boolean {
        if (!validateActivityFields(tripId, title, description, date, time)) return false
        val activity = Activity(
            id          = id,
            tripId      = tripId,
            title       = title.trim(),
            description = description.trim(),
            date        = date!!,
            time        = time!!
        )
        activityRepository.updateActivity(activity)
        loadActivities(tripId)
        Log.i(TAG, "updateActivity: '${activity.title}' updated")
        return true
    }

    // ── Delete ─────────────────────────────────────────────────────────────────

    /** Deletes an activity and refreshes the list for the given trip. */
    fun deleteActivity(id: String, tripId: String) {
        activityRepository.deleteActivity(id)
        loadActivities(tripId)
        Log.i(TAG, "deleteActivity: activity id=$id deleted")
    }

    // ── Error handling ─────────────────────────────────────────────────────────

    /** Clears the current error message. */
    fun clearError() {
        _errorMessage.value = null
    }

    // ── Validation ─────────────────────────────────────────────────────────────

    /**
     * Validates all activity fields.
     * Checks that date falls within the trip's date range.
     */
    private fun validateActivityFields(
        tripId: String,
        title: String,
        description: String,
        date: LocalDate?,
        time: LocalTime?
    ): Boolean {
        if (title.isBlank()) {
            _errorMessage.value = ERROR_TITLE_EMPTY
            Log.w(TAG, "Validation failed: activity title is blank")
            return false
        }
        if (date == null) {
            _errorMessage.value = ERROR_DATE_EMPTY
            Log.w(TAG, "Validation failed: activity date is null")
            return false
        }
        if (time == null) {
            _errorMessage.value = ERROR_TIME_EMPTY
            Log.w(TAG, "Validation failed: activity time is null")
            return false
        }
        if (!isDateWithinTripRange(tripId, date)) {
            _errorMessage.value = ERROR_DATE_OUT_OF_RANGE
            Log.w(TAG, "Validation failed: activity date $date is outside trip range")
            return false
        }
        return true
    }

    /**
     * Returns true if the given date falls within the trip's start–end range.
     * If the trip is not found or dates cannot be parsed, returns false.
     */
    private fun isDateWithinTripRange(tripId: String, date: LocalDate): Boolean {
        val trip = tripRepository.getTripById(tripId) ?: run {
            Log.e(TAG, "isDateWithinTripRange: trip $tripId not found")
            return false
        }
        return try {
            val tripStart = LocalDate.parse(trip.startDate, dateFormatter)
            val tripEnd   = LocalDate.parse(trip.endDate, dateFormatter)
            !date.isBefore(tripStart) && !date.isAfter(tripEnd)
        } catch (e: DateTimeParseException) {
            Log.e(TAG, "Date parsing error while validating range: ${e.message}")
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

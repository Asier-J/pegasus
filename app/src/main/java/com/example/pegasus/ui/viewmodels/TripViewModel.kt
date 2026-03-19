package com.example.pegasus.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.pegasus.data.repository.TripRepositoryImpl
import com.example.pegasus.domain.Trip
import com.example.pegasus.domain.TripRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

// ─── TripViewModel ─────────────────────────────────────────────────────────────
// Manages trip state and exposes CRUD operations to the UI layer.
// Validates input before passing data to the repository.
// Architecture: UI → TripViewModel → TripRepository → FakeTripDataSource
class TripViewModel : ViewModel() {

    private val repository: TripRepository = TripRepositoryImpl()
    private val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    // ── State ──────────────────────────────────────────────────────────────────
    private val _trips = MutableStateFlow<List<Trip>>(emptyList())
    val trips: StateFlow<List<Trip>> = _trips.asStateFlow()

    /** Holds validation or operation error messages to display in the UI. */
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        loadTrips()
    }

    // ── Read ───────────────────────────────────────────────────────────────────

    /** Refreshes the trip list from the data source. */
    fun loadTrips() {
        _trips.value = repository.getTrips()
        Log.d(TAG, "loadTrips: ${_trips.value.size} trips loaded")
    }

    /** Returns a single trip by its id, or null if not found. */
    fun getTripById(id: String): Trip? = repository.getTripById(id)

    // ── Create ─────────────────────────────────────────────────────────────────

    /**
     * Validates input and adds a new trip.
     * @return true if the trip was added successfully, false on validation error.
     */
    fun addTrip(title: String, startDate: String, endDate: String, description: String): Boolean {
        if (!validateTripFields(title, startDate, endDate, description)) return false
        val trip = Trip(
            title       = title.trim(),
            startDate   = startDate,
            endDate     = endDate,
            description = description.trim()
        )
        repository.addTrip(trip)
        loadTrips()
        Log.i(TAG, "addTrip: '${trip.title}' added successfully")
        return true
    }

    // ── Update ─────────────────────────────────────────────────────────────────

    /**
     * Validates input and updates an existing trip.
     * @return true if the trip was updated successfully, false on validation error.
     */
    fun editTrip(
        id: String,
        title: String,
        startDate: String,
        endDate: String,
        description: String
    ): Boolean {
        if (!validateTripFields(title, startDate, endDate, description)) return false
        val trip = Trip(
            id          = id,
            title       = title.trim(),
            startDate   = startDate,
            endDate     = endDate,
            description = description.trim()
        )
        repository.editTrip(trip)
        loadTrips()
        Log.i(TAG, "editTrip: '${trip.title}' updated successfully")
        return true
    }

    // ── Delete ─────────────────────────────────────────────────────────────────

    /** Deletes the trip with the given id (also cascades to its activities). */
    fun deleteTrip(id: String) {
        repository.deleteTrip(id)
        loadTrips()
        Log.i(TAG, "deleteTrip: trip id=$id deleted")
    }

    // ── Error handling ─────────────────────────────────────────────────────────

    /** Clears the current error message (call after showing it to the user). */
    fun clearError() {
        _errorMessage.value = null
    }

    // ── Validation ─────────────────────────────────────────────────────────────

    /**
     * Validates all trip fields.
     * Sets _errorMessage on failure.
     */
    private fun validateTripFields(
        title: String,
        startDate: String,
        endDate: String,
        description: String
    ): Boolean {
        // Required: title
        if (title.isBlank()) {
            _errorMessage.value = ERROR_TITLE_EMPTY
            Log.w(TAG, "Validation failed: title is blank")
            return false
        }
        // Required: startDate
        if (startDate.isBlank()) {
            _errorMessage.value = ERROR_START_DATE_EMPTY
            Log.w(TAG, "Validation failed: start date is blank")
            return false
        }
        // Required: endDate
        if (endDate.isBlank()) {
            _errorMessage.value = ERROR_END_DATE_EMPTY
            Log.w(TAG, "Validation failed: end date is blank")
            return false
        }
        // Required: description
        if (description.isBlank()) {
            _errorMessage.value = ERROR_DESCRIPTION_EMPTY
            Log.w(TAG, "Validation failed: description is blank")
            return false
        }
        // Date logic: start must be before or equal to end
        if (!isStartBeforeOrEqualEnd(startDate, endDate)) {
            _errorMessage.value = ERROR_DATE_ORDER
            Log.w(TAG, "Validation failed: startDate=$startDate is not before endDate=$endDate")
            return false
        }
        return true
    }

    /**
     * Returns true if startDate <= endDate.
     * Logs an error and returns false if parsing fails.
     */
    private fun isStartBeforeOrEqualEnd(startDate: String, endDate: String): Boolean {
        return try {
            val start = LocalDate.parse(startDate, formatter)
            val end   = LocalDate.parse(endDate, formatter)
            !start.isAfter(end)
        } catch (e: DateTimeParseException) {
            Log.e(TAG, "Date parsing error: ${e.message}")
            false
        }
    }

    companion object {
        private const val TAG = "TripViewModel"

        const val ERROR_TITLE_EMPTY       = "El título no puede estar vacío"
        const val ERROR_START_DATE_EMPTY  = "Selecciona una fecha de inicio"
        const val ERROR_END_DATE_EMPTY    = "Selecciona una fecha de fin"
        const val ERROR_DESCRIPTION_EMPTY = "La descripción no puede estar vacía"
        const val ERROR_DATE_ORDER        = "La fecha de inicio debe ser anterior o igual a la fecha de fin"
    }
}

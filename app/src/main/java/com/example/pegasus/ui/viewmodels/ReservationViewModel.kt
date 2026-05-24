package com.example.pegasus.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pegasus.domain.AuthRepository
import com.example.pegasus.domain.HotelRepository
import com.example.pegasus.domain.Reservation
import com.example.pegasus.domain.ReservationRepository
import com.example.pegasus.domain.TripImageRepository
import com.example.pegasus.domain.TripRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Sprint 04 — Lists local reservations and lets the user cancel them.
 *
 * Cancellation is best-effort against the server: if the DELETE fails (offline,
 * server error, already cancelled) we still remove the local row so the user
 * doesn't get stuck with a phantom reservation in the UI.
 *
 * T4.1 requires "indicating the trip related", so the VM also exposes a
 * tripId → tripTitle map taken from the logged-in user's trips. The UI uses it
 * to label every reservation card with the parent trip.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ReservationViewModel @Inject constructor(
    private val reservationRepository: ReservationRepository,
    private val hotelRepository: HotelRepository,
    private val tripRepository: TripRepository,
    private val tripImageRepository: TripImageRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    val reservations: StateFlow<List<Reservation>> =
        reservationRepository.observeReservations()
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /** Map of tripId → tripTitle for every trip owned by the logged-in user. */
    val tripTitlesById: StateFlow<Map<String, String>> =
        authRepository.observeCurrentUser()
            .flatMapLatest { user ->
                if (user == null) flowOf(emptyList())
                else tripRepository.observeTrips(user.uid)
            }
            .map { trips -> trips.associate { it.id to it.title } }
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun clearError() { _errorMessage.value = null }

    /**
     * Cancels the reservation server-side and removes it locally.
     *
     * @param removeTrip if true, also deletes the linked Trip (matches the
     *   booking flow that created a Trip together with the Reservation).
     */
    fun cancel(reservation: Reservation, removeTrip: Boolean = true) {
        viewModelScope.launch {
            val remoteOk = runCatching { hotelRepository.cancelReservation(reservation.id) }
                .onFailure { Log.w(TAG, "remote cancel failed (cleaning up locally anyway)", it) }
                .isSuccess

            reservationRepository.deleteReservation(reservation.id)
            if (removeTrip) {
                // Wipe trip-scoped image files BEFORE removing the trip row, so
                // we don't leak files on disk when Room cascades the delete.
                runCatching { tripImageRepository.deleteAllForTrip(reservation.tripId) }
                    .onFailure { Log.w(TAG, "image cleanup failed", it) }
                runCatching { tripRepository.deleteTrip(reservation.tripId) }
                    .onFailure { Log.w(TAG, "trip cleanup failed", it) }
            }

            Log.i(TAG, "cancel done id=${reservation.id} remoteOk=$remoteOk")
            if (!remoteOk) _errorMessage.value = ERROR_REMOTE_CANCEL
        }
    }

    companion object {
        private const val TAG = "ReservationViewModel"
        const val ERROR_REMOTE_CANCEL =
            "Cancelado en el dispositivo, pero el servidor no respondió"
    }
}

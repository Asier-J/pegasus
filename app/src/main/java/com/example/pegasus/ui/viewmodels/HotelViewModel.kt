package com.example.pegasus.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pegasus.domain.AuthRepository
import com.example.pegasus.domain.Hotel
import com.example.pegasus.domain.HotelRepository
import com.example.pegasus.domain.Reservation
import com.example.pegasus.domain.ReservationRepository
import com.example.pegasus.domain.Room
import com.example.pegasus.domain.Trip
import com.example.pegasus.domain.TripRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/**
 * Sprint 04 — Drives the hotel search + booking flow.
 *
 * - Holds the current search parameters and the resulting hotel list.
 * - On booking success: creates (or reuses) a local Trip and persists the
 *   Reservation alongside it. The reservation list screen picks it up
 *   automatically via [ReservationRepository.observeReservations].
 */
@HiltViewModel
class HotelViewModel @Inject constructor(
    private val hotelRepository: HotelRepository,
    private val reservationRepository: ReservationRepository,
    private val tripRepository: TripRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    // ── Search state ────────────────────────────────────────────────────────────

    private val _city = MutableStateFlow(City.BCN)
    val city: StateFlow<City> = _city.asStateFlow()

    /** ISO yyyy-MM-dd (API format). Empty when nothing picked yet. */
    private val _startDate = MutableStateFlow("")
    val startDate: StateFlow<String> = _startDate.asStateFlow()

    private val _endDate = MutableStateFlow("")
    val endDate: StateFlow<String> = _endDate.asStateFlow()

    private val _hotels = MutableStateFlow<List<Hotel>>(emptyList())
    val hotels: StateFlow<List<Hotel>> = _hotels.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _lastBooking = MutableStateFlow<Reservation?>(null)
    val lastBooking: StateFlow<Reservation?> = _lastBooking.asStateFlow()

    fun selectCity(city: City)        { _city.value = city }
    fun setStartDate(date: String)    { _startDate.value = date }
    fun setEndDate(date: String)      { _endDate.value = date }
    fun clearError()                  { _errorMessage.value = null }
    fun clearLastBooking()            { _lastBooking.value = null }

    // ── Actions ─────────────────────────────────────────────────────────────────

    /** Calls /availability with the current city + dates. */
    fun searchHotels() {
        val city  = _city.value
        val start = _startDate.value
        val end   = _endDate.value

        if (start.isBlank() || end.isBlank()) {
            _errorMessage.value = ERROR_DATES_REQUIRED
            return
        }
        if (!datesAreInOrder(start, end)) {
            _errorMessage.value = ERROR_DATE_ORDER
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            runCatching { hotelRepository.checkAvailability(city.code, start, end) }
                .onSuccess {
                    _hotels.value = it
                    Log.i(TAG, "search ok → ${it.size} hotels")
                }
                .onFailure {
                    Log.e(TAG, "search failed", it)
                    _errorMessage.value = it.message ?: ERROR_GENERIC
                    _hotels.value = emptyList()
                }
            _isLoading.value = false
        }
    }

    /** Lookup helper used by HotelDetailScreen. */
    fun hotelById(id: String): Hotel? = _hotels.value.firstOrNull { it.id == id }

    /**
     * Books [room] at [hotel] using the current screen dates and the logged-in
     * user as the guest. On success, persists a local Trip + Reservation.
     */
    fun bookRoom(hotel: Hotel, room: Room, onResult: (Boolean) -> Unit = {}) {
        val authUser = authRepository.currentUser() ?: run {
            _errorMessage.value = ERROR_NO_USER
            onResult(false); return
        }
        val guestEmail = authUser.email ?: run {
            _errorMessage.value = ERROR_NO_EMAIL
            onResult(false); return
        }
        val start = _startDate.value
        val end   = _endDate.value
        if (start.isBlank() || end.isBlank()) {
            _errorMessage.value = ERROR_DATES_REQUIRED
            onResult(false); return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val outcome = runCatching {
                val guestName = guestEmail.substringBefore('@')
                val confirmation = hotelRepository.reserveRoom(
                    hotelId    = hotel.id,
                    roomId     = room.id,
                    startDate  = start,
                    endDate    = end,
                    guestName  = guestName,
                    guestEmail = guestEmail
                )

                // Sprint 04 T2.3 — store a Trip + Reservation so it shows up locally.
                val tripTitle = "${hotel.name} (${hotel.id})"
                val trip = Trip(
                    userId      = authUser.uid,
                    title       = uniqueTripTitle(authUser.uid, tripTitle),
                    startDate   = toLocalDateFormat(start),
                    endDate     = toLocalDateFormat(end),
                    description = "${hotel.address} — ${room.roomType}",
                    budget      = (room.price * confirmation.nights).toInt()
                )
                tripRepository.addTrip(trip)

                val reservation = Reservation(
                    id            = confirmation.reservationId,
                    tripId        = trip.id,
                    hotelId       = hotel.id,
                    hotelName     = hotel.name,
                    hotelAddress  = hotel.address,
                    hotelImageUrl = hotel.imageUrl,
                    roomId        = room.id,
                    roomType      = room.roomType,
                    roomImageUrl  = room.images.firstOrNull().orEmpty(),
                    pricePerNight = room.price,
                    startDate     = start,
                    endDate       = end,
                    nights        = confirmation.nights,
                    guestName     = guestName,
                    guestEmail    = guestEmail
                )
                reservationRepository.addReservation(reservation)
                reservation
            }
            outcome
                .onSuccess {
                    _lastBooking.value = it
                    Log.i(TAG, "book ok → reservation=${it.id}, trip=${it.tripId}")
                    onResult(true)
                }
                .onFailure {
                    Log.e(TAG, "book failed", it)
                    _errorMessage.value = it.message ?: ERROR_GENERIC
                    onResult(false)
                }
            _isLoading.value = false
        }
    }

    // ── Helpers ─────────────────────────────────────────────────────────────────

    private suspend fun uniqueTripTitle(userId: String, base: String): String {
        if (!tripRepository.isTitleTakenByOther(userId, base, excludeId = "")) return base
        // Append a discriminator (counter) until the title is unique.
        var n = 2
        while (tripRepository.isTitleTakenByOther(userId, "$base #$n", excludeId = "")) n++
        return "$base #$n"
    }

    private fun datesAreInOrder(startIso: String, endIso: String): Boolean = try {
        val start = LocalDate.parse(startIso, ISO)
        val end   = LocalDate.parse(endIso, ISO)
        !start.isAfter(end)
    } catch (e: Exception) {
        Log.e(TAG, "Date parsing error: ${e.message}")
        false
    }

    /** Converts an API-format date (yyyy-MM-dd) into the local Trip format (dd/MM/yyyy). */
    private fun toLocalDateFormat(isoDate: String): String = try {
        LocalDate.parse(isoDate, ISO).format(LOCAL)
    } catch (e: Exception) {
        isoDate
    }

    /** Cities the API understands. */
    enum class City(val code: String, val displayName: String) {
        BCN("BCN", "Barcelona"),
        PAR("PAR", "Paris"),
        LON("LON", "London")
    }

    companion object {
        private const val TAG = "HotelViewModel"
        private val ISO   = DateTimeFormatter.ISO_LOCAL_DATE
        private val LOCAL = DateTimeFormatter.ofPattern("dd/MM/yyyy")

        const val ERROR_NO_USER         = "No hay sesión activa"
        const val ERROR_NO_EMAIL        = "La cuenta no tiene email asociado"
        const val ERROR_DATES_REQUIRED  = "Selecciona fechas de entrada y salida"
        const val ERROR_DATE_ORDER      = "La fecha de inicio debe ser anterior o igual a la fecha de fin"
        const val ERROR_GENERIC         = "No se pudo completar la operación"
    }
}

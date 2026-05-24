package com.example.pegasus.data.repository

import android.util.Log
import com.example.pegasus.data.local.dao.ReservationDao
import com.example.pegasus.domain.Reservation
import com.example.pegasus.domain.ReservationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReservationRepositoryImpl @Inject constructor(
    private val dao: ReservationDao
) : ReservationRepository {

    override fun observeReservations(): Flow<List<Reservation>> {
        Log.d(TAG, "observe all")
        return dao.observeAll()
    }

    override fun observeReservationsForTrip(tripId: String): Flow<List<Reservation>> {
        Log.d(TAG, "observe trip=$tripId")
        return dao.observeForTrip(tripId)
    }

    override suspend fun getReservationById(id: String): Reservation? =
        dao.getById(id).also { Log.d(TAG, "getById($id) → ${it != null}") }

    override suspend fun addReservation(reservation: Reservation) {
        Log.i(TAG, "insert ${reservation.id} for trip=${reservation.tripId}")
        dao.insert(reservation)
    }

    override suspend fun deleteReservation(reservationId: String) {
        Log.i(TAG, "delete $reservationId")
        dao.deleteById(reservationId)
    }

    override suspend fun deleteReservationsForTrip(tripId: String) {
        Log.i(TAG, "delete all for trip=$tripId")
        dao.deleteForTrip(tripId)
    }

    companion object {
        private const val TAG = "ReservationRepository"
    }
}

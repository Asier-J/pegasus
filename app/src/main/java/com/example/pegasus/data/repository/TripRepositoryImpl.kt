package com.example.pegasus.data.repository

import android.util.Log
import com.example.pegasus.data.local.dao.TripDao
import com.example.pegasus.domain.Trip
import com.example.pegasus.domain.TripRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

// ─── TripRepositoryImpl ────────────────────────────────────────────────────────
// Sprint 03: Room-backed implementation. All operations are user-scoped via the DAO.
@Singleton
class TripRepositoryImpl @Inject constructor(
    private val tripDao: TripDao
) : TripRepository {

    override fun observeTrips(userId: String): Flow<List<Trip>> =
        tripDao.observeAllForUser(userId)

    override suspend fun getTrips(userId: String): List<Trip> =
        tripDao.getAllForUser(userId).also {
            Log.d(TAG, "getTrips($userId): ${it.size} trips")
        }

    override suspend fun getTripById(id: String): Trip? = tripDao.getById(id)

    override suspend fun addTrip(trip: Trip) {
        tripDao.insert(trip)
        Log.i(TAG, "addTrip: '${trip.title}' (id=${trip.id}, owner=${trip.userId})")
    }

    override suspend fun editTrip(trip: Trip) {
        tripDao.update(trip)
        Log.i(TAG, "editTrip: '${trip.title}' (id=${trip.id})")
    }

    override suspend fun deleteTrip(id: String) {
        tripDao.deleteById(id)
        Log.i(TAG, "deleteTrip: id=$id (cascades to activities)")
    }

    override suspend fun isTitleTakenByOther(
        userId: String,
        title: String,
        excludeId: String
    ): Boolean = tripDao.isTitleTakenByOther(userId, title, excludeId)

    private companion object {
        const val TAG = "TripRepository"
    }
}

package com.example.pegasus.data.repository

import com.example.pegasus.data.fakeDB.FakeTripDataSource
import com.example.pegasus.domain.Trip
import com.example.pegasus.domain.TripRepository

// ─── TripRepositoryImpl ────────────────────────────────────────────────────────
// Concrete implementation of TripRepository backed by FakeTripDataSource.
// Architecture: UI → ViewModel → TripRepository → TripRepositoryImpl → FakeTripDataSource
class TripRepositoryImpl : TripRepository {

    override fun getTrips(): List<Trip> =
        FakeTripDataSource.getTrips()

    override fun getTripById(id: String): Trip? =
        FakeTripDataSource.getTripById(id)

    override fun addTrip(trip: Trip) =
        FakeTripDataSource.addTrip(trip)

    override fun editTrip(trip: Trip) =
        FakeTripDataSource.editTrip(trip)

    override fun deleteTrip(id: String) =
        FakeTripDataSource.deleteTrip(id)
}

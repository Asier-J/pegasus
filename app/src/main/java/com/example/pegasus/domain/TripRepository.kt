package com.example.pegasus.domain

// ─── TripRepository ───────────────────────────────────────────────────────────
// Interface defining CRUD operations for Trip entities.
// Implementations: TripRepositoryImpl (uses FakeTripDataSource).
interface TripRepository {
    fun getTrips(): List<Trip>
    fun getTripById(id: String): Trip?
    fun addTrip(trip: Trip)
    fun editTrip(trip: Trip)
    fun deleteTrip(id: String)
}

package com.example.pegasus

import com.example.pegasus.data.fakeDB.FakeTripDataSource
import com.example.pegasus.data.repository.TripRepositoryImpl
import com.example.pegasus.domain.Trip
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for Trip CRUD operations.
 * Tests cover: addTrip, editTrip, deleteTrip, getTrips, getTripById.
 * Each test starts with a clean FakeTripDataSource to ensure isolation.
 */
class TripCrudTest {

    private lateinit var repository: TripRepositoryImpl

    @Before
    fun setUp() {
        // Reset in-memory data source before each test
        FakeTripDataSource.clearAll()
        repository = TripRepositoryImpl()
    }

    // ── addTrip ────────────────────────────────────────────────────────────────

    @Test
    fun addTrip_addsToList() {
        val trip = Trip(title = "Paris", startDate = "01/06/2026", endDate = "07/06/2026", description = "City of Light")
        repository.addTrip(trip)
        val result = repository.getTrips()
        assertEquals(1, result.size)
        assertEquals("Paris", result[0].title)
    }

    @Test
    fun addTrip_multipleTrips_allPresent() {
        val trip1 = Trip(title = "Paris",  startDate = "01/06/2026", endDate = "07/06/2026", description = "A")
        val trip2 = Trip(title = "Tokyo",  startDate = "01/08/2026", endDate = "10/08/2026", description = "B")
        val trip3 = Trip(title = "London", startDate = "01/09/2026", endDate = "05/09/2026", description = "C")
        repository.addTrip(trip1)
        repository.addTrip(trip2)
        repository.addTrip(trip3)
        assertEquals(3, repository.getTrips().size)
    }

    // ── getTrips ───────────────────────────────────────────────────────────────

    @Test
    fun getTrips_emptyWhenNoTrips() {
        assertTrue(repository.getTrips().isEmpty())
    }

    @Test
    fun getTrips_returnsImmutableSnapshot() {
        val trip = Trip(title = "Paris", startDate = "01/06/2026", endDate = "07/06/2026", description = "Test")
        repository.addTrip(trip)
        val snapshot = repository.getTrips()
        // Adding another trip after snapshot should not affect the snapshot
        repository.addTrip(Trip(title = "Tokyo", startDate = "01/08/2026", endDate = "10/08/2026", description = "Test"))
        assertEquals(1, snapshot.size)
    }

    // ── getTripById ────────────────────────────────────────────────────────────

    @Test
    fun getTripById_returnsCorrectTrip() {
        val trip = Trip(id = "test-id-1", title = "Paris", startDate = "01/06/2026", endDate = "07/06/2026", description = "Test")
        repository.addTrip(trip)
        val found = repository.getTripById("test-id-1")
        assertNotNull(found)
        assertEquals("Paris", found!!.title)
    }

    @Test
    fun getTripById_returnsNullForUnknownId() {
        assertNull(repository.getTripById("nonexistent-id"))
    }

    // ── editTrip ───────────────────────────────────────────────────────────────

    @Test
    fun editTrip_updatesExistingTrip() {
        val trip = Trip(id = "edit-id", title = "Paris", startDate = "01/06/2026", endDate = "07/06/2026", description = "Old description")
        repository.addTrip(trip)

        val updated = trip.copy(title = "Paris Updated", description = "New description")
        repository.editTrip(updated)

        val result = repository.getTripById("edit-id")
        assertNotNull(result)
        assertEquals("Paris Updated", result!!.title)
        assertEquals("New description", result.description)
    }

    @Test
    fun editTrip_doesNotChangeTripCount() {
        val trip = Trip(id = "edit-count-id", title = "Paris", startDate = "01/06/2026", endDate = "07/06/2026", description = "Test")
        repository.addTrip(trip)
        repository.editTrip(trip.copy(title = "Paris Updated"))
        assertEquals(1, repository.getTrips().size)
    }

    // ── deleteTrip ─────────────────────────────────────────────────────────────

    @Test
    fun deleteTrip_removesFromList() {
        val trip = Trip(id = "del-id", title = "Paris", startDate = "01/06/2026", endDate = "07/06/2026", description = "Test")
        repository.addTrip(trip)
        assertEquals(1, repository.getTrips().size)

        repository.deleteTrip("del-id")
        assertEquals(0, repository.getTrips().size)
    }

    @Test
    fun deleteTrip_unknownId_doesNotCrash() {
        // Should not throw any exception
        repository.deleteTrip("does-not-exist")
        assertEquals(0, repository.getTrips().size)
    }

    @Test
    fun deleteTrip_onlyDeletesTargetTrip() {
        val trip1 = Trip(id = "id-a", title = "Paris",  startDate = "01/06/2026", endDate = "07/06/2026", description = "A")
        val trip2 = Trip(id = "id-b", title = "Tokyo",  startDate = "01/08/2026", endDate = "10/08/2026", description = "B")
        repository.addTrip(trip1)
        repository.addTrip(trip2)

        repository.deleteTrip("id-a")

        val trips = repository.getTrips()
        assertEquals(1, trips.size)
        assertEquals("Tokyo", trips[0].title)
    }
}

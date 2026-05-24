package com.example.pegasus

import android.os.Build
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.pegasus.data.local.AppDatabase
import com.example.pegasus.data.local.dao.ReservationDao
import com.example.pegasus.data.local.dao.TripDao
import com.example.pegasus.data.local.dao.UserDao
import com.example.pegasus.domain.Reservation
import com.example.pegasus.domain.Trip
import com.example.pegasus.domain.User
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Sprint 04 T1.4 — Room tests for [ReservationDao].
 *
 * Coverage:
 *  - insert / getById / observeAll / observeForTrip
 *  - deleteById / deleteForTrip
 *  - Cascade: deleting the parent Trip removes its reservations.
 *  - Replace semantics on conflicting PK.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P])
class ReservationDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: ReservationDao
    private lateinit var tripDao: TripDao
    private lateinit var userDao: UserDao

    private val userId = "uid-1"
    private val tripA  = "trip-A"
    private val tripB  = "trip-B"

    @Before
    fun setUp() = runTest {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        dao     = db.reservationDao()
        tripDao = db.tripDao()
        userDao = db.userDao()

        userDao.insert(User(uid = userId, email = "x@y.z", username = "x"))
        tripDao.insert(
            Trip(id = tripA, userId = userId,
                title = "Trip A", startDate = "01/05/2025", endDate = "03/05/2025")
        )
        tripDao.insert(
            Trip(id = tripB, userId = userId,
                title = "Trip B", startDate = "10/05/2025", endDate = "12/05/2025")
        )
    }

    @After
    fun tearDown() { db.close() }

    private fun sample(id: String, trip: String, hotel: String = "BCN01") = Reservation(
        id = id, tripId = trip,
        hotelId = hotel, hotelName = "H", hotelAddress = "A", hotelImageUrl = "",
        roomId = "R1", roomType = "single", roomImageUrl = "", pricePerNight = 80.0,
        startDate = "2025-05-01", endDate = "2025-05-03", nights = 2,
        guestName = "Asier", guestEmail = "asier@example.com"
    )

    @Test
    fun `insert and getById round-trip persists every field`() = runTest {
        dao.insert(sample("R-1", tripA))

        val found = dao.getById("R-1")
        assertNotNull(found)
        assertEquals("BCN01", found!!.hotelId)
        assertEquals(tripA, found.tripId)
        assertEquals(2, found.nights)
        assertEquals(160.0, found.totalPrice, 0.001)
    }

    @Test
    fun `observeAll emits ordered DESC by createdAt`() = runTest {
        dao.insert(sample("R-1", tripA).copy(createdAt = 1L))
        dao.insert(sample("R-2", tripB).copy(createdAt = 2L))
        dao.insert(sample("R-3", tripA).copy(createdAt = 3L))

        val ids = dao.observeAll().first().map { it.id }
        assertEquals(listOf("R-3", "R-2", "R-1"), ids)
    }

    @Test
    fun `observeForTrip scopes results to a single trip`() = runTest {
        dao.insert(sample("R-1", tripA))
        dao.insert(sample("R-2", tripB))
        dao.insert(sample("R-3", tripA))

        val tripAReservations = dao.observeForTrip(tripA).first()
        assertEquals(2, tripAReservations.size)
        assertTrue(tripAReservations.all { it.tripId == tripA })
    }

    @Test
    fun `deleteById removes only the targeted row`() = runTest {
        dao.insert(sample("R-1", tripA))
        dao.insert(sample("R-2", tripA))

        dao.deleteById("R-1")

        assertNull(dao.getById("R-1"))
        assertNotNull(dao.getById("R-2"))
    }

    @Test
    fun `deleteForTrip removes all rows for that trip`() = runTest {
        dao.insert(sample("R-1", tripA))
        dao.insert(sample("R-2", tripA))
        dao.insert(sample("R-3", tripB))

        dao.deleteForTrip(tripA)

        assertNull(dao.getById("R-1"))
        assertNull(dao.getById("R-2"))
        assertNotNull(dao.getById("R-3"))
    }

    @Test
    fun `delete trip cascades to its reservations`() = runTest {
        dao.insert(sample("R-1", tripA))
        dao.insert(sample("R-2", tripA))

        tripDao.deleteById(tripA)

        assertNull(dao.getById("R-1"))
        assertNull(dao.getById("R-2"))
    }

    @Test
    fun `insert with same id replaces the previous row`() = runTest {
        dao.insert(sample("R-1", tripA, hotel = "BCN01"))
        dao.insert(sample("R-1", tripA, hotel = "BCN02"))

        val found = dao.getById("R-1")
        assertEquals("BCN02", found!!.hotelId)
    }
}

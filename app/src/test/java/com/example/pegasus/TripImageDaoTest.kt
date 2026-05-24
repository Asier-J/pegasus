package com.example.pegasus

import android.os.Build
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.pegasus.data.local.AppDatabase
import com.example.pegasus.data.local.dao.TripDao
import com.example.pegasus.data.local.dao.TripImageDao
import com.example.pegasus.data.local.dao.UserDao
import com.example.pegasus.domain.Trip
import com.example.pegasus.domain.TripImage
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
 * Sprint 04 T1.4 — Room tests for [TripImageDao].
 *
 * Coverage:
 *  - insert / listForTrip / observeForTrip
 *  - DESC ordering by addedAt
 *  - deleteById / deleteForTrip
 *  - Cascade on trip deletion
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P])
class TripImageDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: TripImageDao
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
        dao     = db.tripImageDao()
        tripDao = db.tripDao()
        userDao = db.userDao()

        userDao.insert(User(uid = userId, email = "x@y.z", username = "x"))
        tripDao.insert(Trip(id = tripA, userId = userId,
            title = "Trip A", startDate = "01/05/2025", endDate = "03/05/2025"))
        tripDao.insert(Trip(id = tripB, userId = userId,
            title = "Trip B", startDate = "10/05/2025", endDate = "12/05/2025"))
    }

    @After fun tearDown() { db.close() }

    @Test
    fun `insert and listForTrip persists path and timestamp`() = runTest {
        dao.insert(TripImage(id = "i1", tripId = tripA, localPath = "/files/i1.jpg", addedAt = 100L))

        val rows = dao.listForTrip(tripA)
        assertEquals(1, rows.size)
        assertEquals("/files/i1.jpg", rows[0].localPath)
        assertEquals(100L, rows[0].addedAt)
    }

    @Test
    fun `observeForTrip emits ordered DESC by addedAt`() = runTest {
        dao.insert(TripImage(id = "a", tripId = tripA, localPath = "p1", addedAt = 1L))
        dao.insert(TripImage(id = "b", tripId = tripA, localPath = "p2", addedAt = 2L))
        dao.insert(TripImage(id = "c", tripId = tripA, localPath = "p3", addedAt = 3L))

        val emitted = dao.observeForTrip(tripA).first().map { it.id }
        assertEquals(listOf("c", "b", "a"), emitted)
    }

    @Test
    fun `observeForTrip is scoped to its tripId`() = runTest {
        dao.insert(TripImage(id = "a", tripId = tripA, localPath = "p1"))
        dao.insert(TripImage(id = "b", tripId = tripB, localPath = "p2"))

        val tripARows = dao.observeForTrip(tripA).first()
        assertEquals(1, tripARows.size)
        assertEquals(tripA, tripARows[0].tripId)
    }

    @Test
    fun `deleteById removes a single image`() = runTest {
        dao.insert(TripImage(id = "a", tripId = tripA, localPath = "p1"))
        dao.insert(TripImage(id = "b", tripId = tripA, localPath = "p2"))

        dao.deleteById("a")

        assertEquals(listOf("b"), dao.listForTrip(tripA).map { it.id })
    }

    @Test
    fun `deleteForTrip wipes the whole gallery`() = runTest {
        dao.insert(TripImage(id = "a", tripId = tripA, localPath = "p1"))
        dao.insert(TripImage(id = "b", tripId = tripA, localPath = "p2"))
        dao.insert(TripImage(id = "c", tripId = tripB, localPath = "p3"))

        dao.deleteForTrip(tripA)

        assertTrue(dao.listForTrip(tripA).isEmpty())
        assertEquals(1, dao.listForTrip(tripB).size)
    }

    @Test
    fun `delete trip cascades to its images`() = runTest {
        dao.insert(TripImage(id = "a", tripId = tripA, localPath = "p1"))
        dao.insert(TripImage(id = "b", tripId = tripA, localPath = "p2"))

        tripDao.deleteById(tripA)

        assertTrue(dao.listForTrip(tripA).isEmpty())
    }
}

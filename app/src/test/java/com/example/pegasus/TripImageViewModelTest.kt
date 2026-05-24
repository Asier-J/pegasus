package com.example.pegasus

import android.net.Uri
import android.os.Build
import com.example.pegasus.domain.TripImage
import com.example.pegasus.domain.TripImageRepository
import com.example.pegasus.ui.viewmodels.TripImageViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Sprint 04 — Tests for [TripImageViewModel].
 *
 * Coverage:
 *  - `images` is empty until `setTripId` is called.
 *  - After `setTripId`, the VM flat-maps to the repository's flow for that trip.
 *  - `addImage` and `deleteImage` delegate to the repository and surface errors
 *    via `errorMessage`.
 *  - `clearError` resets the error state.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P])
class TripImageViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var repo: TripImageRepository
    private lateinit var vm: TripImageViewModel

    private val tripA = "trip-A"
    private val imageA = TripImage(id = "i1", tripId = tripA, localPath = "/x/i1.jpg")

    // Robolectric makes Uri.parse work — Uri is `final`, so it can't be mocked
    // without mockito-inline; using a real parsed Uri is the cleanest path.
    private val sampleUri: Uri = Uri.parse("content://test/photo.jpg")

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repo = mock {
            on { observeImagesForTrip(eq(tripA)) } doReturn flowOf(listOf(imageA))
        }
        vm = TripImageViewModel(repo)
    }

    @After fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun `images is empty when no trip is selected`() = runTest {
        advanceUntilIdle()
        assertTrue(vm.images.value.isEmpty())
    }

    @Test
    fun `setTripId swaps the images flow to the repository value`() = runTest {
        vm.setTripId(tripA)
        advanceUntilIdle()

        assertEquals(1, vm.images.value.size)
        assertEquals("i1", vm.images.value[0].id)
    }

    @Test
    fun `addImage delegates to the repository with the right tripId`() = runTest {
        val uri = sampleUri
        whenever(repo.addImage(eq(tripA), eq(uri))).thenReturn(imageA)

        vm.addImage(tripA, uri)
        advanceUntilIdle()

        verify(repo).addImage(tripA, uri)
        assertNull(vm.errorMessage.value)
    }

    @Test
    fun `addImage failure surfaces the error message`() = runTest {
        val uri = sampleUri
        whenever(repo.addImage(eq(tripA), eq(uri))).thenThrow(RuntimeException("disk full"))

        vm.addImage(tripA, uri)
        advanceUntilIdle()

        assertEquals("disk full", vm.errorMessage.value)
    }

    @Test
    fun `deleteImage delegates to the repository`() = runTest {
        vm.deleteImage(imageA)
        advanceUntilIdle()

        verify(repo).deleteImage(imageA)
        assertNull(vm.errorMessage.value)
    }

    @Test
    fun `deleteImage failure surfaces the error message`() = runTest {
        whenever(repo.deleteImage(eq(imageA))).thenThrow(RuntimeException("boom"))

        vm.deleteImage(imageA)
        advanceUntilIdle()

        assertEquals("boom", vm.errorMessage.value)
    }

    @Test
    fun `clearError resets the error state`() = runTest {
        val uri = sampleUri
        whenever(repo.addImage(any(), any())).thenThrow(RuntimeException("x"))
        vm.addImage(tripA, uri)
        advanceUntilIdle()
        assertNotNull(vm.errorMessage.value)

        vm.clearError()
        assertNull(vm.errorMessage.value)

        // Sanity: a successful subsequent op doesn't bring the error back.
        verify(repo, never()).deleteImage(any())
    }
}

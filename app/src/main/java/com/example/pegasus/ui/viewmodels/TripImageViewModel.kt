package com.example.pegasus.ui.viewmodels

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pegasus.domain.TripImage
import com.example.pegasus.domain.TripImageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Sprint 04 — Owns the gallery state for the currently-open Trip.
 *
 * `setTripId` is called from the screen and re-emits a fresh Flow of images,
 * mirroring the approach the ActivityViewModel uses for trip-scoped activities.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TripImageViewModel @Inject constructor(
    private val repository: TripImageRepository
) : ViewModel() {

    private val _currentTripId = MutableStateFlow<String?>(null)

    val images: StateFlow<List<TripImage>> =
        _currentTripId.flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else repository.observeImagesForTrip(id)
        }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun setTripId(tripId: String) { _currentTripId.value = tripId }

    fun clearError() { _errorMessage.value = null }

    fun addImage(tripId: String, sourceUri: Uri) {
        viewModelScope.launch {
            runCatching { repository.addImage(tripId, sourceUri) }
                .onSuccess { Log.i(TAG, "added image id=${it.id}") }
                .onFailure {
                    Log.e(TAG, "addImage failed", it)
                    _errorMessage.value = it.message ?: ERROR_ADD
                }
        }
    }

    fun deleteImage(image: TripImage) {
        viewModelScope.launch {
            runCatching { repository.deleteImage(image) }
                .onSuccess { Log.i(TAG, "deleted image id=${image.id}") }
                .onFailure {
                    Log.e(TAG, "deleteImage failed", it)
                    _errorMessage.value = it.message ?: ERROR_DELETE
                }
        }
    }

    companion object {
        private const val TAG = "TripImageViewModel"
        const val ERROR_ADD    = "No se pudo añadir la imagen"
        const val ERROR_DELETE = "No se pudo eliminar la imagen"
    }
}

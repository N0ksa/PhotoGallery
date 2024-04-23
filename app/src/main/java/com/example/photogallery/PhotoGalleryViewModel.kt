package com.example.photogallery

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.photogallery.api.GalleryItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


private const val TAG = "PhotoGalleryViewModel"

class PhotoGalleryViewModel : ViewModel() {

    private val photoRepository : PhotoRepository = PhotoRepository()

    private val _uiState : MutableStateFlow<PhotoGalleryUiState> = MutableStateFlow(
        PhotoGalleryUiState()
    )

    private val preferencesRepository = PreferencesRepository.get()

    val uiState : StateFlow<PhotoGalleryUiState>
        get() = _uiState.asStateFlow()


    init {
        viewModelScope.launch {

            preferencesRepository.storedQuery.collectLatest {storedQuery ->

                try {
                    val items = fetchGalleryItems(storedQuery)
                     _uiState.update {oldState ->
                         oldState.copy(
                             images = items,
                             query = storedQuery
                         )
                     }

                } catch (ex: Exception) {
                    Log.e(TAG, "Failed to fetch gallery items", ex)
                }

            }
        }

        viewModelScope.launch {
            preferencesRepository.isPolling.collect {isPolling ->
                _uiState.update {it.copy(isPolling = isPolling)}
            }
        }
    }


    fun setQuery(query: String) {
        viewModelScope.launch {
            preferencesRepository.setStoredQuery(query)
        }
    }

    fun toggleIsPolling() {
        viewModelScope.launch {
            preferencesRepository.setPolling(!uiState.value.isPolling)
        }
    }

    private suspend fun fetchGalleryItems(query: String) : List<GalleryItem> {
        return if (query.isNotEmpty()) {
            photoRepository.searchPhotos(query)
        }else {
            photoRepository.fetchPhotos()
        }
    }

}


data class PhotoGalleryUiState(
    val images: List<GalleryItem> = listOf(),
    val query: String = "",
    var isPolling: Boolean = false)
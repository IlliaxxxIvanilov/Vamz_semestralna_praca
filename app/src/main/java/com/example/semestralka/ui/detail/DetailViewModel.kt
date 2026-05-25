package com.example.semestralka.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.semestralka.model.Place
import com.example.semestralka.repository.PlacesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class DetailUiState {
    data object Loading : DetailUiState()
    data class Success(
        val place: Place,
        val isFavorite: Boolean
    ) : DetailUiState()
    data object NotFound : DetailUiState()
}

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val repository: PlacesRepository
) : ViewModel() {

    private val _placeId = MutableStateFlow<String?>(null)

    val uiState: StateFlow<DetailUiState> = combine(
        repository.getPlaces(),
        repository.getFavoriteIds(),
        _placeId
    ) { places, favoriteIds, placeId ->
        if (placeId == null) return@combine DetailUiState.Loading
        val place = places.find { it.id == placeId }
        if (place == null) DetailUiState.NotFound
        else DetailUiState.Success(
            place = place,
            isFavorite = placeId in favoriteIds
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DetailUiState.Loading
    )
    fun loadPlace(id: String) {
        _placeId.value = id
    }

    fun toggleFavorite(placeId: String, isFavorite: Boolean) {
        viewModelScope.launch {
            if (isFavorite) {
                repository.removeFavorite(placeId)
            } else {
                repository.addFavorite(placeId)
            }
        }
    }
}
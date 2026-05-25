package com.example.semestralka.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.semestralka.model.Place
import com.example.semestralka.repository.PlacesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class FavoritesUiState {
    data object Loading : FavoritesUiState()
    data object Empty : FavoritesUiState()
    data class Success(val places: List<Place>) : FavoritesUiState()
}


@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val repository: PlacesRepository
) : ViewModel() {


    val uiState: StateFlow<FavoritesUiState> = combine(
        repository.getPlaces(),
        repository.getFavoriteIds()
    ) { places, favoriteIds ->
        if (places.isEmpty()) return@combine FavoritesUiState.Loading
        val favorites = places.filter { it.id in favoriteIds }
        if (favorites.isEmpty()) FavoritesUiState.Empty
        else FavoritesUiState.Success(favorites)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = FavoritesUiState.Loading
    )


    fun removeFavorite(placeId: String) {
        viewModelScope.launch {
            repository.removeFavorite(placeId)
        }
    }
}
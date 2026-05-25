package com.example.semestralka.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.semestralka.model.Category
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


sealed class MapUiState {
    data object Loading : MapUiState()
    data class Success(
        val places: List<Place>,
        val userLocation: Pair<Double, Double>? = null
    ) : MapUiState()
    data class Error(val message: String) : MapUiState()
}

@HiltViewModel
class MapViewModel @Inject constructor(
    private val repository: PlacesRepository,
    private val locationHelper: LocationHelper
) : ViewModel() {

    private val _selectedCategory = MutableStateFlow<Category?>(null)
    val selectedCategory: StateFlow<Category?> = _selectedCategory

    private val _userLocation = MutableStateFlow<Pair<Double, Double>?>(null)
    val userLocation: StateFlow<Pair<Double, Double>?> = _userLocation

    val uiState: StateFlow<MapUiState> = combine(
        repository.getPlaces(),
        _selectedCategory,
        _userLocation
    ) { places, category, location ->
        if (places.isEmpty()) {
            MapUiState.Loading
        } else {
            val filtered = if (category == null) places
            else places.filter { it.category == category }
            MapUiState.Success(filtered, location)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MapUiState.Loading
    )

    init {
        loadPlaces()
    }

    private fun loadPlaces() {
        viewModelScope.launch {
            try {
                repository.refreshPlaces()
            } catch (e: Exception) {

            }
        }
    }

    fun fetchUserLocation() {
        viewModelScope.launch {
            val location = locationHelper.getCurrentLocation()
            _userLocation.value = location
        }
    }


    fun onCategorySelected(category: Category?) {
        _selectedCategory.value = category
    }
}
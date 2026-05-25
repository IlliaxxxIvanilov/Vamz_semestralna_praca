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
    data class Success(val places: List<Place>) : MapUiState()
    data class Error(val message: String) : MapUiState()
}

@HiltViewModel
class MapViewModel @Inject constructor(
    private val repository: PlacesRepository
) : ViewModel() {

    private val _selectedCategory = MutableStateFlow<Category?>(null)
    val selectedCategory: StateFlow<Category?> = _selectedCategory

    val uiState: StateFlow<MapUiState> = combine(
        repository.getPlaces(),
        _selectedCategory
    ) { places, category ->
        if (places.isEmpty()) {
            MapUiState.Loading
        } else {
            val filtered = if (category == null) places
            else places.filter { it.category == category }
            MapUiState.Success(filtered)
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
                // Fallback is handled inside repository
            }
        }
    }

    fun onCategorySelected(category: Category?) {
        _selectedCategory.value = category
    }
}
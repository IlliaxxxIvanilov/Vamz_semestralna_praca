package com.example.semestralka.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.semestralka.model.Category
import com.example.semestralka.model.Place
import com.example.semestralka.repository.PlacesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject


sealed class ListUiState {
    data object Loading : ListUiState()
    data class Success(val places: List<Place>) : ListUiState()
    data object Empty : ListUiState()
}

@OptIn(FlowPreview::class)
@HiltViewModel
class ListViewModel @Inject constructor(
    private val repository: PlacesRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedCategory = MutableStateFlow<Category?>(null)
    val selectedCategory: StateFlow<Category?> = _selectedCategory

    private val _sortByDistance = MutableStateFlow(false)
    val sortByDistance: StateFlow<Boolean> = _sortByDistance

    private val _userLat = MutableStateFlow<Double?>(null)

    private val _userLon = MutableStateFlow<Double?>(null)


    val uiState: StateFlow<ListUiState> = combine(
        repository.getPlaces(),
        _searchQuery.debounce(300),
        _selectedCategory,
        _sortByDistance,
        _userLat,
        _userLon
    ) { places, query, category, byDistance, lat, lon ->
        val categoryFiltered = if (category == null) places
        else places.filter { it.category == category }

        val searched = if (query.isBlank()) categoryFiltered
        else categoryFiltered.filter {
            it.name.contains(query, ignoreCase = true)
        }

        val sorted = if (byDistance && lat != null && lon != null) {
            searched.sortedBy { it.distanceTo(lat, lon) }
        } else {
            searched.sortedBy { it.name }
        }

        when {
            sorted.isEmpty() && places.isEmpty() -> ListUiState.Loading
            sorted.isEmpty() -> ListUiState.Empty
            else -> ListUiState.Success(sorted)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ListUiState.Loading
    )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onCategorySelected(category: Category?) {
        _selectedCategory.value = category
    }

    fun onSortToggled() {
        _sortByDistance.value = !_sortByDistance.value
    }

    fun onLocationUpdated(lat: Double, lon: Double) {
        _userLat.value = lat
        _userLon.value = lon
    }
}
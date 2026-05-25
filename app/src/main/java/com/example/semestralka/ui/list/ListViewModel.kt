package com.example.semestralka.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.semestralka.model.Category
import com.example.semestralka.model.Place
import com.example.semestralka.repository.PlacesRepository
import com.example.semestralka.ui.map.LocationHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject


sealed class ListUiState {
    data object Loading : ListUiState()
    data class Success(val places: List<Place>) : ListUiState()
    data object Empty : ListUiState()
}

private data class FilterState(
    val places: List<Place>,
    val query: String,
    val category: Category?,
    val byDistance: Boolean
)


@OptIn(FlowPreview::class)
@HiltViewModel
class ListViewModel @Inject constructor(
    private val repository: PlacesRepository,
    private val locationHelper: LocationHelper
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
        _sortByDistance
    ) { places, query, category, byDistance ->
        FilterState(places, query, category, byDistance)
    }.combine(
        _userLat.combine(_userLon) { lat, lon -> lat to lon }
    ) { filterState, (lat, lon) ->
        val categoryFiltered = if (filterState.category == null) filterState.places
        else filterState.places.filter { it.category == filterState.category }

        val searched = if (filterState.query.isBlank()) categoryFiltered
        else categoryFiltered.filter {
            it.name.contains(filterState.query, ignoreCase = true)
        }

        val sorted = if (filterState.byDistance && lat != null && lon != null) {
            searched.sortedBy { it.distanceTo(lat, lon) }
        } else {
            searched.sortedBy { it.name }
        }

        when {
            sorted.isEmpty() && filterState.places.isEmpty() -> ListUiState.Loading
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

    fun fetchUserLocation() {
        viewModelScope.launch {
            val location = locationHelper.getCurrentLocation()
            location?.let { (lat, lon) ->
                _userLat.value = lat
                _userLon.value = lon
            }
        }
    }
}
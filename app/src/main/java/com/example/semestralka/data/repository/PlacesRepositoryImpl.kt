package com.example.semestralka.data.repository

import com.example.semestralka.data.local.FavoritesDataStore
import com.example.semestralka.data.remote.OverpassApiService
import com.example.semestralka.model.Category
import com.example.semestralka.model.Place
import com.example.semestralka.repository.PlacesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class `PlacesRepositoryImpl.kt` @Inject constructor(
    private val apiService: OverpassApiService,
    private val favoritesDataStore: FavoritesDataStore,
    private val fallbackProvider: FallbackPlacesProvider
) : PlacesRepository {

    private val placesCache = MutableStateFlow<List<Place>>(emptyList())

    private val zilinaBbox = "49.18,18.70,49.24,18.76"

    override fun getPlaces(): Flow<List<Place>> = placesCache

    override fun getPlacesByCategory(category: Category): Flow<List<Place>> =
        placesCache.map { places -> places.filter { it.category == category } }

    override fun getFavoriteIds(): Flow<Set<String>> =
        favoritesDataStore.favoriteIds

    override suspend fun addFavorite(placeId: String) =
        favoritesDataStore.addFavorite(placeId)

    override suspend fun removeFavorite(placeId: String) =
        favoritesDataStore.removeFavorite(placeId)

    override suspend fun refreshPlaces() {
        val places = fetchFromApi() ?: fallbackProvider.load()
        placesCache.value = places
    }

    private suspend fun fetchFromApi(): List<Place>? {
        return try {
            val queries = buildOverpassQuery()
            val response = apiService.fetchPoi(queries)
            response.elements.mapNotNull { element ->
                val category = detectCategory(element.tags) ?: return@mapNotNull null
                Place(
                    id = element.id.toString(),
                    name = element.name,
                    latitude = element.lat,
                    longitude = element.lon,
                    category = category,
                    description = buildDescription(category, element.tags),
                    imageUrl = null,
                    openingHours = element.openingHours
                )
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun buildOverpassQuery(): String {
        return """
            [out:json][timeout:25];
            (
              node["amenity"="cafe"]($zilinaBbox);
              node["amenity"="restaurant"]($zilinaBbox);
              node["tourism"="attraction"]($zilinaBbox);
              node["leisure"="park"]($zilinaBbox);
              node["shop"="mall"]($zilinaBbox);
            );
            out body;
        """.trimIndent()
    }

    private fun detectCategory(tags: Map<String, String>): Category? = when {
        tags["amenity"] == "cafe" -> Category.CAFE
        tags["amenity"] == "restaurant" -> Category.RESTAURANT
        tags["tourism"] == "attraction" -> Category.ATTRACTION
        tags["leisure"] == "park" -> Category.PARK
        tags["shop"] == "mall" -> Category.SHOP
        else -> null
    }

    private fun buildDescription(category: Category, tags: Map<String, String>): String {
        val parts = mutableListOf(category.displayName)
        tags["addr:street"]?.let { parts.add(it) }
        tags["addr:city"]?.let { parts.add(it) }
        return parts.joinToString(", ")
    }
}
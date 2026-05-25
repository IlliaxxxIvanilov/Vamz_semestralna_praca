package com.example.semestralka.repository

import com.example.semestralka.model.Category
import com.example.semestralka.model.Place
import kotlinx.coroutines.flow.Flow

interface PlacesRepository {
    fun getPlaces(): Flow<List<Place>>
    fun getPlacesByCategory(category: Category): Flow<List<Place>>
    fun getFavoriteIds(): Flow<Set<String>>
    suspend fun addFavorite(placeId: String)
    suspend fun removeFavorite(placeId: String)
    suspend fun refreshPlaces()
}
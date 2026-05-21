package com.example.semestralka.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "favorites"
)


@Singleton
class FavoritesDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val favoritesKey = stringSetPreferencesKey("favorite_ids")

    val favoriteIds: Flow<Set<String>> = context.dataStore.data
        .map { preferences -> preferences[favoritesKey] ?: emptySet() }

    suspend fun addFavorite(placeId: String) {
        context.dataStore.edit { preferences ->
            val current = preferences[favoritesKey] ?: emptySet()
            preferences[favoritesKey] = current + placeId
        }
    }

    suspend fun removeFavorite(placeId: String) {
        context.dataStore.edit { preferences ->
            val current = preferences[favoritesKey] ?: emptySet()
            preferences[favoritesKey] = current - placeId
        }
    }
    
    suspend fun isFavorite(placeId: String): Boolean {
        var result = false
        favoriteIds.collect { ids ->
            result = placeId in ids
            return@collect
        }
        return result
    }
}
package com.example.semestralka.data.repository

import android.content.Context
import com.example.semestralka.R
import com.example.semestralka.model.Place
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FallbackPlacesProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    private val moshi: Moshi
) {
    fun load(): List<Place> {
        return try {
            val json = context.resources
                .openRawResource(R.raw.places_fallback)
                .bufferedReader()
                .use { it.readText() }
            val type = Types.newParameterizedType(
                List::class.java,
                FallbackPlaceDto::class.java
            )
            val adapter = moshi.adapter<List<FallbackPlaceDto>>(type)
            adapter.fromJson(json)
                ?.mapNotNull { it.toPlace() }
                ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
package com.example.semestralka.data.repository

import com.example.semestralka.model.Category
import com.example.semestralka.model.Place
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FallbackPlaceDto(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val category: String,
    val description: String,
    val imageUrl: String? = null,
    val openingHours: String? = null
) {
    fun toPlace(): Place? {
        val cat = try {
            Category.valueOf(category)
        } catch (e: IllegalArgumentException) {
            return null
        }
        return Place(id, name, latitude, longitude, cat, description, imageUrl, openingHours)
    }
}
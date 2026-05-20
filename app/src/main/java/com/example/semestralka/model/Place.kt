package com.example.semestralka.model

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class Place(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val category: Category,
    val description: String,
    val imageUrl: String? = null,
    val openingHours: String? = null
) {
    fun distanceTo(lat: Double, lon: Double): Double {
        val earthRadiusKm = 6371.0
        val dLat = Math.toRadians(lat - latitude)
        val dLon = Math.toRadians(lon - longitude)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(latitude)) *
                cos(Math.toRadians(lat)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        val distance = earthRadiusKm * c
        return Math.round(distance * 10) / 10.0
    }
}
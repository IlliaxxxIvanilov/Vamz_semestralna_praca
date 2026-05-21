package com.example.semestralka.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OverpassResponse(
    @Json(name = "elements") val elements: List<OverpassElement>
)

@JsonClass(generateAdapter = true)
data class OverpassElement(
    @Json(name = "id") val id: Long,
    @Json(name = "lat") val lat: Double,
    @Json(name = "lon") val lon: Double,
    @Json(name = "tags") val tags: Map<String, String> = emptyMap()
) {
    val name: String get() = tags["name"] ?: tags["name:sk"] ?: "Neznáme miesto"

    val openingHours: String? get() = tags["opening_hours"]

    val website: String? get() = tags["website"] ?: tags["contact:website"]
}
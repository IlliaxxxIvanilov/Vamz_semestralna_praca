package com.example.semestralka.model

enum class Category(val displayName: String, val overpassTag: String) {
    CAFE("Kaviareň", "amenity=cafe"),
    RESTAURANT("Reštaurácia", "amenity=restaurant"),
    ATTRACTION("Pamiatka", "tourism=attraction"),
    PARK("Park", "leisure=park"),
    SHOP("Obchod", "shop=mall")
}
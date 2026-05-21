package com.example.semestralka.ui.navigation

sealed class NavRoutes(val route: String) {
    data object Map : NavRoutes("map")
    data object List : NavRoutes("list")
    data object Favorites : NavRoutes("favorites")
    data object Detail : NavRoutes("detail/{placeId}") {
        fun createRoute(placeId: String) = "detail/$placeId"
    }
}
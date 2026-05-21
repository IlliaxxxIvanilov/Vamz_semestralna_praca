package com.example.semestralka.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.semestralka.R
import com.example.semestralka.ui.detail.DetailScreen
import com.example.semestralka.ui.favorites.FavoritesScreen
import com.example.semestralka.ui.list.ListScreen
import com.example.semestralka.ui.map.MapScreen

private data class BottomNavItem(
    val route: String,
    val labelRes: Int,
    val iconRes: Int
)

private val bottomNavItems = listOf(
    BottomNavItem(NavRoutes.Map.route, R.string.nav_map, R.drawable.ic_map),
    BottomNavItem(NavRoutes.List.route, R.string.nav_list, R.drawable.ic_list),
    BottomNavItem(NavRoutes.Favorites.route, R.string.nav_favorites, R.drawable.ic_favorite)
)

@Composable
fun ZilinaGuideNavGraph() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val showBottomBar = currentDestination?.route != NavRoutes.Detail.route

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            selected = currentDestination?.hierarchy
                                ?.any { it.route == item.route } == true,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    painter = painterResource(item.iconRes),
                                    contentDescription = stringResource(item.labelRes)
                                )
                            },
                            label = { Text(stringResource(item.labelRes)) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = NavRoutes.Map.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(NavRoutes.Map.route) {
                MapScreen(
                    onPlaceClick = { placeId ->
                        navController.navigate(NavRoutes.Detail.createRoute(placeId))
                    }
                )
            }
            composable(NavRoutes.List.route) {
                ListScreen(
                    onPlaceClick = { placeId ->
                        navController.navigate(NavRoutes.Detail.createRoute(placeId))
                    }
                )
            }
            composable(NavRoutes.Favorites.route) {
                FavoritesScreen(
                    onPlaceClick = { placeId ->
                        navController.navigate(NavRoutes.Detail.createRoute(placeId))
                    }
                )
            }
            composable(NavRoutes.Detail.route) { backStackEntry ->
                val placeId = backStackEntry.arguments?.getString("placeId") ?: return@composable
                DetailScreen(
                    placeId = placeId,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
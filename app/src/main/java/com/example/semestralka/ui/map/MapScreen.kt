package com.example.semestralka.ui.map

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.semestralka.R
import com.example.semestralka.model.Category
import com.example.semestralka.ui.components.CategoryChip
import com.example.semestralka.ui.components.OsmMapView


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    onPlaceClick: (String) -> Unit,
    viewModel: MapViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val userLocation by viewModel.userLocation.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val permissionDeniedMessage = stringResource(R.string.error_location_permission_denied)

    var locationGranted by rememberSaveable {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var centerOnUser by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        locationGranted = granted
        if (granted) {
            viewModel.fetchUserLocation()
            centerOnUser = true
        }
    }

    LaunchedEffect(locationGranted) {
        if (locationGranted) {
            viewModel.fetchUserLocation()
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(snackbarData = data)
            }
        },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (locationGranted) {
                        viewModel.fetchUserLocation()
                        centerOnUser = true
                    } else {
                        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    }
                }
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_my_location),
                    contentDescription = stringResource(R.string.action_my_location)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                item {
                    CategoryChip(
                        label = stringResource(R.string.filter_all),
                        selected = selectedCategory == null,
                        onClick = { viewModel.onCategorySelected(null) }
                    )
                }
                items(Category.entries) { category ->
                    CategoryChip(
                        label = category.displayName,
                        selected = selectedCategory == category,
                        onClick = { viewModel.onCategorySelected(category) },
                        modifier = Modifier.padding(start = 6.dp)
                    )
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                when (val state = uiState) {
                    is MapUiState.Loading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    is MapUiState.Success -> {
                        OsmMapView(
                            places = state.places,
                            userLocation = state.userLocation,
                            centerOnUser = centerOnUser,
                            onCenterConsumed = { centerOnUser = false },
                            onPlaceClick = onPlaceClick,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    is MapUiState.Error -> {
                        LaunchedEffect(state.message) {
                            snackbarHostState.showSnackbar(state.message)
                        }
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }
        }
    }
}
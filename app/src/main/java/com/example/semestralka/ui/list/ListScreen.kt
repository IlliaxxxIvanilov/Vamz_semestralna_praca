package com.example.semestralka.ui.list

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import com.example.semestralka.ui.components.PlaceCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListScreen(
    onPlaceClick: (String) -> Unit,
    viewModel: ListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val sortByDistance by viewModel.sortByDistance.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var locationGranted by rememberSaveable {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        locationGranted = granted
        if (granted) viewModel.fetchUserLocation()
    }

    LaunchedEffect(sortByDistance, locationGranted) {
        if (sortByDistance && locationGranted) {
            viewModel.fetchUserLocation()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.screen_list_title)) },
                actions = {
                    IconButton(
                        onClick = {
                            if (!locationGranted && !sortByDistance) {
                                permissionLauncher.launch(
                                    Manifest.permission.ACCESS_FINE_LOCATION
                                )
                            } else {
                                viewModel.onSortToggled()
                            }
                        }
                    ) {
                        Icon(
                            painter = painterResource(
                                if (sortByDistance) R.drawable.ic_sort_distance_active
                                else R.drawable.ic_sort_distance
                            ),
                            contentDescription = stringResource(R.string.action_sort_distance)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = viewModel::onSearchQueryChanged,
                placeholder = { Text(stringResource(R.string.search_placeholder)) },
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_search),
                        contentDescription = null
                    )
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            )

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp)
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

            Box(modifier = Modifier.fillMaxSize()) {
                when (val state = uiState) {
                    is ListUiState.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                    is ListUiState.Empty -> {
                        Text(
                            text = stringResource(R.string.list_empty),
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(16.dp)
                        )
                    }
                    is ListUiState.Success -> {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(
                                items = state.places,
                                key = { it.id }
                            ) { place ->
                                PlaceCard(
                                    place = place,
                                    onClick = { onPlaceClick(place.id) },
                                    modifier = Modifier.padding(
                                        horizontal = 12.dp,
                                        vertical = 5.dp
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
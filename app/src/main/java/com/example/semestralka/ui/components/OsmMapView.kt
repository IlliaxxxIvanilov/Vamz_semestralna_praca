package com.example.semestralka.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.semestralka.model.Place
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

private const val ZILINA_LAT = 49.2231
private const val ZILINA_LON = 18.7394
private const val DEFAULT_ZOOM = 14.0

@Composable
fun OsmMapView(
    places: List<Place>,
    userLocation: Pair<Double, Double>? = null,
    centerOnUser: Boolean = false,
    onCenterConsumed: () -> Unit = {},
    onPlaceClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        Configuration.getInstance().load(
            context,
            context.getSharedPreferences("osmdroid", android.content.Context.MODE_PRIVATE)
        )
        Configuration.getInstance().userAgentValue = context.packageName
    }

    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            zoomController.setVisibility(
                org.osmdroid.views.CustomZoomButtonsController.Visibility.SHOW_AND_FADEOUT
            )
            controller.setZoom(DEFAULT_ZOOM)
            controller.setCenter(GeoPoint(ZILINA_LAT, ZILINA_LON))
            isHorizontalMapRepetitionEnabled = false
            isVerticalMapRepetitionEnabled = false
        }
    }

    val locationOverlay = remember {
        MyLocationNewOverlay(GpsMyLocationProvider(context), mapView).apply {
            enableMyLocation()
        }
    }

    LaunchedEffect(Unit) {
        if (!mapView.overlays.contains(locationOverlay)) {
            mapView.overlays.add(locationOverlay)
        }
    }

    LaunchedEffect(places) {
        mapView.overlays.removeAll { it is Marker }
        places.forEach { place ->
            val marker = Marker(mapView).apply {
                position = GeoPoint(place.latitude, place.longitude)
                title = place.name
                snippet = place.category.displayName
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                setOnMarkerClickListener { _, _ ->
                    onPlaceClick(place.id)
                    true
                }
            }
            mapView.overlays.add(marker)
        }
        mapView.invalidate()
    }

    LaunchedEffect(centerOnUser, userLocation) {
        if (centerOnUser && userLocation != null) {
            mapView.controller.animateTo(
                GeoPoint(userLocation.first, userLocation.second)
            )
            onCenterConsumed()
        }
    }

    DisposableEffect(Unit) {
        mapView.onResume()
        onDispose {
            locationOverlay.disableMyLocation()
            mapView.onPause()
        }
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier
    )
}
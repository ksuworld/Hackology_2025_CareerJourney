package com.uworld.careerjourney

import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.maps.android.compose.*
import com.google.android.gms.maps.model.*
//import com.google.maps.android.SphericalUtil

@Composable
fun CustomMapScreen1() {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current

    // Screen aspect ratio (width/height)
    val screenWidth = configuration.screenWidthDp
    val screenHeight = configuration.screenHeightDp
    val aspectRatio = screenWidth.toFloat() / screenHeight.toFloat()

    // Center of the map (adjust to your actual location)
    val centerLat = 37.425
    val centerLng = -122.085

    // Calculate bounds based on desired visible width (degrees difference)
    val halfWidthDegrees = 0.01 // About 1.1km at this latitude; tune as needed
    val halfHeightDegrees = halfWidthDegrees / aspectRatio // Fit image to screen width

    val SW_CORNER = LatLng(centerLat - halfHeightDegrees, centerLng - halfWidthDegrees)
    val NE_CORNER = LatLng(centerLat + halfHeightDegrees, centerLng + halfWidthDegrees)
    val overlayBounds = LatLngBounds(SW_CORNER, NE_CORNER)

    val roadCurve = remember { getRoadCurve(centerLat, centerLng, halfWidthDegrees, halfHeightDegrees) }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(centerLat, centerLng), 18f)
    }
    val milestones = remember { mutableStateListOf<LatLng>() }
    var showMilestoneDialog by remember { mutableStateOf(false) }
    var tappedLatLng by remember { mutableStateOf<LatLng?>(null) }

    // Clamp camera position to bounds
    LaunchedEffect(cameraPositionState.position) {
        val currentLatLng = cameraPositionState.position.target
        if (!overlayBounds.contains(currentLatLng)) {
            cameraPositionState.move(CameraUpdateFactory.newLatLng(overlayBounds.center))
        }
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(
            isBuildingEnabled = false,
            isIndoorEnabled = false,
            mapType = MapType.NORMAL
        ),
        uiSettings = MapUiSettings(
            zoomControlsEnabled = true,
            tiltGesturesEnabled = true,
            rotationGesturesEnabled = true
        ),
        onMapClick = { latLng ->
            if (isOnRoadCurve1(latLng, roadCurve)) {
                tappedLatLng = latLng
                showMilestoneDialog = true
            }
        }
    ) {
        // Custom image overlay using calculated bounds
        GroundOverlay(
            image = BitmapDescriptorFactory.fromResource(R.drawable.roadmap_bg),
            position =  GroundOverlayPosition.create(overlayBounds)
        )

        // Draw the road curve polyline
        Polyline(
            points = roadCurve,
            color = Color(0xFF1565C0),
            width = 16f
        )

        // Draw milestone markers
        milestones.forEachIndexed { idx, milestone ->
            Marker(
                state = MarkerState(position = milestone),
                title = "Milestone ${idx + 1}",
                snippet = "Custom milestone",
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)
            )
        }
    }

    // Milestone popup dialog
    if (showMilestoneDialog && tappedLatLng != null) {
        AlertDialog(
            onDismissRequest = { showMilestoneDialog = false },
            title = { Text("Add Milestone") },
            text = { Text("Do you want to add a milestone here?") },
            confirmButton = {
                Button(onClick = {
                    milestones.add(tappedLatLng!!)
                    Toast.makeText(context, "Milestone added!", Toast.LENGTH_SHORT).show()
                    showMilestoneDialog = false
                }) { Text("Add") }
            },
            dismissButton = {
                Button(onClick = { showMilestoneDialog = false }) { Text("Cancel") }
            }
        )
    }
}

// Example curve points, scaled to overlay area
fun getRoadCurve(
    centerLat: Double,
    centerLng: Double,
    halfWidthDegrees: Double,
    halfHeightDegrees: Double
): List<LatLng> {
    // Create points along the image "road" curve
    return listOf(
        LatLng(centerLat - halfHeightDegrees * 0.8, centerLng - halfWidthDegrees * 0.8),
        LatLng(centerLat, centerLng),
        LatLng(centerLat + halfHeightDegrees * 0.6, centerLng + halfWidthDegrees * 0.3),
        LatLng(centerLat + halfHeightDegrees * 0.9, centerLng + halfWidthDegrees * 0.8)
    )
}

fun isOnRoadCurve1(latLng: LatLng, curve: List<LatLng>): Boolean {
    val thresholdMeters = 20
    for (point in curve) {
//        if (SphericalUtil.computeDistanceBetween(latLng, point) < thresholdMeters) {
//            return true
//        }
    }
    return false
}
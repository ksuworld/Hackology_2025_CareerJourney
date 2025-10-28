package com.uworld.careerjourney.old

import android.widget.Toast
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
//import com.google.maps.android.SphericalUtil
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.GroundOverlay
import com.google.maps.android.compose.GroundOverlayPosition
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.uworld.careerjourney.R

@Composable
fun CustomMapScreen() {
    val context = LocalContext.current
    // Replace with your actual bounds
    val SW_CORNER = LatLng(37.420, -122.090) // Southwest corner
    val NE_CORNER = LatLng(37.430, -122.080) // Northeast corner
    val overlayBounds = LatLngBounds(SW_CORNER, NE_CORNER)
    val roadCurve = remember { getRoadCurve() }
    val cameraPositionState = rememberCameraPositionState {
//        position = CameraPosition.fromLatLngZoom(roadCurve.first(), 18f)
        position = CameraPosition.fromLatLngZoom(overlayBounds.center, 18f)
    }
    val milestones = remember { mutableStateListOf<LatLng>() }
    var showMilestoneDialog by remember { mutableStateOf(false) }
    var tappedLatLng by remember { mutableStateOf<LatLng?>(null) }

    LaunchedEffect(cameraPositionState.position) {
        val currentLatLng = cameraPositionState.position.target
        if (!overlayBounds.contains(currentLatLng)) {
            // Clamp to bounds: move camera back inside overlayBounds
            cameraPositionState.move(CameraUpdateFactory.newLatLng(overlayBounds.center))
        }
    }

    GoogleMap(
        modifier = Modifier,
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
        onMapLoaded = {
            cameraPositionState.move(CameraUpdateFactory.newLatLngBounds(overlayBounds, 0))
//            cameraPositionState.map?.setLatLngBoundsForCameraTarget(overlayBounds)
//            cameraPositionState.map?.setMinZoomPreference(18f)
//            cameraPositionState.map?.setMaxZoomPreference(21f)
        },
        onMapClick = { latLng ->
            if (isOnRoadCurve(latLng, roadCurve)) {
                tappedLatLng = latLng
                showMilestoneDialog = true
            }
        }
    ) {
        // Custom image overlay using bounds
        GroundOverlay(
            image = BitmapDescriptorFactory.fromResource(R.drawable.roadmap_bg),
            position = GroundOverlayPosition.create(overlayBounds)
        )

        // Draw the road curve polyline
        Polyline(
            points = roadCurve,
//            color = 0xFF1565C0.toInt(),
            color = Color(0xFF1565C0),
//            color = Color.Blue,
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

// Example curve points—adjust to match your overlay
fun getRoadCurve(): List<LatLng> {
    return listOf(
        LatLng(37.421, -122.089),
        LatLng(37.423, -122.088),
        LatLng(37.426, -122.085),
        LatLng(37.429, -122.083)
    )
}

fun isOnRoadCurve(latLng: LatLng, curve: List<LatLng>): Boolean {
    val thresholdMeters = 20 // Adjust for precision
    for (point in curve) {
//        if (SphericalUtil.computeDistanceBetween(latLng, point) < thresholdMeters) {
//            return true
//        }
    }
    return false
}
package com.uworld.careerjourney

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.*
import androidx.core.graphics.createBitmap

// --- Utility function to convert a drawable resource to a BitmapDescriptor ---
fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor? {
    val drawable = ContextCompat.getDrawable(context, vectorResId) ?: return null
    drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
    val bitmap = createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight)
    val canvas = Canvas(bitmap)
    drawable.draw(canvas)

    return try {
        // This should now be safer as it is executed inside the LaunchedEffect
        BitmapDescriptorFactory.fromBitmap(bitmap)
    } catch (e: Exception) {
        // Log the error and return a fallback to prevent a crash
        Log.e("MAPS_ERROR", "BitmapDescriptorFactory not initialized: ${e.message}")
        // Return a standard marker as a guaranteed fallback
        BitmapDescriptorFactory.defaultMarker()
    }
}

// --- MAIN COMPOSABLE SCREEN ---
private val ROADMAP_BLUE = R.drawable.roadmap_blue_high_res

@OptIn(MapsComposeExperimentalApi::class)
@Composable
fun CustomRoadmapScreen() {
    val context = LocalContext.current

    // 1. Load your roadmap image as a Ground Overlay resource
//    val roadmapOverlay = remember {
//        bitmapDescriptorFromVector(context, R.drawable.roadmap_blue)
//    }
//
//    // 2. Define the geographic area your image will cover.
//    // These coordinates are arbitrary and define the "box" where the image will stretch.
//    // Adjust these LatLngs to change the zoom level and aspect ratio.
//    val SW_CORNER = LatLng(0.0, -10.0) // Southwest corner
//    val NE_CORNER = LatLng(20.0, 10.0) // Northeast corner
//    val overlayBounds = LatLngBounds(SW_CORNER, NE_CORNER)


    // 4. Milestones Data (Using standard LatLng for placement)
    // Place milestones relative to the overlayBounds (0, -10) to (20, 10)
    val milestones = remember {
        listOf(
            // Top/Destination (near 20, 0)
            LatLng(18.0, 0.0) to R.drawable.ic_cap,
            // Middle (near 10, 0)
            LatLng(10.0, 1.0) to R.drawable.ic_star,
            // Bottom/Start (near 2, 0)
            LatLng(2.0, -1.0) to R.drawable.ic_book,
        )
    }

    // 5. Map Properties to make it a "blank canvas"
    val mapProperties = remember {
        MapProperties(
            mapType = MapType.NONE, // Crucial: Hides all standard map tiles
//            isZoomControlsEnabled = false,
            isTrafficEnabled = false,
        )
    }

    val uiSettings = remember {
        MapUiSettings(
            zoomControlsEnabled = false,
            // Enable touch controls for zooming and panning the custom image
            zoomGesturesEnabled = true,
            scrollGesturesEnabled = true,
            // Disable tilt/rotation since it's a 2D image
            tiltGesturesEnabled = false,
            rotationGesturesEnabled = false
        )
    }

    // 1. Define mutable state for the loaded resources, initialized to null
    var roadmapOverlay by remember { mutableStateOf<BitmapDescriptor?>(null) }
    var loadedMilestones by remember { mutableStateOf(emptyList<Pair<LatLng, BitmapDescriptor>>()) }

    // 2. Define the geographic area and initial camera position (Same as before)
    val SW_CORNER = LatLng(0.0, -10.0)
    val NE_CORNER = LatLng(20.0, 10.0)
    val overlayBounds = LatLngBounds(SW_CORNER, NE_CORNER)

    val initialCameraPosition = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(overlayBounds.center, 5f) // Zoom level 5f
    }

    // Milestones Data (using resource IDs temporarily)
    val milestoneData = remember {
        listOf(
            LatLng(18.0, 0.0) to R.drawable.ic_cap,
            LatLng(10.0, 1.0) to R.drawable.ic_star,
            LatLng(2.0, -1.0) to R.drawable.ic_book,
        )
    }

    // --- FIX: Use LaunchedEffect to defer resource loading ---
//    LaunchedEffect(Unit) {
//        // Load roadmap background
//        roadmapOverlay = bitmapDescriptorFromVector(context, R.drawable.roadmap_blue)
//
//        // Load milestone icons
//        loadedMilestones = milestoneData.map { (latLng, iconResId) ->
//            latLng to (bitmapDescriptorFromVector(context, iconResId) ?: BitmapDescriptorFactory.defaultMarker())
//        }
//    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(overlayBounds.center, 5f)
    }

//    // FIX: Use isGoogleMapLoaded property as the "onMapReady" trigger
//    val isMapLoaded = cameraPositionState.isMapLoaded
//
//    // --- FIX: Defer resource loading until map is ready ---
//    LaunchedEffect(isMapLoaded) {
//        if (isMapLoaded) {
//            // Load roadmap background
//            roadmapOverlay = bitmapDescriptorFromVector(context, ROADMAP_BLUE)
//
//            // Load milestone icons
//            loadedMilestones = milestoneData.mapNotNull { (latLng, iconResId) ->
//                val icon = bitmapDescriptorFromVector(context, iconResId)
//                if (icon != null) latLng to icon else null
//            }
//        }
//    }

    // --- FIX: Use MapEffect to execute code ONLY when the map is initialized ---
// This guarantees the map's native environment is ready.
    MapEffect(Unit) {
        // This code runs when the underlying GoogleMap object is created and ready.

        // Load roadmap background
        roadmapOverlay = bitmapDescriptorFromVector(context, ROADMAP_BLUE)

        // Load milestone icons
        loadedMilestones = milestoneData.mapNotNull { (latLng, iconResId) ->
            val icon = bitmapDescriptorFromVector(context, iconResId)
            if (icon != null) latLng to icon else null
        }
    }

    // 3. Only render the map once the necessary resources are loaded
    if (roadmapOverlay == null || loadedMilestones.isEmpty()) {
        // Show a loading indicator until resources are ready
        // You'll need to define a simple composable for this (e.g., CircularProgressIndicator)
        // Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = initialCameraPosition,
        properties = mapProperties,
        uiSettings = uiSettings
    ) {
        val overlayBounds = LatLngBounds(SW_CORNER, NE_CORNER)

        // FIX: Create the required GroundOverlayPosition object from the bounds
        val groundOverlayPosition = remember {
            GroundOverlayPosition.create(overlayBounds)
        }

        // --- GROUND OVERLAY: Your Custom Image ---
        if (roadmapOverlay != null) {
//            GroundOverlay(
//                bounds = overlayBounds,
//                image = roadmapOverlay,
//                // Ensure it draws at the base (index 0)
//                zIndex = 0f,
//                // Set transparency to fully opaque
//                transparency = 0.0f,
//            )
            GroundOverlay(
                // FIX: Pass the GroundOverlayPosition object here
                position = groundOverlayPosition,
                image = roadmapOverlay!!,
                // Use the optional properties you defined
                transparency = 0.0f, // Fully opaque
                zIndex = 0f,
                clickable = false, // Set to true if interaction is needed
            )
        }
//        position: GroundOverlayPosition,
//        image: BitmapDescriptor,
//        anchor: Offset = Offset(0.5f, 0.5f),
//        bearing: Float = 0f,
//        clickable: Boolean = false,
//        tag: Any? = null,
//        transparency: Float = 0f,
//        visible: Boolean = true,
//        zIndex: Float = 0f,
//        onClick: (GroundOverlay) -> Unit = {},

        // --- MILESTONES: Your Checkpoints ---
        milestones.forEach { (latLng, iconResId) ->
            Marker(
                state = MarkerState(position = latLng),
                // Use a custom icon for the milestone (using the same drawable conversion)
                icon = bitmapDescriptorFromVector(context, iconResId),
                title = "Milestone",
            )
        }

        // NOTE: The logic for showing/hiding milestones based on zoom level
        // would require listening to cameraPositionState.position.zoom
        // and conditionally rendering the Markers.
    }
}
package com.uworld.careerjourney

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.GroundOverlay
import com.google.maps.android.compose.GroundOverlayPosition
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.google.maps.android.compose.Marker // Added Marker import for clarity
//import com.google.maps.android.compose.rememberUpdatedMarkerState

// ----------------------------------------------------------------------
// 1. CONSTANTS: Define the arbitrary coordinate system for the custom image
// ----------------------------------------------------------------------

// Arbitrary virtual coordinates for the bottom-left and top-right of the image.
// This creates a 100x100 unit coordinate space for your image.
// Fixed arbitrary width
private const val VIRTUAL_WIDTH = 100.0

// Arbitrary virtual coordinates
private val BOTTOM_LEFT = LatLng(0.0, 0.0)
//private val TOP_RIGHT = LatLng(100.0, 100.0)

// The total area that the custom image covers on the map
//private val OVERLAY_BOUNDS = LatLngBounds(
//    BOTTOM_LEFT,
//    TOP_RIGHT
//)

// ----------------------------------------------------------------------
// 2. MAIN ACTIVITY
// ----------------------------------------------------------------------

class CustomGoogleMapsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 🚨 FIX: Explicitly initialize the Maps SDK before using any of its components.
        // This prevents the "IBitmapDescriptorFactory is not initialized" error.
        MapsInitializer.initialize(applicationContext) /*{
            // Note: If initialization needs to be synchronous, you might need a different approach,
            // but for Compose usage, this often suffices.
        }*/

        // Ensure you have an image file named 'custom_map_image' in res/drawable
        setContent {
            CustomImageMapScreen(R.drawable.roadmap_bg)
//            CustomMapScreen()
//            CustomMapScreen1()
        }
    }
}

@SuppressLint("LocalContextResourcesRead")
@Composable
private fun getOverlayBounds(@DrawableRes resId: Int): LatLngBounds {
    val context = LocalContext.current
    return remember(resId) {
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true // Read only dimensions, no need to load the full bitmap
        }
        BitmapFactory.decodeResource(context.resources, resId, options)

        val imageWidth = options.outWidth
        val imageHeight = options.outHeight

        // Calculate the virtual height based on the image's aspect ratio
        // Virtual Height / VIRTUAL_WIDTH = Image Height / Image Width
        val virtualHeight = (imageHeight.toDouble() / imageWidth.toDouble()) * VIRTUAL_WIDTH

        // Define the top-right coordinate and the final bounds
        val TOP_RIGHT = LatLng(virtualHeight, VIRTUAL_WIDTH)
        LatLngBounds(
            BOTTOM_LEFT,
            TOP_RIGHT
        )
    }
}

// ----------------------------------------------------------------------
// 3. HELPER FUNCTION: Convert image resource to BitmapDescriptor
// ----------------------------------------------------------------------

@SuppressLint("LocalContextResourcesRead")
@Composable
private fun bitmapDescriptorFromResource(@DrawableRes resId: Int): BitmapDescriptor {
    // Use remember to create this heavy object only once
    val context = LocalContext.current
    return remember(resId) {
        val bitmap = BitmapFactory.decodeResource(context.resources, resId)
        BitmapDescriptorFactory.fromBitmap(bitmap)
    }
}

// ----------------------------------------------------------------------
// 4. THE COMPOSE MAP IMPLEMENTATION
// ----------------------------------------------------------------------

@Composable
fun CustomImageMapScreen(@DrawableRes imageResId: Int) {

    val OVERLAY_BOUNDS = getOverlayBounds(imageResId)
    val customImageDescriptor = bitmapDescriptorFromResource(imageResId)

    // A. Camera State: Centers the view on the custom map area (5f is a good initial zoom)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(OVERLAY_BOUNDS.center, 1f)
    }

    LaunchedEffect(Unit) {
        // This will create a CameraUpdate that centers and zooms the map to fit the bounds.
        // 50 is the padding in screen pixels.
        val cameraUpdate = com.google.android.gms.maps.CameraUpdateFactory.newLatLngBounds(OVERLAY_BOUNDS, 500)
        cameraPositionState.move(cameraUpdate)
    }

    // B. Map Properties: Configures the base map behavior
    val mapProperties = MapProperties(
        // CRUCIAL: Hide standard map tiles to show only the GroundOverlay
        mapType = MapType.NONE,

        // Sets the minimum and maximum allowable zoom levels
//        minZoomPreference = 3f,
//        maxZoomPreference = 7f,

        // Restricts the camera from panning outside the image area
        latLngBoundsForCameraTarget = OVERLAY_BOUNDS
    )

    // C. UI Settings: Configures user gestures
    val mapUiSettings = MapUiSettings(
        zoomControlsEnabled = true,   // Show the +/- buttons
        scrollGesturesEnabled = true, // Enable dragging/panning
        zoomGesturesEnabled = true    // Enable pinch-to-zoom
    )

    // D. The main GoogleMap Composable
    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = mapProperties,
        uiSettings = mapUiSettings
    ) {

        // E. GroundOverlay: This places your custom image onto the map's surface
        GroundOverlay(
            // FIX: Use GroundOverlayPosition.create(bounds = ...) for positioning
            position = GroundOverlayPosition.create(latLngBounds = OVERLAY_BOUNDS),

            image = customImageDescriptor,

            // zIndex determines layer order; 0f is the bottom layer
            zIndex = 0f
        )

        // F. Example Marker: You can place markers using your custom coordinates
        val virtualHeight = OVERLAY_BOUNDS.northeast.latitude // The calculated height
        Marker(
            // Positioned at the center of the bottom-left quadrant (25, 25)
            state = rememberMarkerState(position = LatLng(virtualHeight * 0.25, VIRTUAL_WIDTH * 0.25)),
            title = "Custom POI",
            snippet = "Marker uses your custom 0-100 coordinate system."
        )
    }
}
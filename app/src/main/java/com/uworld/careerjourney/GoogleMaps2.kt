package com.uworld.careerjourney

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState

class MapActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    CustomMapBackground()
                }
            }
        }
    }
}

@Composable
fun CustomMapBackground() {
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

//    val mapStyle = MapStyleOptions.loadRawResourceStyle(LocalContext.current, R.raw.map_style)

    Box(modifier = Modifier.fillMaxSize()) {

        // Google Map on top (with transparency to blend)
        val cameraPositionState = rememberCameraPositionState()

        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            uiSettings = uiSettings,
//            properties = MapProperties(mapStyleOptions = mapStyle)
            // You can also style the map using MapStyleOptions if desired
        )

        // Your roadmap background
//        Image(
//            painter = painterResource(id = R.drawable.roadmap_blue),
//            contentDescription = "Custom Roadmap Background",
//            contentScale = ContentScale.Crop,
//            modifier = Modifier.fillMaxSize()
//        )
        Image(
            painter = painterResource(id = R.drawable.roadmap_blue_high_res),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(alpha = 0.25f) // adjust transparency
        )

    }
}

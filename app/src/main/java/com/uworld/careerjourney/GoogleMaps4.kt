package com.uworld.careerjourney

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min

class CustomRoadmapMapActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val milestones = listOf(
                        Offset(0.2f, 0.3f),
                        Offset(0.5f, 0.6f),
                        Offset(0.8f, 0.9f)
                    )
//                    SmoothBoundedInteractiveMap(
//                        imageRes = R.drawable.roadmap_blue,
//                        milestones = milestones
//                    )
                    FullInteractiveMap(
                        imageRes = R.drawable.roadmap_bg,
                        milestones = milestones
                    )
                }
            }
        }
    }
}

@Composable
fun SmoothBoundedInteractiveMap(
    imageRes: Int,
    milestones: List<Offset>
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    var imageSize by remember { mutableStateOf(IntSize(1, 1)) }
    var screenSize by remember { mutableStateOf(IntSize(1, 1)) }

    // For double-tap zoom
    var lastTapTime by remember { mutableLongStateOf(0L) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { screenSize = it }
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    // Zoom
                    val newScale = (scale * zoom).coerceIn(0.5f, 3f)

                    // Tentative offset
                    val tentativeOffset = offset + pan * newScale

                    // Max offsets to stay within bounds
                    val maxX = max(0f, (imageSize.width * newScale - screenSize.width) / 2)
                    val maxY = max(0f, (imageSize.height * newScale - screenSize.height) / 2)

                    offset = Offset(
                        x = tentativeOffset.x.coerceIn(-maxX, maxX),
                        y = tentativeOffset.y.coerceIn(-maxY, maxY)
                    )

                    scale = newScale
                }
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = { tapOffset ->
                        val targetScale = if (scale < 2f) 2f else 1f
                        scale = targetScale

                        // Center zoom on double-tap point
                        offset = Offset(
                            x = (offset.x - (tapOffset.x - screenSize.width / 2) * (targetScale / scale - 1f)),
                            y = (offset.y - (tapOffset.y - screenSize.height / 2) * (targetScale / scale - 1f))
                        )
                    }
                )
            }
    ) {
        // Map image
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { imageSize = it }
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y
                )
        )

        // Milestones overlay
        milestones.forEach { relativePos ->
            MilestoneMarker(relativePos, scale, offset, imageSize)
        }
    }
}

@Composable
fun MilestoneMarker(
    relativePosition: Offset, // 0..1
    scale: Float,
    offset: Offset,
    imageSize: IntSize
) {
    Box(
        modifier = Modifier
            .graphicsLayer(
                translationX = relativePosition.x * imageSize.width * scale + offset.x,
                translationY = relativePosition.y * imageSize.height * scale + offset.y
            )
            .size(40.dp)
            .background(Color.Yellow, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text("★")
    }
}

@Composable
fun FullInteractiveMap(
    imageRes: Int,
    milestones: List<Offset>
) {
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    var imageSize by remember { mutableStateOf(IntSize(1, 1)) }
    var screenSize by remember { mutableStateOf(IntSize(1, 1)) }

    var initialScale by remember { mutableStateOf(1f) }

    val coroutineScope = rememberCoroutineScope()
    val decaySpec = exponentialDecay<Float>()

    val flingX = remember { Animatable(0f) }
    val flingY = remember { Animatable(0f) }

    // Compute initial scale to fit screen width
    LaunchedEffect(screenSize, imageSize) {
        if (screenSize.width > 0 && imageSize.width > 0) {
            initialScale = screenSize.width.toFloat() / imageSize.width.toFloat()
            scale = initialScale
            offset = Offset.Zero
        }
    }

    val density = LocalDensity.current.density
    Box(
        modifier = Modifier
            .graphicsLayer(
                rotationX = 0f, // tilt for 3D effect
                rotationY = 0f,
                cameraDistance = 24f * density,
                shadowElevation = 16f,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                clip = true
            )
            .size(
                width = (imageSize.width * initialScale).dp,
                height = (imageSize.height * initialScale).dp
            )
            .onSizeChanged { screenSize = it }
            // Pinch-to-zoom
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    val newScale = (scale * zoom).coerceIn(initialScale, 3f)
                    val tentativeOffset = offset + pan * newScale

                    val maxX = max(0f, (imageSize.width * newScale - screenSize.width) / 2)
                    val maxY = max(0f, (imageSize.height * newScale - screenSize.height) / 2)

                    offset = Offset(
                        x = tentativeOffset.x.coerceIn(-maxX, maxX),
                        y = tentativeOffset.y.coerceIn(-maxY, maxY)
                    )
                    scale = newScale
                }
            }
            // Double-tap zoom
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = { tapOffset ->
                        val targetScale = if (scale < 2f) 2f else initialScale
                        coroutineScope.launch {
                            scale = targetScale
                        }

                        offset = Offset(
                            x = (offset.x - (tapOffset.x - screenSize.width / 2) * (targetScale / scale - 1f)),
                            y = (offset.y - (tapOffset.y - screenSize.height / 2) * (targetScale / scale - 1f))
                        )
                    }
                )
            }
            // Drag + fling
            .pointerInput(Unit) {
                detectDragGestures(
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val tentativeOffset = offset + dragAmount
                        val maxX = max(0f, (imageSize.width * scale - screenSize.width) / 2)
                        val maxY = max(0f, (imageSize.height * scale - screenSize.height) / 2)
                        offset = Offset(
                            x = tentativeOffset.x.coerceIn(-maxX, maxX),
                            y = tentativeOffset.y.coerceIn(-maxY, maxY)
                        )
                    },
                    onDragEnd = {
                        // Example fling with fixed velocity; replace with actual velocity if calculated
                        val decay = exponentialDecay<Float>()
                        val velocityX = 1000f
                        val velocityY = 500f

                        coroutineScope.launch {
                            flingX.animateDecay(
                                initialVelocity = velocityX,
                                animationSpec = decay
                            ) {
                                val tentativeX = offset.x + value
                                val maxX = max(0f, (imageSize.width * scale - screenSize.width) / 2)
                                offset = offset.copy(x = tentativeX.coerceIn(-maxX, maxX))
                            }
                        }

                        coroutineScope.launch {
                            flingY.animateDecay(
                                initialVelocity = velocityY,
                                animationSpec = decay
                            ) {
                                val tentativeY = offset.y + value
                                val maxY = max(0f, (imageSize.height * scale - screenSize.height) / 2)
                                offset = offset.copy(y = tentativeY.coerceIn(-maxY, maxY))
                            }
                        }
                    }
                )
            }
    ) {
        // Image
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { imageSize = it }
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y
                )
        )

        // Milestones
        milestones.forEach { relativePos ->
            MilestoneMarker(relativePos, scale, offset, imageSize)
        }
    }
}

package com.uworld.careerjourney.old

import android.graphics.PathMeasure
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.uworld.careerjourney.R
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.atan2

// --- DATA STRUCTURES AND HELPERS ---

data class GMilestoneG(
    val position: Float, // Position along the path (0.0 to 1.0)
    val iconResId: Int,
    val isMajor: Boolean
)

// The path needs to be defined from 0 (top) to 1 (bottom) in drawing coordinates
// The destination is at the start (top) of the path.
val allMilestones = listOf(
    // Major milestones (initial 7 max)
    GMilestoneG(0.1f, R.drawable.ic_cap, isMajor = true), // Near the destination
    GMilestoneG(0.2f, R.drawable.ic_star, isMajor = true),
    GMilestoneG(0.35f, R.drawable.ic_book, isMajor = true),
//    GMilestoneG(0.5f, R.drawable.ic_play, isMajor = true),
//    GMilestoneG(0.65f, R.drawable.ic_check, isMajor = true),
//    GMilestoneG(0.8f, R.drawable.ic_trophy, isMajor = true),
//    GMilestoneG(0.95f, R.drawable.ic_flag, isMajor = true), // Near the bottom

    // Minor milestones (hidden initially)
    GMilestoneG(0.15f, R.drawable.ic_dot, isMajor = false),
    GMilestoneG(0.25f, R.drawable.ic_dot, isMajor = false),
    GMilestoneG(0.42f, R.drawable.ic_dot, isMajor = false),
//    GMilestoneG(0.58f, R.drawable.ic_dot, isMajor = false),
//    GMilestoneG(0.72f, R.drawable.ic_dot, isMajor = false),
//    GMilestoneG(0.87f, R.drawable.ic_dot, isMajor = false),
//    GMilestoneG(0.99f, R.drawable.ic_dot, isMajor = false),
)

/**
 * Uses PathMeasure to find the (x, y) coordinates and the tangent angle at a fraction
 * of the total path length.
 */
fun Path.getPosTan(fraction: Float): Triple<Float, Float, Float> {
    val pathMeasure = PathMeasure(this.asAndroidPath(), false)
    val pos = FloatArray(2)
    val tan = FloatArray(2)
    val distance = pathMeasure.length * fraction

    pathMeasure.getPosTan(distance, pos, tan)

    // Calculate the angle in degrees
    val angle = atan2(tan[1], tan[0]) * (180f / PI.toFloat())
    return Triple(pos[0], pos[1], angle)
}

// Data for the initial, unloaded milestone setup
@Immutable
data class MilestoneData(
    val position: Float,
    val iconResId: Int, // Still uses the resource ID here
    val isMajor: Boolean
)

// The structure used after loading the Painter
@Immutable
data class MilestoneG(
    val position: Float,
    val painter: Painter, // Now holds the loaded Painter object
    val isMajor: Boolean
)

// The initial list uses resource IDs
val allMilestoneData = listOf(
    MilestoneData(0.1f, R.drawable.ic_cap, isMajor = true),
    MilestoneData(0.2f, R.drawable.ic_star, isMajor = true),
    MilestoneData(0.35f, R.drawable.ic_book, isMajor = true),
//    MilestoneData(0.5f, R.drawable.ic_play, isMajor = true),
//    MilestoneData(0.65f, R.drawable.ic_check, isMajor = true),
//    MilestoneData(0.8f, R.drawable.ic_trophy, isMajor = true),
//    MilestoneData(0.95f, R.drawable.ic_flag, isMajor = true),

    MilestoneData(0.15f, R.drawable.ic_dot, isMajor = false),
    MilestoneData(0.25f, R.drawable.ic_dot, isMajor = false),
    MilestoneData(0.42f, R.drawable.ic_dot, isMajor = false),
//    MilestoneData(0.58f, R.drawable.ic_dot, isMajor = false),
//    MilestoneData(0.72f, R.drawable.ic_dot, isMajor = false),
//    MilestoneData(0.87f, R.drawable.ic_dot, isMajor = false),
//    MilestoneData(0.99f, R.drawable.ic_dot, isMajor = false),
)

@Composable
fun GeminiMap1() {
    val blueColor = Color(0xFF007BFF)
    val density = LocalDensity.current
    val pathWidthPx = with(density) { 40.dp.toPx() }
    val milestoneRadius = with(density) { 20.dp.toPx() }

    // --- 1. Load Resources (MUST be in Composable Scope) ---
    val loadedMilestones: List<MilestoneG> = allMilestoneData.map { milestoneData ->
        MilestoneG(
            position = milestoneData.position,
            // FIX: painterResource() is called here in the Composable scope
            painter = painterResource(id = milestoneData.iconResId),
            isMajor = milestoneData.isMajor
        )
    }

    // --- 2. State Management ---
    var zoomLevel by remember { mutableFloatStateOf(1.0f) }
    var panY by remember { mutableFloatStateOf(0f) }
    val coroutineScope = rememberCoroutineScope()

    // Gesture State (Pinch-to-Zoom and Pan)
    val transformableState = rememberTransformableState { zoomChange, offsetChange, _ ->
        zoomLevel = (zoomLevel * zoomChange).coerceIn(1.0f, 3.0f)
        panY = (panY + offsetChange.y).coerceIn(-500f, 500f)
    }

    // --- 3. Drawing Canvas ---
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE0F7FA))
            .transformable(transformableState)
            .pointerInput(Unit) {
                // FIX: Use detectTapGestures for a simple tap
                detectTapGestures(onTap = {
                    coroutineScope.launch {
                        panY = 0f
                        zoomLevel = 1.0f
                    }
                })
            }
    ) {
        val width = size.width
        val height = size.height

        // Define the path from top-to-bottom (0 to 1)
        val path = Path().apply {
            moveTo(width / 2f, 0f)
            cubicTo(width * 0.7f, height * 0.15f, width * 0.3f, height * 0.35f, width * 0.5f, height * 0.5f)
            cubicTo(width * 0.8f, height * 0.65f, width * 0.2f, height * 0.85f, width / 2f, height)
        }

        // Draw the path and destination... (omitted for brevity)

        // --- 4. Draw Milestones ---
        val visibleMilestones = loadedMilestones.filter { milestone ->
            milestone.isMajor || zoomLevel > 1.5f // Visibility logic
        }

        visibleMilestones.forEach { milestone ->
            val (x, y, _) = path.getPosTan(milestone.position)

            val perspectiveScale = 0.5f + (y / height) * 0.5f
            val finalY = y + panY
            val scaleFactor = perspectiveScale * zoomLevel

            if (finalY in 0f..height) {

                // FIX: Use DrawScope's withTransform for safe and proper transformations
                withTransform({
                    // Translate to the milestone's position
                    translate(left = x, top = finalY)

                    // Apply scale, centered at the translated point
                    scale(scaleX = scaleFactor, scaleY = scaleFactor)

                }) {
                    // Draw the milestone background circle (centered at 0,0 in this new space)
                    drawCircle(
                        color = if (milestone.isMajor) Color(0xFF4CAF50) else Color(0xFFFF9800),
                        radius = milestoneRadius,
                        center = Offset(0f, 0f)
                    )

                    // Translate the canvas to the desired top-left position, then draw.
                    withTransform({
                        // The translation places the (0,0) point of the drawing scope
                        // at the desired top-left corner for your icon.
                        translate(
                            left = -milestoneRadius * 0.75f,
                            top = -milestoneRadius * 0.75f
                        )
                    }) {
                        // Now, draw the painter. It will be drawn relative to the new (0,0) point.
                        milestone.painter.apply {
                            draw(
                                size = Size(milestoneRadius * 1.5f, milestoneRadius * 1.5f),
                                alpha = 1f,
                                colorFilter = null
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GeminiMap3() {
    val blueColor = Color(0xFF007BFF)
    val density = LocalDensity.current
    val pathWidthPx = with(density) { 40.dp.toPx() }
    val milestoneRadius = with(density) { 20.dp.toPx() }

    // 1. Load Resources (Composable Scope)
    val loadedMilestones = allMilestoneData.map { milestoneData ->
        MilestoneG(
            position = milestoneData.position,
            painter = painterResource(id = milestoneData.iconResId),
            isMajor = milestoneData.isMajor
        )
    }

    // 2. State Management (Zoom and Pan)
    var zoomLevel by remember { mutableFloatStateOf(1.0f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) } // Use Offset for 2D pan
    val coroutineScope = rememberCoroutineScope()
    val maxZoom = 3.0f

    // Gesture State (Pinch-to-Zoom and Pan)
    val transformableState = rememberTransformableState { zoomChange, offsetChange, _ ->
        zoomLevel = (zoomLevel * zoomChange).coerceIn(1.0f, maxZoom)
        panOffset = panOffset + offsetChange
        // Basic constraints on pan (can be tightened based on content size)
        panOffset = Offset(
            x = panOffset.x.coerceIn(-500f, 500f),
            y = panOffset.y.coerceIn(-500f, 500f)
        )
    }

    // 3. Drawing Canvas
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE0F7FA))
            .transformable(transformableState)
            .pointerInput(Unit) {
                // Reset zoom and pan on tap
                detectTapGestures(onTap = {
                    coroutineScope.launch {
                        panOffset = Offset.Zero
                        zoomLevel = 1.0f
                    }
                })
            }
    ) {
        val width = size.width
        val height = size.height

        // Define the 2D Path (Start: Bottom center, End: Top center)
        val path = Path().apply {
            moveTo(width / 2f, height) // Start at bottom
            cubicTo(width * 0.1f, height * 0.75f, width * 0.9f, height * 0.25f, width / 2f, 0f) // End at top
        }

        // --- Apply Global Transformation (Pan and Zoom) to EVERYTHING ---
        withTransform({
            // Apply pan first
            translate(left = panOffset.x, top = panOffset.y)

            // Apply zoom centered on the canvas (or a focal point)
            scale(
                scaleX = zoomLevel,
                scaleY = zoomLevel,
                pivot = Offset(width / 2f, height / 2f)
            )

        }) {
            val baseWidth = pathWidthPx * 1.5f

            // 1. Draw the Road Map Path (Now correctly positioned with zoom/pan)
            drawPath(
                path = path,
                color = blueColor,
                style = Stroke(width = baseWidth, cap = StrokeCap.Round)
            )

            // 2. Draw Destination Building (Now correctly positioned with zoom/pan)
            val buildingBaseHeight = 60.dp.toPx()
            val buildingBaseWidth = 80.dp.toPx()
            val buildingX = width / 2f - buildingBaseWidth / 2f
            val buildingY = 0f - buildingBaseHeight // Place at top edge

            drawRect(
                color = blueColor,
                topLeft = Offset(buildingX, buildingY),
                size = Size(buildingBaseWidth, buildingBaseHeight)
            )
            // Draw roof
            drawRect(
                color = blueColor,
                topLeft = Offset(buildingX, buildingY - buildingBaseHeight / 3f),
                size = Size(buildingBaseWidth, buildingBaseHeight / 3f)
            )


            // 3. Draw Milestones
            val visibleMilestones = loadedMilestones.filter { milestone ->
                milestone.isMajor || zoomLevel > 1.5f
            }

            visibleMilestones.forEach { milestone ->
                // FIX: Get the correct 2D position directly from the path
                val (x, y) = path.getPosTan(milestone.position)

                // No need for secondary translation/scaling inside here!
                // We just draw the milestone at its calculated (x, y) on the path.
                // The main withTransform block handles the zoom/pan.

                // Draw the milestone background circle
                drawCircle(
                    color = if (milestone.isMajor) Color(0xFF4CAF50) else Color(0xFFFF9800),
                    radius = milestoneRadius,
                    center = Offset(x, y)
                )
//
//                // Draw the icon using the pre-loaded Painter
//                milestone.painter.apply {
//                    draw(
//                        size = Size(milestoneRadius * 1.5f, milestoneRadius * 1.5f),
//                        // Offset from (x, y) to center the icon
//                        topLeft = Offset(x - milestoneRadius * 0.75f, y - milestoneRadius * 0.75f),
//                        alpha = 1f,
//                        colorFilter = null
//                    )
//                }

                // Translate the canvas to the desired top-left position, then draw.
                withTransform({
                    // The translation places the (0,0) point of the drawing scope
                    // at the desired top-left corner for your icon.
                    translate(
                        left = -milestoneRadius * 1.5f,
                        top = -milestoneRadius * 1.5f
                    )
                }) {
                    // Now, draw the painter. It will be drawn relative to the new (0,0) point.
                    milestone.painter.apply {
                        draw(
                            size = Size(milestoneRadius * 1.5f, milestoneRadius * 1.5f),
                            alpha = 1f,
                            colorFilter = null
                        )
                    }
                }
            }
        }
    }
}
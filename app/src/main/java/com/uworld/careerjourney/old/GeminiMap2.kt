package com.uworld.careerjourney.old

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun GeminiMap2() {
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

        // Draw the path and destination...
        // We use a large, fixed width. For true variable perspective width,
        // you would need to use PathMeasure to sample points and draw polygons,
        // which is significantly more complex.
        val baseWidth = pathWidthPx * 1.5f

        drawPath(
            path = path,
            color = blueColor,
            style = Stroke(
                width = baseWidth,
                cap = StrokeCap.Round
            )
        )

        // --- 2. DRAW DESTINATION BUILDING (BANK/BUILDING) ---
        // Draw the building at the start of the path (top, y=0)
        // Apply scaling and pan movement to the building as well
        val buildingBaseHeight = 40.dp.toPx()
        val buildingBaseWidth = 60.dp.toPx()

        val destinationScale = 1f + panY / height * 0.5f // Scale based on pan
        val destinationY = panY + 20.dp.toPx() // Move with pan

        // Draw the main building shape
        drawRect(
            color = blueColor,
            topLeft = Offset(width / 2f - buildingBaseWidth / 2f * destinationScale, destinationY),
            size = Size(buildingBaseWidth * destinationScale, buildingBaseHeight * destinationScale)
        )
        // Draw the roof (triangle approximation)
        drawPath(
            path = Path().apply {
                moveTo(width / 2f - buildingBaseWidth / 2f * destinationScale, destinationY)
                lineTo(width / 2f + buildingBaseWidth / 2f * destinationScale, destinationY)
                lineTo(width / 2f, destinationY - buildingBaseHeight / 3f * destinationScale)
                close()
            },
            color = blueColor
        )

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
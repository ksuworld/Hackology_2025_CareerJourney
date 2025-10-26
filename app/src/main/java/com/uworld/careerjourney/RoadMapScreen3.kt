package com.uworld.careerjourney

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlin.math.min

// ------------------------------
// Main Composable
// ------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoadMapScreen3(
    modifier: Modifier = Modifier,
    initialMilestones: List<Milestone> = emptyList()
) {
    var milestones by remember { mutableStateOf(initialMilestones) }
    var selectedMilestone by remember { mutableStateOf<Milestone?>(null) }

    // Zoom & pan
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val candyRoad = remember {
        CandyRoadPath(
            listOf(
                CandyRoadPath.CubicSegment(
                    Offset(200f, 2000f),
                    Offset(400f, 1500f),
                    Offset(100f, 1000f),
                    Offset(400f, 800f)
                ),
                CandyRoadPath.CubicSegment(
                    Offset(400f, 800f),
                    Offset(800f, 600f),
                    Offset(900f, 400f),
                    Offset(700f, 200f)
                )
            )
        )
    }

    val gradient = Brush.linearGradient(
        listOf(Color(0xFF3B82F6), Color(0xFF10B981), Color(0xFF9333EA)),
        start = Offset.Zero,
        end = Offset(1000f, 2200f)
    )

    Box(modifier = modifier.background(Color(0xFFF9FAFB))) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                // Zoom & pan gestures
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(0.5f, 3f)
                        offset += pan
                    }
                }
                // Tap to add
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { pointerOffset ->
                            val canvasTap = (pointerOffset - offset) / scale
                            val hit = milestones.minByOrNull {
                                (candyRoad.pointAt(it.t) - canvasTap).getDistance()
                            }
                            if (hit != null && (candyRoad.pointAt(hit.t) - canvasTap).getDistance() < 60f) {
                                selectedMilestone = hit
                            } else {
                                selectedMilestone = null
                                val (nearestT, nearestPoint) = candyRoad.nearestPointTo(canvasTap)
                                if ((canvasTap - nearestPoint).getDistance() < 60f) {
                                    milestones = milestones + Milestone("New", nearestT)
                                }
                            }
                        },
                        onDrag = { change, dragAmount ->
                            selectedMilestone?.let { ms ->
                                val canvasPos = (change.position - offset) / scale
                                val (nearestT, _) = candyRoad.nearestPointTo(canvasPos)
                                ms.t = nearestT
                            }
                        }
                    )
                }
        ) {
            // Fit path on screen
            val canvasWidth = size.width
            val canvasHeight = size.height
            val scaleFit = min(canvasWidth / 1000f, canvasHeight / 2200f)

            withTransform({
                translate(offset.x, offset.y)
                scale(scale * scaleFit)
            }) {
                // Draw Candy Crush path
                val path = Path().apply {
                    moveTo(candyRoad.pointAt(0f).x, candyRoad.pointAt(0f).y)
                    val steps = 100
                    for (i in 1..steps) {
                        val p = candyRoad.pointAt(i / steps.toFloat())
                        lineTo(p.x, p.y)
                    }
                }

                drawPath(path, brush = gradient, style = Stroke(width = 32f, cap = StrokeCap.Round))

                // Draw milestones
                milestones.forEach { m ->
                    val pos = candyRoad.pointAt(m.t)
//                    val radius by animateFloatAsState(targetValue = if (selectedMilestone == m) 30f else 20f)
                    val radius = if (selectedMilestone == m) 30f else 20f
                    drawCircle(
                        color = if (selectedMilestone == m) Color(0xFFFFA500) else Color(0xFF10B981),
                        radius = radius,
                        center = pos
                    )
                }
            }
        }

        // Reset view
        FloatingActionButton(
            onClick = {
                scale = 1f
                offset = Offset.Zero
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Refresh, contentDescription = "Reset View")
        }
    }
}

// ------------------------------
// Preview
// ------------------------------
@Preview(showBackground = true, widthDp = 420, heightDp = 900)
@Composable
fun RoadmapScreenPreviewInteractive() {
    RoadMapScreen3(
        initialMilestones = listOf(
            Milestone("Orientation", 0.05f),
            Milestone("Midterms", 0.33f),
            Milestone("Finals", 0.78f)
        )
    )
}

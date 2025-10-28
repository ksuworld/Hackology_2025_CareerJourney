package com.uworld.careerjourney.old

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun RoadMapScreen8(
    modifier: Modifier = Modifier,
    initialMilestones: List<Milestone> = emptyList()
) {
    var milestones by remember { mutableStateOf(initialMilestones) }
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

    val roadBounds = candyRoad.bounds()

    val gradient = Brush.linearGradient(
        listOf(Color(0xFF3B82F6), Color(0xFF10B981), Color(0xFF9333EA)),
        start = roadBounds.first,
        end = roadBounds.second
    )

    // Bounce animation for milestones
    val infiniteTransition = rememberInfiniteTransition()
    val bounce by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(modifier = modifier.background(Color(0xFFF9FAFB))) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(0.5f, 3f)
                        offset += pan
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures { tap ->
                        val canvasTap = (tap - offset) / scale
                        val nearestT = candyRoad.nearestTTo(canvasTap)
                        milestones = milestones + Milestone("Step ${milestones.size + 1}", nearestT)
                    }
                }
        ) {
            withTransform({
                translate(offset.x, offset.y)
                scale(scale)
            }) {
                // Draw road shadow for 3D effect
                val path = Path().apply {
                    moveTo(candyRoad.pointAt(0f).x, candyRoad.pointAt(0f).y)
                    for (i in 1..100) lineTo(candyRoad.pointAt(i / 100f).x, candyRoad.pointAt(i / 100f).y)
                }
                drawPath(path, color = Color.Black.copy(alpha = 0.2f), style = Stroke(width = 36f, cap = StrokeCap.Round))

                // Draw candy road gradient
                drawPath(path, brush = gradient, style = Stroke(width = 32f, cap = StrokeCap.Round))

                // Draw milestones
                milestones.forEach { m ->
                    val pos = candyRoad.pointAt(m.t)
                    drawCircle(Color(0x55000000), radius = 28f, center = pos + Offset(0f, 5f))
                    drawCircle(
                        Brush.radialGradient(
                            colors = listOf(Color(0xFF10B981), Color(0xFF059669)),
                            center = pos,
                            radius = 28f
                        ),
                        radius = 28f,
                        center = pos + Offset(0f, -bounce) // bounce effect
                    )
                    drawCircle(Color.White, radius = 20f, center = pos + Offset(0f, -bounce))
                }
            }
        }

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

@Preview(showBackground = true)
@Composable
fun RoadMapScreenCandyRoadFullPreview() {
    RoadMapScreen8(
        initialMilestones = listOf(
            Milestone("Orientation", 0.05f),
            Milestone("Midterms", 0.33f),
            Milestone("Finals", 0.78f)
        )
    )
}

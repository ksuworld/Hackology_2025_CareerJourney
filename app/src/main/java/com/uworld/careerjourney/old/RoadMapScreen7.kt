package com.uworld.careerjourney.old

import android.annotation.SuppressLint
import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.detectDragGestures
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

@SuppressLint("AutoboxingStateCreation")
@Composable
fun RoadMapScreen7(
    modifier: Modifier = Modifier,
    initialMilestones: List<Milestone> = emptyList()
) {
    var milestones by remember { mutableStateOf(initialMilestones) }
    var scale by remember { mutableFloatStateOf(1f) }
    var roadT by remember { mutableFloatStateOf(0.0f) } // represents current scroll along road

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
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        // Project drag along road: positive drag moves forward along path, negative moves back
                        val sensitivity = 0.001f // adjust speed of movement
                        roadT = (roadT - dragAmount.y * sensitivity).coerceIn(0f, 1f)
                    }
                }
                .pointerInput(Unit) {
                    detectTransformGestures { _, _, zoom, _ ->
                        scale = (scale * zoom).coerceIn(0.5f, 3f)
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures { tap ->
                        val tapOffset = (tap / scale) // approximate tap in canvas space
                        val nearestT = candyRoad.nearestTTo(tapOffset)
                        milestones = milestones + Milestone("Step ${milestones.size + 1}", nearestT)
                    }
                }
        ) {
            withTransform({
                // compute offset so that roadT maps center of screen to current point
                val center = candyRoad.pointAt(roadT)
                val canvasCenter = Offset(size.width / 2, size.height / 2)
                translate(canvasCenter.x - center.x * scale, canvasCenter.y - center.y * scale)
                scale(scale)
            }) {
                val path = Path().apply {
                    moveTo(candyRoad.pointAt(0f).x, candyRoad.pointAt(0f).y)
                    for (i in 1..100) lineTo(candyRoad.pointAt(i / 100f).x, candyRoad.pointAt(i / 100f).y)
                }
                drawPath(path, brush = gradient, style = Stroke(width = 32f, cap = StrokeCap.Round))

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
                        center = pos
                    )
                    drawCircle(Color.White, radius = 20f, center = pos)
                    drawContext.canvas.nativeCanvas.apply {
                        drawText(
                            m.name,
                            pos.x,
                            pos.y - 30f,
                            Paint().apply {
                                color = android.graphics.Color.BLACK
                                textAlign = Paint.Align.CENTER
                                textSize = 28f
                                isFakeBoldText = true
                            }
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = {
                scale = 1f
                roadT = 0f
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
fun RoadmapScreenCandyRoadLockedPreview() {
    RoadMapScreen7(
        initialMilestones = listOf(
            Milestone("Orientation", 0.05f),
            Milestone("Midterms", 0.33f),
            Milestone("Finals", 0.78f)
        )
    )
}

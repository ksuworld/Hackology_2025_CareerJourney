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
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlin.math.*

class GameRoadPath(val segments: List<CubicSegment>) {
    data class CubicSegment(val p0: Offset, val p1: Offset, val p2: Offset, val p3: Offset)

    fun pointAt(t: Float): Offset {
        if (segments.isEmpty()) return Offset.Zero
        val segmentIndex = ((segments.size - 1) * t).coerceIn(0f, segments.size - 1f).toInt()
        val localT = ((t * segments.size) - segmentIndex).coerceIn(0f, 1f)
        val s = segments[segmentIndex]
        val u = 1 - localT
        return (s.p0 * u.pow(3)) +
                (s.p1 * 3f * u.pow(2) * localT) +
                (s.p2 * 3f * u * localT.pow(2)) +
                (s.p3 * localT.pow(3))
    }

    fun tangentAt(t: Float): Offset {
        if (segments.isEmpty()) return Offset.Zero
        val segmentIndex = ((segments.size - 1) * t).coerceIn(0f, segments.size - 1f).toInt()
        val localT = ((t * segments.size) - segmentIndex).coerceIn(0f, 1f)
        val s = segments[segmentIndex]
        val u = 1 - localT
        return (s.p1 - s.p0) * 3f * u.pow(2) +
                (s.p2 - s.p1) * 6f * u * localT +
                (s.p3 - s.p2) * 3f * localT.pow(2)
    }

    fun nearestTTo(point: Offset, steps: Int = 500): Float {
        var nearestT = 0f
        var minDist = Float.MAX_VALUE
        for (i in 0..steps) {
            val t = i / steps.toFloat()
            val p = pointAt(t)
            val dist = (p - point).getDistance()
            if (dist < minDist) {
                minDist = dist
                nearestT = t
            }
        }
        return nearestT
    }
}

private operator fun Offset.times(scalar: Float) = Offset(x * scalar, y * scalar)
private operator fun Offset.plus(other: Offset) = Offset(x + other.x, y + other.y)
private operator fun Offset.minus(other: Offset) = Offset(x - other.x, y - other.y)

@Composable
fun RoadMapScreen10(
    modifier: Modifier = Modifier,
    initialMilestones: List<Milestone> = emptyList()
) {
    var milestones by remember { mutableStateOf(initialMilestones) }
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val roadPath = remember {
        GameRoadPath(
            listOf(
                GameRoadPath.CubicSegment(
                    Offset(200f, 2000f),
                    Offset(400f, 1500f),
                    Offset(100f, 1000f),
                    Offset(400f, 800f)
                ),
                GameRoadPath.CubicSegment(
                    Offset(400f, 800f),
                    Offset(600f, 600f),
                    Offset(900f, 400f),
                    Offset(700f, 200f)
                )
            )
        )
    }

    val tileColors = listOf(
        Color(0xFFE53935), Color(0xFFFFC107), Color(0xFF43A047),
        Color(0xFF1E88E5), Color(0xFF8E24AA)
    )

    val infiniteTransition = rememberInfiniteTransition()
    val bounce by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 12f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearOutSlowInEasing),
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
                        val nearestT = roadPath.nearestTTo(canvasTap)
                        milestones = milestones + Milestone("Step ${milestones.size + 1}", nearestT)
                    }
                }
        ) {
            withTransform({
                translate(offset.x, offset.y)
                scale(scale)
            }) {
                // Draw wide road base
                val roadWidth = 100f
                val steps = 100
                val roadGradient = Brush.linearGradient(
                    listOf(Color(0xFF6B7280), Color(0xFF374151)),
                    start = Offset.Zero,
                    end = Offset(1000f, 1000f)
                )

                for (i in 0..steps) {
                    val t = i / steps.toFloat()
                    val p = roadPath.pointAt(t)
                    drawCircle(roadGradient, radius = roadWidth/2, center = p)
                    drawCircle(Color.Black.copy(alpha=0.1f), radius=roadWidth/2 + 4, center=p + Offset(0f,6f))
                }

                // Draw candy tiles along road
                val tileRadius = 18f
                for(i in 0..steps){
                    val t = i / steps.toFloat()
                    val pos = roadPath.pointAt(t)
                    val tangent = roadPath.tangentAt(t)
                    val angle = atan2(tangent.y, tangent.x) * 180f / PI.toFloat()
                    val color = tileColors[i % tileColors.size]
                    withTransform({ rotate(angle, pos) }) {
                        drawCircle(Brush.radialGradient(listOf(color, color.copy(alpha=0.7f)), pos, tileRadius),
                            radius=tileRadius, center=pos)
                        drawCircle(Color.Black.copy(alpha=0.15f), radius=tileRadius+4, center=pos+Offset(0f,4f))
                    }
                }

                // Draw floating milestones
                milestones.forEach { m ->
                    val pos = roadPath.pointAt(m.t)
                    val radius = 20f
                    drawCircle(Color.Black.copy(alpha = 0.25f), radius = radius + 6, center = pos + Offset(0f, 6f))
                    drawCircle(Brush.radialGradient(listOf(Color(0xFFFFEB3B), Color(0xFFFBC02D)), pos, radius),
                        radius = radius, center = pos + Offset(0f, -bounce))
                    drawCircle(Color.White.copy(alpha=0.3f), radius=radius/2, center=pos + Offset(0f, -bounce))
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
fun RoadMapScreenFinalCandyCrushPreview() {
    RoadMapScreen10(
        initialMilestones = listOf(
            Milestone("Orientation", 0.05f),
            Milestone("Midterms", 0.33f),
            Milestone("Finals", 0.78f)
        )
    )
}

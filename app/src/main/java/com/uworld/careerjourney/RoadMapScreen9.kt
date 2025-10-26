package com.uworld.careerjourney

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.atan2

@Composable
fun RoadMapScreen9(
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

    val colors = listOf(
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
                        val nearestT = candyRoad.nearestTTo(canvasTap)
                        milestones = milestones + Milestone("Step ${milestones.size + 1}", nearestT)
                    }
                }
        ) {
            withTransform({
                translate(offset.x, offset.y)
                scale(scale)
            }) {
                val totalSteps = 120
                for (i in 0 until totalSteps) {
                    val t0 = i / totalSteps.toFloat()
                    val t1 = (i + 1) / totalSteps.toFloat()
                    val p0 = candyRoad.pointAt(t0)
                    val p1 = candyRoad.pointAt(t1)
                    val mid = Offset((p0.x + p1.x) / 2, (p0.y + p1.y) / 2)
                    val radius = 16f

                    val tangent = candyRoad.tangentAt(t0)
                    val angle = atan2(tangent.y, tangent.x) * 180f / PI.toFloat()

                    withTransform({
                        rotate(angle, mid)
                    }) {
                        // Shadow
                        drawCircle(Color.Black.copy(alpha = 0.15f), radius = radius + 4, center = mid + Offset(0f, 4f))

                        // Candy tile
                        val color = colors[i % colors.size]
                        drawCircle(
                            Brush.radialGradient(
                                colors = listOf(color, color.copy(alpha = 0.7f)),
                                center = mid,
                                radius = radius
                            ),
                            radius = radius,
                            center = mid
                        )
                    }
                }

                // Floating milestones
                milestones.forEach { m ->
                    val pos = candyRoad.pointAt(m.t)
                    val radius = 20f
                    drawCircle(Color.Black.copy(alpha = 0.25f), radius = radius + 6, center = pos + Offset(0f, 6f))
                    drawCircle(
                        Brush.radialGradient(
                            colors = listOf(Color(0xFFFFEB3B), Color(0xFFFBC02D)),
                            center = pos,
                            radius = radius
                        ),
                        radius = radius,
                        center = pos + Offset(0f, -bounce)
                    )
                    drawCircle(Color.White.copy(alpha = 0.3f), radius = radius / 2, center = pos + Offset(0f, -bounce))
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
fun RoadMapScreenCandyCrushPolishedPreview() {
    RoadMapScreen9(
        initialMilestones = listOf(
            Milestone("Orientation", 0.05f),
            Milestone("Midterms", 0.33f),
            Milestone("Finals", 0.78f)
        )
    )
}

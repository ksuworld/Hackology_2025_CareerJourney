package com.uworld.careerjourney

import androidx.compose.animation.core.LinearEasing
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.sin

private operator fun Offset.times(scalar: Float) = Offset(x * scalar, y * scalar)
private operator fun Offset.plus(other: Offset) = Offset(x + other.x, y + other.y)
private operator fun Offset.minus(other: Offset) = Offset(x - other.x, y - other.y)

@Composable
internal fun RoadMapScreen5(
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

    val gradient = Brush.linearGradient(
        listOf(Color(0xFF3B82F6), Color(0xFF10B981), Color(0xFF9333EA)),
        start = Offset.Zero,
        end = Offset(1000f, 2200f)
    )

    val infiniteTransition = rememberInfiniteTransition()
    val elapsed = infiniteTransition.animateFloat(
        0f,
        1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
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
                        val canvasTap = Offset(
                            x = (tap.x - offset.x) / scale,
                            y = (tap.y - offset.y) / scale
                        )
                        val (nearestT, nearestPoint) = candyRoad.nearestPointTo(canvasTap)
                        if ((canvasTap - nearestPoint).getDistance() < 60f) {
                            val phase = (0..1000).random() / 1000f * 2f * PI.toFloat()
                            milestones = milestones + Milestone("Step ${milestones.size + 1}", nearestT, phase)
                        }
                    }
                }
        ) {
            withTransform({
                translate(offset.x, offset.y)
                scale(scale)
            }) {
                val path = Path().apply {
                    moveTo(candyRoad.pointAt(0f).x, candyRoad.pointAt(0f).y)
                    val steps = 100
                    for (i in 1..steps) {
                        val p = candyRoad.pointAt(i / steps.toFloat())
                        lineTo(p.x, p.y)
                    }
                }
                drawPath(path, brush = gradient, style = Stroke(width = 32f, cap = StrokeCap.Round))

                milestones.forEach { m ->
                    val pos = candyRoad.pointAt(m.t)
                    val bounceOffset = sin((elapsed.value * 2f * PI + m.phase)) * 10f
                    val animatedPos = pos + Offset(0f, bounceOffset.toFloat())

                    // Draw pseudo-3D shadow
                    drawCircle(Color(0x55000000), radius = 28f, center = animatedPos + Offset(5f, 5f))

                    // Draw candy
                    drawCircle(Color(0xFF10B981), radius = 28f, center = animatedPos)
                    drawCircle(Color.White, radius = 20f, center = animatedPos)

                    // Draw label floating above candy
                    drawContext.canvas.nativeCanvas.apply {
                        drawText(
                            m.name,
                            animatedPos.x,
                            animatedPos.y - 30f,
                            android.graphics.Paint().apply {
                                color = android.graphics.Color.BLACK
                                textAlign = android.graphics.Paint.Align.CENTER
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
fun RoadmapScreen5Preview() {
    RoadMapScreen5(
        initialMilestones = listOf(
            Milestone("Orientation", 0.05f),
            Milestone("Midterms", 0.33f),
            Milestone("Finals", 0.78f)
        )
    )
}

package com.uworld.careerjourney

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Popup
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.launch
import kotlin.math.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoadMapScreen11(
    modifier: Modifier = Modifier,
    initialMilestones: List<Milestone> = emptyList(),
    onMilestonesChanged: ((List<Milestone>) -> Unit)? = null
) {
    var milestones by remember { mutableStateOf(initialMilestones.toMutableList()) }
    var scale by remember { mutableStateOf(1f) }

    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }

    val roadPath = remember {
        CandyRoadPath(
            listOf(
                CandyRoadPath.CubicSegment(
                    Offset(200f, 2400f),
                    Offset(400f, 1600f),
                    Offset(100f, 1100f),
                    Offset(420f, 800f)
                ),
                CandyRoadPath.CubicSegment(
                    Offset(420f, 800f),
                    Offset(760f, 600f),
                    Offset(1000f, 420f),
                    Offset(700f, 200f)
                )
            )
        )
    }

    // Center road path initially
    val initialOffset = remember {
        val center = roadPath.pointAt(0.5f)
        Offset(
            (screenWidthPx / 2f) - center.x,
            (screenHeightPx / 2f) - center.y
        )
    }

    var offset by remember { mutableStateOf(initialOffset) }

    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    var showPopup by remember { mutableStateOf(false) }

    val infinite = rememberInfiniteTransition()
    val floatOffset by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            tween(900, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        )
    )

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                scale = 1f
                offset = initialOffset
            }) {
                Icon(Icons.Default.Refresh, contentDescription = "Reset Zoom")
            }
        }
    ) { padding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFFF9FAFB))
                .padding(padding)
        ) {
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
                            val hitIndex = milestones.indexOfFirst { m ->
                                val pos = roadPath.pointAt(m.t)
                                (pos - canvasTap).getDistance() <= 36f
                            }

                            if (hitIndex >= 0) {
                                selectedIndex = hitIndex
                                showPopup = true
                            } else {
                                val (nearestT, _) = roadPath.nearestPointTo(canvasTap)
                                milestones = (milestones + Milestone("Step ${milestones.size + 1}", nearestT)).toMutableList()
                                onMilestonesChanged?.invoke(milestones)
                            }
                        }
                    }
            ) {
                withTransform({
                    translate(offset.x, offset.y)
                    scale(scale)
                }) {
                    val steps = 200
                    val roadPathShape = Path().apply {
                        val first = roadPath.pointAt(0f)
                        moveTo(first.x, first.y)
                        for (i in 1..steps) {
                            val p = roadPath.pointAt(i / steps.toFloat())
                            lineTo(p.x, p.y)
                        }
                    }

                    // Shadow under road
                    drawPath(
                        roadPathShape,
                        color = Color.Black.copy(alpha = 0.18f),
                        style = Stroke(width = 140f, cap = StrokeCap.Round)
                    )

                    // Road
                    drawPath(
                        roadPathShape,
                        brush = Brush.linearGradient(
                            listOf(
                                Color(0xFF4F46E5),
                                Color(0xFF06B6D4),
                                Color(0xFF10B981)
                            )
                        ),
                        style = Stroke(width = 110f, cap = StrokeCap.Round)
                    )

                    // Center highlight
                    drawPath(
                        roadPathShape,
                        color = Color.White.copy(alpha = 0.09f),
                        style = Stroke(width = 8f, cap = StrokeCap.Round)
                    )

                    milestones.forEachIndexed { idx, m ->
                        val pos = roadPath.pointAt(m.t)
                        val floatY = -floatOffset
                        val center = pos + Offset(0f, floatY)

                        // shadow
                        drawCircle(
                            Color.Black.copy(alpha = 0.28f),
                            radius = 26f,
                            center = pos + Offset(0f, 8f)
                        )

                        // milestone body (aligned with shadow)
                        drawCircle(
                            Brush.radialGradient(
                                colors = listOf(Color(0xFFFFE082), Color(0xFFF57C00)),
                                center = center,
                                radius = 22f
                            ),
                            radius = 22f,
                            center = center
                        )

                        drawCircle(
                            Color.White.copy(alpha = 0.32f),
                            radius = 10f,
                            center = center + Offset(-6f, -6f)
                        )

                        // label
                        drawContext.canvas.nativeCanvas.apply {
                            val paint = android.graphics.Paint().apply {
                                color = android.graphics.Color.BLACK
                                textAlign = android.graphics.Paint.Align.CENTER
                                textSize = 28f
                                isFakeBoldText = true
                            }
                            drawText(m.name, center.x, center.y + 42f, paint)
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RoadMapCenteredInteractivePreview() {
    RoadMapScreen11(
        initialMilestones = listOf(
            Milestone("Start", 0.05f),
            Milestone("Mid", 0.5f),
            Milestone("End", 0.9f)
        )
    )
}

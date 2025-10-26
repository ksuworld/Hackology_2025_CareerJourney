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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
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

class RoadPath13(val segments: List<CubicSegment>) {
    data class CubicSegment(val p0: Offset, val p1: Offset, val p2: Offset, val p3: Offset)

    fun pointAt(t: Float): Offset {
        if (segments.isEmpty()) return Offset.Zero
        val segF = (t * segments.size).coerceIn(0f, segments.size.toFloat())
        val segmentIndex = min((segF - 0.0001f).toInt().coerceAtLeast(0), segments.size - 1)
        val localT = (segF - segmentIndex).coerceIn(0f, 1f)
        return bezierPoint(segments[segmentIndex], localT)
    }

    private fun bezierPoint(s: CubicSegment, t: Float): Offset {
        val u = 1 - t
        val b0 = u * u * u
        val b1 = 3 * u * u * t
        val b2 = 3 * u * t * t
        val b3 = t * t * t
        return (s.p0 * b0) + (s.p1 * b1) + (s.p2 * b2) + (s.p3 * b3)
    }

    fun nearestPointTo(point: Offset, steps: Int = 800): Pair<Float, Offset> {
        var bestT = 0f
        var bestP = Offset.Zero
        var minDist = Float.MAX_VALUE
        for (i in 0..steps) {
            val t = i / steps.toFloat()
            val p = pointAt(t)
            val d = (p - point).getDistance()
            if (d < minDist) {
                minDist = d
                bestT = t
                bestP = p
            }
        }
        return bestT to bestP
    }
}

/** ---------- Main Composable ---------- **/

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoadMapScreen13(
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
        RoadPath13(
            listOf(
                RoadPath13.CubicSegment(
                    Offset(200f, 2400f),
                    Offset(400f, 1600f),
                    Offset(100f, 1100f),
                    Offset(420f, 800f)
                ),
                RoadPath13.CubicSegment(
                    Offset(420f, 800f),
                    Offset(760f, 600f),
                    Offset(1000f, 420f),
                    Offset(700f, 200f)
                )
            )
        )
    }

    // Calculate initial scale to fit road with padding
    val roadBounds = remember {
        val allPoints = (0..200).map { roadPath.pointAt(it / 200f) }
        val minX = allPoints.minOf { it.x }
        val maxX = allPoints.maxOf { it.x }
        val minY = allPoints.minOf { it.y }
        val maxY = allPoints.maxOf { it.y }
        Rect(minX, minY, maxX, maxY)
    }
    val paddingPx = 60f
    val scaleX = (screenWidthPx - 2 * paddingPx) / (roadBounds.right - roadBounds.left)
    val scaleY = (screenHeightPx - 2 * paddingPx) / (roadBounds.bottom - roadBounds.top)
    val initialScale = min(scaleX, scaleY)

    scale = initialScale

    val initialOffset = remember {
        Offset(
            (screenWidthPx / 2f) - ((roadBounds.left + roadBounds.right) / 2f) * initialScale,
            (screenHeightPx / 2f) - ((roadBounds.top + roadBounds.bottom) / 2f) * initialScale
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

    // Store last tap position for popup animation origin
    var lastTap by remember { mutableStateOf(Offset.Zero) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                scale = initialScale
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
                            lastTap = tap
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

                    // Road shadow
                    drawPath(
                        roadPathShape,
                        color = Color.Black.copy(alpha = 0.18f),
                        style = Stroke(width = 140f, cap = StrokeCap.Round)
                    )

                    // Road gradient
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

                    drawPath(
                        roadPathShape,
                        color = Color.White.copy(alpha = 0.09f),
                        style = Stroke(width = 8f, cap = StrokeCap.Round)
                    )

                    milestones.forEachIndexed { idx, m ->
                        val pos = roadPath.pointAt(m.t)
                        val center = pos + Offset(0f, -floatOffset)

                        drawCircle(
                            Color.Black.copy(alpha = 0.28f),
                            radius = 26f,
                            center = pos + Offset(0f, 8f)
                        )

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

            val selIdx = selectedIndex?.takeIf { it in milestones.indices }
            val selected = selIdx?.let { milestones[it] }

            if (selected != null && showPopup && selIdx != null) {
                Popup(alignment = Alignment.Center) {
                    AnimatedPopupCard(
                        initialName = selected.name,
                        origin = lastTap,
                        onSave = { newName ->
                            milestones = milestones.toMutableList().also {
                                it[selIdx] = it[selIdx].copy(name = newName)
                            }
                            onMilestonesChanged?.invoke(milestones)
                            showPopup = false
                            selectedIndex = null
                        },
                        onDelete = {
                            milestones = milestones.toMutableList().also { it.removeAt(selIdx) }
                            onMilestonesChanged?.invoke(milestones)
                            showPopup = false
                            selectedIndex = null
                        },
                        onDismiss = {
                            showPopup = false
                            selectedIndex = null
                        }
                    )
                }
            }
        }
    }
}

/** ---------- Animated Popup Card ---------- **/

@Composable
private fun AnimatedPopupCard(
    initialName: String,
    origin: Offset,
    onSave: (String) -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf(initialName) }
    val animSpec = tween<Float>(durationMillis = 300, easing = FastOutSlowInEasing)

    val scale = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        scale.animateTo(1f, animSpec)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.35f))
            .pointerInput(Unit) {
                detectTapGestures { onDismiss() }
            },
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .scale(scale.value)
                .width(300.dp)
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(Color.White)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Edit Milestone", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(onClick = { onSave(text) }) { Text("Save") }
                    Button(onClick = onDelete, colors = ButtonDefaults.buttonColors(Color.Red)) { Text("Delete") }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RoadMapFinalPreview() {
    RoadMapScreen13(
        initialMilestones = listOf(
            Milestone("Start", 0.05f),
            Milestone("Mid", 0.5f),
            Milestone("End", 0.9f)
        )
    )
}

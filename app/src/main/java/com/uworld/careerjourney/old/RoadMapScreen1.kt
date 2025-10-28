package com.uworld.careerjourney.old

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlin.math.min

// ---------------------------------------------
// Main Roadmap Composable
// ---------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoadMapScreen1(
    modifier: Modifier = Modifier,
    previewMilestones: List<Milestone>? = null,
    isPreviewMode: Boolean = false
) {

    var milestones by remember { mutableStateOf(previewMilestones ?: emptyList()) }
    var selectedMilestone by remember { mutableStateOf<Milestone?>(null) }

    // Zoom & pan states
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    // Build Candy Crush style path
    val baseWidth = 1000f
    val baseHeight = 2200f
    val pathSegments = remember {
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
    }
    val candyRoad = remember { CandyRoadPath(pathSegments) }

    // Gradient brush for road
    val gradient = Brush.linearGradient(
        listOf(Color(0xFF3B82F6), Color(0xFF10B981), Color(0xFF9333EA)),
        start = Offset.Zero,
        end = Offset(baseWidth, baseHeight)
    )

    // Coordinate conversion for gestures
    fun Offset.toCanvasSpace(scale: Float, offset: Offset): Offset = (this - offset) / scale

    // Gesture detection
    val pointerModifier = if (!isPreviewMode) {
        Modifier
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(0.5f, 3f)
                    offset += pan
                }
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { tap ->
                        val canvasTap = tap.toCanvasSpace(scale, offset)
                        val (nearestT, nearestPoint) = candyRoad.nearestPointTo(canvasTap)
                        val distance = (canvasTap - nearestPoint).getDistance()
                        if (distance < 60f) {
                            // If close to path, add milestone
                            val newMilestone = Milestone("New", nearestT)
                            milestones = milestones + newMilestone
                        } else {
                            selectedMilestone = null
                        }
                    },
                    onLongPress = { longPress ->
                        val canvasTap = longPress.toCanvasSpace(scale, offset)
                        val (nearestT, nearestPoint) = candyRoad.nearestPointTo(canvasTap)
                        val hit = milestones.minByOrNull { (candyRoad.pointAt(it.t) - canvasTap).getDistance() }
                        if (hit != null && (candyRoad.pointAt(hit.t) - canvasTap).getDistance() < 60f) {
                            selectedMilestone = hit
                        }
                    }
                )
            }
    } else Modifier

    // Animated dragging (only in interactive mode)
    if (!isPreviewMode && selectedMilestone != null) {
        LaunchedEffect(selectedMilestone) {
            // Example of drag animation — user can later move by long press + drag
        }
    }

    CandyRoadWrapper(
        modifier = modifier,
        milestones = milestones,
        selectedMilestone = selectedMilestone,
        candyRoad = candyRoad,
        pathSegments = pathSegments,
        gradient = gradient,
        isPreviewMode = isPreviewMode,
        pointerModifier = pointerModifier
    )
//    Box(modifier = modifier.background(Color(0xFFF9FAFB))) {
//        Canvas(
//            modifier = Modifier
//                .fillMaxSize()
//                .then(pointerModifier)
//        ) {
//            val canvasWidth = size.width
//            val canvasHeight = size.height
//
//            // Auto-fit to screen
//            val scaleFit = min(canvasWidth / baseWidth, canvasHeight / baseHeight)
//            val totalScale = scaleFit * scale
//
//            withTransform({
//                translate(offset.x, offset.y)
//                scale(totalScale)
//            }) {
//                val roadPath = Path().apply {
//                    moveTo(pathSegments.first().start.x, pathSegments.first().start.y)
//                    pathSegments.forEach {
//                        cubicTo(
//                            it.cp1.x, it.cp1.y,
//                            it.cp2.x, it.cp2.y,
//                            it.end.x, it.end.y
//                        )
//                    }
//                }
//
//                drawPath(
//                    path = roadPath,
//                    brush = gradient,
//                    style = Stroke(width = 32f, cap = StrokeCap.Round)
//                )
//
//                milestones.forEach { m ->
//                    val pos = candyRoad.pointAt(m.t)
//                    val animRadius by animateFloatAsState(
//                        targetValue = if (selectedMilestone == m) 30f else 20f
//                    )
//                    drawCircle(
//                        color = if (selectedMilestone == m) Color(0xFFFFA500) else Color(0xFF10B981),
//                        radius = animRadius,
//                        center = pos
//                    )
//                }
//            }
//        }
//
//        if (!isPreviewMode) {
//            ExtendedFloatingActionButton(
//                text = { Text("Reset View") },
//                icon = { Icon(Icons.Default.Refresh, contentDescription = "Reset") },
//                onClick = {
//                    scale = 1f
//                    offset = Offset.Zero
//                },
//                modifier = Modifier
//                    .align(Alignment.BottomStart)
//                    .padding(16.dp)
//            )
//        }
//    }
}

@Composable
fun CandyRoadWrapper(
    modifier: Modifier = Modifier,
    milestones: List<Milestone>,
    selectedMilestone: Milestone?,
    candyRoad: CandyRoadPath,
    pathSegments: List<CandyRoadPath.CubicSegment>,
    gradient: Brush,
    isPreviewMode: Boolean,
    pointerModifier: Modifier = Modifier
) {
    // Make scale and offset mutable states
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    // Precompute animated radii for milestones
    val milestoneRadii = milestones.associateWith { m ->
        animateFloatAsState(targetValue = if (selectedMilestone == m) 30f else 20f).value
    }

    Box(modifier = modifier.background(Color(0xFFF9FAFB))) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .then(pointerModifier)
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            val baseWidth = 1000f
            val baseHeight = 2200f

            // Auto-fit to screen
            val scaleFit = min(canvasWidth / baseWidth, canvasHeight / baseHeight)
            val totalScale = scaleFit * scale

            withTransform({
                translate(offset.x, offset.y)
                scale(totalScale)
            }) {
                // Draw road
                val roadPath = Path().apply {
                    moveTo(pathSegments.first().p0.x, pathSegments.first().p0.y)
                    pathSegments.forEach {
                        cubicTo(it.p1.x, it.p1.y, it.p2.x, it.p2.y, it.p3.x, it.p3.y)
                    }
                }

                drawPath(
                    path = roadPath,
                    brush = gradient,
                    style = Stroke(width = 32f, cap = StrokeCap.Round)
                )

                // Draw milestones using precomputed animated radii
                milestones.forEach { m ->
                    val pos = candyRoad.pointAt(m.t)
                    val radius = milestoneRadii[m] ?: 20f
                    drawCircle(
                        color = if (selectedMilestone == m) Color(0xFFFFA500) else Color(0xFF10B981),
                        radius = radius,
                        center = pos
                    )
                }
            }
        }

        if (!isPreviewMode) {
            ExtendedFloatingActionButton(
                text = { Text("Reset View") },
                icon = { Icon(Icons.Default.Refresh, contentDescription = "Reset") },
                onClick = {
                    scale = 1f
                    offset = Offset.Zero
                },
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            )
        }
    }
}

@Preview(
    name = "Candy Crush Style Roadmap",
    showSystemUi = true,
    showBackground = true,
    backgroundColor = 0xFFF7F8FA,
    widthDp = 420,
    heightDp = 900
)

@Composable
fun RoadmapMapScreen1Preview() {
    val sampleMilestones = listOf(
        Milestone("Orientation", 0.05f),
        Milestone("Join Club", 0.18f),
        Milestone("Midterms", 0.33f),
        Milestone("Internship", 0.52f),
        Milestone("Finals", 0.78f),
        Milestone("Graduation", 0.96f)
    )
    RoadMapScreen1(
        modifier = Modifier.fillMaxSize(),
        previewMilestones = sampleMilestones,
        isPreviewMode = true
    )
}

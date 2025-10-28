package com.uworld.careerjourney.sample_journey

import android.annotation.SuppressLint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import java.util.Calendar
import java.util.Date
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("MutableCollectionMutableState")
@Composable
fun RoadmapScreen(journey: String, onBack: () -> Unit) {
    val defaultMilestones = remember { sampleMilestonesFor(journey) }
    var milestones by remember { mutableStateOf(defaultMilestones.toMutableList()) }
    var selectedMilestone by remember { mutableStateOf<Milestone?>(null) }
    var showThemeSettings by remember { mutableStateOf(false) }
    var themeState by remember { mutableStateOf(ThemeState.default()) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("$journey Roadmap") }, navigationIcon = {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Back") }
            }, actions = {
                IconButton(onClick = { showThemeSettings = true }) {
                    Icon(Icons.Default.Settings, contentDescription = "Theme")
                }
            })
        }
    ) { inset ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(inset)) {
            // background decorative image (optional)
            Image(
                painter = painterResource(id = com.uworld.careerjourney.R.drawable.roadmap_bg), contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .align(Alignment.TopCenter)
            )

            // stylized path + clickable milestones
            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Top) {
                Spacer(modifier = Modifier.height(80.dp))
                Box(modifier = Modifier.fillMaxSize()) {
                    PathCanvas(milestones = milestones, themeState = themeState, onMilestoneClicked = {
                        selectedMilestone = it
                    })

                    // bottom area, list of milestones
                    Column(modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(12.dp)) {
                        Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(6.dp)) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("Milestones (${milestones.count { it.isMain }})", style = MaterialTheme.typography.titleSmall)
                                Spacer(modifier = Modifier.weight(1f))
                                Button(onClick = {
                                    /* add a quick checkpoint */
                                    milestones.add(
                                        Milestone(
                                            UUID.randomUUID().toString(),
                                            "New Checkpoint",
                                            "small checkpoint",
                                            Date(),
                                            isMain = false
                                        )
                                    )
                                }) {
                                    Text("Add checkpoint")
                                }
                            }
                        }
                    }
                }
            }
        }

        // Theme settings
        if (showThemeSettings) {
            ThemeSettingsDialog(themeState = themeState, onDismiss = { showThemeSettings = false }, onSave = {
                themeState = it
                showThemeSettings = false
            })
        }

        // Milestone details popup
        selectedMilestone?.let { m ->
            MilestoneDetailsPopup(
                milestone = m,
                onDismiss = { selectedMilestone = null },
                onUpdate = { updated ->
                    milestones = milestones.map { if (it.id == updated.id) updated else it }.toMutableList()
                    selectedMilestone = updated
                }
            )
        }
    }
}

/** small theme holder for image / path gradient */
data class ThemeState(val primary: Long, val secondary: Long) {
    companion object {
        fun default() = ThemeState(0xFF1E88E5, 0xFF42A5F5)
        fun sunset() = ThemeState(0xFFFB8C00, 0xFFFFCA28)
    }
}

@Composable
fun PathCanvas(milestones: List<Milestone>, themeState: ThemeState, onMilestoneClicked: (Milestone) -> Unit) {
    // Very simple curved path with circles at positions representing milestones
    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val path = Path().apply {
                moveTo(w * 0.1f, h * 0.2f)
                cubicTo(w * 0.4f, h * 0.35f, w * 0.2f, h * 0.6f, w * 0.6f, h * 0.7f)
                cubicTo(w * 0.85f, h * 0.75f, w * 0.4f, h * 0.95f, w * 0.6f, h * 0.9f)
            }
            drawPath(
                path = path,
                color = androidx.compose.ui.graphics.Color(themeState.primary),
                style = Stroke(width = 34f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
            )
            // draw milestones along the path (sampled)
            val positions = listOf(0.05f, 0.25f, 0.48f, 0.72f, 0.92f)
            positions.take(milestones.size).forEachIndexed { idx, t ->
                val point = pathMeasurePoint(path = path, t = t)
                drawCircle(
                    color = androidx.compose.ui.graphics.Color(themeState.secondary),
                    radius = if (milestones[idx].isMain) 28f else 16f,
                    center = point
                )
            }
        }

        // overlay clickable hotspots using pointers
        val positions = listOf(0.05f, 0.25f, 0.48f, 0.72f, 0.92f)
        positions.take(milestones.size).forEachIndexed { idx, t ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(idx) {
                        detectTapGestures { offset ->
                            // naive hit testing: compute where the path point is and check distance
                            // We'll compute approximate coords same as the canvas draw routine
                            val w = size.width.toFloat()
                            val h = size.height.toFloat()
                            val path = androidx.compose.ui.graphics.Path().apply {
                                moveTo(w * 0.1f, h * 0.2f)
                                cubicTo(w * 0.4f, h * 0.35f, w * 0.2f, h * 0.6f, w * 0.6f, h * 0.7f)
                                cubicTo(w * 0.85f, h * 0.75f, w * 0.4f, h * 0.95f, w * 0.6f, h * 0.9f)
                            }
                            val pt = pathMeasurePoint(path = path, t = t)
                            val dx = offset.x - pt.x
                            val dy = offset.y - pt.y
                            if (dx * dx + dy * dy < 60f * 60f) {
                                onMilestoneClicked(milestones[idx])
                            }
                        }
                    }
            ) {}
        }
    }
}

/** Naive path measure function approximating a point along cubic curve t in [0..1] by sampling */
fun pathMeasurePoint(path: Path, t: Float): Offset {
    // This is a quick approximation — for accurate results use PathMeasure via Android Canvas (omitted for brevity)
    // We'll sample using cubic Bezier control points used earlier. Coordinates should match draw.
    // For simplicity, mirror the same control points
    // NOTE: Keep in sync with the Canvas path in PathCanvas
    // cubic: p0 -> p1 -> p2 -> p3 (we used two curves but return approximate for the whole t)
    val p0 = Offset(0.1f, 0.2f)
    val p1 = Offset(0.4f, 0.35f)
    val p2 = Offset(0.2f, 0.6f)
    val p3 = Offset(0.6f, 0.7f)
    val p4 = Offset(0.85f, 0.75f)
    val p5 = Offset(0.4f, 0.95f)
    val p6 = Offset(0.6f, 0.9f)
    val w = 1000f // placeholder scale factor; we will multiply later
    return if (t < 0.6f) {
        val s = t / 0.6f
        val x = cubic(s, p0.x, p1.x, p2.x, p3.x)
        val y = cubic(s, p0.y, p1.y, p2.y, p3.y)
        Offset(x * w, y * w)
    } else {
        val s = (t - 0.6f) / 0.4f
        val x = cubic(s, p3.x, p4.x, p5.x, p6.x)
        val y = cubic(s, p3.y, p4.y, p5.y, p6.y)
        Offset(x * w, y * w)
    }
}

fun cubic(t: Float, p0: Float, p1: Float, p2: Float, p3: Float): Float {
    val u = 1 - t
    return u * u * u * p0 + 3 * u * u * t * p1 + 3 * u * t * t * p2 + t * t * t * p3
}

fun sampleMilestonesFor(journey: String): List<Milestone> {
    val cal = Calendar.getInstance()
    val list = mutableListOf<Milestone>()
    when (JourneyType.fromLabel(journey)) {
        JourneyType.PRE_COLLEGE -> {
            cal.add(Calendar.MONTH, 2); list.add(Milestone("m1", "SAT/ACT Prep", "Start test prep: weekly plan, diagnostics", cal.time, true))
            cal.add(Calendar.MONTH, 4); list.add(Milestone("m2", "College Shortlist", "Pick target & safety colleges", cal.time, true))
            cal.add(Calendar.MONTH, 7); list.add(Milestone("m3", "Applications", "Complete apps, essays, financial aid", cal.time, true))
        }

        JourneyType.IN_COLLEGE -> {
            cal.add(Calendar.MONTH, 1); list.add(Milestone("m4", "Major Declaration", "Choose major & advisor", cal.time, true))
            cal.add(Calendar.MONTH, 6); list.add(Milestone("m5", "Internship Hunt", "Start applying", cal.time, true))
        }

        JourneyType.AFTER_COLLEGE -> {
            cal.add(Calendar.MONTH, 2); list.add(Milestone("m6", "Resume Ready", "Tailor your resume", cal.time, true))
            cal.add(Calendar.MONTH, 5); list.add(Milestone("m7", "Job Interviews", "Mock interviews & prep", cal.time, true))
        }
    }
    return list
}

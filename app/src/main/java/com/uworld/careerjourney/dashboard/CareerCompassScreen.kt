package com.uworld.careerjourney.dashboard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.uworld.careerjourney.InteractiveRoadmapScreenCanvas
import com.uworld.careerjourney.R
import com.uworld.careerjourney.sample_journey.ThemeSettingsDialog
import com.uworld.careerjourney.sample_journey.ThemeState
import java.time.LocalDate

// ---------------------------
// Single-file Career Compass Screen
// ---------------------------
// Usage: setContent { CareerCompassApp() }
// This file contains:
// - NavHost with Dashboard, MilestoneDetail, Chat screens
// - Dashboard UI modeled after provided image (three journey cards, overall progress, gem icon -> Chat)
// - Dummy data for progress bars and milestones
// - Clickable milestones and buttons which navigate to placeholder screens
// ---------------------------

/** Launching Activity (example) */
class CareerCompassActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CareerCompassApp()
        }
    }
}

/** Entry composable with Navigation */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CareerCompassApp() {
    val nav = rememberNavController()
    var showThemeSettings by remember { mutableStateOf(false) }
    var themeState by remember { mutableStateOf(ThemeState.default()) }

    MaterialTheme {
        NavHost(navController = nav, startDestination = "dashboard") {
            composable("dashboard") {
                DashboardScreen(
                    onNavigateToThemeSelection = { nav.navigate("themeSelection") },
                    onNavigateToMilestone = { id -> nav.navigate("milestone/$id") },
                    onNavigateToMap = { id -> nav.navigate("map") },
                    onNavigateChat = { nav.navigate("chat") })
            }
            composable("milestone/{id}", arguments = listOf(navArgument("id") { type = NavType.IntType })) { navBack ->
                val id = navBack.arguments?.getInt("id") ?: 0
                MilestoneDetailScreen(milestoneId = id, onBack = { nav.popBackStack() })
            }
            composable("checkpointsList", arguments = listOf(navArgument("id") { type = NavType.StringType })) { navBack ->
                val id = navBack.arguments?.getString("id") ?: ""
                ChecklistScreen(journeyId = id)
            }
            composable("chat") {
                ChatPlaceholderScreen(onBack = { nav.popBackStack() })
            }
            composable("map") {
                InteractiveRoadmapScreenCanvas(
                    imageResId = R.drawable.roadmap_vector_blue,
                    startDate = LocalDate.now().minusDays(100),
                    endDate = LocalDate.now().plusDays(100)
                )
            }
            composable("themeSelection") { backStackEntry ->
                ThemeSettingsDialog(
                    themeState = themeState,
                    onDismiss = { showThemeSettings = false },
                    onSave = {
                        themeState = it
                        showThemeSettings = false
                    })
            }
        }
    }
}

/** Colors tuned to the reference image (purple, green, orange, and background) */
private val BG = Color(0xFF2F3742)
private val BG1 = Color(0xFFEEEEF1)
private val BG2 = Color(0xFF0A0A0A)
private val CardBg = Color(0xFFEEF2F5)
private val PurpleHeader = Color(0xFFBFA9EA) // pastel purple
private val GreenHeader = Color(0xFFABD39F) // pastel green
private val OrangeHeader = Color(0xFFECB88D) // pastel orange
private val AccentText = Color(0xFF35404A)
private val Muted = Color(0xFF9DA6B0)

private var onGoingJourneyTypeId = "JNY-003"

/** Dashboard composable */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToThemeSelection: (() -> Unit)? = null,
    onNavigateToMilestone: ((Int) -> Unit)? = null,
    onNavigateToMap: ((String) -> Unit)? = null,
    onNavigateChat: (() -> Unit)? = null
) {
    Surface(modifier = Modifier.fillMaxSize(), color = BG1) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(16.dp)
        ) {

            item { // single item for the main content
                // Title row with overall progress pill and gem/chat icon
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "CAREER COMPASS",
                        style = MaterialTheme.typography.titleLarge.copy(color = AccentText, fontWeight = FontWeight.ExtraBold)
                    )
                    Spacer(modifier = Modifier.weight(1f))

                    // map icon circle (navigates to map - not implemented)
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF3C4752))
                            .clickable { onNavigateToMap?.invoke(onGoingJourneyTypeId) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Map, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // gem/star icon circle (navigates to chat)
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF3C4752))
                            .clickable { onNavigateToThemeSelection?.invoke() },
                        contentAlignment = Alignment.Center
                    ) {
//                        GemIcon(size = 22.dp, tint = Color(0xFFDEEAFB))
                        Icon(Icons.Default.ColorLens, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Overall progress pill
                OverallProgressPill(label = "Overall Progress: 35% Complete", progress = 0.35f)

                Spacer(modifier = Modifier.height(18.dp))

                // Journey cards
                // Using Column with spaced cards
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(18.dp)) {
                    // Pre-College
                    JourneyCard(
                        headerColor = PurpleHeader,
                        iconContent = {
                            Icon(Icons.Default.School, contentDescription = null, tint = AccentText, modifier = Modifier.size(32.dp))
//                            Box(
//                                modifier = Modifier
//                                    .size(44.dp)
//                                    .background(PurpleHeader, shape = CircleShape), contentAlignment = Alignment.Center
//                            ) {
//                                // simple mortarboard glyph (circle with cap)
//                                Canvas(modifier = Modifier.size(28.dp)) {
//                                    val w = size.width
//                                    val h = size.height
//                                    drawCircle(Color.White, radius = w * 0.45f, center = Offset(w * 0.5f, h * 0.6f))
//                                    drawRect(
//                                        Color.White,
//                                        topLeft = Offset(w * 0.05f, h * 0.0f),
//                                        size = androidx.compose.ui.geometry.Size(w * 0.9f, h * 0.25f)
//                                    )
//                                }
//                            }
                        },
                        title = "PRE-COLLEGE JOURNEY",
                        description = "Test Prep, College Applications, Scholarships",
                        cards = listOf(
                            SubCardData(
                                "High School Exploration",
                                0.75f,
                                "75%",
                                buttonLabel = "New",
                                showMilestones = true,
                                status = StageStatus.COMPLETED
                            ),
                            SubCardData(
                                "College Application",
                                0.80f,
                                "80%",
                                buttonLabel = "Sew",
                                showMilestones = true,
                                status = StageStatus.COMPLETED
                            )
                        ),
                        onMilestoneClick = {
                            onNavigateToMilestone?.invoke(it)
                        }
                    )

                    // In-College
                    JourneyCard(
                        headerColor = GreenHeader,
                        iconContent = {
                            Icon(Icons.Default.AccountBalance, contentDescription = null, tint = AccentText, modifier = Modifier.size(32.dp))
//
//                            Box(
//                                modifier = Modifier
//                                    .size(44.dp)
//                                    .background(GreenHeader, shape = CircleShape), contentAlignment = Alignment.Center
//                            ) {
//                                Canvas(modifier = Modifier.size(28.dp)) {
//                                    val w = size.width
//                                    val h = size.height
//                                    drawRoundRect(
//                                        Color.White,
//                                        cornerRadius = CornerRadius(4f, 4f),
//                                        size = androidx.compose.ui.geometry.Size(w, h * 0.6f)
//                                    )
//                                }
//                            }
                        },
                        title = "IN-COLLEGE JOURNEY",
                        description = "Internships, Majors, Campus Life",
                        cards = listOf(
                            SubCardData("Freshman Year", 0.4f, "40%", status = StageStatus.CURRENT_FOCUS),
                            SubCardData("Sophomore Year", 0.35f, "35%", buttonLabel = "Start Checklist"),
                            SubCardData("Junior", 0.12f, "12%", showMilestones = true)
                        ),
                        onMilestoneClick = {
                            onNavigateToMilestone?.invoke(it)
                        }
                    )

                    // After-College
                    JourneyCard(
                        headerColor = OrangeHeader,
                        iconContent = {
                            Icon(Icons.Default.BusinessCenter, contentDescription = null, tint = AccentText, modifier = Modifier.size(32.dp))
//
//                            Box(
//                                modifier = Modifier
//                                    .size(44.dp)
//                                    .background(OrangeHeader, shape = CircleShape), contentAlignment = Alignment.Center
//                            ) {
//                                Canvas(modifier = Modifier.size(28.dp)) {
//                                    val w = size.width
//                                    val h = size.height
//                                    drawRoundRect(
//                                        Color.White,
//                                        cornerRadius = CornerRadius(6f, 6f),
//                                        size = androidx.compose.ui.geometry.Size(w * 0.8f, h * 0.5f)
//                                    )
//                                    drawRect(
//                                        Color.White,
//                                        topLeft = Offset(w * 0.2f, h * 0.55f),
//                                        size = androidx.compose.ui.geometry.Size(w * 0.4f, h * 0.2f)
//                                    )
//                                }
//                            }
                        },
                        title = "AFTER-COLLEGE JOURNEY",
                        description = "Grad School, Job Search, Early Career",
                        cards = listOf(
                            SubCardData("Transition to Work", 0.0f, "0%", showMilestones = true),
                            SubCardData("Early Career Growth", 0.0f, "0%", showMilestones = true)
                        ),
                        onMilestoneClick = {
                            onNavigateToMilestone?.invoke(it)
                        }
                    )
                }
            }
        }

        // Floating chat button
        FloatingAIChatButton { onNavigateChat?.invoke() }
    }
}

/** Overall progress pill UI */
@Composable
fun OverallProgressPill(label: String, progress: Float) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(color = AccentText, shape = RoundedCornerShape(24.dp))
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // small user circle
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(Color(0xFF56606A), shape = CircleShape), contentAlignment = Alignment.Center
        ) {
            Text("U", color = Color.White, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = label, color = Color.White, fontSize = 14.sp)
        Spacer(modifier = Modifier.weight(1f))
        // mini progress ring + percent
        CircularProgressIndicator(
            progress = { progress },
            modifier = Modifier.size(20.dp),
//            color = Color(0xFF6AB0FF),
            color = Color.White,
            strokeWidth = 3.dp,
            trackColor = Color(0xFF595C5E),
            strokeCap = ProgressIndicatorDefaults.CircularDeterminateStrokeCap,
        )
    }
}

/** JourneyCard layout with header + content cards */
@Composable
fun JourneyCard(
    headerColor: Color,
    iconContent: @Composable () -> Unit,
    title: String,
    description: String,
    cards: List<SubCardData>,
    onMilestoneClick: (Int) -> Unit
) {
    Spacer(modifier = Modifier.size(3.dp))
    // Card container
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column {
            // Header strip with rounded left circle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
//                    .background(headerColor)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                headerColor,
                                headerColor.copy(alpha = 0f) // 👈 creates the soft gradient feel
                            )
                        )
                    )
                    .height(48.dp)
                    .padding(start = 12.dp, end = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // floated circle icon on left overlapping
                Box(
                    modifier = Modifier
//                        .offset(x = (-16).dp)
                        .padding(end = 8.dp)
                ) {
                    iconContent()
                }
                Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AccentText)
            }

            Box(modifier = Modifier.offset(y = (-16).dp)) {
                Text(
                    text = description,
                    fontWeight = FontWeight.Normal,
                    fontSize = 11.sp,
                    color = AccentText,
                    modifier = Modifier.padding(start = 53.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Content row of sub-cards horizontally scrollable (mimicking the layout)
            LazyRow(contentPadding = PaddingValues(horizontal = 12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(cards.size) { idx ->
                    val d = cards[idx]
                    SubCard(data = d, index = idx, onClick = { onMilestoneClick((d.hashCode() xor idx).absoluteValue() % 10000) })
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

/** Sub card data */
data class SubCardData(
    val label: String,
    val progress: Float,
    val percentText: String,
    val buttonLabel: String? = null,
    val status: StageStatus = StageStatus.UPCOMING,
    val showMilestones: Boolean = false
)

/** Sub-card UI that mimics inner white cards in the design */
@Composable
fun SubCard(data: SubCardData, index: Int, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(260.dp)
            .wrapContentWidth()
            .wrapContentHeight(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            Text(data.label, fontWeight = FontWeight.SemiBold, color = AccentText)
            Spacer(modifier = Modifier.height(8.dp))
            // progress UI
            Row(verticalAlignment = Alignment.CenterVertically) {
                // circular percent
                CircularProgressWithPercent(progress = data.progress, text = data.percentText)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    // thin progress line
//                    LinearProgressIndicator(
//                        progress = { data.progress },
//                        modifier = Modifier
//                            .width(140.dp)
//                            .height(6.dp)
//                            .clip(RoundedCornerShape(6.dp)),
//                        color = Color(0xFF6AB0FF),
//                        trackColor = ProgressIndicatorDefaults.linearTrackColor,
//                        strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
//                    )
                    when (data.status) {
                        StageStatus.COMPLETED -> {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("${data.percentText}% Complete", color = Muted, style = MaterialTheme.typography.bodySmall)
                            }
                        }

                        StageStatus.CURRENT_FOCUS -> {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Menu, contentDescription = null, tint = Color(0xFF666766))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("${data.percentText}% Complete", color = Muted, style = MaterialTheme.typography.bodySmall)
                            }
                        }

                        else -> {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Menu, contentDescription = null, tint = Color(0xFF666766))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("${data.percentText}% Complete", color = Muted, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
//                    HorizontalDivider(
//                        modifier = Modifier.padding(vertical = 8.dp),
//                        thickness = 1.dp,
//                        color = Color.LightGray
//                    )

                    val buttonColor = when (data.status) {
                        StageStatus.COMPLETED -> ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                        StageStatus.CURRENT_FOCUS -> ButtonDefaults.buttonColors(containerColor = AccentText)
                        else -> ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                    }

                    when (data.status) {
                        StageStatus.COMPLETED -> {
                            Box(contentAlignment = Alignment.CenterStart, modifier = Modifier.fillMaxWidth()) {
                                Button(
                                    onClick = onClick,
                                    colors = buttonColor,
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
                                    modifier = Modifier
                                        .wrapContentSize()
                                        .height(30.dp)
                                ) {
                                    Text("Review Milestones", color = Color.Gray, fontSize = 13.sp)
                                }
                            }
                        }

                        StageStatus.CURRENT_FOCUS -> {
                            Box(contentAlignment = Alignment.CenterStart, modifier = Modifier.fillMaxWidth()) {
                                Button(
                                    onClick = onClick,
                                    colors = buttonColor,
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier
                                        .wrapContentSize()
                                        .height(28.dp)
                                ) {
                                    Text("View Details", color = Color.White, fontSize = 12.sp)
                                }
                            }
                        }

                        else -> {
                            Box(contentAlignment = Alignment.CenterStart, modifier = Modifier.fillMaxWidth()) {
                                Button(
                                    onClick = onClick,
                                    colors = buttonColor,
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
                                    modifier = Modifier
                                        .wrapContentSize()
                                        .height(30.dp)
                                ) {
                                    Text("Explore Checklist", color = Color.Gray, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            }

//            Spacer(modifier = Modifier.height(8.dp))
//            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
//                if (data.buttonLabel != null) {
//                    Button(onClick = onClick, modifier = Modifier.height(32.dp)) {
//                        Text(data.buttonLabel)
//                    }
//                } else {
//                    Text("", modifier = Modifier.width(8.dp))
//                }
//                Spacer(modifier = Modifier.weight(1f))
//                TextButton(onClick = onClick) {
//                    Text(if (data.showMilestones) "View Milestones" else "Details", color = Muted)
//                }
//            }
        }
    }
}

/** Circular percent + number */
@Composable
fun CircularProgressWithPercent(progress: Float, text: String) {
    val d = LocalDensity.current
    val animated = animateFloatAsState(targetValue = progress)
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(64.dp)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = with(d) { 5.dp.toPx() }
            val radius = size.minDimension / 2 - stroke
            drawCircle(color = Color(0xFFF2F2F3), radius = radius, center = center, style = Stroke(width = stroke))
            drawArc(
                color = AccentText,
                startAngle = -90f,
                sweepAngle = 360f * animated.value,
                useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2)
            )
        }
        Text(text, fontWeight = FontWeight.SemiBold, color = AccentText, fontSize = 13.sp)
    }
}

/** Milestone detail placeholder screen */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MilestoneDetailScreen(milestoneId: Int, onBack: () -> Unit) {
    Surface(modifier = Modifier.fillMaxSize(), color = BG) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            TopAppBar(title = { Text("Milestone", color = Color.White) }, navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "back", tint = Color.White)
                }
            }, colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent))
            Spacer(modifier = Modifier.height(18.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Milestone ID: $milestoneId", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("This is a placeholder detail screen. In production, you will show milestone info, chat, Lottie animations and edit options.")
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = { /* navigate to chat or other actions */ }) {
                        Text("Open AI Chat")
                    }
                }
            }
        }
    }
}

/** Chat placeholder screen */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatPlaceholderScreen(onBack: () -> Unit) {
    Surface(modifier = Modifier.fillMaxSize(), color = BG) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            TopAppBar(title = { Text("AI Mentor (Chat)", color = Color.White) }, navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "back", tint = Color.White)
                }
            }, colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent))
            Spacer(modifier = Modifier.height(24.dp))
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Chat placeholder", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("This opens your Chat screen. You can replace this screen with the Room-backed chat you previously had.")
                }
            }
        }
    }
}

/** Small gem/diamond icon drawn with Canvas — matches top-right gem in the image */
@Composable
fun GemIcon(size: Dp = 20.dp, tint: Color = Color(0xFFDEEAFB)) {
    Canvas(modifier = Modifier.size(size)) {
        val w = size.toPx()
        val h = size.toPx()
        val center = Offset(w / 2f, h / 2f)
        val points = listOf(
            Offset(center.x, center.y - h * 0.35f),
            Offset(center.x + w * 0.25f, center.y),
            Offset(center.x, center.y + h * 0.35f),
            Offset(center.x - w * 0.25f, center.y)
        )
        drawPath(Path().apply {
            moveTo(points[0].x, points[0].y)
            lineTo(points[1].x, points[1].y)
            lineTo(points[2].x, points[2].y)
            lineTo(points[3].x, points[3].y)
            close()
        }, color = tint)
        // inner shine
        drawPath(Path().apply {
            moveTo(center.x, center.y - h * 0.18f)
            lineTo(center.x + w * 0.12f, center.y - h * 0.02f)
            lineTo(center.x, center.y + h * 0.08f)
            lineTo(center.x - w * 0.11f, center.y - h * 0.02f)
            close()
        }, color = tint.copy(alpha = 0.6f))
    }
}

// ---------------------------
// Helpers
// ---------------------------
private fun Int.absoluteValue(): Int = if (this < 0) -this else this

// ---------------------------
// Preview helper
// ---------------------------
@Preview(showBackground = true)
@Composable
fun DashboardPreview() {
    CareerCompassApp()
}


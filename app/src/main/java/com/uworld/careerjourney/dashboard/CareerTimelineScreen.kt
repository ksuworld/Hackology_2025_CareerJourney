package com.uworld.careerjourney.dashboard

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CareerTimelineScreen(onJourneySelected: (String) -> Unit? = {}) {
    val data = sampleTimelineData // Using sample data for preview purposes

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("CAREER COMPASS", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { GlobalProgressBar(overallProgress = 45) }

            items(data) { section ->
                JourneySectionHeader(section = section)

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    section.stages.forEach { stage ->
                        CareerStageCard(stage = stage)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun GlobalProgressBar(overallProgress: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Journey Progress: $overallProgress% Complete", style = MaterialTheme.typography.titleSmall)
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { overallProgress / 100f },
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(8.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = ProgressIndicatorDefaults.linearTrackColor,
            strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
        )
    }
}

@Composable
fun JourneySectionHeader(section: JourneySection) {
    // **FIXED: Directly using section.color**
    Text(
        text = section.name,
        color = Color.White,
        fontWeight = FontWeight.Black,
        fontSize = 18.sp,
        modifier = Modifier
            .fillMaxWidth()
            .background(section.color.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
            .padding(vertical = 8.dp, horizontal = 12.dp)
    )
}

@Composable
fun CareerStageCard(stage: CareerStage) {
    val cardColor = when (stage.status) {
        StageStatus.COMPLETED -> Color(0xFFE0E0E0)
        StageStatus.CURRENT_FOCUS -> stage.color.copy(alpha = 0.1f)
        StageStatus.UPCOMING -> Color(0xFFF5F5F5)
    }
    val borderColor = if (stage.status == StageStatus.CURRENT_FOCUS) stage.color else Color.LightGray

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, borderColor, RoundedCornerShape(12.dp))
            .background(cardColor, RoundedCornerShape(12.dp))
            .clickable { /* Handle card click to navigate to checklist */ }
            .animateContentSize(animationSpec = tween(300)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Title Row
            Column(
                modifier = Modifier.fillMaxWidth(),
//                verticalAlignment = Alignment.CenterVertically,
//                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stage.journeyType.journeyName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = if (stage.status == StageStatus.COMPLETED) Color.DarkGray else Color.Black
                )

                // Status Indicator
                when (stage.status) {
                    StageStatus.COMPLETED -> Icon(Icons.Filled.CheckCircle, contentDescription = "Completed", tint = stage.color)
                    StageStatus.CURRENT_FOCUS -> Text("Current Focus", color = stage.color, fontWeight = FontWeight.SemiBold)
                    StageStatus.UPCOMING -> Text("Upcoming", color = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Display Stage ID and Target Year
            Text(
                text = "ID: ${stage.journeyType.id} | Target Year: ${stage.milestones.firstOrNull()?.targetYear ?: "N/A"}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )

            // Progress Bar & Action Button logic
            if (stage.status != StageStatus.COMPLETED) {
                LinearProgressIndicator(
                    progress = { stage.progressPercent / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp),
                    color = stage.color,
                    trackColor = ProgressIndicatorDefaults.linearTrackColor,
                    strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Progress: ${stage.progressPercent}% Complete", style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(12.dp))
            } else {
                Text("All milestones achieved in this stage.", style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(12.dp))
            }

            Button(
                onClick = { /* Navigation logic */ },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = stage.color)
            ) {
                val buttonText = when (stage.status) {
                    StageStatus.COMPLETED -> "Review Milestones"
                    StageStatus.CURRENT_FOCUS -> "Continue Checklist"
                    StageStatus.UPCOMING -> "Explore Milestones"
                }
                Text(buttonText)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
            }
        }
    }
}

// --- 4. PREVIEW ---

@Preview(showBackground = true, name = "Career Timeline Screen")
@Composable
fun CareerTimelinePreview() {
    MaterialTheme {
        CareerTimelineScreen()
    }
}
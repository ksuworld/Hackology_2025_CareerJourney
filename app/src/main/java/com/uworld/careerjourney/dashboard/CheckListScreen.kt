package com.example.careerjourney.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.uworld.careerjourney.dashboard.CareerMilestone
import com.uworld.careerjourney.dashboard.StageStatus

@Composable
fun ChecklistScreen(
    navController: NavController? = null
) {
    val milestones = remember {
        listOf(
            CareerMilestone(
                milestoneId = "MS-001",
                journeyTypeId = "JNY-004",
                title = "Secure Summer Internship",
                description = "Gain experience in your target field.",
                targetYear = 3,
                status = StageStatus.COMPLETED
            ),
            CareerMilestone(
                milestoneId = "MS-002",
                journeyTypeId = "JNY-004",
                title = "Complete High-Impact Internship",
                description = "Finish ongoing internship with measurable results.",
                targetYear = 3,
                status = StageStatus.CURRENT_FOCUS
            ),
            CareerMilestone(
                milestoneId = "MS-003",
                journeyTypeId = "JNY-004",
                title = "Master Interview Skills",
                description = "Prepare for final year placements.",
                targetYear = 3,
                status = StageStatus.UPCOMING
            ),
            CareerMilestone(
                milestoneId = "MS-004",
                journeyTypeId = "JNY-004",
                title = "Draft Post-Graduation Plan",
                description = "Define your next career step after college.",
                targetYear = 3,
                status = StageStatus.UPCOMING
            )
        )
    }

    val completedCount = milestones.count { it.status == StageStatus.COMPLETED }
    val progress = completedCount / milestones.size.toFloat()

    // Gradient background + top rounded card section
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF1B2A52), Color(0xFF3B5998))
                )
            )
    ) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 32.dp)
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .animateContentSize()
            ) {
                Spacer(Modifier.height(20.dp))
                ChecklistHeader(progress)

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 16.dp)
                ) {
                    items(milestones) { item ->
                        MilestoneCard(item)
                    }

                    item {
                        Spacer(Modifier.height(20.dp))
                        AddCustomMilestoneButton()
                        Spacer(Modifier.height(40.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ChecklistHeader(progress: Float) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { /* navController?.popBackStack() */ }) {
                Icon(
                    painter = painterResource(android.R.drawable.ic_media_previous),
                    contentDescription = "Back",
                    tint = Color.Black
                )
            }

            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = progress,
                    color = Color(0xFF4CAF50),
                    strokeWidth = 6.dp,
                    modifier = Modifier.size(42.dp)
                )
                Text(
                    "${(progress * 100).toInt()}%",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(Modifier.height(8.dp))
        Text(
            text = "Junior Year Checklist",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        )
        Text(
            text = "Immersion & Application (Year 3)",
            style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray)
        )
    }
}

@Composable
private fun MilestoneCard(item: CareerMilestone) {
    val (bgColor, iconTint, statusText) = when (item.status) {
        StageStatus.COMPLETED -> Triple(Color(0xFFE8F5E9), Color(0xFF4CAF50), "Completed")
        StageStatus.CURRENT_FOCUS -> Triple(Color(0xFFE3F2FD), Color(0xFF2196F3), "In Progress")
        StageStatus.UPCOMING -> Triple(Color(0xFFF5F5F5), Color(0xFF9E9E9E), "Upcoming")
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .shadow(6.dp, shape = RoundedCornerShape(12.dp))
            .clickable { /* navController?.navigate("milestone/${item.milestoneId}") */ },
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .background(bgColor.copy(alpha = 0.25f))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = com.uworld.careerjourney.R.drawable.ic_star),
                    contentDescription = null,
                    tint = iconTint
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = Color.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = item.description,
                    fontSize = 13.sp,
                    color = Color.Gray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = statusText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = iconTint
                )
            }
        }
    }
}

@Composable
private fun AddCustomMilestoneButton() {
    Button(
        onClick = { /* open add milestone dialog */ },
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .padding(horizontal = 8.dp),
        shape = RoundedCornerShape(30.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B2A52))
    ) {
        Text("+ Add Custom Milestone", color = Color.White, fontSize = 16.sp)
    }
}

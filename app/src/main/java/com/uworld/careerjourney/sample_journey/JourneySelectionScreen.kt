package com.uworld.careerjourney.sample_journey

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DownhillSkiing
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.uworld.careerjourney.R

@Composable
fun JourneySelectionScreen(onJourneySelected: (String) -> Unit) {
    // immersive header card
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(18.dp)
    ) {
        Text("Plan your journey", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Pick a starting path — we'll scaffold milestone checkpoints for you.")
        Spacer(modifier = Modifier.height(20.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            JourneyTile(
                title = JourneyType.PRE_COLLEGE.label,
                subtitle = "Test prep, applications, scholarships",
                icon = Icons.Default.DownhillSkiing
            ) { onJourneySelected(JourneyType.PRE_COLLEGE.label) }
            JourneyTile(
                title = JourneyType.IN_COLLEGE.label,
                subtitle = "Internships, majors, campus life",
                icon = Icons.Default.School
            ) { onJourneySelected(JourneyType.IN_COLLEGE.label) }
            JourneyTile(
                title = JourneyType.AFTER_COLLEGE.label,
                subtitle = "Jobs, grad school, career path",
                icon = Icons.Default.Work
            ) { onJourneySelected(JourneyType.AFTER_COLLEGE.label) }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Card(
            shape = RoundedCornerShape(14.dp),
            elevation = CardDefaults.cardElevation(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.roadmap_bg),
                    contentDescription = "hero",
                    modifier = Modifier.size(72.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Lifelong Roadmap", style = MaterialTheme.typography.titleSmall)
                    Text("Create multiple journeys and revisit them anytime.")
                }
                Spacer(modifier = Modifier.weight(1f))
                Button(onClick = { onJourneySelected(JourneyType.PRE_COLLEGE.label) }, colors = ButtonDefaults.buttonColors()) {
                    Text("Create")
                }
            }
        }
    }
}

@Composable
fun JourneyTile(title: String, subtitle: String, icon: ImageVector, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(112.dp)
            .height(150.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), MaterialTheme.colorScheme.surface)))
                .padding(12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(34.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(title, style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(6.dp))
            Text(subtitle, style = MaterialTheme.typography.bodySmall)
        }
    }
}

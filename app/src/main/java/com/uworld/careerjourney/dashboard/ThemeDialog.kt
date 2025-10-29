package com.uworld.careerjourney.dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object GlobalThemeManager {
    val themesIds = listOf(0, 1)
    val themeTitles = listOf("Blue", "Orange")
    val themeResIds = listOf(com.uworld.careerjourney.R.drawable.roadmap_vector_blue, com.uworld.careerjourney.R.drawable.roadmap_orange)
    var selectedThemeId: Int = 0
}

@Composable
fun ThemeSelectionDialog(
    onThemeSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedThemeId by remember { mutableIntStateOf(1) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Choose Your Journey Theme",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = AccentText
            )
        },
        text = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
//                    .horizontalScroll(rememberScrollState())
            ) {
                GlobalThemeManager.themesIds.forEach { themeId ->
                    val isSelected = themeId == selectedThemeId
                    val borderColor = if (isSelected) Color(0xFF4F8DFB) else Color.LightGray

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .height(200.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .border(
                                width = 3.dp,
                                color = borderColor,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable {
                                selectedThemeId = themeId
                            }
                    ) {
                        Image(
                            painter = painterResource(id = GlobalThemeManager.themeResIds[themeId]),
                            contentDescription = "${GlobalThemeManager.themeTitles[themeId]} Theme",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .background(
                                    brush = Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f))),
                                    shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
                                )
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Text(
                                text = GlobalThemeManager.themeTitles[themeId],
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onThemeSelected(selectedThemeId) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedThemeId == 0) Color(0xFF4F8DFB) else Color(0xFFFFA726)
                )
            ) {
                Text("Apply Theme")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

package com.uworld.careerjourney

import android.os.Build
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CheckpointPopUpScreen() {
    Box(
        modifier = Modifier.fillMaxSize().background(Color(0xFF303030)),
        contentAlignment = Alignment.Center,
    ) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Card(
                modifier = Modifier.size(width = 350.dp, height = 400.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF000428),
                                    Color(0xFF003E73)
                                )
                            )
                        )
                ) {


                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopCenter)
                    ) {

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.flag),
                                    modifier = Modifier.size(20.dp),
                                    contentDescription = "A description of the image for accessibility",
                                    colorFilter = ColorFilter.tint(Color(0xFF006AC2))
                                )

                                Text(
                                    text = "First Midterm",
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    modifier = Modifier.padding(start = 8.dp),
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = "Close",
                                modifier = Modifier
                                    .size(18.dp)
                                    .clickable(onClick = { }),
                                tint = Color.White.copy(0.7f)
                            )
                        }

                        HorizontalDivider(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                            color = Color.White.copy(0.15f),
                            thickness = .35.dp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "The first midterm covers chapters 1-5. Make sure to review key concepts and practice problems to prepare effectively. The first midterm covers chapters 1-5. Make sure to review key concepts and practice problems to prepare effectively. Make sure to review key concepts and practice problems to prepare effectively.",
                            color = Color.White.copy(0.8f),
                            maxLines = 4,
                            overflow = TextOverflow.Ellipsis,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }

                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .height(210.dp)
                            .padding(16.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White.copy(alpha = 0.2f),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.ai_icon),
                                    modifier = Modifier.size(20.dp),
                                    contentDescription = "A description of the image for accessibility",
                                    colorFilter = ColorFilter.tint(Color.White)
                                )
                                Text(
                                    text = "AI Coach",
                                    fontSize = 14.sp,
                                    color = Color.White,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(8.dp)
                            ) {
                                SuggestionItem(text = "Review 'SAT' and concentrate more on Mathematics and Reading ")
                                SuggestionItem(text = "Practice 'ACT'")
                                SuggestionItem(text = "Take a quiz on 'AP'")
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "Close",
                modifier = Modifier
                    .size(24.dp)
                    .clickable(onClick = ::closePopUp),
                tint = Color.White.copy(0.4f),
            )
        }
    }
}

@Composable
private fun SuggestionItem(text: String) {
    Row(verticalAlignment = Alignment.Top) {
        Text(
            text = "❯ ",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White.copy(alpha = 0.9f)
        )
        Text(
            text = text,
            fontSize = 14.sp,
            modifier = Modifier.padding(start = 4.dp),
            color = Color.White.copy(alpha = 0.9f)
        )
    }
}

private fun closePopUp() {

}

@Preview(showBackground = true)
@Composable
fun CheckpointPopUpScreenPreview() {
    CheckpointPopUpScreen()
}

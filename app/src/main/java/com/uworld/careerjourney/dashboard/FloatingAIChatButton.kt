package com.uworld.careerjourney.dashboard

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@Composable
fun FloatingAIChatButton(
    onClick: () -> Unit
) {
    // For continuous glow animation
    val infiniteTransition = rememberInfiniteTransition(label = "")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = ""
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.BottomEnd
    ) {
        Box(
            modifier = Modifier
                .size(68.dp)
                .graphicsLayer {
                    shadowElevation = 16.dp.toPx()
                    shape = CircleShape
                    clip = true
                }
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF4F8DFB).copy(alpha = glowAlpha),
                            Color.Transparent
                        ),
                        radius = 100f
                    ),
                    shape = CircleShape
                )
                .clickable(
                    indication = ripple(bounded = false),
                    interactionSource = remember { MutableInteractionSource() }
                ) { onClick() },
            contentAlignment = Alignment.Center
        ) {
            // Chatbot Icon (Gemini-like star)
//            Icon(
//                painter = painterResource(id = com.uworld.careerjourney.R.drawable.ic_gemini_star), // your star icon or chat icon
//                contentDescription = "AI Chat",
//                tint = Color.White,
//                modifier = Modifier
//                    .size(32.dp)
//                    .background(Color(0xFF4F8DFB), shape = CircleShape)
//                    .padding(8.dp)
//            )
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = "AI Chat",
                tint = Color.White,
                modifier = Modifier
                    .size(32.dp)
                    .background(Color(0xFF4F8DFB), shape = CircleShape)
                    .padding(8.dp)
            )
        }
    }
}
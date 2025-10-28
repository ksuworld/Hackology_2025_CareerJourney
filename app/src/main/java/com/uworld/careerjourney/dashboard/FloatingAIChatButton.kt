package com.uworld.careerjourney.dashboard

import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

@Composable
fun FloatingAIChatButton1(
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
            .padding(8.dp),
        contentAlignment = Alignment.BottomEnd
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
//                .graphicsLayer {
//                    shadowElevation = 8.dp.toPx()
//                    shape = CircleShape
//                    clip = true
//                }
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
//                            Color(0xFF4F8DFB).copy(alpha = glowAlpha),
                            AccentText.copy(alpha = glowAlpha),
                            Color.Transparent
                        ),
                        radius = 5f
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
                    .size(36.dp)
//                    .background(Color(0xFF4F8DFB), shape = CircleShape)
//                    .background(AccentText, shape = CircleShape)
                    .background(AccentText
//                        brush = Brush.radialGradient(
//                            colors = listOf(
////                            Color(0xFF4F8DFB).copy(alpha = glowAlpha),
//                                AccentText.copy(alpha = 0.8f),
//                                AccentText.copy(alpha = 0.4f),
//                                AccentText.copy(alpha = 0.1f)
//                            ),
//                            center = androidx.compose.ui.geometry.Offset.Infinite,
//                            radius = 25f
//                        )
                        ,
                        shape = CircleShape
                    )
                    .padding(8.dp)
            )
        }
    }
}

@Composable
fun GlowingAiChatButton(
    onClick: () -> Unit
) {
    // Infinite animation for glow pulse
    val infiniteTransition = rememberInfiniteTransition(label = "")
    val glowRadius by infiniteTransition.animateFloat(
        initialValue = 80f,
        targetValue = 130f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = ""
    )

    Box(
        modifier = Modifier
            .size(100.dp)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        // Glowing radial background
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF00C6FF).copy(alpha = 0.6f),
                            Color(0xFF00C6FF).copy(alpha = 0.2f),
                            Color.Transparent
                        ),
                        radius = glowRadius
                    ),
                    shape = CircleShape
                )
        )

        // Main Button (futuristic look)
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(
                    brush = Brush.linearGradient(
                        listOf(
                            Color(0xFF00C6FF),
                            Color(0xFF0072FF)
                        )
                    ),
                    shape = CircleShape
                )
                .shadow(8.dp, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = "AI Chat",
                tint = Color.White,
                modifier = Modifier.size(30.dp)
            )
        }
    }
}

@Composable
fun FloatingAIChatButton2(
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = ""
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.BottomEnd
    ) {
        Box(
            modifier = Modifier
                .size(70.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF6C63FF).copy(alpha = glowAlpha * 0.4f),
                            Color.Transparent
                        )
                    )
                )
        )

        FloatingActionButton(
            onClick = onClick,
            modifier = Modifier.size(64.dp),
            containerColor = Color(0xFF6C63FF),
            elevation = FloatingActionButtonDefaults.elevation(8.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Chat,
                contentDescription = "Chat",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
fun FloatingAIChatButton3(onClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = ""
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(28.dp),
        contentAlignment = Alignment.BottomEnd
    ) {
        Box(
            modifier = Modifier
                .size(78.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF00E5FF).copy(alpha = glowAlpha * 0.6f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF00C6FF), Color(0xFF0072FF))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Chat,
                    contentDescription = "Chat",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}
package com.uworld.careerjourney.onboarding

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = androidx.compose.ui.graphics.Color(0xFF1E88E5),
    primaryContainer = androidx.compose.ui.graphics.Color(0xFF1565C0),
    secondary = androidx.compose.ui.graphics.Color(0xFF42A5F5)
)

@Composable
fun UWorldRoadmapTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = LightColors, typography = Typography(), shapes = Shapes(), content = content)
}

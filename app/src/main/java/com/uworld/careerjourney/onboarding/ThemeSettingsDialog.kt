package com.uworld.careerjourney.onboarding

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ThemeSettingsDialog(themeState: ThemeState, onDismiss: () -> Unit, onSave: (ThemeState) -> Unit) {
    val selected = remember { mutableIntStateOf(0) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Theme & Image") }, text = {
        Column {
            // Offer a few palette presets
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                RadioButton(selected = selected.intValue == 0, onClick = { selected.intValue = 0 })
                Spacer(modifier = Modifier.width(8.dp))
                Text("Blue Gradient")
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                RadioButton(selected = selected.intValue == 1, onClick = { selected.intValue = 1 })
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sunset")
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                RadioButton(selected = selected.intValue == 2, onClick = { selected.intValue = 2 })
                Spacer(modifier = Modifier.width(8.dp))
                Text("Monochrome")
            }
        }
    }, confirmButton = {
        Button(onClick = {
            val out = when (selected.intValue) {
                1 -> ThemeState.sunset()
                2 -> ThemeState(0xFF333333, 0xFF666666)
                else -> ThemeState.default()
            }
            onSave(out)
        }) { Text("Apply") }
    }, dismissButton = {
        Button(onClick = onDismiss) { Text("Cancel") }
    })
}

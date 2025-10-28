package com.uworld.careerjourney.onboarding

import android.app.DatePickerDialog
import android.content.Context
import android.widget.DatePicker
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun MilestoneDetailsPopup(
    milestone: Milestone,
    onDismiss: () -> Unit,
    onUpdate: (Milestone) -> Unit
) {
    var showEdit by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(milestone.name) },
        text = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Lottie animation small preview
                    LottieSmall()
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Date: ${SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(milestone.date)}")
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(milestone.description)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                // Simulated AI chat bubble with tips
                Card(
                    shape = RoundedCornerShape(10.dp),
                    elevation = CardDefaults.cardElevation(4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("AI Coach", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("• Break the milestone into weekly tasks.\n• Track study hours & practice tests.\n• Use office hours and mentors for feedback.")
                    }
                }
            }
        },
        confirmButton = {
            Row {
                Button(onClick = { showEdit = true }) {
                    Icon(Icons.Default.Edit, contentDescription = "edit")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Edit")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = onDismiss) { Text("Close") }
            }
        }
    )

    if (showEdit) {
        EditMilestoneDialog(milestone = milestone, onDismiss = { showEdit = false }, onSave = {
            onUpdate(it)
            showEdit = false
        })
    }
}

@Composable
fun LottieSmall() {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(com.uworld.careerjourney.R.raw.lottie_gradient))
    val progress by animateLottieCompositionAsState(composition, iterations = LottieConstants.IterateForever)
    LottieAnimation(composition, progress, modifier = Modifier.size(72.dp))
}

@Composable
fun EditMilestoneDialog(milestone: Milestone, onDismiss: () -> Unit, onSave: (Milestone) -> Unit) {
    var name by remember { mutableStateOf(milestone.name) }
    var dateString by remember { mutableStateOf(SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(milestone.date)) }
    var description by remember { mutableStateOf(milestone.description) }
    val ctx = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit milestone") },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") })
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = dateString,
                        onValueChange = { /* no-op */ },
                        label = { Text("Date") },
                        enabled = false,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        showDatePicker(ctx, initial = milestone.date) { newDate ->
                            milestone.date = newDate
                            dateString = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(newDate)
                        }
                    }) {
                        Text("Change")
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val updated = milestone.copy(name = name, description = description, date = milestone.date)
                onSave(updated)
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

fun showDatePicker(context: Context, initial: Date = Date(), onDateSelected: (Date) -> Unit) {
    val cal = Calendar.getInstance().also { it.time = initial }
    val y = cal.get(Calendar.YEAR)
    val m = cal.get(Calendar.MONTH)
    val d = cal.get(Calendar.DAY_OF_MONTH)
    val dp = DatePickerDialog(context, { _: DatePicker, yy: Int, mm: Int, dd: Int ->
        val c = Calendar.getInstance()
        c.set(yy, mm, dd)
        onDateSelected(c.time)
    }, y, m, d)
    dp.show()
}

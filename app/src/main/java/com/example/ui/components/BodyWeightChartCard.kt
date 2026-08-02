package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.WeightLog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BodyWeightChartCard(
    currentWeightKg: Float,
    todayCalories: Int,
    monthlyCalories: Int,
    weightLogs: List<WeightLog>,
    onLogWeight: (weightKg: Float, notes: String) -> Unit
) {
    var showLogDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFF161616),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.MonitorWeight,
                        contentDescription = "Weight Log",
                        tint = Color(0xFFFF6B00),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "BODY WEIGHT & COMPOSITION",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }

                Button(
                    onClick = { showLogDialog = true },
                    shape = RoundedCornerShape(100.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6B00)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Log", modifier = Modifier.size(16.dp), tint = Color.Black)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("LOG WEIGHT", fontWeight = FontWeight.Black, color = Color.Black, fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Calories & Weight Stats Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF1E293B),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("CURRENT WEIGHT", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("${String.format("%.1f", currentWeightKg)} kg", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = Color.White)
                        Text("${String.format("%.1f", currentWeightKg * 2.20462f)} lbs", style = MaterialTheme.typography.labelSmall, color = Color(0xFFFF6B00))
                    }
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF1E293B),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Whatshot, contentDescription = null, tint = Color(0xFFFF6B00), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("ACTIVE MET CALORIES", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("$todayCalories kcal", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = Color(0xFF22C55E))
                        Text("$monthlyCalories kcal month", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Weight Line Chart
            Text("WEIGHT TREND OVER TIME", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))

            val displayLogs = remember(weightLogs, currentWeightKg) {
                if (weightLogs.isEmpty()) {
                    listOf(
                        WeightLog(dateString = "Day 1", weightKg = currentWeightKg + 1.2f),
                        WeightLog(dateString = "Day 2", weightKg = currentWeightKg + 0.8f),
                        WeightLog(dateString = "Day 3", weightKg = currentWeightKg + 0.3f),
                        WeightLog(dateString = "Today", weightKg = currentWeightKg)
                    )
                } else {
                    weightLogs.takeLast(10)
                }
            }

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(Color(0xFF0F172A), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                if (displayLogs.size < 2) return@Canvas

                val minWeight = (displayLogs.minOf { it.weightKg } - 1.0f).coerceAtLeast(40f)
                val maxWeight = (displayLogs.maxOf { it.weightKg } + 1.0f).coerceAtLeast(minWeight + 2f)

                val width = size.width
                val height = size.height

                val points = displayLogs.mapIndexed { index, log ->
                    val x = (index.toFloat() / (displayLogs.size - 1)) * width
                    val normalizedY = (log.weightKg - minWeight) / (maxWeight - minWeight)
                    val y = height - (normalizedY * height)
                    Offset(x, y)
                }

                // Draw connecting line path
                val path = Path().apply {
                    moveTo(points[0].x, points[0].y)
                    for (i in 1 until points.size) {
                        lineTo(points[i].x, points[i].y)
                    }
                }

                drawPath(
                    path = path,
                    color = Color(0xFFFF6B00),
                    style = Stroke(width = 4f)
                )

                // Draw points & labels
                points.forEachIndexed { i, pt ->
                    drawCircle(
                        color = Color(0xFFFF6B00),
                        radius = 6f,
                        center = pt
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 3f,
                        center = pt
                    )
                }
            }
        }
    }

    if (showLogDialog) {
        var inputWeight by remember { mutableStateOf(currentWeightKg.toString()) }
        var inputNotes by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showLogDialog = false },
            confirmButton = {
                Button(
                    onClick = {
                        val weight = inputWeight.toFloatOrNull()
                        if (weight != null && weight > 20f) {
                            onLogWeight(weight, inputNotes)
                            HapticFeedbackHelper.triggerVibration(context)
                            showLogDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6B00))
                ) {
                    Text("SAVE LOG", fontWeight = FontWeight.Black, color = Color.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogDialog = false }) {
                    Text("CANCEL", color = Color.Gray)
                }
            },
            title = {
                Text("LOG MORNING BODY WEIGHT", fontWeight = FontWeight.Black, color = Color(0xFFFF6B00))
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = inputWeight,
                        onValueChange = { inputWeight = it },
                        label = { Text("Weight (kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = inputNotes,
                        onValueChange = { inputNotes = it },
                        label = { Text("Notes (e.g. Fasted, Post-workout)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        )
    }
}

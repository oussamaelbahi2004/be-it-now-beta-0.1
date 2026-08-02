package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppViewModel
import com.example.ui.components.HapticFeedbackHelper
import com.example.ui.components.HeatmapCalendarCard
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthlyPlannerScreen(
    viewModel: AppViewModel
) {
    val context = LocalContext.current
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val monthTasks by viewModel.selectedMonthTasks.collectAsState()

    val currentCal = remember(selectedMonth) {
        val cal = Calendar.getInstance()
        try {
            val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
            val d = sdf.parse(selectedMonth)
            if (d != null) cal.time = d
        } catch (_: Exception) {}
        cal
    }

    val monthYearTitle = remember(currentCal) {
        SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(currentCal.time).uppercase()
    }

    val daysInMonth = remember(currentCal) {
        val clone = currentCal.clone() as Calendar
        clone.set(Calendar.DAY_OF_MONTH, 1)
        val maxDays = clone.getActualMaximum(Calendar.DAY_OF_MONTH)
        val firstDayOfWeek = clone.get(Calendar.DAY_OF_WEEK) - 1 // 0-indexed Sunday

        val list = mutableListOf<String?>()
        for (i in 0 until firstDayOfWeek) list.add(null)

        val monthPrefix = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(clone.time)
        for (day in 1..maxDays) {
            val dayStr = String.format(Locale.US, "%s-%02d", monthPrefix, day)
            list.add(dayStr)
        }
        list
    }

    // Map date string to count of tasks scheduled
    val tasksCountPerDate = remember(monthTasks) {
        monthTasks.groupBy { it.dateString }.mapValues { it.value.size }
    }

    var showQuickAssignSheet by remember { mutableStateOf(false) }
    val templates by viewModel.templates.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "MONTHLY PLANNER",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFFF6B00),
                            letterSpacing = 1.sp
                        )

                        Button(
                            onClick = {
                                viewModel.duplicateWeekAcrossMonth()
                                HapticFeedbackHelper.triggerDoublePulse(context)
                            },
                            shape = RoundedCornerShape(100.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6B00))
                        ) {
                            Text(
                                text = "COPY 7-DAY WEEK 📋",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = Color.Black
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Month Switcher Bar
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFF161616),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        currentCal.add(Calendar.MONTH, -1)
                        val newMonthKey = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(currentCal.time)
                        viewModel.setSelectedMonth(newMonthKey)
                    }) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Prev Month", tint = Color.White)
                    }

                    Text(
                        text = monthYearTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFFF6B00),
                        letterSpacing = 1.sp
                    )

                    IconButton(onClick = {
                        currentCal.add(Calendar.MONTH, 1)
                        val newMonthKey = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(currentCal.time)
                        viewModel.setSelectedMonth(newMonthKey)
                    }) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Next Month", tint = Color.White)
                    }
                }
            }

            // GitHub-Style Activity Heatmap Visualization
            HeatmapCalendarCard(
                monthKey = selectedMonth,
                monthTasks = monthTasks,
                selectedDate = selectedDate,
                onDateSelect = { viewModel.setSelectedDate(it) }
            )

            // Calendar Days Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                listOf("SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT").forEach { day ->
                    Text(
                        text = day,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.width(40.dp)
                    )
                }
            }

            // Calendar Month Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(daysInMonth) { dateStr ->
                    if (dateStr == null) {
                        Spacer(modifier = Modifier.size(46.dp))
                    } else {
                        val dayNum = dateStr.split("-").last().toIntOrNull() ?: 1
                        val isSelected = dateStr == selectedDate
                        val scheduledCount = tasksCountPerDate[dateStr] ?: 0

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) Color(0xFFFF6B00) else Color(0xFF161616),
                            border = BorderStroke(1.dp, if (isSelected) Color(0xFFFF6B00) else Color.White.copy(alpha = 0.08f)),
                            modifier = Modifier
                                .height(52.dp)
                                .clickable {
                                    viewModel.setSelectedDate(dateStr)
                                    HapticFeedbackHelper.triggerVibration(context, 30)
                                }
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "$dayNum",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.Black else Color.White
                                    )
                                    if (scheduledCount > 0) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .background(
                                                    if (isSelected) Color.Black else Color(0xFFFF6B00),
                                                    CircleShape
                                                )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Selected Day Summary Box
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFF161616),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SELECTED: $selectedDate",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFFF6B00)
                        )

                        TextButton(
                            onClick = { showQuickAssignSheet = true }
                        ) {
                            Text("+ QUICK ASSIGN TEMPLATE", fontWeight = FontWeight.Bold, color = Color(0xFFFF6B00), fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val selectedTasks = monthTasks.filter { it.dateString == selectedDate }
                    if (selectedTasks.isEmpty()) {
                        Text(
                            text = "No missions assigned to this date.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.5f)
                        )
                    } else {
                        selectedTasks.forEach { task ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "• ${task.title}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = task.timeString,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFFFF6B00),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showQuickAssignSheet) {
        AlertDialog(
            onDismissRequest = { showQuickAssignSheet = false },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showQuickAssignSheet = false }) {
                    Text("CLOSE", color = Color.Gray)
                }
            },
            title = {
                Text("ASSIGN TEMPLATE TO $selectedDate", fontWeight = FontWeight.Black, color = Color(0xFFFF6B00), fontSize = 16.sp)
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 350.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (templates.isEmpty()) {
                        Text("No templates found in library. Create a template first!", color = Color.Gray)
                    } else {
                        templates.forEach { template ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF1E293B),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.scheduleTask(template, selectedDate, "08:00")
                                        showQuickAssignSheet = false
                                        HapticFeedbackHelper.triggerVibration(context)
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(template.title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                                        Text("${template.category} • ${template.estimatedMinutes}m", color = Color.Gray, fontSize = 12.sp)
                                    }
                                    Text("+ ADD", fontWeight = FontWeight.Black, color = Color(0xFFFF6B00), fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        )
    }
}

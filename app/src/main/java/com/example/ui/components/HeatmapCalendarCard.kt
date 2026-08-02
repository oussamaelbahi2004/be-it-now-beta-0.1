package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ScheduledTask
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun HeatmapCalendarCard(
    monthKey: String, // "YYYY-MM"
    monthTasks: List<ScheduledTask>,
    selectedDate: String,
    onDateSelect: (String) -> Unit
) {
    val cal = remember(monthKey) {
        Calendar.getInstance().apply {
            time = try {
                SimpleDateFormat("yyyy-MM", Locale.getDefault()).parse(monthKey) ?: Date()
            } catch (e: Exception) {
                Date()
            }
            set(Calendar.DAY_OF_MONTH, 1)
        }
    }

    val daysInMonth = remember(cal) { cal.getActualMaximum(Calendar.DAY_OF_MONTH) }
    val daysList = remember(monthKey, daysInMonth) { (1..daysInMonth).toList() }

    val taskMapByDay = remember(monthTasks) {
        monthTasks.groupBy { task ->
            try {
                task.dateString.split("-").last().toInt()
            } catch (e: Exception) {
                0
            }
        }
    }

    val perfectDays = remember(taskMapByDay) {
        taskMapByDay.count { (_, tasks) -> tasks.isNotEmpty() && tasks.all { it.isCompleted } }
    }

    val partialDays = remember(taskMapByDay) {
        taskMapByDay.count { (_, tasks) -> tasks.any { it.isCompleted } && !tasks.all { it.isCompleted } }
    }

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFF121212),
        border = BorderStroke(1.dp, Color(0xFFFF6B00).copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.GridOn,
                        contentDescription = "Heatmap",
                        tint = Color(0xFFFF6B00),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "CONSISTENCY HEATMAP",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }

                Surface(
                    color = Color(0xFFFF6B00).copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, Color(0xFFFF6B00).copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(100.dp)
                ) {
                    Text(
                        text = "🔥 $perfectDays PERFECT DAYS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFFF6B00),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Grid of 7 columns (days of week)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                val chunks = daysList.chunked(7)
                chunks.forEach { week ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        week.forEach { dayNum ->
                            val dayTasks = taskMapByDay[dayNum] ?: emptyList()
                            val total = dayTasks.size
                            val completed = dayTasks.count { it.isCompleted }
                            val is100Percent = total > 0 && completed == total
                            val isPartial = total > 0 && completed > 0 && completed < total

                            val dateStr = String.format(Locale.getDefault(), "%s-%02d", monthKey, dayNum)
                            val isSelected = dateStr == selectedDate

                            val cellBg = when {
                                is100Percent -> Color(0xFFFF6B00)
                                isPartial -> Color(0xFFFF6B00).copy(alpha = 0.35f)
                                else -> Color(0xFF1E293B).copy(alpha = 0.5f)
                            }

                            val cellBorder = when {
                                isSelected -> BorderStroke(2.dp, Color.White)
                                is100Percent -> BorderStroke(1.dp, Color(0xFFFF8C00))
                                else -> BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
                            }

                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .background(cellBg, RoundedCornerShape(8.dp))
                                    .clickable { onDateSelect(dateStr) }
                                    .padding(2.dp)
                            ) {
                                Text(
                                    text = "$dayNum",
                                    fontWeight = if (is100Percent || isSelected) FontWeight.Black else FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = if (is100Percent) Color.Black else Color.White
                                )
                            }
                        }
                        // Fill empty trailing slots in final row
                        if (week.size < 7) {
                            repeat(7 - week.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Legend Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "LESS", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontSize = 9.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(12.dp).background(Color(0xFF1E293B), RoundedCornerShape(3.dp)))
                    Box(modifier = Modifier.size(12.dp).background(Color(0xFFFF6B00).copy(alpha = 0.35f), RoundedCornerShape(3.dp)))
                    Box(modifier = Modifier.size(12.dp).background(Color(0xFFFF6B00), RoundedCornerShape(3.dp)))
                }
                Text(text = "100% EXECUTED", style = MaterialTheme.typography.labelSmall, color = Color(0xFFFF6B00), fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

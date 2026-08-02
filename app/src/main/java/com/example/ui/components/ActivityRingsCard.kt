package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.SportsMma
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ScheduledTask
import com.example.data.TaskType

@Composable
fun ActivityRingsCard(
    tasks: List<ScheduledTask>
) {
    val gymTasks = remember(tasks) { tasks.filter { it.type == TaskType.GYM } }
    val gymCompleted = remember(gymTasks) { gymTasks.count { it.isCompleted } }
    val gymProgress = if (gymTasks.isEmpty()) 0f else (gymCompleted.toFloat() / gymTasks.size)

    val boxingTasks = remember(tasks) { tasks.filter { it.type == TaskType.BOXING } }
    val boxingCompleted = remember(boxingTasks) { boxingTasks.count { it.isCompleted } }
    val boxingProgress = if (boxingTasks.isEmpty()) 0f else (boxingCompleted.toFloat() / boxingTasks.size)

    val cardioTasks = remember(tasks) { tasks.filter { it.type == TaskType.MAIN_SIDE } }
    val cardioCompleted = remember(cardioTasks) { cardioTasks.count { it.isCompleted } }
    val cardioProgress = if (cardioTasks.isEmpty()) 0f else (cardioCompleted.toFloat() / cardioTasks.size)

    val animatedGym by animateFloatAsState(targetValue = gymProgress.coerceIn(0f, 1f), animationSpec = tween(1000), label = "gym")
    val animatedBoxing by animateFloatAsState(targetValue = boxingProgress.coerceIn(0f, 1f), animationSpec = tween(1000), label = "boxing")
    val animatedCardio by animateFloatAsState(targetValue = cardioProgress.coerceIn(0f, 1f), animationSpec = tween(1000), label = "cardio")

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFF121212),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1.2f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "DAILY ACTIVITY RINGS",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFFF6B00),
                    letterSpacing = 1.sp
                )

                Text(
                    text = "CLOSE YOUR RINGS TODAY",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Gym Legend
                RingLegendItem(
                    icon = Icons.Default.FitnessCenter,
                    label = "GYM / STRENGTH",
                    countStr = "${gymCompleted}/${gymTasks.size}",
                    color = Color(0xFFFF6B00)
                )

                // Boxing Legend
                RingLegendItem(
                    icon = Icons.Default.SportsMma,
                    label = "BOXING / DISCIPLINE",
                    countStr = "${boxingCompleted}/${boxingTasks.size}",
                    color = Color(0xFFEF4444)
                )

                // Cardio Legend
                RingLegendItem(
                    icon = Icons.Default.DirectionsRun,
                    label = "CARDIO / MOVEMENT",
                    countStr = "${cardioCompleted}/${cardioTasks.size}",
                    color = Color(0xFF22C55E)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Concentric Rings Canvas
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(120.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 12.dp.toPx()
                    val centerOffset = Offset(size.width / 2, size.height / 2)

                    // Ring 1 (Outer - Gym)
                    val r1 = (size.width / 2) - (strokeWidth / 2)
                    drawArc(
                        color = Color(0xFFFF6B00).copy(alpha = 0.2f),
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = Offset(centerOffset.x - r1, centerOffset.y - r1),
                        size = Size(r1 * 2, r1 * 2),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = Color(0xFFFF6B00),
                        startAngle = -90f,
                        sweepAngle = animatedGym * 360f,
                        useCenter = false,
                        topLeft = Offset(centerOffset.x - r1, centerOffset.y - r1),
                        size = Size(r1 * 2, r1 * 2),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // Ring 2 (Middle - Boxing)
                    val r2 = r1 - strokeWidth - 4.dp.toPx()
                    drawArc(
                        color = Color(0xFFEF4444).copy(alpha = 0.2f),
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = Offset(centerOffset.x - r2, centerOffset.y - r2),
                        size = Size(r2 * 2, r2 * 2),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = Color(0xFFEF4444),
                        startAngle = -90f,
                        sweepAngle = animatedBoxing * 360f,
                        useCenter = false,
                        topLeft = Offset(centerOffset.x - r2, centerOffset.y - r2),
                        size = Size(r2 * 2, r2 * 2),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // Ring 3 (Inner - Cardio)
                    val r3 = r2 - strokeWidth - 4.dp.toPx()
                    drawArc(
                        color = Color(0xFF22C55E).copy(alpha = 0.2f),
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = Offset(centerOffset.x - r3, centerOffset.y - r3),
                        size = Size(r3 * 2, r3 * 2),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = Color(0xFF22C55E),
                        startAngle = -90f,
                        sweepAngle = animatedCardio * 360f,
                        useCenter = false,
                        topLeft = Offset(centerOffset.x - r3, centerOffset.y - r3),
                        size = Size(r3 * 2, r3 * 2),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }

                // Center overall completion %
                val totalTasks = tasks.size
                val totalCompleted = tasks.count { it.isCompleted }
                val overallPercent = if (totalTasks == 0) 0 else ((totalCompleted.toFloat() / totalTasks) * 100).toInt()

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$overallPercent%",
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = Color.White
                    )
                    Text(
                        text = "CLOSED",
                        fontWeight = FontWeight.Bold,
                        fontSize = 8.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
private fun RingLegendItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    countStr: String,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, RoundedCornerShape(100.dp))
        )
        Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(12.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 10.sp
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = countStr,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Black,
            color = color,
            fontSize = 11.sp
        )
    }
}

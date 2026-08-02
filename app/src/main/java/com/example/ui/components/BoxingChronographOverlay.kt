package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.BoxingMovement
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
fun BoxingChronographOverlay(
    title: String,
    movements: List<BoxingMovement>,
    totalDurationSec: Int,
    onCompleteSession: () -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val safeMovements = if (movements.isNotEmpty()) movements else listOf(
        BoxingMovement("Jab-Cross-Hook", 30),
        BoxingMovement("Duck & Counter", 30),
        BoxingMovement("Heavy Bag Power Shots", 45),
        BoxingMovement("Shadowboxing Rest", 15)
    )

    var currentIdx by remember { mutableIntStateOf(0) }
    var currentMovementTimeLeft by remember { mutableIntStateOf(safeMovements.first().durationSeconds) }
    var totalSecondsRemaining by remember { mutableIntStateOf(if (totalDurationSec > 0) totalDurationSec else safeMovements.sumOf { it.durationSeconds }) }
    var isRunning by remember { mutableStateOf(true) }

    val currentMovement = safeMovements[currentIdx]

    // Timer Loop
    LaunchedEffect(isRunning, currentIdx, currentMovementTimeLeft, totalSecondsRemaining) {
        if (isRunning && totalSecondsRemaining > 0) {
            delay(1000L)
            if (currentMovementTimeLeft > 1) {
                currentMovementTimeLeft -= 1
                totalSecondsRemaining -= 1
            } else {
                // Movement Finished -> Advance to next movement
                HapticFeedbackHelper.triggerDoublePulse(context)
                if (currentIdx < safeMovements.size - 1) {
                    currentIdx += 1
                    currentMovementTimeLeft = safeMovements[currentIdx].durationSeconds
                } else {
                    // Loop movements if total session time remains
                    currentIdx = 0
                    currentMovementTimeLeft = safeMovements[0].durationSeconds
                }
                totalSecondsRemaining -= 1
            }
            if (totalSecondsRemaining <= 0) {
                HapticFeedbackHelper.triggerDoublePulse(context)
                onCompleteSession()
            }
        }
    }

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF09090B))
                .padding(24.dp)
        ) {
            // Close Button Top Right
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(48.dp)
                    .background(Color(0xFF1F1F23), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.White
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "BOXING CHRONOGRAPH",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFFFF5722),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Total Session Left: ${formatMinSec(totalSecondsRemaining)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }

                // Center Display: Current Movement
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(260.dp)
                        .clip(CircleShape)
                        .border(6.dp, Color(0xFFFF5722), CircleShape)
                        .background(Color(0xFF141417))
                        .padding(24.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "MOVEMENT ${currentIdx + 1} / ${safeMovements.size}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFFF9800),
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        AnimatedContent(
                            targetState = currentMovement.name,
                            label = "movementName"
                        ) { movementName ->
                            Text(
                                text = movementName,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = formatMinSec(currentMovementTimeLeft),
                            style = MaterialTheme.typography.displayLarge,
                            fontWeight = FontWeight.Black,
                            color = if (currentMovementTimeLeft <= 5) Color(0xFFFF3D00) else Color.White
                        )
                    }
                }

                // Upcoming Movement Preview
                val nextMovement = safeMovements[(currentIdx + 1) % safeMovements.size]
                Surface(
                    color = Color(0xFF18181C),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "NEXT MOVEMENT",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray
                            )
                            Text(
                                text = nextMovement.name,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Text(
                            text = "${nextMovement.durationSeconds}s",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF9800)
                        )
                    }
                }

                // Controls: Play/Pause/Skip & Finish
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Play/Pause
                    IconButton(
                        onClick = { isRunning = !isRunning },
                        modifier = Modifier
                            .size(72.dp)
                            .background(
                                if (isRunning) Color(0xFF27272A) else Color(0xFFFF5722),
                                CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Toggle Timer",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    // Skip Movement
                    IconButton(
                        onClick = {
                            if (currentIdx < safeMovements.size - 1) {
                                currentIdx += 1
                                currentMovementTimeLeft = safeMovements[currentIdx].durationSeconds
                            } else {
                                currentIdx = 0
                                currentMovementTimeLeft = safeMovements[0].durationSeconds
                            }
                            HapticFeedbackHelper.triggerVibration(context, 50)
                        },
                        modifier = Modifier
                            .size(56.dp)
                            .background(Color(0xFF27272A), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Skip Movement",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // Complete Session Button
                    Button(
                        onClick = onCompleteSession,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E)),
                        modifier = Modifier.height(56.dp)
                    ) {
                        Text(
                            text = "FINISH DRILL",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

private fun formatMinSec(seconds: Int): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return String.format(Locale.US, "%02d:%02d", mins, secs)
}

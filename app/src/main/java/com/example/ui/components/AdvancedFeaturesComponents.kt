package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.UserProfile
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class SyncState {
    OFFLINE_READY,
    SYNCING,
    SYNCED
}

@Composable
fun CloudSyncStatusChip(
    syncState: SyncState,
    lastSyncTimestamp: Long,
    onSyncClick: () -> Unit
) {
    val dateStr = remember(lastSyncTimestamp) {
        if (lastSyncTimestamp <= 0) "Offline Ready"
        else SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(lastSyncTimestamp))
    }

    Surface(
        shape = RoundedCornerShape(100.dp),
        color = Color(0xFF161616),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
        modifier = Modifier.clickable { onSyncClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(
                        when (syncState) {
                            SyncState.SYNCED -> Color(0xFF22C55E)
                            SyncState.SYNCING -> Color(0xFFFF914D)
                            SyncState.OFFLINE_READY -> Color(0xFF3B82F6)
                        },
                        CircleShape
                    )
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = when (syncState) {
                    SyncState.SYNCED -> "SYNCED ($dateStr)"
                    SyncState.SYNCING -> "SYNCING CLOUD..."
                    SyncState.OFFLINE_READY -> "OFFLINE FIRST"
                },
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.CloudSync,
                contentDescription = "Cloud Sync",
                tint = Color(0xFFFF6B00),
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
fun DeloadBanner(
    onDeactivate: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF2E1A00),
        border = BorderStroke(1.dp, Color(0xFFFF914D).copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFFFF6B00).copy(alpha = 0.2f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.BatterySaver,
                        contentDescription = "Deload Mode",
                        tint = Color(0xFFFF6B00),
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "SMART DELOAD ACTIVE (-50% VOL)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFFF914D)
                    )
                    Text(
                        text = "Preventing burnout & protecting your streak.",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }

            TextButton(onClick = onDeactivate) {
                Text(
                    text = "RESTORE 100%",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun AccountabilityPartnerCard(
    userProfile: UserProfile,
    onSendNudge: () -> Unit,
    onEditPartner: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFF161616),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFF3B82F6).copy(alpha = 0.15f), CircleShape)
                            .border(1.dp, Color(0xFF3B82F6).copy(alpha = 0.3f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = "Accountability Partner",
                            tint = Color(0xFF3B82F6),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "ACCOUNTABILITY PARTNER",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = Color.White.copy(alpha = 0.5f)
                        )
                        Text(
                            text = "${userProfile.partnerName} (${userProfile.partnerCode})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                IconButton(onClick = onEditPartner) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Partner",
                        tint = Color.White.copy(alpha = 0.4f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onSendNudge,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                border = BorderStroke(1.dp, Color(0xFF3B82F6).copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.NotificationsActive,
                    contentDescription = "Nudge",
                    tint = Color(0xFF60A5FA),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "PING PARTNER FOR MOTIVATION 📣",
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF60A5FA),
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun QrCodeView(
    dataString: String,
    modifier: Modifier = Modifier
) {
    // Generate deterministic 15x15 binary matrix based on hash of input string
    val matrixSize = 15
    val matrix = remember(dataString) {
        val hash = dataString.hashCode()
        Array(matrixSize) { r ->
            BooleanArray(matrixSize) { c ->
                if ((r < 3 && c < 3) || (r < 3 && c >= matrixSize - 3) || (r >= matrixSize - 3 && c < 3)) {
                    // Finder pattern corners
                    true
                } else {
                    ((hash xor (r * 31 + c * 17)) % 3 == 0) || ((r + c) % 2 == 0 && dataString.length % 2 == 0)
                }
            }
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .background(Color.White, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cellWidth = size.width / matrixSize
            val cellHeight = size.height / matrixSize

            for (r in 0 until matrixSize) {
                for (c in 0 until matrixSize) {
                    if (matrix[r][c]) {
                        drawRoundRect(
                            color = Color.Black,
                            topLeft = Offset(c * cellWidth, r * cellHeight),
                            size = Size(cellWidth * 0.88f, cellHeight * 0.88f),
                            cornerRadius = CornerRadius(4f, 4f)
                        )
                    }
                }
            }
        }
    }
}

package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.UserProfile
import com.example.ui.AppViewModel
import com.example.ui.components.NeonProfileAvatar

@Composable
fun WarRoomProfileScreen(viewModel: AppViewModel) {
    val userProfile by viewModel.userProfile.collectAsState()
    val firebaseUser by viewModel.firebaseUser.collectAsState()
    val monthlyTasks by viewModel.selectedMonthTasks.collectAsState()

    val totalWorkoutsCompleted = remember(monthlyTasks) {
        monthlyTasks.count { it.isCompleted }
    }

    var showGoogleAuthDialog by remember { mutableStateOf(false) }
    var bypassGuardForGuest by remember { mutableStateOf(false) }

    val isAuthenticated = (userProfile.uid != "guest_warrior" || firebaseUser != null)

    if (!isAuthenticated && !bypassGuardForGuest) {
        // Aggressive Dark Google Sign-In Splash Guard Screen
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0A0A0A))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(100.dp)
                    .background(Color(0xFFFF6B00).copy(alpha = 0.15f), CircleShape)
                    .border(2.dp, Color(0xFFFF6B00), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.LocalFireDepartment,
                    contentDescription = "Flame",
                    tint = Color(0xFFFF6B00),
                    modifier = Modifier.size(56.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "BE IT NOW",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Black,
                color = Color.White,
                letterSpacing = 2.sp
            )

            Text(
                text = "THE WAR ROOM COMMAND CENTER",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFFFF6B00),
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Sign in with your Google account to sync your fire streaks, unlocked badges, and Legacy Feed posts across devices via Firebase Auth & Firestore.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Primary Google Sign In Button
            Button(
                onClick = {
                    viewModel.signInWithGoogleAccount(
                        name = "Marcus Vance",
                        email = "marcus.vance@gmail.com",
                        photoUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=300&q=80"
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Google Icon",
                    tint = Color.Black,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "SIGN IN WITH GOOGLE",
                    fontWeight = FontWeight.Black,
                    color = Color.Black,
                    fontSize = 14.sp,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = { bypassGuardForGuest = true },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Gray),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(
                    text = "CONTINUE AS OFFLINE WARRIOR",
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0A0A0A))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
        // Title Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "THE WAR ROOM",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = "USER PROFILE & COMMAND METRICS",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF6B00)
                    )
                }

                // Cloud Firestore Status Chip
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF181818),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = if (userProfile.uid != "guest_warrior") Icons.Default.CloudDone else Icons.Default.CloudOff,
                            contentDescription = "Sync",
                            tint = if (userProfile.uid != "guest_warrior") Color(0xFF22C55E) else Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (userProfile.uid != "guest_warrior") "FIRESTORE SYNCED" else "OFFLINE READY",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = if (userProfile.uid != "guest_warrior") Color(0xFF22C55E) else Color.Gray
                        )
                    }
                }
            }
        }

        // Main User Card with Dynamic Neon Avatar
        item {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFF141414),
                border = BorderStroke(1.5.dp, Color(0xFFFF6B00).copy(alpha = 0.8f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    // Neon Profile Avatar with dynamic level coloring
                    NeonProfileAvatar(
                        photoUrl = userProfile.photoUrl,
                        level = userProfile.level,
                        size = 110.dp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = userProfile.displayName.uppercase(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )

                    Text(
                        text = userProfile.email,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.6f)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Level & XP Bar
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Shield, contentDescription = "Level", tint = Color(0xFFFF6B00), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "LEVEL ${userProfile.level}",
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFFF6B00),
                                fontSize = 13.sp
                            )
                        }

                        Text(
                            text = "${userProfile.xp} / ${userProfile.level * 300} XP",
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    val xpProgress = (userProfile.xp % 300).toFloat() / 300f
                    LinearProgressIndicator(
                        progress = { xpProgress.coerceIn(0.05f, 1f) },
                        color = Color(0xFFFF6B00),
                        trackColor = Color(0xFF282828),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Google Authentication Button / Sign Out
                    if (userProfile.uid == "guest_warrior" && firebaseUser == null) {
                        Button(
                            onClick = { showGoogleAuthDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Google Sign In",
                                tint = Color.Black
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "SIGN IN WITH GOOGLE",
                                fontWeight = FontWeight.Black,
                                color = Color.Black,
                                fontSize = 12.sp,
                                letterSpacing = 1.sp
                            )
                        }
                    } else {
                        OutlinedButton(
                            onClick = { viewModel.signOutAccount() },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                            border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.6f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                        ) {
                            Icon(Icons.Default.Logout, contentDescription = "Sign Out", tint = Color.Red, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "SIGN OUT WAR ROOM",
                                fontWeight = FontWeight.Bold,
                                color = Color.Red,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        // Core Performance Metrics Summary (3 Grid Cards)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Fire Streak Card
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF141414),
                    border = BorderStroke(1.dp, Color(0xFFFF6B00).copy(alpha = 0.4f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = "Fire Streak",
                            tint = Color(0xFFFF6B00),
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${userProfile.currentStreak} DAYS",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            text = "FIRE STREAK 🔥",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF6B00)
                        )
                    }
                }

                // Total Workouts Completed
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF141414),
                    border = BorderStroke(1.dp, Color(0xFF22C55E).copy(alpha = 0.4f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.FitnessCenter,
                            contentDescription = "Workouts",
                            tint = Color(0xFF22C55E),
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "$totalWorkoutsCompleted",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            text = "WORKOUTS DONE",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF22C55E)
                        )
                    }
                }
            }
        }

        // Unlocked Badges Showcase
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF141414),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.MilitaryTech, contentDescription = "Badges", tint = Color(0xFFFFD700))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "UNLOCKED BADGES",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val rawBadges = userProfile.unlockedBadgesRaw.split(",").filter { it.isNotBlank() }
                    val allBadges = listOf(
                        "STREAK_3" to "3-Day Discipline Spark 🔥",
                        "STREAK_7" to "7-Day Fire Streak 💥",
                        "IRON_BEAST" to "Iron Beast 🏋️",
                        "BOXING_TITAN" to "Boxing Titan 🥊",
                        "WAR_ROOM_ELITE" to "War Room Elite 🛡️"
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(allBadges) { (code, title) ->
                            val isUnlocked = rawBadges.contains(code) || code == "STREAK_3" || code == "IRON_BEAST"

                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (isUnlocked) Color(0xFF222222) else Color(0xFF121212),
                                border = BorderStroke(
                                    1.dp,
                                    if (isUnlocked) Color(0xFFFFD700) else Color.White.copy(alpha = 0.1f)
                                )
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isUnlocked) Icons.Default.MilitaryTech else Icons.Default.Star,
                                        contentDescription = title,
                                        tint = if (isUnlocked) Color(0xFFFFD700) else Color.Gray,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = if (isUnlocked) Color.White else Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Google Sign-In Simulation Dialog
    if (showGoogleAuthDialog) {
        AlertDialog(
            onDismissRequest = { showGoogleAuthDialog = false },
            containerColor = Color(0xFF141414),
            title = {
                Text(
                    text = "GOOGLE SIGN-IN (FIREBASE AUTH)",
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            },
            text = {
                Column {
                    Text(
                        text = "Authenticating with Firebase Auth using project lalo-b312c...",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Signed in as Marcus Vance (Google Account)",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF6B00)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.signInWithGoogleAccount(
                            name = "Marcus Vance",
                            email = "marcus.vance@gmail.com",
                            photoUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=300&q=80"
                        )
                        showGoogleAuthDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6B00))
                ) {
                    Text("CONNECT GOOGLE ACCOUNT", fontWeight = FontWeight.Black, color = Color.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showGoogleAuthDialog = false }) {
                    Text("CANCEL", color = Color.Gray)
                }
            }
        )
    }
    }
}

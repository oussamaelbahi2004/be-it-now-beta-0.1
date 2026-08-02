package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MonthlyReport
import com.example.ui.AppViewModel
import com.example.ui.components.BodyWeightChartCard
import com.example.ui.components.HapticFeedbackHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsJudgmentDayScreen(
    viewModel: AppViewModel
) {
    val context = LocalContext.current
    val userProfile by viewModel.userProfile.collectAsState()
    val monthlyReports by viewModel.monthlyReports.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val weightLogs by viewModel.allWeightLogs.collectAsState()
    val todayCalories by viewModel.todayCaloriesBurned.collectAsState()
    val monthlyCalories by viewModel.monthlyCaloriesBurned.collectAsState()

    val currentReport = remember(monthlyReports, selectedMonth) {
        monthlyReports.find { it.monthYearKey == selectedMonth }
    }

    val unlockedBadgesList = remember(userProfile.unlockedBadgesRaw) {
        userProfile.unlockedBadgesRaw.split(",").filter { it.isNotBlank() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "JUDGMENT DAY & STATS",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Judgment Day Feature Banner
            item {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color(0xFF161616),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Assessment,
                                contentDescription = "Judgment Day",
                                tint = Color(0xFFFF6B00),
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "MONTHLY JUDGMENT DAY",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Month Assessment: $selectedMonth",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.5f)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        if (currentReport == null) {
                            Button(
                                onClick = {
                                    viewModel.evaluateMonthlyJudgmentDay()
                                    HapticFeedbackHelper.triggerDoublePulse(context)
                                },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6B00)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "RUN JUDGMENT DAY EVALUATION",
                                    fontWeight = FontWeight.Black,
                                    color = Color.Black
                                )
                            }
                        } else {
                            // Grade Badge Box
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val gradeColor = when (currentReport.grade) {
                                    "A" -> Color(0xFF22C55E)
                                    "B" -> Color(0xFFFF914D)
                                    else -> Color(0xFFEF4444)
                                }

                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(90.dp)
                                        .background(gradeColor.copy(alpha = 0.15f), CircleShape)
                                        .border(1.dp, gradeColor.copy(alpha = 0.3f), CircleShape)
                                ) {
                                    Text(
                                        text = "GRADE ${currentReport.grade}",
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Black,
                                        color = gradeColor
                                    )
                                }

                                Column {
                                    Text(
                                        text = "${currentReport.completionPercentage.toInt()}% COMPLETED",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "${currentReport.totalCompleted} / ${currentReport.totalScheduled} Missions Done",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.6f)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Surface(
                                color = Color.White.copy(alpha = 0.05f),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = currentReport.evaluationNotes,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.8f),
                                    modifier = Modifier.padding(14.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Body Weight & Composition Dashboard (MET Active Calories & Line Chart)
            item {
                BodyWeightChartCard(
                    currentWeightKg = userProfile.bodyWeightKg,
                    todayCalories = todayCalories,
                    monthlyCalories = monthlyCalories,
                    weightLogs = weightLogs,
                    onLogWeight = { weightKg, notes ->
                        viewModel.logBodyWeight(weightKg, notes)
                    }
                )
            }

            // Streak & Level Overview Card
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = Color(0xFF161616),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = null,
                                tint = Color(0xFFFF6B00),
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("BEST STREAK", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.5f), fontWeight = FontWeight.Bold)
                            Text("${userProfile.maxStreak} Days", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = Color.White)
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = Color(0xFF161616),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = Color(0xFFFF914D),
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("TOTAL XP", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.5f), fontWeight = FontWeight.Bold)
                            Text("${userProfile.xp} XP", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = Color.White)
                        }
                    }
                }
            }

            // Unlocked Badges Grid Section
            item {
                Text(
                    text = "UNLOCKED BADGES & TROPHIES (${unlockedBadgesList.size})",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Black,
                    color = Color.White.copy(alpha = 0.5f),
                    letterSpacing = 1.sp
                )
            }

            item {
                val allPossibleBadges = listOf(
                    "3-Day Discipline Spark 🔥" to "STREAK_3",
                    "7-Day Fire Streak 💥" to "STREAK_7",
                    "Boxing Titan 🥊" to "BOXING_TITAN",
                    "Iron Beast 🏋️" to "IRON_BEAST"
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    allPossibleBadges.forEach { (badgeTitle, badgeCode) ->
                        val isUnlocked = unlockedBadgesList.contains(badgeCode)
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color(0xFF161616),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MilitaryTech,
                                    contentDescription = null,
                                    tint = if (isUnlocked) Color(0xFFFF6B00) else Color.White.copy(alpha = 0.2f),
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = badgeTitle,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isUnlocked) Color.White else Color.White.copy(alpha = 0.4f)
                                    )
                                    Text(
                                        text = if (isUnlocked) "UNLOCKED" else "LOCKED - Complete more missions to unlock",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isUnlocked) Color(0xFF22C55E) else Color.White.copy(alpha = 0.4f),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

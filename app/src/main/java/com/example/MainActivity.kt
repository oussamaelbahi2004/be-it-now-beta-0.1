package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.AppViewModel
import com.example.ui.components.DopamineDialog
import com.example.ui.screens.ExecutionDashboardScreen
import com.example.ui.screens.LegacyFeedScreen
import com.example.ui.screens.MonthlyPlannerScreen
import com.example.ui.screens.StatsJudgmentDayScreen
import com.example.ui.screens.TaskCreatorScreen
import com.example.ui.screens.WarRoomProfileScreen
import com.example.ui.theme.BeItNowTheme

class MainActivity : ComponentActivity() {

    private val viewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            BeItNowTheme {
                val navController = rememberNavController()
                val backStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = backStackEntry?.destination?.route ?: "execution"

                val rewardEvent by viewModel.rewardEvent.collectAsState()

                // Reward Celebration Dialog Popup
                rewardEvent?.let { event ->
                    DopamineDialog(
                        xpEarned = event.xpEarned,
                        level = event.newLevel,
                        streak = event.streak,
                        newBadges = event.newBadges,
                        onDismiss = { viewModel.dismissRewardDialog() }
                    )
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        NavigationBar(
                            containerColor = Color(0xFF141414),
                            contentColor = Color(0xFFFF6B00)
                        ) {
                            NavigationBarItem(
                                selected = currentRoute == "execution",
                                onClick = {
                                    if (currentRoute != "execution") {
                                        navController.navigate("execution") {
                                            popUpTo("execution") { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                },
                                icon = { Icon(Icons.Default.FlashOn, contentDescription = "Execute") },
                                label = { Text("EXECUTE", fontWeight = FontWeight.Black, fontSize = 9.sp, maxLines = 1, softWrap = false) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.Black,
                                    selectedTextColor = Color(0xFFFF6B00),
                                    indicatorColor = Color(0xFFFF6B00),
                                    unselectedIconColor = Color.White.copy(alpha = 0.4f),
                                    unselectedTextColor = Color.White.copy(alpha = 0.4f)
                                )
                            )

                            NavigationBarItem(
                                selected = currentRoute == "templates",
                                onClick = {
                                    if (currentRoute != "templates") {
                                        navController.navigate("templates") {
                                            popUpTo("execution") { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                },
                                icon = { Icon(Icons.Default.FitnessCenter, contentDescription = "Templates") },
                                label = { Text("TEMPLATES", fontWeight = FontWeight.Black, fontSize = 9.sp, maxLines = 1, softWrap = false) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.Black,
                                    selectedTextColor = Color(0xFFFF6B00),
                                    indicatorColor = Color(0xFFFF6B00),
                                    unselectedIconColor = Color.White.copy(alpha = 0.4f),
                                    unselectedTextColor = Color.White.copy(alpha = 0.4f)
                                )
                            )

                            NavigationBarItem(
                                selected = currentRoute == "planner",
                                onClick = {
                                    if (currentRoute != "planner") {
                                        navController.navigate("planner") {
                                            popUpTo("execution") { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                },
                                icon = { Icon(Icons.Default.CalendarMonth, contentDescription = "Planner") },
                                label = { Text("PLANNER", fontWeight = FontWeight.Black, fontSize = 9.sp, maxLines = 1, softWrap = false) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.Black,
                                    selectedTextColor = Color(0xFFFF6B00),
                                    indicatorColor = Color(0xFFFF6B00),
                                    unselectedIconColor = Color.White.copy(alpha = 0.4f),
                                    unselectedTextColor = Color.White.copy(alpha = 0.4f)
                                )
                            )

                            NavigationBarItem(
                                selected = currentRoute == "judgment",
                                onClick = {
                                    if (currentRoute != "judgment") {
                                        navController.navigate("judgment") {
                                            popUpTo("execution") { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                },
                                icon = { Icon(Icons.Default.Assessment, contentDescription = "Judgment Day") },
                                label = { Text("JUDGMENT", fontWeight = FontWeight.Black, fontSize = 9.sp, maxLines = 1, softWrap = false) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.Black,
                                    selectedTextColor = Color(0xFFFF6B00),
                                    indicatorColor = Color(0xFFFF6B00),
                                    unselectedIconColor = Color.White.copy(alpha = 0.4f),
                                    unselectedTextColor = Color.White.copy(alpha = 0.4f)
                                )
                            )
                            NavigationBarItem(
                                selected = currentRoute == "legacy",
                                onClick = {
                                    if (currentRoute != "legacy") {
                                        navController.navigate("legacy") {
                                            popUpTo("execution") { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                },
                                icon = { Icon(Icons.Default.CameraAlt, contentDescription = "Legacy") },
                                label = { Text("LEGACY", fontWeight = FontWeight.Black, fontSize = 9.sp, maxLines = 1, softWrap = false) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.Black,
                                    selectedTextColor = Color(0xFFFF6B00),
                                    indicatorColor = Color(0xFFFF6B00),
                                    unselectedIconColor = Color.White.copy(alpha = 0.4f),
                                    unselectedTextColor = Color.White.copy(alpha = 0.4f)
                                )
                            )

                            NavigationBarItem(
                                selected = currentRoute == "war_room",
                                onClick = {
                                    if (currentRoute != "war_room") {
                                        navController.navigate("war_room") {
                                            popUpTo("execution") { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                },
                                icon = { Icon(Icons.Default.Shield, contentDescription = "War Room") },
                                label = { Text("WAR ROOM", fontWeight = FontWeight.Black, fontSize = 9.sp, maxLines = 1, softWrap = false) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.Black,
                                    selectedTextColor = Color(0xFFFF6B00),
                                    indicatorColor = Color(0xFFFF6B00),
                                    unselectedIconColor = Color.White.copy(alpha = 0.4f),
                                    unselectedTextColor = Color.White.copy(alpha = 0.4f)
                                )
                            )
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "execution",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("execution") {
                            ExecutionDashboardScreen(viewModel = viewModel)
                        }
                        composable("templates") {
                            TaskCreatorScreen(viewModel = viewModel)
                        }
                        composable("planner") {
                            MonthlyPlannerScreen(viewModel = viewModel)
                        }
                        composable("judgment") {
                            StatsJudgmentDayScreen(viewModel = viewModel)
                        }
                        composable("legacy") {
                            LegacyFeedScreen(viewModel = viewModel)
                        }
                        composable("war_room") {
                            WarRoomProfileScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}

package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.ScheduledTask
import com.example.data.TaskType
import com.example.ui.AppViewModel
import com.example.ui.components.BoxingChronographOverlay
import com.example.ui.components.HapticFeedbackHelper
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

import com.example.ui.components.AccountabilityPartnerCard
import com.example.ui.components.ActivityRingsCard
import com.example.ui.components.CloudSyncStatusChip
import com.example.ui.components.DeloadBanner
import com.example.ui.dialogs.AccountabilityPartnerDialog
import com.example.ui.dialogs.HealthSyncDialog
import com.example.ui.dialogs.SmartDeloadDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExecutionDashboardScreen(
    viewModel: AppViewModel
) {
    val context = LocalContext.current
    val selectedDate by viewModel.selectedDate.collectAsState()
    val tasks by viewModel.selectedDateTasks.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val syncState by viewModel.syncState.collectAsState()
    val partnerNudgeAlert by viewModel.partnerNudgeAlert.collectAsState()
    val showHealthSyncDialog by viewModel.showHealthSyncDialog.collectAsState()
    val showDeloadDialog by viewModel.showDeloadDialog.collectAsState()

    var showPartnerEditDialog by remember { mutableStateOf(false) }

    var activeBoxingTask by remember { mutableStateOf<ScheduledTask?>(null) }
    var filterType by remember { mutableStateOf<TaskType?>(null) }

    val filteredTasks = remember(tasks, filterType) {
        if (filterType == null) tasks else tasks.filter { it.type == filterType }
    }

    // Active Boxing Timer Dialog
    activeBoxingTask?.let { task ->
        val movements = remember(task) { AppViewModel.parseBoxingMovements(task.title) }
        BoxingChronographOverlay(
            title = task.title,
            movements = movements,
            totalDurationSec = 1200,
            onCompleteSession = {
                viewModel.completeTask(task)
                activeBoxingTask = null
                HapticFeedbackHelper.triggerDoublePulse(context)
            },
            onClose = { activeBoxingTask = null }
        )
    }

    // Health Connect Sync Dialog
    if (showHealthSyncDialog) {
        HealthSyncDialog(
            matchingTasks = tasks.filter { !it.isCompleted && (it.type == TaskType.MAIN_SIDE || it.type == TaskType.BOXING) },
            onConfirmAutoComplete = {
                viewModel.confirmHealthSyncAutoComplete()
                HapticFeedbackHelper.triggerDoublePulse(context)
            },
            onDismiss = { viewModel.dismissHealthSyncDialog() }
        )
    }

    // Smart Deload Dialog
    if (showDeloadDialog) {
        SmartDeloadDialog(
            isActive = userProfile.isDeloadModeActive,
            onToggle = { viewModel.toggleDeloadMode(it) },
            onDismiss = { viewModel.dismissDeloadDialog() }
        )
    }

    // Partner Edit Dialog
    if (showPartnerEditDialog) {
        AccountabilityPartnerDialog(
            partnerName = userProfile.partnerName,
            partnerCode = userProfile.partnerCode,
            onSavePartner = { name, code -> viewModel.updatePartnerInfo(name, code) },
            onDismiss = { showPartnerEditDialog = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "BE IT NOW",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFFF6B00),
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = Color(0xFFFF6B00).copy(alpha = 0.2f),
                                border = BorderStroke(1.dp, Color(0xFFFF6B00).copy(alpha = 0.4f)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "EXECUTION",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFF6B00),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        // Cloud Sync Badge Button
                        CloudSyncStatusChip(
                            syncState = syncState,
                            lastSyncTimestamp = userProfile.lastCloudSyncTimestamp,
                            onSyncClick = { viewModel.triggerCloudSync() }
                        )
                    }
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
            // Partner Nudge Alert Banner
            partnerNudgeAlert?.let { alert ->
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF1E293B),
                        border = BorderStroke(1.dp, Color(0xFF3B82F6)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = null,
                                    tint = Color(0xFF60A5FA),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = alert,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            IconButton(onClick = { viewModel.dismissPartnerNudge() }) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                            }
                        }
                    }
                }
            }

            // Smart Deload Mode Active Banner
            if (userProfile.isDeloadModeActive) {
                item {
                    DeloadBanner(
                        onDeactivate = { viewModel.toggleDeloadMode(false) }
                    )
                }
            }

            // Header Streak Card
            item {
                StreakHeaderCard(
                    streak = userProfile.currentStreak,
                    xp = userProfile.xp,
                    level = userProfile.level
                )
            }

            // Daily Activity Rings
            item {
                ActivityRingsCard(tasks = tasks)
            }

            // Health Connect & Smartwatch Sync Quick Action Bar
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF161616),
                    border = BorderStroke(1.dp, Color(0xFF22C55E).copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color(0xFF22C55E).copy(alpha = 0.15f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Watch,
                                    contentDescription = "Watch Sync",
                                    tint = Color(0xFF22C55E),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "HEALTH CONNECT AUTO-SYNC",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF22C55E)
                                )
                                Text(
                                    text = "Apple Health & Google Fit active",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.6f)
                                )
                            }
                        }

                        Button(
                            onClick = { viewModel.openHealthSyncDialog() },
                            shape = RoundedCornerShape(100.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E))
                        ) {
                            Text(
                                text = "SYNC ⌚",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = Color.Black
                            )
                        }
                    }
                }
            }

            // Date Selector Carousel
            item {
                DateSelectorRow(
                    selectedDate = selectedDate,
                    onDateSelect = { viewModel.setSelectedDate(it) }
                )
            }

            // Category Filter Pills
            item {
                FilterPillRow(
                    selectedType = filterType,
                    onSelect = { filterType = it }
                )
            }

            // Today's Missions Count Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TODAY'S MISSIONS (${filteredTasks.count { it.isCompleted }}/${filteredTasks.size})",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = selectedDate,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Empty state check
            if (filteredTasks.isEmpty()) {
                item {
                    EmptyExecutionCard()
                }
            } else {
                items(filteredTasks, key = { it.id }) { task ->
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = { dismissValue ->
                            when (dismissValue) {
                                SwipeToDismissBoxValue.StartToEnd -> {
                                    if (task.isCompleted) {
                                        viewModel.uncompleteTask(task)
                                    } else {
                                        viewModel.completeTask(task)
                                        HapticFeedbackHelper.triggerVibration(context)
                                    }
                                    false
                                }
                                SwipeToDismissBoxValue.EndToStart -> {
                                    viewModel.deleteScheduledTask(task.id)
                                    HapticFeedbackHelper.triggerDoublePulse(context)
                                    true
                                }
                                SwipeToDismissBoxValue.Settled -> false
                            }
                        }
                    )

                    SwipeToDismissBox(
                        state = dismissState,
                        backgroundContent = {
                            val direction = dismissState.dismissDirection
                            val color = when (dismissState.targetValue) {
                                SwipeToDismissBoxValue.StartToEnd -> Color(0xFF22C55E)
                                SwipeToDismissBoxValue.EndToStart -> Color(0xFFEF4444)
                                SwipeToDismissBoxValue.Settled -> Color.Transparent
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(color, RoundedCornerShape(24.dp))
                                    .padding(horizontal = 20.dp),
                                contentAlignment = if (direction == SwipeToDismissBoxValue.StartToEnd) Alignment.CenterStart else Alignment.CenterEnd
                            ) {
                                if (direction == SwipeToDismissBoxValue.StartToEnd) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Check, contentDescription = "Complete", tint = Color.Black)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("TOGGLE COMPLETE", fontWeight = FontWeight.Black, color = Color.Black, fontSize = 12.sp)
                                    }
                                } else if (direction == SwipeToDismissBoxValue.EndToStart) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("DELETE PROTOCOL", fontWeight = FontWeight.Black, color = Color.White, fontSize = 12.sp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White)
                                    }
                                }
                            }
                        }
                    ) {
                        TaskExecutionCard(
                            task = task,
                            onCompleteToggle = {
                                if (task.isCompleted) {
                                    viewModel.uncompleteTask(task)
                                } else {
                                    viewModel.completeTask(task)
                                    HapticFeedbackHelper.triggerVibration(context)
                                }
                            },
                            onStartBoxing = {
                                activeBoxingTask = task
                            },
                            onUpdateSideMission = { missionTitle, isChecked ->
                                viewModel.updateSideMissionStatus(task, missionTitle, isChecked)
                                HapticFeedbackHelper.triggerVibration(context, 40)
                            },
                            onDelete = {
                                viewModel.deleteScheduledTask(task.id)
                            }
                        )
                    }
                }
            }

            // Accountability Partner Card
            item {
                AccountabilityPartnerCard(
                    userProfile = userProfile,
                    onSendNudge = { viewModel.sendPartnerMotivationNudge() },
                    onEditPartner = { showPartnerEditDialog = true }
                )
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun StreakHeaderCard(streak: Int, xp: Int, level: Int) {
    val nextLevelXp = level * 300
    val progress = (xp % 300).toFloat() / 300f

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFF161616),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Background Hero Watermark
            Image(
                painter = painterResource(id = R.drawable.img_hero_banner_1785678635339),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                contentScale = ContentScale.Crop,
                alpha = 0.20f
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
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
                                .size(48.dp)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(Color(0xFFFF6B00), Color(0xFFFF914D))
                                    ),
                                    CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = "Fire Streak",
                                tint = Color.Black,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "$streak DAY STREAK",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                                Text(
                                    text = " 🔥",
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                            Text(
                                text = "FIRE STREAK ACTIVE",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF6B00)
                            )
                        }
                    }

                    Surface(
                        color = Color.White.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(100.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = "Level",
                                tint = Color(0xFFFF6B00),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "LEVEL $level",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // XP Progress Bar
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "DISCIPLINE XP",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.5f),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$xp / $nextLevelXp XP",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFFF6B00),
                            fontWeight = FontWeight.Black
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape),
                        color = Color(0xFFFF6B00),
                        trackColor = Color.White.copy(alpha = 0.1f)
                    )
                }
            }
        }
    }
}

@Composable
fun DateSelectorRow(
    selectedDate: String,
    onDateSelect: (String) -> Unit
) {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
    val numFormat = SimpleDateFormat("dd", Locale.getDefault())

    val dates = remember {
        val list = mutableListOf<String>()
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -3)
        for (i in 0..7) {
            list.add(sdf.format(cal.time))
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        list
    }

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(dates) { dateStr ->
            val dateObj = remember(dateStr) { sdf.parse(dateStr) ?: Date() }
            val dayName = remember(dateObj) { dayFormat.format(dateObj).uppercase() }
            val dayNum = remember(dateObj) { numFormat.format(dateObj) }
            val isSelected = dateStr == selectedDate

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (isSelected) Color(0xFFFF6B00) else Color(0xFF161616),
                border = BorderStroke(1.dp, if (isSelected) Color(0xFFFF6B00) else Color.White.copy(alpha = 0.08f)),
                modifier = Modifier
                    .width(62.dp)
                    .clickable { onDateSelect(dateStr) }
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = dayName,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) Color.Black else Color.White.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = dayNum,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (isSelected) Color.Black else Color.White,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

@Composable
fun FilterPillRow(
    selectedType: TaskType?,
    onSelect: (TaskType?) -> Unit
) {
    val filters = listOf(
        null to "All Missions",
        TaskType.MAIN_SIDE to "Main & Side",
        TaskType.BOXING to "Boxing Timer",
        TaskType.GYM to "Gym & Weights",
        TaskType.NORMAL to "Normal Tasks"
    )

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(filters) { (type, label) ->
            val isSelected = selectedType == type
            FilterChip(
                selected = isSelected,
                onClick = { onSelect(type) },
                shape = RoundedCornerShape(100.dp),
                label = {
                    Text(
                        text = label,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFFFF6B00),
                    selectedLabelColor = Color.Black,
                    containerColor = Color(0xFF161616),
                    labelColor = Color.White.copy(alpha = 0.6f)
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = Color.White.copy(alpha = 0.08f),
                    selectedBorderColor = Color(0xFFFF6B00)
                )
            )
        }
    }
}

@Composable
fun TaskExecutionCard(
    task: ScheduledTask,
    onCompleteToggle: () -> Unit,
    onStartBoxing: () -> Unit,
    onUpdateSideMission: (String, Boolean) -> Unit,
    onDelete: () -> Unit
) {
    val activeBorder = if (!task.isCompleted) {
        BorderStroke(1.5.dp, Color(0xFFFF6B00).copy(alpha = 0.8f))
    } else {
        BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    }

    val cardBg = if (!task.isCompleted) Color(0xFF161616) else Color(0xFF0F0F0F)

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = cardBg,
        border = activeBorder,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Task Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Type Badge
                    val (badgeIcon, badgeColor) = when (task.type) {
                        TaskType.MAIN_SIDE -> Icons.Default.DirectionsRun to Color(0xFFFF6B00)
                        TaskType.BOXING -> Icons.Default.SportsMma to Color(0xFFFF914D)
                        TaskType.GYM -> Icons.Default.FitnessCenter to Color(0xFF22C55E)
                        TaskType.NORMAL -> Icons.Default.TaskAlt to Color(0xFF3B82F6)
                    }
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(40.dp)
                            .background(badgeColor.copy(alpha = 0.15f), CircleShape)
                            .border(1.dp, badgeColor.copy(alpha = 0.3f), CircleShape)
                    ) {
                        Icon(
                            imageVector = badgeIcon,
                            contentDescription = task.type.name,
                            tint = badgeColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = task.title.uppercase(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = if (task.isCompleted) Color.White.copy(alpha = 0.4f) else Color.White,
                            textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "${task.timeString} • ${task.category.uppercase()} • SWIPE TO COMPLETE",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (!task.isCompleted) Color(0xFFFF6B00).copy(alpha = 0.8f) else Color.White.copy(alpha = 0.5f),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete Task",
                            tint = Color.White.copy(alpha = 0.4f)
                        )
                    }
                    Checkbox(
                        checked = task.isCompleted,
                        onCheckedChange = { onCompleteToggle() },
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color(0xFFFF6B00),
                            uncheckedColor = Color.White.copy(alpha = 0.3f),
                            checkmarkColor = Color.Black
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Specific Card Body based on Task Type
            when (task.type) {
                TaskType.MAIN_SIDE -> {
                    val sideStatus = remember(task.sideMissionsStatusRaw) {
                        AppViewModel.parseSideMissionsStatus(task.sideMissionsStatusRaw)
                    }
                    if (sideStatus.isNotEmpty()) {
                        Divider(color = Color.White.copy(alpha = 0.08f))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "SIDE MISSIONS",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFFF6B00),
                            fontWeight = FontWeight.Bold
                        )
                        sideStatus.forEach { (mission, isChecked) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onUpdateSideMission(mission, !isChecked) }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = { onUpdateSideMission(mission, it) },
                                    modifier = Modifier.size(32.dp),
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = Color(0xFFFF6B00),
                                        checkmarkColor = Color.Black
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = mission,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (isChecked) Color.White.copy(alpha = 0.4f) else Color.White,
                                    textDecoration = if (isChecked) TextDecoration.LineThrough else TextDecoration.None
                                )
                            }
                        }
                    }
                }

                TaskType.BOXING -> {
                    Button(
                        onClick = onStartBoxing,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF6B00)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = "Start Boxing Chronograph",
                            tint = Color.Black
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "START BOXING CHRONOGRAPH",
                            fontWeight = FontWeight.Black,
                            color = Color.Black
                        )
                    }
                }

                TaskType.GYM -> {
                    val exercises = remember(task) {
                        AppViewModel.parseGymExercises(task.title)
                    }
                    Divider(color = Color.White.copy(alpha = 0.08f))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "EXERCISE SETS LOG",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFFF6B00),
                        fontWeight = FontWeight.Bold
                    )
                    exercises.forEach { ex ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = ex.exerciseName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Surface(
                                color = Color.White.copy(alpha = 0.08f),
                                shape = RoundedCornerShape(100.dp)
                            ) {
                                Text(
                                    text = "${ex.targetSets} sets x ${ex.targetReps} reps @ ${ex.targetWeightKg}kg",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFFFF6B00),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                TaskType.NORMAL -> {
                    // Normal task description if available
                }
            }
        }
    }
}

@Composable
fun EmptyExecutionCard() {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFF121212),
        border = BorderStroke(1.dp, Color(0xFFFF6B00).copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(32.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(64.dp)
                    .background(Color(0xFFFF6B00).copy(alpha = 0.15f), CircleShape)
                    .border(1.dp, Color(0xFFFF6B00), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.LocalFireDepartment,
                    contentDescription = "Flame",
                    tint = Color(0xFFFF6B00),
                    modifier = Modifier.size(36.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "NO PROTOCOL SCHEDULED TODAY",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = Color.White,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "\"A goal without a plan is just a wish. Plan your protocol now.\"",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFF6B00)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Go to 'TEMPLATES' or 'PLANNER' tab to assign daily missions.",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
        }
    }
}

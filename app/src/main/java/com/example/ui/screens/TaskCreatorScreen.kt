package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BoxingMovement
import com.example.data.ExerciseLibrary
import com.example.data.GymExercise
import com.example.data.TaskTemplate
import com.example.data.TaskType
import com.example.ui.AppViewModel
import com.example.ui.components.HapticFeedbackHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import com.example.ui.dialogs.ImportRoutineDialog
import com.example.ui.dialogs.QrShareDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskCreatorScreen(
    viewModel: AppViewModel
) {
    val context = LocalContext.current
    val templates by viewModel.templates.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var qrShareTemplate by remember { mutableStateOf<TaskTemplate?>(null) }

    // Quick Schedule Dialog state
    var selectedTemplateForScheduling by remember { mutableStateOf<TaskTemplate?>(null) }

    selectedTemplateForScheduling?.let { template ->
        ScheduleTemplateDialog(
            template = template,
            onSchedule = { dateStr, timeStr ->
                viewModel.scheduleTask(template, dateStr, timeStr)
                selectedTemplateForScheduling = null
                HapticFeedbackHelper.triggerVibration(context)
            },
            onDismiss = { selectedTemplateForScheduling = null }
        )
    }

    if (showImportDialog) {
        ImportRoutineDialog(
            onImportLink = { link ->
                viewModel.importTemplateFromDeepLink(link)
                HapticFeedbackHelper.triggerVibration(context)
            },
            onDismiss = { showImportDialog = false }
        )
    }

    qrShareTemplate?.let { template ->
        val shareLink = remember(template) { viewModel.encodeTemplateToDeepLink(template) }
        QrShareDialog(
            template = template,
            shareLinkString = shareLink,
            onDismiss = { qrShareTemplate = null }
        )
    }

    if (showCreateDialog) {
        NewTaskTemplateDialog(
            onCreate = { title, type, category, estMin, goal, sides, boxDuration, boxMovements, muscle, gymEx, note ->
                viewModel.createTemplate(
                    title = title,
                    type = type,
                    category = category,
                    estimatedMinutes = estMin,
                    mainMissionGoal = goal,
                    sideMissions = sides,
                    boxingDurationSec = boxDuration,
                    boxingMovements = boxMovements,
                    gymMuscle = muscle,
                    gymExercises = gymEx,
                    normalNote = note
                )
                showCreateDialog = false
                HapticFeedbackHelper.triggerVibration(context)
            },
            onDismiss = { showCreateDialog = false }
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
                        Text(
                            text = "TEMPLATES",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFFF6B00),
                            letterSpacing = 1.sp
                        )

                        OutlinedButton(
                            onClick = { showImportDialog = true },
                            shape = RoundedCornerShape(100.dp),
                            border = BorderStroke(1.dp, Color(0xFFFF6B00).copy(alpha = 0.4f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF6B00))
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = "Import",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "IMPORT QR",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showCreateDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = "Create Template") },
                text = { Text("NEW TASK TEMPLATE", fontWeight = FontWeight.Bold) },
                containerColor = Color(0xFFFF6B00),
                contentColor = Color.Black
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
            item {
                Text(
                    text = "ESTABLISH THEN EXECUTE",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = "Create custom templates or scan QR codes / deep links to import routines into your library.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.LightGray
                )
            }

            // Quick Preset Templates Horizontal Carousel
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⚡ QUICK PRESET ROUTINES",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFFF6B00)
                        )
                        Text(
                            text = "1-TAP INSERT",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }

                    androidx.compose.foundation.lazy.LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(ExerciseLibrary.quickPresets) { preset ->
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = Color(0xFF1E293B),
                                border = BorderStroke(1.dp, Color(0xFFFF6B00).copy(alpha = 0.3f)),
                                modifier = Modifier
                                    .width(220.dp)
                                    .clickable {
                                        viewModel.savePresetTemplate(preset)
                                        HapticFeedbackHelper.triggerDoublePulse(context)
                                    }
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp)
                                ) {
                                    Text(
                                        text = preset.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${preset.category} • ${preset.estimatedMinutes}m",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.Gray
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Surface(
                                        color = Color(0xFFFF6B00).copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(100.dp)
                                    ) {
                                        Text(
                                            text = "+ ADD TO LIBRARY",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Black,
                                            color = Color(0xFFFF6B00),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            items(templates, key = { it.id }) { template ->
                TemplateCard(
                    template = template,
                    onScheduleClick = { selectedTemplateForScheduling = template },
                    onShareQrClick = { qrShareTemplate = template },
                    onDelete = { viewModel.deleteTemplate(template.id) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun TemplateCard(
    template: TaskTemplate,
    onScheduleClick: () -> Unit,
    onShareQrClick: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFF161616),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val (icon, color) = when (template.type) {
                        TaskType.MAIN_SIDE -> Icons.Default.DirectionsRun to Color(0xFFFF6B00)
                        TaskType.BOXING -> Icons.Default.SportsMma to Color(0xFFFF914D)
                        TaskType.GYM -> Icons.Default.FitnessCenter to Color(0xFF22C55E)
                        TaskType.NORMAL -> Icons.Default.TaskAlt to Color(0xFF3B82F6)
                    }
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(40.dp)
                            .background(color.copy(alpha = 0.15f), CircleShape)
                            .border(1.dp, color.copy(alpha = 0.3f), CircleShape)
                    ) {
                        Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = template.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "${template.type.name.replace('_', ' ')} • ~${template.estimatedMinutes} mins",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.5f)
                        )
                    }
                }

                Row {
                    IconButton(onClick = onShareQrClick) {
                        Icon(imageVector = Icons.Default.QrCode2, contentDescription = "Share QR", tint = Color(0xFFFF6B00))
                    }
                    IconButton(onClick = onDelete) {
                        Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = "Delete", tint = Color.White.copy(alpha = 0.4f))
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action: Schedule to Monthly Planner
            Button(
                onClick = onScheduleClick,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.08f)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = "Schedule",
                    tint = Color(0xFFFF6B00),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "ADD TO PLANNER",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewTaskTemplateDialog(
    onCreate: (
        title: String,
        type: TaskType,
        category: String,
        estMin: Int,
        goal: String,
        sides: List<String>,
        boxingDuration: Int,
        boxingMovements: List<BoxingMovement>,
        gymMuscle: String,
        gymExercises: List<GymExercise>,
        note: String
    ) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedType by remember { mutableStateOf(TaskType.MAIN_SIDE) }
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Fitness") }
    var estMinutes by remember { mutableStateOf("30") }

    // Type 1 fields
    var mainGoal by remember { mutableStateOf("") }
    var sideInput by remember { mutableStateOf("") }
    val sideMissionsList = remember { mutableStateListOf<String>() }

    // Type 2 fields
    var movementNameInput by remember { mutableStateOf("") }
    var movementDurationInput by remember { mutableStateOf("30") }
    val boxingMovementsList = remember { mutableStateListOf<BoxingMovement>() }

    // Type 3 fields
    var gymMuscleInput by remember { mutableStateOf("Full Body") }
    var exerciseNameInput by remember { mutableStateOf("") }
    var exerciseSetsInput by remember { mutableStateOf("4") }
    var exerciseRepsInput by remember { mutableStateOf("10") }
    var exerciseWeightInput by remember { mutableStateOf("60") }
    val gymExercisesList = remember { mutableStateListOf<GymExercise>() }

    // Type 4 fields
    var normalNoteInput by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onCreate(
                            title,
                            selectedType,
                            category,
                            estMinutes.toIntOrNull() ?: 30,
                            mainGoal,
                            sideMissionsList,
                            1200,
                            boxingMovementsList,
                            gymMuscleInput,
                            gymExercisesList,
                            normalNoteInput
                        )
                    }
                },
                enabled = title.isNotBlank()
            ) {
                Text("SAVE TEMPLATE", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = Color.Gray)
            }
        },
        title = {
            Text("CREATE TASK TEMPLATE", fontWeight = FontWeight.Black)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 450.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Type Selector Tabs
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = selectedType == TaskType.MAIN_SIDE,
                        onClick = { selectedType = TaskType.MAIN_SIDE },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 4)
                    ) { Text("Main", fontSize = 11.sp) }
                    SegmentedButton(
                        selected = selectedType == TaskType.BOXING,
                        onClick = { selectedType = TaskType.BOXING },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 4)
                    ) { Text("Boxing", fontSize = 11.sp) }
                    SegmentedButton(
                        selected = selectedType == TaskType.GYM,
                        onClick = { selectedType = TaskType.GYM },
                        shape = SegmentedButtonDefaults.itemShape(index = 2, count = 4)
                    ) { Text("Gym", fontSize = 11.sp) }
                    SegmentedButton(
                        selected = selectedType == TaskType.NORMAL,
                        onClick = { selectedType = TaskType.NORMAL },
                        shape = SegmentedButtonDefaults.itemShape(index = 3, count = 4)
                    ) { Text("Normal", fontSize = 11.sp) }
                }

                var showAutoCompleteDropdown by remember { mutableStateOf(false) }
                val suggestions = remember(title) { ExerciseLibrary.search(title) }

                Column {
                    OutlinedTextField(
                        value = title,
                        onValueChange = {
                            title = it
                            showAutoCompleteDropdown = it.isNotBlank()
                        },
                        label = { Text("Task Title (e.g., Barbell Squat, 5km Run)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (showAutoCompleteDropdown && suggestions.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF1E293B),
                            border = BorderStroke(1.dp, Color(0xFFFF6B00).copy(alpha = 0.4f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 140.dp)
                                .padding(top = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(6.dp)) {
                                Text("💡 AUTO-COMPLETE SUGGESTIONS", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color(0xFFFF6B00), modifier = Modifier.padding(4.dp))
                                suggestions.take(3).forEach { exercise ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                title = exercise.name
                                                category = exercise.category
                                                selectedType = exercise.type
                                                estMinutes = exercise.defaultMinutes.toString()
                                                mainGoal = exercise.suggestedGoal
                                                if (exercise.suggestedSideMissions.isNotBlank()) {
                                                    sideMissionsList.clear()
                                                    sideMissionsList.addAll(exercise.suggestedSideMissions.split(",").map { it.trim() })
                                                }
                                                showAutoCompleteDropdown = false
                                            }
                                            .padding(vertical = 6.dp, horizontal = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(exercise.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Text("${exercise.category} • ${exercise.defaultMinutes}m", color = Color.Gray, fontSize = 10.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Category") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = estMinutes,
                        onValueChange = { estMinutes = it },
                        label = { Text("Duration (mins)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                Divider(color = Color(0xFF27272A))

                // Dynamic Form Based on Type
                when (selectedType) {
                    TaskType.MAIN_SIDE -> {
                        OutlinedTextField(
                            value = mainGoal,
                            onValueChange = { mainGoal = it },
                            label = { Text("Main Mission Goal (e.g., Run 10KM)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = sideInput,
                                onValueChange = { sideInput = it },
                                label = { Text("Add Side Mission") },
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = {
                                if (sideInput.isNotBlank()) {
                                    sideMissionsList.add(sideInput.trim())
                                    sideInput = ""
                                }
                            }) {
                                Icon(Icons.Default.AddCircle, contentDescription = "Add")
                            }
                        }
                        sideMissionsList.forEach { side ->
                            Text("• $side", style = MaterialTheme.typography.bodySmall, color = Color.LightGray)
                        }
                    }

                    TaskType.BOXING -> {
                        Text("Add Boxing Punch Combinations:", style = MaterialTheme.typography.labelSmall)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = movementNameInput,
                                onValueChange = { movementNameInput = it },
                                label = { Text("Movement (e.g. Jab-Cross)") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = movementDurationInput,
                                onValueChange = { movementDurationInput = it },
                                label = { Text("Sec") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.width(70.dp)
                            )
                            IconButton(onClick = {
                                if (movementNameInput.isNotBlank()) {
                                    boxingMovementsList.add(
                                        BoxingMovement(
                                            movementNameInput.trim(),
                                            movementDurationInput.toIntOrNull() ?: 30
                                        )
                                    )
                                    movementNameInput = ""
                                }
                            }) {
                                Icon(Icons.Default.AddCircle, contentDescription = "Add")
                            }
                        }
                        boxingMovementsList.forEach { m ->
                            Text("• ${m.name} (${m.durationSeconds}s)", style = MaterialTheme.typography.bodySmall, color = Color.LightGray)
                        }
                    }

                    TaskType.GYM -> {
                        OutlinedTextField(
                            value = gymMuscleInput,
                            onValueChange = { gymMuscleInput = it },
                            label = { Text("Target Muscle Group") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = exerciseNameInput,
                                onValueChange = { exerciseNameInput = it },
                                label = { Text("Exercise") },
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = {
                                if (exerciseNameInput.isNotBlank()) {
                                    gymExercisesList.add(
                                        GymExercise(
                                            exerciseNameInput.trim(),
                                            exerciseSetsInput.toIntOrNull() ?: 4,
                                            exerciseRepsInput.toIntOrNull() ?: 10,
                                            exerciseWeightInput.toFloatOrNull() ?: 60f
                                        )
                                    )
                                    exerciseNameInput = ""
                                }
                            }) {
                                Icon(Icons.Default.AddCircle, contentDescription = "Add")
                            }
                        }
                        gymExercisesList.forEach { ex ->
                            Text("• ${ex.exerciseName} (${ex.targetSets}x${ex.targetReps} @ ${ex.targetWeightKg}kg)", style = MaterialTheme.typography.bodySmall, color = Color.LightGray)
                        }
                    }

                    TaskType.NORMAL -> {
                        OutlinedTextField(
                            value = normalNoteInput,
                            onValueChange = { normalNoteInput = it },
                            label = { Text("Task Description / Checklist Note") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    )
}

@Composable
fun ScheduleTemplateDialog(
    template: TaskTemplate,
    onSchedule: (dateStr: String, timeStr: String) -> Unit,
    onDismiss: () -> Unit
) {
    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    var dateString by remember { mutableStateOf(today) }
    var timeString by remember { mutableStateOf("07:00") }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = { onSchedule(dateString, timeString) }
            ) {
                Text("ASSIGN TO SCHEDULE", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = Color.Gray)
            }
        },
        title = { Text("SCHEDULE MISSION", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Mission: ${template.title}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                OutlinedTextField(
                    value = dateString,
                    onValueChange = { dateString = it },
                    label = { Text("Date (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = timeString,
                    onValueChange = { timeString = it },
                    label = { Text("Time (HH:mm)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    )
}

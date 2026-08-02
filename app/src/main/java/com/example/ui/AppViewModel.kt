package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

import com.example.ui.components.SyncState
import java.net.URLDecoder
import java.net.URLEncoder

data class RewardEvent(
    val xpEarned: Int,
    val newLevel: Int,
    val streak: Int,
    val newBadges: List<String>
)

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val repository = Repository(db.taskDao())

    private val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    private val monthStr = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())

    private val _selectedDate = MutableStateFlow(todayStr)
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    private val _selectedMonth = MutableStateFlow(monthStr)
    val selectedMonth: StateFlow<String> = _selectedMonth.asStateFlow()

    private val _rewardEvent = MutableStateFlow<RewardEvent?>(null)
    val rewardEvent: StateFlow<RewardEvent?> = _rewardEvent.asStateFlow()

    private val _syncState = MutableStateFlow(SyncState.SYNCED)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    private val _partnerNudgeAlert = MutableStateFlow<String?>(null)
    val partnerNudgeAlert: StateFlow<String?> = _partnerNudgeAlert.asStateFlow()

    private val _showHealthSyncDialog = MutableStateFlow(false)
    val showHealthSyncDialog: StateFlow<Boolean> = _showHealthSyncDialog.asStateFlow()

    private val _showDeloadDialog = MutableStateFlow(false)
    val showDeloadDialog: StateFlow<Boolean> = _showDeloadDialog.asStateFlow()

    private val firebaseManager = FirebaseManager(application)
    val firebaseUser: StateFlow<com.google.firebase.auth.FirebaseUser?> = firebaseManager.currentUserFlow

    val templates: StateFlow<List<TaskTemplate>> = repository.allTemplates
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userProfile: StateFlow<UserProfile> = repository.userProfile
        .map { it ?: UserProfile() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserProfile())

    val allProofPosts: StateFlow<List<ProofPost>> = repository.allProofPosts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedDateTasks: StateFlow<List<ScheduledTask>> = _selectedDate
        .flatMapLatest { date -> repository.getTasksForDate(date) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedMonthTasks: StateFlow<List<ScheduledTask>> = _selectedMonth
        .flatMapLatest { month -> repository.getTasksForMonth(month) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val monthlyReports: StateFlow<List<MonthlyReport>> = repository.monthlyReports
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allWeightLogs: StateFlow<List<WeightLog>> = repository.allWeightLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayCaloriesBurned: StateFlow<Int> = combine(selectedDateTasks, userProfile) { tasks, profile ->
        tasks.filter { it.isCompleted }.sumOf { task ->
            val met = ExerciseLibrary.getMetForTask(task.type, task.category)
            val duration = if (task.actualDurationMinutes > 0) task.actualDurationMinutes else 30
            ExerciseLibrary.calculateCalories(duration, profile.bodyWeightKg, met)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val monthlyCaloriesBurned: StateFlow<Int> = combine(selectedMonthTasks, userProfile) { tasks, profile ->
        tasks.filter { it.isCompleted }.sumOf { task ->
            val met = ExerciseLibrary.getMetForTask(task.type, task.category)
            val duration = if (task.actualDurationMinutes > 0) task.actualDurationMinutes else 30
            ExerciseLibrary.calculateCalories(duration, profile.bodyWeightKg, met)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    init {
        viewModelScope.launch {
            // Check if DB needs initialization
            repository.allTemplates.firstOrNull()?.let { list ->
                if (list.isEmpty()) {
                    repository.initializeDefaultDataIfEmpty()
                }
            } ?: run {
                repository.initializeDefaultDataIfEmpty()
            }
            
            // Check for smart deload mode trigger (if past 2 days had 0 completed tasks)
            checkBurnoutDeloadCondition()
        }
    }

    fun triggerCloudSync() {
        viewModelScope.launch {
            _syncState.value = SyncState.SYNCING
            kotlinx.coroutines.delay(1200) // Simulate background sync to Firebase/Supabase
            repository.updateLastCloudSyncTimestamp(System.currentTimeMillis())
            _syncState.value = SyncState.SYNCED
        }
    }

    fun openHealthSyncDialog() {
        _showHealthSyncDialog.value = true
    }

    fun dismissHealthSyncDialog() {
        _showHealthSyncDialog.value = false
    }

    fun confirmHealthSyncAutoComplete() {
        viewModelScope.launch {
            val tasksToday = selectedDateTasks.value
            val uncompletedCardio = tasksToday.filter { !it.isCompleted && (it.type == TaskType.MAIN_SIDE || it.type == TaskType.BOXING) }
            uncompletedCardio.forEach { task ->
                completeTask(task)
            }
            _showHealthSyncDialog.value = false
            triggerCloudSync()
        }
    }

    private suspend fun checkBurnoutDeloadCondition() {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -1)
        val yesterday = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
        cal.add(Calendar.DAY_OF_YEAR, -1)
        val dayBeforeYesterday = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)

        val tasksYest = repository.getTasksForDate(yesterday).firstOrNull() ?: emptyList()
        val tasksDayBefore = repository.getTasksForDate(dayBeforeYesterday).firstOrNull() ?: emptyList()

        if (tasksYest.isNotEmpty() && tasksYest.none { it.isCompleted } &&
            tasksDayBefore.isNotEmpty() && tasksDayBefore.none { it.isCompleted }) {
            // Missing all tasks 2 days in a row triggers deload recommendation
            _showDeloadDialog.value = true
            repository.updateDeloadMode(true)
        }
    }

    fun openDeloadDialog() {
        _showDeloadDialog.value = true
    }

    fun dismissDeloadDialog() {
        _showDeloadDialog.value = false
    }

    fun toggleDeloadMode(active: Boolean) {
        viewModelScope.launch {
            repository.updateDeloadMode(active)
        }
    }

    fun updatePartnerInfo(name: String, code: String) {
        viewModelScope.launch {
            repository.updatePartnerInfo(name, code)
        }
    }

    fun sendPartnerMotivationNudge() {
        viewModelScope.launch {
            val pName = userProfile.value.partnerName
            _partnerNudgeAlert.value = "$pName sent: 'NO EXCUSES TODAY! Get up and hit your heavy bag session right now! 🔥'"
        }
    }

    fun dismissPartnerNudge() {
        _partnerNudgeAlert.value = null
    }

    fun encodeTemplateToDeepLink(template: TaskTemplate): String {
        val encodedTitle = URLEncoder.encode(template.title, "UTF-8")
        val encodedCat = URLEncoder.encode(template.category, "UTF-8")
        val encodedType = template.type.name
        val encodedMins = template.estimatedMinutes
        val encodedGoal = URLEncoder.encode(template.mainMissionGoal, "UTF-8")
        val encodedSides = URLEncoder.encode(template.sideMissionsRaw, "UTF-8")

        return "beitnow://routine?title=$encodedTitle&type=$encodedType&cat=$encodedCat&mins=$encodedMins&goal=$encodedGoal&sides=$encodedSides"
    }

    fun importTemplateFromDeepLink(link: String) {
        viewModelScope.launch {
            try {
                val cleaned = if (link.contains("?")) link.substringAfter("?") else link
                val params = cleaned.split("&").associate {
                    val kv = it.split("=")
                    if (kv.size >= 2) kv[0] to URLDecoder.decode(kv[1], "UTF-8") else kv[0] to ""
                }

                val title = params["title"] ?: "Imported Routine"
                val typeStr = params["type"] ?: "MAIN_SIDE"
                val type = try { TaskType.valueOf(typeStr) } catch (e: Exception) { TaskType.MAIN_SIDE }
                val cat = params["cat"] ?: "Fitness"
                val mins = params["mins"]?.toIntOrNull() ?: 30
                val goal = params["goal"] ?: ""
                val sides = params["sides"] ?: ""

                val template = TaskTemplate(
                    title = title,
                    type = type,
                    category = cat,
                    estimatedMinutes = mins,
                    mainMissionGoal = goal,
                    sideMissionsRaw = sides
                )
                repository.saveTemplate(template)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun setSelectedDate(date: String) {
        _selectedDate.value = date
        _selectedMonth.value = if (date.length >= 7) date.substring(0, 7) else monthStr
    }

    fun setSelectedMonth(month: String) {
        _selectedMonth.value = month
    }

    fun createTemplate(
        title: String,
        type: TaskType,
        category: String,
        estimatedMinutes: Int,
        mainMissionGoal: String = "",
        sideMissions: List<String> = emptyList(),
        boxingDurationSec: Int = 1200,
        boxingMovements: List<BoxingMovement> = emptyList(),
        gymMuscle: String = "",
        gymExercises: List<GymExercise> = emptyList(),
        normalNote: String = ""
    ) {
        viewModelScope.launch {
            val sideMissionsRaw = sideMissions.joinToString(",")
            val boxingMovementsRaw = boxingMovements.joinToString(",") { "${it.name}:${it.durationSeconds}" }
            val gymExercisesRaw = gymExercises.joinToString(",") { "${it.exerciseName}|${it.targetSets}|${it.targetReps}|${it.targetWeightKg}" }

            val template = TaskTemplate(
                title = title,
                type = type,
                category = category,
                estimatedMinutes = estimatedMinutes,
                mainMissionGoal = mainMissionGoal,
                sideMissionsRaw = sideMissionsRaw,
                boxingTotalDurationSec = boxingDurationSec,
                boxingMovementsRaw = boxingMovementsRaw,
                gymTargetMuscle = gymMuscle,
                gymExercisesRaw = gymExercisesRaw,
                normalTaskNote = normalNote
            )

            repository.saveTemplate(template)
        }
    }

    fun deleteTemplate(id: Long) {
        viewModelScope.launch {
            repository.deleteTemplate(id)
        }
    }

    fun scheduleTask(template: TaskTemplate, dateString: String, timeString: String) {
        viewModelScope.launch {
            // Build initial side missions raw map status
            val sideMissionsMap = if (template.sideMissionsRaw.isNotBlank()) {
                template.sideMissionsRaw.split(",").joinToString(",") { "$it:false" }
            } else ""

            val scheduled = ScheduledTask(
                templateId = template.id,
                title = template.title,
                type = template.type,
                category = template.category,
                dateString = dateString,
                timeString = timeString,
                sideMissionsStatusRaw = sideMissionsMap
            )
            repository.saveScheduledTask(scheduled)
        }
    }

    fun completeTask(task: ScheduledTask) {
        viewModelScope.launch {
            val profile = userProfile.value
            val (updatedProfile, badges) = repository.completeTask(task, profile)
            
            _rewardEvent.value = RewardEvent(
                xpEarned = when (task.type) {
                    TaskType.MAIN_SIDE -> 100
                    TaskType.BOXING -> 120
                    TaskType.GYM -> 110
                    TaskType.NORMAL -> 50
                },
                newLevel = updatedProfile.level,
                streak = updatedProfile.currentStreak,
                newBadges = badges
            )
        }
    }

    fun uncompleteTask(task: ScheduledTask) {
        viewModelScope.launch {
            repository.uncompleteTask(task)
        }
    }

    fun deleteScheduledTask(id: Long) {
        viewModelScope.launch {
            repository.deleteScheduledTask(id)
        }
    }

    fun updateSideMissionStatus(task: ScheduledTask, missionTitle: String, isCompleted: Boolean) {
        viewModelScope.launch {
            val statusMap = parseSideMissionsStatus(task.sideMissionsStatusRaw).toMutableMap()
            statusMap[missionTitle] = isCompleted
            val updatedRaw = statusMap.entries.joinToString(",") { "${it.key}:${it.value}" }
            
            val updatedTask = task.copy(sideMissionsStatusRaw = updatedRaw)
            repository.saveScheduledTask(updatedTask)
        }
    }

    fun evaluateMonthlyJudgmentDay() {
        viewModelScope.launch {
            val monthKey = selectedMonth.value
            val tasks = repository.getTasksForMonth(monthKey).first()
            repository.evaluateJudgmentDay(monthKey, tasks)
        }
    }

    fun logBodyWeight(weightKg: Float, notes: String = "") {
        viewModelScope.launch {
            repository.addWeightLog(selectedDate.value, weightKg, notes)
        }
    }

    fun duplicateWeekAcrossMonth() {
        viewModelScope.launch {
            repository.duplicateFirstWeekToFullMonth(selectedMonth.value)
        }
    }

    fun savePresetTemplate(template: TaskTemplate) {
        viewModelScope.launch {
            repository.saveTemplate(template)
        }
    }

    fun dismissRewardDialog() {
        _rewardEvent.value = null
    }

    // Utility parsers
    companion object {
        fun parseSideMissionsStatus(raw: String): Map<String, Boolean> {
            if (raw.isBlank()) return emptyMap()
            return raw.split(",").mapNotNull {
                val parts = it.split(":")
                if (parts.size >= 2) parts[0] to parts[1].toBoolean() else null
            }.toMap()
        }

        fun parseBoxingMovements(raw: String): List<BoxingMovement> {
            if (raw.isBlank()) return listOf(
                BoxingMovement("Jab-Cross-Hook", 30),
                BoxingMovement("Duck & Counter", 30),
                BoxingMovement("Power Shots", 45),
                BoxingMovement("Shadowboxing Rest", 15)
            )
            return raw.split(",").mapNotNull {
                val parts = it.split(":")
                if (parts.size >= 2) {
                    BoxingMovement(parts[0], parts[1].toIntOrNull() ?: 30)
                } else null
            }
        }

        fun parseGymExercises(raw: String): List<GymExercise> {
            if (raw.isBlank()) return listOf(
                GymExercise("Bench Press", 4, 10, 80f),
                GymExercise("Incline Dumbbell Press", 3, 12, 28f)
            )
            return raw.split(",").mapNotNull {
                val parts = it.split("|")
                if (parts.size >= 4) {
                    GymExercise(
                        parts[0],
                        parts[1].toIntOrNull() ?: 3,
                        parts[2].toIntOrNull() ?: 10,
                        parts[3].toFloatOrNull() ?: 20f
                    )
                } else null
            }
        }
    }

    fun signInWithGoogleAccount(name: String, email: String, photoUrl: String) {
        viewModelScope.launch {
            val user = firebaseManager.signInAnonymously()
            val uid = user?.uid ?: "google_user_${System.currentTimeMillis()}"
            repository.updateAccountInfo(uid, name, email, photoUrl)
            val profile = userProfile.value.copy(uid = uid, displayName = name, email = email, photoUrl = photoUrl)
            firebaseManager.saveProfileToFirestore(profile)
            triggerCloudSync()
        }
    }

    fun signOutAccount() {
        viewModelScope.launch {
            firebaseManager.signOut()
            repository.updateAccountInfo("guest_warrior", "WARRIOR", "warrior@beitnow.app", "")
        }
    }

    fun addProofPost(
        workoutTitle: String,
        workoutCategory: String,
        statsText: String,
        photoUri: String,
        caption: String = "Proof of Work logged.",
        isBeforeAfterPair: Boolean = false,
        beforePhotoUri: String = "",
        afterPhotoUri: String = ""
    ) {
        viewModelScope.launch {
            val profile = userProfile.value
            val post = ProofPost(
                userId = profile.uid,
                userName = profile.displayName,
                userPhotoUrl = profile.photoUrl,
                workoutTitle = workoutTitle,
                workoutCategory = workoutCategory,
                statsText = statsText,
                photoUri = photoUri,
                dateString = todayStr,
                caption = caption,
                isBeforeAfterPair = isBeforeAfterPair,
                beforePhotoUri = beforePhotoUri,
                afterPhotoUri = afterPhotoUri
            )
            repository.saveProofPost(post)
            firebaseManager.uploadProofPost(post)
            triggerCloudSync()
        }
    }

    fun deleteProofPost(id: String) {
        viewModelScope.launch {
            repository.deleteProofPost(id)
        }
    }
}

package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class Repository(private val taskDao: TaskDao) {

    val allTemplates: Flow<List<TaskTemplate>> = taskDao.getAllTemplates()
    val userProfile: Flow<UserProfile?> = taskDao.getUserProfile()
    val monthlyReports: Flow<List<MonthlyReport>> = taskDao.getAllMonthlyReports()
    val allWeightLogs: Flow<List<WeightLog>> = taskDao.getAllWeightLogs()
    val allProofPosts: Flow<List<ProofPost>> = taskDao.getAllProofPosts()

    suspend fun saveProofPost(post: ProofPost) = taskDao.insertProofPost(post)

    suspend fun deleteProofPost(id: String) = taskDao.deleteProofPost(id)

    suspend fun updateAccountInfo(uid: String, name: String, email: String, photoUrl: String) {
        val current = userProfile.firstOrNull() ?: UserProfile()
        taskDao.saveUserProfile(
            current.copy(
                uid = uid,
                displayName = name,
                email = email,
                photoUrl = photoUrl
            )
        )
    }

    fun getTasksForDate(dateString: String): Flow<List<ScheduledTask>> =
        taskDao.getTasksForDate(dateString)

    fun getTasksForMonth(monthKey: String): Flow<List<ScheduledTask>> =
        taskDao.getTasksForMonth(monthKey)

    suspend fun saveTemplate(template: TaskTemplate): Long = taskDao.insertTemplate(template)

    suspend fun deleteTemplate(id: Long) = taskDao.deleteTemplate(id)

    suspend fun saveScheduledTask(task: ScheduledTask): Long = taskDao.insertScheduledTask(task)

    suspend fun deleteScheduledTask(id: Long) = taskDao.deleteScheduledTask(id)

    suspend fun completeTask(task: ScheduledTask, currentProfile: UserProfile): Pair<UserProfile, List<String>> {
        val updatedTask = task.copy(
            isCompleted = true,
            completedAtTimestamp = System.currentTimeMillis()
        )
        taskDao.updateScheduledTask(updatedTask)

        // Calculate XP reward based on task type
        val xpReward = when (task.type) {
            TaskType.MAIN_SIDE -> 100
            TaskType.BOXING -> 120
            TaskType.GYM -> 110
            TaskType.NORMAL -> 50
        }

        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val yesterdayCalendar = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        val yesterdayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(yesterdayCalendar.time)

        var newStreak = currentProfile.currentStreak
        if (currentProfile.lastCompletedDate != todayStr) {
            newStreak = if (currentProfile.lastCompletedDate == yesterdayStr || currentProfile.lastCompletedDate.isEmpty()) {
                currentProfile.currentStreak + 1
            } else {
                1
            }
        }

        val maxStreak = maxOf(newStreak, currentProfile.maxStreak)
        val newXp = currentProfile.xp + xpReward
        val newLevel = (newXp / 300) + 1

        // Check for newly unlocked badges
        val existingBadges = currentProfile.unlockedBadgesRaw.split(",").filter { it.isNotBlank() }.toMutableSet()
        val newlyUnlocked = mutableListOf<String>()

        if (newStreak >= 3 && !existingBadges.contains("STREAK_3")) {
            existingBadges.add("STREAK_3")
            newlyUnlocked.add("3-Day Discipline Spark 🔥")
        }
        if (newStreak >= 7 && !existingBadges.contains("STREAK_7")) {
            existingBadges.add("STREAK_7")
            newlyUnlocked.add("7-Day Fire Streak 💥")
        }
        if (task.type == TaskType.BOXING && !existingBadges.contains("BOXING_TITAN")) {
            existingBadges.add("BOXING_TITAN")
            newlyUnlocked.add("Boxing Titan 🥊")
        }
        if (task.type == TaskType.GYM && !existingBadges.contains("IRON_BEAST")) {
            existingBadges.add("IRON_BEAST")
            newlyUnlocked.add("Iron Beast 🏋️")
        }

        val updatedProfile = currentProfile.copy(
            xp = newXp,
            level = newLevel,
            currentStreak = newStreak,
            maxStreak = maxStreak,
            lastCompletedDate = todayStr,
            unlockedBadgesRaw = existingBadges.joinToString(",")
        )

        taskDao.saveUserProfile(updatedProfile)
        return Pair(updatedProfile, newlyUnlocked)
    }

    suspend fun saveUserProfile(profile: UserProfile) {
        taskDao.saveUserProfile(profile)
    }

    suspend fun uncompleteTask(task: ScheduledTask) {
        val updatedTask = task.copy(
            isCompleted = false,
            completedAtTimestamp = null
        )
        taskDao.updateScheduledTask(updatedTask)
    }

    suspend fun updateDeloadMode(isDeloadActive: Boolean) {
        val current = userProfile.firstOrNull() ?: UserProfile()
        taskDao.saveUserProfile(current.copy(isDeloadModeActive = isDeloadActive))
    }

    suspend fun updatePartnerInfo(name: String, code: String) {
        val current = userProfile.firstOrNull() ?: UserProfile()
        taskDao.saveUserProfile(current.copy(partnerName = name, partnerCode = code))
    }

    suspend fun updateLastCloudSyncTimestamp(timestamp: Long = System.currentTimeMillis()) {
        val current = userProfile.firstOrNull() ?: UserProfile()
        taskDao.saveUserProfile(current.copy(lastCloudSyncTimestamp = timestamp))
    }

    suspend fun updateBodyWeight(weightKg: Float) {
        val current = userProfile.firstOrNull() ?: UserProfile()
        taskDao.saveUserProfile(current.copy(bodyWeightKg = weightKg))
    }

    suspend fun addWeightLog(dateStr: String, weightKg: Float, notes: String = ""): Long {
        updateBodyWeight(weightKg)
        return taskDao.insertWeightLog(WeightLog(dateString = dateStr, weightKg = weightKg, notes = notes))
    }

    suspend fun duplicateFirstWeekToFullMonth(monthKey: String) {
        // monthKey is "YYYY-MM"
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cal = Calendar.getInstance()
        cal.time = SimpleDateFormat("yyyy-MM", Locale.getDefault()).parse(monthKey) ?: Date()
        cal.set(Calendar.DAY_OF_MONTH, 1)

        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val monthPrefix = monthKey

        // Get tasks for days 1 to 7
        val week1Tasks = mutableListOf<ScheduledTask>()
        for (day in 1..7) {
            val dateStr = String.format(Locale.getDefault(), "%s-%02d", monthPrefix, day)
            val dayTasks = getTasksForDate(dateStr).firstOrNull() ?: emptyList()
            week1Tasks.addAll(dayTasks)
        }

        if (week1Tasks.isEmpty()) return

        // Duplicate for days 8 to end of month
        for (day in 8..daysInMonth) {
            val dayOfWeekOffset = (day - 1) % 7 + 1 // 1..7 mapping to week1 day
            val sourceDateStr = String.format(Locale.getDefault(), "%s-%02d", monthPrefix, dayOfWeekOffset)
            val targetDateStr = String.format(Locale.getDefault(), "%s-%02d", monthPrefix, day)

            val sourceTasks = week1Tasks.filter { it.dateString == sourceDateStr }
            for (st in sourceTasks) {
                val copy = st.copy(
                    id = 0,
                    dateString = targetDateStr,
                    isCompleted = false,
                    completedAtTimestamp = null
                )
                taskDao.insertScheduledTask(copy)
            }
        }
    }

    suspend fun evaluateJudgmentDay(monthKey: String, tasks: List<ScheduledTask>): MonthlyReport {
        val total = tasks.size
        val completed = tasks.count { it.isCompleted }
        val percentage = if (total > 0) (completed.toFloat() / total.toFloat()) * 100f else 100f

        val grade = when {
            percentage >= 85f -> "A"
            percentage >= 65f -> "B"
            else -> "C"
        }

        val note = when (grade) {
            "A" -> "EXCELLENT DISCIPLINE! You executed your plans with fierce consistency. Keep pushing the limits!"
            "B" -> "GOOD EXECUTION. Solid performance, but room for greater mental toughness next month."
            else -> "WARNING: LOW CONSISTENCY. Eliminate excuses and execute your daily missions without delay!"
        }

        val report = MonthlyReport(
            monthYearKey = monthKey,
            grade = grade,
            completionPercentage = percentage,
            totalScheduled = total,
            totalCompleted = completed,
            evaluationNotes = note
        )

        taskDao.saveMonthlyReport(report)
        return report
    }

    suspend fun initializeDefaultDataIfEmpty() {
        // Ensure user profile exists
        val profile = UserProfile(id = 1, xp = 150, level = 1, currentStreak = 3, maxStreak = 5)
        taskDao.saveUserProfile(profile)

        // Seed sample templates if empty
        val sampleTemplates = listOf(
            TaskTemplate(
                title = "Morning 10KM & Bodyweight",
                type = TaskType.MAIN_SIDE,
                category = "Cardio & Fitness",
                estimatedMinutes = 60,
                mainMissionGoal = "Run 10 km under 50 mins",
                sideMissionsRaw = "100 Pushups,50 Bodyweight Squats,3 min Plank"
            ),
            TaskTemplate(
                title = "Heavy Heavyweight Boxing Drill",
                type = TaskType.BOXING,
                category = "Boxing",
                estimatedMinutes = 20,
                boxingTotalDurationSec = 1200,
                boxingMovementsRaw = "Jab-Cross Combo:45,Duck & Counter Hook:45,Heavy Bag Power Shots:60,Footwork Shadowboxing:30,Jump Rope Rest:30"
            ),
            TaskTemplate(
                title = "Chest & Triceps Hypertrophy",
                type = TaskType.GYM,
                category = "Gym & Weights",
                estimatedMinutes = 50,
                gymTargetMuscle = "Chest / Triceps",
                gymExercisesRaw = "Incline Barbell Bench Press|4|10|75,Dumbbell Flyes|3|12|20,Triceps Cable Pushdowns|4|15|35"
            ),
            TaskTemplate(
                title = "Deep Work & Mindset Reading",
                type = TaskType.NORMAL,
                category = "Productivity",
                estimatedMinutes = 30,
                normalTaskNote = "Read 20 pages of Stoic Philosophy or High Performance book."
            )
        )

        sampleTemplates.forEach { taskDao.insertTemplate(it) }

        // Seed today's tasks
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val defaultScheduled = listOf(
            ScheduledTask(
                templateId = 1,
                title = "Morning 10KM & Bodyweight",
                type = TaskType.MAIN_SIDE,
                category = "Cardio & Fitness",
                dateString = todayStr,
                timeString = "07:00",
                sideMissionsStatusRaw = "100 Pushups:false,50 Bodyweight Squats:false,3 min Plank:false"
            ),
            ScheduledTask(
                templateId = 2,
                title = "Heavy Heavyweight Boxing Drill",
                type = TaskType.BOXING,
                category = "Boxing",
                dateString = todayStr,
                timeString = "12:30"
            ),
            ScheduledTask(
                templateId = 3,
                title = "Chest & Triceps Hypertrophy",
                type = TaskType.GYM,
                category = "Gym & Weights",
                dateString = todayStr,
                timeString = "18:00"
            )
        )

        defaultScheduled.forEach { taskDao.insertScheduledTask(it) }
    }
}

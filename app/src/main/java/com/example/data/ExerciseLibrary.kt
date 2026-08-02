package com.example.data

data class ExerciseItem(
    val name: String,
    val category: String,
    val type: TaskType,
    val metValue: Float,
    val defaultMinutes: Int,
    val suggestedGoal: String,
    val suggestedSideMissions: String
)

object ExerciseLibrary {
    val presetExercises = listOf(
        ExerciseItem(
            name = "Barbell Squat",
            category = "Legs",
            type = TaskType.GYM,
            metValue = 6.0f,
            defaultMinutes = 45,
            suggestedGoal = "4 sets x 10 reps @ 80kg",
            suggestedSideMissions = "Warm up 5m, Leg Extension 3x15, Hamstring Curls 3x15, Calves 4x20"
        ),
        ExerciseItem(
            name = "Bench Press",
            category = "Chest",
            type = TaskType.GYM,
            metValue = 6.0f,
            defaultMinutes = 40,
            suggestedGoal = "4 sets x 8 reps @ 75kg",
            suggestedSideMissions = "Incline Dumbbell Press 3x12, Cable Flyes 3x15, Tricep Dips 3x12"
        ),
        ExerciseItem(
            name = "Deadlift",
            category = "Back",
            type = TaskType.GYM,
            metValue = 8.0f,
            defaultMinutes = 45,
            suggestedGoal = "5 sets x 5 reps @ 120kg",
            suggestedSideMissions = "Lat Pulldown 4x12, Barbell Rows 3x10, Face Pulls 4x15"
        ),
        ExerciseItem(
            name = "Heavy Bag 10 Rounds",
            category = "Boxing",
            type = TaskType.BOXING,
            metValue = 9.8f,
            defaultMinutes = 30,
            suggestedGoal = "10 Rounds x 3 mins (30s rest)",
            suggestedSideMissions = "Jab-Cross-Hook combos, Slip-Counters, Body shots, High volume output"
        ),
        ExerciseItem(
            name = "5km Outdoor Run",
            category = "Cardio",
            type = TaskType.MAIN_SIDE,
            metValue = 10.0f,
            defaultMinutes = 25,
            suggestedGoal = "Maintain pace < 5:15 min/km",
            suggestedSideMissions = "Dynamic stretch 5m, Hydrate 500ml, Post-run stride cool down"
        ),
        ExerciseItem(
            name = "Jump Rope HIIT",
            category = "Cardio",
            type = TaskType.MAIN_SIDE,
            metValue = 11.0f,
            defaultMinutes = 20,
            suggestedGoal = "20 mins Interval (45s work / 15s rest)",
            suggestedSideMissions = "Double unders practice, High knees 2m, Core plank finisher"
        ),
        ExerciseItem(
            name = "Overhead Shoulder Press",
            category = "Shoulders",
            type = TaskType.GYM,
            metValue = 6.0f,
            defaultMinutes = 35,
            suggestedGoal = "4 sets x 10 reps @ 50kg",
            suggestedSideMissions = "Lateral Raises 4x15, Rear Delt Flyes 4x15, Shrugs 3x12"
        ),
        ExerciseItem(
            name = "Pull-Ups & Core",
            category = "Back/Core",
            type = TaskType.GYM,
            metValue = 7.0f,
            defaultMinutes = 30,
            suggestedGoal = "50 Total Pull-ups (5 sets x 10)",
            suggestedSideMissions = "Hanging Leg Raises 4x15, Ab Wheel Rollouts 3x12, Cable Woodchoppers"
        ),
        ExerciseItem(
            name = "Shadowboxing & Footwork",
            category = "Boxing",
            type = TaskType.BOXING,
            metValue = 7.5f,
            defaultMinutes = 20,
            suggestedGoal = "6 Rounds x 3 mins with weights",
            suggestedSideMissions = "Pivot drill 5m, Duck & weave 5m, Speed combination drills"
        )
    )

    val quickPresets = listOf(
        TaskTemplate(
            title = "Heavy Bag Boxing (10 Rounds)",
            type = TaskType.BOXING,
            category = "Boxing",
            estimatedMinutes = 30,
            boxingTotalDurationSec = 1800,
            boxingMovementsRaw = "Jab-Cross-Hook:30,Slip-Counter:30,Pivot:30,Body Shot:30",
            mainMissionGoal = "Complete all 10 rounds with max power",
            sideMissionsRaw = "Speed round 3m, Endurance round 3m, Core finisher 50 sit-ups"
        ),
        TaskTemplate(
            title = "Push Day Hypertrophy",
            type = TaskType.GYM,
            category = "Gym",
            estimatedMinutes = 50,
            gymTargetMuscle = "Chest / Shoulders / Triceps",
            mainMissionGoal = "Bench Press 4x8 @ RPE 8",
            sideMissionsRaw = "Incline DB Press 3x12, OHP 4x10, Tricep Pushdowns 4x15"
        ),
        TaskTemplate(
            title = "Standard Leg Day Blitz",
            type = TaskType.GYM,
            category = "Gym",
            estimatedMinutes = 60,
            gymTargetMuscle = "Quads / Glutes / Calves",
            mainMissionGoal = "Barbell Back Squat 5x5",
            sideMissionsRaw = "Romanian Deadlift 4x10, Leg Press 3x15, Standing Calf Raise 4x20"
        ),
        TaskTemplate(
            title = "5km Pace Cardio Run",
            type = TaskType.MAIN_SIDE,
            category = "Cardio",
            estimatedMinutes = 25,
            mainMissionGoal = "Run 5.0 km continuous under 25 mins",
            sideMissionsRaw = "5 min warm up walk, Post-run hamstring stretch 5m"
        )
    )

    fun search(query: String): List<ExerciseItem> {
        if (query.isBlank()) return presetExercises
        val q = query.lowercase().trim()
        return presetExercises.filter {
            it.name.lowercase().contains(q) ||
            it.category.lowercase().contains(q) ||
            it.suggestedGoal.lowercase().contains(q)
        }
    }

    fun getMetForTask(type: TaskType, category: String): Float {
        return when {
            type == TaskType.BOXING -> 9.8f
            type == TaskType.GYM -> 6.5f
            category.equals("Cardio", ignoreCase = true) -> 10.0f
            else -> 7.0f
        }
    }

    fun calculateCalories(durationMinutes: Int, weightKg: Float, met: Float): Int {
        if (durationMinutes <= 0 || weightKg <= 0f) return 0
        // Cal = MET * 3.5 * weightKg / 200 * durationMinutes
        val cal = (met * 3.5f * weightKg / 200f) * durationMinutes
        return cal.toInt()
    }
}

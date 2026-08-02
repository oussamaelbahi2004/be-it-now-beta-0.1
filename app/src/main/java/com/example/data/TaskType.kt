package com.example.data

enum class TaskType {
    MAIN_SIDE,   // Main & Side Missions (e.g., Running + 100 Pushups)
    BOXING,      // Custom Boxing Timer with Movement sequence
    GYM,         // Gym & Weightlifting with Reps/Weight per set
    NORMAL       // Normal task (Text + Checkbox)
}

data class SideMissionItem(
    val title: String,
    val isCompleted: Boolean = false
)

data class BoxingMovement(
    val name: String,
    val durationSeconds: Int
)

data class GymExercise(
    val exerciseName: String,
    val targetSets: Int,
    val targetReps: Int,
    val targetWeightKg: Float
)

data class GymSetLog(
    val setIndex: Int,
    val repsCompleted: Int,
    val weightKg: Float,
    val isDone: Boolean = false
)

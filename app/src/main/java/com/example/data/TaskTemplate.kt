package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "task_templates")
data class TaskTemplate(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val type: TaskType,
    val category: String = "Fitness",
    val estimatedMinutes: Int = 30,
    
    // Type 1: Main & Side
    val mainMissionGoal: String = "",
    val sideMissionsRaw: String = "", // JSON list of side mission titles: "Pushups,Squats"
    
    // Type 2: Boxing Timer
    val boxingTotalDurationSec: Int = 1800,
    val boxingMovementsRaw: String = "", // JSON string: "Jab-Cross:30,Hook-Duck:45,Shadowbox:60"
    
    // Type 3: Gym
    val gymTargetMuscle: String = "Full Body",
    val gymExercisesRaw: String = "", // JSON string: "Bench Press|4|10|80,Squat|4|8|100"
    
    // Type 4: Normal
    val normalTaskNote: String = ""
)

package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey
    val id: Int = 1,
    val uid: String = "guest_warrior",
    val displayName: String = "WARRIOR",
    val email: String = "warrior@beitnow.app",
    val photoUrl: String = "",
    val xp: Int = 150,
    val level: Int = 1,
    val currentStreak: Int = 3,
    val maxStreak: Int = 5,
    val lastCompletedDate: String = "",
    val unlockedBadgesRaw: String = "STREAK_3,IRON_BEAST", // Comma separated badge codes
    val isDeloadModeActive: Boolean = false,
    val partnerName: String = "Coach Marcus",
    val partnerCode: String = "BEAST-8821",
    val bodyWeightKg: Float = 75.0f,
    val lastCloudSyncTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "monthly_reports")
data class MonthlyReport(
    @PrimaryKey
    val monthYearKey: String, // Format: YYYY-MM
    val grade: String,        // "A", "B", "C"
    val completionPercentage: Float,
    val totalScheduled: Int,
    val totalCompleted: Int,
    val evaluationNotes: String
)

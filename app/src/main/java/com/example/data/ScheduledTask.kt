package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scheduled_tasks")
data class ScheduledTask(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val templateId: Long,
    val title: String,
    val type: TaskType,
    val category: String,
    val dateString: String, // Format: YYYY-MM-DD
    val timeString: String, // Format: HH:mm
    val isCompleted: Boolean = false,
    
    // Execution records
    val actualDurationMinutes: Int = 0,
    val sideMissionsStatusRaw: String = "", // Map of side mission title to completed status
    val gymLogDataRaw: String = "",         // Completed sets and weights raw
    val completedAtTimestamp: Long? = null
)

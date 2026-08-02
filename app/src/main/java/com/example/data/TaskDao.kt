package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    // Templates
    @Query("SELECT * FROM task_templates ORDER BY id DESC")
    fun getAllTemplates(): Flow<List<TaskTemplate>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplate(template: TaskTemplate): Long

    @Query("DELETE FROM task_templates WHERE id = :id")
    suspend fun deleteTemplate(id: Long)

    // Scheduled Tasks
    @Query("SELECT * FROM scheduled_tasks WHERE dateString = :date ORDER BY timeString ASC")
    fun getTasksForDate(date: String): Flow<List<ScheduledTask>>

    @Query("SELECT * FROM scheduled_tasks WHERE dateString LIKE :monthPrefix || '%'")
    fun getTasksForMonth(monthPrefix: String): Flow<List<ScheduledTask>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScheduledTask(task: ScheduledTask): Long

    @Update
    suspend fun updateScheduledTask(task: ScheduledTask)

    @Query("DELETE FROM scheduled_tasks WHERE id = :id")
    suspend fun deleteScheduledTask(id: Long)

    // User Profile
    @Query("SELECT * FROM user_profile WHERE id = 1")
    fun getUserProfile(): Flow<UserProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUserProfile(profile: UserProfile)

    // Monthly Reports
    @Query("SELECT * FROM monthly_reports ORDER BY monthYearKey DESC")
    fun getAllMonthlyReports(): Flow<List<MonthlyReport>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveMonthlyReport(report: MonthlyReport)

    // Weight Logs
    @Query("SELECT * FROM weight_logs ORDER BY dateString ASC")
    fun getAllWeightLogs(): Flow<List<WeightLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeightLog(weightLog: WeightLog): Long

    // Proof Posts (Legacy Feed)
    @Query("SELECT * FROM proof_posts ORDER BY timestamp DESC")
    fun getAllProofPosts(): Flow<List<ProofPost>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProofPost(post: ProofPost)

    @Query("DELETE FROM proof_posts WHERE id = :id")
    suspend fun deleteProofPost(id: String)
}

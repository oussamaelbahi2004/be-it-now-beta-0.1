package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "proof_posts")
data class ProofPost(
    @PrimaryKey
    val id: String = java.util.UUID.randomUUID().toString(),
    val userId: String = "local_warrior",
    val userName: String = "BEAST WARRIOR",
    val userPhotoUrl: String = "",
    val workoutTitle: String = "Heavy Weightlifting",
    val workoutCategory: String = "Gym",
    val statsText: String = "45m • 120kg • 420 kcal",
    val photoUri: String = "",
    val dateString: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isBeforeAfterPair: Boolean = false,
    val beforePhotoUri: String = "",
    val afterPhotoUri: String = "",
    val caption: String = "Proof of Work logged. Excuses destroyed."
)

package com.example.data

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.PersistentCacheSettings
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseManager(private val context: Context) {

    private val TAG = "FirebaseManager"
    private var auth: FirebaseAuth? = null
    private var firestore: FirebaseFirestore? = null

    private val _currentUserFlow = MutableStateFlow<FirebaseUser?>(null)
    val currentUserFlow: StateFlow<FirebaseUser?> = _currentUserFlow.asStateFlow()

    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()

    init {
        initializeFirebase()
    }

    private fun initializeFirebase() {
        try {
            val app = if (FirebaseApp.getApps(context).isEmpty()) {
                val options = FirebaseOptions.Builder()
                    .setApiKey("AIzaSyAgoqSIkddXN0Kd8dJ64LxyQfe-93p0OlU")
                    .setApplicationId("1:220622723501:web:88e152346f83b00cd06924")
                    .setProjectId("lalo-b312c")
                    .setStorageBucket("lalo-b312c.firebasestorage.app")
                    .setGcmSenderId("220622723501")
                    .build()
                FirebaseApp.initializeApp(context, options)
            } else {
                FirebaseApp.getInstance()
            }

            auth = FirebaseAuth.getInstance(app)
            firestore = FirebaseFirestore.getInstance(app).apply {
                firestoreSettings = FirebaseFirestoreSettings.Builder()
                    .setLocalCacheSettings(PersistentCacheSettings.newBuilder().build())
                    .build()
            }

            auth?.addAuthStateListener { firebaseAuth ->
                _currentUserFlow.value = firebaseAuth.currentUser
            }

            _isInitialized.value = true
            Log.d(TAG, "Firebase successfully initialized with project lalo-b312c")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing Firebase: ${e.message}", e)
            _isInitialized.value = false
        }
    }

    suspend fun signInAnonymously(): FirebaseUser? {
        return try {
            val result = auth?.signInAnonymously()?.await()
            _currentUserFlow.value = result?.user
            result?.user
        } catch (e: Exception) {
            Log.e(TAG, "Anonymous sign in error: ${e.message}")
            null
        }
    }

    fun getCurrentUser(): FirebaseUser? = auth?.currentUser

    fun signOut() {
        auth?.signOut()
        _currentUserFlow.value = null
    }

    suspend fun saveProfileToFirestore(profile: UserProfile) {
        val uid = auth?.currentUser?.uid ?: profile.uid
        if (uid.isBlank()) return

        val userMap = hashMapOf(
            "uid" to uid,
            "displayName" to profile.displayName,
            "email" to profile.email,
            "photoUrl" to profile.photoUrl,
            "xp" to profile.xp,
            "level" to profile.level,
            "currentStreak" to profile.currentStreak,
            "maxStreak" to profile.maxStreak,
            "lastCompletedDate" to profile.lastCompletedDate,
            "unlockedBadges" to profile.unlockedBadgesRaw,
            "bodyWeightKg" to profile.bodyWeightKg,
            "updatedAt" to System.currentTimeMillis()
        )

        try {
            firestore?.collection("users")?.document(uid)?.set(userMap)?.await()
            Log.d(TAG, "User profile synced to Firestore for uid: $uid")
        } catch (e: Exception) {
            Log.w(TAG, "Failed to sync user profile to Firestore (Offline cache active): ${e.message}")
        }
    }

    suspend fun uploadProofPost(post: ProofPost) {
        val uid = auth?.currentUser?.uid ?: post.userId

        val postMap = hashMapOf(
            "id" to post.id,
            "userId" to uid,
            "userName" to post.userName,
            "userPhotoUrl" to post.userPhotoUrl,
            "workoutTitle" to post.workoutTitle,
            "workoutCategory" to post.workoutCategory,
            "statsText" to post.statsText,
            "photoUri" to post.photoUri,
            "dateString" to post.dateString,
            "timestamp" to post.timestamp,
            "isBeforeAfterPair" to post.isBeforeAfterPair,
            "beforePhotoUri" to post.beforePhotoUri,
            "afterPhotoUri" to post.afterPhotoUri,
            "caption" to post.caption
        )

        try {
            firestore?.collection("proof_posts")?.document(post.id)?.set(postMap)?.await()
            Log.d(TAG, "Proof post saved to Firestore: ${post.id}")
        } catch (e: Exception) {
            Log.w(TAG, "Failed to send proof post to Firestore (cached locally): ${e.message}")
        }
    }

    fun observeRemotePosts(): Flow<List<ProofPost>> = callbackFlow {
        val listener = firestore?.collection("proof_posts")
            ?.orderBy("timestamp", Query.Direction.DESCENDING)
            ?.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Listen failed for proof_posts", error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val posts = snapshot.documents.mapNotNull { doc ->
                        try {
                            ProofPost(
                                id = doc.getString("id") ?: doc.id,
                                userId = doc.getString("userId") ?: "",
                                userName = doc.getString("userName") ?: "WARRIOR",
                                userPhotoUrl = doc.getString("userPhotoUrl") ?: "",
                                workoutTitle = doc.getString("workoutTitle") ?: "Workout",
                                workoutCategory = doc.getString("workoutCategory") ?: "Gym",
                                statsText = doc.getString("statsText") ?: "",
                                photoUri = doc.getString("photoUri") ?: "",
                                dateString = doc.getString("dateString") ?: "",
                                timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis(),
                                isBeforeAfterPair = doc.getBoolean("isBeforeAfterPair") ?: false,
                                beforePhotoUri = doc.getString("beforePhotoUri") ?: "",
                                afterPhotoUri = doc.getString("afterPhotoUri") ?: "",
                                caption = doc.getString("caption") ?: ""
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                    trySend(posts)
                }
            }

        awaitClose { listener?.remove() }
    }
}

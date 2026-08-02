package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.ProofPost
import com.example.ui.AppViewModel
import com.example.ui.components.BeforeAfterSlider
import com.example.ui.components.StatOverlayPhotoPicker

@Composable
fun LegacyFeedScreen(viewModel: AppViewModel) {
    val proofPosts by viewModel.allProofPosts.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()

    var showPhotoPicker by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) } // 0 = Timeline Feed, 1 = Before & After Comparison

    // Seed default sample proof posts if empty
    LaunchedEffect(proofPosts) {
        if (proofPosts.isEmpty()) {
            viewModel.addProofPost(
                workoutTitle = "HEAVY SQUAT & DEADLIFT",
                workoutCategory = "Gym",
                statsText = "60 MINS • 180 KG • 520 KCAL",
                photoUri = "https://images.unsplash.com/photo-1517838277536-f5f99be501cd?auto=format&fit=crop&w=800&q=80",
                caption = "Pushed maximum intensity. Excuses destroyed."
            )
            viewModel.addProofPost(
                workoutTitle = "10KM MORNING PROTOCOL",
                workoutCategory = "Cardio",
                statsText = "10 KM • 48:12 MINS • 680 KCAL",
                photoUri = "https://images.unsplash.com/photo-1534438327276-14e5300c3a48?auto=format&fit=crop&w=800&q=80",
                caption = "No delay. Cold morning 10K executed."
            )
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Screen Title Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "THE LEGACY FEED",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = "VISUAL PROOF OF WORK & TRANSFORMATION",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF6B00)
                    )
                }

                Button(
                    onClick = { showPhotoPicker = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6B00)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.AddAPhoto, contentDescription = "Add Proof", tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("ADD PROOF", fontWeight = FontWeight.Black, color = Color.Black, fontSize = 11.sp)
                }
            }
        }

        // Tab Selector Row (0 = PROOF FEED, 1 = BEFORE & AFTER TOOL)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF141414), RoundedCornerShape(12.dp))
                    .padding(4.dp)
            ) {
                Surface(
                    onClick = { selectedTab = 0 },
                    shape = RoundedCornerShape(10.dp),
                    color = if (selectedTab == 0) Color(0xFFFF6B00) else Color.Transparent,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(vertical = 10.dp)
                    ) {
                        Text(
                            text = "PROOF OF WORK FEED",
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            color = if (selectedTab == 0) Color.Black else Color.Gray
                        )
                    }
                }

                Surface(
                    onClick = { selectedTab = 1 },
                    shape = RoundedCornerShape(10.dp),
                    color = if (selectedTab == 1) Color(0xFFFF6B00) else Color.Transparent,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(vertical = 10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Compare,
                                contentDescription = "Compare",
                                tint = if (selectedTab == 1) Color.Black else Color.Gray,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "BEFORE & AFTER",
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp,
                                color = if (selectedTab == 1) Color.Black else Color.Gray
                            )
                        }
                    }
                }
            }
        }

        // Tab Content
        if (selectedTab == 1) {
            // BEFORE & AFTER COMPARISON TOOL SLIDER
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "DAY 1 VS DAY 40 TRANSFORMATION SLIDER",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )

                    Text(
                        text = "Drag handle horizontally to compare Day 1 baseline with Day 40 peak condition.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    BeforeAfterSlider(
                        beforePhotoUrl = "https://images.unsplash.com/photo-1581009146145-b5ef050c2e1e?auto=format&fit=crop&w=800&q=80",
                        afterPhotoUrl = "https://images.unsplash.com/photo-1517838277536-f5f99be501cd?auto=format&fit=crop&w=800&q=80",
                        beforeLabel = "DAY 1 (BASELINE)",
                        afterLabel = "DAY 40 (WARRIOR)"
                    )
                }
            }
        } else {
            // PROOF OF WORK TIMELINE POSTS
            items(proofPosts, key = { it.id }) { post ->
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF141414),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // User Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF282828))
                                ) {
                                    if (post.userPhotoUrl.isNotBlank()) {
                                        AsyncImage(
                                            model = post.userPhotoUrl,
                                            contentDescription = "Avatar",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.LocalFireDepartment,
                                            contentDescription = "User",
                                            tint = Color(0xFFFF6B00),
                                            modifier = Modifier
                                                .size(20.dp)
                                                .align(Alignment.Center)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = post.userName.uppercase(),
                                            fontWeight = FontWeight.Black,
                                            color = Color.White,
                                            fontSize = 13.sp
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.Default.Verified,
                                            contentDescription = "Verified",
                                            tint = Color(0xFFFF6B00),
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                    Text(
                                        text = post.dateString,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.Gray
                                    )
                                }
                            }

                            IconButton(onClick = { viewModel.deleteProofPost(post.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Photo with Aggressive Stat Overlay
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF1E1E1E))
                        ) {
                            if (post.photoUri.isNotBlank()) {
                                AsyncImage(
                                    model = post.photoUri,
                                    contentDescription = post.workoutTitle,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            // Dark Scrim Overlay
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Transparent,
                                                Color.Black.copy(alpha = 0.85f)
                                            )
                                        )
                                    )
                            )

                            // Stat Overlay Text
                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(14.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFFFF6B00), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = post.workoutCategory.uppercase(),
                                        fontWeight = FontWeight.Black,
                                        fontSize = 9.sp,
                                        color = Color.Black
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = post.workoutTitle.uppercase(),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )

                                Text(
                                    text = post.statsText.uppercase(),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFFFF6B00)
                                )
                            }
                        }

                        if (post.caption.isNotBlank()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "\"${post.caption}\"",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }
    }

    // Photo Picker Dialog Popup
    if (showPhotoPicker) {
        StatOverlayPhotoPicker(
            workoutTitle = "HEAVY GYM WORKOUT",
            workoutCategory = "GYM",
            onDismiss = { showPhotoPicker = false },
            onSubmitProof = { photoUri, caption, statsText ->
                viewModel.addProofPost(
                    workoutTitle = "HEAVY GYM WORKOUT",
                    workoutCategory = "GYM",
                    statsText = statsText,
                    photoUri = photoUri,
                    caption = caption
                )
                showPhotoPicker = false
            }
        )
    }
}

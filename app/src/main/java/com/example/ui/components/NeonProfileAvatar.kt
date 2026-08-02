package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun NeonProfileAvatar(
    photoUrl: String,
    level: Int,
    size: Dp = 96.dp,
    modifier: Modifier = Modifier
) {
    // Determine neon colors based on Level
    val primaryNeon = when {
        level >= 10 -> Color(0xFFFFD700) // Glowing Gold for Titan Level
        level >= 5 -> Color(0xFF00E5FF)  // Electric Cyan for Beast Level
        else -> Color(0xFFFF6B00)        // Aggressive Orange for Initiate Level
    }

    val secondaryNeon = when {
        level >= 10 -> Color(0xFFFF6500)
        level >= 5 -> Color(0xFF7000FF)
        else -> Color(0xFFFF0055)
    }

    val infiniteTransition = rememberInfiniteTransition(label = "NeonRotation")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Angle"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(size)
    ) {
        // Neon Glowing Outer Border Canvas
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawWithContent {
                    drawContent()
                    rotate(rotationAngle) {
                        drawCircle(
                            brush = Brush.sweepGradient(
                                listOf(
                                    primaryNeon,
                                    secondaryNeon,
                                    Color.White,
                                    primaryNeon
                                )
                            ),
                            style = Stroke(width = 6.dp.toPx())
                        )
                    }
                }
                .padding(6.dp)
        )

        // User Image or Icon Box
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(Color(0xFF1F1F1F))
                .border(2.dp, Color.Black, CircleShape)
        ) {
            if (photoUrl.isNotBlank()) {
                AsyncImage(
                    model = photoUrl,
                    contentDescription = "Profile Avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Default Avatar",
                    tint = primaryNeon,
                    modifier = Modifier.size(size / 2)
                )
            }
        }
    }
}

package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@Composable
fun BeforeAfterSlider(
    beforePhotoUrl: String,
    afterPhotoUrl: String,
    beforeLabel: String = "DAY 1 (START)",
    afterLabel: String = "DAY 40 (BEAST)",
    modifier: Modifier = Modifier
) {
    var sliderFraction by remember { mutableStateOf(0.5f) }
    var containerWidthPx by remember { mutableStateOf(1f) }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF141414),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFFF6B00)),
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp)
            .clip(RoundedCornerShape(20.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { containerWidthPx = it.width.toFloat() }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures { change, dragAmount ->
                        change.consume()
                        if (containerWidthPx > 0) {
                            sliderFraction = (sliderFraction + (dragAmount / containerWidthPx)).coerceIn(0.05f, 0.95f)
                        }
                    }
                }
        ) {
            // After Image (Full width background)
            if (afterPhotoUrl.isNotBlank()) {
                AsyncImage(
                    model = afterPhotoUrl,
                    contentDescription = "After Transformation",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF221100))
                ) {
                    Text(
                        text = "AFTER (DAY 40 PHOTO)",
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFFF6B00)
                    )
                }
            }

            // Before Image (Clipped overlay up to sliderFraction)
            val clipShape = remember(sliderFraction) {
                object : Shape {
                    override fun createOutline(
                        size: Size,
                        layoutDirection: LayoutDirection,
                        density: Density
                    ): Outline {
                        return Outline.Rectangle(
                            Rect(0f, 0f, size.width * sliderFraction, size.height)
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(clipShape)
            ) {
                if (beforePhotoUrl.isNotBlank()) {
                    AsyncImage(
                        model = beforePhotoUrl,
                        contentDescription = "Before Transformation",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF1A1A1A))
                    ) {
                        Text(
                            text = "BEFORE (DAY 1 PHOTO)",
                            fontWeight = FontWeight.Black,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            // Overlay Badges for Labels
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
                    .background(Color.Black.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = beforeLabel,
                    fontWeight = FontWeight.Black,
                    fontSize = 11.sp,
                    color = Color.White
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .background(Color(0xFFFF6B00).copy(alpha = 0.9f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = afterLabel,
                    fontWeight = FontWeight.Black,
                    fontSize = 11.sp,
                    color = Color.Black
                )
            }

            // Vertical Slider Line with Handle
            val lineOffsetDp = (containerWidthPx * sliderFraction).dp

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(3.dp)
                    .offset(x = lineOffsetDp - 1.5.dp)
                    .background(Color.White)
            )

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .offset(x = lineOffsetDp - 20.dp)
                    .size(40.dp)
                    .background(Color(0xFFFF6B00), CircleShape)
                    .border(2.dp, Color.White, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.SwapHoriz,
                    contentDescription = "Drag Handle",
                    tint = Color.Black,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

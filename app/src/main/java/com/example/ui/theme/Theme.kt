package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = FieryOrangePrimary,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF331500),
    onPrimaryContainer = Color(0xFFFFCCAA),
    secondary = ElectricAmberSecondary,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF261800),
    onSecondaryContainer = Color(0xFFFFE0B2),
    tertiary = FlameRedTertiary,
    onTertiary = Color.White,
    background = ObsidianBackground,
    onBackground = TextPrimary,
    surface = ObsidianSurface,
    onSurface = TextPrimary,
    surfaceVariant = ObsidianSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = BentoBorder,
    error = ErrorRed,
    onError = Color.White
)

@Composable
fun BeItNowTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}

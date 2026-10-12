package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = NeonPink,
    onPrimary = Color.White,
    primaryContainer = NeonPurple,
    onPrimaryContainer = Color.White,
    secondary = NeonCyan,
    onSecondary = BrandDark,
    secondaryContainer = BrandDarkCard,
    onSecondaryContainer = NeonCyan,
    tertiary = NeonAmber,
    onTertiary = BrandDark,
    background = BrandDark,
    onBackground = TextPrimary,
    surface = BrandDarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = BrandDarkCard,
    onSurfaceVariant = TextSecondary,
    outline = BorderSubtle,
    error = LiveRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Live streaming app looks best in immersive dark studio mode
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}

package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val RetroGbaColorScheme = darkColorScheme(
    primary = RetroRed,
    onPrimary = Color.White,
    secondary = RetroGold,
    onSecondary = RetroDarkBg,
    tertiary = RetroBlue,
    onTertiary = Color.White,
    background = RetroDarkBg,
    onBackground = RetroWindowHighlight,
    surface = RetroSurface,
    onSurface = RetroWindowHighlight,
    surfaceVariant = RetroWindowBevel,
    onSurfaceVariant = RetroWindowHighlight
)

@Composable
fun FoodCardMasterTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = RetroGbaColorScheme,
        typography = AppTypography,
        content = content
    )
}

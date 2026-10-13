package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = CyberCyan,
    onPrimary = CyberDarkNavy,
    primaryContainer = CyberBlue.copy(alpha = 0.3f),
    onPrimaryContainer = CyberCyan,
    secondary = CyberGreen,
    onSecondary = Color.Black,
    secondaryContainer = CyberGreen.copy(alpha = 0.2f),
    onSecondaryContainer = CyberGreen,
    tertiary = CyberAmber,
    background = CyberDarkNavy,
    onBackground = TextPrimary,
    surface = CyberSurface,
    onSurface = TextPrimary,
    surfaceVariant = CyberCard,
    onSurfaceVariant = TextSecondary,
    outline = CyberBorder,
    error = CyberRed,
    onError = Color.White
)

private val LightColorScheme = darkColorScheme( // Keep cyber high-tech dark theme as default for cyber guard vibe
    primary = CyberCyan,
    onPrimary = CyberDarkNavy,
    background = CyberDarkNavy,
    surface = CyberSurface,
    onBackground = TextPrimary,
    onSurface = TextPrimary
)

@Composable
fun AntiReturnTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}

package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = TelemetryCyan,
    onPrimary = DarkBackground,
    secondary = TelemetryBlue,
    onSecondary = DarkBackground,
    tertiary = TelemetryGreen,
    onTertiary = DarkBackground,
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkCard,
    onSurfaceVariant = TextSecondary,
    outline = DarkCardBorder,
    error = TelemetryRed
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Dark mode by default for IoT dashboard
    dynamicColor: Boolean = false, // Keep high-contrast telemetry palette
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}


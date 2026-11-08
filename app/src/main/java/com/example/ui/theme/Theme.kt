package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val ShortlyColorScheme = darkColorScheme(
    primary = ShortlyPrimary,
    onPrimary = ShortlyTextPrimary,
    primaryContainer = ShortlySurfaceLight,
    onPrimaryContainer = ShortlyTextPrimary,
    secondary = ShortlyCyan,
    onSecondary = ShortlyDark,
    background = DarkColorSchemeBackground,
    onBackground = ShortlyTextPrimary,
    surface = ShortlySurfaceDark,
    onSurface = ShortlyTextPrimary,
    surfaceVariant = ShortlySurfaceLight,
    onSurfaceVariant = ShortlyTextSecondary,
    error = ShortlyError,
    onError = ShortlyTextPrimary
)

@Composable
fun ShortlyTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ShortlyColorScheme,
        typography = Typography,
        content = content
    )
}

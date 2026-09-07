package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = JarvisCyan,
    onPrimary = Color.Black,
    primaryContainer = JarvisDeepBlue,
    onPrimaryContainer = JarvisCyanLight,
    secondary = JarvisCyanLight,
    onSecondary = Color.Black,
    tertiary = JarvisAmber,
    background = JarvisDarkBg,
    onBackground = Color.White,
    surface = JarvisSurfaceDark,
    onSurface = Color.White,
    surfaceVariant = JarvisNavy,
    onSurfaceVariant = JarvisCyanLight,
    outline = JarvisSurfaceBorder
)

private val LightColorScheme = lightColorScheme(
    primary = JarvisLightPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFCCE8F4),
    onPrimaryContainer = Color(0xFF003344),
    secondary = JarvisCyanDark,
    onSecondary = Color.White,
    tertiary = JarvisAmber,
    background = JarvisLightBg,
    onBackground = Color(0xFF101B24),
    surface = JarvisLightSurface,
    onSurface = Color(0xFF101B24),
    surfaceVariant = Color(0xFFD6E4EE),
    onSurfaceVariant = Color(0xFF223645),
    outline = JarvisLightBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep sci-fi HUD styling intentional
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

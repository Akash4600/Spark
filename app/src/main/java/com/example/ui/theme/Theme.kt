package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color

val BrandPrimary = Color(0xFFF94C57) // Fiery Orange / Coral
val BrandSecondary = Color(0xFFA63489) // Deep Magenta
val BrandBackground = Color(0xFF0F0F13)
val BrandSurface = Color(0xFF1E1E24)
val BrandOnSurface = Color(0xFFF2F2F2)
val BrandOnPrimary = Color.White

private val DarkColorScheme = darkColorScheme(
    primary = BrandPrimary,
    secondary = BrandSecondary,
    tertiary = Color(0xFFFF715B),
    background = BrandBackground,
    surface = BrandSurface,
    onPrimary = BrandOnPrimary,
    onSecondary = BrandOnPrimary,
    onTertiary = BrandOnPrimary,
    onBackground = BrandOnSurface,
    onSurface = BrandOnSurface,
    surfaceVariant = Color(0xFF2B2B36),
    onSurfaceVariant = Color(0xFFCACACA)
)

private val LightColorScheme = DarkColorScheme // Forced dark theme for the premium vibe

@Composable
fun SparkTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}

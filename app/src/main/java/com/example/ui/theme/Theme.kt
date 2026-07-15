package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = NeonViolet,
    secondary = NeonGreen,
    tertiary = NeonBlue,
    background = BackgroundDark,
    surface = SurfaceDark,
    error = NeonRed,
    onPrimary = Color.White,
    onSecondary = BackgroundDark,
    onBackground = TextPrimary,
    onSurface = TextPrimary
  )

private val LightColorScheme = DarkColorScheme // Always premium dark theme

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Force dark-mode for futuristic trading UI
  dynamicColor: Boolean = false, // Disable dynamic colors to preserve branding theme
  content: @Composable () -> Unit,
) {
  val colorScheme = DarkColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

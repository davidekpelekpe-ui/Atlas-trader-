package com.example.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

object ThemeState {
    var currentTheme by mutableStateOf("obsidian_pitch_black")

    val background: Color
        get() = when (currentTheme) {
            "sleek_brutalist_light" -> Color(0xFFF7F6FD) // Light pastel glass theme
            "terminal_dark" -> Color(0xFF020A04)
            else -> Color(0xFF070510) // obsidian_pitch_black: gorgeous deep space midnight-purple
        }

    val surface: Color
        get() = when (currentTheme) {
            "sleek_brutalist_light" -> Color(0xFFEBE9F5)
            "terminal_dark" -> Color(0xFF041608)
            else -> Color(0xFF120E2C) // rich midnight-purple surface
        }

    val surfaceCard: Color
        get() = when (currentTheme) {
            "sleek_brutalist_light" -> Color(0xC0FFFFFF) // translucent white glass
            "terminal_dark" -> Color(0xFF06250E)
            else -> Color(0x20FFFFFF) // translucent frosted glass card
        }

    val borderColor: Color
        get() = when (currentTheme) {
            "sleek_brutalist_light" -> Color(0xFFD6D1E8)
            "terminal_dark" -> Color(0xFF059669)
            else -> Color(0x359D7CFF) // elegant glowing lavender border
        }

    val primary: Color
        get() = when (currentTheme) {
            "sleek_brutalist_light" -> Color(0xFF7540F0) // rich royal violet
            "terminal_dark" -> Color(0xFF10B981)
            else -> Color(0xFF9061FF) // glowing neon violet
        }

    val neonGreen: Color
        get() = when (currentTheme) {
            "sleek_brutalist_light" -> Color(0xFF10B981)
            "terminal_dark" -> Color(0xFF00FF66)
            else -> Color(0xFF2EE59D) // sleek vibrant green
        }

    val neonRed: Color
        get() = when (currentTheme) {
            "sleek_brutalist_light" -> Color(0xFFEF4444)
            "terminal_dark" -> Color(0xFFFF3333)
            else -> Color(0xFFFF497C) // sleek neon red
        }

    val neonBlue: Color
        get() = when (currentTheme) {
            "sleek_brutalist_light" -> Color(0xFF3B82F6)
            "terminal_dark" -> Color(0xFF06B6D4)
            else -> Color(0xFF00D2FF) // vibrant neon cyan
        }

    val textPrimary: Color
        get() = when (currentTheme) {
            "sleek_brutalist_light" -> Color(0xFF1B163A)
            "terminal_dark" -> Color(0xFF34D399)
            else -> Color(0xFFFFFFFF)
        }

    val textSecondary: Color
        get() = when (currentTheme) {
            "sleek_brutalist_light" -> Color(0xFF787299)
            "terminal_dark" -> Color(0xFF059669)
            else -> Color(0xFFA5A2B5)
        }

    val textMuted: Color
        get() = when (currentTheme) {
            "sleek_brutalist_light" -> Color(0xFFAAA5C7)
            "terminal_dark" -> Color(0xFF047857)
            else -> Color(0xFF6F6C85)
        }
}

val BackgroundDark: Color
    get() = ThemeState.background

val SurfaceDark: Color
    get() = ThemeState.surface

val SurfaceCard: Color
    get() = ThemeState.surfaceCard

val BorderColor: Color
    get() = ThemeState.borderColor

val NeonViolet: Color
    get() = ThemeState.primary

val NeonGreen: Color
    get() = ThemeState.neonGreen

val NeonRed: Color
    get() = ThemeState.neonRed

val NeonBlue: Color
    get() = ThemeState.neonBlue

val TextPrimary: Color
    get() = ThemeState.textPrimary

val TextSecondary: Color
    get() = ThemeState.textSecondary

val TextMuted: Color
    get() = ThemeState.textMuted

val Gold = Color(0xFFFFB300)

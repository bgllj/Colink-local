package com.colink.app.ui.theme

/* UI palette adapted from ShiGuangSchedule v2.1.0 (Apache-2.0). */

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Paper = Color(0xFFFFFAFE)
val PaperLight = Color(0xFFFFFFFF)
val Ink = Color(0xFF1D1B20)
val Green = Color(0xFF4B5038)
val GreenSoft = Color(0xFFECE7B2)
val Moss = Color(0xFF66675F)
val Sand = Color(0xFFFFE699)
val Line = Color(0xFFE8E6E1)

private val courseAccents = listOf(
    Color(0xFF854C0E),
    Color(0xFF735B00),
    Color(0xFF4F6200),
    Color(0xFF35651A),
    Color(0xFF00663A),
    Color(0xFF006658),
    Color(0xFF006666),
    Color(0xFF004D66),
    Color(0xFF422B8A),
    Color(0xFF7B1B62),
    Color(0xFF801C4C),
    Color(0xFF801A39),
)

private val courseTints = listOf(
    Color(0xFFFFCC99),
    Color(0xFFFFE699),
    Color(0xFFE6FF99),
    Color(0xFFCCFF99),
    Color(0xFF99FFB3),
    Color(0xFF99FFE6),
    Color(0xFF99FFFF),
    Color(0xFF99E6FF),
    Color(0xFFB399FF),
    Color(0xFFFF99E6),
    Color(0xFFFF99CC),
    Color(0xFFFF99B3),
)

fun courseAccent(courseId: String): Color = courseAccents[Math.floorMod(courseId.hashCode(), courseAccents.size)]

fun courseTint(courseId: String): Color = courseTints[Math.floorMod(courseId.hashCode(), courseTints.size)]

private val ColinkScheme = lightColorScheme(
    primary = Green,
    onPrimary = Color.White,
    primaryContainer = GreenSoft,
    onPrimaryContainer = Ink,
    secondary = Moss,
    background = Paper,
    onBackground = Ink,
    surface = PaperLight,
    onSurface = Ink,
    surfaceVariant = Color(0xFFF0E9DD),
    outline = Color(0xFF817B70),
)

@Composable
fun ColinkTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = ColinkScheme, typography = Typography(), content = content)
}

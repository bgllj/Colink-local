package com.colink.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Paper = Color(0xFFF5F6F5)
val PaperLight = Color(0xFFFFFFFF)
val Ink = Color(0xFF1F322B)
val Green = Color(0xFF2F6F5E)
val GreenSoft = Color(0xFFDCECE2)
val Moss = Color(0xFF6D8D74)
val Sand = Color(0xFFF0E1BE)
val Line = Color(0xFFE3E7E3)

private val courseAccents = listOf(
    Color(0xFF2F6F5E),
    Color(0xFF506B9A),
    Color(0xFFAF762E),
    Color(0xFFA55252),
    Color(0xFF735A94),
    Color(0xFF35777E),
)

private val courseTints = listOf(
    Color(0xFFE0EFE8),
    Color(0xFFE5EAF5),
    Color(0xFFF5EBD7),
    Color(0xFFF2E3E3),
    Color(0xFFE8E3F4),
    Color(0xFFE1EEF0),
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

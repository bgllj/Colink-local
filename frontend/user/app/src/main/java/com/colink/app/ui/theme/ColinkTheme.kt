package com.colink.app.ui.theme

/* UI palette adapted from ShiGuangSchedule v2.1.0 (Apache-2.0). */

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

enum class AppThemePalette(
    val storageKey: String,
    val label: String,
    val previewColor: Color,
) {
    CLASSIC("classic", "默认", Color(0xFFECE7B2)),
    MINT("mint", "薄荷", Color(0xFFD6F3E5)),
    OCEAN("ocean", "海盐", Color(0xFFD6EAF8));

    companion object {
        fun fromStorageKey(key: String?): AppThemePalette =
            values().firstOrNull { it.storageKey == key } ?: CLASSIC
    }
}

private data class ColinkColors(
    val paper: Color,
    val paperLight: Color,
    val ink: Color,
    val green: Color,
    val greenSoft: Color,
    val moss: Color,
    val sand: Color,
    val line: Color,
    val surfaceVariant: Color,
    val outline: Color,
    val courseAccents: List<Color>,
    val courseTints: List<Color>,
)

private val classicCourseAccents = listOf(
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

private val classicCourseTints = listOf(
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

private val mintCourseAccents = listOf(
        Color(0xFF47715A), Color(0xFF3C7565), Color(0xFF427A62), Color(0xFF3D7473),
        Color(0xFF3B6E7A), Color(0xFF426B83), Color(0xFF4E6385), Color(0xFF675A83),
        Color(0xFF805A79), Color(0xFF8A644D), Color(0xFF7A703E), Color(0xFF5E744C),
)

private val oceanCourseAccents = listOf(
        Color(0xFF386684), Color(0xFF3B6487), Color(0xFF416985), Color(0xFF3A717A),
        Color(0xFF4B745E), Color(0xFF6B7543), Color(0xFF8A7135), Color(0xFF8B624A),
        Color(0xFF805C68), Color(0xFF6D5A7D), Color(0xFF565F89), Color(0xFF456C87),
)

private val mintCourseTints = listOf(
        Color(0xFFD6F3E5), Color(0xFFC6EEDD), Color(0xFFB9E8D4), Color(0xFFC2E9E9),
        Color(0xFFBDE5F2), Color(0xFFD0E6F5), Color(0xFFD4DBF6), Color(0xFFE6DDF4),
        Color(0xFFF1DDF0), Color(0xFFF5E0D3), Color(0xFFF5E7C8), Color(0xFFE6EEC9),
)

private val oceanCourseTints = listOf(
        Color(0xFFD6EAF8), Color(0xFFCBE3F4), Color(0xFFC4DFF0), Color(0xFFC9E9ED),
        Color(0xFFD2EBE2), Color(0xFFDEECCD), Color(0xFFF4EABF), Color(0xFFF3DFC5),
        Color(0xFFEBD8D0), Color(0xFFE8D9ED), Color(0xFFDCDCF2), Color(0xFFCCDCEC),
)

private val themeColors = mapOf(
    AppThemePalette.CLASSIC to ColinkColors(
        paper = Color(0xFFFFFAFE),
        paperLight = Color.White,
        ink = Color(0xFF1D1B20),
        green = Color(0xFF4B5038),
        greenSoft = Color(0xFFECE7B2),
        moss = Color(0xFF66675F),
        sand = Color(0xFFFFE699),
        line = Color(0xFFE8E6E1),
        surfaceVariant = Color(0xFFF0E9DD),
        outline = Color(0xFF817B70),
        courseAccents = classicCourseAccents,
        courseTints = classicCourseTints,
    ),
    AppThemePalette.MINT to ColinkColors(
        paper = Color(0xFFF5FBF7),
        paperLight = Color(0xFFFFFFFF),
        ink = Color(0xFF1D2923),
        green = Color(0xFF386B54),
        greenSoft = Color(0xFFDDEFE3),
        moss = Color(0xFF5D7566),
        sand = Color(0xFFF5E7C8),
        line = Color(0xFFDDE9E0),
        surfaceVariant = Color(0xFFEDF5F0),
        outline = Color(0xFF728779),
        courseAccents = mintCourseAccents,
        courseTints = mintCourseTints,
    ),
    AppThemePalette.OCEAN to ColinkColors(
        paper = Color(0xFFF5F9FC),
        paperLight = Color(0xFFFFFFFF),
        ink = Color(0xFF202832),
        green = Color(0xFF3D637B),
        greenSoft = Color(0xFFDCEBF3),
        moss = Color(0xFF617380),
        sand = Color(0xFFF3E8D3),
        line = Color(0xFFDCE6EC),
        surfaceVariant = Color(0xFFECF3F7),
        outline = Color(0xFF70828E),
        courseAccents = oceanCourseAccents,
        courseTints = oceanCourseTints,
    ),
)

private val LocalColinkColors = staticCompositionLocalOf { themeColors.getValue(AppThemePalette.CLASSIC) }

val Paper: Color
    @Composable get() = LocalColinkColors.current.paper
val PaperLight: Color
    @Composable get() = LocalColinkColors.current.paperLight
val Ink: Color
    @Composable get() = LocalColinkColors.current.ink
val Green: Color
    @Composable get() = LocalColinkColors.current.green
val GreenSoft: Color
    @Composable get() = LocalColinkColors.current.greenSoft
val Moss: Color
    @Composable get() = LocalColinkColors.current.moss
val Sand: Color
    @Composable get() = LocalColinkColors.current.sand
val Line: Color
    @Composable get() = LocalColinkColors.current.line

fun courseAccent(courseId: String, palette: AppThemePalette = AppThemePalette.CLASSIC): Color {
    val colors = themeColors.getValue(palette).courseAccents
    return colors[Math.floorMod(courseId.hashCode(), colors.size)]
}

fun courseTint(courseId: String, palette: AppThemePalette = AppThemePalette.CLASSIC): Color {
    val colors = themeColors.getValue(palette).courseTints
    return colors[Math.floorMod(courseId.hashCode(), colors.size)]
}

@Composable
fun ColinkTheme(palette: AppThemePalette, content: @Composable () -> Unit) {
    val colors = themeColors.getValue(palette)
    val scheme = lightColorScheme(
        primary = colors.green,
        onPrimary = Color.White,
        primaryContainer = colors.greenSoft,
        onPrimaryContainer = colors.ink,
        secondary = colors.moss,
        secondaryContainer = colors.greenSoft,
        onSecondaryContainer = colors.ink,
        background = colors.paper,
        onBackground = colors.ink,
        surface = colors.paperLight,
        onSurface = colors.ink,
        surfaceVariant = colors.surfaceVariant,
        onSurfaceVariant = colors.moss,
        outline = colors.outline,
    )
    CompositionLocalProvider(LocalColinkColors provides colors) {
        MaterialTheme(colorScheme = scheme, typography = Typography(), content = content)
    }
}

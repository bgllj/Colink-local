package com.colink.app.ui.theme

/* UI palette adapted from ShiGuangSchedule v2.1.0 (Apache-2.0). */

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance

enum class AppThemePalette(
    val storageKey: String,
    val label: String,
    val previewColor: Color,
    val isDark: Boolean = false,
) {
    CLASSIC("classic", "默认", Color(0xFFECE7B2)),
    MINT("mint", "薄荷", Color(0xFFD6F3E5)),
    OCEAN("ocean", "海盐", Color(0xFFD6EAF8)),
    SUNSET("sunset", "日落", Color(0xFFF4D9D2)),
    LAVENDER("lavender", "薰衣草", Color(0xFFE8E2F3)),
    NIGHT("night", "夜幕", Color(0xFF202832), isDark = true);

    companion object {
        fun fromStorageKey(key: String?): AppThemePalette =
            values().firstOrNull { it.storageKey == key } ?: CLASSIC
    }
}

enum class AppBackgroundMode(val storageKey: String, val label: String) {
    THEME("theme", "主题"),
    CUSTOM("custom", "自定义"),
    DYNAMIC("dynamic", "动态");

    companion object {
        fun fromStorageKey(key: String?): AppBackgroundMode =
            values().firstOrNull { it.storageKey == key } ?: THEME
    }
}

const val DEFAULT_CUSTOM_BACKGROUND_ARGB: Int = 0xFFF1F3EF.toInt()

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

private val sunsetCourseAccents = listOf(
    Color(0xFF9A5147), Color(0xFF3D6C7D), Color(0xFF58745D), Color(0xFF8A672D),
    Color(0xFF725A82), Color(0xFF41767A), Color(0xFF9A6343), Color(0xFF596D8A),
    Color(0xFF6C7450), Color(0xFF8A5368), Color(0xFF4B7669), Color(0xFF806044),
)

private val sunsetCourseTints = listOf(
    Color(0xFFF5DDD7), Color(0xFFD9EAF0), Color(0xFFE0EBDC), Color(0xFFF2E6C9),
    Color(0xFFE8DFF0), Color(0xFFD6E9E7), Color(0xFFF0E0D2), Color(0xFFDDE5F0),
    Color(0xFFE8E9D7), Color(0xFFF0DCE5), Color(0xFFDDEBE1), Color(0xFFECE2D5),
)

private val lavenderCourseAccents = listOf(
    Color(0xFF596A87), Color(0xFF48746C), Color(0xFF84657C), Color(0xFF7A7047),
    Color(0xFF4F7182), Color(0xFF81614E), Color(0xFF56775B), Color(0xFF695D8A),
    Color(0xFF8A5E66), Color(0xFF496D78), Color(0xFF766C4A), Color(0xFF5B718F),
)

private val lavenderCourseTints = listOf(
    Color(0xFFDDE5F2), Color(0xFFD8EBE5), Color(0xFFECE0EC), Color(0xFFF0E9CF),
    Color(0xFFD9E9EF), Color(0xFFEFE1D6), Color(0xFFDDEBDD), Color(0xFFE5DFF2),
    Color(0xFFF0DDE0), Color(0xFFD9E9E8), Color(0xFFECE8D8), Color(0xFFDCE5F0),
)

private val nightCourseAccents = listOf(
    Color(0xFF91C5E8), Color(0xFF9AD5C0), Color(0xFFF0C879), Color(0xFFE7A8B5),
    Color(0xFFB8A8EA), Color(0xFF8FC9CC), Color(0xFFE5B08A), Color(0xFF9FB5E4),
    Color(0xFFC3D88B), Color(0xFFE0A1D0), Color(0xFF9FD6A8), Color(0xFFD4BD91),
)

private val nightCourseTints = listOf(
    Color(0xFF293B49), Color(0xFF2B433B), Color(0xFF4A402A), Color(0xFF4A3037),
    Color(0xFF3B3450), Color(0xFF294348), Color(0xFF49382C), Color(0xFF303B51),
    Color(0xFF3B472D), Color(0xFF493144), Color(0xFF2F4737), Color(0xFF48402F),
)

private val themeColors = mapOf(
    AppThemePalette.CLASSIC to ColinkColors(
        paper = Color(0xFFFFF8E5),
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
        paper = Color(0xFFEAF7EF),
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
        paper = Color(0xFFEAF4FB),
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
    AppThemePalette.SUNSET to ColinkColors(
        paper = Color(0xFFFFEFE7),
        paperLight = Color(0xFFFFFFFF),
        ink = Color(0xFF342825),
        green = Color(0xFF9A5147),
        greenSoft = Color(0xFFF4DED8),
        moss = Color(0xFF78625C),
        sand = Color(0xFFF3E3D4),
        line = Color(0xFFEDE1DD),
        surfaceVariant = Color(0xFFF6ECE8),
        outline = Color(0xFF8A756E),
        courseAccents = sunsetCourseAccents,
        courseTints = sunsetCourseTints,
    ),
    AppThemePalette.LAVENDER to ColinkColors(
        paper = Color(0xFFF0ECF8),
        paperLight = Color(0xFFFFFFFF),
        ink = Color(0xFF292833),
        green = Color(0xFF625C7C),
        greenSoft = Color(0xFFE8E4F0),
        moss = Color(0xFF6D697B),
        sand = Color(0xFFF1EBD9),
        line = Color(0xFFE6E3EC),
        surfaceVariant = Color(0xFFF1EFF6),
        outline = Color(0xFF787386),
        courseAccents = lavenderCourseAccents,
        courseTints = lavenderCourseTints,
    ),
    AppThemePalette.NIGHT to ColinkColors(
        paper = Color(0xFF151A21),
        paperLight = Color(0xFF202832),
        ink = Color(0xFFF2F4F7),
        green = Color(0xFF9AD5B7),
        greenSoft = Color(0xFF30463C),
        moss = Color(0xFFB5C0C8),
        sand = Color(0xFF4A402A),
        line = Color(0xFF39434E),
        surfaceVariant = Color(0xFF28313B),
        outline = Color(0xFF8996A2),
        courseAccents = nightCourseAccents,
        courseTints = nightCourseTints,
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
fun ColinkTheme(
    palette: AppThemePalette,
    backgroundMode: AppBackgroundMode = AppBackgroundMode.THEME,
    customBackgroundArgb: Int = DEFAULT_CUSTOM_BACKGROUND_ARGB,
    content: @Composable () -> Unit,
) {
    val baseColors = themeColors.getValue(palette)
    val background = when (backgroundMode) {
        AppBackgroundMode.THEME -> baseColors.paper
        AppBackgroundMode.CUSTOM -> Color(customBackgroundArgb)
        AppBackgroundMode.DYNAMIC -> baseColors.paper
    }
    val isDark = if (backgroundMode == AppBackgroundMode.CUSTOM) {
        background.luminance() < 0.42f
    } else {
        palette.isDark
    }
    val colors = if (backgroundMode == AppBackgroundMode.CUSTOM) {
        val ink = if (isDark) Color(0xFFF3F5F7) else Color(0xFF20242A)
        val surface = lerp(background, Color.White, if (isDark) 0.12f else 0.78f)
        baseColors.copy(
            paper = background,
            paperLight = surface,
            ink = ink,
            green = if (isDark) Color(0xFF9AD5B7) else baseColors.green,
            greenSoft = lerp(background, if (isDark) Color(0xFF9AD5B7) else baseColors.green, 0.18f),
            moss = if (isDark) Color(0xFFB8C2CB) else Color(0xFF626B70),
            line = lerp(background, ink, 0.12f),
            surfaceVariant = lerp(surface, background, 0.22f),
            outline = if (isDark) Color(0xFF98A4AE) else Color(0xFF777F84),
        )
    } else {
        baseColors.copy(paper = background)
    }
    val scheme = if (isDark) darkColorScheme(
        primary = colors.green,
        onPrimary = Color(0xFF17211B),
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
    ) else lightColorScheme(
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

@Composable
fun appBackgroundBrush(
    palette: AppThemePalette,
    mode: AppBackgroundMode,
    customBackgroundArgb: Int,
): Brush = when (mode) {
    AppBackgroundMode.THEME -> SolidColor(themeColors.getValue(palette).paper)
    AppBackgroundMode.CUSTOM -> SolidColor(Color(customBackgroundArgb))
    AppBackgroundMode.DYNAMIC -> SolidColor(animatedBackgroundColor(palette))
}

@Composable
private fun animatedBackgroundColor(palette: AppThemePalette): Color {
    val colors = dynamicBackgroundColors(palette)
    val transition = rememberInfiniteTransition(label = "backgroundMotion")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = colors.size.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 18000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "backgroundPhase",
    )
    val index = phase.toInt().coerceIn(0, colors.lastIndex)
    return lerp(colors[index], colors[(index + 1) % colors.size], phase - index)
}

private fun dynamicBackgroundColors(palette: AppThemePalette): List<Color> = when (palette) {
    AppThemePalette.CLASSIC -> listOf(Color(0xFFFFF8E5), Color(0xFFEFF4E2), Color(0xFFF8EDE2))
    AppThemePalette.MINT -> listOf(Color(0xFFEAF7EF), Color(0xFFE5F2EF), Color(0xFFEDF5E5))
    AppThemePalette.OCEAN -> listOf(Color(0xFFEAF4FB), Color(0xFFE5F0F6), Color(0xFFE8F3F1))
    AppThemePalette.SUNSET -> listOf(Color(0xFFFFEFE7), Color(0xFFF9E9E5), Color(0xFFF8EFDF))
    AppThemePalette.LAVENDER -> listOf(Color(0xFFF0ECF8), Color(0xFFEBEFF8), Color(0xFFF6EAF1))
    AppThemePalette.NIGHT -> listOf(Color(0xFF151A21), Color(0xFF1B202B), Color(0xFF17231F))
}

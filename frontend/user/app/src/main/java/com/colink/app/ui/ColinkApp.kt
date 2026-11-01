package com.colink.app.ui

/* Navigation styling adapted from ShiGuangSchedule v2.1.0 (Apache-2.0). */

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.ViewWeek
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.ViewAgenda
import androidx.compose.material.icons.outlined.ViewWeek
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.colink.app.UserProfile
import com.colink.app.data.CourseLoadState
import com.colink.app.data.ScheduleLoadResult
import com.colink.app.data.SemesterMeta
import com.colink.app.data.SharedPrefsLocalScheduleStore
import com.colink.app.data.toDisplayCourses
import com.colink.app.importflow.ImportFlowService
import com.colink.app.ui.home.HomeScreen
import com.colink.app.ui.imports.ImportScreen
import com.colink.app.ui.profile.PersonalizationSettingsScreen
import com.colink.app.ui.profile.ProfileScreen
import com.colink.app.ui.schedule.CourseCardEffect
import com.colink.app.ui.schedule.ScheduleDisplayPreferences
import com.colink.app.ui.schedule.ScheduleScreen
import com.colink.app.ui.theme.AppBackgroundMode
import com.colink.app.ui.theme.AppThemePalette
import com.colink.app.ui.theme.ColinkTheme
import com.colink.app.ui.theme.DEFAULT_CUSTOM_BACKGROUND_ARGB
import com.colink.app.ui.theme.PaperLight
import com.colink.app.ui.theme.appBackgroundBrush
import com.colink.app.ui.widget.TodayScheduleWidgetProvider
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun ColinkApp() {
    val context = LocalContext.current
    val preferences = remember { context.getSharedPreferences("colink_profile", 0) }
    var appThemePalette by remember {
        mutableStateOf(
            AppThemePalette.fromStorageKey(
                preferences.getString("appThemePalette", null) ?: preferences.getString("coursePalette", null),
            ),
        )
    }
    var backgroundMode by remember {
        mutableStateOf(AppBackgroundMode.fromStorageKey(preferences.getString("appBackgroundMode", null)))
    }
    var customBackgroundArgb by remember {
        mutableIntStateOf(preferences.getInt("customBackgroundArgb", DEFAULT_CUSTOM_BACKGROUND_ARGB))
    }
    ColinkTheme(appThemePalette, backgroundMode, customBackgroundArgb) {
        val scheduleStore = remember {
            SharedPrefsLocalScheduleStore(context.getSharedPreferences("colink_local_schedule", 0))
        }
        val importService = remember { ImportFlowService(scheduleStore) }
        val systemToday = LocalDate.now()
        val defaultSemesterStart = LocalDate.of(systemToday.year, 9, 1)
        var selectedTab by rememberSaveable { mutableIntStateOf(0) }
        var showImport by rememberSaveable { mutableStateOf(false) }
        var showPersonalization by rememberSaveable { mutableStateOf(false) }
        var profile by remember {
            mutableStateOf(
                UserProfile(
                    name = preferences.getString("name", "同学") ?: "同学",
                    studentId = preferences.getString("studentId", "2025XXXXXXXX") ?: "2025XXXXXXXX",
                    college = preferences.getString("college", "计算机科学与技术学院") ?: "计算机科学与技术学院",
                    major = preferences.getString("major", "计算机科学与技术") ?: "计算机科学与技术",
                    grade = preferences.getString("grade", "2025级") ?: "2025级",
                    className = preferences.getString("className", "25计科9(1)") ?: "25计科9(1)",
                ),
            )
        }
        var semesterStart by remember {
            mutableStateOf(
                runCatching {
                    val stored = preferences.getString("semesterStart", null)
                    stored?.let(LocalDate::parse) ?: defaultSemesterStart
                }.getOrDefault(defaultSemesterStart),
            )
        }
        var semesterStartConfigured by remember {
            mutableStateOf(
                preferences.getBoolean(
                    "semesterStartConfigured",
                    preferences.getString("semesterStart", null)?.let { it != "2025-09-01" } ?: false,
                ),
            )
        }
        var showWeekends by remember {
            mutableStateOf(preferences.getBoolean("showWeekends", true))
        }
        var displayPreferences by remember {
            val defaults = ScheduleDisplayPreferences.DEFAULT
            mutableStateOf(
                ScheduleDisplayPreferences(
                    showCourseLocation = preferences.getBoolean("showCourseLocation", defaults.showCourseLocation),
                    showCoursePeriod = preferences.getBoolean("showCoursePeriod", defaults.showCoursePeriod),
                    showDateUnderDay = preferences.getBoolean("showDateUnderDay", defaults.showDateUnderDay),
                    showGridLines = preferences.getBoolean("showGridLines", defaults.showGridLines),
                    courseCardEffect = CourseCardEffect.fromStorageKey(preferences.getString("courseCardEffect", null)),
                    rowHeightDp = preferences.getInt("scheduleRowHeightDp", defaults.rowHeightDp)
                        .takeIf { it in ScheduleDisplayPreferences.ROW_HEIGHT_OPTIONS } ?: defaults.rowHeightDp,
                    courseFontSizeSp = preferences.getInt("scheduleCourseFontSizeSp", defaults.courseFontSizeSp)
                        .takeIf { it in ScheduleDisplayPreferences.FONT_SIZE_OPTIONS } ?: defaults.courseFontSizeSp,
                    cardCornerRadiusDp = preferences.getInt("scheduleCardCornerRadiusDp", defaults.cardCornerRadiusDp)
                        .takeIf { it in ScheduleDisplayPreferences.CORNER_RADIUS_OPTIONS } ?: defaults.cardCornerRadiusDp,
                ),
            )
        }
        var courseLoadState by remember { mutableStateOf<CourseLoadState>(CourseLoadState.Loading) }
        var sourceFileName by remember { mutableStateOf<String?>(null) }
        var importedAt by remember { mutableStateOf<String?>(null) }
        var refreshTick by remember { mutableIntStateOf(0) }

        LaunchedEffect(refreshTick) {
            courseLoadState = CourseLoadState.Loading
            val result = withContext(Dispatchers.IO) { scheduleStore.load() }
            courseLoadState = when (result) {
                is ScheduleLoadResult.Ok -> {
                    val schedule = result.schedule
                    sourceFileName = schedule.sourceFileName
                    importedAt = schedule.importedAt
                    if (!semesterStartConfigured) {
                        schedule.semester.startDate?.let { storedStart ->
                            runCatching { LocalDate.parse(storedStart) }.getOrNull()?.let { parsed ->
                                if (parsed != semesterStart) {
                                    semesterStart = parsed
                                    preferences.edit().putString("semesterStart", parsed.toString()).apply()
                                }
                            }
                        }
                    }
                    CourseLoadState.Ready(
                        classId = "local",
                        className = schedule.semester.className ?: profile.className,
                        courses = schedule.toDisplayCourses(),
                        semester = SemesterMeta(
                            startDate = schedule.semester.startDate,
                            maxWeek = schedule.semester.maxWeek,
                        ),
                    )
                }
                is ScheduleLoadResult.Empty -> {
                    sourceFileName = null
                    importedAt = null
                    CourseLoadState.Ready(
                        classId = "local",
                        className = profile.className,
                        courses = emptyList(),
                        semester = SemesterMeta(),
                    )
                }
                is ScheduleLoadResult.Corrupt -> {
                    sourceFileName = null
                    importedAt = null
                    CourseLoadState.Error("课表数据损坏，请重新导入")
                }
            }
            TodayScheduleWidgetProvider.refreshAll(context)
        }

        BackHandler(enabled = showPersonalization) {
            showPersonalization = false
        }

        Box(
            Modifier.fillMaxSize().background(
                appBackgroundBrush(appThemePalette, backgroundMode, customBackgroundArgb),
            ),
        ) {
            Scaffold(
                containerColor = Color.Transparent,
                bottomBar = {
                    if (!showImport && !showPersonalization) {
                        NavigationBar(containerColor = PaperLight) {
                            NavigationBarItem(
                                selected = selectedTab == 0,
                                onClick = { selectedTab = 0 },
                                icon = { Icon(if (selectedTab == 0) Icons.Filled.ViewAgenda else Icons.Outlined.ViewAgenda, contentDescription = "今日课表") },
                                label = { Text("今日课表") },
                            )
                            NavigationBarItem(
                                selected = selectedTab == 1,
                                onClick = { selectedTab = 1 },
                                icon = { Icon(if (selectedTab == 1) Icons.Filled.ViewWeek else Icons.Outlined.ViewWeek, contentDescription = "课表") },
                                label = { Text("课表") },
                            )
                            NavigationBarItem(
                                selected = selectedTab == 2,
                                onClick = { selectedTab = 2 },
                                icon = { Icon(if (selectedTab == 2) Icons.Filled.AccountCircle else Icons.Outlined.AccountCircle, contentDescription = "我的") },
                                label = { Text("我的") },
                            )
                        }
                    }
                },
            ) { padding ->
                if (showImport) {
                    ImportScreen(
                        service = importService,
                        onDone = {
                            showImport = false
                            refreshTick += 1
                        },
                        onClose = {
                            showImport = false
                            refreshTick += 1
                        },
                        onStartDateConfirmed = { date ->
                            semesterStart = date
                            semesterStartConfigured = true
                            preferences.edit()
                                .putString("semesterStart", date.toString())
                                .putBoolean("semesterStartConfigured", true)
                                .apply()
                        },
                        modifier = Modifier.padding(padding),
                    )
                } else {
                    AppContent(
                        selectedTab = selectedTab,
                        onSelectTab = { selectedTab = it },
                        padding = padding,
                        profile = profile,
                        semesterStart = semesterStart,
                        showWeekends = showWeekends,
                        displayPreferences = displayPreferences,
                        onDisplayPreferencesChange = { updated ->
                            displayPreferences = updated
                            preferences.edit()
                                .putBoolean("showCourseLocation", updated.showCourseLocation)
                                .putBoolean("showCoursePeriod", updated.showCoursePeriod)
                                .putBoolean("showDateUnderDay", updated.showDateUnderDay)
                                .putBoolean("showGridLines", updated.showGridLines)
                                .putString("courseCardEffect", updated.courseCardEffect.storageKey)
                                .putInt("scheduleRowHeightDp", updated.rowHeightDp)
                                .putInt("scheduleCourseFontSizeSp", updated.courseFontSizeSp)
                                .putInt("scheduleCardCornerRadiusDp", updated.cardCornerRadiusDp)
                                .apply()
                        },
                        onShowWeekendsChange = { enabled ->
                            showWeekends = enabled
                            preferences.edit().putBoolean("showWeekends", enabled).apply()
                        },
                        appThemePalette = appThemePalette,
                        backgroundMode = backgroundMode,
                        customBackgroundArgb = customBackgroundArgb,
                        onBackgroundModeChange = { mode ->
                            backgroundMode = mode
                            preferences.edit().putString("appBackgroundMode", mode.storageKey).apply()
                        },
                        onCustomBackgroundChange = { argb ->
                            customBackgroundArgb = argb
                            preferences.edit().putInt("customBackgroundArgb", argb).apply()
                        },
                        onAppThemePaletteChange = { selectedPalette ->
                            appThemePalette = selectedPalette
                            preferences.edit()
                                .putString("appThemePalette", selectedPalette.storageKey)
                                .remove("coursePalette")
                                .apply()
                        },
                        onSemesterStartChange = { date ->
                            semesterStart = date
                            semesterStartConfigured = true
                            preferences.edit()
                                .putString("semesterStart", date.toString())
                                .putBoolean("semesterStartConfigured", true)
                                .apply()
                        },
                        maxWeek = (courseLoadState as? CourseLoadState.Ready)?.semester?.maxWeek ?: 32,
                        courseLoadState = courseLoadState,
                        showPersonalization = showPersonalization,
                        onOpenPersonalization = { showPersonalization = true },
                        onClosePersonalization = { showPersonalization = false },
                        onRetry = { refreshTick += 1 },
                        onImportClick = { showImport = true },
                        sourceFileName = sourceFileName,
                        importedAt = importedAt,
                        onSaveProfile = { updated ->
                            profile = updated
                            preferences.edit()
                                .putString("name", updated.name)
                                .putString("studentId", updated.studentId)
                                .putString("college", updated.college)
                                .putString("major", updated.major)
                                .putString("grade", updated.grade)
                                .putString("className", updated.className)
                                .apply()
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun AppContent(
    selectedTab: Int,
    onSelectTab: (Int) -> Unit,
    padding: PaddingValues,
    profile: UserProfile,
    semesterStart: LocalDate,
    showWeekends: Boolean,
    displayPreferences: ScheduleDisplayPreferences,
    onDisplayPreferencesChange: (ScheduleDisplayPreferences) -> Unit,
    onShowWeekendsChange: (Boolean) -> Unit,
    appThemePalette: AppThemePalette,
    backgroundMode: AppBackgroundMode,
    customBackgroundArgb: Int,
    onBackgroundModeChange: (AppBackgroundMode) -> Unit,
    onCustomBackgroundChange: (Int) -> Unit,
    onAppThemePaletteChange: (AppThemePalette) -> Unit,
    onSemesterStartChange: (LocalDate) -> Unit,
    maxWeek: Int,
    courseLoadState: CourseLoadState,
    showPersonalization: Boolean,
    onOpenPersonalization: () -> Unit,
    onClosePersonalization: () -> Unit,
    onRetry: () -> Unit,
    onImportClick: () -> Unit,
    sourceFileName: String?,
    importedAt: String?,
    onSaveProfile: (UserProfile) -> Unit,
) {
    when {
        showPersonalization -> PersonalizationSettingsScreen(
            appThemePalette = appThemePalette,
            onAppThemePaletteChange = onAppThemePaletteChange,
            backgroundMode = backgroundMode,
            onBackgroundModeChange = onBackgroundModeChange,
            customBackgroundArgb = customBackgroundArgb,
            onCustomBackgroundChange = onCustomBackgroundChange,
            showWeekends = showWeekends,
            onShowWeekendsChange = onShowWeekendsChange,
            displayPreferences = displayPreferences,
            onDisplayPreferencesChange = onDisplayPreferencesChange,
            onBack = onClosePersonalization,
            modifier = Modifier.padding(padding),
        )
        selectedTab == 0 -> HomeScreen(
            semesterStart = semesterStart,
            maxWeek = maxWeek,
            loadState = courseLoadState,
            onRetry = onRetry,
            onOpenSchedule = { onSelectTab(1) },
            onImportClick = onImportClick,
            appThemePalette = appThemePalette,
            displayPreferences = displayPreferences,
            modifier = Modifier.padding(padding),
        )
        selectedTab == 1 -> ScheduleScreen(
            semesterStart = semesterStart,
            maxWeek = maxWeek,
            loadState = courseLoadState,
            onRetry = onRetry,
            onImportClick = onImportClick,
            showWeekends = showWeekends,
            appThemePalette = appThemePalette,
            displayPreferences = displayPreferences,
            modifier = Modifier.padding(padding),
        )
        else -> ProfileScreen(
            profile = profile,
            semesterStart = semesterStart,
            onSemesterStartChange = onSemesterStartChange,
            onSave = onSaveProfile,
            sourceFileName = sourceFileName,
            importedAt = importedAt,
            onImportClick = onImportClick,
            onOpenPersonalization = onOpenPersonalization,
            modifier = Modifier.padding(padding),
        )
    }
}

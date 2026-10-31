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
import com.colink.app.ui.profile.ProfileScreen
import com.colink.app.ui.schedule.ScheduleScreen
import com.colink.app.ui.theme.AppThemePalette
import com.colink.app.ui.theme.ColinkTheme
import com.colink.app.ui.theme.Paper
import com.colink.app.ui.theme.PaperLight
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
    ColinkTheme(appThemePalette) {
        val scheduleStore = remember {
            SharedPrefsLocalScheduleStore(context.getSharedPreferences("colink_local_schedule", 0))
        }
        val importService = remember { ImportFlowService(scheduleStore) }
        val systemToday = LocalDate.now()
        val defaultSemesterStart = LocalDate.of(systemToday.year, 9, 1)
        var selectedTab by rememberSaveable { mutableIntStateOf(0) }
        var showImport by rememberSaveable { mutableStateOf(false) }
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
        }

        Box(Modifier.fillMaxSize().background(Paper)) {
            Scaffold(
                containerColor = Color.Transparent,
                bottomBar = {
                    if (!showImport) {
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
                        onShowWeekendsChange = { enabled ->
                            showWeekends = enabled
                            preferences.edit().putBoolean("showWeekends", enabled).apply()
                        },
                        appThemePalette = appThemePalette,
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
    onShowWeekendsChange: (Boolean) -> Unit,
    appThemePalette: AppThemePalette,
    onAppThemePaletteChange: (AppThemePalette) -> Unit,
    onSemesterStartChange: (LocalDate) -> Unit,
    maxWeek: Int,
    courseLoadState: CourseLoadState,
    onRetry: () -> Unit,
    onImportClick: () -> Unit,
    sourceFileName: String?,
    importedAt: String?,
    onSaveProfile: (UserProfile) -> Unit,
) {
    when (selectedTab) {
        0 -> HomeScreen(
            semesterStart = semesterStart,
            maxWeek = maxWeek,
            loadState = courseLoadState,
            onRetry = onRetry,
            onOpenSchedule = { onSelectTab(1) },
            onImportClick = onImportClick,
            appThemePalette = appThemePalette,
            modifier = Modifier.padding(padding),
        )
        1 -> ScheduleScreen(
            semesterStart = semesterStart,
            maxWeek = maxWeek,
            loadState = courseLoadState,
            onRetry = onRetry,
            onImportClick = onImportClick,
            showWeekends = showWeekends,
            appThemePalette = appThemePalette,
            modifier = Modifier.padding(padding),
        )
        else -> ProfileScreen(
            profile = profile,
            semesterStart = semesterStart,
            onSemesterStartChange = onSemesterStartChange,
            showWeekends = showWeekends,
            onShowWeekendsChange = onShowWeekendsChange,
            appThemePalette = appThemePalette,
            onAppThemePaletteChange = onAppThemePaletteChange,
            onSave = onSaveProfile,
            sourceFileName = sourceFileName,
            importedAt = importedAt,
            onImportClick = onImportClick,
            modifier = Modifier.padding(padding),
        )
    }
}

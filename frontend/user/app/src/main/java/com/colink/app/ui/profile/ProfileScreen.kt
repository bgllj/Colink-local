package com.colink.app.ui.profile

/* Settings layout adapted from ShiGuangSchedule v2.1.0 (Apache-2.0). */

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.IconButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.colink.app.UserProfile
import com.colink.app.ui.schedule.CourseCardEffect
import com.colink.app.ui.schedule.ScheduleDisplayPreferences
import com.colink.app.ui.theme.AppBackgroundMode
import com.colink.app.ui.theme.AppThemePalette
import com.colink.app.ui.theme.Green
import com.colink.app.ui.theme.GreenSoft
import com.colink.app.ui.theme.Ink
import com.colink.app.ui.theme.Line
import com.colink.app.ui.theme.Moss
import com.colink.app.ui.theme.Paper
import com.colink.app.ui.theme.PaperLight
import com.colink.app.ui.theme.courseAccent
import com.colink.app.ui.theme.courseTint
import com.colink.app.ui.widget.TodayScheduleWidgetProvider
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    profile: UserProfile,
    semesterStart: LocalDate,
    onSemesterStartChange: (LocalDate) -> Unit,
    onSave: (UserProfile) -> Unit,
    modifier: Modifier = Modifier,
    sourceFileName: String? = null,
    importedAt: String? = null,
    onImportClick: () -> Unit = {},
    onOpenPersonalization: () -> Unit,
) {
    var editing by rememberSaveable { mutableStateOf(false) }
    var editingSemesterStart by rememberSaveable { mutableStateOf(false) }
    var draft by remember(profile) { mutableStateOf(profile) }
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, top = 4.dp, end = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Text(
                "我的",
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.titleLarge,
                color = Ink,
                fontWeight = FontWeight.Bold,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
        }
        item { ProfileHero(profile) }
        item {
            AnimatedContent(
                targetState = editing,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "profileMode",
            ) { isEditing ->
                if (isEditing) {
                    ProfileEditor(
                        draft = draft,
                        onDraftChanged = { draft = it },
                        onCancel = { draft = profile; editing = false },
                        onSave = { onSave(draft); editing = false },
                    )
                } else {
                    ProfileOverview(
                        profile = profile,
                        semesterStart = semesterStart,
                        sourceFileName = sourceFileName,
                        importedAt = importedAt,
                        onEdit = { editing = true },
                        onEditSemesterStart = { editingSemesterStart = true },
                        onImportClick = onImportClick,
                        onOpenPersonalization = onOpenPersonalization,
                    )
                }
            }
        }
    }
    if (editingSemesterStart) {
        val state = rememberDatePickerState(initialSelectedDateMillis = semesterStart.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli())
        DatePickerDialog(
            onDismissRequest = { editingSemesterStart = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { millis -> onSemesterStartChange(Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()) }
                    editingSemesterStart = false
                }) { Text("确定") }
            },
            dismissButton = { TextButton(onClick = { editingSemesterStart = false }) { Text("取消") } },
        ) { DatePicker(state = state) }
    }
}

@Composable
private fun ProfileHero(profile: UserProfile) {
    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = PaperLight), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(horizontal = 16.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = CircleShape, color = GreenSoft, modifier = Modifier.size(48.dp)) {
                Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Person, contentDescription = null, tint = Green, modifier = Modifier.size(24.dp)) }
            }
            Spacer(Modifier.size(13.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(profile.name.ifBlank { "同学" }, style = MaterialTheme.typography.titleMedium, color = Ink, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text("个人信息与课表设置", style = MaterialTheme.typography.bodySmall, color = Moss)
            }
        }
    }
}

@Composable
private fun ProfileOverview(
    profile: UserProfile,
    semesterStart: LocalDate,
    sourceFileName: String?,
    importedAt: String?,
    onEdit: () -> Unit,
    onEditSemesterStart: () -> Unit,
    onImportClick: () -> Unit,
    onOpenPersonalization: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
        SettingsSection(title = "个人设置") {
            SettingsRow(
                icon = Icons.Default.Person,
                title = "显示名称",
                description = "用于首页问候",
                onClick = onEdit,
                trailing = "编辑",
            )
        }
        SettingsSection(title = "课表与学期") {
            Column {
                SettingsRow(
                    icon = Icons.Default.CalendarMonth,
                    title = "开学时间",
                    description = semesterStart.format(DateTimeFormatter.ofPattern("yyyy年M月d日")),
                    onClick = onEditSemesterStart,
                )
                androidx.compose.material3.HorizontalDivider(color = Line, modifier = Modifier.padding(start = 68.dp, end = 16.dp))
                SettingsRow(
                    icon = Icons.Default.FolderOpen,
                    title = "课表文件",
                    description = sourceFileName?.let { "$it · ${formatImportedAt(importedAt)} 导入" } ?: "尚未导入课表",
                    onClick = onImportClick,
                    trailing = if (sourceFileName == null) "导入" else "更换",
                )
            }
        }
        PersonalizationEntryButton(onClick = onOpenPersonalization)
        Text("可在此管理个人资料与课程安排。", modifier = Modifier.padding(horizontal = 2.dp), style = MaterialTheme.typography.bodySmall, color = Moss)
    }
}

@Composable
private fun PersonalizationEntryButton(onClick: () -> Unit) {
    Surface(shape = RoundedCornerShape(12.dp), color = PaperLight, modifier = Modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 15.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(shape = RoundedCornerShape(11.dp), color = GreenSoft, modifier = Modifier.size(40.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Palette, contentDescription = null, tint = Green, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(Modifier.size(13.dp))
            Column(Modifier.weight(1f)) {
                Text("个性化设置", style = MaterialTheme.typography.bodyLarge, color = Ink, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(3.dp))
                Text("主题、课表显示与课程卡片样式", style = MaterialTheme.typography.bodySmall, color = Moss)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = "打开个性化设置", tint = Moss, modifier = Modifier.size(20.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalizationSettingsScreen(
    appThemePalette: AppThemePalette,
    onAppThemePaletteChange: (AppThemePalette) -> Unit,
    backgroundMode: AppBackgroundMode,
    onBackgroundModeChange: (AppBackgroundMode) -> Unit,
    customBackgroundArgb: Int,
    onCustomBackgroundChange: (Int) -> Unit,
    showWeekends: Boolean,
    onShowWeekendsChange: (Boolean) -> Unit,
    displayPreferences: ScheduleDisplayPreferences,
    onDisplayPreferencesChange: (ScheduleDisplayPreferences) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("个性化设置", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item { AddTodayWidgetPreference() }
            item {
                SettingsSection(title = "主题") {
                    ThemePalettePreference(appThemePalette, onAppThemePaletteChange)
                }
            }
            item {
                SettingsSection(title = "页面背景") {
                    BackgroundPreference(
                        mode = backgroundMode,
                        customBackgroundArgb = customBackgroundArgb,
                        onModeChange = onBackgroundModeChange,
                        onCustomBackgroundChange = onCustomBackgroundChange,
                    )
                }
            }
            item { ThemeCardPreview(appThemePalette, displayPreferences) }
            item {
                SettingsSection(title = "课程卡片") {
                    Column {
                        CourseCardEffectPreference(
                            effect = displayPreferences.courseCardEffect,
                            palette = appThemePalette,
                            onEffectChange = { onDisplayPreferencesChange(displayPreferences.copy(courseCardEffect = it)) },
                        )
                        androidx.compose.material3.HorizontalDivider(color = Line, modifier = Modifier.padding(horizontal = 16.dp))
                        DisplayPreferenceOptions(
                            title = "课程字体",
                            description = "影响周视图和今日课程卡片的文字大小",
                            options = listOf(10 to "小", 11 to "标准", 12 to "大"),
                            selected = displayPreferences.courseFontSizeSp,
                            onSelected = { onDisplayPreferencesChange(displayPreferences.copy(courseFontSizeSp = it)) },
                        )
                        androidx.compose.material3.HorizontalDivider(color = Line, modifier = Modifier.padding(horizontal = 16.dp))
                        DisplayPreferenceOptions(
                            title = "卡片圆角",
                            description = "调整课程卡片的边角样式",
                            options = listOf(4 to "直角", 8 to "柔和", 12 to "圆润"),
                            selected = displayPreferences.cardCornerRadiusDp,
                            onSelected = { onDisplayPreferencesChange(displayPreferences.copy(cardCornerRadiusDp = it)) },
                        )
                        androidx.compose.material3.HorizontalDivider(color = Line, modifier = Modifier.padding(horizontal = 16.dp))
                        DisplayPreferenceSwitch(
                            title = "显示上课地点",
                            description = "在今日和周课表的课程卡片中显示地点",
                            checked = displayPreferences.showCourseLocation,
                            onCheckedChange = { onDisplayPreferencesChange(displayPreferences.copy(showCourseLocation = it)) },
                        )
                        DisplayPreferenceSwitch(
                            title = "卡片显示节次",
                            description = "在周课表课程卡片中显示节次范围",
                            checked = displayPreferences.showCoursePeriod,
                            onCheckedChange = { onDisplayPreferencesChange(displayPreferences.copy(showCoursePeriod = it)) },
                        )
                    }
                }
            }
            item {
                SettingsSection(title = "课表显示") {
                    Column {
                        WeekendPreferenceRow(showWeekends, onShowWeekendsChange)
                        androidx.compose.material3.HorizontalDivider(color = Line, modifier = Modifier.padding(horizontal = 16.dp))
                        DisplayPreferenceOptions(
                            title = "课表密度",
                            description = "调整周课表节次高度和今日课程卡片高度",
                            options = listOf(48 to "紧凑", 60 to "标准", 72 to "宽松"),
                            selected = displayPreferences.rowHeightDp,
                            onSelected = { onDisplayPreferencesChange(displayPreferences.copy(rowHeightDp = it)) },
                        )
                        androidx.compose.material3.HorizontalDivider(color = Line, modifier = Modifier.padding(horizontal = 16.dp))
                        DisplayPreferenceSwitch(
                            title = "显示日期",
                            description = "在周视图星期名称下方显示月日",
                            checked = displayPreferences.showDateUnderDay,
                            onCheckedChange = { onDisplayPreferencesChange(displayPreferences.copy(showDateUnderDay = it)) },
                        )
                        DisplayPreferenceSwitch(
                            title = "显示网格线",
                            description = "显示星期与节次之间的分隔线",
                            checked = displayPreferences.showGridLines,
                            onCheckedChange = { onDisplayPreferencesChange(displayPreferences.copy(showGridLines = it)) },
                        )
                    }
                }
            }
            item {
                TextButton(
                    onClick = { onDisplayPreferencesChange(ScheduleDisplayPreferences.DEFAULT) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("恢复卡片与课表显示默认值") }
            }
        }
    }
}

@Composable
private fun AddTodayWidgetPreference() {
    val context = LocalContext.current
    SettingsSection(title = "桌面组件") {
        OutlinedButton(
            onClick = {
                val manager = AppWidgetManager.getInstance(context)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && manager.isRequestPinAppWidgetSupported) {
                    val callback = android.app.PendingIntent.getBroadcast(
                        context,
                        0,
                        Intent(context, com.colink.app.ui.widget.WidgetPinResultReceiver::class.java).apply {
                            action = com.colink.app.ui.widget.WidgetPinResultReceiver.ACTION_PIN_RESULT
                        },
                        android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE,
                    )
                    val accepted = manager.requestPinAppWidget(
                        ComponentName(context, TodayScheduleWidgetProvider::class.java),
                        null,
                        callback,
                    )
                    val message = if (accepted) "已发送桌面添加请求，请在桌面确认添加" else "桌面未接受添加请求"
                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "当前桌面不支持快速添加组件", Toast.LENGTH_LONG).show()
                }
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(Icons.Default.Widgets, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("添加今日课表组件")
        }
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium, color = Ink, fontWeight = FontWeight.SemiBold)
        Surface(shape = RoundedCornerShape(12.dp), color = PaperLight, modifier = Modifier.fillMaxWidth()) { content() }
    }
}

@Composable
private fun SettingsRow(icon: ImageVector, title: String, description: String, onClick: () -> Unit, trailing: String? = null) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 15.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(shape = RoundedCornerShape(11.dp), color = GreenSoft, modifier = Modifier.size(40.dp)) {
            Box(contentAlignment = Alignment.Center) { Icon(icon, contentDescription = null, tint = Green, modifier = Modifier.size(20.dp)) }
        }
        Spacer(Modifier.size(13.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, color = Ink, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(3.dp))
            Text(description, style = MaterialTheme.typography.bodySmall, color = Moss, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
        if (trailing != null) {
            Text(trailing, style = MaterialTheme.typography.labelMedium, color = Green, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.size(4.dp))
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Moss, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun WeekendPreferenceRow(showWeekends: Boolean, onShowWeekendsChange: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 15.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text("显示周末", style = MaterialTheme.typography.bodyLarge, color = Ink, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(3.dp))
            Text(
                if (showWeekends) "周视图显示周一至周日" else "周视图仅显示周一至周五",
                style = MaterialTheme.typography.bodySmall,
                color = Moss,
            )
        }
        Switch(checked = showWeekends, onCheckedChange = onShowWeekendsChange)
    }
}

@Composable
private fun ThemePalettePreference(palette: AppThemePalette, onPaletteChange: (AppThemePalette) -> Unit) {
    Column(
        Modifier.fillMaxWidth().padding(horizontal = 15.dp, vertical = 13.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("选择配色", style = MaterialTheme.typography.bodyLarge, color = Ink, fontWeight = FontWeight.SemiBold)
        Text("应用于页面背景、控件和课程卡片", style = MaterialTheme.typography.bodySmall, color = Moss)
        AppThemePalette.values().toList().chunked(3).forEach { rowOptions ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowOptions.forEach { option ->
                    val selected = option == palette
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (selected) GreenSoft else PaperLight,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .border(1.dp, if (selected) Green else Line, RoundedCornerShape(8.dp))
                            .clickable { onPaletteChange(option) },
                    ) {
                        Row(
                            Modifier.padding(horizontal = 8.dp, vertical = 7.dp),
                            horizontalArrangement = Arrangement.spacedBy(7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Surface(shape = CircleShape, color = option.previewColor, modifier = Modifier.size(18.dp)) {}
                            Text(option.label, style = MaterialTheme.typography.labelMedium, color = Ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
                repeat(3 - rowOptions.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun ThemeCardPreview(palette: AppThemePalette, preferences: ScheduleDisplayPreferences) {
    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Text("课程卡片预览", style = MaterialTheme.typography.titleSmall, color = Ink, fontWeight = FontWeight.SemiBold)
        Card(
            shape = RoundedCornerShape(preferences.cardCornerRadiusDp.dp),
            colors = CardDefaults.cardColors(containerColor = courseTint("theme-preview", palette)),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                if (preferences.courseCardEffect == CourseCardEffect.ACCENT_STRIPE) {
                    Box(Modifier.width(4.dp).fillMaxHeight().background(courseAccent("theme-preview", palette)))
                }
                Column(
                    Modifier.weight(1f).padding(
                        start = 12.dp,
                        end = 12.dp,
                        top = (preferences.rowHeightDp / 6).coerceIn(8, 12).dp,
                        bottom = (preferences.rowHeightDp / 6).coerceIn(8, 12).dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    if (preferences.showDateUnderDay) {
                        Text("星期一 · 9月1日", style = MaterialTheme.typography.labelSmall, color = Ink.copy(alpha = 0.7f))
                    }
                    Text(
                        "高等数学",
                        style = TextStyle(
                            fontSize = preferences.courseFontSizeSp.sp,
                            lineHeight = (preferences.courseFontSizeSp + 2).sp,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = Ink,
                    )
                    if (preferences.showCoursePeriod) {
                        Text(
                            "第 1–2 节",
                            style = TextStyle(fontSize = (preferences.courseFontSizeSp - 2).coerceAtLeast(8).sp),
                            color = Ink.copy(alpha = 0.78f),
                        )
                    }
                    if (preferences.showCourseLocation) {
                        Text(
                            "@教学楼 A203",
                            style = TextStyle(fontSize = (preferences.courseFontSizeSp - 2).coerceAtLeast(8).sp),
                            color = Ink.copy(alpha = 0.78f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BackgroundPreference(
    mode: AppBackgroundMode,
    customBackgroundArgb: Int,
    onModeChange: (AppBackgroundMode) -> Unit,
    onCustomBackgroundChange: (Int) -> Unit,
) {
    val hexColors = "0123456789ABCDEF"
    var hexInput by remember(customBackgroundArgb) {
        mutableStateOf("%06X".format(customBackgroundArgb and 0x00FFFFFF))
    }
    val presets = listOf(
        0xFFF1F3EF.toInt(),
        0xFFFFF1DD.toInt(),
        0xFFEAF4FB.toInt(),
        0xFFF0ECF8.toInt(),
        0xFF202832.toInt(),
        0xFF263B34.toInt(),
    )
    Column(
        Modifier.fillMaxWidth().padding(horizontal = 15.dp, vertical = 13.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text("背景样式", style = MaterialTheme.typography.bodyLarge, color = Ink, fontWeight = FontWeight.SemiBold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AppBackgroundMode.values().forEach { option ->
                val selected = option == mode
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (selected) GreenSoft else Paper,
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, if (selected) Green else Line, RoundedCornerShape(8.dp))
                        .clickable { onModeChange(option) },
                ) {
                    Box(Modifier.padding(horizontal = 8.dp, vertical = 10.dp), contentAlignment = Alignment.Center) {
                        Text(option.label, style = MaterialTheme.typography.labelLarge, color = Ink)
                    }
                }
            }
        }
        Text(
            when (mode) {
                AppBackgroundMode.THEME -> "背景跟随当前主题配色"
                AppBackgroundMode.CUSTOM -> "选择预设色或输入十六进制颜色"
                AppBackgroundMode.DYNAMIC -> "背景颜色缓慢循环变化，可搭配任意主题使用"
            },
            style = MaterialTheme.typography.bodySmall,
            color = Moss,
        )
        if (mode == AppBackgroundMode.CUSTOM) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                presets.forEach { argb ->
                    val selected = argb == customBackgroundArgb
                    Surface(
                        shape = CircleShape,
                        color = Color(argb),
                        modifier = Modifier
                            .size(30.dp)
                            .border(2.dp, if (selected) Green else Line, CircleShape)
                            .clickable {
                                hexInput = "%06X".format(argb and 0x00FFFFFF)
                                onCustomBackgroundChange(argb)
                            },
                    ) {}
                }
            }
            OutlinedTextField(
                value = hexInput,
                onValueChange = { input ->
                    val normalized = input.uppercase().filter { it in hexColors }.take(6)
                    hexInput = normalized
                    if (normalized.length == 6) {
                        normalized.toLongOrNull(16)?.let { value ->
                            onCustomBackgroundChange((0xFF000000L or value).toInt())
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("背景颜色") },
                leadingIcon = { Text("#", color = Moss) },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

@Composable
private fun CourseCardEffectPreference(
    effect: CourseCardEffect,
    palette: AppThemePalette,
    onEffectChange: (CourseCardEffect) -> Unit,
) {
    Column(
        Modifier.fillMaxWidth().padding(horizontal = 15.dp, vertical = 13.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        Text("课程卡片效果", style = MaterialTheme.typography.bodyLarge, color = Ink, fontWeight = FontWeight.SemiBold)
        Text("让主题色以不同方式出现在课程卡片上", style = MaterialTheme.typography.bodySmall, color = Moss)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CourseCardEffect.values().forEach { option ->
                val selected = option == effect
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (selected) GreenSoft else Paper,
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, if (selected) Green else Line, RoundedCornerShape(8.dp))
                        .clickable { onEffectChange(option) },
                ) {
                    Row(
                        Modifier.padding(horizontal = 8.dp, vertical = 11.dp),
                        horizontalArrangement = Arrangement.spacedBy(7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            Modifier.size(width = 30.dp, height = 20.dp)
                                .background(courseTint("effect-preview", palette), RoundedCornerShape(4.dp)),
                        ) {
                            if (option == CourseCardEffect.ACCENT_STRIPE) {
                                Box(Modifier.width(4.dp).fillMaxHeight().background(courseAccent("effect-preview", palette)))
                            }
                        }
                        Text(option.label, style = MaterialTheme.typography.labelMedium, color = Ink, maxLines = 1)
                    }
                }
            }
        }
    }
}

@Composable
private fun DisplayPreferenceSwitch(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 15.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, color = Ink, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(3.dp))
            Text(description, style = MaterialTheme.typography.bodySmall, color = Moss)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun DisplayPreferenceOptions(
    title: String,
    description: String,
    options: List<Pair<Int, String>>,
    selected: Int,
    onSelected: (Int) -> Unit,
) {
    Column(
        Modifier.fillMaxWidth().padding(horizontal = 15.dp, vertical = 13.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        Text(title, style = MaterialTheme.typography.bodyLarge, color = Ink, fontWeight = FontWeight.SemiBold)
        Text(description, style = MaterialTheme.typography.bodySmall, color = Moss)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            options.forEach { (value, label) ->
                val isSelected = value == selected
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) Green else Paper,
                    modifier = Modifier.weight(1f).clickable { onSelected(value) },
                ) {
                    Box(Modifier.padding(horizontal = 6.dp, vertical = 10.dp), contentAlignment = Alignment.Center) {
                        Text(
                            label,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isSelected) PaperLight else Moss,
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }
}

private fun formatImportedAt(importedAt: String?): String {
    if (importedAt.isNullOrBlank()) return "时间未知"
    return runCatching {
        Instant.parse(importedAt).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
    }.getOrDefault(importedAt)
}

@Composable
private fun ProfileEditor(draft: UserProfile, onDraftChanged: (UserProfile) -> Unit, onCancel: () -> Unit, onSave: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("编辑资料", style = MaterialTheme.typography.titleLarge, color = Ink, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Icon(Icons.Default.Edit, contentDescription = null, tint = Green, modifier = Modifier.size(20.dp))
        }
        Surface(shape = RoundedCornerShape(20.dp), color = PaperLight, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ProfileField("姓名", draft.name) { onDraftChanged(draft.copy(name = it)) }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) { Text("取消") }
            Button(onClick = onSave, modifier = Modifier.weight(1f)) { Text("保存资料") }
        }
    }
}

@Composable
private fun ProfileField(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(value = value, onValueChange = onValueChange, label = { Text(label) }, singleLine = true, modifier = Modifier.fillMaxWidth())
}

package com.colink.app.ui.schedule

/* Weekly timetable UI adapted from ShiGuangSchedule v2.1.0 (Apache-2.0). */

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.colink.app.Course
import com.colink.app.data.CourseLoadState
import com.colink.app.semesterWeekOrNull
import com.colink.app.ui.theme.Green
import com.colink.app.ui.theme.AppThemePalette
import com.colink.app.ui.schedule.CourseCardEffect
import com.colink.app.ui.theme.GreenSoft
import com.colink.app.ui.theme.Ink
import com.colink.app.ui.theme.Line
import com.colink.app.ui.theme.Moss
import com.colink.app.ui.theme.PaperLight
import com.colink.app.ui.theme.courseAccent
import com.colink.app.ui.theme.courseTint
import java.time.LocalDate
import java.time.format.TextStyle as DateTextStyle
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.delay

private val dayDateFormatter = DateTimeFormatter.ofPattern("MM-dd", Locale.CHINA)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    semesterStart: LocalDate,
    maxWeek: Int = 32,
    loadState: CourseLoadState,
    onRetry: () -> Unit,
    onImportClick: () -> Unit = {},
    showWeekends: Boolean = true,
    appThemePalette: AppThemePalette = AppThemePalette.CLASSIC,
    displayPreferences: ScheduleDisplayPreferences = ScheduleDisplayPreferences.DEFAULT,
    modifier: Modifier = Modifier,
) {
    var today by remember { mutableStateOf(LocalDate.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            today = LocalDate.now()
            delay(30_000)
        }
    }
    var viewDateEpochDay by rememberSaveable { mutableLongStateOf(today.toEpochDay()) }
    var weekPickerOpen by remember { mutableStateOf(false) }
    val viewDate = LocalDate.ofEpochDay(viewDateEpochDay)
    val weekStart = viewDate.minusDays((viewDate.dayOfWeek.value - 1).toLong())
    val weekDates = List(7) { index -> weekStart.plusDays(index.toLong()) }
    val displayedWeekDates = if (showWeekends) weekDates else weekDates.take(5)
    val weekNumbers = weekDates.mapNotNull { semesterWeekOrNull(semesterStart, it, maxWeek) }.distinct()
    val currentWeekLabel = when (weekNumbers.size) {
        0 -> "学期外"
        1 -> "第 ${weekNumbers.first()} 周"
        else -> "第 ${weekNumbers.joinToString(" / ")} 周"
    }
    val isCurrentWeek = weekStart == today.minusDays((today.dayOfWeek.value - 1).toLong())
    var selectedCourse by remember { mutableStateOf<Course?>(null) }

    val allCourses = (loadState as? CourseLoadState.Ready)?.courses
    val coursesByDate = displayedWeekDates.associateWith { date ->
        val dateWeek = semesterWeekOrNull(semesterStart, date, maxWeek)
        allCourses.orEmpty()
            .filter { dateWeek != null && it.day == date.dayOfWeek.value && dateWeek in it.weeks }
            .sortedBy { it.start ?: Int.MAX_VALUE }
    }
    val weekCourses = coursesByDate.values.flatten()

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(start = 12.dp, top = 0.dp, end = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        item {
            ScheduleHeader(
                weekStart = weekStart,
                weekLabel = currentWeekLabel,
                isCurrentWeek = isCurrentWeek,
                onWeekPickerClick = { weekPickerOpen = true },
                onPreviousWeek = { viewDateEpochDay -= 7 },
                onNextWeek = { viewDateEpochDay += 7 },
                onToday = { viewDateEpochDay = today.toEpochDay() },
            )
        }
        when (loadState) {
            is CourseLoadState.Loading -> item { LoadingScheduleCard() }
            is CourseLoadState.NoClassSelected -> item {
                NoConfirmedScheduleCard(onImportClick)
            }
            is CourseLoadState.Error -> item { ErrorScheduleCard(loadState.message, onRetry) }
            is CourseLoadState.Ready -> when {
                allCourses.isNullOrEmpty() -> item { NoConfirmedScheduleCard(onImportClick) }
                weekCourses.isEmpty() -> item { EmptyScheduleCard() }
                else -> item {
                    WeeklyTimetableGrid(
                        days = displayedWeekDates,
                        coursesByDate = coursesByDate,
                        today = today,
                        appThemePalette = appThemePalette,
                        displayPreferences = displayPreferences,
                        onCourseClick = { selectedCourse = it },
                    )
                }
            }
        }
    }
    selectedCourse?.let { CourseDetailSheet(it) { selectedCourse = null } }
    if (weekPickerOpen) {
        WeekPickerSheet(
            maxWeek = maxWeek,
            selectedWeek = semesterWeekOrNull(semesterStart, viewDate, maxWeek),
            onSelectWeek = { week ->
                viewDateEpochDay = semesterStart.plusDays((week - 1) * 7L + 3L).toEpochDay()
                weekPickerOpen = false
            },
            onDismiss = { weekPickerOpen = false },
        )
    }
}

@Composable
private fun ScheduleHeader(
    weekStart: LocalDate,
    weekLabel: String,
    isCurrentWeek: Boolean,
    onWeekPickerClick: () -> Unit,
    onPreviousWeek: () -> Unit,
    onNextWeek: () -> Unit,
    onToday: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().pointerInput(weekStart) {
            var dragDistance = 0f
            detectHorizontalDragGestures(
                onDragEnd = {
                    if (dragDistance > 56f) onPreviousWeek()
                    if (dragDistance < -56f) onNextWeek()
                    dragDistance = 0f
                },
                onHorizontalDrag = { _, amount -> dragDistance += amount },
            )
        },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            "本周课程",
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Ink,
            maxLines = 1,
        )
        if (!isCurrentWeek) {
            TextButton(onClick = onToday, contentPadding = PaddingValues(horizontal = 4.dp)) {
                Text("本周", color = Green, maxLines = 1)
            }
        }
        IconButton(onClick = onPreviousWeek) {
            Icon(Icons.Default.ChevronLeft, contentDescription = "上一周", tint = Green)
        }
        TextButton(onClick = onWeekPickerClick, contentPadding = PaddingValues(horizontal = 4.dp)) {
            Text(weekLabel, color = Green, fontWeight = FontWeight.SemiBold, maxLines = 1)
            Icon(Icons.Default.ArrowDropDown, contentDescription = "选择周次", tint = Green)
        }
        IconButton(onClick = onNextWeek) {
            Icon(Icons.Default.ChevronRight, contentDescription = "下一周", tint = Green)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WeekPickerSheet(
    maxWeek: Int,
    selectedWeek: Int?,
    onSelectWeek: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = PaperLight) {
        Column(
            Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text("快速跳转周次", style = MaterialTheme.typography.titleLarge, color = Ink, fontWeight = FontWeight.Bold)
            if (maxWeek < 1) {
                Text("课表尚未提供学期周数", style = MaterialTheme.typography.bodyMedium, color = Moss)
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(5),
                    modifier = Modifier.heightIn(max = 360.dp),
                    contentPadding = PaddingValues(bottom = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items((1..maxWeek).toList()) { week ->
                        val selected = week == selectedWeek
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (selected) Green else GreenSoft,
                            modifier = Modifier.fillMaxWidth().height(42.dp).clickable { onSelectWeek(week) },
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    "第 $week 周",
                                    color = if (selected) PaperLight else Green,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WeeklyTimetableGrid(
    days: List<LocalDate>,
    coursesByDate: Map<LocalDate, List<Course>>,
    today: LocalDate,
    appThemePalette: AppThemePalette,
    displayPreferences: ScheduleDisplayPreferences,
    onCourseClick: (Course) -> Unit,
) {
    val allCourses = coursesByDate.values.flatten()
    val timedCourses = allCourses.filter { it.start != null && it.end != null }
    val firstPeriod = 1
    val lastPeriod = timedCourses.mapNotNull { it.end }.maxOrNull() ?: firstPeriod
    val periodCount = if (timedCourses.isEmpty()) 0 else lastPeriod.coerceAtLeast(firstPeriod)
    val rowHeight = displayPreferences.rowHeightDp.dp
    val periodAxisWidth = 40.dp
    val dayScrollState = rememberScrollState()

    BoxWithConstraints(Modifier.fillMaxWidth().background(PaperLight)) {
        val dayCellWidth = ((maxWidth - periodAxisWidth) / 5f).coerceAtLeast(48.dp)
        Column(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.width(periodAxisWidth).height(45.dp).then(gridCellModifier(displayPreferences.showGridLines)),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "${days.firstOrNull()?.year ?: today.year}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Moss,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                    )
                    Text("年", style = MaterialTheme.typography.labelSmall, color = Moss, textAlign = TextAlign.Center)
                }
            }
            Row(Modifier.weight(1f).horizontalScroll(dayScrollState)) {
                days.forEach { date ->
                    val isToday = date == today
                    Column(
                        modifier = Modifier
                            .width(dayCellWidth)
                            .height(45.dp)
                            .background(if (isToday) GreenSoft.copy(alpha = 0.34f) else PaperLight)
                            .then(gridCellModifier(displayPreferences.showGridLines)),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            date.dayOfWeek.getDisplayName(DateTextStyle.SHORT, Locale.CHINA).removePrefix("周"),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isToday) Green else Moss,
                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                        )
                        if (displayPreferences.showDateUnderDay) {
                            Text(date.format(dayDateFormatter), style = MaterialTheme.typography.labelSmall, color = Ink)
                        }
                    }
                }
            }
        }
        if (periodCount > 0) {
            Row(verticalAlignment = Alignment.Top) {
                Column {
                    repeat(periodCount) { index ->
                        Box(
                            Modifier.width(periodAxisWidth).height(rowHeight).then(gridCellModifier(displayPreferences.showGridLines)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("${firstPeriod + index}", style = MaterialTheme.typography.labelSmall, color = Moss)
                        }
                    }
                }
                Row(Modifier.weight(1f).horizontalScroll(dayScrollState)) {
                    days.forEach { date ->
                        DayCourseColumn(
                            modifier = Modifier.width(dayCellWidth),
                            courses = coursesByDate[date].orEmpty().filter { it.start != null && it.end != null },
                            firstPeriod = firstPeriod,
                            periodCount = periodCount,
                            rowHeight = rowHeight,
                            appThemePalette = appThemePalette,
                            displayPreferences = displayPreferences,
                            onCourseClick = onCourseClick,
                        )
                    }
                }
            }
        }
        val unknownCoursesExist = allCourses.any { it.start == null || it.end == null }
        if (unknownCoursesExist) {
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    Modifier.width(periodAxisWidth).height(48.dp).then(gridCellModifier(displayPreferences.showGridLines)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("—", style = MaterialTheme.typography.labelSmall, color = Moss, textAlign = TextAlign.Center)
                }
                Row(Modifier.weight(1f).horizontalScroll(dayScrollState)) {
                    days.forEach { date ->
                        Column(Modifier.width(dayCellWidth)) {
                            coursesByDate[date].orEmpty()
                                .filter { it.start == null || it.end == null }
                                .forEach { course -> UnscheduledCourseBlock(course, appThemePalette, displayPreferences, onCourseClick) }
                            if (coursesByDate[date].orEmpty().none { it.start == null || it.end == null }) {
                                Box(Modifier.fillMaxWidth().height(48.dp).then(gridCellModifier(displayPreferences.showGridLines)))
                            }
                        }
                    }
                }
            }
        }
        }
    }
}

@Composable
private fun DayCourseColumn(
    modifier: Modifier,
    courses: List<Course>,
    firstPeriod: Int,
    periodCount: Int,
    rowHeight: Dp,
    appThemePalette: AppThemePalette,
    displayPreferences: ScheduleDisplayPreferences,
    onCourseClick: (Course) -> Unit,
) {
    val placements = placeCourses(courses)
    BoxWithConstraints(modifier.height(rowHeight * periodCount.toFloat())) {
        Column {
            repeat(periodCount) {
                Box(Modifier.fillMaxWidth().height(rowHeight).then(gridCellModifier(displayPreferences.showGridLines)))
            }
        }
        placements.forEach { placement ->
            val course = placement.course
            val start = course.start ?: return@forEach
            val end = maxOf(start, course.end ?: start)
            val laneWidth = maxWidth / placement.laneCount
            val duration = (end - start + 1).coerceAtLeast(1)
            WeekCourseBlock(
                course = course,
                modifier = Modifier
                    .offset(
                        x = laneWidth * placement.lane.toFloat() + 1.dp,
                        y = rowHeight * (start - firstPeriod).toFloat() + 1.dp,
                    )
                    .width(laneWidth - 2.dp)
                    .height(rowHeight * duration.toFloat() - 2.dp),
                appThemePalette = appThemePalette,
                displayPreferences = displayPreferences,
                onClick = { onCourseClick(course) },
            )
        }
    }
}

@Composable
private fun WeekCourseBlock(
    course: Course,
    modifier: Modifier,
    appThemePalette: AppThemePalette,
    displayPreferences: ScheduleDisplayPreferences,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(displayPreferences.cardCornerRadiusDp.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .background(courseTint(course.id, appThemePalette))
            .clickable(onClick = onClick)
    ) {
        Column(
            Modifier.fillMaxSize().padding(
                start = if (displayPreferences.courseCardEffect == CourseCardEffect.ACCENT_STRIPE) 8.dp else 4.dp,
                end = 4.dp,
                top = 4.dp,
                bottom = 4.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                course.name,
                color = Ink,
                style = TextStyle(
                    fontSize = displayPreferences.courseFontSizeSp.sp,
                    lineHeight = (displayPreferences.courseFontSizeSp + 2).sp,
                    fontWeight = FontWeight.Bold,
                ),
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
            )
            if (displayPreferences.showCoursePeriod) {
                val start = course.start
                val end = course.end
                if (start != null) {
                    Text(
                        if (end != null && end > start) "第 $start–$end 节" else "第 $start 节",
                        color = Ink.copy(alpha = 0.82f),
                        style = TextStyle(fontSize = (displayPreferences.courseFontSizeSp - 2).coerceAtLeast(8).sp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            if (displayPreferences.showCourseLocation && course.room.isNotBlank()) {
                Text(
                    "@${course.room}",
                    color = Ink.copy(alpha = 0.82f),
                    style = TextStyle(
                        fontSize = (displayPreferences.courseFontSizeSp - 2).coerceAtLeast(8).sp,
                        lineHeight = (displayPreferences.courseFontSizeSp).sp,
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        if (displayPreferences.courseCardEffect == CourseCardEffect.ACCENT_STRIPE) {
            Box(Modifier.width(4.dp).matchParentSize().background(courseAccent(course.id, appThemePalette)))
        }
    }
}

@Composable
private fun UnscheduledCourseBlock(
    course: Course,
    appThemePalette: AppThemePalette,
    displayPreferences: ScheduleDisplayPreferences,
    onCourseClick: (Course) -> Unit,
) {
    val shape = RoundedCornerShape(displayPreferences.cardCornerRadiusDp.dp)
    Box(
        Modifier
            .fillMaxWidth()
            .padding(1.dp)
            .clip(shape)
            .background(courseTint(course.id, appThemePalette))
            .clickable { onCourseClick(course) },
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(
                    start = if (displayPreferences.courseCardEffect == CourseCardEffect.ACCENT_STRIPE) 8.dp else 4.dp,
                    end = 4.dp,
                    top = 4.dp,
                    bottom = 4.dp,
                ),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                course.name,
                color = Ink,
                style = TextStyle(fontSize = displayPreferences.courseFontSizeSp.sp, fontWeight = FontWeight.SemiBold),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
            if (displayPreferences.showCourseLocation && course.room.isNotBlank()) {
                Text(
                    "@${course.room}",
                    color = Ink.copy(alpha = 0.82f),
                    style = TextStyle(fontSize = (displayPreferences.courseFontSizeSp - 2).coerceAtLeast(8).sp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        if (displayPreferences.courseCardEffect == CourseCardEffect.ACCENT_STRIPE) {
            Box(Modifier.width(4.dp).matchParentSize().background(courseAccent(course.id, appThemePalette)))
        }
    }
}

@Composable
private fun gridCellModifier(showGridLines: Boolean): Modifier =
    if (showGridLines) Modifier.border(0.5.dp, Line) else Modifier

private data class CoursePlacement(val course: Course, val lane: Int, val laneCount: Int)

private fun placeCourses(courses: List<Course>): List<CoursePlacement> {
    val laneEnds = mutableListOf<Int>()
    val assigned = mutableListOf<Pair<Course, Int>>()
    courses
        .filter { it.start != null && it.end != null }
        .sortedWith(compareBy<Course> { it.start }.thenBy { it.end })
        .forEach { course ->
            val start = course.start ?: return@forEach
            val end = maxOf(start, course.end ?: start)
            val availableLane = laneEnds.indexOfFirst { it < start }
            val lane = if (availableLane >= 0) {
                laneEnds[availableLane] = end
                availableLane
            } else {
                laneEnds.add(end)
                laneEnds.lastIndex
            }
            assigned += course to lane
        }
    val laneCount = laneEnds.size.coerceAtLeast(1)
    return assigned.map { (course, lane) -> CoursePlacement(course, lane, laneCount) }
}

private fun periodLabel(course: Course): String {
    val start = course.start
    val end = course.end
    return if (start == null || end == null) "节次未定" else "第 $start–$end 节"
}

@Composable
private fun LoadingScheduleCard() {
    StatusCard(
        icon = Icons.Default.Schedule,
        title = "正在加载课表",
        description = "正在读取课表数据",
    )
}

@Composable
private fun ErrorScheduleCard(message: String, onRetry: () -> Unit) {
    Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = PaperLight), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(vertical = 30.dp, horizontal = 22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(shape = CircleShape, color = GreenSoft, modifier = Modifier.size(52.dp)) {
                Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = Green) }
            }
            Spacer(Modifier.height(14.dp))
            Text("课表加载失败", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(5.dp))
            Text(message, style = MaterialTheme.typography.bodyMedium, color = Moss)
            Spacer(Modifier.height(16.dp))
            OutlinedButton(onClick = onRetry) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.size(7.dp))
                Text("重新加载")
            }
        }
    }
}

@Composable
private fun NoConfirmedScheduleCard(onImportClick: () -> Unit) {
    StatusCard(
        icon = Icons.Default.CalendarToday,
        title = "还没有课表",
        description = "导入学校下发的课表 Excel 文件",
        actionLabel = "导入课表",
        onAction = onImportClick,
    )
}

@Composable
private fun EmptyScheduleCard() {
    StatusCard(
        icon = Icons.Default.CalendarToday,
        title = "本周没有课程",
        description = "这一周没有课程安排",
    )
}

@Composable
private fun StatusCard(
    icon: ImageVector,
    title: String,
    description: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = PaperLight), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(vertical = 30.dp, horizontal = 22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(shape = CircleShape, color = GreenSoft, modifier = Modifier.size(52.dp)) {
                Box(contentAlignment = Alignment.Center) { Icon(icon, contentDescription = null, tint = Green) }
            }
            Spacer(Modifier.height(14.dp))
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(5.dp))
            Text(description, style = MaterialTheme.typography.bodyMedium, color = Moss)
            if (actionLabel != null && onAction != null) {
                Spacer(Modifier.height(16.dp))
                Button(onClick = onAction) { Text(actionLabel) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CourseDetailSheet(course: Course, onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = PaperLight) {
        Column(Modifier.padding(start = 24.dp, end = 24.dp, bottom = 32.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("课程详情", style = MaterialTheme.typography.labelLarge, color = Green)
                    Spacer(Modifier.height(4.dp))
                    Text(course.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                }
                IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = "关闭课程详情") }
            }
            Spacer(Modifier.height(20.dp))
            DetailRow(Icons.Default.LocationOn, "上课地点", course.room)
            DetailRow(Icons.Default.Schedule, "上课时间", periodLabel(course))
            DetailRow(Icons.Default.Class, "开课周次", formatWeeks(course.weeks))
            DetailRow(Icons.Default.School, "课程代码", course.code)
            Spacer(Modifier.height(8.dp)); HorizontalDivider(color = Line); Spacer(Modifier.height(14.dp))
            Text("课程信息来自已导入课表", style = MaterialTheme.typography.bodySmall, color = Moss)
        }
    }
}

@Composable
private fun DetailRow(icon: ImageVector, label: String, value: String) {
    Row(Modifier.padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = Green, modifier = Modifier.size(20.dp)); Spacer(Modifier.width(13.dp))
        Column { Text(label, style = MaterialTheme.typography.labelMedium, color = Moss); Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium) }
    }
}

private fun formatWeeks(weeks: Set<Int>): String {
    val sorted = weeks.sorted()
    if (sorted.isEmpty()) return "未设置"
    return if (sorted == (sorted.first()..sorted.last()).toList()) "第 ${sorted.first()}–${sorted.last()} 周" else "第 ${sorted.joinToString("、")} 周"
}

package com.colink.app.ui.home

/* Today timeline UI adapted from ShiGuangSchedule v2.1.0 (Apache-2.0). */

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.colink.app.Course
import com.colink.app.data.CourseLoadState
import com.colink.app.semesterWeekOrNull
import com.colink.app.ui.theme.Green
import com.colink.app.ui.theme.GreenSoft
import com.colink.app.ui.theme.Ink
import com.colink.app.ui.theme.Moss
import com.colink.app.ui.theme.Paper
import com.colink.app.ui.theme.PaperLight
import com.colink.app.ui.theme.courseAccent
import com.colink.app.ui.theme.courseTint
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.delay

private val homeBackground = Paper
private val dateFormatter = DateTimeFormatter.ofPattern("M月d日 · EEEE", Locale.CHINA)

@Composable
fun HomeScreen(
    semesterStart: LocalDate,
    maxWeek: Int,
    loadState: CourseLoadState,
    onRetry: () -> Unit,
    onOpenSchedule: () -> Unit,
    onImportClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var today by remember { mutableStateOf(LocalDate.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000)
            today = LocalDate.now()
        }
    }
    val currentWeek = semesterWeekOrNull(semesterStart, today, maxWeek)
    val allCourses = (loadState as? CourseLoadState.Ready)?.courses.orEmpty()
    val todayCourses = allCourses
        .filter { currentWeek != null && it.day == today.dayOfWeek.value && currentWeek in it.weeks }
        .sortedBy { it.start ?: Int.MAX_VALUE }

    LazyColumn(
        modifier = modifier.fillMaxSize().background(homeBackground),
        contentPadding = PaddingValues(start = 16.dp, top = 4.dp, end = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Text(
                "今日课表",
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.titleLarge,
                color = Ink,
                fontWeight = FontWeight.Bold,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
        }
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(today.format(dateFormatter), modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleMedium, color = Ink, fontWeight = FontWeight.Bold)
                Surface(shape = RoundedCornerShape(12.dp), color = GreenSoft) {
                    Text(
                        currentWeek?.let { "第 $it 周" } ?: "学期外",
                        Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = Green,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
        when (loadState) {
            CourseLoadState.Loading -> item { HomeStatus("正在读取本地课表", "请稍候片刻。") }
            CourseLoadState.NoClassSelected -> item {
                HomeActionStatus("还没有导入课表", "选择学校导出的课表文件，解析和保存都在本机完成。", "导入课表", onImportClick)
            }
            is CourseLoadState.Error -> item {
                HomeErrorStatus(loadState.message, onRetry, onImportClick)
            }
            is CourseLoadState.Ready -> when {
                allCourses.isEmpty() -> item {
                    HomeActionStatus("还没有课程安排", "导入课表后，今天的课程会显示在这里。", "导入课表", onImportClick)
                }
                currentWeek == null -> item { HomeStatus("当前日期不在学期范围内", "可以在“我的”中检查开学时间设置。") }
                todayCourses.isEmpty() -> item { HomeStatus("今天没有课程", "今日无课，留一点时间给自己。") }
                else -> {
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("今日课程", style = MaterialTheme.typography.titleMedium, color = Ink, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                            Text("${todayCourses.size} 门", style = MaterialTheme.typography.labelMedium, color = Moss)
                        }
                    }
                    items(todayCourses, key = { it.id }) { course -> HomeCourseRow(course) }
                }
            }
        }
        item {
            TextButton(onClick = onOpenSchedule, modifier = Modifier.fillMaxWidth()) {
                Text("查看完整课表", fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.width(7.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(17.dp))
            }
        }
    }
}
@Composable
private fun HomeCourseRow(course: Course) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Column(Modifier.width(72.dp).padding(top = 8.dp), horizontalAlignment = Alignment.End) {
            Text(course.start?.let { "第 $it 节" } ?: "待定", style = MaterialTheme.typography.labelLarge, color = Ink, fontWeight = FontWeight.Bold)
            course.end?.let { Text("至第 $it 节", style = MaterialTheme.typography.labelSmall, color = Moss) }
        }
        Spacer(Modifier.width(12.dp))
        Card(
            modifier = Modifier.weight(1f),
            colors = CardDefaults.cardColors(containerColor = courseTint(course.id)),
            shape = MaterialTheme.shapes.medium,
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Box(Modifier.fillMaxWidth()) {
                Column(Modifier.fillMaxWidth().padding(start = 16.dp, end = 12.dp, top = 12.dp, bottom = 12.dp)) {
                    Text(course.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Ink, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    if (course.room.isNotBlank()) {
                        Text("@${course.room}", style = MaterialTheme.typography.bodySmall, color = Ink.copy(alpha = 0.78f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
                Box(Modifier.width(4.dp).matchParentSize().background(courseAccent(course.id)))
            }
        }
    }
}

@Composable
private fun HomeStatus(title: String, description: String) {
    Surface(shape = RoundedCornerShape(18.dp), color = PaperLight, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(horizontal = 20.dp, vertical = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(shape = CircleShape, color = GreenSoft, modifier = Modifier.size(44.dp)) {
                Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = Green, modifier = Modifier.size(21.dp)) }
            }
            Spacer(Modifier.height(12.dp))
            Text(title, style = MaterialTheme.typography.titleMedium, color = Ink, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(5.dp))
            Text(description, style = MaterialTheme.typography.bodyMedium, color = Moss)
        }
    }
}

@Composable
private fun HomeActionStatus(title: String, description: String, actionLabel: String, onAction: () -> Unit) {
    Surface(shape = RoundedCornerShape(18.dp), color = PaperLight, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(horizontal = 20.dp, vertical = 22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = Ink, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(5.dp))
            Text(description, style = MaterialTheme.typography.bodyMedium, color = Moss)
            Spacer(Modifier.height(13.dp))
            OutlinedButton(onClick = onAction) { Text(actionLabel) }
        }
    }
}

@Composable
private fun HomeErrorStatus(message: String, onRetry: () -> Unit, onImportClick: () -> Unit) {
    Surface(shape = RoundedCornerShape(18.dp), color = PaperLight, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("课表暂时无法读取", style = MaterialTheme.typography.titleMedium, color = Ink, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(5.dp))
            Text(message, style = MaterialTheme.typography.bodyMedium, color = Moss)
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = onRetry) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(17.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("重试")
                }
                OutlinedButton(onClick = onImportClick) { Text("重新导入") }
            }
        }
    }
}

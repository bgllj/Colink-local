package com.colink.app.ui.widget

import com.colink.app.data.StoredMeeting
import com.colink.app.data.StoredSchedule
import com.colink.app.data.expandedWeeks
import com.colink.app.semesterWeekOrNull
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

internal data class TodayWidgetCourse(
    val name: String,
    val period: String,
    val location: String,
)

internal data class TodayWidgetContent(
    val dateLabel: String,
    val weekLabel: String,
    val countLabel: String,
    val courses: List<TodayWidgetCourse>,
    val emptyMessage: String?,
    val remainingCount: Int,
)

internal fun buildTodayWidgetContent(
    schedule: StoredSchedule?,
    today: LocalDate,
    semesterStart: LocalDate,
): TodayWidgetContent {
    val week = semesterWeekOrNull(semesterStart, today, schedule?.semester?.maxWeek ?: 32)
    val meetings = schedule?.meetings.orEmpty()
    val todayMeetings = if (week == null) {
        emptyList()
    } else {
        meetings.asSequence()
            .filter { it.weekday == today.dayOfWeek.value && week in it.expandedWeeks() }
            .sortedBy { it.periodStart ?: Int.MAX_VALUE }
            .toList()
    }

    val emptyMessage = when {
        schedule == null -> "尚未导入课表"
        week == null -> "当前日期不在学期范围内"
        todayMeetings.isEmpty() -> "今天没有课程"
        else -> null
    }

    return TodayWidgetContent(
        dateLabel = today.format(DATE_FORMAT),
        weekLabel = week?.let { "第 $it 周" } ?: "学期外",
        countLabel = "${todayMeetings.size} 门",
        courses = todayMeetings.take(MAX_VISIBLE_COURSES).map(StoredMeeting::toWidgetCourse),
        emptyMessage = emptyMessage,
        remainingCount = (todayMeetings.size - MAX_VISIBLE_COURSES).coerceAtLeast(0),
    )
}

private fun StoredMeeting.toWidgetCourse(): TodayWidgetCourse {
    val period = when {
        periodStart != null && periodEnd != null && periodStart != periodEnd -> "$periodStart-$periodEnd 节"
        periodStart != null -> "第 $periodStart 节"
        else -> "节次待定"
    }
    return TodayWidgetCourse(
        name = courseName,
        period = period,
        location = roomText?.takeIf(String::isNotBlank) ?: "地点待补全",
    )
}

private const val MAX_VISIBLE_COURSES = 2
private val DATE_FORMAT = DateTimeFormatter.ofPattern("EEEE · M月d日", Locale.CHINA)

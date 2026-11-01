package com.colink.app.ui.widget

import com.colink.app.data.StoredMeeting
import com.colink.app.data.StoredSchedule
import com.colink.app.data.StoredSemester
import com.colink.app.data.StoredWeekRange
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TodayWidgetContentTest {

    private val semesterStart = LocalDate.parse("2026-08-31")
    private val schedule = StoredSchedule(
        meetings = listOf(
            StoredMeeting(
                courseName = "高等数学",
                weekday = 2,
                periodStart = 1,
                periodEnd = 2,
                weekRanges = listOf(StoredWeekRange(1, 5, "ODD")),
                roomText = "教A101",
            ),
            StoredMeeting(
                courseName = "大学英语",
                weekday = 2,
                periodStart = 3,
                periodEnd = 4,
                weekRanges = listOf(StoredWeekRange(1, 5, "ALL")),
                roomText = "教B202",
            ),
            StoredMeeting(
                courseName = "数据结构",
                weekday = 2,
                periodStart = 5,
                periodEnd = 6,
                weekRanges = listOf(StoredWeekRange(1, 5, "ALL")),
                roomText = "实训楼301",
            ),
        ),
        semester = StoredSemester(startDate = "2026-08-31", maxWeek = 5),
        importedAt = "2026-10-29T10:00:00Z",
    )

    @Test
    fun showsCurrentWeekCoursesInPeriodOrderAndCapsVisibleRows() {
        val content = buildTodayWidgetContent(schedule, LocalDate.parse("2026-09-01"), semesterStart)

        assertEquals("3 门", content.countLabel)
        assertEquals(listOf("高等数学", "大学英语"), content.courses.map { it.name })
        assertEquals("另有 1 门课程", content.remainingCount.let { "另有 $it 门课程" })
        assertNull(content.emptyMessage)
    }

    @Test
    fun hidesOddWeekCourseOnEvenWeek() {
        val content = buildTodayWidgetContent(schedule, LocalDate.parse("2026-09-08"), semesterStart)

        assertEquals(listOf("大学英语", "数据结构"), content.courses.map { it.name })
    }

    @Test
    fun showsEmptyStateForDayWithoutCourses() {
        val content = buildTodayWidgetContent(schedule, LocalDate.parse("2026-09-06"), semesterStart)

        assertEquals("今天没有课程", content.emptyMessage)
        assertEquals(emptyList<Any>(), content.courses)
    }
}

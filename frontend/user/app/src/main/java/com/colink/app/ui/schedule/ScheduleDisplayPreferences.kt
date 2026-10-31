package com.colink.app.ui.schedule

enum class CourseCardEffect(val storageKey: String, val label: String) {
    SOFT("soft", "柔和彩块"),
    ACCENT_STRIPE("accent_stripe", "主题色条");

    companion object {
        fun fromStorageKey(key: String?): CourseCardEffect =
            values().firstOrNull { it.storageKey == key } ?: ACCENT_STRIPE
    }
}

data class ScheduleDisplayPreferences(
    val showCourseLocation: Boolean = true,
    val showCoursePeriod: Boolean = false,
    val showDateUnderDay: Boolean = true,
    val showGridLines: Boolean = true,
    val rowHeightDp: Int = 60,
    val courseFontSizeSp: Int = 11,
    val cardCornerRadiusDp: Int = 4,
    val courseCardEffect: CourseCardEffect = CourseCardEffect.ACCENT_STRIPE,
) {
    companion object {
        val DEFAULT = ScheduleDisplayPreferences()
        val ROW_HEIGHT_OPTIONS = listOf(48, 60, 72)
        val FONT_SIZE_OPTIONS = listOf(10, 11, 12)
        val CORNER_RADIUS_OPTIONS = listOf(4, 8, 12)
    }
}

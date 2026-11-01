package com.colink.app.ui.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import com.colink.app.MainActivity
import com.colink.app.R
import com.colink.app.data.ScheduleJson
import com.colink.app.data.StoredSchedule
import java.time.LocalDate

class TodayScheduleWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, manager: AppWidgetManager, widgetIds: IntArray) {
        widgetIds.forEach { id -> updateWidget(context, manager, id) }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action in DATE_ACTIONS) refreshAll(context)
    }

    companion object {
        private val DATE_ACTIONS = setOf(
            Intent.ACTION_DATE_CHANGED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
        )

        fun refreshAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val provider = ComponentName(context, TodayScheduleWidgetProvider::class.java)
            val widgetIds = manager.getAppWidgetIds(provider)
            if (widgetIds.isNotEmpty()) {
                TodayScheduleWidgetProvider().onUpdate(context, manager, widgetIds)
            }
        }

        private fun updateWidget(context: Context, manager: AppWidgetManager, widgetId: Int) {
            val today = LocalDate.now()
            val schedule = loadSchedule(context)
            val preferences = context.getSharedPreferences(PROFILE_PREFERENCES, Context.MODE_PRIVATE)
            val semesterStart = runCatching {
                preferences.getString(KEY_SEMESTER_START, null)?.let(LocalDate::parse)
                    ?: LocalDate.of(today.year, 9, 1)
            }.getOrDefault(LocalDate.of(today.year, 9, 1))
            val content = buildTodayWidgetContent(schedule, today, semesterStart)
            val views = RemoteViews(context.packageName, R.layout.today_schedule_widget).apply {
                setTextViewText(R.id.widget_date, content.dateLabel)
                setTextViewText(R.id.widget_week, content.weekLabel)
                setTextViewText(R.id.widget_count, content.countLabel)
                setViewVisibility(R.id.widget_empty, if (content.emptyMessage == null) View.GONE else View.VISIBLE)
                setTextViewText(R.id.widget_empty, content.emptyMessage.orEmpty())
                setViewVisibility(R.id.widget_course_row_1, if (content.courses.isNotEmpty()) View.VISIBLE else View.GONE)
                setViewVisibility(R.id.widget_course_row_2, if (content.courses.size > 1) View.VISIBLE else View.GONE)
                setViewVisibility(R.id.widget_more, if (content.remainingCount > 0) View.VISIBLE else View.GONE)
                content.courses.getOrNull(0)?.let { setCourseRow(1, it) }
                content.courses.getOrNull(1)?.let { setCourseRow(2, it) }
                setTextViewText(R.id.widget_more, if (content.remainingCount > 0) "另有 ${content.remainingCount} 门课程" else "")
                val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
                if (launchIntent != null) {
                    val pendingIntent = PendingIntent.getActivity(
                        context,
                        0,
                        launchIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                    )
                    setOnClickPendingIntent(R.id.widget_root, pendingIntent)
                }
            }
            manager.updateAppWidget(widgetId, views)
        }

        private fun RemoteViews.setCourseRow(index: Int, course: TodayWidgetCourse) {
            val nameId = if (index == 1) R.id.widget_course_name_1 else R.id.widget_course_name_2
            val periodId = if (index == 1) R.id.widget_course_period_1 else R.id.widget_course_period_2
            val locationId = if (index == 1) R.id.widget_course_location_1 else R.id.widget_course_location_2
            setTextViewText(nameId, course.name)
            setTextViewText(periodId, course.period)
            setTextViewText(locationId, course.location)
        }

        private fun loadSchedule(context: Context): StoredSchedule? {
            val serialized = context.getSharedPreferences(SCHEDULE_PREFERENCES, Context.MODE_PRIVATE)
                .getString(KEY_SCHEDULE, null) ?: return null
            return runCatching {
                ScheduleJson.format.decodeFromString(StoredSchedule.serializer(), serialized)
            }.getOrNull()
        }

        private const val SCHEDULE_PREFERENCES = "colink_local_schedule"
        private const val PROFILE_PREFERENCES = "colink_profile"
        private const val KEY_SCHEDULE = "local_schedule_json"
        private const val KEY_SEMESTER_START = "semesterStart"
    }
}

class WidgetPinResultReceiver : android.content.BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_PIN_RESULT) {
            TodayScheduleWidgetProvider.refreshAll(context)
        }
    }

    companion object {
        const val ACTION_PIN_RESULT = "com.colink.app.action.WIDGET_PIN_RESULT"
    }
}

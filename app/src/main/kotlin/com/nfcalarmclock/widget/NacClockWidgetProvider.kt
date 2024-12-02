package com.nfcalarmclock.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.format.DateFormat
import android.text.style.StyleSpan
import android.util.TypedValue
import android.view.View
import android.widget.RemoteViews
import com.nfcalarmclock.R
import com.nfcalarmclock.main.NacMainActivity
import com.nfcalarmclock.shared.NacSharedPreferences
import com.nfcalarmclock.util.NacCalendar
import java.util.Calendar

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in [NacClockWidgetConfigureActivity]
 */
class NacClockWidgetProvider : AppWidgetProvider()
{

	/**
	 * Called when the widget is deleted.
	 */
	override fun onAppWidgetOptionsChanged(
		context: Context,
		appWidgetManager: AppWidgetManager,
		appWidgetId: Int,
		newOptions: Bundle)
	{
		super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)

		println("WIDGET OPTION CHANGED")

		//val bundle = appWidgetManager.getAppWidgetOptions(appWidgetId)
		val bundle = newOptions
		println("New Width  : ${bundle.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)} x ${bundle.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH)}")
		println("New Height : ${bundle.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT)} x ${bundle.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT)}")

		updateAppWidget(context, appWidgetManager, appWidgetId)

		// When the user deletes the widget, delete the preference associated with it.
		//for (id in appWidgetIds)
		//{
		//	//deleteTitlePref(context, id)
		//}
	}

	/**
	 * Called when the widget is deleted.
	 */
	override fun onDeleted(context: Context, appWidgetIds: IntArray)
	{
		println("WIDGET DELETED")
		// When the user deletes the widget, delete the preference associated with it.
		//for (id in appWidgetIds)
		//{
		//	//deleteTitlePref(context, id)
		//}
	}

	/**
	 * Called when the wiget is removed from the last host.
	 */
	override fun onDisabled(context: Context)
	{
		// Enter relevant functionality for when the last widget is disabled
		println("WIDGET DISABLED")
	}

	/**
	 * Called when the widget is added to a host for the first time, such as at boot time.
	 */
	override fun onEnabled(context: Context)
	{
		// Enter relevant functionality for when the first widget is created
		println("WIDGET ENABLED")
	}

	override fun onReceive(context: Context, intent: Intent?)
	{
		println("WIDGET RECEIVE : ${intent?.action}")

		if ((intent?.action == "android.intent.action.TIME_SET")
			|| (intent?.action == Intent.ACTION_TIMEZONE_CHANGED)
			|| (intent?.action == Intent.ACTION_LOCALE_CHANGED)
			|| (intent?.action == AlarmManager.ACTION_NEXT_ALARM_CLOCK_CHANGED))
		{
			println("Hello in here!")
			refreshAppWidgets(context)
		}
	}

	/**
	 * Called when the widget is updated/restored.
	 */
	override fun onUpdate(
		context: Context,
		appWidgetManager: AppWidgetManager,
		appWidgetIds: IntArray)
	{
		println("WIDGET UPDATED")

		// Iterate over each widget
		for (id in appWidgetIds)
		{
			// Update the widget
			updateAppWidget(context, appWidgetManager, id)
		}
	}

}

/**
 * Refresh the list of widgets.
 */
internal fun refreshAppWidgets(context: Context)
{
	println("refreshAppWidgets()")
	// Get the list of widget IDs
	val componentName = ComponentName(context, NacClockWidgetProvider::class.java)
	val appWidgetManager = AppWidgetManager.getInstance(context)
	val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)

	// Iterate over each widget ID and update it
	for (id in appWidgetIds)
	{
		println("WIDGET ID : $id")
		updateAppWidget(context, appWidgetManager, id)
	}
}

/**
 * Update an widget.
 */
internal fun updateAppWidget(
	context: Context,
	appWidgetManager: AppWidgetManager,
	appWidgetId: Int)
{
	println("Updating app widget")

	// Get the shared preferences
	val shared = NacSharedPreferences(context)

	// Construct the RemoteViews object
	val views = RemoteViews(context.packageName, R.layout.nac_clock_widget)

	// Set on click pending intent
	val pendingIntent = NacMainActivity.getStartPendingIntent(context)
	views.setOnClickPendingIntent(R.id.parent, pendingIntent)

	// Get the AM/PM meridian
	val nowCal = Calendar.getInstance()
	val amPm = NacCalendar.getMeridian(context, nowCal[Calendar.HOUR_OF_DAY])

	// Get the next alarm
	val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
	val alarmInfo = alarmManager.nextAlarmClock

	// Get which parts of the widget should be shown
	val showTimeVis = if (shared.shouldClockWidgetShowTime) View.VISIBLE else View.GONE
	val showHourBoldVis = if (shared.shouldClockWidgetShowTime && shared.shouldClockWidgetBoldHour) View.VISIBLE else View.GONE
	val showHourVis = if (showHourBoldVis == View.VISIBLE) View.GONE else View.VISIBLE
	val showMinuteBoldVis = if (shared.shouldClockWidgetShowTime && shared.shouldClockWidgetBoldMinute) View.VISIBLE else View.GONE
	val showMinuteVis = if (showMinuteBoldVis == View.VISIBLE) View.GONE else View.VISIBLE
	val showMeridianBoldVis = if (amPm.isNotEmpty() && shared.shouldClockWidgetBoldAmPm) View.VISIBLE else View.GONE
	val showMeridianVis = if (showMeridianBoldVis == View.VISIBLE) View.GONE else View.VISIBLE
	val showDateBoldVis = if (shared.shouldClockWidgetShowDate && shared.shouldClockWidgetBoldDate) View.VISIBLE else View.GONE
	val showDateVis = if (shared.shouldClockWidgetShowDate && !shared.shouldClockWidgetBoldDate) View.VISIBLE else View.GONE
	val showAlarmTimeVis = if (shared.shouldClockWidgetShowAlarmTime && (alarmInfo != null)) View.VISIBLE else View.GONE
	val showAlarmIconVis = if (shared.shouldClockWidgetShowAlarmIcon && (alarmInfo != null)) View.VISIBLE else View.GONE

	// Set view visibility
	views.setViewVisibility(R.id.time, showTimeVis)
	views.setViewVisibility(R.id.hour, showHourVis)
	views.setViewVisibility(R.id.hour_bold, showHourBoldVis)
	views.setViewVisibility(R.id.minute, showMinuteVis)
	views.setViewVisibility(R.id.minute_bold, showMinuteBoldVis)
	views.setViewVisibility(R.id.am_pm, showMeridianVis)
	views.setViewVisibility(R.id.am_pm_bold, showMeridianBoldVis)
	views.setViewVisibility(R.id.date, showDateVis)
	views.setViewVisibility(R.id.date_bold, showDateBoldVis)
	views.setViewVisibility(R.id.alarm_time, showAlarmTimeVis)
	views.setViewVisibility(R.id.alarm_icon, showAlarmIconVis)

	// Check if the alarm time should be customized
	if (showAlarmTimeVis == View.VISIBLE)
	{
		// Create a calendar with the next alarm time
		val alarmCal = Calendar.getInstance()
		alarmCal.timeInMillis = alarmInfo.triggerTime

		// Get the alarm time as a spannable string
		val is24HourFormat = DateFormat.is24HourFormat(context)
		val alarmTime = NacCalendar.getFullTime(alarmCal, is24HourFormat)
		val alarmTimeSpan = SpannableString(alarmTime)

		// Bold alarm time
		if (shared.shouldClockWidgetBoldAlarmTime)
		{
			alarmTimeSpan.setSpan(StyleSpan(Typeface.BOLD), 0, alarmTimeSpan.length, 0)
		}

		// Set the text
		views.setTextViewText(R.id.alarm_time, alarmTimeSpan)
	}

	// Set text size
	views.setTextViewTextSize(R.id.hour, TypedValue.COMPLEX_UNIT_SP, shared.clockWidgetTimeTextSize)
	views.setTextViewTextSize(R.id.hour_bold, TypedValue.COMPLEX_UNIT_SP, shared.clockWidgetTimeTextSize)
	views.setTextViewTextSize(R.id.colon, TypedValue.COMPLEX_UNIT_SP, shared.clockWidgetTimeTextSize)
	views.setTextViewTextSize(R.id.minute, TypedValue.COMPLEX_UNIT_SP, shared.clockWidgetTimeTextSize)
	views.setTextViewTextSize(R.id.minute_bold, TypedValue.COMPLEX_UNIT_SP, shared.clockWidgetTimeTextSize)
	views.setTextViewTextSize(R.id.date, TypedValue.COMPLEX_UNIT_SP, shared.clockWidgetDateTextSize)
	views.setTextViewTextSize(R.id.date_bold, TypedValue.COMPLEX_UNIT_SP, shared.clockWidgetDateTextSize)
	views.setTextViewTextSize(R.id.alarm_time, TypedValue.COMPLEX_UNIT_SP, shared.clockWidgetAlarmTimeTextSize)

	// Set the background color and transparency
	val alpha = ((1f - shared.clockWidgetBackgroundTransparency/100f) * 255f).toInt()
	views.setInt(R.id.parent, "setBackgroundColor", Color.argb(alpha, 0, 0, 0))

	// Instruct the widget manager to update the widget
	appWidgetManager.updateAppWidget(appWidgetId, views)
}

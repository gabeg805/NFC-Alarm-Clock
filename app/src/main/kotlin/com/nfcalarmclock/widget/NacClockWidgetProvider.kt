package com.nfcalarmclock.widget

import android.app.AlarmManager
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.text.format.DateFormat
import android.util.TypedValue
import android.view.View
import android.widget.RemoteViews
import com.nfcalarmclock.R
import com.nfcalarmclock.main.NacMainActivity
import com.nfcalarmclock.shared.NacSharedPreferences
import com.nfcalarmclock.system.NacCalendar
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in [NacClockWidgetConfigureActivity]
 */
class NacClockWidgetProvider : AppWidgetProvider()
{

	/**
	 * Called when intent is received.
	 */
	override fun onReceive(context: Context, intent: Intent?)
	{
		// Super
		super.onReceive(context, intent)

		// Check if certain action pertaining to booting up, time change, or clock change
		if ((intent?.action == Intent.ACTION_BOOT_COMPLETED)
			|| (intent?.action == "android.intent.action.TIME_SET")
			|| (intent?.action == Intent.ACTION_TIMEZONE_CHANGED)
			|| (intent?.action == Intent.ACTION_LOCALE_CHANGED))
		{
			// Refresh all widgets
			println("onReceived() : refresh all widgets")
			refreshAllWidgets(context)
		}
		else if (intent?.action == AlarmManager.ACTION_NEXT_ALARM_CLOCK_CHANGED)
		{
			println("onReceived() : partially update all all widgets")
			partiallyUpdateAllWidgets(context)
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
		println("onUpdate() : partially update all widgets")
		// Partially update all widgets
		partiallyUpdateAllWidgets(
			context,
			appWidgetManager = appWidgetManager,
			appWidgetIds = appWidgetIds)
	}

}

/**
 * Partially update all the widgets.
 */
internal fun partiallyUpdateAllWidgets(
	context: Context,
	appWidgetManager: AppWidgetManager = AppWidgetManager.getInstance(context),
	appWidgetIds: IntArray = appWidgetManager.getAppWidgetIds(ComponentName(context, NacClockWidgetProvider::class.java))
)
{
	println("partiallyUpdateAllWidgets()")
	for (id in appWidgetIds)
	{
		partiallyUpdateWidget(context, appWidgetManager, id)
	}
}

/**
 * Partially update a widget.
 */
internal fun partiallyUpdateWidget(
	context: Context,
	appWidgetManager: AppWidgetManager,
	widgetId: Int
)
{
	// Construct the RemoteViews object
	val views = RemoteViews(context.packageName, R.layout.nac_clock_widget)

	// Build the clock widget helper
	val helper = NacClockWidgetDataHelper(context)

	// Update the widget
	views.updateText(helper)

	// Instruct the widget manager to partially update the widget
	appWidgetManager.partiallyUpdateAppWidget(widgetId, views)
}

/**
 * Refresh all the widgets.
 */
internal fun refreshAllWidgets(
	context: Context,
	appWidgetManager: AppWidgetManager = AppWidgetManager.getInstance(context),
	appWidgetIds: IntArray = appWidgetManager.getAppWidgetIds(ComponentName(context, NacClockWidgetProvider::class.java))
)
{
	println("refreshAllWidgets()")
	for (id in appWidgetIds)
	{
		refreshWidget(context, appWidgetManager, id)
	}
}

/**
 * Refresh a widget.
 */
internal fun refreshWidget(
	context: Context,
	appWidgetManager: AppWidgetManager = AppWidgetManager.getInstance(context),
	widgetId: Int
)
{
	// Construct the RemoteViews object
	val views = RemoteViews(context.packageName, R.layout.nac_clock_widget)

	// Build the clock widget helper
	val helper = NacClockWidgetDataHelper(context)

	// Refresh the widget
	views.updateVisuals(context, helper)
	views.updateText(helper)

	// Instruct the widget manager to update the widget
	appWidgetManager.updateAppWidget(widgetId, views)
}

/**
 * Update the text of a RemoteView.
 */
internal fun RemoteViews.updateText(helper: NacClockWidgetDataHelper)
{
	// Alarm visible
	if ((helper.alarmVis == View.VISIBLE) || (helper.alarmBoldVis == View.VISIBLE))
	{
		// Set the text
		this.setTextViewText(R.id.widget_alarm_time, helper.nextAlarm)
		this.setTextViewText(R.id.widget_alarm_time_above, helper.nextAlarm)
		this.setTextViewText(R.id.widget_alarm_time_below, helper.nextAlarm)
		this.setTextViewText(R.id.widget_alarm_time_bold, helper.nextAlarm)
		this.setTextViewText(R.id.widget_alarm_time_bold_above, helper.nextAlarm)
		this.setTextViewText(R.id.widget_alarm_time_bold_below, helper.nextAlarm)
	}

	// Date visible
	if ((helper.dateVis == View.VISIBLE) || (helper.dateBoldVis == View.VISIBLE))
	{
		this.setTextViewText(R.id.widget_date, helper.date)
		this.setTextViewText(R.id.widget_date_bold, helper.date)
	}
}

/**
 * Update the visual attributes of a RemoteView.
 */
internal fun RemoteViews.updateVisuals(
	context: Context,
	helper: NacClockWidgetDataHelper
)
{
	// Get the shared preferences
	val shared = helper.sharedPreferences

	// Set on click pending intent
	val pendingIntent = NacMainActivity.getStartPendingIntent(context)
	this.setOnClickPendingIntent(R.id.widget_parent, pendingIntent)

	// Set view visibility
	this.setViewVisibility(R.id.widget_time, helper.timeVis)
	this.setViewVisibility(R.id.widget_hour, helper.hourVis)
	this.setViewVisibility(R.id.widget_hour_bold, helper.hourBoldVis)
	this.setViewVisibility(R.id.widget_minute, helper.minuteVis)
	this.setViewVisibility(R.id.widget_minute_bold, helper.minuteBoldVis)
	this.setViewVisibility(R.id.widget_am_pm, helper.meridianVis)
	this.setViewVisibility(R.id.widget_am_pm_bold, helper.meridianBoldVis)
	this.setViewVisibility(R.id.widget_date, helper.dateVis)
	this.setViewVisibility(R.id.widget_date_bold, helper.dateBoldVis)
	this.setViewVisibility(R.id.widget_alarm_icon, helper.alarmIconVis)
	this.setViewVisibility(R.id.widget_alarm_icon_above, helper.alarmIconVis)
	this.setViewVisibility(R.id.widget_alarm_icon_below, helper.alarmIconVis)
	this.setViewVisibility(R.id.widget_alarm_time, helper.alarmVis)
	this.setViewVisibility(R.id.widget_alarm_time_above, helper.alarmVis)
	this.setViewVisibility(R.id.widget_alarm_time_below, helper.alarmVis)
	this.setViewVisibility(R.id.widget_alarm_time_bold, helper.alarmBoldVis)
	this.setViewVisibility(R.id.widget_alarm_time_bold_above, helper.alarmBoldVis)
	this.setViewVisibility(R.id.widget_alarm_time_bold_below, helper.alarmBoldVis)
	this.setViewVisibility(R.id.widget_alarm_same_line_as_date_container, helper.alarmPositionSameLineAsDateVis)
	this.setViewVisibility(R.id.widget_alarm_above_container, helper.alarmPositionAboveDateVis)
	this.setViewVisibility(R.id.widget_alarm_below_container, helper.alarmPositionBelowDateVis)

	// Set the gravity
	this.setInt(R.id.widget_time, "setGravity", helper.gravity)
	this.setInt(R.id.widget_alarm_date_inline_container, "setGravity", helper.gravity)
	this.setInt(R.id.widget_alarm_above_container, "setGravity", helper.gravity)
	this.setInt(R.id.widget_alarm_below_container, "setGravity", helper.gravity)

	// Set the background color and transparency
	this.setInt(R.id.widget_parent, "setBackgroundColor", helper.bgColor)

	// Set text and icon colors
	this.setTextColor(R.id.widget_hour, shared.clockWidgetHourColor)
	this.setTextColor(R.id.widget_hour_bold, shared.clockWidgetHourColor)
	this.setTextColor(R.id.widget_colon, shared.clockWidgetMinuteColor)
	this.setTextColor(R.id.widget_minute, shared.clockWidgetMinuteColor)
	this.setTextColor(R.id.widget_minute_bold, shared.clockWidgetMinuteColor)
	this.setTextColor(R.id.widget_am_pm, shared.clockWidgetAmPmColor)
	this.setTextColor(R.id.widget_am_pm_bold, shared.clockWidgetAmPmColor)
	this.setTextColor(R.id.widget_date, shared.clockWidgetDateColor)
	this.setTextColor(R.id.widget_date_bold, shared.clockWidgetDateColor)
	this.setTextColor(R.id.widget_alarm_time, shared.clockWidgetAlarmTimeColor)
	this.setTextColor(R.id.widget_alarm_time_above, shared.clockWidgetAlarmTimeColor)
	this.setTextColor(R.id.widget_alarm_time_below, shared.clockWidgetAlarmTimeColor)
	this.setTextColor(R.id.widget_alarm_time_bold, shared.clockWidgetAlarmTimeColor)
	this.setTextColor(R.id.widget_alarm_time_bold_above, shared.clockWidgetAlarmTimeColor)
	this.setTextColor(R.id.widget_alarm_time_bold_below, shared.clockWidgetAlarmTimeColor)
	this.setInt(R.id.widget_alarm_icon, "setColorFilter", shared.clockWidgetAlarmIconColor)
	this.setInt(R.id.widget_alarm_icon_above, "setColorFilter", shared.clockWidgetAlarmIconColor)
	this.setInt(R.id.widget_alarm_icon_below, "setColorFilter", shared.clockWidgetAlarmIconColor)

	// Set text size
	this.setTextViewTextSize(R.id.widget_hour, TypedValue.COMPLEX_UNIT_SP, shared.clockWidgetTimeTextSize)
	this.setTextViewTextSize(R.id.widget_hour_bold, TypedValue.COMPLEX_UNIT_SP, shared.clockWidgetTimeTextSize)
	this.setTextViewTextSize(R.id.widget_colon, TypedValue.COMPLEX_UNIT_SP, shared.clockWidgetTimeTextSize)
	this.setTextViewTextSize(R.id.widget_minute, TypedValue.COMPLEX_UNIT_SP, shared.clockWidgetTimeTextSize)
	this.setTextViewTextSize(R.id.widget_minute_bold, TypedValue.COMPLEX_UNIT_SP, shared.clockWidgetTimeTextSize)
	this.setTextViewTextSize(R.id.widget_am_pm, TypedValue.COMPLEX_UNIT_SP, shared.clockWidgetAmPmTextSize)
	this.setTextViewTextSize(R.id.widget_am_pm_bold, TypedValue.COMPLEX_UNIT_SP, shared.clockWidgetAmPmTextSize)
	this.setTextViewTextSize(R.id.widget_date, TypedValue.COMPLEX_UNIT_SP, shared.clockWidgetDateTextSize)
	this.setTextViewTextSize(R.id.widget_date_bold, TypedValue.COMPLEX_UNIT_SP, shared.clockWidgetDateTextSize)
	this.setTextViewTextSize(R.id.widget_alarm_time, TypedValue.COMPLEX_UNIT_SP, shared.clockWidgetAlarmTimeTextSize)
	this.setTextViewTextSize(R.id.widget_alarm_time_above, TypedValue.COMPLEX_UNIT_SP, shared.clockWidgetAlarmTimeTextSize)
	this.setTextViewTextSize(R.id.widget_alarm_time_below, TypedValue.COMPLEX_UNIT_SP, shared.clockWidgetAlarmTimeTextSize)
	this.setTextViewTextSize(R.id.widget_alarm_time_bold, TypedValue.COMPLEX_UNIT_SP, shared.clockWidgetAlarmTimeTextSize)
	this.setTextViewTextSize(R.id.widget_alarm_time_bold_above, TypedValue.COMPLEX_UNIT_SP, shared.clockWidgetAlarmTimeTextSize)
	this.setTextViewTextSize(R.id.widget_alarm_time_bold_below, TypedValue.COMPLEX_UNIT_SP, shared.clockWidgetAlarmTimeTextSize)

	// Set margin
	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
	{
		// Get the average text size, depending on if the alarm is shown inline or
		// different line that the date
		val avgTextSize = if (shared.clockWidgetAlarmTimePositionSameLineAsDate)
		{
			(shared.clockWidgetDateTextSize+shared.clockWidgetAlarmTimeTextSize) / 2
		}
		else
		{
			shared.clockWidgetAlarmTimeTextSize
		}

		// Calculate the new margin
		val newMargin = NacClockWidgetDataHelper.calcAlarmIconMargin(context, avgTextSize)

		// Start
		this.setViewLayoutMargin(R.id.widget_alarm_icon, RemoteViews.MARGIN_START,
			newMargin, TypedValue.COMPLEX_UNIT_DIP)

		// End
		this.setViewLayoutMargin(R.id.widget_alarm_icon, RemoteViews.MARGIN_END,
			newMargin, TypedValue.COMPLEX_UNIT_DIP)
		this.setViewLayoutMargin(R.id.widget_alarm_icon_above, RemoteViews.MARGIN_END,
			newMargin, TypedValue.COMPLEX_UNIT_DIP)
		this.setViewLayoutMargin(R.id.widget_alarm_icon_below, RemoteViews.MARGIN_END,
			newMargin, TypedValue.COMPLEX_UNIT_DIP)
	}
}

/**
 * Helper for determining various important aspects of the clock widget, such as what
 * views should be visible, text for the alarm, should views be bold or not, etc.
 */
internal class NacClockWidgetDataHelper(

	/**
	 * Context
	 */
	val context: Context,

	/**
	 * Shared preferences.
	 */
	val sharedPreferences: NacSharedPreferences = NacSharedPreferences(context)

)
{


	/**
	 * AM/PM string, if present in the current locale.
	 */
	private val amPm: String
		get()
		{
			// Get the current calendar
			val cal = Calendar.getInstance()

			// Return the meridian
			return NacCalendar.getMeridian(context, cal[Calendar.HOUR_OF_DAY])
		}

	/**
	 * Gravity.
	 */
	val gravity: Int
		get()
		{
			return sharedPreferences.clockWidgetGeneralAlignment
		}

	/**
	 * Time visibility.
	 */
	val timeVis: Int
		get()
		{
			return if (sharedPreferences.shouldClockWidgetShowTime)
			{
				View.VISIBLE
			}
			else
			{
				View.GONE
			}
		}

	/**
	 * Bold hour visibility.
	 */
	val hourBoldVis: Int
		get()
		{
			return if (sharedPreferences.shouldClockWidgetShowTime && sharedPreferences.shouldClockWidgetBoldHour)
			{
				View.VISIBLE
			}
			else
			{
				View.GONE
			}
		}

	/**
	 * Regular hour visibility.
	 */
	val hourVis: Int
		get()
		{
			return if (hourBoldVis == View.VISIBLE)
			{
				View.GONE
			}
			else
			{
				View.VISIBLE
			}
		}

	/**
	 * Bold minute visibility.
	 */
	val minuteBoldVis: Int
		get()
		{
			return if (sharedPreferences.shouldClockWidgetShowTime && sharedPreferences.shouldClockWidgetBoldMinute)
			{
				View.VISIBLE
			}
			else
			{
				View.GONE
			}
		}

	/**
	 * Regular minute visibility.
	 */
	val minuteVis: Int
		get()
		{
			return if (minuteBoldVis == View.VISIBLE)
			{
				View.GONE
			}
			else
			{
				View.VISIBLE
			}
		}

	/**
	 * Bold meridian visibility.
	 */
	val meridianBoldVis: Int
		get()
		{
			return if (amPm.isNotEmpty() && sharedPreferences.shouldClockWidgetBoldAmPm)
			{
				View.VISIBLE
			}
			else
			{
				View.GONE
			}
		}

	/**
	 * Regular meridian visibility.
	 */
	val meridianVis: Int
		get()
		{
			return if (meridianBoldVis == View.VISIBLE)
			{
				View.GONE
			}
			else
			{
				View.VISIBLE
			}
		}

	/**
	 * Bold date visibility.
	 */
	val dateBoldVis: Int
		get()
		{
			return if (sharedPreferences.shouldClockWidgetShowDate && sharedPreferences.shouldClockWidgetBoldDate)
			{
				View.VISIBLE
			}
			else
			{
				View.GONE
			}
		}

	/**
	 * Regular date visibility.
	 */
	val dateVis: Int
		get()
		{
			return if (sharedPreferences.shouldClockWidgetShowDate && !sharedPreferences.shouldClockWidgetBoldDate)
			{
				View.VISIBLE
			}
			else
			{
				View.GONE
			}
		}

	/**
	 * Bold Alarm visibility.
	 */
	val alarmBoldVis: Int
		get()
		{
			return if (sharedPreferences.shouldClockWidgetShowAlarm && (nextAlarmCal != null) && sharedPreferences.shouldClockWidgetBoldAlarmTime)
			{
				View.VISIBLE
			}
			else
			{
				View.GONE
			}
		}

	/**
	 * Alarm icon visibility.
	 */
	val alarmIconVis: Int
		get()
		{
			return if (sharedPreferences.shouldClockWidgetShowAlarm && (nextAlarmCal != null))
			{
				View.VISIBLE
			}
			else
			{
				View.GONE
			}
		}

	/**
	 * Regular alarm visibility.
	 */
	val alarmVis: Int
		get()
		{
			return if (sharedPreferences.shouldClockWidgetShowAlarm && (nextAlarmCal != null) && !sharedPreferences.shouldClockWidgetBoldAlarmTime)
			{
				View.VISIBLE
			}
			else
			{
				View.GONE
			}
		}

	/**
	 * Alarm position above date visibility.
	 */
	val alarmPositionAboveDateVis: Int
		get()
		{
			return if (sharedPreferences.clockWidgetAlarmTimePositionAboveDate)
			{
				View.VISIBLE
			}
			else
			{
				View.GONE
			}
		}

	/**
	 * Alarm position same linee as date visibility.
	 */
	val alarmPositionSameLineAsDateVis: Int
		get()
		{
			return if (sharedPreferences.clockWidgetAlarmTimePositionSameLineAsDate)
			{
				View.VISIBLE
			}
			else
			{
				View.GONE
			}
		}

	/**
	 * Alarm position below date visibility.
	 */
	val alarmPositionBelowDateVis: Int
		get()
		{
			return if (sharedPreferences.clockWidgetAlarmTimePositionBelowDate)
			{
				View.VISIBLE
			}
			else
			{
				View.GONE
			}
		}

	/**
	 * ARGB background color.
	 */
	val bgColor: Int
		get()
		{
			return calcBackgroundColor(sharedPreferences.clockWidgetBackgroundColor,
				sharedPreferences.clockWidgetBackgroundTransparency)
		}

	/**
	 * The next alarm calendar.
	 */
	private val nextAlarmCal: Calendar?
		get()
		{
			// Create a calendar for the next alarm time
			val alarmCal = Calendar.getInstance()

			// App next alarm
			if (sharedPreferences.appShouldSaveNextAlarm && sharedPreferences.shouldClockWidgetShowAppSpecificAlarms)
			{
				// Get the current time in milliseconds and compute any timezone offset
				val millis = sharedPreferences.appNextAlarmTimeMillis
				val fromTimezone = alarmCal.timeZone
				val toTimezone = TimeZone.getTimeZone(sharedPreferences.appNextAlarmTimezoneId)
				val offset = fromTimezone.getOffset(millis) - toTimezone.getOffset(millis)

				// Return if there is not a next alarm set
				if (millis == 0L)
				{
					return null
				}

				// Set the next alarm time
				alarmCal.timeInMillis = millis - offset
			}
			else
			{
				// Get the alarm manager
				val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

				// Set the next alarm time or return null if there is no next alarm
				alarmCal.timeInMillis = alarmManager.nextAlarmClock?.triggerTime ?: return null
			}

			return alarmCal
		}

	/**
	 * Time at which the next alarm will run.
	 */
	val nextAlarm: String
		get()
		{
			// Check if the alarm time should be customized
			if ((alarmVis != View.VISIBLE) && (alarmBoldVis != View.VISIBLE))
			{
				return ""
			}

			// Return the alarm time as a spannable string
			return NacCalendar.getFullTime(context, nextAlarmCal!!).replace("  ", " ")
		}

	/**
	 * Date in the current locale.
	 */
	val date: String
		get()
		{
			val locale = Locale.getDefault()
			val now = Calendar.getInstance()
			val skeletonFormat = "E, MMM d"
			val betterFormat = DateFormat.getBestDateTimePattern(locale, skeletonFormat)

			return DateFormat.format(betterFormat, now).toString()
		}

	companion object
	{

		/**
		 * Calculate what the alarm icon margin should be.
		 */
		fun calcAlarmIconMargin(context: Context, textSize: Float): Float
		{
			// Get the base margin
			val res = context.resources
			val baseMargin = res.getDimension(R.dimen.nudge) / res.displayMetrics.density

			// Determine the correct margin
			return if (textSize >= 20)
			{
				2 * baseMargin
			}
			else if (textSize >= 14)
			{
				baseMargin
			}
			else
			{
				0f
			}
		}

		/**
		 * Calculate the correct background color + alpha channel.
		 */
		fun calcBackgroundColor(color: Int, transparency: Int): Int
		{
			// Compute the ARGB components of the color
			val alpha = ((1f - transparency / 100f) * 255f).toInt()
			val r = Color.red(color)
			val g = Color.green(color)
			val b = Color.blue(color)

			// Return the color
			return Color.argb(alpha, r, g, b)
		}

	}

}

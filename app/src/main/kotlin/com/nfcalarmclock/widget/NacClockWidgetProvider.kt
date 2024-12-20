package com.nfcalarmclock.widget

import android.app.AlarmManager
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
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

	override fun onReceive(context: Context, intent: Intent?)
	{
		// Super
		super.onReceive(context, intent)

		// Check if certain action pertaining to booting up, time change, or clock change
		if ((intent?.action == Intent.ACTION_BOOT_COMPLETED)
			|| (intent?.action == "android.intent.action.TIME_SET")
			|| (intent?.action == Intent.ACTION_TIMEZONE_CHANGED)
			|| (intent?.action == Intent.ACTION_LOCALE_CHANGED)
			|| (intent?.action == AlarmManager.ACTION_NEXT_ALARM_CLOCK_CHANGED))
		{
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
	// Get the list of widget IDs
	val componentName = ComponentName(context, NacClockWidgetProvider::class.java)
	val appWidgetManager = AppWidgetManager.getInstance(context)
	val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)

	// Iterate over each widget ID and update it
	for (id in appWidgetIds)
	{
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
	// Construct the RemoteViews object
	val views = RemoteViews(context.packageName, R.layout.nac_clock_widget)

	// Set on click pending intent
	val pendingIntent = NacMainActivity.getStartPendingIntent(context)
	views.setOnClickPendingIntent(R.id.widget_parent, pendingIntent)

	// Get the clock widget helper
	val shared = NacSharedPreferences(context)
	val helper = NacClockWidgetDataHelper(context, shared)

	// Set view visibility
	views.setViewVisibility(R.id.widget_time, helper.timeVis)
	views.setViewVisibility(R.id.widget_hour, helper.hourVis)
	views.setViewVisibility(R.id.widget_hour_bold, helper.hourBoldVis)
	views.setViewVisibility(R.id.widget_minute, helper.minuteVis)
	views.setViewVisibility(R.id.widget_minute_bold, helper.minuteBoldVis)
	views.setViewVisibility(R.id.widget_am_pm, helper.meridianVis)
	views.setViewVisibility(R.id.widget_am_pm_bold, helper.meridianBoldVis)
	views.setViewVisibility(R.id.widget_date, helper.dateVis)
	views.setViewVisibility(R.id.widget_date_bold, helper.dateBoldVis)
	views.setViewVisibility(R.id.widget_alarm_icon, helper.alarmIconVis)
	views.setViewVisibility(R.id.widget_alarm_time, helper.alarmVis)
	views.setViewVisibility(R.id.widget_alarm_time_bold, helper.alarmBoldVis)

	// Check if the alarm time should be customized
	if ((helper.alarmVis == View.VISIBLE) || (helper.alarmBoldVis == View.VISIBLE))
	{
		// Set the text
		views.setTextViewText(R.id.widget_alarm_time, helper.nextAlarm)
		views.setTextViewText(R.id.widget_alarm_time_bold, helper.nextAlarm)
	}

	// Set the background color and transparency
	views.setInt(R.id.widget_parent, "setBackgroundColor", helper.bgColor)

	// Set text and icon colors
	views.setTextColor(R.id.widget_hour, shared.clockWidgetHourColor)
	views.setTextColor(R.id.widget_hour_bold, shared.clockWidgetHourColor)
	views.setTextColor(R.id.widget_colon, shared.clockWidgetMinuteColor)
	views.setTextColor(R.id.widget_minute, shared.clockWidgetMinuteColor)
	views.setTextColor(R.id.widget_minute_bold, shared.clockWidgetMinuteColor)
	views.setTextColor(R.id.widget_am_pm, shared.clockWidgetAmPmColor)
	views.setTextColor(R.id.widget_am_pm_bold, shared.clockWidgetAmPmColor)
	views.setTextColor(R.id.widget_date, shared.clockWidgetDateColor)
	views.setTextColor(R.id.widget_date_bold, shared.clockWidgetDateColor)
	views.setTextColor(R.id.widget_alarm_time, shared.clockWidgetAlarmTimeColor)
	views.setTextColor(R.id.widget_alarm_time_bold, shared.clockWidgetAlarmTimeColor)
	views.setInt(R.id.widget_alarm_icon, "setColorFilter", shared.clockWidgetAlarmIconColor)

	// Set text size
	views.setTextViewTextSize(R.id.widget_hour, TypedValue.COMPLEX_UNIT_SP, shared.clockWidgetTimeTextSize)
	views.setTextViewTextSize(R.id.widget_hour_bold, TypedValue.COMPLEX_UNIT_SP, shared.clockWidgetTimeTextSize)
	views.setTextViewTextSize(R.id.widget_colon, TypedValue.COMPLEX_UNIT_SP, shared.clockWidgetTimeTextSize)
	views.setTextViewTextSize(R.id.widget_minute, TypedValue.COMPLEX_UNIT_SP, shared.clockWidgetTimeTextSize)
	views.setTextViewTextSize(R.id.widget_minute_bold, TypedValue.COMPLEX_UNIT_SP, shared.clockWidgetTimeTextSize)
	views.setTextViewTextSize(R.id.widget_am_pm, TypedValue.COMPLEX_UNIT_SP, shared.clockWidgetAmPmTextSize)
	views.setTextViewTextSize(R.id.widget_am_pm_bold, TypedValue.COMPLEX_UNIT_SP, shared.clockWidgetAmPmTextSize)
	views.setTextViewTextSize(R.id.widget_date, TypedValue.COMPLEX_UNIT_SP, shared.clockWidgetDateTextSize)
	views.setTextViewTextSize(R.id.widget_date_bold, TypedValue.COMPLEX_UNIT_SP, shared.clockWidgetDateTextSize)
	views.setTextViewTextSize(R.id.widget_alarm_time, TypedValue.COMPLEX_UNIT_SP, shared.clockWidgetAlarmTimeTextSize)
	views.setTextViewTextSize(R.id.widget_alarm_time_bold, TypedValue.COMPLEX_UNIT_SP, shared.clockWidgetAlarmTimeTextSize)

	// Set margin
	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
	{
		// Calculate the average text size
		val avgTextSize = (shared.clockWidgetDateTextSize+shared.clockWidgetAlarmTimeTextSize) / 2
		val newMargin = NacClockWidgetDataHelper.calcAlarmIconMargin(context, avgTextSize)

		// Start
		views.setViewLayoutMargin(R.id.widget_alarm_icon, RemoteViews.MARGIN_START,
			newMargin, TypedValue.COMPLEX_UNIT_DIP)

		// End
		views.setViewLayoutMargin(R.id.widget_alarm_icon, RemoteViews.MARGIN_END,
			newMargin, TypedValue.COMPLEX_UNIT_DIP)
	}

	// Instruct the widget manager to update the widget
	appWidgetManager.updateAppWidget(appWidgetId, views)
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
	val sharedPreferences: NacSharedPreferences

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
			return if (sharedPreferences.shouldClockWidgetShowAlarm && (nextAlarmInfo != null) && sharedPreferences.shouldClockWidgetBoldAlarmTime)
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
			return if (sharedPreferences.shouldClockWidgetShowAlarm && (nextAlarmInfo != null))
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
			return if (sharedPreferences.shouldClockWidgetShowAlarm && (nextAlarmInfo != null) && !sharedPreferences.shouldClockWidgetBoldAlarmTime)
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
	 * The next alarm info.
	 */
	private val nextAlarmInfo: AlarmManager.AlarmClockInfo?
		get()
		{
			// Get the alarm manager
			val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

			// Return the next alarm
			return alarmManager.nextAlarmClock
		}

	/**
	 * String containing the time at which the next alarm will run.
	 */
	val nextAlarm: String
		get()
		{
			// Check if the alarm time should be customized
			if ((alarmVis != View.VISIBLE) && (alarmBoldVis != View.VISIBLE))
			{
				return ""
			}

			// Create a calendar with the next alarm time
			val alarmCal = Calendar.getInstance()
			alarmCal.timeInMillis = nextAlarmInfo!!.triggerTime

			// Return the alarm time as a spannable string
			return NacCalendar.getFullTime(context, alarmCal).replace("  ", " ")
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
			else
			{
				baseMargin
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

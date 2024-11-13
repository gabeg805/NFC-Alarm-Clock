package com.nfcalarmclock.alarm.options.tts

import android.content.Context
import android.content.res.Resources
import android.os.Build
import com.nfcalarmclock.R
import com.nfcalarmclock.util.NacCalendar

/**
 * How to translate certain phrases to other languages.
 */
object NacTranslate
{

	/**
	 * Get how to say the alarm name in any language.
	 */
	@Suppress("unused_parameter")
	fun getSayAlarmName(
		resources: Resources,
		name: String
	): String
	{
		return name
	}

	/**
	 * Get how to say the current time in any language.
	 */
	@Suppress("deprecation")
	fun getSayCurrentTime(
		context: Context,
		hour: Int,
		minute: Int
	): String
	{
		// Get the current locale being used on the device
		val locale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
		{
			Resources.getSystem().configuration.locales.get(0)
		}
		else
		{
			Resources.getSystem().configuration.locale
		}

		// Check if using English
		val extra = if (locale.language.equals("en"))
		{
			// Check if the minute should be said as "oh" e.g. 8:05 would be eight oh five
			if (minute in 1..9) "O" else ""
		}
		// Using non-english language
		else
		{
			// Do nothing for extra
			""
		}

		// Get the meridian (if it should be used based on the user's preferences)
		val meridian = NacCalendar.getMeridian(context, hour)

		// Check if the meridian is null or empty. This means the time is in
		// 24 hour format
		return if (meridian.isEmpty())
		{
			// Return the statement that should be said
			context.resources.getQuantityString(R.plurals.tts_say_time_24h, minute,
				hour, minute, extra)
		}
		// The time is in 12 hour format
		else
		{
			// Convert the hour to 12 hour format
			val showHour = NacCalendar.to12HourFormat(hour)
			val showMinute = if (minute == 0) "" else minute.toString()

			// Return the statement that should be said
			context.resources.getQuantityString(R.plurals.tts_say_time_12h, hour,
				showHour, showMinute, meridian, extra)
		}
	}

	/**
	 * Get how to say the alarm reminder in any language.
	 */
	fun getSayReminder(
		context: Context,
		name: String,
		minute: Int
	): String
	{
		// Get the alarm name if it is set, but if it is empty, then get the
		// generic name for an alarm
		val reminder = name.ifEmpty { context.resources.getString(R.string.word_alarm) }

		// Return the statement that should be said
		return context.resources.getQuantityString(R.plurals.tts_say_reminder, minute,
			reminder, minute)
	}

}
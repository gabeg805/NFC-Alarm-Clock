package com.nfcalarmclock.alarm.options.tts

import android.content.Context
import android.content.res.Resources
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
	@Suppress("Unused")
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
	fun getSayCurrentTime(
		context: Context,
		hour: Int,
		minute: Int
	): String
	{
		// Get the meridian (if it should be used based on the user's preferences)
		val meridian = NacCalendar.getMeridian(context, hour)

		// Get the hour and minutes how they should be said by TTS
		val showHour = if (meridian.isNotEmpty()) hour.toString() else NacCalendar.to12HourFormat(hour)
		val showMinute = minute.toString().padStart(2, '0')

		// Return the TTS phrase
		return context.resources.getString(R.string.tts_say_time, showHour, showMinute, meridian)
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
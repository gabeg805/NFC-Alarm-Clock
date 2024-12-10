package com.nfcalarmclock.util

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.AlarmClock
import com.nfcalarmclock.R
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.shared.NacSharedPreferences
import com.nfcalarmclock.util.NacCalendar.Day
import com.nfcalarmclock.util.NacIntent.MEDIA_BUNDLE_NAME
import com.nfcalarmclock.util.media.NacMedia
import java.util.Calendar

/**
 * Add a media information to an intent.
 *
 * @param mediaPath A media path.
 * @param shuffleMedia Whether to shuffle media or not.
 * @param recursivelyPlayMedia Whether to recursively play media or not.
 *
 * @return The intent that was passed in with the media path and how to
 *         play the media inside a bundle in that intent.
 */
fun Intent.addMediaInfo(
	mediaPath: String,
	mediaArtist: String,
	mediaTitle: String,
	mediaType: Int,
	shuffleMedia: Boolean,
	recursivelyPlayMedia: Boolean
): Intent
{
	// Create a bundle with the media
	val bundle = Bundle().addMediaInfo(mediaPath, mediaArtist, mediaTitle, mediaType,
		shuffleMedia, recursivelyPlayMedia)

	// Add the bundle to the intent
	this.putExtra(MEDIA_BUNDLE_NAME, bundle)

	return this
}

/**
 * Get the media bundle from an intent.
 *
 * @return The media bundle from an intent.
 */
fun Intent.getMediaBundle(): Bundle
{
	// Get the bundle from the intent
	return this.getBundleExtra(MEDIA_BUNDLE_NAME) ?: Bundle()
}

/**
 * Intent helper object.
 */
object NacIntent
{

	/**
	 * Tag name for retrieving a NacAlarm from a bundle.
	 */
	private const val ALARM_BUNDLE_NAME = "NacAlarmBundle"

	/**
	 * Tag name for retrieving a media path from a bundle.
	 */
	const val MEDIA_BUNDLE_NAME = "NacMediaBundle"

	/**
	 * Add an alarm to an intent.
	 *
	 * @param intent An intent.
	 * @param alarm  An alarm.
	 *
	 * @return The passed in intent with the alarm.
	 */
	fun addAlarm(intent: Intent, alarm: NacAlarm?): Intent
	{
		// Create a bundle with the alarm
		val bundle = NacBundle.alarmToBundle(alarm)

		// Add the bundle to the intent
		intent.putExtra(ALARM_BUNDLE_NAME, bundle)

		return intent
	}

	/**
	 * Get the alarm associated with the given Intent.
	 *
	 * @param intent An intent.
	 *
	 * @return The alarm associated with the given Intent.
	 */
	fun getAlarm(intent: Intent?): NacAlarm?
	{
		// Check if the intent is null
		if (intent == null)
		{
			return null
		}

		// Get the bundle from the intent
		val bundle = intent.getBundleExtra(ALARM_BUNDLE_NAME)

		// Get the alarm from the bundle
		return NacBundle.getAlarm(bundle)
	}

	/**
	 * Get the alarm that was specified using the SET_ALARM action.
	 *
	 * @return The alarm that was specified using the SET_ALARM action.
	 */
	fun getSetAlarm(context: Context, intent: Intent): NacAlarm?
	{
		// Check if the intent action is NOT for a SET_ALARM intent
		if (intent.action != AlarmClock.ACTION_SET_ALARM)
		{
			return null
		}

		val shared = NacSharedPreferences(context)
		val alarm = NacAlarm.build(shared)
		val calendar = Calendar.getInstance()
		var isSet = false

		// Check if the HOUR is in the intent
		if (intent.hasExtra(AlarmClock.EXTRA_HOUR))
		{
			val hour = intent.getIntExtra(AlarmClock.EXTRA_HOUR,
				calendar[Calendar.HOUR_OF_DAY])
			isSet = true

			// Add to the alarm
			alarm.hour = hour
		}

		// Check if the MINUTES is in the intent
		if (intent.hasExtra(AlarmClock.EXTRA_MINUTES))
		{
			val minute = intent.getIntExtra(AlarmClock.EXTRA_MINUTES,
				calendar[Calendar.MINUTE])
			isSet = true

			// Add to the alarm
			alarm.minute = minute
		}

		// Check if the MESSAGE (Name) is in the intent
		if (intent.hasExtra(AlarmClock.EXTRA_MESSAGE))
		{
			val name = intent.getStringExtra(AlarmClock.EXTRA_MESSAGE)
			isSet = true

			// Add to the alarm
			alarm.name = name ?: ""
		}

		// Check if the DAYS is in the intent
		if (intent.hasExtra(AlarmClock.EXTRA_DAYS))
		{
			val extraDays = intent.getIntegerArrayListExtra(AlarmClock.EXTRA_DAYS)
			val days = Day.NONE
			isSet = true

			// Iterate over each day
			if (extraDays != null)
			{
				for (d in extraDays)
				{
					days.add(Day.calendarDayToDay(d))
				}
			}

			// Add to the alarm
			alarm.days = days
		}

		// Check if the RINGTONE is in the intent
		if (intent.hasExtra(AlarmClock.EXTRA_RINGTONE))
		{
			val ringtone = intent.getStringExtra(AlarmClock.EXTRA_RINGTONE) ?: ""
			isSet = true

			// Add to the alarm
			alarm.mediaPath = ringtone
			alarm.mediaArtist = NacMedia.getArtist(context, ringtone)
			alarm.mediaTitle = NacMedia.getTitle(context, ringtone)
			alarm.mediaType = NacMedia.getType(context, ringtone)
		}

		// Check if the VIBRATE is in the intent
		if (intent.hasExtra(AlarmClock.EXTRA_VIBRATE))
		{
			val defaultVibrate = context.resources.getBoolean(R.bool.default_vibrate)
			val vibrate = intent.getBooleanExtra(AlarmClock.EXTRA_VIBRATE,
				defaultVibrate)
			isSet = true

			// Add to the alarm
			alarm.vibrate = vibrate
		}

		//getBooleanExtra(AlarmClock.EXTRA_SKIP_UI);
		// Check if one or more alarm attributes were set
		return if (isSet)
			{
				alarm
			}
			else
			{
				null
			}
	}

}
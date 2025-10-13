package com.nfcalarmclock.system

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.AlarmClock
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.shared.NacSharedPreferences
import com.nfcalarmclock.timer.db.NacTimer
import com.nfcalarmclock.util.media.buildLocalMediaPath
import com.nfcalarmclock.util.media.getMediaArtist
import com.nfcalarmclock.util.media.getMediaTitle
import com.nfcalarmclock.util.media.getMediaType
import java.util.Calendar

/**
 * Tag name for retrieving a NacAlarm from a bundle.
 */
const val ALARM_BUNDLE_NAME = "NacAlarmBundle"

/**
 * Tag name for retrieving a media path from a bundle.
 */
const val MEDIA_BUNDLE_NAME = "NacMediaBundle"

/**
 * Tag name for retrieving a NacTimer from a bundle.
 */
const val TIMER_BUNDLE_NAME = "NacTimerBundle"

/**
 * Add an alarm to an intent.
 *
 * @param alarm An alarm.
 *
 * @return The passed in intent with the alarm.
 */
fun Intent.addAlarm(alarm: NacAlarm?): Intent
{
	// Create a bundle with the alarm
	val bundle = Bundle().addAlarm(alarm)

	// Add the bundle to the intent
	this.putExtra(ALARM_BUNDLE_NAME, bundle)

	return this
}

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
 * Add a timer to an intent.
 *
 * @param timer A timer.
 *
 * @return The passed in intent with the timer.
 */
fun Intent.addTimer(timer: NacTimer?): Intent
{
	// Create a bundle with the timer
	val bundle = Bundle().addTimer(timer)

	// Add the bundle to the intent
	this.putExtra(TIMER_BUNDLE_NAME, bundle)

	return this
}

/**
 * Get the alarm associated with the given Intent.
 *
 * @return The alarm associated with the given Intent.
 */
fun Intent.getAlarm(): NacAlarm?
{
	// Get the bundle from the intent
	val bundle = this.getBundleExtra(ALARM_BUNDLE_NAME)

	// Get the alarm from the bundle
	return bundle?.getAlarm()
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
 * Get the alarm that was specified using the SET_ALARM action.
 *
 * @return The alarm that was specified using the SET_ALARM action.
 */
fun Intent.getSetAlarm(context: Context): NacAlarm?
{
	// Check if the intent action is NOT for a SET_ALARM intent
	if (this.action != AlarmClock.ACTION_SET_ALARM)
	{
		return null
	}

	val shared = NacSharedPreferences(context)
	val alarm = NacAlarm.build(shared)
	val calendar = Calendar.getInstance()
	var isSet = false

	// Check if the HOUR is in the intent
	if (this.hasExtra(AlarmClock.EXTRA_HOUR))
	{
		val hour = this.getIntExtra(AlarmClock.EXTRA_HOUR,
			calendar[Calendar.HOUR_OF_DAY])
		isSet = true

		// Add to the alarm
		alarm.hour = hour
	}

	// Check if the MINUTES is in the intent
	if (this.hasExtra(AlarmClock.EXTRA_MINUTES))
	{
		val minute = this.getIntExtra(AlarmClock.EXTRA_MINUTES,
			calendar[Calendar.MINUTE])
		isSet = true

		// Add to the alarm
		alarm.minute = minute
	}

	// Check if the MESSAGE (Name) is in the intent
	if (this.hasExtra(AlarmClock.EXTRA_MESSAGE))
	{
		val name = this.getStringExtra(AlarmClock.EXTRA_MESSAGE)
		isSet = true

		// Add to the alarm
		alarm.name = name ?: ""
	}

	// Check if the DAYS is in the intent
	if (this.hasExtra(AlarmClock.EXTRA_DAYS))
	{
		val extraDays = this.getIntegerArrayListExtra(AlarmClock.EXTRA_DAYS)
		val days = NacCalendar.Day.Companion.NONE
		isSet = true

		// Iterate over each day
		if (extraDays != null)
		{
			for (d in extraDays)
			{
				days.add(d.toDay())
			}
		}

		// Add to the alarm
		alarm.days = days
	}

	// Check if the RINGTONE is in the intent
	if (this.hasExtra(AlarmClock.EXTRA_RINGTONE))
	{
		// Get the ringtone
		val ringtone = this.getStringExtra(AlarmClock.EXTRA_RINGTONE) ?: ""
		val uri = ringtone.toUri()
		isSet = true

		// Add to the alarm
		alarm.mediaPath = ringtone
		alarm.mediaArtist = uri.getMediaArtist(context)
		alarm.mediaTitle = uri.getMediaTitle(context)
		alarm.mediaType = uri.getMediaType(context)
		alarm.localMediaPath = buildLocalMediaPath(context,
			alarm.mediaArtist, alarm.mediaTitle, alarm.mediaType)
	}

	// Check if the VIBRATE is in the intent
	if (this.hasExtra(AlarmClock.EXTRA_VIBRATE))
	{
		val defaultVibrate = true
		val vibrate = this.getBooleanExtra(AlarmClock.EXTRA_VIBRATE,
			defaultVibrate)
		isSet = true

		// Add to the alarm
		alarm.shouldVibrate = vibrate
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

/**
 * Get the timer associated with the given Intent.
 *
 * @return The timer associated with the given Intent.
 */
fun Intent.getTimer(): NacTimer?
{
	// Get the bundle from the intent
	val bundle = this.getBundleExtra(TIMER_BUNDLE_NAME)

	// Get the alarm from the bundle
	return bundle?.getTimer()
}

/**
 * Store any NFC intent in a LiveData singleton object so it can be saved in an activity
 * and observed in whichever fragment is visible.
 */
object NacNfcIntent
{

	/**
	 * The NFC intent as mutable LiveData. Keep private so that only this object is able
	 * to modify it.
	 */
	private val mutableLiveData: MutableLiveData<Intent> = MutableLiveData<Intent>()

	/**
	 * The NFC intent LiveData that is publically available.
	 */
	val liveData: LiveData<Intent> = mutableLiveData

	/**
	 * The NFC intent LiveData that is publically available.
	 */
	fun update(newIntent: Intent)
	{
		mutableLiveData.value = newIntent
	}

}

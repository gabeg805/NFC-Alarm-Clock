package com.nfcalarmclock.util

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.AlarmClock
import com.nfcalarmclock.R
import com.nfcalarmclock.activealarm.NacActiveAlarmActivity
import com.nfcalarmclock.activealarm.NacActiveAlarmService
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.main.NacMainActivity
import com.nfcalarmclock.nfc.NacNfc
import com.nfcalarmclock.nfc.NacNfcTag
import com.nfcalarmclock.shared.NacSharedPreferences
import com.nfcalarmclock.util.NacCalendar.Day
import java.util.Calendar

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
	private const val MEDIA_BUNDLE_NAME = "NacMediaBundle"

	/**
	 * Add an alarm to an intent.
	 */
	private fun addAlarm(intent: Intent, bundle: Bundle?): Intent
	{
		// Make sure intent and bundle are not null
		if (bundle != null)
		{
			intent.putExtra(ALARM_BUNDLE_NAME, bundle)
		}

		return intent
	}

	/**
	 * Add an alarm to an intent.
	 *
	 * @param  intent  An intent.
	 * @param  alarm  An alarm.
	 *
	 * @return The passed in intent with the alarm.
	 */
	private fun addAlarm(intent: Intent, alarm: NacAlarm?): Intent
	{
		return addAlarm(intent, NacBundle.toBundle(alarm))
	}

	/**
	 * Get an intent that will be used to dismiss the alarm activity.
	 *
	 * @return An intent that will be used to dismiss the alarm activity.
	 */
	fun autoDismissAlarmActivity(context: Context, alarm: NacAlarm?): Intent
	{
		// Create an intent with the alarm activity
		val intent = createAlarmActivity(context, alarm)

		// Set the intent's action
		intent.action = NacActiveAlarmActivity.ACTION_AUTO_DISMISS_ACTIVITY

		return intent
	}

	/**
	 * Create an intent that will be used to start the Alarm activity.
	 *
	 * @param  context  A context.
	 * @param  bundle  A bundle.
	 *
	 * @return The Alarm activity intent.
	 */
	fun createAlarmActivity(context: Context, bundle: Bundle?): Intent
	{
		// Create the intent and its flags
		val intent = Intent(context, NacActiveAlarmActivity::class.java)
		val flags = (Intent.FLAG_ACTIVITY_NEW_TASK
			or Intent.FLAG_ACTIVITY_CLEAR_TASK)

		// Add the flags to the intent
		intent.addFlags(flags)

		return addAlarm(intent, bundle)
	}

	/**
	 * @see NacIntent.createAlarmActivity
	 */
	fun createAlarmActivity(context: Context, alarm: NacAlarm?): Intent
	{
		// Create a bundle with an alarm
		val bundle = NacBundle.toBundle(alarm)

		// Create an alarm activity intent
		return createAlarmActivity(context, bundle)
	}

	/**
	 * Create an intent that will be used to start the foreground alarm service.
	 *
	 *
	 *
	 * @param  context  A context.
	 * @param  bundle  A bundle.
	 *
	 * @return The Foreground service intent.
	 */
	fun createForegroundService(context: Context, bundle: Bundle?): Intent
	{
		// Create an intent with the alarm service
		val intent = Intent(NacActiveAlarmService.ACTION_START_SERVICE, null,
			context, NacActiveAlarmService::class.java)

		// Add the alarm to the intent
		return addAlarm(intent, bundle)
	}

	/**
	 * @see NacIntent.createForegroundService
	 */
	fun createForegroundService(context: Context, alarm: NacAlarm?): Intent
	{
		// Create a bundle with an alarm
		val bundle = NacBundle.toBundle(alarm)

		// Create an intent with the alarm service and add the bundle
		return createForegroundService(context, bundle)
	}

	/**
	 * Create an intent that will be used to start the Main activity.
	 *
	 * @param  context  A context.
	 * @param  bundle  A bundle.
	 *
	 * @return The Main activity intent.
	 */
	fun createMainActivity(context: Context, bundle: Bundle?): Intent
	{
		// Create an intent with the main activity
		val intent = Intent(context, NacMainActivity::class.java)
		val flags = (Intent.FLAG_ACTIVITY_NEW_TASK
			or Intent.FLAG_ACTIVITY_CLEAR_TASK)

		// Add the flags to the intent
		intent.addFlags(flags)

		// Add the alarm to the intent
		return addAlarm(intent, bundle)
	}

	/**
	 * @see NacIntent.createMainActivity
	 */
	fun createMainActivity(context: Context): Intent
	{
		return createMainActivity(context, null as Bundle?)
	}

	/**
	 * @see NacIntent.createMainActivity
	 */
	fun createMainActivity(context: Context, alarm: NacAlarm?): Intent
	{
		// Create a bundle with the alarm
		val bundle = NacBundle.toBundle(alarm)

		// Create an intent with the main activity and add the bundle
		return createMainActivity(context, bundle)
	}

	/**
	 * @return An intent that will be used to dismiss the alarm activity.
	 */
	fun dismissAlarmActivity(context: Context, alarm: NacAlarm?): Intent
	{
		// Create an intent with the alarm activity
		val intent = createAlarmActivity(context, alarm)

		// Set the intent's action
		intent.action = NacActiveAlarmActivity.ACTION_DISMISS_ACTIVITY

		return intent
	}

	/**
	 * @return An intent that will be used to dismiss the alarm activity with NFC.
	 */
	fun dismissAlarmActivityWithNfc(context: Context, tag: NacNfcTag): Intent
	{
		// Create the intent with the alarm activity
		val intent = createAlarmActivity(context, tag.activeAlarm)

		// Setup the intent's action and NFC tag
		//intent.setAction(NacActiveAlarmActivity.ACTION_DISMISS_ACTIVITY);
		intent.action = tag.nfcAction
		NacNfc.addTagToIntent(intent, tag.nfcTag)

		return intent
	}

	/**
	 * @return An intent that will be used to dismiss the foreground alarm service.
	 */
	fun dismissForegroundService(context: Context, alarm: NacAlarm?): Intent
	{
		// Create the intent with the alarm service
		val intent = Intent(NacActiveAlarmService.ACTION_DISMISS_ALARM, null,
			context, NacActiveAlarmService::class.java)

		// Add the alarm to the intent
		return addAlarm(intent, alarm)
	}

	/**
	 * @return An intent that will be used to dismiss the foreground alarm service
	 * and indicates that NFC was used.
	 */
	fun dismissForegroundServiceWithNfc(context: Context, alarm: NacAlarm?): Intent
	{
		// Create the intent with the alarm service
		val intent = Intent(NacActiveAlarmService.ACTION_DISMISS_ALARM_WITH_NFC,
			null, context, NacActiveAlarmService::class.java)

		// Add the alarm to the intnet
		return addAlarm(intent, alarm)
	}

	/**
	 * Get the alarm associated with the given Intent.
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
		val bundle = getAlarmBundle(intent)

		// Get the alarm from the bundle
		return NacBundle.getAlarm(bundle)
	}

	/**
	 * @see .getBundle
	 */
	fun getAlarmBundle(intent: Intent?): Bundle?
	{
		return getBundle(intent, ALARM_BUNDLE_NAME)
	}

	/**
	 * Get the extra data bundle that is part of the intent.
	 *
	 * @return The extra data bundle that is part of the intent.
	 */
	private fun getBundle(intent: Intent?, name: String): Bundle?
	{
		return intent?.getBundleExtra(name)
	}

	/**
	 * Get the alarm that was specified using the SET_ALARM action.
	 *
	 * @return The alarm that was specified using the SET_ALARM action.
	 */
	fun getSetAlarm(context: Context, intent: Intent): NacAlarm?
	{
		// Check if this is a SET_ALARM intent
		if (!isSetAlarmAction(intent))
		{
			return null
		}

		val shared = NacSharedPreferences(context)
		val builder = NacAlarm.Builder(shared)
		val calendar = Calendar.getInstance()
		var isSet = false

		// Check if the HOUR is in the intent
		if (intent.hasExtra(AlarmClock.EXTRA_HOUR))
		{
			val hour = intent.getIntExtra(AlarmClock.EXTRA_HOUR,
				calendar[Calendar.HOUR_OF_DAY])
			isSet = true

			// Add to the alarm builder
			builder.setHour(hour)
		}

		// Check if the MINUTES is in the intent
		if (intent.hasExtra(AlarmClock.EXTRA_MINUTES))
		{
			val minute = intent.getIntExtra(AlarmClock.EXTRA_MINUTES,
				calendar[Calendar.MINUTE])
			isSet = true

			// Add to the alarm builder
			builder.setMinute(minute)
		}

		// Check if the MESSAGE (Name) is in the intent
		if (intent.hasExtra(AlarmClock.EXTRA_MESSAGE))
		{
			val name = intent.getStringExtra(AlarmClock.EXTRA_MESSAGE)
			isSet = true

			// Add to the alarm builder
			builder.setName(name ?: "")
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

			// Add to the alarm builder
			builder.setDays(days)
		}

		// Check if the RINGTONE is in the intent
		if (intent.hasExtra(AlarmClock.EXTRA_RINGTONE))
		{
			val ringtone = intent.getStringExtra(AlarmClock.EXTRA_RINGTONE)
			isSet = true

			// Add to the alarm builder
			builder.setMedia(context, ringtone ?: "")
		}

		// Check if the VIBRATE is in the intent
		if (intent.hasExtra(AlarmClock.EXTRA_VIBRATE))
		{
			val defaultVibrate = context.resources.getBoolean(R.bool.default_vibrate)
			val vibrate = intent.getBooleanExtra(AlarmClock.EXTRA_VIBRATE,
				defaultVibrate)
			isSet = true

			// Add to the alarm builder
			builder.setVibrate(vibrate)
		}

		//getBooleanExtra(AlarmClock.EXTRA_SKIP_UI);
		// Check if one or more alarm attributes were set
		return if (isSet)
			{
				builder.build()
			}
			else
			{
				null
			}
	}

	/**
	 * @return The sound associated with the given intent.
	 */
	fun getMedia(intent: Intent?): String?
	{
		// Check if the intent is null
		if (intent == null)
		{
			return null
		}

		// Get the bundle from the intent
		val bundle = getMediaBundle(intent)

		// Get the media from the bundle
		return NacBundle.getMedia(bundle)
	}

	/**
	 * @return The sound bundle.
	 */
	private fun getMediaBundle(intent: Intent?): Bundle?
	{
		return getBundle(intent, MEDIA_BUNDLE_NAME)
	}

	/**
	 * @return True if the intent was called from the SET_ALARM action, and
	 * False otherwise.
	 */
	private fun isSetAlarmAction(intent: Intent?): Boolean
	{
		// Check if the intent is null
		if (intent == null)
		{
			return false
		}

		return intent.action == AlarmClock.ACTION_SET_ALARM
		//return action != null && action == AlarmClock.ACTION_SET_ALARM
	}

	/**
	 * @return An intent that will be used to snooze the foreground alarm service.
	 */
	fun snoozeForegroundService(context: Context?, alarm: NacAlarm?): Intent
	{
		// Create the intent with the alarm service
		val intent = Intent(NacActiveAlarmService.ACTION_SNOOZE_ALARM, null,
			context, NacActiveAlarmService::class.java)

		// Add the alarm to the intent
		return addAlarm(intent, alarm)
	}

	/**
	 * @return An intent that allows you to stop the alarm activity.
	 */
	fun stopAlarmActivity(alarm: NacAlarm?): Intent
	{
		// Create the intent with the alarm activity
		val intent = Intent(NacActiveAlarmActivity.ACTION_STOP_ACTIVITY)

		// Check if the alarm is null
		return if (alarm != null)
			{
				// Add the alarm to the intent
				addAlarm(intent, alarm)
			}
			else
			{
				// Simply return the intent since the alarm is null
				intent
			}
	}

	/**
	 * @return An intent with a sound.
	 */
	fun toIntent(media: String?): Intent
	{
		return toIntent(null, null, media)
	}

	/**
	 * Create an intent with an alarm attached in the Extra part of the intent.
	 *
	 * @return An intent.
	 *
	 * @param  context  The application context.
	 * @param  cls      The name of the class for the intent to run.
	 * @param  alarm    The alarm to attach to the intent.
	 */
	fun toIntent(context: Context?, cls: Class<*>?, alarm: NacAlarm?): Intent
	{
		// Create an intent with the given class
		val intent = cls?.let { Intent(context, it) } ?: Intent()

		// Create a bundle with the alarm
		val bundle = NacBundle.toBundle(alarm)

		// Add the bundle to the intent
		intent.putExtra(ALARM_BUNDLE_NAME, bundle)

		return intent
	}

	/**
	 * Create an intent with a media path attached in the Extra part of the
	 * intent.
	 *
	 * @return An intent.
	 *
	 * @param  context  The application context.
	 * @param  cls      The name of the class for the intent to run.
	 * @param  media    The media path to attach to the intent.
	 */
	fun toIntent(context: Context?, cls: Class<*>?, media: String?): Intent
	{
		// Create an intent with the given class
		val intent = cls?.let { Intent(context, it) } ?: Intent()

		// Create a bundle with the media
		val bundle = NacBundle.toBundle(media)

		// Add the bundle to the intent
		intent.putExtra(MEDIA_BUNDLE_NAME, bundle)

		return intent
	}

}
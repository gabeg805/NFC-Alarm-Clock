package com.nfcalarmclock.alarm.options.upcomingreminder

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.IBinder
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.alarm.options.tts.NacTextToSpeech
import com.nfcalarmclock.alarm.options.tts.NacTranslate
import com.nfcalarmclock.log.NacLog
import com.nfcalarmclock.shared.NacSharedPreferences
import com.nfcalarmclock.system.NacCalendar
import com.nfcalarmclock.system.NacLifecycleService
import com.nfcalarmclock.system.addAlarm
import com.nfcalarmclock.system.getAlarm
import com.nfcalarmclock.system.media.NacAudioAttributes
import com.nfcalarmclock.system.media.saveCurrentVolume
import com.nfcalarmclock.system.media.setStreamVolume
import com.nfcalarmclock.system.media.toStreamVolume
import com.nfcalarmclock.system.scheduler.NacScheduler
import java.util.Calendar

/**
 * Upcoming reminder service.
 */
@OptIn(UnstableApi::class)
class NacUpcomingReminderService
	: NacLifecycleService()
{

	/**
	 * Get the number of minutes until the next alarm will run.
	 *
	 * @return The number of minutes until the next alarm will run.
	 */
	private fun getMinutesUntilNextAlarm(nextAlarmCal: Calendar): Int
	{
		// Get the calendar for right now
		val nowCal = Calendar.getInstance()

		// Clear all fields smaller than MINUTE
		nowCal.set(Calendar.SECOND, 0)
		nowCal.set(Calendar.MILLISECOND, 0)

		return ((nextAlarmCal.timeInMillis - nowCal.timeInMillis) / 1000L / 60L).toInt()
	}

	/**
	 * Called when the service is bound.
	 */
	override fun onBind(intent: Intent): IBinder?
	{
		// Super
		super.onBind(intent)

		return null
	}

	/**
	 * Called when the service is started.
	 */
	@UnstableApi
	override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int
	{
		// Super
		super.onStartCommand(intent, flags, startId)

		NacLog.i("Starting upcoming reminder service. Action=${intent?.action}")

		// Attempt to get the alarm from the intent
		val alarm = intent?.getAlarm()

		// Intent action
		val intentAction = if (alarm == null)
		{
			ACTION_STOP_SERVICE
		}
		else
		{
			intent.action
		}

		// Check the intent action
		when (intentAction)
		{

			// Clear the reminder by stopping the service
			ACTION_STOP_SERVICE ->
			{
				NacLog.i("Stopping upcoming reminder service")

				// Cancel any remaining reminders
				if (alarm != null)
				{
					NacScheduler.cancelUpcomingReminder(this, alarm)
				}

				// Stop the service
				stopThisService()
			}

			// Normal path for the service
			else ->
			{
				// Create the reminder notification
				val notification = NacUpcomingReminderNotification(this, alarm!!)

				// Start the service in the foreground
				showForegroundNotification {
					startForeground(notification.id, notification.build())
				}

				NacLog.i("Showing upcoming reminder notification in service")

				// Start the reminder process
				startReminderProcess(alarm)
			}

		}

		return START_NOT_STICKY
	}

	/**
	 * Setup the text-to-speech.
	 */
	private fun setupTextToSpeech(alarm: NacAlarm, nextAlarmCal: Calendar)
	{
		// Audio manager and attributes
		val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
		val audioAttributes = NacAudioAttributes(this, alarm)
		val sharedPreferences = NacSharedPreferences(this)
		val stream = audioAttributes.stream

		// Start the wakeup process
		val textToSpeech = NacTextToSpeech(this, object: NacTextToSpeech.OnSpeakingListener {

			/**
			 * Called when done speaking.
			 */
			override fun onDoneSpeaking()
			{
				// Revert the volume back to what it was
				audioManager.setStreamVolume(stream, sharedPreferences.previousVolume)
			}

			/**
			 * Called when the text-to-speech engine has started.
			 */
			override fun onStartSpeaking()
			{
			}

		})

		// Save the current volume level so it can be reverted later
		audioManager.saveCurrentVolume(sharedPreferences, stream)

		// Set the volume to the alarm volume and save the volume level so
		// that it can be correctly reverted back once the wakeup process
		// is complete
		val alarmVolumeIndex = alarm.toStreamVolume(audioManager, stream)
		audioManager.setStreamVolume(stream, alarmVolumeIndex)

		// Get the phrase that should be said for the reminder
		val timeUntilNextAlarm = getMinutesUntilNextAlarm(nextAlarmCal)
		val phrase = NacTranslate.getSayReminder(this, alarm.name, timeUntilNextAlarm)

		// Speak via TTS
		textToSpeech.speak(phrase, audioAttributes)
	}

	/**
	 * Check if the next upcoming reminder should be shown or not.
	 *
	 * @return True if the next upcoming reminder should be shown, and False
	 *         otherwise.
	 */
	private fun shouldShowNextUpcomingReminder(
		alarm: NacAlarm,
		nextAlarmCal: Calendar,
		nextReminderCal: Calendar
	): Boolean
	{
		// Check if reminder frequency is set
		if (alarm.reminderFrequency <= 0)
		{
			// Reminder frequency is not set so do not show the next reminder
			return false
		}

		// Compute the time difference between the next alarm and reminder
		val nextReminderTime = (nextAlarmCal.timeInMillis - nextReminderCal.timeInMillis) / 1000L

		// Compute the accetable tolerance within which another reminder can
		// run
		val tolerance = alarm.reminderFrequency*60 - 15

		// Compare the two calendars. The reminder should run no closer than 45
		// sec to the alarm. Realistically, it should never be close enough
		// for 45 seconds, but 1 min before should still be possible
		return (nextReminderTime >= tolerance)
	}

	/**
	 * Start the reminder process.
	 */
	private fun startReminderProcess(alarm: NacAlarm)
	{
		NacLog.i("Starting reminder process")

		// Get the calendar for when the next alarm will run and when the next
		// reminder will run
		val nextAlarmCal = NacCalendar.getNextAlarmDay(alarm)!!
		val nextReminderCal = NacCalendar.getNextAlarmUpcomingReminder(alarm)

		// Check if text-to-speech should be used
		if (alarm.shouldUseTts && alarm.shouldUseTtsForReminder)
		{
			// Setup text-to-speech
			setupTextToSpeech(alarm, nextAlarmCal)
		}

		// Check if reminder frequency is set and should show the next
		// upcoming reminder
		if (shouldShowNextUpcomingReminder(alarm, nextAlarmCal, nextReminderCal))
		{
			// Schedule the next reminder
			NacScheduler.addUpcomingReminder(this, alarm, nextReminderCal)
		}
	}

	companion object
	{

		/**
		 * Action to clear the notification and stop the service.
		 */
		private const val ACTION_STOP_SERVICE = "com.nfcalarmclock.alarm.options.upcomingreminder.ACTION_STOP_SERVICE"

		/**
		 * Create an intent that will be used to start the foreground upcoming
		 * reminder service.
		 *
		 * @param context A context.
		 * @param alarm   An alarm.
		 *
		 * @return The Foreground service intent.
		 */
		fun getStartIntent(context: Context, alarm: NacAlarm?): Intent
		{
			// Create an intent with the alarm service
			return Intent(context, NacUpcomingReminderService::class.java)
				.addAlarm(alarm)
		}

		/**
		 * Get an intent that will be used to clear the reminder and stop the service.
		 *
		 * @return An intent that will be used to clear the reminder and stop the service.
		 */
		fun getStopIntent(context: Context, alarm: NacAlarm?): Intent
		{
			// Create the intent with the alarm service
			return Intent(ACTION_STOP_SERVICE, null, context, NacUpcomingReminderService::class.java)
				.addAlarm(alarm)
		}

		/**
		 * Stop the service.
		 */
		fun stopService(context: Context, alarm: NacAlarm?)
		{
			// Create the stop intent
			val intent = getStopIntent(context, alarm)

			// Stop the service
			context.stopService(intent)
		}

	}

}

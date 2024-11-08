package com.nfcalarmclock.alarm.options.upcomingreminder

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.media3.common.util.UnstableApi
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.util.media.NacAudioAttributes
import com.nfcalarmclock.system.scheduler.NacScheduler
import com.nfcalarmclock.alarm.options.tts.NacTextToSpeech
import com.nfcalarmclock.alarm.options.tts.NacTranslate
import com.nfcalarmclock.util.NacCalendar
import com.nfcalarmclock.util.NacIntent
import java.util.Calendar

class NacUpcomingReminderService
	: Service()
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
		return null
	}

	/**
	 * Called when the service is started.
	 */
	@UnstableApi
	override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int
	{
		// Attempt to get the alarm from the intent
		val alarm = NacIntent.getAlarm(intent)

		when (intent?.action)
		{

			// Clear the reminder by stopping the service
			ACTION_CLEAR_REMINDER ->
			{
				// Cancel any remaining reminders
				NacScheduler.cancelUpcomingReminder(this, alarm!!)

				// Stop the service
				stopReminderService()
			}

			// Normal path for the service
			else ->
			{
				// Show the notification
				showReminderNotification(alarm)

				// Check if alarm is not null
				if (alarm != null)
				{
					// Start the reminder process
					startReminderProcess(alarm)
				}
			}

		}

		return START_NOT_STICKY
	}

	/**
	 * Setup the text-to-speech.
	 */
	private fun setupTextToSpeech(alarm: NacAlarm, nextAlarmCal: Calendar)
	{
		// Audio attributes
		val audioAttributes = NacAudioAttributes(this, alarm)

		// Start the wakeup process
		val textToSpeech = NacTextToSpeech(this, object: NacTextToSpeech.OnSpeakingListener {

			/**
			 * Called when done speaking.
			 */

			/**
			 * Called when done speaking.
			 */
			/**
			 * Called when done speaking.
			 */
			/**
			 * Called when done speaking.
			 */
			override fun onDoneSpeaking(tts: NacTextToSpeech)
			{
				// Revert the volume
				audioAttributes.revertVolume()
			}

			/**
			 * Called when the text-to-speech engine has started.
			 */

			/**
			 * Called when the text-to-speech engine has started.
			 */
			/**
			 * Called when the text-to-speech engine has started.
			 */
			/**
			 * Called when the text-to-speech engine has started.
			 */
			override fun onStartSpeaking(tts: NacTextToSpeech)
			{
			}

		})

		// Save the current volume level so it can be reverted later
		audioAttributes.saveCurrentVolume()

		// Set the volume to the alarm volume and save the volume level so
		// that it can be correctly reverted back once the wakeup process
		// is complete
		audioAttributes.setStreamVolume()

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
	 * Show the reminder notification.
	 */
	private fun showReminderNotification(alarm: NacAlarm?)
	{
		// Create the reminder notification
		val notification = NacUpcomingReminderNotification(this, alarm)

		// Start the service in the foreground
		startForeground(notification.id,
			notification.builder().build())
	}

	/**
	 * Start the reminder process.
	 */
	private fun startReminderProcess(alarm: NacAlarm)
	{
		// Get the calendar for when the next alarm will run and when the next
		// reminder will run
		val nextAlarmCal = NacCalendar.getNextAlarmDay(alarm)
		val nextReminderCal = NacCalendar.getNextAlarmUpcomingReminder(alarm)

		// Check if text-to-speech should be used
		if (alarm.shouldUseTtsForReminder)
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

	/**
	 * Stop the service.
	 */
	@Suppress("deprecation")
	private fun stopReminderService()
	{
		// Stop the foreground service using the updated form of
		// stopForeground() for API >= 33
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
		{
			super.stopForeground(STOP_FOREGROUND_REMOVE)
		}
		else
		{
			super.stopForeground(true)
		}

		// Stop the service
		super.stopSelf()
	}

	companion object
	{

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
			val intent = Intent(context, NacUpcomingReminderService::class.java)

			// Add the alarm to the intent
			return NacIntent.addAlarm(intent, alarm)
		}

		/**
		 * Action to start the service.
		 */
		private const val ACTION_CLEAR_REMINDER = "com.nfcalarmclock.ACTION_CLEAR_REMINDER"

		/**
		 * Get an intent that will be used to clear the reminder.
		 *
		 * @return An intent that will be used to clear the reminder.
		 */
		fun getClearReminderIntent(context: Context, alarm: NacAlarm?): Intent
		{
			// Create the intent with the alarm service
			val intent = Intent(ACTION_CLEAR_REMINDER, null, context,
				NacUpcomingReminderService::class.java)

			// Add the alarm to the intent
			return NacIntent.addAlarm(intent, alarm)
		}

	}

}

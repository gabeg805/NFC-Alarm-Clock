package com.nfcalarmclock.upcomingreminder

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.media3.common.util.UnstableApi
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.media.NacAudioAttributes
import com.nfcalarmclock.tts.NacTextToSpeech
import com.nfcalarmclock.tts.NacTranslate
import com.nfcalarmclock.util.NacIntent

class NacUpcomingReminderService
	: Service()
{

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

		// Create the reminder notification
		val notification = NacUpcomingReminderNotification(this, alarm)

		// Start the service in the foreground
		startForeground(notification.id,
			notification.builder().build())

		// Check if alarm is not null and text-to-speech should be used
		if (alarm?.shouldUseTtsForReminder == true)
		{
			// Setup text-to-speech
			setupTextToSpeech(alarm)
		}

		return START_NOT_STICKY
	}

	/**
	 * Setup the text-to-speech.
	 */
	private fun setupTextToSpeech(alarm: NacAlarm)
	{
		// Audio attributes
		val audioAttributes = NacAudioAttributes(this, alarm)

		// Start the wakeup process
		val textToSpeech = NacTextToSpeech(this, object: NacTextToSpeech.OnSpeakingListener {

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
		val phrase = NacTranslate.getSayReminder(this, alarm.name, alarm.timeToShowReminder)

		// Speak via TTS
		textToSpeech.speak(phrase, audioAttributes)
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

	}

}
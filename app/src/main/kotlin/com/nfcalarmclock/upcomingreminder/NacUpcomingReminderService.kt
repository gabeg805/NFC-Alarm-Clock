package com.nfcalarmclock.upcomingreminder

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.media3.common.util.UnstableApi
import com.nfcalarmclock.activealarm.NacActiveAlarmNotification
import com.nfcalarmclock.activealarm.NacActiveAlarmService
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.media.NacAudioAttributes
import com.nfcalarmclock.tts.NacTextToSpeech
import com.nfcalarmclock.util.NacIntent

class NacUpcomingReminderService
	: Service()
{

	/**
	 * The text-to-speech phrase to say.
	 */
	private fun getTtsPhrase(alarm: NacAlarm): String
	{
		// Initialize the phrase
		var phrase = ""

		// Check if should say the current time
		if (alarm.shouldSayCurrentTime)
		{
			//phrase += sayCurrentTime
		}

		// Check if should say the alarm name
		if (alarm.shouldSayAlarmName)
		{
			//phrase += sayAlarmName
		}

		return phrase
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

		// Create the reminder notification
		val notification = NacUpcomingReminderNotification(this, alarm)

		// Start the service in the foreground
		startForeground(notification.id,
			notification.builder().build())

		// Start the wakeup process
		val textToSpeech = NacTextToSpeech(this, null)

		// Audio attributes
		val audioAttributes = NacAudioAttributes(this, alarm!!)

		// Save the current volume level so it can be reverted later
		audioAttributes.saveCurrentVolume()

		// Set the volume to the alarm volume and save the volume level so
		// that it can be correctly reverted back once the wakeup process
		// is complete
		audioAttributes.setStreamVolume()

		// Speak via TTS
		// TODO: Revert volume after done speaking? Maybe do need a listener
		textToSpeech.speak(getTtsPhrase(alarm), audioAttributes)

		return START_NOT_STICKY
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
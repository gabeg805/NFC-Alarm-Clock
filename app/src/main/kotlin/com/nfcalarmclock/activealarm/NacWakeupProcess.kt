package com.nfcalarmclock.activealarm

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.media.NacAudioAttributes
import com.nfcalarmclock.media.NacMedia
import com.nfcalarmclock.mediaplayer.NacMediaPlayer
import com.nfcalarmclock.shared.NacSharedPreferences
import com.nfcalarmclock.tts.NacTextToSpeech
import com.nfcalarmclock.tts.NacTextToSpeech.OnSpeakingListener
import com.nfcalarmclock.util.NacCalendar
import java.util.Calendar
import java.util.Locale

/**
 * Actions to take upon waking up, such as enabling NFC, playing music, etc.
 */
@UnstableApi
class NacWakeupProcess(

	/**
	 * The application context.
	 */
	private val context: Context,

	/**
	 * Alarm.
	 */
	private val alarm: NacAlarm

	// Interfaces
) : Player.Listener
{

	companion object
	{

		/**
		 * Period at which to gradually increase volume.
		 */
		private const val PERIOD_GRADUALLY_INCREASE_VOLUME = 5000L

		/**
		 * Period at which to ensure the volume is restricted.
		 */
		private const val PERIOD_RESTRICT_VOLUME = 1000L

	}

	/**
	 * Shared preferences.
	 */
	private val sharedPreferences: NacSharedPreferences = NacSharedPreferences(context)

	/**
	 * Audio attributes.
	 */
	private var audioAttributes: NacAudioAttributes = NacAudioAttributes(context, alarm)

	/**
	 * Vibrate handler, to vibrate the phone at periodic intervals.
	 */
	private val vibrateHandler: Handler = Handler(context.mainLooper)

	/**
	 * Say the current time at user specified intervals.
	 */
	private val speakHandler: Handler = Handler(context.mainLooper)

	/**
	 * Continue the wakeup process after being done with speaking with text-to-speech.
	 */
	private val continueWakeupHandler: Handler = Handler(context.mainLooper)

	/**
	 * Gradually increase the volume.
	 */
	private val graduallyIncreaseVolumeHandler: Handler = Handler(context.mainLooper)

	/**
	 * Restrict the volume.
	 */
	private val restrictVolumeHandler: Handler = Handler(context.mainLooper)

	/**
	 * Volume level to restrict any volume changes to.
	 *
	 * This is not just the alarm volume, since if the user wants to gradually
	 * increase the volume, the restricted volume in that case should be lower
	 * than the alarm volume.
	 */
	private var volumeToRestrictChangeTo: Int = -1

	/**
	 * Flag indicating whether to skip the restrict volume check until the
	 * gradually increase volume process has started.
	 */
	private var hasGraduallyIncreaseVolumeStarted: Boolean = false

	/**
	 * Media player.
	 */
	private val mediaPlayer: NacMediaPlayer? = if (alarm.hasMedia)
	{
		NacMediaPlayer(context, this)
	}
	else
	{
		null
	}

	/**
	 * Vibrator object to vibratee the phone.
	 */
	@Suppress("deprecation")
	private val vibrator: Vibrator? = if (alarm.shouldVibrate)
	{
		// Check the Android API
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
		{
			// Get the manager
			val manager = context.getSystemService(
				Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager

			// Return the vibrator
			manager.defaultVibrator
		}
		// Use the old API
		else
		{
			// Return the vibrator
			context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
		}
	}
	else
	{
		null
	}

	/**
	 * Text-to-speech engine.
	 */
	private val textToSpeech: NacTextToSpeech? = if (alarm.shouldUseTts)
	{
		NacTextToSpeech(context, object: OnSpeakingListener {

			/**
			 * Called when done speaking.
			 */
			override fun onDoneSpeaking(tts: NacTextToSpeech)
			{
				continueWakeupHandler.post { simpleStart() }
			}

			/**
			 * Called when the text-to-speech engine has started.
			 */
			override fun onStartSpeaking(tts: NacTextToSpeech)
			{
				// Stop any vibration when TTS is playing
				cleanupVibrate()
			}

		})
	}
	else
	{
		null
	}

	/**
	 * The text-to-speech phrase to say.
	 */
	private val ttsPhrase: String
		get()
		{
			// Initialize the phrase
			var phrase = ""

			// Check if should say the current time
			if (alarm.shouldSayCurrentTime)
			{
				phrase += sayCurrentTime
			}

			// Check if should say the alarm name
			if (alarm.shouldSayAlarmName)
			{
				phrase += sayAlarmName
			}

			return phrase
		}

	/**
	 * The words that should be said when stating the alarm name, in the
	 * designated language.
	 */
	private val sayAlarmName: String
		get()
		{
			return alarm.name
		}

	/**
	 * The words that should be said when stating the time, in the
	 * designated language.
	 */
	private val sayCurrentTime: String
		get()
		{
			// Get the default locale
			val locale = Locale.getDefault()

			// Get the current hour and minute
			val calendar = Calendar.getInstance()
			val hour = calendar[Calendar.HOUR_OF_DAY]
			val minute = calendar[Calendar.MINUTE]

			// Get the meridian (if it should be used based on the user's preferences
			val meridian = NacCalendar.getMeridian(context, hour)

			// Check if the language is Spanish
			return if (locale.language == "es")
			{
				getSayCurrentTimeEs(hour, minute, meridian)
			}
			else
			{
				getSayCurrentTimeEn(hour, minute, meridian)
			}
		}

	/**
	 * Cleanup various alarm objects.
	 */
	fun cleanup()
	{
		// Cleanup vibrate
		cleanupVibrate()

		// Cleanup the media player
		mediaPlayer?.release()

		// Revert the volume
		audioAttributes.revertVolume()

		// Cleanup the text-to-speech engine
		textToSpeech?.shutdown()
		speakHandler.removeCallbacksAndMessages(null)

		// Cleanup the continue wakeup handler
		continueWakeupHandler.removeCallbacksAndMessages(null)

		// Cleanup the gradually increasing volume handler
		graduallyIncreaseVolumeHandler.removeCallbacksAndMessages(null)

		// Cleanup the restrict volume handler
		restrictVolumeHandler.removeCallbacksAndMessages(null)
	}

	/**
	 * Cleanup vibrating the phone.
	 */
	private fun cleanupVibrate()
	{
		// Stop any current vibrations
		vibrator?.cancel()

		// Stop any future vibrations from occuring
		vibrateHandler.removeCallbacksAndMessages(null)
	}

	/**
	 * Get how to say the current time in English.
	 */
	private fun getSayCurrentTimeEn(hour: Int, minute: Int, meridian: String?): String
	{
		val locale = Locale.getDefault()

		// Check if the minute should be said as "oh" e.g. 8:05 would be eight oh five
		val oh = if (minute in 1..9) "O" else ""
		var showHour = hour
		val showMinute = if (minute == 0) "" else minute.toString()

		// Check if the meridian is set. This means the time is in 12 hour format
		if (!meridian.isNullOrEmpty())
		{
			// Convert the hour to 12 hour format
			showHour = NacCalendar.to12HourFormat(showHour)
		}

		// Return the statement that should be said
		return String.format(locale,
			"The time, is, $showHour, $oh, $showMinute, $meridian. ")
	}

	/**
	 * Get how to say the current time in Spanish.
	 */
	private fun getSayCurrentTimeEs(
		hour: Int,
		minute: Int,
		meridian: String?
	): String
	{
		val locale = Locale.getDefault()
		var showHour = hour

		// Check if the meridian is null or empty. This means the time is in
		// 24 hour format
		return if (meridian.isNullOrEmpty())
		{
			// Pluralize the minutes if not equal to 1
			val plural = if (minute != 1) "s" else ""

			// Return the statement that should be said
			String.format(locale,
				"Es, la hora, $showHour, con, $minute, minuto$plural. ")
		}
		else
		{
			// Convert the hour to 12 hour format
			showHour = NacCalendar.to12HourFormat(showHour)

			// Get how the time should be said based on the hour and minute
			val theTimeIs = if (showHour == 1) "Es, la," else "Son, las,"
			val showMinute = if (minute == 0) "" else minute.toString()

			// Return the statement that should be said
			String.format(locale,
				"$theTimeIs $showHour, $showMinute, $meridian. ")
		}
	}

	/**
	 * Gradually increase the volume.
	 */
	private fun graduallyIncreaseVolume()
	{
		// Get the alarm volume
		val alarmVolume = audioAttributes.alarmToStreamVolume()

		// Get the current volume
		val currentVolume = if (alarm.shouldRestrictVolume)
		{
			// Previous volume that was restricted to
			volumeToRestrictChangeTo
		}
		else
		{
			// Device volume
			audioAttributes.streamVolume
		}

		// Volume has not reached the alarm level yet
		if (currentVolume < alarmVolume)
		{
			// Gradually increase the volume by one step
			val newVolume = currentVolume + 1

			volumeToRestrictChangeTo = newVolume
			audioAttributes.streamVolume = newVolume
		}

		// Wait for a period of time before increasing the volume again.
		// This will get called even if the volume does not need to change, in
		// case the user tries to lower then volume after the alarm volume
		// level has been reached
		graduallyIncreaseVolumeHandler.postDelayed({ graduallyIncreaseVolume() },
			PERIOD_GRADUALLY_INCREASE_VOLUME)
	}

	/**
	 * Restrict the volume.
	 */
	private fun restrictVolume()
	{
		// Check if the volume is below the restrict volume.
		// If the volume will be gradually increasing, check that the process
		// has already started
		if ((audioAttributes.streamVolume < volumeToRestrictChangeTo)
			&& (!alarm.shouldGraduallyIncreaseVolume || hasGraduallyIncreaseVolumeStarted))
		{
			// Change the volume
			audioAttributes.streamVolume = volumeToRestrictChangeTo
		}

		// Run the handler
		restrictVolumeHandler.postDelayed({ restrictVolume() },
			PERIOD_RESTRICT_VOLUME)
	}

	/**
	 * Play music.
	 */
	private fun playMusic()
	{
		// Unable to play music
		if (mediaPlayer== null)
		{
			return
		}

		// Check if the media player was playing music
		if (mediaPlayer.wasPlaying)
		{
			// Continue playing what was playing before
			mediaPlayer.play()
		}
		else
		{
			// Set shuffle mode (can be set or not set) based on the preference
			if (NacMedia.isDirectory(alarm.mediaType))
			{
				mediaPlayer.exoPlayer.shuffleModeEnabled = sharedPreferences.shuffle
			}

			// Play the alarm
			mediaPlayer.playAlarm(alarm)
		}
	}

	/**
	 * Setup gradually increasing the volume.
	 */
	private fun setupGraduallyIncreaseVolume()
	{
		// Set the volume to 0 to start with
		volumeToRestrictChangeTo = 0
		audioAttributes.streamVolume = 0

		// Run handler at a cadence in order to gradually increase the volume
		graduallyIncreaseVolumeHandler.postDelayed({

			// Gradually increase volume
			graduallyIncreaseVolume()

			// Set flag indicating that the gradual increase process has
			// started
			hasGraduallyIncreaseVolumeStarted = true

		}, PERIOD_GRADUALLY_INCREASE_VOLUME)
	}

	/**
	 * Setup the restrict volume.
	 */
	private fun setupRestrictVolume()
	{
		// Set the volume to restrict to, if any changes occur
		volumeToRestrictChangeTo = audioAttributes.streamVolume

		// Run handler at a cadence in order to restrict the volume. Volume
		// change events cannot be caught, so need to run this every X
		// milliseconds to enforce it
		restrictVolumeHandler.postDelayed({ restrictVolume() }, PERIOD_RESTRICT_VOLUME)
	}

	/**
	 * Setup the volume. This includes features like gradually increasing
	 * volume and restrict volume.
	 */
	private fun setupVolume()
	{
		// Check if using text-to-speech or playing music. The reason being
		// that if these are not being used, then there is no point in changing
		// the volume
		if (alarm.shouldUseTts || alarm.hasMedia)
		{
			// Save the current volume level so it can be reverted later
			audioAttributes.saveCurrentVolume()

			// Set the volume to the alarm volume and save the volume level so
			// that it can be correctly reverted back once the wakeup process
			// is complete
			audioAttributes.setStreamVolume()

			// Check if should gradually increase the volume
			if (alarm.shouldGraduallyIncreaseVolume)
			{
				setupGraduallyIncreaseVolume()
			}

			// Check if should restrict the volume
			if (alarm.shouldRestrictVolume)
			{
				setupRestrictVolume()
			}
		}
	}

	/**
	 * Speak at the desired frequency, specified in the shared preference.
	 */
	private fun speak()
	{
		// Unable to speak via TTS. The engine is not set yet, or is already
		// speaking, or there is something in the buffer, or the alarm is not set
		// yet, or the alarm should not use TTS
		if (textToSpeech == null || textToSpeech.isSpeaking() || textToSpeech.hasBuffer()
			|| !alarm.shouldUseTts)
		{
			return
		}

		// Speak via TTS
		textToSpeech.speak(ttsPhrase, audioAttributes)

		// Check if text to speech should be run at a certain frequency
		if (alarm.ttsFrequencyMillis != 0L)
		{
			// Wait for some period of time before speaking through TTS again
			speakHandler.postDelayed({ speak() }, alarm.ttsFrequencyMillis)
		}
	}

	/**
	 * Start the simple wake up process.
	 */
	fun simpleStart()
	{
		// Play music
		if (alarm.hasMedia)
		{
			playMusic()
		}

		// Vibrate the phone
		if (alarm.shouldVibrate)
		{
			vibrate()
		}
	}

	/**
	 * Start the wake up process.
	 */
	fun start()
	{
		// Setup the volume
		setupVolume()

		// Start text-to-speech
		if (alarm.shouldUseTts)
		{
			speak()
		}
		// Start the simple wakeup process
		else
		{
			simpleStart()
		}
	}

	/**
	 * Vibrate the phone repeatedly until the alarm is dismissed.
	 */
	@Suppress("deprecation")
	@TargetApi(Build.VERSION_CODES.O)
	fun vibrate()
	{
		// Unable to vibrate. Vibrator is not set yet, or alarm is not set yet, or
		// alarm should not vibrate
		if (vibrator == null)
		{
			return
		}

		// Cancel the previous vibration, if any
		cleanupVibrate()

		// Set the amount of time to vibrate, and the amount of time to wait in
		// between vibrations
		val duration = 500L
		val waitTime = 1000 + duration

		// Check if the new API needs to be used
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
		{
			// Create the vibration effect
			val effect = VibrationEffect.createOneShot(duration,
				VibrationEffect.DEFAULT_AMPLITUDE)

			// Vibrate
			vibrator.vibrate(effect)
		}
		// The old API can be used
		else
		{
			// Vibrate
			vibrator.vibrate(duration)
		}

		// Wait for a period of time before vibrating the phone again
		vibrateHandler.postDelayed({ vibrate() }, waitTime)
	}

}
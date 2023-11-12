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
import com.nfcalarmclock.R
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.media.NacAudioAttributes
import com.nfcalarmclock.media.NacMedia.isDirectory
import com.nfcalarmclock.mediaplayer.NacMediaPlayer
import com.nfcalarmclock.shared.NacSharedPreferences
import com.nfcalarmclock.tts.NacTextToSpeech
import com.nfcalarmclock.tts.NacTextToSpeech.OnSpeakingListener
import com.nfcalarmclock.util.NacCalendar
import com.nfcalarmclock.util.NacUtility.quickToast
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
) : OnSpeakingListener,
	Player.Listener
{

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
	 * Volume level to restrict any volume changes to.
	 *
	 * This is not just the alarm volume, since if the user wants to gradually
	 * increase the volume, the restricted volume in that case should be lower
	 * than the alarm volume.
	 */
	private var volumeToRestrictChangeTo: Int = -1

	/**
	 * Flag indicating whether to ignore the next volume change or not.
	 */
	private var ignoreNextVolumeChange: Boolean = false

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
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
		{
			// Get the manager
			val manager = context.getSystemService(
				Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager

			// Return the vibrator
			manager.defaultVibrator
		}
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
		NacTextToSpeech(context, this)
	}
	else
	{
		null
	}

	/**
	 * Constructor.
	 */
	init
	{
		// TODO: Can the handler for the media player in onDoneSpeaking() be created
		// here? This way it is only done once?
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

		// Cleanup the text-to-speech engine.
		textToSpeech?.shutdown()
		speakHandler.removeCallbacksAndMessages(null)

		// Cleanup the continue wakeup handler
		continueWakeupHandler.removeCallbacksAndMessages(null)

		// Cleanup gradually increasing the volume.
		graduallyIncreaseVolumeHandler.removeCallbacksAndMessages(null)
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
	 * The words that should be said when stating the time, in the designated language.
	 */
	private val timeToSay: String
		get()
		{
			//val name = alarm!!.name
			// TODO: Say the name of the alarm after the time.

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
				getTimeToSayEs(hour, minute, meridian)
			}
			else
			{
				getTimeToSayEn(hour, minute, meridian)
			}
		}

	/**
	 * @see .getTimeToSay
	 */
	private fun getTimeToSayEn(hour: Int, minute: Int, meridian: String?): String
	{
		// Get the locale
		var hour = hour
		val locale = Locale.getDefault()

		// Check if the minute should be said as "oh" e.g. 8:05 would be eight oh five
		val oh = if (minute in 1..9) "O" else ""
		val showMinute = if (minute == 0) "" else minute.toString()

		// Check if the meridian is set. This means the time is in 12 hour format
		if (!meridian.isNullOrEmpty())
		{
			// Convert the hour to 12 hour format
			hour = NacCalendar.to12HourFormat(hour)
		}

		// Return the statement that should be said
		return String.format(locale,
			", , The time, is, %1\$d, %2\$s, %3\$s, %4\$s",
			hour, oh, showMinute, meridian)
	}

	/**
	 * @see .getTimeToSay
	 */
	private fun getTimeToSayEs(hour: Int, minute: Int, meridian: String?): String
	{
		// Get the locale
		var hour = hour
		val locale = Locale.getDefault()

		// Check if the meridian is null or empty. This means the time is in
		// 24 hour format
		return if (meridian.isNullOrEmpty())
		{
			// Pluralize the minutes if not equal to 1
			val plural = if (minute != 1) "s" else ""

			// Return the statement that should be said
			String.format(locale,
				", , Es, la hora, %1\$d, con, %2\$d, minuto%3\$s",
				hour, minute, plural)
		}
		else
		{
			// Convert the hour to 12 hour format
			hour = NacCalendar.to12HourFormat(hour)

			// Get how the time should be said based on the hour and minute
			val theTimeIs = if (hour == 1) "Es, la," else "Son, las,"
			val showMinute = if (minute == 0) "" else minute.toString()

			// Return the statement that should be said
			String.format(locale,
				", , %1\$s %2\$d, %3\$s, %4\$s",
				theTimeIs, hour, showMinute, meridian)
		}
	}

	/**
	 * Gradually increase the volume.
	 */
	private fun graduallyIncreaseVolume()
	{
		val currentVolume = audioAttributes.streamVolume
		val alarmVolume = audioAttributes.toStreamVolume()
		val newVolume = currentVolume + 1

		// Do not change the volume. It is already at the alarm volume or greater
		if (currentVolume >= alarmVolume)
		{
			return
		}

		// Gradually increase the volume by one step
		ignoreNextVolumeChange = true
		volumeToRestrictChangeTo = newVolume
		audioAttributes.streamVolume = newVolume

		// Wait for a period of time before increasing the volume again
		graduallyIncreaseVolumeHandler.postDelayed({ graduallyIncreaseVolume() }, 5000)
	}

	/**
	 * Called when the device volume is changed.
	 */
	override fun onDeviceVolumeChanged(volume: Int, muted: Boolean)
	{
		println("DEVICE VOLUME CHANGED : $ignoreNextVolumeChange")
		// Do not handle the volume change. Alarm is not set yet, or volume does not
		// need to be restricted, or this volume change can be ignored.
		if (!alarm.shouldRestrictVolume || ignoreNextVolumeChange)
		{
			// Toggle the flag so that the next volume change is NOT ignored
			ignoreNextVolumeChange = false
			return
		}

		try
		{
			// Restrict changing the volume
			// TODO: setDeviceMuted() in case muted=True?
			println("TRY TO RESTRICT VOLUME")
			mediaPlayer!!.exoPlayer.deviceVolume = volumeToRestrictChangeTo
		}
		catch (e: SecurityException)
		{
			// Show a toast indicating that the volume could not be restricted
			quickToast(context, R.string.error_message_restrict_volume_change)
		}
	}

	/**
	 * Called when done speaking.
	 */
	override fun onDoneSpeaking(tts: NacTextToSpeech)
	{
		continueWakeupHandler.post { this.start() }
	}

	/**
	 * Called when the text-to-speech engine has started.
	 */
	override fun onStartSpeaking(tts: NacTextToSpeech)
	{
		// Stop any vibration when TTS is playing
		cleanupVibrate()
	}

	/**
	 * Play music.
	 */
	private fun playMusic()
	{
		// Unable to play music
		if (mediaPlayer== null || !alarm.hasMedia)
		{
			return
		}

		// Continue playing what was being played before
		if (mediaPlayer.wasPlaying)
		{
			mediaPlayer.play()
		}
		else
		{
			val shared = sharedPreferences

			// Set shuffle mode (can be set or not set) based on the preference
			if (isDirectory(alarm.mediaType))
			{
				mediaPlayer.exoPlayer.shuffleModeEnabled = shared.shuffle
			}

			// TODO: Maybe call reset by NOT default
			//player.getMediaPlayer().stop();
			mediaPlayer.playAlarm(alarm)
		}
	}

	/**
	 * Set the volume for the music player and text-to-speech engine.
	 */
	private fun setVolume()
	{
		// Unable to set volume. Alarm is not set yet or player is not setup yet.
		if (mediaPlayer == null)
		{
			return
		}

		// TODO: Update the player and its audio attributes

		// Set the volume to the alarm volume
		ignoreNextVolumeChange = true
		audioAttributes.setVolume()
		//attrs.merge(alarm).setVolume();

		// Set the volume to restrict to, if any changes occur
		if (alarm.shouldRestrictVolume)
		{
			volumeToRestrictChangeTo = audioAttributes.streamVolume
		}
	}

	/**
	 * Setup gradually increasing the volume.
	 */
	private fun setupGraduallyIncreaseVolume()
	{
		// Set the frequency to gradually increase volume
		val freq = 5000L

		// Set the volume to 0 to start with
		ignoreNextVolumeChange = true
		volumeToRestrictChangeTo = 0
		audioAttributes.streamVolume = 0

		// Periodically increase the volume
		graduallyIncreaseVolumeHandler.postDelayed({ graduallyIncreaseVolume() }, freq)
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
		textToSpeech.speak(timeToSay, audioAttributes)

		// Check if text to speech should be run at a certain frequency
		val freq = alarm.ttsFrequencyMillis

		if (freq != 0L)
		{
			// Wait for some period of time before speaking through TTS again
			speakHandler.postDelayed({ speak() }, freq)
		}
	}

	/**
	 * Start the wake up process.
	 *
	 * @param  alarm  The alarm to use during wake up.
	 */
	fun start(alarm: NacAlarm?)
	{
		// Unable to start the wakeup process. Alarm is not set
		if (alarm == null)
		{
			return
		}

		// Set the volume (if going to use text-to-speech or play music)
		// TODO: Make this into a method?
		if (alarm.shouldUseTts || alarm.hasMedia)
		{
			// Start to gradually increase the alarm volume
			if (alarm.shouldGraduallyIncreaseVolume)
			{
				setupGraduallyIncreaseVolume()
			}
			else
			{
				setVolume()
			}
		}

		// Start text-to-speech
		if (alarm.shouldUseTts)
		{
			speak()
		}
		else
		{
			this.start()
		}
	}

	/**
	 * Start the normal wake up process.
	 */
	fun start()
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
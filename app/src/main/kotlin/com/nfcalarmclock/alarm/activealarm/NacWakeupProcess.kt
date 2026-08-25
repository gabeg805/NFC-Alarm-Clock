package com.nfcalarmclock.alarm.activealarm

import android.content.Context
import android.os.Handler
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.alarm.options.flashlight.NacFlashlight
import com.nfcalarmclock.alarm.options.tts.NacTextToSpeech
import com.nfcalarmclock.alarm.options.tts.NacTextToSpeech.OnSpeakingListener
import com.nfcalarmclock.alarm.options.tts.NacTranslate
import com.nfcalarmclock.alarm.options.vibrate.NacVibrator
import com.nfcalarmclock.alarm.options.volume.NacVolumeManager
import com.nfcalarmclock.shared.NacSharedPreferences
import com.nfcalarmclock.system.getDeviceProtectedStorageContext
import com.nfcalarmclock.system.media.NacAudioAttributes
import com.nfcalarmclock.system.media.NacAudioManager
import com.nfcalarmclock.system.mediaplayer.NacMediaPlayer

/**
 * Actions to take upon waking up, such as enabling NFC, playing music, etc.
 *
 * @param context Application context.
 * @param alarm Alarm.
 */
@UnstableApi
class NacWakeupProcess(
	private val context: Context,
	private val alarm: NacAlarm
)
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
	 * Say the current time at user specified intervals.
	 */
	private val speakHandler: Handler = Handler(context.mainLooper)

	/**
	 * Continue the wakeup process after being done with speaking with text-to-speech.
	 */
	private val continueWakeupHandler: Handler = Handler(context.mainLooper)

	/**
	 * Watchdog to make sure media is playing when it should be playing.
	 */
	private val mediaWatchdogHandler: Handler = Handler(context.mainLooper)

	/**
	 * Whether the alarm should vibrate or not.
	 */
	private val shouldVibrate: Boolean
		get() = alarm.shouldVibrate && sharedPreferences.shouldShowVibrateButton

	/**
	 * Whether the alarm should use the flashlight or not.
	 */
	private val shouldUseFlashlight: Boolean
		get() = alarm.shouldUseFlashlight && sharedPreferences.shouldShowFlashlightButton

	/**
	 * Vibrate the device.
	 */
	private val vibrator: NacVibrator? = if (shouldVibrate) NacVibrator(context) else null

	/**
	 * Flashlight.
	 */
	private val flashlight: NacFlashlight? = if (shouldUseFlashlight)
	{
		NacFlashlight(context)
	}
	else
	{
		null
	}

	/**
	 * Media player.
	 */
	private val mediaPlayer: NacMediaPlayer? = if (alarm.mediaPath.isNotEmpty())
	{
		// Create the media player
		val deviceContext = getDeviceProtectedStorageContext(context)
		val player = NacMediaPlayer(deviceContext, object : Player.Listener {

			/**
			 * Media item that is current playing changes.
			 */
			override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int)
			{
				// Super
				super.onMediaItemTransition(mediaItem, reason)

				// Get the path to the current media item
				val mediaPath = mediaItem?.mediaId ?: ""
				// TODO: Could the issue where shuffle does not work happen after reboot? Alarm runs, and then next alarm media player breaks?
				//println("Media item transition : $mediaPath")
				//println("Artist : ${mediaItem?.mediaMetadata?.artist}")
				//println("Title  : ${mediaItem?.mediaMetadata?.title}")

				// Save the path of the current media item
				sharedPreferences.currentPlayingAlarmMedia = mediaPath
			}

		})

		// Setup the media player
		player.onAudioFocusChangeListener = object : NacMediaPlayer.OnAudioFocusChangeListener {

			// Empty override functions so that nothing happens when audio
			// focus is lost. This means that audio should keep playing even if
			// audio focus is lost
			override fun onAudioFocusLoss(mediaPlayer: NacMediaPlayer) { }
			override fun onAudioFocusLossTransient(mediaPlayer: NacMediaPlayer) { }

		}

		// Return the media player
		player
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
			override fun onDoneSpeaking()
			{
				// Abandon audio focus
				NacAudioManager.abandonFocus(context, audioAttributes)

				// Use handler to start wake up process so that the media
				// player is accessed on the correct thread
				continueWakeupHandler.post { startNoTts() }
			}

			/**
			 * Called when the text-to-speech engine has started.
			 */
			override fun onStartSpeaking()
			{
				// Stop any vibration and flashlight when TTS is playing
				vibrator?.cleanup()
				flashlight?.cleanup()

				// Use handler to start wake up process so that the media
				// player is accessed on the correct thread
				continueWakeupHandler.post {

					// Check if the media player was playing music
					if (mediaPlayer?.wasPlaying == true)
					{
						// Pause the media player until done speaking
						mediaPlayer.pause()
					}

				}
			}

		})
	}
	else
	{
		null
	}

	/**
	 * Volume manager (gradually increase, restrict, snooze/dismiss with volume buttons).
	 */
	val volumeManager: NacVolumeManager = NacVolumeManager(context, alarm, audioAttributes)

	/**
	 * Cleanup various alarm objects.
	 */
	fun cleanup()
	{
		// Cleanup vibrate
		vibrator?.cleanup()

		// Cleanup the flashlight
		flashlight?.cleanup()

		// Cleanup the media player
		mediaPlayer?.release()

		// Cleanup the text-to-speech engine
		textToSpeech?.cleanup()
		speakHandler.removeCallbacksAndMessages(null)

		// Cleanup the continue wakeup handler
		continueWakeupHandler.removeCallbacksAndMessages(null)

		// Cleanup the media watchdog handler
		mediaWatchdogHandler.removeCallbacksAndMessages(null)

		// Cleanup volume resources
		volumeManager.cleanup()
	}

	/**
	 * Play music.
	 */
	private fun playMusic()
	{
		// Unable to play music
		if (mediaPlayer == null)
		{
			return
		}

		// Media player was playing music, so continue playing what was playing before
		if (mediaPlayer.wasPlaying)
		{
			mediaPlayer.play()
		}
		// Play the alarm
		else
		{
			// Uri of media that is playing
			val playingUri = mediaPlayer.playAlarm(alarm)

			// Check if the current playing uri does not match the path from the alarm
			if ((playingUri == null)
				|| ((playingUri.toString() != alarm.mediaPath) && (playingUri.toString() != alarm.localMediaPath)))
			{
				// Selected media for alarm is not available
				sharedPreferences.isSelectedMediaForAlarmNotAvailable = true
			}
		}
	}

	/**
	 * Setup the media watchdog to make sure media is playing when it should be.
	 */
	private fun setupMediaWatchdog()
	{
		// No media needs to be played for this alarm
		if ((mediaPlayer == null) && (textToSpeech == null))
		{
			return
		}

		// Start the watchdog
		mediaWatchdogHandler.postDelayed({

			// Media is not playing and TTS is not speaking, which means media should be
			// playing
			if (((mediaPlayer != null) && !mediaPlayer.exoPlayer.isPlaying)
				&& ((textToSpeech == null) || !textToSpeech.isSpeaking()))
			{
				// Start the wakeup process, everything except for TTS
				mediaPlayer.shouldShowToasts = false
				playMusic()
				mediaPlayer.shouldShowToasts = true
			}

			// Recursively call the watchdog
			setupMediaWatchdog()

		}, PERIOD_MEDIA_WATCHDOG)
	}

	/**
	 * Speak at the desired frequency.
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
		val phrase = NacTranslate.getTtsPhrase(context, alarm.shouldSayCurrentTime, alarm.shouldSayName, alarm.name)

		textToSpeech.speak(phrase, audioAttributes)

		// Check if text to speech should be run at a certain frequency
		if (alarm.ttsFrequency != 0)
		{
			// Wait for some period of time before speaking through TTS again
			speakHandler.postDelayed({ speak() }, alarm.ttsFrequency*60L*1000L)
		}
	}

	/**
	 * Start the wake up process.
	 */
	fun start()
	{
		// Setup the volume
		volumeManager.setup(alarm)

		// Start TTS
		if (alarm.shouldUseTts)
		{
			speak()
		}
		// Start everything except TTS
		else
		{
			startNoTts()
		}

		// Setup the media watchdog
		setupMediaWatchdog()
	}

	/**
	 * Start the wake up process, everything except for TTS.
	 */
	private fun startNoTts()
	{
		// Play music
		if (alarm.mediaPath.isNotEmpty())
		{
			playMusic()
		}

		// Vibrate
		if (shouldVibrate)
		{
			vibrator?.vibrate(alarm)
		}

		// Flashlight
		if (shouldUseFlashlight)
		{
			// Blink the flashlight
			if (alarm.shouldBlinkFlashlight)
			{
				flashlight?.blink(alarm.flashlightOnDuration, alarm.flashlightOffDuration)
			}
			// Turn on the flashlight
			else
			{
				flashlight?.turnOn()
			}
		}
	}

	companion object
	{

		/**
		 * Period at which to check for media playing with the watchdog.
		 */
		private const val PERIOD_MEDIA_WATCHDOG = 10000L

	}

}
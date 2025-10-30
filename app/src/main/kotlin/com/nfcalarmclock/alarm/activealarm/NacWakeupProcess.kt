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
import com.nfcalarmclock.shared.NacSharedPreferences
import com.nfcalarmclock.system.getDeviceProtectedStorageContext
import com.nfcalarmclock.system.media.NacAudioAttributes
import com.nfcalarmclock.system.media.NacAudioManager
import com.nfcalarmclock.system.media.isMediaDirectory
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
	 * Listener if a volume key press occurred.
	 */
	fun interface OnVolumeKeyPressListener
	{
		fun onVolumeKeyPress(alarm: NacAlarm)
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
	 * Volume key press.
	 */
	private val volumeKeyPressHandler: Handler = Handler(context.mainLooper)

	/**
	 * Watchdog to make sure media is playing when it should be playing.
	 */
	private val mediaWatchdogHandler: Handler = Handler(context.mainLooper)

	/**
	 * Flag indicating whether to skip the restrict volume check until the
	 * gradually increase volume process has started.
	 */
	private var hasGraduallyIncreaseVolumeStarted: Boolean = false

	/**
	 * Volume level to restrict any volume changes to.
	 *
	 * This is not just the alarm volume, since if the user wants to gradually
	 * increase the volume, the restricted volume in that case should be lower
	 * than the alarm volume.
	 */
	private var volumeToRestrictChangeTo: Int = -1

	/**
	 * Volume level to restrict any volume changes to.
	 *
	 * This is not just the alarm volume, since if the user wants to gradually
	 * increase the volume, the restricted volume in that case should be lower
	 * than the alarm volume.
	 */
	private var initialVolume: Int = 0

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
	private val mediaPlayer: NacMediaPlayer? = if (alarm.hasMedia)
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
	 * Volume key press listener.
	 */
	var onVolumeKeyPressListener: OnVolumeKeyPressListener? = null

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

		// Check if the the current volume was saved and if so, then it should
		// be reverted
		if (alarm.shouldUseTts || alarm.hasMedia)
		{
			// Revert the volume
			audioAttributes.revertVolume()
		}

		// Cleanup the text-to-speech engine
		textToSpeech?.cleanup()
		speakHandler.removeCallbacksAndMessages(null)

		// Cleanup the continue wakeup handler
		continueWakeupHandler.removeCallbacksAndMessages(null)

		// Cleanup the media watchdog handler
		mediaWatchdogHandler.removeCallbacksAndMessages(null)

		// Cleanup the gradually increasing volume handler
		graduallyIncreaseVolumeHandler.removeCallbacksAndMessages(null)

		// Cleanup the restrict volume handler
		restrictVolumeHandler.removeCallbacksAndMessages(null)

		// Cleanup the volume key press handler
		volumeKeyPressHandler.removeCallbacksAndMessages(null)
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
			alarm.graduallyIncreaseVolumeWaitTime * 1000L)
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

			// Call the volume key press listener
			println("RESTRICT VOLUME FOUND KEY PRESS!")
			onVolumeKeyPressListener?.onVolumeKeyPress(alarm)
			volumeKeyPressHandler.removeCallbacksAndMessages(null)
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
			// Check if the media being played is a directory
			if (alarm.mediaType.isMediaDirectory())
			{
				// Set shuffle mode (can be set or not set) based on the preference
				mediaPlayer.exoPlayer.shuffleModeEnabled = alarm.shouldShuffleMedia
			}

			// Play the alarm
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

		}, alarm.graduallyIncreaseVolumeWaitTime * 1000L)
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
		// Using text-to-speech or playing music. The reason being that if these are
		// not being used, then there is no point in changing the volume
		if (alarm.shouldUseTts || alarm.hasMedia)
		{
			// Save the current volume level so it can be reverted later
			audioAttributes.saveCurrentVolume()

			// Set the volume to the alarm volume and save the volume level so
			// that it can be correctly reverted back once the wakeup process
			// is complete
			audioAttributes.setStreamVolume()
			println("Current stream volume : ${audioAttributes.streamVolume}")

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

		// Set the initial volume
		initialVolume = audioAttributes.streamVolume
		println("Setting initial volume : $initialVolume")

		// Watch for volume key press
		if (alarm.shouldVolumeSnooze)
		{
			volumeKeyPressWatchdog()
		}
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
		setupVolume()

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
		if (alarm.hasMedia)
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

	/**
	 * Setup whether pressing the volume buttons should snooze the alarm. This runs a
	 * handler every second to see if the volume level has changed.
	 */
	private fun volumeKeyPressWatchdog()
	{
		volumeKeyPressHandler.postDelayed({

			// Get the current volume
			val currentVolume = audioAttributes.streamVolume

			// Volume was changed
			println("Checking volume levels! $initialVolume | $currentVolume")
			if (initialVolume != currentVolume)
			{
				println("OMG VOLUME PRESSED!")
				// Call the volume key press listener
				onVolumeKeyPressListener?.onVolumeKeyPress(alarm)
				volumeKeyPressHandler.removeCallbacksAndMessages(null)
			}
			// No change in volume. Keep the watchdog running
			else
			{
				volumeKeyPressWatchdog()
			}

		}, PERIOD_VOLUME_KEY_PRESS)
	}

	companion object
	{

		/**
		 * Period at which to check for media playing with the watchdog.
		 */
		private const val PERIOD_MEDIA_WATCHDOG = 10000L

		/**
		 * Period at which to ensure the volume is restricted.
		 */
		private const val PERIOD_RESTRICT_VOLUME = 1000L

		/**
		 * Period at which to check for volume key press by looking for volume changes.
		 */
		private const val PERIOD_VOLUME_KEY_PRESS = 1000L

	}

}
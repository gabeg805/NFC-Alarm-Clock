package com.nfcalarmclock.activealarm

import android.annotation.TargetApi
import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.Handler
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.media.NacAudioAttributes
import com.nfcalarmclock.media.NacMedia
import com.nfcalarmclock.mediaplayer.NacMediaPlayer
import com.nfcalarmclock.shared.NacSharedPreferences
import com.nfcalarmclock.tts.NacTextToSpeech
import com.nfcalarmclock.tts.NacTextToSpeech.OnSpeakingListener
import com.nfcalarmclock.tts.NacTranslate
import java.util.Calendar

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
		 * Period at which to ensure the volume is restricted.
		 */
		private const val PERIOD_RESTRICT_VOLUME = 1000L

	}

	/**
	 * The camera manager for enabling the flashlight.
	 */
	private val cameraManager = context.getSystemService(AppCompatActivity.CAMERA_SERVICE) as CameraManager

	/**
	 * Audio attributes.
	 */
	private var audioAttributes: NacAudioAttributes = NacAudioAttributes(context, alarm)

	/**
	 * Vibrate handler to vibrate the phone at periodic intervals.
	 */
	private val vibrateHandler: Handler = Handler(context.mainLooper)

	/**
	 * Flashlight handler to shine the flashlight at periodic intervals.
	 */
	private val flashlightHandler: Handler = Handler(context.mainLooper)

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
	 * The first camera ID that is able to use the flashlight.
	 */
	private val cameraId: String? = cameraManager.cameraIdList.firstOrNull { id ->

			// Get the camera characteristics
			val char = cameraManager.getCameraCharacteristics(id)
			//println("Face: ${char.get(CameraCharacteristics.LENS_FACING)}")
			//println("Str : ${char.get(CameraCharacteristics.FLASH_INFO_AVAILABLE)}")

			// Check if the flashlight is available
			char.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true

	}

	/**
	 * Media player.
	 */
	private val mediaPlayer: NacMediaPlayer? = if (alarm.hasMedia)
	{
		// Create the media player
		val player = NacMediaPlayer(context, this)

		// Setup the media player
		player.onAudioFocusChangeListener = object: NacMediaPlayer.OnAudioFocusChangeListener {

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
				// Use handler to start wake up process so that the media
				// player is accessed on the correct thread
				continueWakeupHandler.post { simpleStart() }
			}

			/**
			 * Called when the text-to-speech engine has started.
			 */
			override fun onStartSpeaking(tts: NacTextToSpeech)
			{
				// Stop any vibration when TTS is playing
				cleanupVibrate()

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
				phrase += " $sayAlarmName"
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
			return NacTranslate.getSayAlarmName(context.resources, alarm.name)
		}

	/**
	 * The words that should be said when stating the time, in the
	 * designated language.
	 */
	private val sayCurrentTime: String
		get()
		{
			// Get the current hour and minute
			val calendar = Calendar.getInstance()
			val hour = calendar[Calendar.HOUR_OF_DAY]
			val minute = calendar[Calendar.MINUTE]

			// Get how to say the current time
			return NacTranslate.getSayCurrentTime(context, hour, minute)
		}

	/**
	 * Cleanup various alarm objects.
	 */
	fun cleanup()
	{
		// Cleanup vibrate
		cleanupVibrate()

		// Cleanup the flashlight
		cleanupFlashlight()

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
	 * Cleanup shining the flashlight.
	 */
	private fun cleanupFlashlight()
	{
		// Check if the camera ID is set
		if (cameraId != null)
		{
			// Turn off the flashlight
			println("TURNING OFF FLASHLIGHT")
			cameraManager.setTorchMode(cameraId, false)
		}

		// Stop any future flashing to occur
		flashlightHandler.removeCallbacksAndMessages(null)
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
	 * Find the camera ID.
	 */
	private fun findCameraId(): String
	{
		// Iterate over the list of camera IDs
		for (id in cameraManager.cameraIdList)
		{
			// Get the camera characteristics
			val char = cameraManager.getCameraCharacteristics(id)
			//println("Face: ${char.get(CameraCharacteristics.LENS_FACING)}")
			//println("Str : ${char.get(CameraCharacteristics.FLASH_INFO_AVAILABLE)}")

			// Check if the flashlight is available
			if (char.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true)
			{
				return id
			}
		}

		// Unable to find a camera ID
		return ""
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
	 * Called when the media item that is current playing changes.
	 */
	override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int)
	{
		// Super
		super.onMediaItemTransition(mediaItem, reason)

		// Get the path to the current media item
		val sharedPreferences = NacSharedPreferences(context)
		val mediaPath = mediaItem?.mediaId ?: ""

		// Save the path of the current media item
		sharedPreferences.editCurrentPlayingAlarmMedia(mediaPath)
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
			// Check if the media being played is a directory
			if (NacMedia.isDirectory(alarm.mediaType))
			{
				// Set shuffle mode (can be set or not set) based on the preference
				mediaPlayer.exoPlayer.shuffleModeEnabled = alarm.shuffleMedia
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
	 * Shine the flashlight.
	 */
	fun shineFlashlight()
	{
		// Check if the camera ID is set
		if (cameraId != null)
		{
			// Turn on the flashlight
			println("TURNING ON FLASHLIGHT")
			cameraManager.setTorchMode(cameraId, true)
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

		// Vibrate
		if (alarm.shouldVibrate)
		{
			vibrate()
		}

		// Flashlight
		if (alarm.shouldUseFlashlight)
		{
			println("CAMERA ID: $cameraId")
			shineFlashlight()
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
		textToSpeech.speak(ttsPhrase, audioAttributes)

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
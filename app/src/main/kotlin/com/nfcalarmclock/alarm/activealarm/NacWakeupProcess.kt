package com.nfcalarmclock.alarm.activealarm

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
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
import com.nfcalarmclock.log.NacLog
import com.nfcalarmclock.shared.NacSharedPreferences
import com.nfcalarmclock.system.getDeviceProtectedStorageContext
import com.nfcalarmclock.system.media.NacAudioAttributes
import com.nfcalarmclock.system.media.abandonFocus
import com.nfcalarmclock.system.media.getSafeStreamVolume
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
	 * Audio manager.
	 */
	val audioManager: AudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

	/**
	 * Shared preferences.
	 */
	private val sharedPreferences: NacSharedPreferences = NacSharedPreferences(context)

	/**
	 * Audio attributes.
	 */
	private var audioAttributes: NacAudioAttributes = NacAudioAttributes(context, alarm)

	/**
	 * Bluetooth audio attributes.
	 */
	private var bluetoothAudioAttributes: NacAudioAttributes? = if (alarm.shouldPlayAudioThroughSpeakersAndBluetooth)
	{
		NacAudioAttributes(context, alarm)
	}
	else
	{
		null
	}

	/**
	 * Say the current time at user specified intervals.
	 */
	private val speakHandler: Handler = Handler(context.mainLooper)

	/**
	 * Continue the wakeup process after being done with speaking with text-to-speech.
	 *
	 * TODO: Can maybe change usage of this handler with a lifecyclescope?
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
		// Get device protected storage context
		// TODO: Why does device protected storage context matter?
		val deviceContext = getDeviceProtectedStorageContext(context)

		// Create the media player
		NacMediaPlayer(deviceContext,
			listener = object : Player.Listener
			{

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
					//println("Artist : ${mediaItem?.mediaMetadata?.artist}")
					//println("Title  : ${mediaItem?.mediaMetadata?.title}")

					NacLog.i("Media player item transition. mediaPath=$mediaPath")

					if ((mediaItem != null) && (bluetoothMediaPlayer != null))
					{
						NacLog.i("Playing same media item on bluetooth device")
						bluetoothMediaPlayer.playMediaItem(context, mediaItem)
					}

					// Save the path of the current media item
					sharedPreferences.currentPlayingAlarmMedia = mediaPath
				}
			},
			audioAttributes = audioAttributes)
			.apply {
				onAudioFocusChangeListener = object : NacMediaPlayer.OnAudioFocusChangeListener {

					// Empty override functions so that nothing happens when audio
					// focus is lost. This means that audio should keep playing even if
					// audio focus is lost
					override fun onAudioFocusLoss(mediaPlayer: NacMediaPlayer) {}
					override fun onAudioFocusLossTransient(mediaPlayer: NacMediaPlayer) {}

				}
			}
	}
	else
	{
		null
	}

	/**
	 * Bluetooth media player.
	 */
	private val bluetoothMediaPlayer: NacMediaPlayer? = if ((mediaPlayer != null) && alarm.shouldPlayAudioThroughSpeakersAndBluetooth)
	{
		// Get device protected storage context
		// TODO: Why does device protected storage context matter?
		val deviceContext = getDeviceProtectedStorageContext(context)

		// Create the media player
		NacMediaPlayer(deviceContext, listener = null, audioAttributes = bluetoothAudioAttributes!!)
			.apply {
				onAudioFocusChangeListener = object : NacMediaPlayer.OnAudioFocusChangeListener {

					// Empty override functions so that nothing happens when audio
					// focus is lost. This means that audio should keep playing even if
					// audio focus is lost
					override fun onAudioFocusLoss(mediaPlayer: NacMediaPlayer) {}
					override fun onAudioFocusLossTransient(mediaPlayer: NacMediaPlayer) {}

				}
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
			 * Done speaking.
			 */
			override fun onDoneSpeaking()
			{
				// Abandon audio focus
				audioManager.abandonFocus(audioAttributes)

				NacLog.i("Done speaking. currentVolume=${mediaPlayer?.audioManager?.getSafeStreamVolume(audioAttributes.stream)}")

				// Use handler to start wake up process so that the media
				// player is accessed on the correct thread
				continueWakeupHandler.post { startNoTts() }
			}

			/**
			 * Text-to-speech engine has started.
			 */
			override fun onStartSpeaking()
			{
				NacLog.i("Starting speaking. currentVolume=${mediaPlayer?.audioManager?.getSafeStreamVolume(audioAttributes.stream)}")

				// Stop any vibration and flashlight when TTS is playing
				vibrator?.cleanup()
				flashlight?.cleanup()

				// Use handler to start wake up process so that the media
				// player is accessed on the correct thread
				continueWakeupHandler.post {

					// Media player was playing music. Pause it until done speaking
					if (mediaPlayer?.wasPlaying == true)
					{
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
	 * Bluetooth text-to-speech engine.
	 */
	private val bluetoothTextToSpeech: NacTextToSpeech? = if ((textToSpeech != null) && alarm.shouldPlayAudioThroughSpeakersAndBluetooth)
	{
		NacTextToSpeech(context, object: OnSpeakingListener {

			/**
			 * Done speaking.
			 */
			override fun onDoneSpeaking()
			{
				// Abandon audio focus
				audioManager.abandonFocus(bluetoothAudioAttributes!!)

				NacLog.i("Done speaking through bluetooth. currentVolume=${bluetoothMediaPlayer?.audioManager?.getSafeStreamVolume(bluetoothAudioAttributes!!.stream)}")

				// Use handler to start wake up process so that the media
				// player is accessed on the correct thread
				continueWakeupHandler.post { startNoTts() }
			}

			/**
			 * Text-to-speech engine has started.
			 */
			override fun onStartSpeaking()
			{
				NacLog.i("Starting speaking through bluetooth. currentVolume=${bluetoothMediaPlayer?.audioManager?.getSafeStreamVolume(bluetoothAudioAttributes!!.stream)}")

				// Use handler to start wake up process so that the media
				// player is accessed on the correct thread
				continueWakeupHandler.post {

					// Media player was playing music. Pause it until done speaking
					if (bluetoothMediaPlayer?.wasPlaying == true)
					{
						bluetoothMediaPlayer.pause()
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
	 * Bluetooth volume manager (gradually increase, restrict, snooze/dismiss with volume buttons).
	 */
	val bluetoothVolumeManager: NacVolumeManager? = if (alarm.shouldPlayAudioThroughSpeakersAndBluetooth)
	{
		NacVolumeManager(context, alarm, bluetoothAudioAttributes!!)
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
		// Audio should be played through speakers and bluetooth
		if (alarm.shouldPlayAudioThroughSpeakersAndBluetooth)
		{
			// Set the correct audio usages. Phone should always be ALARM and bluetooth should
			// either be the original audio source the user selected or MEDIA
			// TODO: ContentType SONIFICATION for USAGE_ALARM
			if (audioAttributes.audioUsage == AudioAttributes.USAGE_ALARM)
			{
				bluetoothAudioAttributes!!.audioUsage = AudioAttributes.USAGE_MEDIA
			}
			else
			{
				bluetoothAudioAttributes!!.audioUsage = audioAttributes.audioUsage
				audioAttributes.audioUsage = AudioAttributes.USAGE_ALARM
			}

			NacLog.i("Normal audio attr usage    : ${audioAttributes.audioUsage}")
			NacLog.i("Bluetooth audio attr usage : ${bluetoothAudioAttributes?.audioUsage}")

			// Find the builtin speaker and bluetooth device
			findPreferredAudioDevices()
		}
	}

	/**
	 * Find the preferred audio devices.
	 */
	private fun findPreferredAudioDevices()
	{
		NacLog.i("Finding preferred audio devices")

		// Register callback to find any connected bluetooth speakers and play audio through
		// them as well
		audioManager.registerAudioDeviceCallback(object : AudioDeviceCallback()
		{

			override fun onAudioDevicesAdded(devices: Array<out AudioDeviceInfo>)
			{
				devices.toList().forEach {
					println("Device type=${it.type} | product=${it.productName}")
					NacLog.i("Device type=${it.type} | product=${it.productName}")
				}

				//val bluetoothHearingAid = devices.find {
				//	// TODO: Add this when supporting API 37
				//	//|| ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.37)
				//	//	&& (it.type == AudioDeviceInfo.TYPE_BLE_HEARING_AID))
				//}

				// Attempt to find different types of bluetooth devices
				val bluetoothA2dp: AudioDeviceInfo? = devices.find { it.type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP }
				var bluetoothSpeaker: AudioDeviceInfo? = null
				var bluetoothHeadset: AudioDeviceInfo? = null

				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
				{
					bluetoothSpeaker = devices.find { it.type == AudioDeviceInfo.TYPE_BLE_HEADSET }
					bluetoothHeadset = devices.find { it.type == AudioDeviceInfo.TYPE_BLE_SPEAKER }
				}

				// Choose bluetooth device based on priority
				val bluetoothDevice = bluetoothA2dp ?: bluetoothSpeaker ?: bluetoothHeadset

				// Find the builtin speaker
				val builtinSpeakerDevice = devices.find { it.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER }

				// Phone speakers as the preferred device for the regular media player
				if (builtinSpeakerDevice != null)
				{
					NacLog.i("Setting builtin speaker preferred device. name=${builtinSpeakerDevice.productName} | type=${builtinSpeakerDevice.type}")
					mediaPlayer!!.exoPlayer.setPreferredAudioDevice(builtinSpeakerDevice)
				}

				// Bluetooth device for the bluetooth media player
				if (bluetoothDevice != null)
				{
					NacLog.i("Setting bluetooth preferred device. name=${bluetoothDevice.productName} | type=${bluetoothDevice.type}")
					bluetoothMediaPlayer!!.exoPlayer.setPreferredAudioDevice(bluetoothDevice)
				}

				// Unregister the callback
				audioManager.unregisterAudioDeviceCallback(this)
			}

		}, null)
	}

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
		bluetoothMediaPlayer?.release()

		// Cleanup the text-to-speech engine
		textToSpeech?.cleanup()
		bluetoothTextToSpeech?.cleanup()
		speakHandler.removeCallbacksAndMessages(null)

		// Cleanup the continue wakeup handler
		continueWakeupHandler.removeCallbacksAndMessages(null)

		// Cleanup the media watchdog handler
		mediaWatchdogHandler.removeCallbacksAndMessages(null)

		// Cleanup volume resources
		volumeManager.cleanup()
		bluetoothVolumeManager?.cleanup()
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
			NacLog.i("Was playing so playing both media player and bluetooth player. bluetoothPlayer=${bluetoothMediaPlayer != null}")
			mediaPlayer.play(context)
			bluetoothMediaPlayer?.play(context)
		}
		// Play the alarm
		else
		{
			// Uri of media that is playing
			val playingUri = mediaPlayer.playAlarm(context, alarm)
			//bluetoothMediaPlayer?.playAlarm(context, alarm)

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
		bluetoothTextToSpeech?.speak(phrase, bluetoothAudioAttributes!!)

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
		bluetoothVolumeManager?.setup(alarm)

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
			vibrator?.vibrateAlarm(alarm)
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
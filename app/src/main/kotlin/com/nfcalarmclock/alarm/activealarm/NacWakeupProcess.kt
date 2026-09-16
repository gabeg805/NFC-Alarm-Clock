package com.nfcalarmclock.alarm.activealarm

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.os.Handler
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.alarm.options.flashlight.NacFlashlight
import com.nfcalarmclock.alarm.options.tts.NacTextToSpeech
import com.nfcalarmclock.alarm.options.tts.NacTranslate
import com.nfcalarmclock.alarm.options.vibrate.NacVibrator
import com.nfcalarmclock.alarm.options.volume.NacVolumeManager
import com.nfcalarmclock.log.NacLog
import com.nfcalarmclock.shared.NacSharedPreferences
import com.nfcalarmclock.system.getDeviceProtectedStorageContext
import com.nfcalarmclock.system.media.NacAudioAttributes
import com.nfcalarmclock.system.media.abandonFocus
import com.nfcalarmclock.system.media.getSafeStreamVolume
import com.nfcalarmclock.system.media.saveCurrentBluetoothVolume
import com.nfcalarmclock.system.media.saveCurrentVolume
import com.nfcalarmclock.system.media.setStreamVolume
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
	 * Audio attributes for text-to-speech.
	 *
	 * Note: There are no bluetooth audio attributes for TTS because even though there could be
	 * two TTS objects, one for phone and one for bluetooth, the system queues the two speak()
	 * requests because the TTS engine is single threaded. This would result in TTS playing once
	 * for the phone, which also goes through bluetooth, and then once solely through the
	 * bluetooth. A user could hear the same TTS sentence twice through bluetooth. I thought this
	 * would be annoying so TTS will only play through the phone.
	 */
	private var audioAttributesTts: NacAudioAttributes? = null

	/**
	 * Bluetooth audio attributes.
	 *
	 * Note: This can also be set to null if no bluetooth device is found.
	 */
	private var bluetoothAudioAttributes: NacAudioAttributes? = if (alarm.shouldPlayAudioThroughSpeakersAndBluetooth)
	{
		NacAudioAttributes(context, alarm, contentType = AudioAttributes.CONTENT_TYPE_MUSIC)
			.apply {

				// Set the correct audio usages. Phone should always be ALARM and bluetooth should
				// either be the original audio source the user selected or MEDIA
				if (this@NacWakeupProcess.audioAttributes.audioUsage == AudioAttributes.USAGE_ALARM)
				{
					audioUsage = AudioAttributes.USAGE_MEDIA
				}
				else
				{
					audioUsage = this@NacWakeupProcess.audioAttributes.audioUsage
					this@NacWakeupProcess.audioAttributes.audioUsage = AudioAttributes.USAGE_ALARM
				}

			}
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
	private val vibrator: NacVibrator? = if (shouldVibrate)
	{
		NacVibrator(context)
	}
	else
	{
		null
	}

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
		NacMediaPlayer(
			getDeviceProtectedStorageContext(context),
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

					// Save the path of the current media item
					sharedPreferences.currentPlayingAlarmMedia = mediaPath
				}
			},
			audioAttributes = audioAttributes)
	}
	else
	{
		null
	}

	/**
	 * Bluetooth media player.
	 *
	 * Note: This can also be set to null if no bluetooth device is found.
	 */
	private var bluetoothMediaPlayer: NacMediaPlayer? = if ((mediaPlayer != null) && alarm.shouldPlayAudioThroughSpeakersAndBluetooth)
	{
		NacMediaPlayer(
			getDeviceProtectedStorageContext(context),
			listener = null,
			audioAttributes = bluetoothAudioAttributes!!)
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
		NacTextToSpeech(context,
			onStartSpeaking = {
				NacLog.i("Starting to speak", offsetIndex = 1)
			},
			onDoneSpeaking = {
				NacLog.i("Done speaking", offsetIndex = 1)

				// Abandon audio focus
				audioManager.abandonFocus(audioAttributesTts!!)

				// Use handler to start wake up process so that the media player is accessed on
				// the correct thread. Have a delay so that there is no OS level volume ducking
				isHandlerRunning = true
				continueWakeupHandler.postDelayed({
					startNoTts()
					isHandlerRunning = false
				}, 500)
			})
	}
	else
	{
		null
	}

	/**
	 * Whether the continue wakeup handler is running a task or not.
	 */
	private var isHandlerRunning: Boolean = false

	/**
	 * Volume manager (gradually increase, restrict, snooze/dismiss with volume buttons).
	 */
	val volumeManager: NacVolumeManager = NacVolumeManager(context, alarm, audioAttributes)

	/**
	 * Bluetooth volume manager (gradually increase, restrict, snooze/dismiss with volume buttons).
	 *
	 * Note: This can also be set to null if no bluetooth device is found.
	 */
	var bluetoothVolumeManager: NacVolumeManager? = if (alarm.shouldPlayAudioThroughSpeakersAndBluetooth)
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
			NacLog.i("Normal audio attr usage    : ${audioAttributes.audioUsage}")
			NacLog.i("Bluetooth audio attr usage : ${bluetoothAudioAttributes?.audioUsage}")

			// Find the builtin speaker and bluetooth device
			findPreferredAudioDevices()
		}

		// Create the TTS audio attributes
		if (textToSpeech != null)
		{
			audioAttributesTts = audioAttributes.copy()
				.apply { contentType = AudioAttributes.CONTENT_TYPE_SPEECH }
		}

		// Create the audio focus requests
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
		{
			// Media player audio focus requests
			if (mediaPlayer != null)
			{
				audioAttributes.audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
					.setAudioAttributes(audioAttributes.audioAttributes)
					.setWillPauseWhenDucked(true)
					.setOnAudioFocusChangeListener(mediaPlayer.onAudioFocusRequestChangeListener)
					.build()
				bluetoothAudioAttributes?.audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
					.setAudioAttributes(audioAttributes.audioAttributes)
					.setWillPauseWhenDucked(true)
					.setOnAudioFocusChangeListener(bluetoothMediaPlayer!!.onAudioFocusRequestChangeListener)
					.build()
			}

			// TTS audio focus requests
			if (textToSpeech != null)
			{
				audioAttributesTts!!.audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
					.setAudioAttributes(audioAttributesTts!!.audioAttributes)
					.setOnAudioFocusChangeListener { focusChange -> NacLog.i("FOCUS CHANGE : $focusChange") }
					.build()
			}
		}

	}

	/**
	 * Cleanup various alarm objects.
	 */
	fun cleanup()
	{
		NacLog.i("Cleaning up wakeup process")

		// Cleanup vibrate
		vibrator?.cleanup()

		// Cleanup the flashlight
		flashlight?.cleanup()

		// Cleanup the media player
		mediaPlayer?.release()
		bluetoothMediaPlayer?.release()

		// Cleanup the text-to-speech engine
		textToSpeech?.cleanup()
		speakHandler.removeCallbacksAndMessages(null)

		// Cleanup the continue wakeup handler
		continueWakeupHandler.removeCallbacksAndMessages(null)

		// Cleanup the media watchdog handler
		mediaWatchdogHandler.removeCallbacksAndMessages(null)

		// Cleanup volume resources
		volumeManager.cleanup(
			onRevertVolume = {
				NacLog.i("Reverting normal stream volume. fromVolume=${audioManager.getSafeStreamVolume(audioAttributes.stream)} | toVolume=${sharedPreferences.previousVolume}", offsetIndex = 1)
				audioManager.setStreamVolume(audioAttributes.stream, sharedPreferences.previousVolume)
			})
		bluetoothVolumeManager?.cleanup(
			onRevertVolume = {
				NacLog.i("Reverting bluetooth stream volume. fromVolume=${audioManager.getSafeStreamVolume(bluetoothAudioAttributes!!.stream)} | toVolume=${sharedPreferences.previousBluetoothVolume}", offsetIndex = 1)
				audioManager.setStreamVolume(bluetoothAudioAttributes!!.stream, sharedPreferences.previousBluetoothVolume)
			})
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
				// No bluetooth device found. Null all bluetooth objects so they cannot be used
				else
				{
					NacLog.i("No bluetooth device found")

					bluetoothMediaPlayer = null
					bluetoothAudioAttributes = null
					bluetoothVolumeManager = null
				}

				// Unregister the callback
				audioManager.unregisterAudioDeviceCallback(this)
			}

		}, null)
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
		if (mediaPlayer.isPaused)
		{
			NacLog.i("Media player(s) are paused. Playing both media player and bluetooth player. bluetoothPlayer=${bluetoothMediaPlayer != null}")
			mediaPlayer.play(context)
			bluetoothMediaPlayer?.play(context)
		}
		// Play the alarm
		else
		{
			NacLog.i("Starting to play alarm media")

			// Media item(s) to play
			val mediaItems = mediaPlayer.playAlarm(context, alarm)
			bluetoothMediaPlayer?.playMediaItems(context, mediaItems)

			// Selected media for alarm is not available
			if (mediaItems.isEmpty())
			{
				NacLog.i("Media items list is empty. Selected media for alarm not available")
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

			// Media is not playing and TTS is not speaking, which means media should be playing
			if (((mediaPlayer != null) && !mediaPlayer.exoPlayer.isPlaying && !mediaPlayer.isPaused)
				&& ((textToSpeech == null) || !textToSpeech.isSpeaking())
				&& !isHandlerRunning)
			{
				NacLog.w("Attempting to start media via watchdog")

				// Start the wakeup process, everything except for TTS
				mediaPlayer.shouldShowToasts = false
				bluetoothMediaPlayer?.shouldShowToasts = false
				playMusic()
				mediaPlayer.shouldShowToasts = true
				bluetoothMediaPlayer?.shouldShowToasts = true
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

		NacLog.i("Speaking with TTS")

		// Speak via TTS
		val phrase = NacTranslate.getTtsPhrase(context, alarm.shouldSayCurrentTime, alarm.shouldSayName, alarm.name)
		textToSpeech.speak(phrase, audioAttributesTts!!)

		// Check if text to speech should be run at a certain frequency
		if (alarm.ttsFrequency != 0)
		{
			// Wait for some period of time before speaking through TTS again
			speakHandler.postDelayed({

				// Set handler running flag (for continue wakeup handler)
				isHandlerRunning = true

				// Stop any vibration and flashlight when TTS is playing
				vibrator?.cleanup()
				flashlight?.cleanup()

				// Pause phone media player until done speaking
				if (mediaPlayer?.exoPlayer?.isPlaying == true)
				{
					NacLog.i("Pausing and abandoning media player focus")
					mediaPlayer.pause()
					audioManager.abandonFocus(audioAttributes)
				}

				// Pause bluetooth media player until done speaking
				if (bluetoothMediaPlayer?.exoPlayer?.isPlaying == true)
				{
					NacLog.i("Pausing and abandoning bluetooth media player focus")
					bluetoothMediaPlayer!!.pause()
					audioManager.abandonFocus(bluetoothAudioAttributes!!)
				}

				// Speak TTS. Have a delay so that there is no OS level volume ducking
				continueWakeupHandler.postDelayed({
					speak()
					isHandlerRunning = false
				}, 500)

			}, alarm.ttsFrequency*60L*1000L)
		}
	}

	/**
	 * Start the wake up process.
	 */
	fun start()
	{
		// Setup the volume
		volumeManager.setup(alarm, onSaveVolume = {
			NacLog.i("Saving normal stream volume. volume=${audioManager.getSafeStreamVolume(audioAttributes.stream)}", offsetIndex = 1)
			audioManager.saveCurrentVolume(sharedPreferences, audioAttributes.stream)
		})
		bluetoothVolumeManager?.setup(alarm, onSaveVolume = {
			NacLog.i("Saving bluetooth stream volume. volume=${audioManager.getSafeStreamVolume(bluetoothAudioAttributes!!.stream)}", offsetIndex = 1)
			audioManager.saveCurrentBluetoothVolume(sharedPreferences, bluetoothAudioAttributes!!.stream)
		})

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
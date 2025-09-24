package com.nfcalarmclock.util.media

import android.content.Context
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.shared.NacSharedPreferences

/**
 * Audio attributes.
 */
class NacAudioAttributes(

	/**
	 * Context.
	 */
	private val context: Context,

	/**
	 * Source.
	 */
	source: String = ""

)
{

	/**
	 * Shared preferences.
	 */
	private val sharedPreferences: NacSharedPreferences = NacSharedPreferences(context)

	/**
	 * Audio usage.
	 */
	private var audioUsage = 0

	/**
	 * Volume level.
	 */
	private var volumeLevel = 0

	/**
	 * Flag indicating if was ducking or not.
	 */
	private var wasDucking = false

	/**
	 * Audio attributes.
	 */
	val audioAttributes: AudioAttributes
		get() = AudioAttributes.Builder()
					.setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
					.setUsage(audioUsage)
					.build()

	/**
	 * Audio attributes v21.
	 */
	val audioAttributesV21: android.media.AudioAttributes
		get() = audioAttributes.audioAttributesV21.audioAttributes

	/**
	 * Audio manager.
	 */
	private val audioManager: AudioManager
		get() = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

	/**
	 * Audio focus request object that is used when initially requesting audio focus.
	 * This is set by the NacAudioManager.
	 */
	var audioFocusRequest: AudioFocusRequest? = null

	/**
	 * Audio stream.
	 */
	val stream: Int
		get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
		{
			audioAttributesV21.volumeControlStream
		}
		else
		{
			NacAudioManager.usageToStream(audioUsage)
		}

	/**
	 * Volume of the stream.
	 */
	var streamVolume: Int
		get() = if (stream != AudioManager.USE_DEFAULT_STREAM_TYPE)
		{
			audioManager.getStreamVolume(stream)
		}
		else
		{
			0
		}
		set(volume)
		{
			// Unable to change the volume because the volume is fixed or because the
			// stream is invalid
			if (audioManager.isVolumeFixed || stream == AudioManager.USE_DEFAULT_STREAM_TYPE)
			{
				return
			}

			// Set the stream volume
			try
			{
				audioManager.setStreamVolume(stream, volume, 0)
			}
			catch (_: SecurityException)
			{
			}
		}

	/**
	 * Maximum stream volume.
	 */
	private val streamMaxVolume: Int
		get() = if (stream != AudioManager.USE_DEFAULT_STREAM_TYPE)
		{
			audioManager.getStreamMaxVolume(stream)
		}
		else
		{
			0
		}

	/**
	 * Speech rate for text-to-speech.
	 */
	var speechRate: Float = 0f

	/**
	 * Voice name for text-to-speech.
	 */
	var voice: String = ""

	/**
	 * Constructor.
	 */
	constructor(context: Context, alarm: NacAlarm) : this(context, "")
	{
		merge(alarm)
	}

	/**
	 * Constructor.
	 */
	init
	{
		// Set usage from audio source
		setUsageFromSource(source)
	}

	/**
	 * Convert the alarm volume to a stream volume.
	 */
	fun alarmToStreamVolume(): Int
	{
		return (streamMaxVolume * volumeLevel / 100.0f).toInt()
	}

	/**
	 * Duck the volume.
	 */
	fun duckVolume()
	{
		// Set the ducking flag
		wasDucking = true

		// Save the current volume
		println("Duck volume : $streamVolume")
		sharedPreferences.previousVolume = streamVolume

		// Set the volume to half its current value
		streamVolume /= 2
	}

	/**
	 * Merge the current audio attributes with that of the alarm.
	 */
	fun merge(alarm: NacAlarm): NacAudioAttributes
	{
		// Set audio usage from audio source
		setUsageFromSource(alarm.audioSource)

		// Set the volume level
		volumeLevel = alarm.volume

		// Set the text-to-speech rate and voice
		speechRate = alarm.ttsSpeechRate
		voice = alarm.ttsVoice

		return this
	}

	/**
	 * Revert the effects of ducking.
	 */
	fun revertDucking()
	{
		if (wasDucking)
		{
			wasDucking = false

			// TODO: Check if this new addition is ok
			revertVolume()
		}
	}

	/**
	 * Revert the volume level.
	 */
	fun revertVolume()
	{
		// Set the volume to the previous volume
		println("Revert volume : ${sharedPreferences.previousVolume}")
		streamVolume = sharedPreferences.previousVolume
	}

	/**
	 * Save the current volume.
	 */
	fun saveCurrentVolume()
	{
		println("Save current volume : $streamVolume")
		sharedPreferences.previousVolume = streamVolume
	}

		/**
	 * Set the audio usage from the source name.
	 */
	private fun setUsageFromSource(source: String)
	{
		audioUsage = NacAudioManager.sourceToUsage(context, source)
	}

	/**
	 * Set the stream volume.
	 */
	fun setStreamVolume()
	{
		// Set the stream volume
		streamVolume = alarmToStreamVolume()
	}

}
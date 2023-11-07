package com.nfcalarmclock.media

import android.content.Context
import android.media.AudioManager
import android.os.Build
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.shared.NacSharedPreferences
import com.nfcalarmclock.util.NacUtility.printf

/**
 * Audio attributes.
 */
class NacAudioAttributes @JvmOverloads constructor(

	/**
	 * Context.
	 */
	private val context: Context,

	/**
	 * Source.
	 */
	source: String? = "")
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
	var volumeLevel = 0

	/**
	 * Flag indicating if was ducking or not.
	 */
	private var wasDucking = false

	/**
	 * Check if volume can be changed or not
	 */
	val canVolumeChange: Boolean
		get() = volumeLevel >= 0

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
			catch (e: SecurityException)
			{
				printf("NacAudioAttributes : SecurityException : setStreamVolume")
			}
		}

	/**
	 * Maximum stream volume.
	 */
	val streamMaxVolume: Int
		get() = if (stream != AudioManager.USE_DEFAULT_STREAM_TYPE)
		{
			audioManager.getStreamMaxVolume(stream)
		}
		else
		{
			0
		}

	/**
	 * Check if the stream volume is already set to the desired volume level.
	 */
	val isStreamVolumeAlreadySet: Boolean
		get()
		{
			val previous = streamVolume
			val volume = this.toStreamVolume()
			return previous == volume
		}

	/**
	 * Constructor.
	 */
	constructor(context: Context, alarm: NacAlarm?) : this(context, "")
	{
		merge(alarm)
	}

	/**
	 * Constructor.
	 */
	init
	{
		// Set usage volume source
		setUsageFromSource(source)

		// Not sure why setting volume level to -1
		volumeLevel = -1

		// Set ducking flag
		wasDucking = false
	}

	/**
	 * Duck the volume.
	 */
	fun duckVolume()
	{
		// Set the ducking flag
		wasDucking = true

		// Save the current volume
		sharedPreferences.editPreviousVolume(streamVolume)

		// Set the volume to half its current value
		streamVolume /= 2
	}

	/**
	 * Merge the current audio attributes with that of the alarm.
	 */
	fun merge(alarm: NacAlarm?): NacAudioAttributes
	{
		// Check if the alarm is not null
		if (alarm != null)
		{
			// Set audio usage from audio source
			setUsageFromSource(alarm.audioSource)

			// Set the volume level
			volumeLevel = alarm.volume
		}

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
		}
	}

	/**
	 * Revert the volume level.
	 */
	fun revertVolume()
	{
		// Check if the volume cannot be changed
		if (!canVolumeChange)
		{
			return
		}

		// Set the volume to the previous volume
		streamVolume = sharedPreferences.previousVolume
	}

	/**
	 * Set the audio usage from the source name.
	 */
	private fun setUsageFromSource(source: String?)
	{
		audioUsage = NacAudioManager.sourceToUsage(context, source)
	}

	/**
	 * Set the volume.
	 */
	fun setVolume()
	{
		// Check if the volume cannot be changed
		if (!canVolumeChange)
		{
			return
		}

		// Get the previous and current volumes
		val previous = streamVolume
		val volume = this.toStreamVolume()

		// Set the stream volume
		streamVolume = volume

		// Save the previous volume
		sharedPreferences.editPreviousVolume(previous)
	}

	/**
	 * @see NacAudioAttributes.toStreamVolume
	 */
	fun toStreamVolume(): Int
	{
		return this.toStreamVolume(volumeLevel)
	}

	/**
	 * Convert the volume level to the stream volume format.
	 */
	fun toStreamVolume(volumeLevel: Int): Int
	{
		return (streamMaxVolume * volumeLevel / 100.0f).toInt()
	}

}
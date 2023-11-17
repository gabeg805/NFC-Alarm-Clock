package com.nfcalarmclock.media

import android.content.Context
import android.media.AudioManager
import android.os.Build
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.shared.NacSharedPreferences

/**
 * Audio attributes.
 */
class NacAudioAttributes constructor(

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
			println("BIG FAT 0 when getting steream volume")
			0
		}
		set(volume)
		{
			println("BIG SET VOLUME")
			// Unable to change the volume because the volume is fixed or because the
			// stream is invalid
			if (audioManager.isVolumeFixed || stream == AudioManager.USE_DEFAULT_STREAM_TYPE)
			{
				println("Unable to because volume is fixed or stream is default : ${audioManager.isVolumeFixed} | $stream")
				return
			}

			// Set the stream volume
			try
			{
				println("Set stream ($stream) volume : $volume")
				audioManager.setStreamVolume(stream, volume, 0)
			}
			catch (e: SecurityException)
			{
				println("NacAudioAttributes : SecurityException : setStreamVolume")
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
		sharedPreferences.editPreviousVolume(streamVolume)

		// Set the volume to half its current value
		streamVolume /= 2
	}

	/**
	 * Merge the current audio attributes with that of the alarm.
	 */
	fun merge(alarm: NacAlarm): NacAudioAttributes
	{
		println("MERGE ALARM")
		// Set audio usage from audio source
		println("Set usage from source : ${alarm.audioSource}")
		setUsageFromSource(alarm.audioSource)

		// Set the volume level
		println("Set volume level : ${alarm.volume}")
		volumeLevel = alarm.volume

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
		println("Reverting volume : ${sharedPreferences.previousVolume}")
		streamVolume = sharedPreferences.previousVolume
	}

	/**
	 * Save the current volume.
	 */
	fun saveCurrentVolume()
	{
		println("Save current volume : $streamVolume")
		sharedPreferences.editPreviousVolume(streamVolume)
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
		println("Set volume : $streamVolume")
	}

}
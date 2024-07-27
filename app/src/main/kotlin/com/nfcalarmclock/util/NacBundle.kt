package com.nfcalarmclock.util

import android.os.BadParcelableException
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.media.NacAudioAttributes

/**
 * Bundle helper.
 */
object NacBundle
{

	/**
	 * Tag name associated with a parceled alarm.
	 */
	const val ALARM_PARCEL_NAME = "NacAlarmParcel"

	/**
	 * Key associated with a media path.
	 */
	private const val MEDIA_PATH_KEY = "NacMediaKey"

	/**
	 * Key associated with whether media should be shuffled.
	 */
	private const val SHUFFLE_MEDIA_KEY = "NacShuffleMediaKey"

	/**
	 * Key associated with whether media should be recursively played.
	 */
	private const val RECURSIVELY_PLAY_MEDIA_KEY = "NacRecursivelyPlayMediaKey"

	/**
	 * Get a bundle that contains the alarm.
	 *
	 * @return A bundle that contains the alarm.
	 */
	fun alarmToBundle(alarm: NacAlarm?): Bundle
	{
		// Create the bundle
		val bundle = Bundle()

		// Put the alarm in the bundle
		bundle.putParcelable(ALARM_PARCEL_NAME, alarm)

		return bundle
	}

	/**
	 * Get the alarm contained in the bundle.
	 *
	 * @return The alarm contained in the bundle.
	 */
	@Suppress("deprecation")
	fun getAlarm(bundle: Bundle?): NacAlarm?
	{
		return if (bundle != null)
			{
				try
				{
					bundle.classLoader = NacAlarm::class.java.classLoader

					// Use the updated form of Bundle.getParcelable() for API >= 33
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
					{
						bundle.getParcelable(ALARM_PARCEL_NAME, NacAlarm::class.java)
					}
					// Use the old form of getting parcelable
					else
					{
						bundle.getParcelable(ALARM_PARCEL_NAME)
					}
				}
				catch (bpe: BadParcelableException)
				{
					try
					{
						// Use the updated form of Bundle.getParcelable() for API >= 33
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
						{
							bundle.getParcelable(ALARM_PARCEL_NAME, NacAlarm::class.java)
						}
						// Use the old form of getting parcelable
						else
						{
							bundle.getParcelable(ALARM_PARCEL_NAME)
						}
					}
					catch (re: RuntimeException)
					{
						null
					}
				}
			}
			else
			{
				null
			}
	}

	/**
	 * Get the media path from a bundle.
	 *
	 * @return The media path from a bundle.
	 */
	fun getMediaPath(bundle: Bundle?): String
	{
		return bundle?.getString(MEDIA_PATH_KEY) ?: ""
	}

	/**
	 * Get whether media should be played recursively from a bundle.
	 *
	 * @return Whether media should be played recursively from a bundle.
	 */
	fun getRecursivelyPlayMedia(bundle: Bundle?): Boolean
	{
		return bundle?.getBoolean(RECURSIVELY_PLAY_MEDIA_KEY) ?: false
	}

	/**
	 * Get whether media should be shuffled from a bundle.
	 *
	 * @return Whether media should be shuffled from a bundle.
	 */
	fun getShuffleMedia(bundle: Bundle?): Boolean
	{
		return bundle?.getBoolean(SHUFFLE_MEDIA_KEY) ?: false
	}

	/**
	 * Get a bundle that contains a media path and how to play that media.
	 *
	 * @param mediaPath A media path.
	 * @param shuffleMedia Whether to shuffle media or not.
	 * @param recursivelyPlayMedia Whether to recursively play media or not.
	 *
	 * @return A bundle that contains a media path and how to play that media.
	 */
	fun mediaInfoToBundle(
		mediaPath: String?,
		shuffleMedia: Boolean,
		recursivelyPlayMedia: Boolean
	): Bundle
	{
		// Create the bundle
		val bundle = Bundle()

		// Media path
		bundle.putString(MEDIA_PATH_KEY, mediaPath)

		// Shuffle media
		bundle.putBoolean(SHUFFLE_MEDIA_KEY, shuffleMedia)

		// Recursively play media
		bundle.putBoolean(RECURSIVELY_PLAY_MEDIA_KEY, recursivelyPlayMedia)

		return bundle
	}

	/**
	 * @return A bundle that contains the parameter to control the volume level
	 * of a Text-to-Speech engine.
	 */
	fun toBundle(attrs: NacAudioAttributes): Bundle
	{
		// Create the bundle
		val bundle = Bundle()

		// Put the audio attributes in the bundle
		bundle.putInt(TextToSpeech.Engine.KEY_PARAM_STREAM, attrs.stream)

		return bundle
	}

}
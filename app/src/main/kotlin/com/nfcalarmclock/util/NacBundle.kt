package com.nfcalarmclock.util

import android.os.BadParcelableException
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.util.NacBundle.MEDIA_ARTIST_KEY
import com.nfcalarmclock.util.NacBundle.MEDIA_PATH_KEY
import com.nfcalarmclock.util.NacBundle.MEDIA_TITLE_KEY
import com.nfcalarmclock.util.NacBundle.MEDIA_TYPE_KEY
import com.nfcalarmclock.util.NacBundle.RECURSIVELY_PLAY_MEDIA_KEY
import com.nfcalarmclock.util.NacBundle.SHUFFLE_MEDIA_KEY
import com.nfcalarmclock.util.media.NacAudioAttributes

/**
 * Get a bundle that contains a media path and how to play that media.
 *
 * @param mediaPath A media path.
 * @param shuffleMedia Whether to shuffle media or not.
 * @param recursivelyPlayMedia Whether to recursively play media or not.
 *
 * @return A bundle that contains a media path and how to play that media.
 */
fun Bundle.addMediaInfo(
	mediaPath: String,
	mediaArtist: String,
	mediaTitle: String,
	mediaType: Int,
	shuffleMedia: Boolean,
	recursivelyPlayMedia: Boolean
): Bundle
{
	// Media path
	this.putString(MEDIA_PATH_KEY, mediaPath)

	// Media artist
	this.putString(MEDIA_ARTIST_KEY, mediaArtist)

	// Media title
	this.putString(MEDIA_TITLE_KEY, mediaTitle)

	// Media type
	this.putInt(MEDIA_TYPE_KEY, mediaType)

	// Shuffle media
	this.putBoolean(SHUFFLE_MEDIA_KEY, shuffleMedia)

	// Recursively play media
	this.putBoolean(RECURSIVELY_PLAY_MEDIA_KEY, recursivelyPlayMedia)

	return this
}

/**
 * Get the media artist from a bundle.
 *
 * @return The media artist from a bundle.
 */
fun Bundle.getMediaArtist(): String
{
	return this.getString(MEDIA_ARTIST_KEY) ?: ""
}

/**
 * Get the media path from a bundle.
 *
 * @return The media path from a bundle.
 */
fun Bundle.getMediaPath(): String
{
	return this.getString(MEDIA_PATH_KEY) ?: ""
}

/**
 * Get the media title from a bundle.
 *
 * @return The media title from a bundle.
 */
fun Bundle.getMediaTitle(): String
{
	return this.getString(MEDIA_TITLE_KEY) ?: ""
}

/**
 * Get the media type from a bundle.
 *
 * @return The media type from a bundle.
 */
fun Bundle.getMediaType(): Int
{
	return this.getInt(MEDIA_TYPE_KEY)
}

/**
 * Get whether media should be played recursively from a bundle.
 *
 * @return Whether media should be played recursively from a bundle.
 */
fun Bundle.getRecursivelyPlayMedia(): Boolean
{
	return this.getBoolean(RECURSIVELY_PLAY_MEDIA_KEY)
}

/**
 * Get whether media should be shuffled from a bundle.
 *
 * @return Whether media should be shuffled from a bundle.
 */
fun Bundle.getShuffleMedia(): Boolean
{
	return this.getBoolean(SHUFFLE_MEDIA_KEY)
}

/**
 * Bundle helper.
 */
object NacBundle
{

	/**
	 * Tag name for a parceled alarm.
	 */
	const val ALARM_PARCEL_NAME = "NacAlarmParcel"

	/**
	 * Media path key.
	 */
	const val MEDIA_PATH_KEY = "NacMediaPathKey"

	/**
	 * Media artist key.
	 */
	const val MEDIA_ARTIST_KEY = "NacMediaArtistKey"

	/**
	 * Media title key.
	 */
	const val MEDIA_TITLE_KEY = "NacMediaTitleKey"

	/**
	 * Media type key.
	 */
	const val MEDIA_TYPE_KEY = "NacMediaTypeKey"

	/**
	 * Whether media should be shuffled or not key.
	 */
	const val SHUFFLE_MEDIA_KEY = "NacShuffleMediaKey"

	/**
	 * Whether media should be recursively played or not key.
	 */
	const val RECURSIVELY_PLAY_MEDIA_KEY = "NacRecursivelyPlayMediaKey"

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
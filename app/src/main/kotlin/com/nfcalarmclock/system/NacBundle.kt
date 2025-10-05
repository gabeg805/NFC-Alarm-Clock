package com.nfcalarmclock.system

import android.os.BadParcelableException
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.system.NacBundle.ALARM_PARCEL_NAME
import com.nfcalarmclock.system.NacBundle.MEDIA_ARTIST_KEY
import com.nfcalarmclock.system.NacBundle.MEDIA_PATH_KEY
import com.nfcalarmclock.system.NacBundle.MEDIA_TITLE_KEY
import com.nfcalarmclock.system.NacBundle.MEDIA_TYPE_KEY
import com.nfcalarmclock.system.NacBundle.RECURSIVELY_PLAY_MEDIA_KEY
import com.nfcalarmclock.system.NacBundle.SHUFFLE_MEDIA_KEY
import com.nfcalarmclock.system.NacBundle.TIMER_PARCEL_NAME
import com.nfcalarmclock.timer.db.NacTimer
import com.nfcalarmclock.util.media.NacAudioAttributes

/**
 * Add an alarm to a bundle.
 *
 * @return A bundle that contains the alarm.
 */
fun Bundle.addAlarm(alarm: NacAlarm?): Bundle
{
	// Put the alarm in the bundle
	this.putParcelable(ALARM_PARCEL_NAME, alarm)

	return this
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
 * Add a timer to a bundle.
 *
 * @return A bundle that contains the timer.
 */
fun Bundle.addTimer(timer: NacTimer?): Bundle
{
	// Put the timer in the bundle
	this.putParcelable(TIMER_PARCEL_NAME, timer)

	return this
}

/**
 * Get the alarm contained in the bundle.
 *
 * @return The alarm contained in the bundle.
 */
@Suppress("deprecation")
fun Bundle.getAlarm(): NacAlarm?
{
	return try
	{
		this.classLoader = NacAlarm::class.java.classLoader

		// Use the updated form of Bundle.getParcelable() for API >= 33
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
		{
			this.getParcelable(ALARM_PARCEL_NAME, NacAlarm::class.java)
		}
		// Use the old form of getting parcelable
		else
		{
			this.getParcelable(ALARM_PARCEL_NAME)
		}
	}
	catch (_: BadParcelableException)
	{
		try
		{
			// Use the updated form of Bundle.getParcelable() for API >= 33
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
			{
				this.getParcelable(ALARM_PARCEL_NAME, NacAlarm::class.java)
			}
			// Use the old form of getting parcelable
			else
			{
				this.getParcelable(ALARM_PARCEL_NAME)
			}
		}
		catch (_: RuntimeException)
		{
			null
		}
	}
}

/**
 * Get the timer contained in the bundle.
 *
 * @return The timer contained in the bundle.
 */
@Suppress("deprecation")
fun Bundle.getTimer(): NacTimer?
{
	return try
		{
			this.classLoader = NacTimer::class.java.classLoader

			// Use the updated form of Bundle.getParcelable() for API >= 33
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
			{
				this.getParcelable(TIMER_PARCEL_NAME, NacTimer::class.java)
			}
			// Use the old form of getting parcelable
			else
			{
				this.getParcelable(TIMER_PARCEL_NAME)
			}
		}
		catch (_: BadParcelableException)
		{
			try
			{
				// Use the updated form of Bundle.getParcelable() for API >= 33
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
				{
					this.getParcelable(TIMER_PARCEL_NAME, NacTimer::class.java)
				}
				// Use the old form of getting parcelable
				else
				{
					this.getParcelable(TIMER_PARCEL_NAME)
				}
			}
			catch (_: RuntimeException)
			{
				null
			}
		}
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
 * Convert an alarm to a bundle.
 *
 * @return A bundle that contains the alarm.
 */
fun NacAlarm.toBundle(): Bundle
{
	// Create a bundle
	val bundle = Bundle()

	// Put the alarm in a bundle
	bundle.putParcelable(ALARM_PARCEL_NAME, this)

	return bundle
}

/**
 * Convert a timer to a bundle.
 *
 * @return A bundle that contains the timer.
 */
fun NacTimer.toBundle(): Bundle
{
	// Create a bundle
	val bundle = Bundle()

	// Put the alarm in a bundle
	bundle.putParcelable(TIMER_PARCEL_NAME, this)

	return bundle
}

/**
 * Convert text-to-speech audio attributes to a bundle.
 *
 * @return A bundle that contains the parameter to control the volume level
 *         of a Text-to-Speech engine.
 */
fun NacAudioAttributes.toBundle(): Bundle
{
	// Create a bundle
	val bundle = Bundle()

	// Put the audio attributes in a bundle
	bundle.putInt(TextToSpeech.Engine.KEY_PARAM_STREAM, this.stream)

	return bundle
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
	 * Intent action that needs to be passed to a Bundle.
	 */
	const val BUNDLE_INTENT_ACTION = "NacBundleIntentAction"

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
	 * Whether media should be recursively played or not key.
	 */
	const val RECURSIVELY_PLAY_MEDIA_KEY = "NacRecursivelyPlayMediaKey"

	/**
	 * Whether media should be shuffled or not key.
	 */
	const val SHUFFLE_MEDIA_KEY = "NacShuffleMediaKey"

	/**
	 * Tag name for a parceled timer.
	 */
	const val TIMER_PARCEL_NAME = "NacTimerParcel"

}
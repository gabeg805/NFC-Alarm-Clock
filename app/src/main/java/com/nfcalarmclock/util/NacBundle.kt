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
	 * Tag name associated with a media path.
	 */
	const val MEDIA_PARCEL_NAME = "NacMediaParcel"

	/**
	 * Get the alarm contained in the bundle.
	 *
	 * @return The alarm contained in the bundle.
	 */
	@JvmStatic
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
						bundle.classLoader = NacAlarm.Builder::class.java.classLoader

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
	 * Get the media contained in the bundle.
	 *
	 * @return The media contained in the bundle.
	 */
	@JvmStatic
	fun getMedia(bundle: Bundle?): String?
	{
		return bundle?.getString(MEDIA_PARCEL_NAME)
	}

	/**
	 * Get a  bundle that contains the alarm.
	 *
	 * @return A bundle that contains the alarm.
	 */
	@JvmStatic
	fun toBundle(alarm: NacAlarm?): Bundle
	{
		// Create the bundle
		val bundle = Bundle()

		// Put the alarm in the bundle
		bundle.putParcelable(ALARM_PARCEL_NAME, alarm)

		return bundle
	}

	/**
	 * Get a  bundle that contains the sound.
	 *
	 * @return A bundle that contains the sound.
	 */
	@JvmStatic
	fun toBundle(media: String?): Bundle
	{
		// Create the bundle
		val bundle = Bundle()

		// Put the media in the bundle
		bundle.putString(MEDIA_PARCEL_NAME, media)

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

		// Put the audio attributes  in the bundle
		bundle.putInt(TextToSpeech.Engine.KEY_PARAM_STREAM, attrs.stream)

		return bundle
	}

}
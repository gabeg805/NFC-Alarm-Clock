package com.nfcalarmclock.util;

import android.os.Build;
import android.os.Bundle;
import android.os.BadParcelableException;
import android.speech.tts.TextToSpeech;

import com.nfcalarmclock.alarm.db.NacAlarm;
import com.nfcalarmclock.media.NacAudioAttributes;

/**
 */
public class NacBundle
{

	/**
	 * Tag name associated with a parceled alarm.
	 */
	public static final String ALARM_PARCEL_NAME = "NacAlarmParcel";

	/**
	 * Tag name associated with a media path.
	 */
	public static final String MEDIA_PARCEL_NAME = "NacMediaParcel";

	/**
	 * @return The alarm contained in the bundle.
	 */
	@SuppressWarnings("deprecation")
	public static NacAlarm getAlarm(Bundle bundle)
	{
		if (bundle != null)
		{
			try
			{
				bundle.setClassLoader(NacAlarm.class.getClassLoader());

				// Use the updated form of Bundle.getParcelable() for API >= 33
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
				{
					return bundle.getParcelable(ALARM_PARCEL_NAME, NacAlarm.class);
				}
				// Use the deprecated form of Bundle.getParcelable() for API < 33
				else
				{
					return bundle.getParcelable(ALARM_PARCEL_NAME);
				}
			}
			catch (BadParcelableException bpe)
			{
				try
				{
					bundle.setClassLoader(NacAlarm.Builder.class.getClassLoader());

					// Use the updated form of Bundle.getParcelable() for API >= 33
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
					{
						return bundle.getParcelable(ALARM_PARCEL_NAME, NacAlarm.class);
					}
					// Use the deprecated form of Bundle.getParcelable() for API < 33
					else
					{
						return bundle.getParcelable(ALARM_PARCEL_NAME);
					}
				}
				catch (RuntimeException re)
				{
					return null;
				}
			}
		}
		else
		{
			return null;
		}
	}

	/**
	 * @return The sound contained in the bundle.
	 */
	public static String getMedia(Bundle bundle)
	{
		return (bundle != null) ? bundle.getString(MEDIA_PARCEL_NAME) : null;
	}

	/**
	 * @return A bundle that contains the alarm.
	 */
	public static Bundle toBundle(NacAlarm alarm)
	{
		Bundle bundle = new Bundle();

		bundle.putParcelable(ALARM_PARCEL_NAME, alarm);
		return bundle;
	}

	/**
	 * @return A bundle that contains the sound.
	 */
	public static Bundle toBundle(String media)
	{
		Bundle bundle = new Bundle();

		bundle.putString(MEDIA_PARCEL_NAME, media);
		return bundle;
	}

	/**
	 * @return A bundle that contains the parameter to control the volume level
	 *         of a Text-to-Speech engine.
	 */
	public static Bundle toBundle(NacAudioAttributes attrs)
	{
		Bundle bundle = new Bundle();

		bundle.putInt(TextToSpeech.Engine.KEY_PARAM_STREAM, attrs.getStream());
		return bundle;
	}

}

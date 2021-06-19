package com.nfcalarmclock.system;

import android.os.Bundle;
import android.os.BadParcelableException;
import android.speech.tts.TextToSpeech;

import com.nfcalarmclock.alarm.NacAlarm;
import com.nfcalarmclock.audio.NacAudio;

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
	public static NacAlarm getAlarm(Bundle bundle)
	{
		if (bundle != null)
		{
			try
			{
				bundle.setClassLoader(NacAlarm.class.getClassLoader());
				return (NacAlarm) bundle.getParcelable(ALARM_PARCEL_NAME);
			}
			catch (BadParcelableException e)
			{
				bundle.setClassLoader(NacAlarm.Builder.class.getClassLoader());
				return (NacAlarm) bundle.getParcelable(ALARM_PARCEL_NAME);
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
	public static Bundle toBundle(NacAudio.Attributes attrs)
	{
		Bundle bundle = new Bundle();
		bundle.putInt(TextToSpeech.Engine.KEY_PARAM_STREAM, attrs.getStream());
		return bundle;

		//float value = attrs.getVolumeLevel() / 100.0f;
		//bundle.putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, value);
	}

}

package com.nfcalarmclock;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;

/**
 */
public class NacBundle
{

	/**
	 * The tag name associated with a parceled alarm.
	 */
	public static final String ALARM_PARCEL_NAME = "NacAlarmParcel";

	/**
	 * The tag name associated with a parceled sound.
	 */
	public static final String SOUND_PARCEL_NAME = "NacSoundParcel";

	/**
	 * @return The alarm contained in the bundle.
	 */
	public static NacAlarm getAlarm(Bundle bundle)
	{
		return (bundle != null)
			? (NacAlarm) bundle.getParcelable(ALARM_PARCEL_NAME) : null;
	}

	/**
	 * @return The sound contained in the bundle.
	 */
	public static NacSound getSound(Bundle bundle)
	{
		return (bundle != null)
			? (NacSound) bundle.getParcelable(SOUND_PARCEL_NAME) : null;
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
	public static Bundle toBundle(NacSound sound)
	{
		Bundle bundle = new Bundle();

		bundle.putParcelable(SOUND_PARCEL_NAME, sound);

		return bundle;
	}

	/**
	 * @return A bundle that contains the parameter to control the volume level
	 *         of a Text-to-Speech engine.
	 */
	public static Bundle toBundle(NacAudio.Attributes attrs)
	{
		Bundle bundle = new Bundle();

		bundle.putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME,
			attrs.getVolumeLevel());

		return bundle;
	}

}

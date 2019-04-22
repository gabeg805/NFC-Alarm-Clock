package com.nfcalarmclock;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

/**
 */
public class NacIntent
{

	/**
	 * Tag name for retrieving a NacAlarm from a bundle.
	 */
	public static final String ALARM_BUNDLE_NAME = "NacAlarmBundle";

	/**
	 * Tag name for retrieving a NacSound from a bundle.
	 */
	public static final String SOUND_BUNDLE_NAME = "NacSoundBundle";

	/**
	 * @return The intent that will be used to start the Alarm activity.
	 */
	public static Intent createAlarmActivity(Context context, Bundle bundle)
	{
		Intent intent = new Intent(context, NacAlarmActivity.class);

		intent.putExtra(ALARM_BUNDLE_NAME, bundle);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		return intent;
	}

	/**
	 * @return The intent that will be used when starting the service for
	 *         excecuting schedule and database updates.
	 */
	public static Intent createService(Context context, String message,
		NacAlarm alarm)
	{
		if (alarm == null)
		{
			return null;
		}

		Intent intent = new Intent(context, NacService.class);
		Bundle bundle = NacBundle.toBundle(alarm);
		Uri uri = Uri.parse(message);

		intent.putExtra(ALARM_BUNDLE_NAME, bundle);
		intent.setData(uri);

		return intent;
	}

	/**
	 * @return The intent that will be used when starting the service for
	 *         excecuting schedule and database updates.
	 */
	public static Intent createService(Context context, String message,
		NacSound sound)
	{
		if (sound == null)
		{
			return null;
		}

		Intent intent = new Intent(context, NacService.class);
		Bundle bundle = NacBundle.toBundle(sound);
		Uri uri = Uri.parse(message);

		intent.putExtra(SOUND_BUNDLE_NAME, bundle);
		intent.setData(uri);

		return intent;
	}

	/**
	 * @return The alarm associated with the given Intent.
	 */
	public static NacAlarm getAlarm(Intent intent)
	{
		if (intent == null)
		{
			return null;
		}

		Bundle bundle = NacIntent.getAlarmBundle(intent);

		return NacBundle.getAlarm(bundle);
	}

	/**
	 * @return The alarm bundle.
	 */
	public static Bundle getAlarmBundle(Intent intent)
	{
		return NacIntent.getBundle(intent, ALARM_BUNDLE_NAME);
	}

	/**
	 * @return The sound associated with the given intent.
	 */
	public static NacSound getSound(Intent intent)
	{
		if (intent == null)
		{
			return null;
		}

		Bundle bundle = NacIntent.getSoundBundle(intent);

		return NacBundle.getSound(bundle);
	}

	/**
	 * @return The sound bundle.
	 */
	public static Bundle getSoundBundle(Intent intent)
	{
		return NacIntent.getBundle(intent, SOUND_BUNDLE_NAME);
	}

	/**
	 * @return The extra data bundle that is part of the intent.
	 */
	public static Bundle getBundle(Intent intent, String name)
	{
		if (intent == null)
		{
			return null;
		}
		else if (intent.hasExtra(name))
		{
			return intent.getBundleExtra(name);
		}
		else
		{
			return null;
		}
	}

	/**
	 * @return An intent with an alarm.
	 */
	public static Intent toIntent(Context packageContext, Class<?> cls,
		NacAlarm alarm)
	{
		Intent intent = new Intent(packageContext, cls);
		Bundle bundle = NacBundle.toBundle(alarm);

		intent.putExtra(ALARM_BUNDLE_NAME, bundle);

		return intent;
	}

	/**
	 * @return An intent with a sound.
	 */
	public static Intent toIntent(Context packageContext, Class<?> cls,
		NacSound sound)
	{
		Intent intent = new Intent(packageContext, cls);
		Bundle bundle = NacBundle.toBundle(sound);

		intent.putExtra(SOUND_BUNDLE_NAME, bundle);

		return intent;
	}

}

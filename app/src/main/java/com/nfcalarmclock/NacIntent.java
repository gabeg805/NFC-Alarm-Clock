package com.nfcalarmclock;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.AlarmClock;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.EnumSet;
import java.util.List;

/**
 */
public class NacIntent
{

	/**
	 * Tag name for retrieving a NacAlarm from a bundle.
	 */
	public static final String ALARM_BUNDLE_NAME = "NacAlarmBundle";

	/**
	 * Tag name for retrieving the "From" NacAlarm from a bundle.
	 */
	public static final String ALARM_FROM_BUNDLE_NAME = "NacAlarmFromBundle";

	/**
	 * Tag name for retrieving the "To" NacAlarm from a bundle.
	 */
	public static final String ALARM_TO_BUNDLE_NAME = "NacAlarmToBundle";

	/**
	 * Tag name for retrieving a NacSound from a bundle.
	 */
	public static final String SOUND_BUNDLE_NAME = "NacSoundBundle";

	/**
	 * Add an alarm to an intent.
	 */
	public static Intent addAlarm(Intent intent, Bundle bundle)
	{
		if (intent != null)
		{
			intent.putExtra(ALARM_BUNDLE_NAME, bundle);
		}

		return intent;
	}

	/**
	 * Add an alarm to an intent.
	 */
	public static Intent addAlarm(Intent intent, NacAlarm alarm)
	{
		return NacIntent.addAlarm(intent, NacBundle.toBundle(alarm));
	}

	/**
	 * @return The intent that will be used to start the Alarm activity.
	 */
	public static Intent createAlarmActivity(Context context, NacAlarm alarm)
	{
		Bundle bundle = NacBundle.toBundle(alarm);

		return NacIntent.createAlarmActivity(context, bundle);
	}

	/**
	 * @return The intent that will be used to start the Alarm activity.
	 */
	public static Intent createAlarmActivity(Context context, Bundle bundle)
	{
		Intent intent = new Intent(context, NacAlarmActivity.class);
		int flags = Intent.FLAG_ACTIVITY_NEW_TASK
			| Intent.FLAG_ACTIVITY_CLEAR_TASK;

		intent.addFlags(flags);
		intent.putExtra(ALARM_BUNDLE_NAME, bundle);

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

		Intent intent = new Intent(context,
			NacDatabase.BackgroundService.class);
		Bundle bundle = NacBundle.toBundle(alarm);
		Uri uri = Uri.parse(message);

		intent.putExtra(ALARM_BUNDLE_NAME, bundle);
		intent.setData(uri);

		return intent;
	}

	/**
	 * @return An intent to swap two alarms.
	 */
	public static Intent createService(Context context, String message,
		NacAlarm fromAlarm, NacAlarm toAlarm)
	{
		if ((fromAlarm == null) || (toAlarm == null))
		{
			return null;
		}

		Intent intent = new Intent(context,
			NacDatabase.BackgroundService.class);
		Bundle fromBundle = NacBundle.toBundle(fromAlarm);
		Bundle toBundle = NacBundle.toBundle(toAlarm);
		Uri uri = Uri.parse(message);

		intent.putExtra(ALARM_FROM_BUNDLE_NAME, fromBundle);
		intent.putExtra(ALARM_TO_BUNDLE_NAME, toBundle);
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

		Intent intent = new Intent(context,
			NacDatabase.BackgroundService.class);
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
	 * @return The alarms that are associated with the given intent.
	 */
	public static NacAlarm[] getAlarms(Intent intent)
	{
		if (intent == null)
		{
			return null;
		}

		Bundle fromBundle = NacIntent.getAlarmFromBundle(intent);
		Bundle toBundle = NacIntent.getAlarmToBundle(intent);
		NacAlarm[] alarms = new NacAlarm[2];
		alarms[0] = NacBundle.getAlarm(fromBundle);
		alarms[1] = NacBundle.getAlarm(toBundle);

		return alarms;
	}

	/**
	 * @see getBundle
	 */
	public static Bundle getAlarmBundle(Intent intent)
	{
		return NacIntent.getBundle(intent, ALARM_BUNDLE_NAME);
	}

	/**
	 * @see getBundle
	 */
	public static Bundle getAlarmFromBundle(Intent intent)
	{
		return NacIntent.getBundle(intent, ALARM_FROM_BUNDLE_NAME);
	}

	/**
	 * @see getBundle
	 */
	public static Bundle getAlarmToBundle(Intent intent)
	{
		return NacIntent.getBundle(intent, ALARM_TO_BUNDLE_NAME);
	}

	/**
	 * @return The extra data bundle that is part of the intent.
	 */
	public static Bundle getBundle(Intent intent, String name)
	{
		return (intent != null) ? intent.getBundleExtra(name) : null;
	}

	/**
	 * @return The alarm that was specified using the SET_ALARM action.
	 */
	public static NacAlarm getSetAlarm(Context context, Intent intent)
	{
		if (!NacIntent.isSetAlarmAction(intent))
		{
			return null;
		}

		NacAlarm.Builder builder = new NacAlarm.Builder();
		Calendar calendar = Calendar.getInstance();
		boolean isSet = false;

		if (intent.hasExtra(AlarmClock.EXTRA_HOUR))
		{
			int hour = intent.getIntExtra(AlarmClock.EXTRA_HOUR,
				calendar.get(Calendar.HOUR_OF_DAY));
			isSet = true;

			builder.setHour(hour);
		}

		if (intent.hasExtra(AlarmClock.EXTRA_MINUTES))
		{
			int minute = intent.getIntExtra(AlarmClock.EXTRA_MINUTES,
				calendar.get(Calendar.MINUTE));
			isSet = true;

			builder.setMinute(minute);
		}

		if (intent.hasExtra(AlarmClock.EXTRA_MESSAGE))
		{
			String name = intent.getStringExtra(AlarmClock.EXTRA_MESSAGE);
			isSet = true;

			builder.setName(name);
		}

		if (intent.hasExtra(AlarmClock.EXTRA_DAYS))
		{
			ArrayList<Integer> extraDays =
				intent.getIntegerArrayListExtra(AlarmClock.EXTRA_DAYS);
			EnumSet<NacCalendar.Day> days =
				EnumSet.noneOf(NacCalendar.Day.class);
			isSet = true;

			for (int d : extraDays)
			{
				days.add(NacCalendar.Days.toWeekDay(d));
			}

			builder.setDays(days);
		}

		if (intent.hasExtra(AlarmClock.EXTRA_RINGTONE))
		{
			String ringtone = intent.getStringExtra(AlarmClock.EXTRA_RINGTONE);
			isSet = true;

			builder.setSound(context, ringtone);
		}

		if (intent.hasExtra(AlarmClock.EXTRA_VIBRATE))
		{
			boolean vibrate = intent.getBooleanExtra(AlarmClock.EXTRA_VIBRATE,
				NacSharedPreferences.DEFAULT_VIBRATE);
			isSet = true;

			builder.setVibrate(vibrate);
		}

		//getBooleanExtra(AlarmClock.EXTRA_SKIP_UI);

		return (isSet) ? builder.build() : null;
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
	 * @return True if the intent was called from the SET_ALARM action, and
	 *         False otherwise.
	 */
	public static boolean isSetAlarmAction(Intent intent)
	{
		if (intent == null)
		{
			return false;
		}

		String action = intent.getAction();

		return (action != null) ? action.equals(AlarmClock.ACTION_SET_ALARM)
			: false;
	}

	/**
	 * @return An intent with a sound.
	 */
	public static Intent toIntent(NacAlarm alarm)
	{
		return NacIntent.toIntent(null, null, alarm);
	}

	/**
	 * @return An intent with a sound.
	 */
	public static Intent toIntent(NacSound sound)
	{
		return NacIntent.toIntent(null, null, sound);
	}

	/**
	 * @see toIntent
	 */
	public static Intent toIntent(Context packageContext, Class<?> cls,
		NacAlarm alarm)
	{
		Intent intent = (cls != null) ? new Intent(packageContext, cls)
			: new Intent();
		Bundle bundle = NacBundle.toBundle(alarm);

		intent.putExtra(ALARM_BUNDLE_NAME, bundle);

		return intent;
	}

	/**
	 * @see toIntent
	 */
	public static Intent toIntent(Context packageContext, Class<?> cls,
		NacSound sound)
	{
		Intent intent = (cls != null) ? new Intent(packageContext, cls)
			: new Intent();
		Bundle bundle = NacBundle.toBundle(sound);

		intent.putExtra(SOUND_BUNDLE_NAME, bundle);

		return intent;
	}

}

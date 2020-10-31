package com.nfcalarmclock;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.AlarmClock;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.EnumSet;

/**
 */
@SuppressWarnings("RedundantSuppression")
public class NacIntent
{

	/**
	 * Tag name for retrieving a NacAlarm from a bundle.
	 */
	public static final String ALARM_BUNDLE_NAME = "NacAlarmBundle";

	/**
	 * Tag name for retrieving a media path from a bundle.
	 */
	public static final String MEDIA_BUNDLE_NAME = "NacMediaBundle";

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
		return NacIntent.addAlarm(intent, bundle);
	}

	/**
	 * @return The intent that will be used to start the foreground alarm service.
	 */
	@SuppressWarnings("unused")
	public static Intent createForegroundService(Context context, NacAlarm alarm)
	{
		Bundle bundle = NacBundle.toBundle(alarm);
		return NacIntent.createForegroundService(context, bundle);
	}

	/**
	 * @return The intent that will be used to start the foreground alarm service.
	 */
	public static Intent createForegroundService(Context context, Bundle bundle)
	{
		Intent intent = new Intent(NacForegroundService.ACTION_START_SERVICE, null,
			context, NacForegroundService.class);
		return NacIntent.addAlarm(intent, bundle);
	}

	/**
	 * @return An intent that will be used to dismiss the alarm activity.
	 */
	public static Intent dismissAlarmActivity(Context context, NacAlarm alarm)
	{
		Intent intent = NacIntent.createAlarmActivity(context, alarm);
		intent.setAction(NacAlarmActivity.ACTION_DISMISS_ACTIVITY);
		return intent;
	}

	/**
	 * @return An intent that will be used to dismiss the foreground alarm service.
	 */
	public static Intent dismissForegroundService(Context context, NacAlarm alarm)
	{
		Intent intent = new Intent(NacForegroundService.ACTION_DISMISS_ALARM, null,
			context, NacForegroundService.class);
		return NacIntent.addAlarm(intent, alarm);
	}

	/**
	 * @return The intent action (never null).
	 */
	public static String getAction(Intent intent)
	{
		String action = (intent != null) ? intent.getAction() : null;
		return (action != null) ? action : "";
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
	 * @see #getBundle(Intent, String)
	 */
	public static Bundle getAlarmBundle(Intent intent)
	{
		return NacIntent.getBundle(intent, ALARM_BUNDLE_NAME);
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

		NacSharedDefaults defaults = new NacSharedDefaults(context);
		NacAlarm.Builder builder = new NacAlarm.Builder(context);
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

			builder.setMedia(context, ringtone);
		}

		if (intent.hasExtra(AlarmClock.EXTRA_VIBRATE))
		{
			boolean vibrate = intent.getBooleanExtra(AlarmClock.EXTRA_VIBRATE,
				defaults.getVibrate());
			isSet = true;

			builder.setVibrate(vibrate);
		}

		//getBooleanExtra(AlarmClock.EXTRA_SKIP_UI);

		return (isSet) ? builder.build() : null;
	}

	/**
	 * @return The sound associated with the given intent.
	 */
	public static String getMedia(Intent intent)
	{
		if (intent == null)
		{
			return null;
		}

		Bundle bundle = NacIntent.getMediaBundle(intent);

		return NacBundle.getMedia(bundle);
	}

	/**
	 * @return The sound bundle.
	 */
	public static Bundle getMediaBundle(Intent intent)
	{
		return NacIntent.getBundle(intent, MEDIA_BUNDLE_NAME);
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
		return (action != null) && action.equals(AlarmClock.ACTION_SET_ALARM);
	}

	/**
	 * @return An intent that will be used to snooze the foreground alarm service.
	 */
	public static Intent snoozeForegroundService(Context context, NacAlarm alarm)
	{
		Intent intent = new Intent(NacForegroundService.ACTION_SNOOZE_ALARM, null,
			context, NacForegroundService.class);
		return NacIntent.addAlarm(intent, alarm);
	}

	/**
	 * @return An intent that allows you to stop the alarm activity.
	 */
	public static Intent stopAlarmActivity(NacAlarm alarm)
	{
		Intent intent = new Intent(NacAlarmActivity.ACTION_STOP_ACTIVITY);
		return NacIntent.addAlarm(intent, alarm);
	}

	/**
	 * @return An intent that will be used to stop the foreground alarm service.
	 */
	@SuppressWarnings("unused")
	public static Intent stopForegroundService(Context context, NacAlarm alarm)
	{
		Intent intent = new Intent(NacForegroundService.ACTION_STOP_SERVICE, null,
			context, NacForegroundService.class);
		return NacIntent.addAlarm(intent, alarm);
	}

	/**
	 * @return An intent with a sound.
	 */
	public static Intent toIntent(String media)
	{
		return NacIntent.toIntent(null, null, media);
	}

	/**
	 * Create an intent with an alarm attached in the Extra part of the intent.
	 *
	 * @return An intent.
	 *
	 * @param  context  The application context.
	 * @param  cls      The name of the class for the intent to run.
	 * @param  alarm    The alarm to attach to the intent.
	 */
	public static Intent toIntent(Context context, Class<?> cls, NacAlarm alarm)
	{
		Intent intent = (cls != null) ? new Intent(context, cls)
			: new Intent();
		Bundle bundle = NacBundle.toBundle(alarm);

		intent.putExtra(ALARM_BUNDLE_NAME, bundle);
		return intent;
	}

	/**
	 * Create an intent with a media path attached in the Extra part of the
	 * intent.
	 *
	 * @return An intent.
	 *
	 * @param  context  The application context.
	 * @param  cls      The name of the class for the intent to run.
	 * @param  media    The media path to attach to the intent.
	 */
	public static Intent toIntent(Context context, Class<?> cls, String media)
	{
		Intent intent = (cls != null) ? new Intent(context, cls)
			: new Intent();
		Bundle bundle = NacBundle.toBundle(media);

		intent.putExtra(MEDIA_BUNDLE_NAME, bundle);
		return intent;
	}

}

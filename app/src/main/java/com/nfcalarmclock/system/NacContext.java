package com.nfcalarmclock.system;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import com.nfcalarmclock.alarm.NacAlarm;
import com.nfcalarmclock.util.NacUtility;
import com.nfcalarmclock.nfc.NacNfc;
import com.nfcalarmclock.nfc.NacNfcTag;
import com.nfcalarmclock.shared.NacSharedConstants;

/**
 * Context.
 */
public class NacContext
{

	/**
	 * Dismiss the alarm activity for the given alarm.
	 *
	 * If alarm is null, it will stop the currently active alarm activity.
	 */
	public static void dismissAlarmActivity(Context context, NacAlarm alarm)
	{
		Intent intent = NacIntent.dismissAlarmActivity(context, alarm);

		context.startActivity(intent);
	}

	/**
	 * Dismiss the alarm activity for the given alarm due with NFC.
	 *
	 * If alarm is null, it will stop the currently active alarm activity.
	 */
	public static void dismissAlarmActivityWithNfc(Context context, NacNfcTag tag)
	{
		Intent intent = NacIntent.dismissAlarmActivityWithNfc(context, tag);

		context.startActivity(intent);
	}

	/**
	 * Dismiss the foreground service for the given alarm.
	 *
	 * If alarm is null, it will stop the currently active foreground service.
	 */
	public static void dismissForegroundService(Context context, NacAlarm alarm)
	{
		Intent intent = NacIntent.dismissForegroundService(context, alarm);

		context.startService(intent);
	}

	/**
	 * Dismiss the foreground service for the given alarm with NFC.
	 *
	 * If alarm is null, it will stop the currently active foreground service.
	 */
	public static void dismissForegroundServiceWithNfc(Context context, NacAlarm alarm)
	{
		Intent intent = NacIntent.dismissForegroundServiceWithNfc(context, alarm);

		context.startService(intent);
	}

	/**
	 * Snooze the foreground service for the given alarm.
	 *
	 * The alarm cannot be null, unlike the dismissForegroundService() method.
	 */
	public static void snoozeForegroundService(Context context, NacAlarm alarm)
	{
		Intent intent = NacIntent.snoozeForegroundService(context, alarm);

		context.startService(intent);
	}

	/**
	 * Stop the alarm activity for the given alarm.
	 *
	 * If alarm is null, it will stop the currently active alarm activity.
	 */
	public static void stopAlarmActivity(Context context, NacAlarm alarm)
	{
		Intent intent = NacIntent.stopAlarmActivity(alarm);

		context.sendBroadcast(intent);
	}

	/**
	 * Start the running the alarm activity and service.
	 *
	 * @param  context  A context.
	 * @param  bundle  A bundle, typically with an alarm inside.
	 */
	public static void startAlarm(Context context, Bundle bundle)
	{
		if (bundle == null)
		{
			return;
		}

		Intent activityIntent = NacIntent.createAlarmActivity(context, bundle);
		Intent serviceIntent = NacIntent.createForegroundService(context, bundle);

		context.startActivity(activityIntent);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
		{
			context.startForegroundService(serviceIntent);
		}
		else
		{
			context.startService(serviceIntent);
		}
	}

	/**
	 * @see NacContext#startAlarm(Context, Bundle)
	 */
	public static void startAlarm(Context context, NacAlarm alarm)
	{
		if (alarm == null)
		{
			return;
		}

		Bundle bundle = NacBundle.toBundle(alarm);

		NacContext.startAlarm(context, bundle);
	}

	/**
	 * Start the main activity.
	 *
	 * @param  context  A context.
	 */
	public static void startMainActivity(Context context)
	{
		Intent intent = NacIntent.createMainActivity(context);

		context.startActivity(intent);
	}

}

package com.nfcalarmclock;

import android.content.Context;
import android.content.Intent;

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
	 * Dismiss the foreground service for the given alarm due to an NFC tag being
	 * scanned.
	 *
	 * @return True if decided to dismiss the foreground service, and False if
	 *         unable to due to null values or NFC tag IDs not matching.
	 */
	public static boolean dismissForegroundServiceFromNfcScan(Context context,
		Intent intent, NacAlarm alarm)
	{
		if ((intent == null) || (alarm == null))
		{
			return false;
		}

		if (NacNfc.doIdsMatch(alarm, intent))
		{
			NacContext.dismissForegroundService(context, alarm);
			return true;
		}
		else
		{
			NacSharedConstants cons = new NacSharedConstants(context);
			NacUtility.quickToast(context, cons.getErrorMessageNfcMismatch());
			return false;
		}
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

}

package com.nfcalarmclock;

import android.content.Context;
import android.content.Intent;
import android.service.notification.StatusBarNotification;

/**
 * Context.
 */
public class NacContext
{

	/**
	 * Stop the currently active alarm.
	 */
	public static void stopActiveAlarm(Context context)
	{
		StatusBarNotification notification = NacNotificationHelper
			.getActiveNotification(context);
		if (notification == null)
		{
			return;
		}

		NacAlarm alarm = NacNotificationHelper.findAlarm(context, notification);
		if (alarm == null)
		{
			return;
		}

		NacContext.stopForegroundService(context, alarm);
	}

	/**
	 * Stop the alarm activity for the given alarm.
	 *
	 * If alarm is null, it will stop the currently active alarm activity.
	 */
	public static void stopAlarmActivity(Context context, NacAlarm alarm)
	{
		Intent intent = NacIntent.stopAlarmActivity(context, alarm);
		context.sendBroadcast(intent);
	}

	/**
	 * Stop the foreground service for the given alarm.
	 *
	 * If alarm is null, it will stop the currently active foreground service.
	 */
	public static void stopForegroundService(Context context, NacAlarm alarm)
	{
		Intent intent = NacIntent.stopForegroundService(context, alarm);
		context.startService(intent);
	}

}

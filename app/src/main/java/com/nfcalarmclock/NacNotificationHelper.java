package com.nfcalarmclock;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.service.notification.StatusBarNotification;
import androidx.core.app.NotificationCompat;
import java.util.ArrayList;
import java.util.List;

/**
 * Notification helper.
 */
public class NacNotificationHelper
{

	/**
	 * @return True if the given groups match, and False otherwise.
	 */
	public static boolean doGroupsMatch(String group1, String group2)
	{
		return (group1 != null) && (group2 != null) && group1.equals(group2);
	}

	/**
	 * @see findAlarm
	 */
	public static NacAlarm findAlarm(Context context)
	{
		StatusBarNotification notification = NacNotificationHelper
			.getActiveNotification(context);
		return NacNotificationHelper.findAlarm(context, notification);
	}

	/**
	 * @return NacAlarm that is found by using the information in the currently
	 *         active notification
	 */
	public static NacAlarm findAlarm(Context context,
		StatusBarNotification notification)
	{
		if (notification == null)
		{
			return null;
		}

		int id = notification.getId();
		return NacDatabase.findAlarm(context, id);
	}

	/**
	 * @return The notification for the current active alarm.
	 */
	@TargetApi(Build.VERSION_CODES.M)
	public static StatusBarNotification getActiveNotification(Context context)
	{
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
		{
			return null;
		}

		NotificationManager manager = context.getSystemService(
			NotificationManager.class);
		StatusBarNotification[] statusbar = manager.getActiveNotifications();
		StatusBarNotification activeNotification = null;

		for (StatusBarNotification sb : statusbar)
		{
			Notification notification = sb.getNotification();
			String group = notification.getGroup();
			long posted = sb.getPostTime();

			//if (!group.equals(NacActiveAlarmNotification.GROUP))
			if (!NacActiveAlarmNotification.isActiveGroup(group))
			{
				continue;
			}

			if ((activeNotification == null)
				|| (activeNotification.getPostTime() > posted))
			{
				activeNotification = sb;
				continue;
			}
		}

		return activeNotification;
	}

	/**
	 * @return A list of notification lines.
	 */
	@TargetApi(Build.VERSION_CODES.M)
	public static List<CharSequence> getExtraLines(Context context, String groupKey)
	{
		List<CharSequence> lines = new ArrayList<>();
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
		{
			return lines;
		}

		NotificationManager manager = (NotificationManager)
			context.getSystemService(Context.NOTIFICATION_SERVICE);
		StatusBarNotification[] statusbar = manager
			.getActiveNotifications();

		for (StatusBarNotification sb : statusbar)
		{
			Notification notification = sb.getNotification();
			String sbGroup = notification.getGroup();

			if (NacNotificationHelper.doGroupsMatch(groupKey, sbGroup))
			{
				CharSequence[] extraLines = (CharSequence[]) notification.extras
					.get(NotificationCompat.EXTRA_TEXT_LINES);
				for (CharSequence line : extraLines)
				{
					lines.add(line);
				}
			}
		}

		return lines;
	}

}

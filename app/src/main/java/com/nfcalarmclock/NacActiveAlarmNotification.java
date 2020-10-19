package com.nfcalarmclock;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import java.lang.Float;
import java.util.Calendar;
import java.util.Locale;

/**
 * Notification to display for active alarms.
 */
public class NacActiveAlarmNotification
	extends NacNotification
{

	/**
	 * Group key.
	 */
	public static final String GROUP = "NacNotiGroupActiveAlarm";

	/**
	 * Alarm.
	 */
	private NacAlarm mAlarm;

	/**
	 */
	public NacActiveAlarmNotification()
	{
		super();
		this.mAlarm = null;
	}

	/**
	 */
	public NacActiveAlarmNotification(Context context)
	{
		super(context);
		this.mAlarm = null;
	}

	/**
	 * @see builder
	 */
	@Override
	protected NotificationCompat.Builder builder()
	{
		Context context = this.getContext();
		Bitmap icon = this.getIcon();
		PendingIntent activityPending = this.getContentPendingIntent();
		PendingIntent snoozePending = this.getSnoozePendingIntent();
		PendingIntent dismissPending = this.getDismissPendingIntent(activityPending);
		NacSharedConstants cons = new NacSharedConstants(context);

		return super.builder()
			.setLargeIcon(icon)
			.setFullScreenIntent(activityPending, true)
			.setAutoCancel(false)
			.setOngoing(true)
			.setShowWhen(true)
			.setTicker(cons.getAppName())
			.addAction(R.mipmap.snooze, cons.getActionSnooze(), snoozePending)
			.addAction(R.mipmap.dismiss, cons.getActionDismiss(), dismissPending);
	}

	/**
	 */
	@TargetApi(Build.VERSION_CODES.O)
	@Override
	protected NotificationChannel createChannel()
	{
		NotificationChannel channel = super.createChannel();
		if (channel == null)
		{
			return null;
		}

		channel.setShowBadge(true);
		channel.enableLights(true);
		channel.enableVibration(true);
		return channel;
	}

	/**
	 * @return The alarm.
	 */
	public NacAlarm getAlarm()
	{
		return this.mAlarm;
	}

	/**
	 * @return The notification description.
	 */
	protected String getChannelDescription()
	{
		Context context = this.getContext();
		NacSharedConstants cons = new NacSharedConstants(context);
		return cons.getDescriptionActiveNotification();
	}

	/**
	 * @return The notification name.
	 */
	protected String getChannelName()
	{
		Context context = this.getContext();
		NacSharedConstants cons = new NacSharedConstants(context);
		return cons.getActiveNotification();
	}

	/**
	 * @see getChannel
	 */
	protected String getChannelId()
	{
		return "NacNotiChannelActiveAlarm";
	}

	/**
	 * @see getContentPendingIntent
	 */
	protected PendingIntent getContentPendingIntent()
	{
		Context context = this.getContext();
		NacAlarm alarm = this.getAlarm();
		int id = (alarm == null) ? 0 : alarm.getId();
		Intent intent = NacIntent.createAlarmActivity(context, alarm);

		return PendingIntent.getActivity(context, id, intent,
			PendingIntent.FLAG_UPDATE_CURRENT);
	}

	/**
	 * @see getContentText
	 */
	public String getContentText()
	{
		Context context = this.getContext();
		NacAlarm alarm = this.getAlarm();
		Calendar now = Calendar.getInstance();
		String time = NacCalendar.Time.getFullTime(context, now);
		String name = alarm.getName();
		Locale locale = Locale.getDefault();

		return name.isEmpty() ? time
			: String.format(locale, "%1$s  —  %2$s", time, name);
	}

	/**
	 * @return The pending intent to use when dismissing the alarm.
	 */
	private PendingIntent getDismissPendingIntent(
		PendingIntent activityPendingIntent)
	{
		Context context = this.getContext();
		NacAlarm alarm = this.getAlarm();

		if (NacNfc.shouldUseNfc(context, alarm))
		{
			return activityPendingIntent;
		}

		Intent intent = NacIntent.dismissForegroundService(context, alarm);
		return PendingIntent.getService(context, 0, intent, 0);
	}

	/**
	 * @see getGroup
	 */
	protected String getGroup()
	{
		return GROUP;
	}

	/**
	 * @return The notification large icon.
	 */
	protected Bitmap getIcon()
	{
		Context context = this.getContext();
		Resources res = context.getResources();
		Bitmap icon = BitmapFactory.decodeResource(res, R.mipmap.app);
		float density = res.getDisplayMetrics().density;
		float size = 0f;

		if (Float.compare(density, 4.0f) >= 0)
		{
			size = 256f;
		}
		else if (Float.compare(density, 3.0f) >= 0)
		{
			size = 192f;
		}
		else if (Float.compare(density, 2.0f) >= 0)
		{
			size = 128f;
		}
		else if (Float.compare(density, 1.5f) >= 0)
		{
			size = 96f;
		}
		else if (Float.compare(density, 1.0f) >= 0)
		{
			size = 64f;
		}
		else if (Float.compare(density, 0.75f) >= 0)
		{
			size = 48f;
		}
		else
		{
			return icon;
		}

		return Bitmap.createScaledBitmap(icon, (int)(size*density),
			(int)(size*density), true);
	}

	/**
	 * @see getId
	 */
	protected int getId()
	{
		return 79;
	}

	/**
	 * @see getImportance
	 */
	protected int getImportance()
	{
		return NotificationManagerCompat.IMPORTANCE_HIGH;
	}

	/**
	 * @see getPriority
	 */
	protected int getPriority()
	{
		return NotificationCompat.PRIORITY_MAX;
	}

	/**
	 * @return The notification title.
	 */
	public String getTitle()
	{
		Context context = this.getContext();
		Locale locale = Locale.getDefault();
		NacSharedConstants cons = new NacSharedConstants(context);

		return String.format(locale, "<b>%s</b>", cons.getAppName());
	}

	/**
	 * @return The pending intent to use when snoozing.
	 */
	private PendingIntent getSnoozePendingIntent()
	{
		Context context = this.getContext();
		Intent intent = new Intent(NacForegroundService.ACTION_SNOOZE_ALARM,
			null, context, NacForegroundService.class);

		return PendingIntent.getService(context, 0, intent, 0);
	}

	/**
	 * @return True if the given group matches this notification group.
	 */
	public static boolean isActiveGroup(String groupKey)
	{
		return GROUP.equals(groupKey);
	}

	/**
	 * Set the alarm.
	 */
	public void setAlarm(NacAlarm alarm)
	{
		this.mAlarm = alarm;
	}

}

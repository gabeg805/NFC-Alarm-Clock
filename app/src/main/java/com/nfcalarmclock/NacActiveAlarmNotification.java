package com.nfcalarmclock;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.text.Html;
import android.text.Spanned;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import java.lang.Float;
import java.util.Calendar;

/**
 * Notification to display for active alarms.
 */
public class NacActiveAlarmNotification
{

	/**
	 * ID.
	 */
	public static final int ID = 79;

	/**
	 * Channel.
	 */
	public static final String CHANNEL = "NacNotiChannelActiveAlarm";

	/**
	 * Group.
	 */
	public static final String GROUP = "NacNotiGroupActiveAlarm";

	/**
	 * Title of this type of notification.
	 */
	public static final String TITLE = "NFC Alarm Clock";

	/**
	 * Context.
	 */
	private Context mContext;

	/**
	 * Alarm.
	 */
	private NacAlarm mAlarm;

	/**
	 */
	public NacActiveAlarmNotification()
	{
		this.mContext = null;
		this.mAlarm = null;
	}

	/**
	 */
	public NacActiveAlarmNotification(Context context, NacAlarm alarm)
	{
		this.mContext = context;
		this.mAlarm = alarm;

		this.createChannel();
	}

	/**
	 * Build the notification.
	 */
	public Notification build()
	{
		Context context = this.getContext();
		Spanned title = this.getTitle();
		String text = this.getText();
		Bitmap icon = this.getIcon();
		PendingIntent activityPending = this.getActivityPendingIntent();
		PendingIntent snoozePending = this.getSnoozePendingIntent();
		PendingIntent dismissPending = this.getDismissPendingIntent(
			activityPending);

		NotificationCompat.Builder builder = new NotificationCompat.Builder(
			context, CHANNEL)
			.setGroup(GROUP)
			.setContentIntent(activityPending)
			.setContentTitle(title)
			.setContentText(text)
			.setLargeIcon(icon)
			.setSmallIcon(R.mipmap.notification_icon)
			.setCategory(NotificationCompat.CATEGORY_ALARM)
			.setPriority(NotificationCompat.PRIORITY_MAX)
			.setFullScreenIntent(activityPending, true)
			.setAutoCancel(false)
			.setOngoing(true)
			.setShowWhen(true)
			.addAction(R.mipmap.alarm_snooze, "Snooze", snoozePending)
			.addAction(R.mipmap.alarm_dismiss, "Dismiss", dismissPending);
			//.setTicker("Truiton Music Player")

		return builder.build();
	}

	/**
	 */
	@TargetApi(Build.VERSION_CODES.O)
	public void createChannel()
	{
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
		{
			return;
		}

		Context context = this.getContext();
		Resources res = context.getResources();
		CharSequence name = res.getString(R.string.noti_channel_active_name);
		String description = res.getString(R.string.noti_channel_active_description);
		NotificationChannel channel = new NotificationChannel(CHANNEL, name,
			NotificationManager.IMPORTANCE_MAX);
		NotificationManager manager = context.getSystemService(
			NotificationManager.class);

		channel.setDescription(description);
		channel.setShowBadge(true);
		channel.enableLights(true);
		channel.enableVibration(true);
		channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
		manager.createNotificationChannel(channel);
	}

	/**
	 * @return The pending intent to use to start the NacAlarmActivity.
	 */
	private PendingIntent getActivityPendingIntent()
	{
		Context context = this.getContext();
		NacAlarm alarm = this.getAlarm();
		int id = (alarm == null) ? 0 : alarm.getId();
		Intent intent = NacIntent.createAlarmActivity(context, alarm);

		return PendingIntent.getActivity(context, id, intent,
			PendingIntent.FLAG_UPDATE_CURRENT);
	}

	/**
	 * @return The alarm.
	 */
	private NacAlarm getAlarm()
	{
		return this.mAlarm;
	}

	/**
	 * @return The context.
	 */
	private Context getContext()
	{
		return this.mContext;
	}

	/**
	 * @return The pending intent to use when dismissing the alarm.
	 */
	private PendingIntent getDismissPendingIntent(
		PendingIntent activityPendingIntent)
	{
		NacAlarm alarm = this.getAlarm();

		if ((alarm == null) || (alarm.getUseNfc()))
		{
			return activityPendingIntent;
		}

		Context context = this.getContext();
		Intent intent = new Intent(NacForegroundService.ACTION_DISMISS_ALARM,
			null, context, NacForegroundService.class);

		return PendingIntent.getService(context, 0, intent, 0);
	}

	/**
	 * @return The notification large icon.
	 */
	private Bitmap getIcon()
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
	 * @return The notification text.
	 */
	public String getText()
	{
		NacAlarm alarm = this.getAlarm();
		Calendar now = Calendar.getInstance();
		String text = NacCalendar.toString(now, "EEE h:mm a");

		if ((alarm != null) && !alarm.getName().isEmpty())
		{
			text = alarm.getName() + " - " + text;
		}

		return text;
	}

	/**
	 * @return The notification title.
	 */
	@SuppressWarnings("deprecation")
	@TargetApi(Build.VERSION_CODES.N)
	public Spanned getTitle()
	{
		String title = "<b>" + TITLE + "</b>";

		return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
			? Html.fromHtml(title, Html.FROM_HTML_MODE_LEGACY)
			: Html.fromHtml(title);
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

}

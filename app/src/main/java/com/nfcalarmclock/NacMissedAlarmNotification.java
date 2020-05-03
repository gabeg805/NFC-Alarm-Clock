package com.nfcalarmclock;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 */
public class NacMissedAlarmNotification
	extends NacNotification
{

	/**
	 * Channel.
	 */
	protected static final String MISSED_CHANNEL = "NacNotiChannelMissed";

	/**
	 * Group.
	 */
	protected static final String MISSED_GROUP = "NacNotiGroupMissed";

	/**
	 * Notification ID.
	 */
	protected static final int MISSED_ID = 222;

	/**
	 * List of lines to show in the notification.
	 */
	protected List<CharSequence> mLines;

	/**
	 */
	public NacMissedAlarmNotification()
	{
		super();
	}

	/**
	 */
	public NacMissedAlarmNotification(Context context)
	{
		super(context);
		this.mLines = null;
	}

	/**
	 */
	@Override
	public void createChannel()
	{
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
		{
			return;
		}

		Context context = this.getContext();
		NacSharedConstants cons = new NacSharedConstants(context);
		NotificationChannel channel = new NotificationChannel(MISSED_CHANNEL,
			cons.getMissedNotification(),
			NotificationManager.IMPORTANCE_DEFAULT);
		NotificationManager manager = context.getSystemService(
			NotificationManager.class);

		channel.setDescription(cons.getDescriptionMissedNotification());
		channel.setShowBadge(true);
		channel.enableLights(true);
		channel.enableVibration(true);
		channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
		manager.createNotificationChannel(channel);
	}

	/**
	 * @return The notification group.
	 */
	public String getGroup()
	{
		return MISSED_GROUP;
	}

	/**
	 * @return The list of lines in the notification.
	 */
	protected List<CharSequence> getLines()
	{
		return this.mLines;
	}

	/**
	 * @return The notification text.
	 */
	public String getText(NacAlarm alarm)
	{
		Context context = this.getContext();
		String time = alarm.getFullTime(context);
		String name = alarm.getName();
		Locale locale = Locale.getDefault();

		return (name.isEmpty()) ? time
			: String.format(locale, "%1$s  —  %2$s", time, name);
	}

	/**
	 * @return The notification title.
	 */
	public String getTitle()
	{
		Context context = this.getContext();
		List<CharSequence> lines = this.getLines();
		NacSharedConstants cons = new NacSharedConstants(context);
		int count = (lines == null) ? 1 : lines.size();

		return cons.getMissedAlarm(count);
	}

	/**
	 * @see hide
	 */
	public void hide()
	{
		Context context = this.getContext();
		NotificationManagerCompat manager = this.getNotificationManager(context);
		manager.cancel(MISSED_ID);
	}

	/**
	 * Show the notification.
	 */
	@TargetApi(Build.VERSION_CODES.M)
	public void show(NacAlarm alarm)
	{
		Context context = this.getContext();
		NotificationManager manager = (NotificationManager)
			context.getSystemService(Context.NOTIFICATION_SERVICE);

		if (alarm == null)
		{
			//manager.cancel(MISSED_ID);
			return;
		}


		this.mLines = this.getNotificationText(manager, alarm);
		List<CharSequence> lines = this.mLines;
		NotificationCompat.InboxStyle inbox = new NotificationCompat
			.InboxStyle();
		NotificationCompat.Builder builder = this.build(MISSED_CHANNEL)
			.setAutoCancel(true)
			.setShowWhen(true);
		int size = lines.size();

		for (CharSequence l : lines)
		{
			inbox.addLine(l);
		}

		if (size == 0)
		{
			manager.cancel(MISSED_ID);
		}
		else if (size > 0)
		{
			NacSharedConstants cons = new NacSharedConstants(context);
			Locale locale = Locale.getDefault();
			String word = cons.getAlarm(size);
			String content = String.format(locale, "%1$d %2$s", size, word);

			builder.setContentText(content);
		}

		builder.setStyle(inbox);
		manager.notify(MISSED_ID, builder.build());
	}

}

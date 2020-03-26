package com.nfcalarmclock;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.service.notification.StatusBarNotification;
import android.text.Html;
import android.text.Spanned;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Notification for the app to keep it in memory.
 */
public abstract class NacNotification
{

	/**
	 * Create the notificaion channel.
	 */
	public abstract void createChannel();

	/**
	 * @return The notification group.
	 */
	public abstract String getGroup();

	/**
	 * @return The notification text.
	 */
	public abstract String getText(NacAlarm alarm);

	/**
	 * @return The notification title.
	 */
	public abstract String getTitle();

	/**
	 * Context.
	 */
	protected final Context mContext;

	/**
	 */
	public NacNotification()
	{
		super();

		this.mContext = null;
	}

	/**
	 */
	public NacNotification(Context context)
	{
		this.mContext = context;

		this.createChannel();
	}

	/**
	 * Build the notification.
	 */
	@SuppressWarnings("deprecation")
	@TargetApi(Build.VERSION_CODES.N)
	protected NotificationCompat.Builder build(String channel)
	{
		Context context = this.getContext();
		PendingIntent pending = this.getMainActivityIntent(context);
		String title = "<b>"+this.getTitle()+"</b>";
		Spanned boldTitle;

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
		{
			boldTitle = Html.fromHtml(title, Html.FROM_HTML_MODE_LEGACY);
		}
		else
		{
			boldTitle = Html.fromHtml(title);
		}

		NotificationCompat.Builder builder = new NotificationCompat.Builder(
			context, channel)
			.setGroup(this.getGroup())
			.setContentTitle(boldTitle)
			.setSmallIcon(R.mipmap.notification)
			.setPriority(NotificationCompat.PRIORITY_DEFAULT)
			.setCategory(NotificationCompat.CATEGORY_ALARM)
			.setContentIntent(pending)
			.setGroupSummary(true);

		return builder;
	}

	/**
	 * @return The context.
	 */
	protected Context getContext()
	{
		return this.mContext;
	}

	/**
	 * @return The pending intent for the main activity.
	 */
	protected PendingIntent getMainActivityIntent(Context context)
	{
		Intent intent = new Intent(context, NacMainActivity.class);
		int flags = Intent.FLAG_ACTIVITY_NEW_TASK
			| Intent.FLAG_ACTIVITY_CLEAR_TASK;

		intent.addFlags(flags);

		return PendingIntent.getActivity(context, 0, intent, 0);
	}

	/**
	 * @return The notification manager.
	 */
	protected NotificationManagerCompat getNotificationManager(Context context)
	{
		return NotificationManagerCompat.from(context);
	}

	/**
	 * @return A list of notification lines.
	 */
	@TargetApi(Build.VERSION_CODES.M)
	public List<CharSequence> getNotificationText(NotificationManager manager,
		NacAlarm alarm)
	{
		List<CharSequence> lines = new ArrayList<>();
		String text = this.getText(alarm);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
		{
			StatusBarNotification[] statusbar = manager
				.getActiveNotifications();

			for (StatusBarNotification sb : statusbar)
			{
				Notification notification = sb.getNotification();
				String group = notification.getGroup();
				CharSequence[] extraLines = (CharSequence[]) notification.extras
					.get(NotificationCompat.EXTRA_TEXT_LINES);

				if (group.equals(this.getGroup()))
				{
					for (CharSequence line : extraLines)
					{
						lines.add(line);
					}
				}

			}
		}

		lines.add((CharSequence) text);

		return lines;
	}

	/**
	 * @return A list of notification lines.
	 */
	public List<CharSequence> getNotificationText(List<NacAlarm> alarms)
	{
		List<CharSequence> lines = new ArrayList<>();

		for (NacAlarm a : alarms)
		{
			if (!a.getEnabled())
			{
				continue;
			}

			String text = this.getText(a);

			lines.add(text);
		}

		return lines;
	}

}

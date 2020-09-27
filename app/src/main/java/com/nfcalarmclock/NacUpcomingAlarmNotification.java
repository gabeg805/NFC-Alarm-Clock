package com.nfcalarmclock;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 */
public class NacUpcomingAlarmNotification
	extends NacNotification
{

	/**
	 * List of alarms.
	 */
	private List<NacAlarm> mAlarmList;

	/**
	 */
	public NacUpcomingAlarmNotification()
	{
		super();
	}
	
	/**
	 */
	public NacUpcomingAlarmNotification(Context context)
	{
		super(context);
	}

	/**
	 * @see builder
	 */
	@Override
	protected NotificationCompat.Builder builder()
	{
		NotificationCompat.InboxStyle inbox = new NotificationCompat
			.InboxStyle();
		List<CharSequence> body = this.getBody();

		for (CharSequence line : body)
		{
			inbox.addLine(line);
		}

		return super.builder()
			.setGroupSummary(true)
			.setAutoCancel(false)
			.setShowWhen(false)
			.setStyle(inbox);
	}

	/**
	 * @see createChannel
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

		channel.setShowBadge(false);
		channel.enableLights(false);
		channel.enableVibration(false);
		return channel;
	}

	/**
	 * @return Alarm list.
	 */
	public List<NacAlarm> getAlarmList()
	{
		return this.mAlarmList;
	}

	/**
	 * @see getChannelDescription
	 */
	protected String getChannelDescription()
	{
		Context context = this.getContext();
		NacSharedConstants cons = new NacSharedConstants(context);
		return cons.getDescriptionUpcomingNotification();
	}

	/**
	 * @see getChannelName
	 */
	protected String getChannelName()
	{
		Context context = this.getContext();
		NacSharedConstants cons = new NacSharedConstants(context);
		return cons.getUpcomingNotification();
	}

	/**
	 * @see getChannelId
	 */
	protected String getChannelId()
	{
		return "NacNotiChannelUpcoming";
	}

	/**
	 * @see getContentPendingIntent
	 */
	protected PendingIntent getContentPendingIntent()
	{
		Context context = this.getContext();
		Intent intent = new Intent(context, NacMainActivity.class);
		int flags = Intent.FLAG_ACTIVITY_NEW_TASK
			| Intent.FLAG_ACTIVITY_CLEAR_TASK;

		intent.addFlags(flags);
		return PendingIntent.getActivity(context, 0, intent, 0);
	}

	/**
	 * @see getContentText
	 */
	protected String getContentText()
	{
		Context context = this.getContext();
		Locale locale = Locale.getDefault();
		NacSharedConstants cons = new NacSharedConstants(context);
		int size = this.getBody().size();
		String word = cons.getAlarm(size);

		return (size > 0) ? String.format(locale, "%1$d %2$s", size, word) : "";
	}

	/**
	 * @see getGroup
	 */
	protected String getGroup()
	{
		return "NacNotiGroupUpcoming";
	}

	/**
	 * @see getId
	 */
	protected int getId()
	{
		return 111;
	}

	/**
	 * @see getImportance
	 */
	protected int getImportance()
	{
		return NotificationManagerCompat.IMPORTANCE_LOW;
	}

	/**
	 * @see getPriority
	 */
	protected int getPriority()
	{
		return NotificationCompat.PRIORITY_DEFAULT;
	}

	/**
	 * @see getTitle
	 */
	public String getTitle()
	{
		Context context = this.getContext();
		Locale locale = Locale.getDefault();
		NacSharedConstants cons = new NacSharedConstants(context);
		int count = this.getLineCount();

		return String.format(locale, "<b>%1$s</b>", cons.getUpcomingAlarm(count));
	}

	/**
	 * Set the alarm list.
	 */
	public void setAlarmList(List<NacAlarm> alarmList)
	{
		this.mAlarmList = alarmList;
	}

	/**
	 * @see setupBody
	 */
	protected void setupBody()
	{
		List<NacAlarm> alarms = this.getAlarmList();
		List<CharSequence> body = new ArrayList<>();

		for (NacAlarm a : alarms)
		{
			if (!a.getEnabled())
			{
				continue;
			}

			String line = this.getBodyLine(a);
			body.add(line);
		}

		this.mBody = body;
	}

	/**
	 * @see show
	 */
	public void show()
	{
		List<NacAlarm> alarms = this.getAlarmList();
		if ((alarms == null) || (alarms.size() == 0))
		{
			this.cancel();
			return;
		}

		this.setupBody();

		if (this.hasBody())
		{
			super.show();
		}
		else
		{
			this.cancel();
		}
	}

}

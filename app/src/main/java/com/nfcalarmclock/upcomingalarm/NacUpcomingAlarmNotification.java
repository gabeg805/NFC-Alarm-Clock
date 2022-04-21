package com.nfcalarmclock.upcomingalarm;

import android.annotation.TargetApi;
import android.app.NotificationChannel;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.nfcalarmclock.alarm.NacAlarm;
import com.nfcalarmclock.util.notification.NacNotification;
import com.nfcalarmclock.shared.NacSharedConstants;
import com.nfcalarmclock.system.NacIntent;

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
	public NacUpcomingAlarmNotification(Context context)
	{
		super(context);
	}

	/**
	 * @see NacNotification#builder()
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
	 * @see NacNotification#createChannel()
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
	 * @see NacNotification#getChannelDescription()
	 */
	protected String getChannelDescription()
	{
		NacSharedConstants cons = this.getSharedConstants();
		return cons.getDescriptionUpcomingNotification();
	}

	/**
	 * @see NacNotification#getChannelName()
	 */
	protected String getChannelName()
	{
		NacSharedConstants cons = this.getSharedConstants();
		return cons.getUpcomingNotification();
	}

	/**
	 * @see NacNotification#getChannelId()
	 */
	protected String getChannelId()
	{
		return "NacNotiChannelUpcoming";
	}

	/**
	 * @see NacNotification#getContentPendingIntent()
	 */
	protected PendingIntent getContentPendingIntent()
	{
		Context context = this.getContext();
		Intent intent = NacIntent.createMainActivity(context);

		// Determine the pending intent flags
		int flags = 0;

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
		{
			flags |= PendingIntent.FLAG_IMMUTABLE;
		}

		return PendingIntent.getActivity(context, 0, intent, flags);
	}

	/**
	 * @see NacNotification#getContentText()
	 */
	protected String getContentText()
	{
		Locale locale = Locale.getDefault();
		NacSharedConstants cons = this.getSharedConstants();
		int size = this.getBody().size();
		String word = cons.getAlarm(size);

		return (size > 0) ? String.format(locale, "%1$d %2$s", size, word) : "";
	}

	/**
	 * @see NacNotification#getGroup()
	 */
	protected String getGroup()
	{
		return "NacNotiGroupUpcoming";
	}

	/**
	 * @see NacNotification#getId()
	 */
	protected int getId()
	{
		return 111;
	}

	/**
	 * @see NacNotification#getImportance()
	 */
	protected int getImportance()
	{
		return NotificationManagerCompat.IMPORTANCE_LOW;
	}

	/**
	 * @see NacNotification#getPriority()
	 */
	protected int getPriority()
	{
		return NotificationCompat.PRIORITY_DEFAULT;
	}

	/**
	 * @see NacNotification#getTitle()
	 */
	public String getTitle()
	{
		Locale locale = Locale.getDefault();
		NacSharedConstants cons = this.getSharedConstants();
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
	 * @see NacNotification#setupBody()
	 */
	protected void setupBody()
	{
		List<NacAlarm> alarms = this.getAlarmList();
		List<CharSequence> body = new ArrayList<>();

		for (NacAlarm a : alarms)
		{
			if (!a.isEnabled())
			{
				continue;
			}

			String line = this.getBodyLine(a);
			body.add(line);
		}

		this.mBody = body;
	}

	/**
	 * @see NacNotification#show()
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

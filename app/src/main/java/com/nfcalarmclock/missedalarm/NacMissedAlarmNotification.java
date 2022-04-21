package com.nfcalarmclock.missedalarm;

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

import java.util.List;
import java.util.Locale;

/**
 */
public class NacMissedAlarmNotification
	extends NacNotification
{

	/**
	 * Alarm.
	 */
	private NacAlarm mAlarm;

	/**
	 */
	public NacMissedAlarmNotification(Context context)
	{
		super(context);

		this.mAlarm = null;
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
			.setAutoCancel(true)
			.setShowWhen(true)
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

		channel.setShowBadge(true);
		channel.enableLights(true);
		channel.enableVibration(true);
		return channel;
	}

	/**
	 * @return Alarm.
	 */
	public NacAlarm getAlarm()
	{
		return this.mAlarm;
	}

	///**
	// * @see NacNotification#getBodyLine(NacAlarm)
	// */
	//public String getBodyLine(NacAlarm alarm)
	//{

	//	Context context = this.getContext();
	//	//NacAlarmRepository repo = new NacAlarmRepository(context);
	//	//NacAlarm actualAlarm = repo.findAlarm(alarm);

	//	return super.getBodyLine(alarm);
	//	//return super.getBodyLine(actualAlarm);
	//}

	/**
	 * @see NacNotification#getChannelDescription()
	 */
	protected String getChannelDescription()
	{
		NacSharedConstants cons = this.getSharedConstants();
		return cons.getDescriptionMissedNotification();
	}

	/**
	 * @see NacNotification#getChannelName()
	 */
	protected String getChannelName()
	{
		NacSharedConstants cons = this.getSharedConstants();
		return cons.getMissedNotification();
	}

	/**
	 * @see NacNotification#getChannelId()
	 */
	protected String getChannelId()
	{
		return "NacNotiChannelMissed";
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
		return "NacNotiGroupMissed";
	}

	/**
	 * @see NacNotification#getId()
	 */
	protected int getId()
	{
		return 222;
	}

	/**
	 * @see NacNotification#getImportance()
	 */
	protected int getImportance()
	{
		return NotificationManagerCompat.IMPORTANCE_DEFAULT;
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

		return String.format(locale, "<b>%1$s</b>", cons.getMissedAlarm(count));
	}

	/**
	 * Set the alarm.
	 */
	public void setAlarm(NacAlarm alarm)
	{
		this.mAlarm = alarm;
	}

	/**
	 * @see NacNotification#setupBody()
	 */
	protected void setupBody()
	{
		Context context = this.getContext();
		String groupKey = this.getGroup();
		NacAlarm alarm = this.getAlarm();
		String line = this.getBodyLine(alarm);
		List<CharSequence> body = NacNotification.getExtraLines(context, groupKey);

		body.add(line);

		this.mBody = body;
	}

	/**
	 * @see NacNotification#show()
	 */
	public void show()
	{
		NacAlarm alarm = this.getAlarm();
		if (alarm == null)
		{
			return;
		}

		// Used to call cancel() if size was 0. Might not have to because
		// show() should cancel it anyway
		this.setupBody();
		super.show();
	}

}

package com.nfcalarmclock;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import java.util.List;
import java.util.Locale;

/**
 * Notification for the app to keep it in memory.
 */
public abstract class NacNotification
{

	/**
	 * @return Channel description.
	 */
	protected abstract String getChannelDescription();

	/**
	 * @return Channel ID.
	 */
	protected abstract String getChannelId();

	/**
	 * @return Channel name.
	 */
	protected abstract String getChannelName();

	/**
	 * @return Content pending intent when building the notification.
	 */
	protected abstract PendingIntent getContentPendingIntent();

	/**
	 * @return Content text when building the notification.
	 */
	protected abstract String getContentText();

	/**
	 * @return Group name.
	 */
	protected abstract String getGroup();

	/**
	 * @return Notification ID.
	 */
	protected abstract int getId();

	/**
	 * @return Level of importance of the notification.
	 */
	protected abstract int getImportance();

	/**
	 * @return Level of priority of the notification.
	 */
	protected abstract int getPriority();

	/**
	 * @return Notification title.
	 */
	public abstract String getTitle();

	/**
	 * Context.
	 */
	protected final Context mContext;

	/**
	 * Body text of the notification.
	 */
	protected List<CharSequence> mBody;

	/**
	 */
	public NacNotification()
	{
		super();
		this.mContext = null;
		this.mBody = null;
	}

	/**
	 */
	public NacNotification(Context context)
	{
		this.mContext = context;
		this.mBody = null;
		this.setupChannel();
	}

	/**
	 * Build the notification.
	 */
	@SuppressWarnings("deprecation")
	@TargetApi(Build.VERSION_CODES.N)
	protected NotificationCompat.Builder builder()
	{
		return new NotificationCompat.Builder(this.getContext(), this.getChannelId())
			.setGroup(this.getGroup())
			.setContentIntent(this.getContentPendingIntent())
			.setContentTitle(NacUtility.toSpannedString(this.getTitle()))
			.setContentText(this.getContentText())
			.setSmallIcon(this.getSmallIcon())
			.setPriority(this.getPriority())
			.setCategory(this.getCategory());
	}

	/**
	 * Cancel a previously shown notification.
	 */
	public void cancel()
	{
		NotificationManagerCompat manager = this.getNotificationManager();
		int id = this.getId();
		manager.cancel(id);
	}

	/**
	 * Create the notification channel.
	 */
	@TargetApi(Build.VERSION_CODES.O)
	protected NotificationChannel createChannel()
	{
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
		{
			return null;
		}

		NotificationChannel channel = new NotificationChannel(
			this.getChannelId(),
			this.getChannelName(),
			this.getImportance());

		channel.setDescription(this.getChannelDescription());
		channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
		return channel;
	}

	/**
	 * @return True if the given group key matches that of the notification, and
	 *         False otherwise.
	 */
	protected boolean doesGroupMatch(String groupKey)
	{
		return this.getGroup().equals(groupKey);
	}

	/**
	 * @return Body text of the notification.
	 */
	protected List<CharSequence> getBody()
	{
		return this.mBody;
	}

	/**
	 * @return A line that can appear in the body text of the notification.
	 */
	protected String getBodyLine(NacAlarm alarm)
	{
		Context context = this.getContext();
		String time = alarm.getFullTime(context);
		String name = alarm.getName();
		Locale locale = Locale.getDefault();

		return name.isEmpty() ? time
			: String.format(locale, "%1$s  —  %2$s", time, name);
	}

	/**
	 * @return The category of the notification.
	 */
	protected String getCategory()
	{
		return NotificationCompat.CATEGORY_ALARM;
	}

	/**
	 * @return The context.
	 */
	protected Context getContext()
	{
		return this.mContext;
	}

	/**
	 * @return Number of lines in the body.
	 * 
	 * TODO: See if this can be 0 instead of 1.
	 */
	protected int getLineCount()
	{
		List<CharSequence> body = this.getBody();
		return (body == null) ? 1 : body.size();
	}

	/**
	 * @return The notification manager.
	 */
	protected NotificationManagerCompat getNotificationManager()
	{
		Context context = this.getContext();
		return NotificationManagerCompat.from(context);
	}

	/**
	 * @return The small icon of the notification.
	 */
	protected int getSmallIcon()
	{
		return R.mipmap.notification;
	}

	/**
	 * @return True if there is text in the body, and False otherwise.
	 */
	protected boolean hasBody()
	{
		List<CharSequence> body = this.getBody();
		int size = (body != null) ? body.size() : 0;
		return (size > 0);
	}

	/**
	 * Setup the notification body lines.
	 */
	protected void setupBody()
	{
	}

	/**
	 * Setup the notification channel.
	 */
	protected void setupChannel()
	{
		Context context = this.getContext();
		NotificationChannel channel = this.createChannel();
		if (channel != null)
		{
			NotificationManagerCompat manager = this.getNotificationManager();
			manager.createNotificationChannel(channel);
		}
	}

	/**
	 * Show the notification.
	 */
	protected void show()
	{
		NotificationCompat.Builder builder = this.builder();
		NotificationManagerCompat manager = this.getNotificationManager();
		int id = this.getId();

		manager.notify(id, builder.build());
	}

}

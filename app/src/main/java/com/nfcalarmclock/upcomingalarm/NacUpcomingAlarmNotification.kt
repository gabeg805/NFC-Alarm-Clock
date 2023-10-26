package com.nfcalarmclock.upcomingalarm

import android.annotation.TargetApi
import android.app.NotificationChannel
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.util.NacIntent.createMainActivity
import com.nfcalarmclock.view.notification.NacNotification
import java.util.Locale

/**
 */
class NacUpcomingAlarmNotification(context: Context) : NacNotification(context)
{

	/**
	 * @see NacNotification.id
	 */
	override val id: Int
		get() = 111

	/**
	 * @see NacNotification.channelId
	 */
	override val channelId: String
		get() = "NacNotiChannelUpcoming"

	/**
	 * @see NacNotification.channelName
	 */
	override val channelName: String
		get() = sharedConstants.upcomingNotification

	/**
	 * @see NacNotification.channelDescription
	 */
	override val channelDescription: String
		get() = sharedConstants.descriptionUpcomingNotification

	/**
	 * @see NacNotification.title
	 */
	override val title: String
		get()
		{
			val locale = Locale.getDefault()

			return String.format(locale, "<b>%1\$s</b>", sharedConstants.getUpcomingAlarm(lineCount))
		}

	/**
	 * @see NacNotification.priority
	 */
	override val priority: Int
		get() = NotificationCompat.PRIORITY_DEFAULT

	/**
	 * @see NacNotification.importance
	 */
	override val importance: Int
		get() = NotificationManagerCompat.IMPORTANCE_LOW

	/**
	 * @see NacNotification.group
	 */
	override val group: String
		get() = "NacNotiGroupUpcoming"

	/**
	 * @see NacNotification.contentText
	 */
	override val contentText: String
		get()
		{
			val locale = Locale.getDefault()
			val word = sharedConstants.getAlarm(body.size)

			return if (body.isNotEmpty())
			{
				String.format(locale, "%1\$d %2\$s", body.size, word)
			}
			else
			{
				""
			}
		}

	/**
	 * @see NacNotification.contentPendingIntent
	 */
	override val contentPendingIntent: PendingIntent
		get()
		{
			val intent = createMainActivity(context)

			// Determine the pending intent flags
			var flags = 0

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
			{
				flags = flags or PendingIntent.FLAG_IMMUTABLE
			}

			// Return the pending intent for the activity
			return PendingIntent.getActivity(context, 0, intent, flags)
		}

	/**
	 * List of alarms.
	 */
	var alarmList: List<NacAlarm> = emptyList()

	/**
	 * @see NacNotification.builder
	 */
	override fun builder(): NotificationCompat.Builder
	{
		// Create notification with inbox style
		val inbox = NotificationCompat.InboxStyle()

		// Iterate over each line in body
		for (line in body)
		{
			inbox.addLine(line)
		}

		// Build notification
		return super.builder()
			.setGroupSummary(true)
			.setAutoCancel(false)
			.setShowWhen(false)
			.setStyle(inbox)
	}

	/**
	 * @see NacNotification.createChannel
	 */
	@TargetApi(Build.VERSION_CODES.O)
	override fun createChannel(): NotificationChannel
	{
		// Create the channel
		val channel = super.createChannel()

		// Setup the channel
		channel.setShowBadge(false)
		channel.enableLights(false)
		channel.enableVibration(false)

		return channel
	}

	/**
	 * @see NacNotification.setupBody
	 */
	override fun setupBody()
	{
		val newBody: MutableList<CharSequence> = ArrayList()

		// Iterate over each alarm
		for (alarm in alarmList)
		{
			if (!alarm.isEnabled)
			{
				continue
			}

			// Determine the new line to add
			val line = getBodyLine(alarm)

			// Add new line to notification
			newBody.add(line)
		}

		// Set the new body
		body = newBody
	}

	/**
	 * @see NacNotification.show
	 */
	public override fun show()
	{
		// Check if alarm list is valid
		if (alarmList.isEmpty())
		{
			cancel()
			return
		}

		// Setup the body
		setupBody()

		// Show the notification if body has text
		if (body.isNotEmpty())
		{
			super.show()
		}
		// Cancel the notification
		else
		{
			cancel()
		}
	}

}
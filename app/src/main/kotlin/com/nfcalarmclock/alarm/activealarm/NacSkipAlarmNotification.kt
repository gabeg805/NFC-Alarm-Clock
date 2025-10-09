package com.nfcalarmclock.alarm.activealarm

import android.app.NotificationChannel
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.nfcalarmclock.R
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.main.NacMainActivity
import com.nfcalarmclock.system.NacCalendar
import com.nfcalarmclock.view.notification.NacBaseNotificationBuilder
import com.nfcalarmclock.view.toSpannedString
import java.util.Calendar

/**
 * Notification when an alarm is skipped.
 *
 * Note: This may not actually be shown to the user because the logic for skipping an
 *       alarm in the active alarm service is so fast.
 *
 * @param context Context.
 * @param alarm Alarm.
 */
class NacSkipAlarmNotification(
	context: Context,
	private val alarm: NacAlarm?
) : NacBaseNotificationBuilder(context, "NacNotiChannelSkipAlarm")
{

	/**
	 * @see NacBaseNotificationBuilder.id
	 */
	override val id: Int
		get() = ID

	/**
	 * @see NacBaseNotificationBuilder.channelName
	 */
	override val channelName: String
		get() = context.resources.getQuantityString(R.plurals.skipped_alarm, 7)

	/**
	 * @see NacBaseNotificationBuilder.channelDescription
	 */
	override val channelDescription: String
		get() = context.getString(R.string.description_skipped_alarm)

	/**
	 * @see NacBaseNotificationBuilder.channelImportance
	 */
	override val channelImportance: Int = NotificationManagerCompat.IMPORTANCE_LOW

	/**
	 * @see NacBaseNotificationBuilder.priorityLevel
	 */
	override val priorityLevel: Int = NotificationCompat.PRIORITY_LOW

	/**
	 * @see NacBaseNotificationBuilder.group
	 */
	override val group: String = "NacNotiGroupSkipAlarm"

	/**
	 * @see NacBaseNotificationBuilder.contentText
	 */
	override val contentText: String
		get()
		{
			// Get the full time
			val now = Calendar.getInstance()
			val time = NacCalendar.getFullTime(context, now)

			// Get the alarm name
			val name = alarm?.name ?: ""

			return if (name.isEmpty())
			{
				time
			}
			else
			{
				"$time  —  $name"
			}
		}

	/**
	 * @see NacBaseNotificationBuilder.contentPendingIntent
	 */
	override val contentPendingIntent: PendingIntent
		get() = NacMainActivity.getStartPendingIntent(context)

	/**
	 * Constructor.
	 */
	init
	{
		// Create the channel
		setupChannel()

		// Get the title
		val title = "<b>${context.resources.getQuantityString(R.plurals.skipped_alarm, 1)}</b>"

		// Build the notification
		this.setPriority(priorityLevel)
			.setCategory(category)
			.setGroup(group)
			.setContentTitle(title.toSpannedString())
			.setContentText(contentText)
			.setContentIntent(contentPendingIntent)
			.setSmallIcon(smallIcon)
			.setTicker(channelName)
			.setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_DEFAULT)
			.setColor(ContextCompat.getColor(context, R.color.ic_launcher_background))
			.setAutoCancel(true)
			.setOngoing(false)
			.setShowWhen(true)
			.setSound(null)
	}

	/**
	 * @see NacBaseNotificationBuilder.createChannel
	 */
	@RequiresApi(Build.VERSION_CODES.O)
	override fun createChannel(): NotificationChannel
	{
		// Create the channel
		val channel = super.createChannel()

		// Setup the channel
		channel.setShowBadge(true)
		channel.enableLights(false)
		channel.enableVibration(false)
		channel.setSound(null, null)

		return channel
	}

	companion object
	{

		/**
		 * Notification ID.
		 */
		const val ID = 49

	}

}
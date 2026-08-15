package com.nfcalarmclock.alarm.options.missedalarm

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
import java.util.Calendar

/**
 * Missed alarm notification.
 *
 * @param context Context.
 * @param alarm Alarm.
 */
class NacMissedAlarmNotification(
	context: Context,
	private val alarm: NacAlarm
) : NacBaseNotificationBuilder(context, "NacNotiChannelMissed")
{

	/**
	 * @see NacBaseNotificationBuilder.id
	 */
	override val id: Int
		get() = BASE_ID + alarm.id.toInt()

	/**
	 * @see NacBaseNotificationBuilder.channelName
	 */
	override val channelName: String = context.getString(R.string.title_missed_alarms)

	/**
	 * @see NacBaseNotificationBuilder.channelDescription
	 */
	override val channelDescription: String = context.getString(R.string.description_missed_alarm)

	/**
	 * @see NacBaseNotificationBuilder.priorityLevel
	 */
	override val priorityLevel: Int = NotificationCompat.PRIORITY_DEFAULT

	/**
	 * @see NacBaseNotificationBuilder.channelImportance
	 */
	override val channelImportance: Int = NotificationManagerCompat.IMPORTANCE_DEFAULT

	/**
	 * @see NacBaseNotificationBuilder.group
	 */
	override val group: String = "NacNotiGroupMissed"

	/**
	 * @see NacBaseNotificationBuilder.contentText
	 */
	override val contentText: String
		get()
		{
			// Calendar of the alarm. If it is in the future, subtract a day
			val now = Calendar.getInstance()
			val cal = NacCalendar.alarmToCalendar(alarm)

			if (cal > now)
			{
				cal.add(Calendar.DAY_OF_MONTH, -1)
			}

			// Time of the alarm
			val time = NacCalendar.getFullTime(context, cal)

			// Notification text
			return if (alarm.name.isEmpty())
			{
				time
			}
			else
			{
				"$time  —  ${alarm.name}"
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
		val title = context.resources.getString(R.string.title_missed_alarm)

		// Build the notification
		this.setPriority(priorityLevel)
			.setCategory(category)
			.setGroup(group)
			.setContentTitle(title)
			.setContentText(contentText)
			.setContentIntent(contentPendingIntent)
			.setSmallIcon(smallIcon)
			.setTicker(channelName)
			.setColor(ContextCompat.getColor(context, R.color.ic_launcher_background))
			.setGroupSummary(true)
			.setAutoCancel(true)
			.setShowWhen(true)
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
		channel.enableLights(true)
		channel.enableVibration(true)

		return channel
	}

	companion object
	{

		/**
		 * The base ID value to use for this type of notification.
		 */
		const val BASE_ID: Int = 222

	}

}
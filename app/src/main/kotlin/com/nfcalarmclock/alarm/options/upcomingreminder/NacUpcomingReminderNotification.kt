package com.nfcalarmclock.alarm.options.upcomingreminder

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
 * Upcoming reminder notification indicating that an alarm will go off soon.
 *
 * @param context Context.
 * @param alarm Alarm.
 */
class NacUpcomingReminderNotification(
	context: Context,
	private val alarm: NacAlarm?
) : NacBaseNotificationBuilder(context, "NacNotiChannelUpcoming")
{

	/**
	 * @see NacBaseNotificationBuilder.id
	 */
	public override val id: Int
		get() = BASE_ID + (alarm?.id?.toInt() ?: 0)

	/**
	 * @see NacBaseNotificationBuilder.channelName
	 */
	override val channelName: String = context.getString(R.string.title_upcoming_reminders)

	/**
	 * @see NacBaseNotificationBuilder.channelDescription
	 */
	override val channelDescription: String = context.getString(R.string.description_upcoming_reminder)

	/**
	 * @see NacBaseNotificationBuilder.channelImportance
	 */
	override val channelImportance: Int = NotificationManagerCompat.IMPORTANCE_HIGH

	/**
	 * @see NacBaseNotificationBuilder.priorityLevel
	 */
	override val priorityLevel: Int = NotificationCompat.PRIORITY_MAX

	/**
	 * @see NacBaseNotificationBuilder.group
	 */
	override val group: String = "NacNotiGroupUpcomingReminder"

	/**
	 * @see NacBaseNotificationBuilder.contentText
	 */
	override val contentText: String
		get()
		{
			// Get the calendar of the alarm
			val cal = if (alarm != null)
			{
				NacCalendar.getNextAlarmDay(alarm)!!
			}
			else
			{
				Calendar.getInstance()
			}

			// Get the full time from the calendar
			val time = NacCalendar.getFullTime(context, cal)

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
	 * The pending intent to clear a recurring reminder.
	 */
	private val clearReminderPendingIntent: PendingIntent
		get()
		{
			// Create the intent
			val intent = NacUpcomingReminderService.getStopIntent(context, alarm)

			// Determine the pending intent flags
			val flags = PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE

			// Create the pending intent
			return PendingIntent.getService(context, 0, intent, flags)
		}

	/**
	 * Constructor.
	 */
	init
	{
		// Create the channel
		setupChannel()

		// Get the title
		val title = "<b>${context.getString(R.string.word_reminder)}</b>"

		// Build the notification
		// Note: Added the parentheses so that the custom addAction() can be called
		(this.setPriority(priorityLevel)
			.setCategory(category)
			.setGroup(group)
			.setContentTitle(title.toSpannedString())
			.setContentText(contentText)
			.setContentIntent(contentPendingIntent)
			.setSmallIcon(smallIcon)
			.setTicker(channelName)
			.setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
			.setColor(ContextCompat.getColor(context, R.color.ic_launcher_background))
			.setAutoCancel(true)
			.setOngoing(false)
			.setShowWhen(true) as NacBaseNotificationBuilder)
			.apply {
				// Add a button to clear the recurring reminder notification
				if ((alarm != null) && (alarm.reminderFrequency > 0))
				{
					addAction(R.drawable.dismiss, R.string.action_clear_reminder, clearReminderPendingIntent)
				}
			}
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
		const val BASE_ID: Int = 111

	}

}
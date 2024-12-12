package com.nfcalarmclock.alarm.options.upcomingreminder

import android.annotation.TargetApi
import android.app.NotificationChannel
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.nfcalarmclock.R
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.main.NacMainActivity
import com.nfcalarmclock.util.NacCalendar
import com.nfcalarmclock.view.notification.NacNotification
import java.util.Calendar

/**
 */
class NacUpcomingReminderNotification(

	/**
	 * Context.
	 */
	context: Context,

	/**
	 * Alarm.
	 */
	private val alarm: NacAlarm?

) : NacNotification(context)
{

	/**
	 * @see NacNotification.id
	 */
	public override val id: Int
		get() = BASE_ID + (alarm?.id?.toInt() ?: 0)

	/**
	 * @see NacNotification.channelId
	 */
	override val channelId: String
		get() = "NacNotiChannelUpcoming"

	/**
	 * @see NacNotification.channelName
	 */
	override val channelName: String
		get() = context.getString(R.string.title_upcoming_reminders)

	/**
	 * @see NacNotification.channelDescription
	 */
	override val channelDescription: String
		get() = context.getString(R.string.description_upcoming_reminder)

	/**
	 * @see NacNotification.title
	 */
	override val title: String
		get()
		{
			// Get the title
			val reminder = context.getString(R.string.word_reminder)

			// Format the title
			return "<b>$reminder</b>"
		}

	/**
	 * @see NacNotification.priority
	 */
	override val priority: Int
		get() = NotificationCompat.PRIORITY_MAX

	/**
	 * @see NacNotification.importance
	 */
	override val importance: Int
		get() = NotificationManagerCompat.IMPORTANCE_HIGH

	/**
	 * @see NacNotification.group
	 */
	override val group: String
		get() = "NacNotiGroupUpcomingReminder"

	/**
	 * @see NacNotification.contentText
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
	 * @see NacNotification.contentPendingIntent
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
			val intent = NacUpcomingReminderService.getClearReminderIntent(context, alarm)

			// Determine the pending intent flags
			val flags = PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE

			// Create the pending intent
			return PendingIntent.getService(context, 0, intent, flags)
		}

	/**
	 * @see NacNotification.builder
	 */
	public override fun builder(): NotificationCompat.Builder
	{
		// Build the notification
		val notificationBuilder = super.builder()
			.setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
			.setAutoCancel(true)
			.setOngoing(false)
			.setShowWhen(true)

		// Check if the alarm uses a recurring notification
		return if ((alarm != null) && (alarm.reminderFrequency > 0))
		{
			// Notification actions
			val clear = context.getString(R.string.action_clear_reminder)

			// Add a button to clear the recurring reminder
			notificationBuilder
				.addAction(R.drawable.dismiss, clear, clearReminderPendingIntent)
		}
		else
		{
			// Notification is perfectly built as is
			notificationBuilder
		}
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
		channel.setShowBadge(true)
		channel.enableLights(true)
		channel.enableVibration(true)

		return channel
	}

	/**
	 * @see NacNotification.show
	 */
	public override fun show()
	{
		// Super
		super.show()
	}

	companion object
	{

		/**
		 * The base ID value to use for this type of notification.
		 */
		const val BASE_ID: Int = 111

	}

}
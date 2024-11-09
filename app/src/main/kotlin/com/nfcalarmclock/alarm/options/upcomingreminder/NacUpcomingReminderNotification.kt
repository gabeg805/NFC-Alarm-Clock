package com.nfcalarmclock.alarm.options.upcomingreminder

import android.annotation.TargetApi
import android.app.NotificationChannel
import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.text.format.DateFormat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.nfcalarmclock.R
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.main.NacMainActivity
import com.nfcalarmclock.util.NacCalendar
import com.nfcalarmclock.view.notification.NacNotification
import java.util.Calendar
import java.util.Locale

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
		get() = 111 + (alarm?.id?.toInt() ?: 0)

	/**
	 * @see NacNotification.channelId
	 */
	override val channelId: String
		get() = "NacNotiChannelUpcoming"

	/**
	 * @see NacNotification.channelName
	 */
	override val channelName: String
		get() = context.getString(R.string.title_upcoming_reminder)

	/**
	 * @see NacNotification.channelDescription
	 */
	override val channelDescription: String
		get() = context.getString(R.string.description_upcoming_reminder)

	/**
	 * @see NacNotification.title
	 */
	override val title: String
		get() = "<b>$appName</b>"

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
				NacCalendar.getNextAlarmDay(alarm)
			}
			else
			{
				Calendar.getInstance()
			}

			// Get the full time from the calendar
			val is24HourFormat = DateFormat.is24HourFormat(context)
			val time = NacCalendar.getFullTime(cal, is24HourFormat)

			// Get the alarm name
			// TODO: Should the name be normalized?
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
		get()
		{
			val intent = NacMainActivity.getStartIntent(context)

			// Determine the pending intent flags
			var flags = PendingIntent.FLAG_IMMUTABLE

			// Return the pending intent for the activity
			return PendingIntent.getActivity(context, 0, intent, flags)
		}

	/**
	 * The pending intent to clear a recurring reminder.
	 */
	private val clearReminderPendingIntent: PendingIntent
		get()
		{
			// Create the intent
			val intent = NacUpcomingReminderService.getClearReminderIntent(context, alarm)

			// Determine the pending intent flags
			var flags = PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE

			// Create the pending intent
			return PendingIntent.getService(context, 0, intent, flags)
		}

	/**
	 * @return The notification large icon.
	 */
	private val largeIcon: Bitmap?
		get()
		{
			val res = context.resources
			val icon = BitmapFactory.decodeResource(res, R.mipmap.app)
			val density = res.displayMetrics.density

			// Determine the size of the bitmap
			val size: Float = if (density.compareTo(4.0f) >= 0)
			{
				256f
			}
			else if (density.compareTo(3.0f) >= 0)
			{
				192f
			}
			else if (density.compareTo(2.0f) >= 0)
			{
				128f
			}
			else if (density.compareTo(1.5f) >= 0)
			{
				96f
			}
			else if (density.compareTo(1.0f) >= 0)
			{
				64f
			}
			else if (density.compareTo(0.75f) >= 0)
			{
				48f
			}
			else
			{
				return icon
			}

			// Determine the integer size
			val actualSize = (size * density).toInt()

			// Create the bitmap
			return if (icon != null)
			{
				Bitmap.createScaledBitmap(icon, actualSize, actualSize, true)
			}
			else
			{
				null
			}
		}

	/**
	 * Name of the app.
	 */
	private val appName = context.getString(R.string.word_reminder)

	/**
	 * @see NacNotification.builder
	 */
	public override fun builder(): NotificationCompat.Builder
	{
		// Build the notification
		val notificationBuilder = super.builder()
			.setLargeIcon(largeIcon)
			.setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
			.setAutoCancel(true)
			.setOngoing(false)
			.setShowWhen(true)
			.setTicker(appName)

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

}
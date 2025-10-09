package com.nfcalarmclock.alarm.options.dismissoptions

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
 * Dismiss an alarm early notification.
 *
 * @param context Context.
 * @param alarm Alarm.
 */
class NacDismissEarlyNotification(
	context: Context,
	private val alarm: NacAlarm?
) : NacBaseNotificationBuilder(context, "NacNotiChannelDismissEarly")
{

	/**
	 * @see NacBaseNotificationBuilder.id
	 */
	public override val id: Int
		get() = BASE_ID + (alarm?.id?.toInt() ?: 0)

	/**
	 * @see NacBaseNotificationBuilder.channelName
	 */
	override val channelName: String = context.getString(R.string.title_dismiss_options_dismiss_early)

	/**
	 * @see NacBaseNotificationBuilder.channelDescription
	 */
	override val channelDescription: String = context.getString(R.string.description_dismiss_options_dismiss_early)

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
	override val group: String = "NacNotiGroupDismissEarly"

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
		get()
		{
			val id = alarm?.id ?: 0
			val intent = NacMainActivity.getStartIntent(context)

			// Determine the pending intent flags
			val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE

			// Return the pending intent for the activity
			return PendingIntent.getActivity(context, id.toInt(), intent, flags)
		}

	/**
	 * The pending intent to use when dismissing the alarm.
	 */
	private val dismissPendingIntent: PendingIntent
		get()
		{
			// Create an intent to dismiss the active alarm service
			val intent = NacMainActivity.getDismissEarlyIntent(context, alarm!!)

			// Determine the pending intent flags
			val flags = PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE

			// Create the pending intent
			return PendingIntent.getActivity(context, 0, intent, flags)
		}

	/**
	 * Constructor.
	 */
	init
	{
		// Create the channel
		setupChannel()

		// Build the notification
		// Note: Added the parentheses so that the custom addAction() can be called
		(this.setPriority(priorityLevel)
			.setCategory(category)
			.setGroup(group)
			.setContentTitle("<b>$channelName</b>".toSpannedString())
			.setContentText(contentText)
			.setContentIntent(contentPendingIntent)
			.setSmallIcon(smallIcon)
			.setTicker(channelName)
			.setColor(ContextCompat.getColor(context, R.color.ic_launcher_background))
			.setAutoCancel(true)
			.setOngoing(false)
			.setShowWhen(true) as NacBaseNotificationBuilder)
			.addAction(R.drawable.dismiss, R.string.action_alarm_dismiss, dismissPendingIntent)
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
		 * Notification ID.
		 */
		const val BASE_ID = 333

	}

}
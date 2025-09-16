package com.nfcalarmclock.alarm.options.dismissoptions

import android.app.NotificationChannel
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.nfcalarmclock.R
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.main.NacMainActivity
import com.nfcalarmclock.system.NacCalendar
import com.nfcalarmclock.view.notification.NacNotification
import java.util.Calendar

/**
 * Notification to for when an alarm can be dismissed early.
 */
class NacDismissEarlyNotification(

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
		get() = "NacNotiChannelDismissEarly"

	/**
	 * @see NacNotification.channelName
	 */
	override val channelName: String
		get() = context.getString(R.string.title_dismiss_options_dismiss_early)

	/**
	 * @see NacNotification.channelDescription
	 */
	override val channelDescription: String
		get() = context.getString(R.string.description_dismiss_options_dismiss_early)

	/**
	 * @see NacNotification.title
	 */
	override val title: String
		get() = "<b>$channelName</b>"

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
		get() = "NacNotiGroupDismissEarly"

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
	 * @see NacNotification.builder
	 */
	public override fun builder(): NotificationCompat.Builder
	{
		// Notification actions
		val dismiss = context.getString(R.string.action_alarm_dismiss)

		// Build the notification
		return super.builder()
			.setAutoCancel(true)
			.setOngoing(false)
			.setShowWhen(true)
			.addAction(R.drawable.dismiss, dismiss, dismissPendingIntent)
	}

	/**
	 * @see NacNotification.createChannel
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
package com.nfcalarmclock.alarm.activealarm

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
 * Notification to display when an alarm is skipped.
 */
class NacSkipAlarmNotification(

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
	override val id: Int
		get() = ID

	/**
	 * @see NacNotification.channelId
	 */
	override val channelId: String
		get() = "NacNotiChannelSkipAlarm"

	/**
	 * @see NacNotification.channelName
	 */
	override val channelName: String
		get() = context.resources.getQuantityString(R.plurals.skipped_alarm, 7)

	/**
	 * @see NacNotification.channelDescription
	 */
	override val channelDescription: String
		get() = context.getString(R.string.description_skipped_alarm)

	/**
	 * @see NacNotification.title
	 */
	override val title: String
		get() = "<b>${context.resources.getQuantityString(R.plurals.skipped_alarm, 1)}</b>"

	/**
	 * @see NacNotification.priority
	 */
	override val priority: Int
		get() = NotificationCompat.PRIORITY_LOW

	/**
	 * @see NacNotification.importance
	 */
	override val importance: Int
		get() = NotificationManagerCompat.IMPORTANCE_LOW

	/**
	 * @see NacNotification.group
	 */
	override val group: String
		get() = "NacNotiGroupSkipAlarm"

	/**
	 * @see NacNotification.contentText
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
	 * @see NacNotification.contentPendingIntent
	 */
	override val contentPendingIntent: PendingIntent
		get() = NacMainActivity.getStartPendingIntent(context)

	/**
	 * @see NacNotification.builder
	 */
	public override fun builder(): NotificationCompat.Builder
	{
		// Build the notification
		return super.builder()
			.setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_DEFAULT)
			.setAutoCancel(true)
			.setOngoing(false)
			.setShowWhen(true)
			.setSound(null)
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
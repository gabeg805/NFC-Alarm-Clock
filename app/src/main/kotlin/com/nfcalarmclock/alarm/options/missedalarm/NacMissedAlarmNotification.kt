package com.nfcalarmclock.alarm.options.missedalarm

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
import com.nfcalarmclock.view.notification.NacNotification

/**
 */
class NacMissedAlarmNotification(

	/**
	 * Context.
	 */
	context: Context,

	/**
	 * The alarm to show the notification about.
	 */
	private val alarm: NacAlarm

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
		get() = "NacNotiChannelMissed"

	/**
	 * @see NacNotification.channelName
	 */
	override val channelName: String
		get() = context.getString(R.string.title_missed_alarms)

	/**
	 * @see NacNotification.channelDescription
	 */
	override val channelDescription: String
		get() = context.getString(R.string.description_missed_alarm)

	/**
	 * @see NacNotification.title
	 */
	override val title: String
		get()
		{
			// Get the title
			val missedAlarm = context.resources.getQuantityString(R.plurals.missed_alarm, lineCount, lineCount)

			// Format the title
			return "<b>${missedAlarm.replace("$lineCount ", "")}</b>"
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
		get() = NotificationManagerCompat.IMPORTANCE_DEFAULT

	/**
	 * @see NacNotification.group
	 */
	override val group: String
		get() = "NacNotiGroupMissed"

	/**
	 * @see NacNotification.contentText
	 */
	override val contentText: String
		get()
		{
			val alarmPlural = context.resources.getQuantityString(R.plurals.alarm, body.size, body.size)

			// Check if the body has stuff present
			return if (body.isNotEmpty())
			{
				alarmPlural
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
		get() = NacMainActivity.getStartPendingIntent(context)

	/**
	 * @see NacNotification.builder
	 */
	override fun builder(): NotificationCompat.Builder
	{
		// Create a notification with an inbox style
		val inbox = NotificationCompat.InboxStyle()

		// Iterate over each line in the body
		for (line in body)
		{
			inbox.addLine(line)
		}

		// Build the notification
		return super.builder()
			.setGroupSummary(true)
			.setAutoCancel(true)
			.setShowWhen(true)
			.setStyle(inbox)
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

	/**
	 * Setup the notification body lines.
	 */
	private fun setupBody()
	{
		// Get the lines from the notification
		val newBody = getExtraLines(context, group)

		// Determine the new line to add
		val line = getBodyLine(alarm)

		// Add new line to notification
		newBody.add(line)

		// Set the new body
		body = newBody
	}

	/**
	 * @see NacNotification.show
	 */
	public override fun show()
	{
		// Used to call cancel() if size was 0. Might not have to because
		// show() should cancel it anyway
		setupBody()

		super.show()
	}

	companion object
	{

		/**
		 * Notification ID.
		 */
		const val ID = 222

	}

}
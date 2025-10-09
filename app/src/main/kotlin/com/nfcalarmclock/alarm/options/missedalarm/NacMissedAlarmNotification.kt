package com.nfcalarmclock.alarm.options.missedalarm

import android.app.NotificationChannel
import android.app.NotificationManager
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
	public override val id: Int
		get() = ID

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
	 * @see NacBaseNotificationBuilder.contentPendingIntent
	 */
	override val contentPendingIntent: PendingIntent
		get() = NacMainActivity.getStartPendingIntent(context)

	/**
	 * Body text of the notification.
	 */
	private var body: List<CharSequence> = emptyList()

	/**
	 * Constructor.
	 */
	init
	{
		// Create the channel
		setupChannel()

		// Get the title
		val missedAlarm = context.resources.getQuantityString(R.plurals.missed_alarm, body.size, body.size)
		val title = "<b>${missedAlarm.replace("${body.size}", "")}</b>"

		// Create a notification with an inbox style and add each line in the body
		val inbox = NotificationCompat.InboxStyle()

		for (line in body)
		{
			inbox.addLine(line)
		}

		// Used to call cancel() if size was 0. Might not have to because
		// show() should cancel it anyway
		setupBody()

		// Build the notification
		this.setPriority(priorityLevel)
			.setCategory(category)
			.setGroup(group)
			.setContentTitle(title.toSpannedString())
			.setContentText(contentText)
			.setContentIntent(contentPendingIntent)
			.setSmallIcon(smallIcon)
			.setTicker(channelName)
			.setColor(ContextCompat.getColor(context, R.color.ic_launcher_background))
			.setGroupSummary(true)
			.setAutoCancel(true)
			.setShowWhen(true)
			.setStyle(inbox)
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

	/**
	 * Get a line that can appear in the body text of the notification.
	 *
	 * @return A line that can appear in the body text of the notification.
	 */
	private fun getBodyLine(alarm: NacAlarm): String
	{
		val cal = NacCalendar.alarmToCalendar(alarm)
		val time = NacCalendar.getFullTime(context, cal)

		// Check if the alarm name is empty
		return if (alarm.name.isEmpty())
		{
			// The line in the notification will just be the time
			time
		}
		else
		{
			// The line in the notification will be the time and the name of the
			// alarm
			"$time  —  ${alarm.name}"
		}
	}

	/**
	 * Get a list of notification lines.
	 *
	 * @return A list of notification lines.
	 */
	private fun getExtraLines(context: Context, groupKey: String?): MutableList<CharSequence>
	{
		// Create a list of lines
		val lines: MutableList<CharSequence> = ArrayList()

		// Get the notification manager
		val manager = context.getSystemService(Context.NOTIFICATION_SERVICE)
				as NotificationManager

		// Get the active notifications in the status bar
		val statusbar = manager.activeNotifications

		// Iterate over each notification
		for (sb in statusbar)
		{
			val notification = sb.notification
			val sbGroup = notification.group

			// Check that the groups match
			if ((groupKey != null) && (groupKey == sbGroup))
			{
				val extraLines = notification.extras.getCharSequenceArray(
					NotificationCompat.EXTRA_TEXT_LINES)

				// Check that the extra lines are not null
				if (extraLines != null)
				{
					lines.addAll(listOf(*extraLines))
				}
			}
		}

		return lines
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

	//public override fun show()
	//{
	//	// Used to call cancel() if size was 0. Might not have to because
	//	// show() should cancel it anyway
	//	setupBody()

	//	super.show()
	//}

	companion object
	{

		/**
		 * Notification ID.
		 */
		const val ID = 222

	}

}
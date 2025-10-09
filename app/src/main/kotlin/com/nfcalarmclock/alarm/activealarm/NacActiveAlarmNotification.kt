package com.nfcalarmclock.alarm.activealarm

import android.app.NotificationChannel
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.media3.common.util.UnstableApi
import com.nfcalarmclock.R
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.alarm.options.nfc.NacNfc
import com.nfcalarmclock.shared.NacSharedPreferences
import com.nfcalarmclock.system.NacCalendar
import com.nfcalarmclock.view.notification.NacBaseNotificationBuilder
import com.nfcalarmclock.view.toSpannedString
import java.util.Calendar

/**
 * Active alarm notification.
 *
 * @param context Context.
 * @param alarm Alarm.
 */
class NacActiveAlarmNotification(
	context: Context,
	private val alarm: NacAlarm?
) : NacBaseNotificationBuilder(context, "NacNotiChannelActiveAlarm")
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
		get() = context.getString(R.string.title_active_alarms)

	/**
	 * @see NacBaseNotificationBuilder.channelDescription
	 */
	override val channelDescription: String
		get() = context.getString(R.string.description_active_alarm)

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
	override val group: String = "NacNotiGroupActiveAlarm"

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
		get()
		{
			val id = alarm?.id ?: 0
			val intent = NacActiveAlarmActivity.getStartIntent(context, alarm)

			// Determine the pending intent flags
			val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE

			// Return the pending intent for the activity
			return PendingIntent.getActivity(context, id.toInt(), intent, flags)
		}

	/**
	 * The pending intent to use when dismissing the alarm.
	 */
	private val dismissPendingIntent: PendingIntent
		@OptIn(UnstableApi::class)
		get()
		{
			// Create an intent to dismiss the active alarm service
			val intent = NacActiveAlarmService.getDismissIntent(context, alarm)

			// Determine the pending intent flags
			val flags = PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE

			// Create the pending intent
			return PendingIntent.getService(context, 0, intent, flags)
		}

	/**
	 * The pending intent to use when snoozing.
	 */
	private val snoozePendingIntent: PendingIntent
		@OptIn(UnstableApi::class)
		get()
		{
			val intent = NacActiveAlarmService.getSnoozeIntent(context, alarm)

			// Determine the pending intent flags
			val flags = PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE

			// Create the pending intent
			return PendingIntent.getService(context, 0, intent, flags)
		}

	/**
	 * Whether the alarm should use NFC or not.
	 */
	private val shouldUseNfc: Boolean
		get() = (alarm != null) && alarm.shouldUseNfc && NacSharedPreferences(context).shouldShowNfcButton

	/**
	 * Constructor.
	 */
	init
	{
		// Get shared preferences
		val sharedPreferences = NacSharedPreferences(context)

		// Create the channel
		setupChannel()

		// Get the title
		val appName = context.getString(R.string.app_name)

		// Build the notification
		// Note: Added the parentheses so that the custom addAction() can be called
		(this.setPriority(priorityLevel)
			.setCategory(category)
			.setGroup(group)
			.setContentTitle("<b>$appName</b>".toSpannedString())
			.setContentText(contentText)
			.setContentIntent(contentPendingIntent)
			.setSmallIcon(smallIcon)
			.setTicker(channelName)
			.setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
			.setColor(ContextCompat.getColor(context, R.color.ic_launcher_background))
			.setAutoCancel(false)
			.setOngoing(true)
			.setShowWhen(true)
			.setSound(null) as NacBaseNotificationBuilder)
			.addAction(R.drawable.snooze, R.string.action_alarm_snooze, snoozePendingIntent)
			.apply {
				// Check if NFC does not need to be used to dismiss the alarm
				// Note: This evaluates to False on the emulator because the emulator
				// is unable to use NFC
				if (!NacNfc.exists(context) || !shouldUseNfc)
				{
					// Add the dismiss button to the notification
					addAction(R.drawable.dismiss, R.string.action_alarm_dismiss, dismissPendingIntent)
				}

				// Check if battery saver option is disabled
				if (!sharedPreferences.shouldSaveBatteryInAlarmScreen)
				{
					setFullScreenIntent(contentPendingIntent, true)
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
		channel.setSound(null, null)

		return channel
	}

	companion object
	{

		/**
		 * Notification ID.
		 */
		const val ID = 69

	}

}
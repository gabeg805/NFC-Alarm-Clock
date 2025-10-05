package com.nfcalarmclock.alarm.activealarm

import android.app.NotificationChannel
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.media3.common.util.UnstableApi
import com.nfcalarmclock.R
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.alarm.options.nfc.NacNfc
import com.nfcalarmclock.shared.NacSharedPreferences
import com.nfcalarmclock.system.NacCalendar
import com.nfcalarmclock.view.notification.NacNotification
import java.util.Calendar

/**
 * Active alarm notification.
 */
class NacActiveAlarmNotification(

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
		get() = "NacNotiChannelActiveAlarm"

	/**
	 * @see NacNotification.channelName
	 */
	override val channelName: String
		get() = context.getString(R.string.title_active_alarms)

	/**
	 * @see NacNotification.channelDescription
	 */
	override val channelDescription: String
		get() = context.getString(R.string.description_active_alarm)

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
		get() = "NacNotiGroupActiveAlarm"

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
	 * Whether the alarm should use NFC or not.
	 */
	private val shouldUseNfc: Boolean
		get() = (alarm != null) && alarm.shouldUseNfc && NacSharedPreferences(context).shouldShowNfcButton

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
	 * Name of the app.
	 */
	private val appName = context.getString(R.string.app_name)

	/**
	 * @see NacNotification.builder
	 */
	public override fun builder(): NotificationCompat.Builder
	{
		// Get shared preferences
		val sharedPreferences = NacSharedPreferences(context)

		// Notification actions
		val dismiss = context.getString(R.string.action_alarm_dismiss)
		val snooze = context.getString(R.string.action_alarm_snooze)

		// Build the notification
		var builder = super.builder()
			.setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
			.setAutoCancel(false)
			.setOngoing(true)
			.setShowWhen(true)
			.setSound(null)
			.addAction(R.drawable.snooze, snooze, snoozePendingIntent)

		// Check if battery saver option is disabled
		if (!sharedPreferences.shouldSaveBatteryInAlarmScreen)
		{
			builder = builder.setFullScreenIntent(contentPendingIntent, true)
		}

		// Check if NFC does not need to be used to dismiss the alarm
		// Note: This evaluates to False on the emulator because the emulator
		// is unable to use NFC
		if (!NacNfc.exists(context) || !shouldUseNfc)
		{
			// Add the dismiss button to the notification
			builder = builder.addAction(R.drawable.dismiss, dismiss, dismissPendingIntent)
		}

		// Return the builder
		return builder
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
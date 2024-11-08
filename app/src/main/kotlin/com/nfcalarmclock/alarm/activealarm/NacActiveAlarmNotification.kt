package com.nfcalarmclock.alarm.activealarm

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
import com.nfcalarmclock.alarm.options.nfc.NacNfc
import com.nfcalarmclock.shared.NacSharedPreferences
import com.nfcalarmclock.util.NacCalendar
import com.nfcalarmclock.view.notification.NacNotification
import java.util.Calendar
import java.util.Locale

/**
 * Notification to display for active alarms.
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
		get() = context.getString(R.string.title_active_alarm)

	/**
	 * @see NacNotification.channelDescription
	 */
	override val channelDescription: String
		get() = context.getString(R.string.description_active_alarm)

	/**
	 * @see NacNotification.title
	 */
	override val title: String
		get()
		{
			val locale = Locale.getDefault()

			return String.format(locale, "<b>$appName</b>")
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
		get() = "NacNotiGroupActiveAlarm"

	/**
	 * @see NacNotification.contentText
	 */
	override val contentText: String
		get()
		{
			// Get the full time
			val now = Calendar.getInstance()
			val is24HourFormat = DateFormat.is24HourFormat(context)
			val time = NacCalendar.getFullTime(now, is24HourFormat)

			// Get the alarm name
			val name = alarm?.name ?: ""

			return if (name.isEmpty())
			{
				time
			}
			else
			{
				val locale = Locale.getDefault()

				// Format the string
				String.format(locale, "$time  —  $name")
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
			var flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE

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
			val intent = NacActiveAlarmService.getDismissIntent(context, alarm)

			// Determine the pending intent flags
			var flags = PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE

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
		get()
		{
			val intent = NacActiveAlarmService.getSnoozeIntent(context, alarm)

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
	private val appName = context.getString(R.string.app_name)

	/**
	 * @see NacNotification.builder
	 */
	public override fun builder(): NotificationCompat.Builder
	{
		// Notification actions
		val dismiss = context.getString(R.string.action_alarm_dismiss)
		val snooze = context.getString(R.string.action_alarm_snooze)

		// Build the notification
		var builder = super.builder()
			.setLargeIcon(largeIcon)
			.setFullScreenIntent(contentPendingIntent, true)
			.setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
			.setAutoCancel(false)
			.setOngoing(true)
			.setShowWhen(true)
			.setTicker(appName)
			.addAction(R.drawable.snooze, snooze, snoozePendingIntent)

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
		 * Notification ID.
		 */
		const val ID = 79

	}

}
package com.nfcalarmclock.view.notification

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.nfcalarmclock.R
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.system.NacCalendar
import com.nfcalarmclock.view.toSpannedString

/**
 * Notification for the app to keep it in memory.
 */
abstract class NacNotification(

	/**
	 * Context.
	 */
	protected val context: Context
)
{

	/**
	 * Notification ID.
	 */
	protected abstract val id: Int

	/**
	 * Channel ID.
	 */
	protected abstract val channelId: String

	/**
	 * Channel name.
	 */
	protected abstract val channelName: String

	/**
	 * Channel description.
	 */
	protected abstract val channelDescription: String

	/**
	 * Notification title.
	 */
	abstract val title: String

	/**
	 * Priority level of the notification.
	 */
	protected abstract val priority: Int

	/**
	 * Importance level of the notification.
	 */
	protected abstract val importance: Int

	/**
	 * Group name.
	 */
	protected abstract val group: String

	/**
	 * Context text when building the notification.
	 */
	protected abstract val contentText: String

	/**
	 * Context pending intent when building the notification.
	 */
	protected abstract val contentPendingIntent: PendingIntent

	/**
	 * The notification manager.
	 */
	private val notificationManager: NotificationManagerCompat
		get() = NotificationManagerCompat.from(context)

	/**
	 * Category of the notification.
	 */
	private val category: String = NotificationCompat.CATEGORY_ALARM

	/**
	 * Body text of the notification.
	 */
	protected var body: List<CharSequence> = emptyList()

	/**
	 * The small icon of the notification.
	 */
	private val smallIcon: Int = R.drawable.alarm

	/**
	 * Get the number of lines in the body.
	 */
	protected val lineCount: Int
		get() = body.size

	/**
	 * Check if should create channel by checking if the API level >= 26.
	 */
	private val shouldCreateChannel: Boolean
		get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O

	/**
	 * Constructor.
	 */
	init
	{
		setupChannel()
	}

	/**
	 * Build the notification.
	 */
	protected open fun builder(): NotificationCompat.Builder
	{
		// Create the builder
		var builder = NotificationCompat.Builder(context, channelId)
			.setGroup(group)
			.setContentIntent(contentPendingIntent)
			.setContentText(contentText)
			.setColor(ContextCompat.getColor(context, R.color.ic_launcher_background))
			.setSmallIcon(smallIcon)
			.setPriority(priority)
			.setCategory(category)
			.setTicker(channelName)

		// Check if title should be added
		if (title.isNotEmpty())
		{
			builder = builder.setContentTitle(title.toSpannedString())
		}

		// Return the builder
		return builder
	}

	/**
	 * Create the notification channel.
	 */
	@RequiresApi(Build.VERSION_CODES.O)
	protected open fun createChannel(): NotificationChannel
	{
		// Create the channel
		val channel = NotificationChannel(channelId, channelName, importance)

		// Basic setup of the channel
		channel.description = channelDescription
		channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE

		return channel
	}

	/**
	 * Check if the given group key matches that of the notification.
	 *
	 * @return True if the given group key matches that of the notification, and
	 *         False otherwise.
	 */
	@Suppress("unused")
	protected fun doesGroupMatch(groupKey: String): Boolean
	{
		return group == groupKey
	}

	/**
	 * Get a line that can appear in the body text of the notification.
	 *
	 * @return A line that can appear in the body text of the notification.
	 */
	protected fun getBodyLine(alarm: NacAlarm): String
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
	 * Setup the notification channel.
	 */
	private fun setupChannel()
	{
		// Check if should create channel
		if (!shouldCreateChannel)
		{
			return
		}

		// Create the channel
		val channel = createChannel()

		// Tell the notification manager to create the channel
		notificationManager.createNotificationChannel(channel)
	}

	/**
	 * Show the notification.
	 */
	protected open fun show()
	{
		// Build the notification
		val builder = builder()

		// Check that the app is able to post notification on APK >= 33
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
		{
			if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
				!= PackageManager.PERMISSION_GRANTED)
			{
				// TODO: Consider calling ActivityCompat#requestPermissions
				// here to request the missing permissions, and then overriding
				//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
				//                                          int[] grantResults)
				// to handle the case where the user grants the permission. See the documentation
				// for ActivityCompat#requestPermissions for more details.
				return
			}
		}

		// Show the notification
		notificationManager.notify(id, builder.build())
	}

	companion object
	{

		/**
		 * Get a list of notification lines.
		 *
		 * @return A list of notification lines.
		 */
		fun getExtraLines(context: Context, groupKey: String?): MutableList<CharSequence>
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

	}

}
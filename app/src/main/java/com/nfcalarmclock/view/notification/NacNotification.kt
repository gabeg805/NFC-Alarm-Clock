package com.nfcalarmclock.view.notification

import android.Manifest
import android.annotation.TargetApi
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.nfcalarmclock.R
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.util.NacUtility.toSpannedString
import java.util.Arrays
import java.util.Locale

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
	private val smallIcon: Int
		get() = R.mipmap.notification

	/**
	 * Get the number of lines in the body.
	 *
	 * TODO: See if this can be 0 instead of 1.
	 *
	 * @return Number of lines in the body.
	 */
	protected val lineCount: Int
		get() = body.size

	/**
	 * Check if should create channel by checkin if the API level < 26.
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
	@TargetApi(Build.VERSION_CODES.N)
	protected open fun builder(): NotificationCompat.Builder
	{
		return NotificationCompat.Builder(context, channelId)
			.setGroup(group)
			.setContentIntent(contentPendingIntent)
			.setContentTitle(toSpannedString(title))
			.setContentText(contentText)
			.setSmallIcon(smallIcon)
			.setPriority(priority)
			.setCategory(category)
	}

	/**
	 * Cancel a previously shown notification.
	 */
	fun cancel()
	{
		notificationManager.cancel(id)
	}

	/**
	 * Create the notification channel.
	 */
	@TargetApi(Build.VERSION_CODES.O)
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
		val time = alarm.getFullTime(context)
		val name = alarm.name
		val locale = Locale.getDefault()

		// Check if the alarm name is empty
		return if (name.isEmpty())
			{
				// The line in the notification will just be the time
				time
			}
			else
			{
				// The line in the notification will be the time and the name of the
				// alarm
				String.format(locale, "%1\$s  —  %2\$s", time, name)
			}
	}

	/**
	 * Setup the notification body lines.
	 */
	@Suppress("unused")
	protected open fun setupBody()
	{
	}

	/**
	 * Setup the notification channel.
	 */
	private fun setupChannel()
	{
		// Check if should create channel
		if (!shouldCreateChannel)
		{
			println("Will not create channel")
			return
		}

		// Create the channel
		println("Create channel")
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
				// TODO: Consider calling
				//    ActivityCompat#requestPermissions
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
		@TargetApi(Build.VERSION_CODES.M)
		fun getExtraLines(context: Context, groupKey: String?): MutableList<CharSequence>
		{
			// Create a list of lines
			val lines: MutableList<CharSequence> = ArrayList()

			// Check if API < 23
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
			{
				return lines
			}

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
						lines.addAll(Arrays.asList(*extraLines))
					}
				}
			}

			return lines
		}

	}

}
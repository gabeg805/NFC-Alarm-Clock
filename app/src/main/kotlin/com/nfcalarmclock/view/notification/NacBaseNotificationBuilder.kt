package com.nfcalarmclock.view.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.nfcalarmclock.R

/**
 * General notification builder.
 *
 * @param context Context.
 * @param channelId Channel ID.
 */
abstract class NacBaseNotificationBuilder(
	val context: Context,
	val channelId: String
) : NotificationCompat.Builder(context, channelId)
{

	/**
	 * Notification ID.
	 */
	protected abstract val id: Int

	/**
	 * Channel name.
	 */
	protected abstract val channelName: String

	/**
	 * Channel description.
	 */
	protected abstract val channelDescription: String

	/**
	 * Importance level of the notification.
	 */
	protected abstract val channelImportance: Int

	/**
	 * Priority level of the notification.
	 */
	protected abstract val priorityLevel: Int

	/**
	 * Category of the notification.
	 */
	protected open val category: String = NotificationCompat.CATEGORY_ALARM

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
	 * The small icon of the notification.
	 */
	protected open val smallIcon: Int = R.drawable.alarm

	/**
	 * Add an action to the notification.
	 */
	open fun addAction(
		icon: Int,
		stringId: Int,
		intent: PendingIntent?
	): NacBaseNotificationBuilder
	{
		// Get the string
		val label = context.resources.getString(stringId)

		// Add the action
		return super.addAction(icon, label, intent) as NacBaseNotificationBuilder
	}

	/**
	 * Create the notification channel.
	 */
	@RequiresApi(Build.VERSION_CODES.O)
	protected open fun createChannel(): NotificationChannel
	{
		//println("Id : $channelId | Name : $channelName | Import : $channelImportance")
		// Create the channel
		val channel = NotificationChannel(channelId, channelName, channelImportance)

		// Basic setup of the channel
		channel.description = channelDescription
		channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE

		return channel
	}

	/**
	 * Setup the notification channel.
	 */
	protected fun setupChannel()
	{
		// Channel only needs to be created on API level >= 26.
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
		{
			return
		}

		// Build the channel
		val notificationManager = NotificationManagerCompat.from(context)
		val channel = createChannel()

		// Create the channel
		notificationManager.createNotificationChannel(channel)
	}

}
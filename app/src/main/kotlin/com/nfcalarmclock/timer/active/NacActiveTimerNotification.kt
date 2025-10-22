package com.nfcalarmclock.timer.active

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
import androidx.navigation.NavDeepLinkBuilder
import com.nfcalarmclock.R
import com.nfcalarmclock.main.NacMainActivity
import com.nfcalarmclock.system.toBundle
import com.nfcalarmclock.timer.db.NacTimer
import com.nfcalarmclock.view.notification.NacBaseNotificationBuilder

/**
 * Active timer notification.
 *
 * @param context Context.
 * @param timer Timer.
 */
class NacActiveTimerNotification(
	context: Context,
	private val timer: NacTimer?
) : NacBaseNotificationBuilder(context, "NacNotiChannelActiveTimer")
{

	/**
	 * @see NacBaseNotificationBuilder.id
	 */
	public override val id: Int
		get() = BASE_ID + (timer?.id?.toInt() ?: 0)

	/**
	 * @see NacBaseNotificationBuilder.channelName
	 */
	override val channelName: String = context.getString(R.string.title_active_timers)

	/**
	 * @see NacBaseNotificationBuilder.channelDescription
	 */
	override val channelDescription: String = context.getString(R.string.description_active_timer)

	/**
	 * @see NacBaseNotificationBuilder.channelImportance
	 */
	override val channelImportance: Int = NotificationManagerCompat.IMPORTANCE_DEFAULT

	/**
	 * @see NacBaseNotificationBuilder.priorityLevel
	 */
	override val priorityLevel: Int = NotificationCompat.PRIORITY_DEFAULT

	/**
	 * @see NacBaseNotificationBuilder.group
	 */
	override val group: String = "NacNotiGroupActiveTimer"

	/**
	 * @see NacBaseNotificationBuilder.contentText
	 */
	override val contentText: String
		get()
		{
			// Name
			return if (timer!!.name.isNotEmpty())
			{
				timer.name
			}
			// Generic "Timer"
			else
			{
				context.resources.getString(R.string.word_timer)
			}
		}

	/**
	 * @see NacBaseNotificationBuilder.contentPendingIntent
	 */
	override val contentPendingIntent: PendingIntent
		get()
		{
			return NavDeepLinkBuilder(context.applicationContext)
				.setComponentName(NacMainActivity::class.java)
				.setGraph(R.navigation.nav_main_fragments)
				.addDestination(R.id.nacShowTimersFragment)
				.addDestination(R.id.nacActiveTimerFragment)
				.setArguments(timer!!.toBundle())
				.createPendingIntent()
		}

	/**
	 * The pending intent to dismiss the timer.
	 */
	val dismissPendingIntent: PendingIntent
		@OptIn(UnstableApi::class)
		get()
		{
			// Create the intent and get the pending intent flags
			val intent = NacActiveTimerService.getDismissIntent(context, timer)
			val flags = PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE

			// Create the pending intent
			return PendingIntent.getService(context, 0, intent, flags)
		}

	/**
	 * The pending intent to pause the timer.
	 */
	val pausePendingIntent: PendingIntent
		@OptIn(UnstableApi::class)
		get()
		{
			// Create an intent to pause the active timer service
			val intent = NacActiveTimerService.getPauseIntent(context, timer)

			// Determine the pending intent flags
			val flags = PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE

			// Create the pending intent
			return PendingIntent.getService(context, 0, intent, flags)
		}

	/**
	 * The pending intent to resume the timer.
	 */
	val resumePendingIntent: PendingIntent
		@OptIn(UnstableApi::class)
		get()
		{
			// Create an intent to resume the active timer service
			val intent = NacActiveTimerService.getResumeIntent(context, timer)

			// Determine the pending intent flags
			val flags = PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE

			// Create the pending intent
			return PendingIntent.getService(context, 0, intent, flags)
		}

	/**
	 * @see NacBaseNotificationBuilder.smallIcon
	 */
	override val smallIcon: Int = R.drawable.hourglass_empty_32

	/**
	 * Constructor.
	 */
	init
	{
		// Create the channel
		setupChannel()

		// Build the notification
		this.setPriority(priorityLevel)
			.setCategory(category)
			.setGroup(group)
			.setContentText(contentText)
			.setContentIntent(contentPendingIntent)
			.setSmallIcon(smallIcon)
			.setTicker(channelName)
			.setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_DEFAULT)
			.setColor(ContextCompat.getColor(context, R.color.ic_launcher_background))
			.setAutoCancel(false)
			.setOngoing(true)
			.setOnlyAlertOnce(true)
			.setShowWhen(true)
			.setSound(null)
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
		channel.enableVibration(false)
		channel.setSound(null, null)

		return channel
	}

	companion object
	{

		/**
		 * Notification ID.
		 */
		const val BASE_ID = 1069

	}

}
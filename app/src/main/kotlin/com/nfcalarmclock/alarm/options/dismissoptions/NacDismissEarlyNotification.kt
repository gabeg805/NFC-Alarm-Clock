package com.nfcalarmclock.alarm.options.dismissoptions

import android.app.NotificationChannel
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavDeepLinkBuilder
import com.nfcalarmclock.R
import com.nfcalarmclock.alarm.NacShowAlarmsFragment
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.main.NacMainActivity
import com.nfcalarmclock.system.NacBundle.BUNDLE_INTENT_ACTION
import com.nfcalarmclock.system.NacCalendar
import com.nfcalarmclock.system.toBundle
import com.nfcalarmclock.view.notification.NacBaseNotificationBuilder
import com.nfcalarmclock.view.toSpannedString
import java.util.Calendar

/**
 * Dismiss an alarm early notification.
 *
 * @param context Context.
 * @param alarm Alarm.
 */
class NacDismissEarlyNotification(
	context: Context,
	private val alarm: NacAlarm?
) : NacBaseNotificationBuilder(context, "NacNotiChannelDismissEarly")
{

	/**
	 * @see NacBaseNotificationBuilder.id
	 */
	public override val id: Int
		get() = BASE_ID + (alarm?.id?.toInt() ?: 0)

	/**
	 * @see NacBaseNotificationBuilder.channelName
	 */
	override val channelName: String = context.getString(R.string.title_dismiss_options_dismiss_early)

	/**
	 * @see NacBaseNotificationBuilder.channelDescription
	 */
	override val channelDescription: String = context.getString(R.string.description_dismiss_options_dismiss_early)

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
	override val group: String = "NacNotiGroupDismissEarly"

	/**
	 * @see NacBaseNotificationBuilder.contentText
	 */
	override val contentText: String
		get()
		{
			// Get the calendar of the alarm
			val cal = if (alarm != null)
			{
				NacCalendar.getNextAlarmDay(alarm)!!
			}
			else
			{
				Calendar.getInstance()
			}

			// Get the full time from the calendar
			val time = NacCalendar.getFullTime(context, cal)

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
			return NavDeepLinkBuilder(context.applicationContext)
				.setComponentName(NacMainActivity::class.java)
				.setGraph(R.navigation.nav_main_fragments)
				.setDestination(R.id.nacShowAlarmsFragment)
				.setArguments(alarm!!.toBundle())
				.createPendingIntent()
		}

	/**
	 * The pending intent to use when dismissing the alarm early.
	 */
	private val dismissPendingIntent: PendingIntent
		get()
		{
			return NavDeepLinkBuilder(context.applicationContext)
				.setComponentName(NacMainActivity::class.java)
				.setGraph(R.navigation.nav_main_fragments)
				.setDestination(R.id.nacShowAlarmsFragment)
				.setArguments(alarm!!.toBundle()
					.apply {
						putString(BUNDLE_INTENT_ACTION, NacShowAlarmsFragment.ACTION_DISMISS_ALARM_EARLY)
					})
				.createPendingIntent()
		}

	/**
	 * Constructor.
	 */
	init
	{
		// Create the channel
		setupChannel()

		// Build the notification
		// Note: Added the parentheses so that the custom addAction() can be called
		(this.setPriority(priorityLevel)
			.setCategory(category)
			.setGroup(group)
			.setContentTitle("<b>$channelName</b>".toSpannedString())
			.setContentText(contentText)
			.setContentIntent(contentPendingIntent)
			.setSmallIcon(smallIcon)
			.setTicker(channelName)
			.setColor(ContextCompat.getColor(context, R.color.ic_launcher_background))
			.setAutoCancel(true)
			.setOngoing(false)
			.setShowWhen(true) as NacBaseNotificationBuilder)
			.addAction(R.drawable.dismiss, R.string.action_alarm_dismiss, dismissPendingIntent)
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

	companion object
	{

		/**
		 * Notification ID.
		 */
		const val BASE_ID = 333

	}

}
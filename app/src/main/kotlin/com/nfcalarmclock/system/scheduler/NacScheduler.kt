package com.nfcalarmclock.system.scheduler

import android.app.AlarmManager
import android.app.AlarmManager.AlarmClockInfo
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import com.nfcalarmclock.alarm.activealarm.NacActiveAlarmBroadcastReceiver
import com.nfcalarmclock.alarm.activealarm.NacActiveAlarmService
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.alarm.options.dismissoptions.NacDismissEarlyService
import com.nfcalarmclock.alarm.options.upcomingreminder.NacUpcomingReminderService
import com.nfcalarmclock.main.NacMainActivity
import com.nfcalarmclock.util.NacCalendar
import java.util.Calendar
import kotlin.random.Random

/**
 * The alarm scheduler.
 */
object NacScheduler
{

	/**
	 * Add all alarm days to the scheduler.
	 */
	fun add(context: Context, alarm: NacAlarm?)
	{
		// Check if the alarm is null or not enabled
		if (alarm?.isEnabled != true)
		{
			return
		}

		// Get the calendar for the next alarm
		val nextAlarmCal = if (alarm.date.isNotEmpty())
		{
			// Alarm has a date. Converting to a calendar is easy
			println("HELLO")
			NacCalendar.alarmToCalendar(alarm)
		}
		// TODO: FIGURE OUT WHAT TO DO HERE
		//else if (alarm.)
		//{
		//}
		else
		{
			// Get the calendar for the next alarm
			NacCalendar.getNextAlarmDay(alarm, ignoreSkip = true)!!
		}

		// Default ignore=false
		//println("Before : ${getDateTimeInstance().format(NacCalendar.getNextAlarmDay(alarm, ignoreSkip = true)!!.time)}")
		//println("After  : ${getDateTimeInstance().format(NacCalendar.getNextAlarmDay(alarm, ignoreSkip = false)!!.time)}")

		// Add the alarm
		addAlarm(context, alarm, nextAlarmCal)

		// Check if should show an upcoming reminder
		if (alarm.shouldShowReminder && !alarm.shouldSkipNextAlarm)
		{
			// Get the calendar for the first upcoming reminder
			val firstReminderCal = NacCalendar.getFirstAlarmUpcomingReminder(alarm, nextAlarmCal)

			// Add the upcoming reminder
			addUpcomingReminder(context, alarm, firstReminderCal)
		}

		// Check if should schedule a dismiss early notification
		if (alarm.canDismissEarly && alarm.shouldShowDismissEarlyNotification)
		{
			addDismissEarly(context, alarm)
		}
	}

	/**
	 * Add an alarm to the scheduler.
	 */
	private fun addAlarm(
		context: Context,
		alarm: NacAlarm,
		cal: Calendar)
	{
		// Operation to perform when the alarm goes off
		val pendingIntent = buildAddAlarmPendingIntent(context, alarm)

		// Check if the next alarm should be skipped
		if (alarm.shouldSkipNextAlarm)
		{
			println("SHOULD SKIP")
			// Schedule the notification, but not as an alarm so it does not show up in
			// the status bar tile
			val manager = getAlarmManager(context)
			manager.setExactAndAllowWhileIdle(AlarmManager.RTC, cal.timeInMillis, pendingIntent)
		}
		// Normal alarm
		else
		{
			// Schedule the alarm clock
			addToAlarmManager(context, cal, pendingIntent)
		}
	}

	/**
	 * Add a dismiss early notification to the scheduler.
	 */
	fun addDismissEarly(context: Context, alarm: NacAlarm)
	{
		// Get the current calendar and the calendar when the next alarm will run
		val nextCal = NacCalendar.getNextAlarmDay(alarm)
		val now = Calendar.getInstance()

		// Get the time when the next alarm can be dismissed early
		val nextMillis = nextCal!!.timeInMillis
		var dismissMillis = nextMillis - alarm.dismissEarlyTime*60*1000
		val diffMillis = dismissMillis - now.timeInMillis

		// Check the time to make sure it is valid and not imminent
		if (diffMillis < 0)
		{
			// Imminent so do not schedule anything
			if ((nextMillis-now.timeInMillis) < 60000)
			{
				return
			}
			// Use the current time
			else
			{
				dismissMillis = now.timeInMillis
			}
		}

		// Create the intent
		val intent = NacDismissEarlyService.getStartIntent(context, alarm)

		// Build the pending intent
		val pendingIntent = buildServicePendingIntent(context, alarm, intent, PendingIntent.FLAG_CANCEL_CURRENT)!!

		// Schedule the notification, but not as an alarm so it does not show up in
		// the status bar tile
		val manager = getAlarmManager(context)
		manager.setExactAndAllowWhileIdle(AlarmManager.RTC, dismissMillis, pendingIntent)
	}

	/**
	 * Add an alarm calendar to the scheduler.
	 */
	private fun addToAlarmManager(
		context: Context,
		cal: Calendar,
		operationPendingIntent: PendingIntent)
	{
		// Time at which the alarm should go off
		val millis = cal.timeInMillis

		// Show the main activity
		val showPendingIntent = buildMainActivityPendingIntent(context)

		// Get the clock info and manager
		val clockInfo = AlarmClockInfo(millis, showPendingIntent)
		val manager = getAlarmManager(context)

		// Set the alarm
		manager.setAlarmClock(clockInfo, operationPendingIntent)
	}

	/**
	 * Add an upcoming reminder to the scheduler.
	 */
	fun addUpcomingReminder(
		context: Context,
		alarm: NacAlarm,
		reminderCal: Calendar)
	{
		// Get the current calendar and make a copy of the alarm calendar
		val now = Calendar.getInstance()

		// Check if the calendar for the upcoming reminder has already passed
		if (reminderCal.before(now))
		{
			// Do not schedule the upcoming reminder
			return
		}

		// Create the intent
		val intent = NacUpcomingReminderService.getStartIntent(context, alarm)

		// Build the pending intent
		val pendingIntent = buildServicePendingIntent(context, alarm, intent, PendingIntent.FLAG_CANCEL_CURRENT)!!

		// Schedule the notification, but not as an alarm so it does not show up in
		// the status bar tile
		val manager = getAlarmManager(context)
		manager.setExactAndAllowWhileIdle(AlarmManager.RTC, reminderCal.timeInMillis, pendingIntent)
	}

	/**
	 * Build the pending intent for adding an alarm.
	 *
	 * @return The pending intent for adding an alarm.
	 */
	@OptIn(UnstableApi::class)
	private fun buildAddAlarmPendingIntent(
		context: Context,
		alarm: NacAlarm
	): PendingIntent
	{
		// Create the intent
		val intent = if (alarm.shouldSkipNextAlarm)
		{
			NacActiveAlarmService.getSkipIntent(context, alarm)
		}
		else
		{
			NacActiveAlarmService.getStartIntent(context, alarm)
		}

		// Build the pending intent
		return buildServicePendingIntent(context, alarm, intent,
			PendingIntent.FLAG_CANCEL_CURRENT)!!
	}

	/**
	 * Build the pending intent for canceling an alarm.
	 *
	 * @return The pending intent for canceling an alarm.
	 */
	@OptIn(UnstableApi::class)
	private fun buildCancelAlarmPendingIntent(
		context: Context,
		alarm: NacAlarm
	): PendingIntent?
	{
		// Create the intent
		val intent = NacActiveAlarmService.getStartIntent(context, null)

		// Build the pending intent
		return buildServicePendingIntent(context, alarm, intent,
			PendingIntent.FLAG_NO_CREATE)
	}

	/**
	 * Build the pending intent for canceling a skipped alarm.
	 *
	 * @return The pending intent for canceling a skipped alarm.
	 */
	@OptIn(UnstableApi::class)
	private fun buildCancelSkipPendingIntent(
		context: Context,
		alarm: NacAlarm
	): PendingIntent?
	{
		// Create the intent
		val intent = NacActiveAlarmService.getSkipIntent(context, null)

		// Build the pending intent
		return buildServicePendingIntent(context, alarm, intent,
			PendingIntent.FLAG_NO_CREATE)
	}

	/**
	 * Build the pending intent to launch the main activity.
	 *
	 * @return The pending intent to launch the main activity.
	 */
	private fun buildMainActivityPendingIntent(context: Context): PendingIntent?
	{
		// Get a random number to use for the ID
		val id = Random.nextInt(1, 500)

		// Get the flags
		val flags = PendingIntent.FLAG_IMMUTABLE

		// Create the intent
		val intent = Intent(context, NacMainActivity::class.java)

		// Build the pending intent
		return PendingIntent.getActivity(context, id, intent, flags)
	}

	/**
	 * Build the pending intent for an alarm.
	 *
	 * @return The pending intent for an alarm.
	 */
	private fun buildServicePendingIntent(
		context: Context,
		alarm: NacAlarm,
		intent: Intent,
		flags: Int
	): PendingIntent?
	{
		// Get the alarm ID
		val id = alarm.id.toInt()

		// Prepare the flags
		val intentFlags = flags or PendingIntent.FLAG_IMMUTABLE

		// Create the pending intent
		return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
		{
			PendingIntent.getForegroundService(context, id, intent, intentFlags)
		}
		else
		{
			PendingIntent.getService(context, id, intent, intentFlags)
		}
	}

	/**
	 * @see NacScheduler.cancel
	 */
	fun cancel(context: Context, alarm: NacAlarm?)
	{
		// Check if the alarm is null
		if (alarm == null)
		{
			return
		}

		// Cancel the alarm
		cancelAlarm(context, alarm)

		// Cancel the upcoming reminder
		cancelUpcomingReminder(context, alarm)

		// Cancel the dismiss early
		cancelDismissEarly(context, alarm)
	}

	/**
	 * Cancel an alarm.
	 */
	private fun cancelAlarm(context: Context, alarm: NacAlarm)
	{
		// Build the pending intent for the alarm
		val alarmPendingIntent = buildCancelAlarmPendingIntent(context, alarm)
		val skipPendingIntent = buildCancelSkipPendingIntent(context, alarm)

		// Check if the alarm pending intent can be canceled
		if (alarmPendingIntent != null)
		{
			// Cancel the alarm
			getAlarmManager(context).cancel(alarmPendingIntent)
		}

		// Check if the skipped alarm pending intent can be canceled
		if (skipPendingIntent != null)
		{
			// Cancel the skipped alarm
			getAlarmManager(context).cancel(skipPendingIntent)
		}
	}

	/**
	 * Cancel dismiss early.
	 */
	fun cancelDismissEarly(context: Context, alarm: NacAlarm)
	{
		// Create the intent
		val intent = NacDismissEarlyService.getStartIntent(context, null)

		// Build the pending intent
		val pendingIntent = buildServicePendingIntent(context, alarm, intent, PendingIntent.FLAG_NO_CREATE)

		// Check if the pending intent for the upcoming reminder is not null
		if (pendingIntent != null)
		{
			// Cancel the alarm
			getAlarmManager(context).cancel(pendingIntent)
		}
	}

	/**
	 * Cancel the old alarm type with a given ID.
	 *
	 * @param  context  Context.
	 * @param  id  Alarm ID.
	 */
	fun cancelOld(context: Context, id: Int)
	{
		// Prepare the flags
		val flags = PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE

		// Create the pending intent for the old type
		val intent = Intent(context, NacActiveAlarmBroadcastReceiver::class.java)
		val pending = PendingIntent.getBroadcast(context, id, intent, flags)

		// Cancel the alarm
		if (pending != null)
		{
			getAlarmManager(context).cancel(pending)
		}
	}

	/**
	 * Cancel the older alarm type with a given ID.
	 *
	 * @param  context  Context.
	 * @param  id  Alarm ID.
	 */
	private fun cancelOlder(context: Context, id: Int)
	{
		// Prepare the flags
		var flags = PendingIntent.FLAG_NO_CREATE

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
		{
			flags = flags or PendingIntent.FLAG_MUTABLE
		}

		// Create the pending intent for the old type
		val intent = Intent(context, NacActiveAlarmBroadcastReceiver::class.java)
		val pending = PendingIntent.getBroadcast(context, id, intent, flags)

		// Cancel the alarm
		if (pending != null)
		{
			getAlarmManager(context).cancel(pending)
		}
	}

	/**
	 * Cancel an upcoming reminder.
	 */
	fun cancelUpcomingReminder(context: Context, alarm: NacAlarm)
	{
		// Create the intent
		val intent = NacUpcomingReminderService.getStartIntent(context, null)

		// Build the pending intent
		val pendingIntent = buildServicePendingIntent(context, alarm, intent, PendingIntent.FLAG_NO_CREATE)

		// Check if the pending intent for the upcoming reminder is not null
		if (pendingIntent != null)
		{
			// Cancel the alarm
			getAlarmManager(context).cancel(pendingIntent)
		}
	}

	/**
	 * Get the AlarmManager.
	 *
	 * @return The AlarmManager.
	 */
	private fun getAlarmManager(context: Context): AlarmManager
	{
		return context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
	}

	/**
	 * Refresh all alarms.
	 */
	fun refreshAll(context: Context, alarms: List<NacAlarm>)
	{
		// Iterate over each alarm
		for (a in alarms)
		{
			val id = a.id.toInt()

			// Clear out the older alarms (Do not use IMMUTABLE flag)
			cancelOlder(context, id)

			// Clear out the old alarms (Use IMMUTABLE flag)
			cancelOld(context, id)

			// Clear out any new alarms, just in case
			cancel(context, a)

			// Add each alarm
			add(context, a)
		}
	}

	/**
	 * Update all days in a given alarm.
	 */
	fun update(context: Context, alarm: NacAlarm?)
	{
		// Cancel the alarm
		cancel(context, alarm)

		// Add the alarm
		add(context, alarm)
	}

	/**
	 * Update a single calendar in a given alarm.
	 */
	fun update(context: Context, alarm: NacAlarm, cal: Calendar)
	{
		// Cancel the alarm
		cancel(context, alarm)

		// Add the alarm. This will not add the reminder, but that is OK
		addAlarm(context, alarm, cal)
	}

	/**
	 * Update a list of alarms.
	 */
	fun updateAll(context: Context, alarms: List<NacAlarm>)
	{
		// Iterate over each alarm
		for (a in alarms)
		{
			// Update the alarm
			update(context, a)
		}
	}

}

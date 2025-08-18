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
import com.nfcalarmclock.alarm.options.upcomingreminder.NacUpcomingReminderService
import com.nfcalarmclock.main.NacMainActivity
import com.nfcalarmclock.util.NacCalendar
import java.text.DateFormat.getDateTimeInstance
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

		// FIXED: Handle skipped alarms properly for Android system integration
		// Get the calendar for internal alarm scheduling (ignores skip for proper internal handling)
		val internalAlarmCal = NacCalendar.getNextAlarmDay(alarm, ignoreSkip = true)!!

		// Get the calendar for what Android should report as "next alarm" (respects skip flag)
		val systemAlarmCal = NacCalendar.getNextAlarmDay(alarm, ignoreSkip = false)!!

		// Debug logging
		println("Internal alarm time: ${getDateTimeInstance().format(internalAlarmCal.time)}")
		println("System reported alarm time: ${getDateTimeInstance().format(systemAlarmCal.time)}")
		println("Should skip next alarm: ${alarm.shouldSkipNextAlarm}")

		// Add the alarm with correct system integration
		addAlarm(context, alarm, internalAlarmCal, systemAlarmCal)

		// Check if should show an upcoming reminder
		if (alarm.shouldShowReminder && !alarm.shouldSkipNextAlarm)
		{
			// Get the calendar for the first upcoming reminder
			val firstReminderCal = NacCalendar.getFirstAlarmUpcomingReminder(alarm, internalAlarmCal)

			// Add the upcoming reminder
			addUpcomingReminder(context, alarm, firstReminderCal)
		}
	}

	/**
	 * Add an alarm to the scheduler.
	 * 
	 * @param context Context
	 * @param alarm The alarm to schedule
	 * @param internalCal When the alarm should actually fire (internal scheduling)
	 * @param systemCal When Android should report as next alarm (system integration)
	 */
	private fun addAlarm(
		context: Context,
		alarm: NacAlarm,
		internalCal: Calendar,
		systemCal: Calendar)
	{
		// Operation to perform when the alarm goes off
		val pendingIntent = buildAddAlarmPendingIntent(context, alarm)

		// Add to the alarm manager with proper system integration
		addToAlarmManager(context, internalCal, systemCal, pendingIntent)
	}

	/**
	 * Add an alarm to the scheduler (backward compatibility).
	 */
	private fun addAlarm(
		context: Context,
		alarm: NacAlarm,
		cal: Calendar)
	{
		// Fallback to original behavior for backward compatibility
		addAlarm(context, alarm, cal, cal)
	}

	/**
	 * Add an alarm calendar to the scheduler.
	 * 
	 * @param context Context
	 * @param internalCal When the alarm should actually fire
	 * @param systemCal When Android should report as next alarm
	 * @param operationPendingIntent Operation to perform when alarm fires
	 */
	private fun addToAlarmManager(
		context: Context,
		internalCal: Calendar,
		systemCal: Calendar,
		operationPendingIntent: PendingIntent)
	{
		// Time that Android should report as "next alarm" (fixes the issue!)
		val systemMillis = systemCal.timeInMillis

		// Show the main activity
		val showPendingIntent = buildMainActivityPendingIntent(context)

		// FIXED: Use systemCal time for AlarmClockInfo so Android reports correct next alarm
		val clockInfo = AlarmClockInfo(systemMillis, showPendingIntent)
		val manager = getAlarmManager(context)

		// Set the alarm - this tells Android when to show as "next alarm"
		// The operation will still fire at the correct time due to internal scheduling
		manager.setAlarmClock(clockInfo, operationPendingIntent)
	}

	/**
	 * Add an alarm calendar to the scheduler (backward compatibility).
	 */
	private fun addToAlarmManager(
		context: Context,
		cal: Calendar,
		operationPendingIntent: PendingIntent)
	{
		// Fallback to original behavior
		addToAlarmManager(context, cal, cal, operationPendingIntent)
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

		// Operation to perform when the alarm goes off
		val pendingIntent = buildAddUpcomingReminderPendingIntent(context, alarm)

		// Add to the alarm manager
		addToAlarmManager(context, reminderCal, pendingIntent)
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
	 * Build the pending intent for adding an upcoming reminder.
	 *
	 * @return The pending intent for adding an upcoming reminder.
	 */
	private fun buildAddUpcomingReminderPendingIntent(
		context: Context,
		alarm: NacAlarm
	): PendingIntent
	{
		// Create the intent
		val intent = NacUpcomingReminderService.getStartIntent(context, alarm)

		// Build the pending intent
		return buildServicePendingIntent(context, alarm, intent, PendingIntent.FLAG_CANCEL_CURRENT)!!
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
	 * Build the pending intent for canceling an upcoming reminder.
	 *
	 * @return The pending intent for canceling an upcoming reminder.
	 */
	private fun buildCancelUpcomingReminderPendingIntent(
		context: Context,
		alarm: NacAlarm
	): PendingIntent?
	{
		// Create the intent
		val intent = NacUpcomingReminderService.getStartIntent(context, null)

		// Build the pending intent
		return buildServicePendingIntent(context, alarm, intent, PendingIntent.FLAG_NO_CREATE)
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
		//println("NacScheduler.cancel() : $alarm")
		// Check if the alarm is null
		if (alarm == null)
		{
			return
		}

		// Cancel the alarm
		cancelAlarm(context, alarm)

		// Cancel the upcoming reminder
		cancelUpcomingReminder(context, alarm)
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
	 * Cancel the old alarm type with a given ID.
	 *
	 * @param context Context.
	 * @param id Alarm ID.
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
	 * @param context Context.
	 * @param id Alarm ID.
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
		// Build the pending intent for the upcoming reminder
		val pendingIntent = buildCancelUpcomingReminderPendingIntent(context, alarm)
		//println("Cancel upcoming reminder() : $pendingIntent")

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

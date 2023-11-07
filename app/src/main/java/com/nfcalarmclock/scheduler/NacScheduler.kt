package com.nfcalarmclock.scheduler

import android.app.AlarmManager
import android.app.AlarmManager.AlarmClockInfo
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.nfcalarmclock.activealarm.NacActiveAlarmBroadcastReceiver
import com.nfcalarmclock.alarm.NacAlarmRepository
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.main.NacMainActivity
import com.nfcalarmclock.util.NacCalendar
import com.nfcalarmclock.util.NacIntent.createForegroundService
import java.util.Calendar

/**
 * The alarm scheduler.
 */
object NacScheduler
{

	/**
	 * Add an alarm day to the scheduler.
	 */
	fun add(context: Context, alarm: NacAlarm?, day: Calendar)
	{
		// Check if the alarm is null or not enabled
		if (alarm?.isEnabled == false)
		{
			return
		}

		// Time at which the alarm should go off
		val millis = day.timeInMillis

		// Show the main activity
		val showPendingIntent = buildMainActivityPendingIntent(context, alarm)

		// Operation to perform when the alarm goes off
		val operationPendingIntent = buildAlarmPendingIntent(context, alarm,
			PendingIntent.FLAG_CANCEL_CURRENT)

		// Get the clock info and manager
		val clockInfo = AlarmClockInfo(millis, showPendingIntent)
		val manager = getAlarmManager(context)

		// Set the alarm
		manager.setAlarmClock(clockInfo, operationPendingIntent)
	}

	/**
	 * Add all alarm days to the scheduler.
	 */
	fun add(context: Context, alarm: NacAlarm?)
	{
		// Check if the alarm is null or not enabled
		if (alarm?.isEnabled == false)
		{
			return
		}

		// Get the next alarm day
		val day = NacCalendar.getNextAlarmDay(alarm)

		// Add the alarm for that day
		add(context, alarm, day)
	}

	/**
	 * Build the pending intent for an alarm.
	 *
	 * @return The pending intent for an alarm.
	 */
	private fun buildAlarmPendingIntent(context: Context, id: Int, intent: Intent,
		flags: Int): PendingIntent?
	{
		// Prepare the flags
		val intentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
		{
			flags or PendingIntent.FLAG_IMMUTABLE
		}
		else
		{
			flags
		}

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
	 * Build the pending intent for an alarm.
	 *
	 * @return The pending intent for an alarm.
	 */
	private fun buildAlarmPendingIntent(context: Context, alarm: NacAlarm?,
		flags: Int): PendingIntent?
	{
		// Unable to build the pending intent because the alarm is null
		if (alarm == null)
		{
			return null
		}

		// Get the alarm ID
		val id = alarm.id.toInt()

		// Create the intent
		val intent = createForegroundService(context, alarm)

		// Build the pending intent
		return buildAlarmPendingIntent(context, id, intent, flags)
	}

	/**
	 * Build the pending intent to launch the main activity.
	 *
	 * @return The pending intent to launch the main activity.
	 */
	private fun buildMainActivityPendingIntent(context: Context, alarm: NacAlarm?): PendingIntent?
	{
		// Unable to build the pending intent because the alarm is null
		if (alarm == null)
		{
			return null
		}

		// Get the alarm ID
		val id = alarm.id.toInt()

		// Get the flags
		var flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
		{
			PendingIntent.FLAG_IMMUTABLE
		}
		else
		{
			0
		}

		// Create the intent
		val intent = Intent(context, NacMainActivity::class.java)

		// Build the pending intent
		return PendingIntent.getActivity(context, id, intent, flags)
	}

	/**
	 * Cancel the alarm with a given ID.
	 *
	 * @param  context  Context.
	 * @param  id  Alarm ID.
	 */
	fun cancel(context: Context, id: Int)
	{
		// Build the pending intent for the new type
		val intent = createForegroundService(context, null as NacAlarm?)
		val pending = buildAlarmPendingIntent(context, id, intent,
			PendingIntent.FLAG_NO_CREATE)

		// Check if the pending intent is not null
		if (pending != null)
		{
			// Cancel the alarm
			getAlarmManager(context).cancel(pending)
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
		cancel(context, alarm.id.toInt())
	}

	/**
	 * Cancel all active alarms.
	 */
	fun cancelAllActive(context: Context)
	{
		// Get the active alarms from the repository
		val repo = NacAlarmRepository(context)
		val activeAlarms = repo.activeAlarmsNow

		// Iterate over each active alarm
		for (a in activeAlarms)
		{
			// Dismiss the alarm
			a.dismiss()

			// Update the repo now that the alarm is no longer active
			repo.update(a)

			// Cancel the alarm
			cancel(context, a)
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
		var flags = PendingIntent.FLAG_NO_CREATE

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
		{
			flags = flags or PendingIntent.FLAG_IMMUTABLE
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
	fun refreshAll(context: Context)
	{
		// Get all alarms from the repository
		val repo = NacAlarmRepository(context)
		val alarms = repo.allAlarmsNow

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
	@JvmStatic
	fun update(context: Context, alarm: NacAlarm?)
	{
		// Cancel the alarm
		cancel(context, alarm)

		// Add the alarm
		add(context, alarm)
	}

	/**
	 * Update a single day in a given alarm.
	 */
	@JvmStatic
	fun update(context: Context, alarm: NacAlarm?, day: Calendar)
	{
		// Cancel the alarm
		cancel(context, alarm)

		// Add the alarm
		add(context, alarm, day)
	}

	/**
	 * Update all alarms.
	 */
	fun updateAll(context: Context)
	{
		// Get all alarms from the repository
		val repo = NacAlarmRepository(context)
		val alarms: List<NacAlarm?> = repo.allAlarmsNow

		// Update all alarms
		updateAll(context, alarms)
	}

	/**
	 * Update a list of alarms.
	 */
	private fun updateAll(context: Context, alarms: List<NacAlarm?>)
	{
		// Iterate over each alarm
		for (a in alarms)
		{
			// Update the alarm
			update(context, a)
		}
	}

}
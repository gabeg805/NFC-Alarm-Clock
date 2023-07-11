package com.nfcalarmclock.scheduler;

import android.app.AlarmManager;
import android.app.AlarmManager.AlarmClockInfo;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.nfcalarmclock.alarm.NacAlarm;
import com.nfcalarmclock.alarm.NacAlarmRepository;
import com.nfcalarmclock.activealarm.NacActiveAlarmBroadcastReceiver;
import com.nfcalarmclock.main.NacMainActivity;
import com.nfcalarmclock.system.NacCalendar;
import com.nfcalarmclock.system.NacIntent;

import java.util.Calendar;
import java.util.List;

/**
 * The alarm scheduler.
 */
public class NacScheduler
{

	/**
	 * Add an alarm day to the scheduler.
	 */
	public static void add(Context context, NacAlarm alarm, Calendar day)
	{
		if ((alarm == null) || !alarm.isEnabled())
		{
			return;
		}

		// Time at which the alarm should go off
		long millis = day.getTimeInMillis();

		// Show the main activity
		PendingIntent showPendingIntent = NacScheduler.buildMainActivityPendingIntent(
			context, alarm);

		// Operation to perform when the alarm goes off
		PendingIntent operationPendingIntent = NacScheduler.buildAlarmPendingIntent(
			context, alarm, PendingIntent.FLAG_CANCEL_CURRENT);

		// Set the alarm
		AlarmClockInfo clock = new AlarmClockInfo(millis, showPendingIntent);
		AlarmManager manager = NacScheduler.getAlarmManager(context);

		manager.setAlarmClock(clock, operationPendingIntent);
	}

	/**
	 * Add all alarm days to the scheduler.
	 */
	public static void add(Context context, NacAlarm alarm)
	{
		if ((alarm == null) || !alarm.isEnabled())
		{
			return;
		}

		Calendar day = NacCalendar.getNextAlarmDay(alarm);
		NacScheduler.add(context, alarm, day);
	}

	/**
	 * @return Build the pending intent for an alarm.
	 */
	public static PendingIntent buildAlarmPendingIntent(Context context, int id,
		Intent intent, int flags)
	{
		// Prepare the flags
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
		{
			flags |= PendingIntent.FLAG_IMMUTABLE;
		}

		// Create the pending intent
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
		{
			return PendingIntent.getForegroundService(context, id, intent, flags);
		}
		else
		{
			return PendingIntent.getService(context, id, intent, flags);
		}
	}

	/**
	 * @return Build the pending intent for an alarm.
	 */
	public static PendingIntent buildAlarmPendingIntent(Context context,
		NacAlarm alarm, int flags)
	{
		// Unable to build the pending intent because the alarm is null
		if (alarm == null)
		{
			return null;
		}

		// Get the alarm ID
		int id = (int) alarm.getId();

		// Create the intent
		Intent intent = NacIntent.createForegroundService(context, alarm);

		// Build the pending intent
		return NacScheduler.buildAlarmPendingIntent(context, id, intent, flags);
	}

	/**
	 * @return Build the pending intent to launch the main activity.
	 */
	public static PendingIntent buildMainActivityPendingIntent(Context context,
		NacAlarm alarm)
	{
		// Unable to build the pending intent because the alarm is null
		if (alarm == null)
		{
			return null;
		}

		// Get the alarm ID
		int id = (int) alarm.getId();

		// Get the flags
		int flags = 0;

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
		{
			flags |= PendingIntent.FLAG_IMMUTABLE;
		}

		// Create the intent
		Intent intent = new Intent(context, NacMainActivity.class);

		// Build the pending intent
		return PendingIntent.getActivity(context, id, intent , flags);
	}

	/**
	 * Cancel the alarm with a given ID.
	 *
	 * @param  context  Context.
	 * @param  id  Alarm ID.
	 */
	public static void cancel(Context context, int id)
	{
		// Build the pending intent for the new type
		Intent intent = NacIntent.createForegroundService(context, (NacAlarm)null);
		PendingIntent pending = NacScheduler.buildAlarmPendingIntent(context, id,
			intent, PendingIntent.FLAG_NO_CREATE);

		// Cancel the alarm
		if (pending != null)
		{
			NacScheduler.getAlarmManager(context).cancel(pending);
		}
	}

	/**
	 * @see NacScheduler#cancel(Context, int)
	 */
	public static void cancel(Context context, NacAlarm alarm)
	{
		if (alarm == null)
		{
			return;
		}

		NacScheduler.cancel(context, (int)alarm.getId());
	}

	/**
	 * Cancel all active alarms.
	 */
	public static void cancelAllActive(Context context)
	{
		NacAlarmRepository repo = new NacAlarmRepository(context);
		List<NacAlarm> activeAlarms = repo.getActiveAlarmsNow();

		for (NacAlarm a : activeAlarms)
		{
			a.dismiss();
			repo.update(a);
			NacScheduler.cancel(context, a);
		}
	}

	/**
	 * Cancel the old alarm type with a given ID.
	 *
	 * @param  context  Context.
	 * @param  id  Alarm ID.
	 */
	public static void cancelOld(Context context, int id)
	{
		// Prepare the flags
		int flags = PendingIntent.FLAG_NO_CREATE;

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
		{
			flags |= PendingIntent.FLAG_IMMUTABLE;
		}

		// Create the pending intent for the old type
		Intent intent = new Intent(context, NacActiveAlarmBroadcastReceiver.class);
		PendingIntent pending = PendingIntent.getBroadcast(context, id, intent, flags);

		// Cancel the alarm
		if (pending != null)
		{
			NacScheduler.getAlarmManager(context).cancel(pending);
		}
	}

	/**
	 * Cancel the older alarm type with a given ID.
	 *
	 * @param  context  Context.
	 * @param  id  Alarm ID.
	 */
	public static void cancelOlder(Context context, int id)
	{
		// Prepare the flags
		int flags = PendingIntent.FLAG_NO_CREATE;

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
		{
			flags |= PendingIntent.FLAG_MUTABLE;
		}

		// Create the pending intent for the old type
		Intent intent = new Intent(context, NacActiveAlarmBroadcastReceiver.class);
		PendingIntent pending = PendingIntent.getBroadcast(context, id, intent, flags);

		// Cancel the alarm
		if (pending != null)
		{
			NacScheduler.getAlarmManager(context).cancel(pending);
		}
	}

	/**
	 * @return The AlarmManager.
	 */
	public static AlarmManager getAlarmManager(Context context)
	{
		return (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
	}

	/**
	 * Refresh all alarms.
	 */
	public static void refreshAll(Context context)
	{
		NacAlarmRepository repo = new NacAlarmRepository(context);
		List<NacAlarm> alarms = repo.getAllAlarmsNow();

		for (NacAlarm a : alarms)
		{
			int id = (int) a.getId();

			// Clear out the older alarms (Do not use IMMUTABLE flag)
			NacScheduler.cancelOlder(context, id);

			// Clear out the old alarms (Use IMMUTABLE flag)
			NacScheduler.cancelOld(context, id);

			// Clear out any new alarms, just in case
			NacScheduler.cancel(context, a);

			// Add each alarm
			NacScheduler.add(context, a);
		}
	}

	/**
	 * Update all days in a given alarm.
	 */
	public static void update(Context context, NacAlarm alarm)
	{
		NacScheduler.cancel(context, alarm);
		NacScheduler.add(context, alarm);
	}

	/**
	 * Update a single day in a given alarm.
	 */
	public static void update(Context context, NacAlarm alarm, Calendar day)
	{
		NacScheduler.cancel(context, alarm);
		NacScheduler.add(context, alarm, day);
	}

	/**
	 * Update all alarms.
	 */
	public static void updateAll(Context context)
	{
		NacAlarmRepository repo = new NacAlarmRepository(context);
		List<NacAlarm> alarms = repo.getAllAlarmsNow();

		NacScheduler.updateAll(context, alarms);
	}

	/**
	 * Update a list of alarms.
	 */
	public static void updateAll(Context context, List<NacAlarm> alarms)
	{
		for (NacAlarm a : alarms)
		{
			NacScheduler.update(context, a);
		}
	}

}

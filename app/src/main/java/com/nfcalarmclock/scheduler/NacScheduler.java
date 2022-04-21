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

		long id = alarm.getId();
		long millis = day.getTimeInMillis();

		// Determine the pending intent flags
		int showFlags = 0;
		int operationFlags = PendingIntent.FLAG_CANCEL_CURRENT;

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
		{
			showFlags |= PendingIntent.FLAG_IMMUTABLE;
			operationFlags |= PendingIntent.FLAG_IMMUTABLE;
		}

		// Show details of the alarm clock
		Intent showIntent = new Intent(context, NacMainActivity.class);
		PendingIntent showPendingIntent = PendingIntent.getActivity(context, (int)id,
			showIntent, showFlags);

		// Operation to perform the alarm is active
		Intent operationIntent = NacIntent.toIntent(context,
			NacActiveAlarmBroadcastReceiver.class, alarm);
		PendingIntent operationPendingIntent = PendingIntent.getBroadcast(
			context, (int)id, operationIntent, operationFlags);

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
	 * Cancel the alarm with a given ID.
	 *
	 * @param  context  Context.
	 * @param  id  Alarm ID.
	 */
	public static void cancel(Context context, int id)
	{
		// Determine the pending intent flags
		int flags = PendingIntent.FLAG_NO_CREATE;

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
		{
			flags |= PendingIntent.FLAG_IMMUTABLE;
		}

		Intent intent = new Intent(context, NacActiveAlarmBroadcastReceiver.class);
		PendingIntent pending = PendingIntent.getBroadcast(context, id, intent, flags);

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
	 * @return The AlarmManager.
	 */
	public static AlarmManager getAlarmManager(Context context)
	{
		return (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
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

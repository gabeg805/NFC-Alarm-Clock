package com.nfcalarmclock;

import android.app.AlarmManager;
import android.app.AlarmManager.AlarmClockInfo;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
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
		if (!alarm.getEnabled())
		{
			return;
		}

		int id = alarm.getId(day);
		long millis = day.getTimeInMillis();
		Intent operationIntent = NacIntent.toIntent(context,
			NacAlarmBroadcastReceiver.class, alarm);
		PendingIntent operationPendingIntent = PendingIntent.getBroadcast(
			context, id, operationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
		Intent showIntent = new Intent(context, NacMainActivity.class);
		PendingIntent showPendingIntent = PendingIntent.getActivity(context, id,
			showIntent, 0);
		AlarmClockInfo clock = new AlarmClockInfo(millis, showPendingIntent);
		AlarmManager manager = NacScheduler.getAlarmManager(context);

		manager.setAlarmClock(clock, operationPendingIntent);
	}

	/**
	 * Add all alarm days to the scheduler.
	 */
	public static void add(Context context, NacAlarm alarm)
	{
		if (!alarm.getEnabled())
		{
			return;
		}

		List<Calendar> days = NacCalendar.toCalendars(alarm);

		for (Calendar d : days)
		{
			NacScheduler.add(context, alarm, d);
		}
	}

	/**
	 * Cancel the alarm that was going to run on the given day.
	 */
	public static void cancel(Context context, NacAlarm alarm, Calendar day)
	{
		int id = alarm.getId(day);
		Intent intent = new Intent(context, NacAlarmBroadcastReceiver.class);
		PendingIntent pending = PendingIntent.getBroadcast(context, id, intent,
			PendingIntent.FLAG_NO_CREATE);

		if (pending != null)
		{
			NacScheduler.getAlarmManager(context).cancel(pending);
		}
	}

	/**
	 * Cancel all days the given alarm was going to run on.
	 */
	public static void cancel(Context context, NacAlarm alarm)
	{
		Calendar day = Calendar.getInstance();

		for (NacCalendar.Day d : NacCalendar.WEEK)
		{
			day.set(Calendar.DAY_OF_WEEK, NacCalendar.Days.toCalendarDay(d));
			NacScheduler.cancel(context, alarm, day);
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
	 * Schedule the next alarm.
	 */
	public static void scheduleNext(Context context, NacAlarm alarm)
	{
		if (!alarm.getRepeat() || !alarm.areDaysSelected())
		{
			return;
		}

		Calendar next = Calendar.getInstance();
		next.set(Calendar.HOUR_OF_DAY, alarm.getHour());
		next.set(Calendar.MINUTE, alarm.getMinute());
		next.set(Calendar.SECOND, 0);
		next.set(Calendar.MILLISECOND, 0);
		next.add(Calendar.DAY_OF_MONTH, 7);
		NacScheduler.update(context, alarm, next);
	}

	/**
	 * Toggle the the current day/enabled attribute of the alarm, and update it in
	 * the database.
	 */
	public static void toggleAlarm(Context context, NacAlarm alarm)
	{
		NacDatabase db = new NacDatabase(context);

		if (alarm.areDaysSelected())
		{
			alarm.toggleToday();
		}
		else
		{
			alarm.setEnabled(false);
		}

		db.update(alarm);
		db.close();
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
		NacScheduler.cancel(context, alarm, day);
		NacScheduler.add(context, alarm, day);
	}

	/**
	 * Update all alarms.
	 */
	public static void updateAll(Context context)
	{
		NacDatabase db = new NacDatabase(context);
		List<NacAlarm> alarms = db.read();

		NacScheduler.updateAll(context, alarms);
		db.close();
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

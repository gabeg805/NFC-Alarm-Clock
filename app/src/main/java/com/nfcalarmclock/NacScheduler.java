package com.nfcalarmclock;

import android.app.AlarmManager;
import android.app.AlarmManager.AlarmClockInfo;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import java.util.Calendar;
import java.util.List;

/**
 * The alarm scheduler.
 */
public class NacScheduler
{

	/**
	 * Context.
	 */
	private Context mContext;

	/**
	 */
	public NacScheduler(Context context)
	{
		this.mContext = context;
	}

	/**
	 * Add all alarms to the scheduler.
	 */
	public static void add(Context context, NacAlarm alarm)
	{
		if (!alarm.getEnabled())
		{
			return;
		}

		List<Calendar> calendars = NacCalendar.toCalendars(alarm);

		for (Calendar c : calendars)
		{
			NacScheduler.add(context, alarm, c);
		}
	}

	/**
	 * @see add
	 */
	public void add(NacAlarm alarm)
	{
		Context context = this.getContext();

		NacScheduler.add(context, alarm);
	}

	/**
	 * Add the alarm to the scheduler.
	 */
	public static void add(Context context, NacAlarm alarm, Calendar calendar)
	{
		if (!alarm.getEnabled())
		{
			return;
		}

		int id = alarm.getId(calendar);
		long millis = calendar.getTimeInMillis();
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
	 * @see add
	 */
	public void add(NacAlarm alarm, Calendar calendar)
	{
		Context context = this.getContext();

		NacScheduler.add(context, alarm, calendar);
	}

	/**
	 * Cancel all matching alarms.
	 */
	public static void cancel(Context context, NacAlarm alarm)
	{
		Calendar calendar = Calendar.getInstance();

		for (NacCalendar.Day d : NacCalendar.WEEK)
		{
			calendar.set(Calendar.DAY_OF_WEEK, NacCalendar.Days
				.toCalendarDay(d));
			NacScheduler.cancel(context, alarm, calendar);
		}
	}

	/**
	 * @see cancel
	 */
	public void cancel(NacAlarm alarm)
	{
		Context context = this.getContext();

		NacScheduler.cancel(context, alarm);
	}

	/**
	 * Cancel the matching alarm.
	 */
	public static void cancel(Context context, NacAlarm alarm,
		Calendar calendar)
	{
		int id = alarm.getId(calendar);
		Intent intent = new Intent(context, NacAlarmBroadcastReceiver.class);
		PendingIntent pending = PendingIntent.getBroadcast(context, id, intent,
			PendingIntent.FLAG_NO_CREATE);

		if (pending != null)
		{
			NacScheduler.getAlarmManager(context).cancel(pending);
		}
	}

	/**
	 * @see cancel
	 */
	public void cancel(NacAlarm alarm, Calendar calendar)
	{
		Context context = this.getContext();

		NacScheduler.cancel(context, alarm, calendar);
	}

	/**
	 * @return The AlarmManager.
	 */
	public static AlarmManager getAlarmManager(Context context)
	{
		return (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
	}

	/**
	 * @see getAlarmManager
	 */
	public AlarmManager getAlarmManager()
	{
		return (AlarmManager) this.getContext()
			.getSystemService(Context.ALARM_SERVICE);
	}

	/**
	 * @return The context.
	 */
	private Context getContext()
	{
		return this.mContext;
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
	 * Schedule the next alarm.
	 */
	public void scheduleNext(NacAlarm alarm)
	{
		Context context = this.getContext();

		NacScheduler.scheduleNext(context, alarm);
	}

	/**
	 * Update a list of alarms.
	 */
	public static void update(Context context, List<NacAlarm> alarms)
	{
		for (NacAlarm a : alarms)
		{
			NacScheduler.update(context, a);
		}
	}

	/**
	 * @see update
	 */
	public void update(List<NacAlarm> alarms)
	{
		Context context = this.getContext();

		NacScheduler.update(context, alarms);
	}

	/**
	 * Update a single alarm.
	 */
	public static void update(Context context, NacAlarm alarm)
	{
		NacScheduler.cancel(context, alarm);
		NacScheduler.add(context, alarm);
	}

	/**
	 * @see update
	 */
	public void update(NacAlarm alarm)
	{
		Context context = this.getContext();

		NacScheduler.update(context, alarm);
	}

	/**
	 * Update the scheduler.
	 */
	public static void update(Context context, NacAlarm alarm,
		Calendar calendar)
	{
		NacScheduler.cancel(context, alarm, calendar);
		NacScheduler.add(context, alarm, calendar);
	}

	/**
	 * Update the scheduler.
	 */
	public void update(NacAlarm alarm, Calendar calendar)
	{
		Context context = this.getContext();

		NacScheduler.update(context, alarm, calendar);
	}

}

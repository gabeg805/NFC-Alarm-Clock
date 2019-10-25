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
	public void add(NacAlarm alarm)
	{
		if (!alarm.getEnabled())
		{
			return;
		}

		List<Calendar> calendars = NacCalendar.toCalendars(alarm);

		for (Calendar c : calendars)
		{
			this.add(alarm, c);
		}
	}

	/**
	 * Add the alarm to the scheduler.
	 */
	public void add(NacAlarm alarm, Calendar calendar)
	{
		if (!alarm.getEnabled())
		{
			return;
		}

		Context context = this.getContext();
		int id = alarm.getId(calendar);
		long millis = calendar.getTimeInMillis();
		Intent operationIntent = NacIntent.toIntent(context,
			NacAlarmActivity.class, alarm);
		PendingIntent operationPendingIntent = PendingIntent.getActivity(
			context, id, operationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		Intent showIntent = new Intent(context, NacMainActivity.class);
		PendingIntent showPendingIntent = PendingIntent.getActivity(context, id,
			showIntent, 0);
		AlarmClockInfo clock = new AlarmClockInfo(millis, showPendingIntent);
		AlarmManager manager = this.getAlarmManager(context);

		manager.setAlarmClock(clock, operationPendingIntent);
	}

	/**
	 * Cancel all matching alarms.
	 */
	public void cancel(NacAlarm alarm)
	{
		Calendar calendar = Calendar.getInstance();

		for (NacCalendar.Day d : NacCalendar.WEEK)
		{
			calendar.set(Calendar.DAY_OF_WEEK, NacCalendar.Days
				.toCalendarDay(d));
			this.cancel(alarm, calendar);
		}
	}

	/**
	 * Cancel the matching alarm.
	 */
	public void cancel(NacAlarm alarm, Calendar calendar)
	{
		Context context = this.getContext();
		Intent intent = new Intent(context, NacAlarmActivity.class);
		int id = alarm.getId(calendar);
		PendingIntent pending = PendingIntent.getActivity(context, id, intent,
			PendingIntent.FLAG_NO_CREATE);

		if (pending != null)
		{
			this.getAlarmManager(context).cancel(pending);
		}
	}

	/**
	 * @return The AlarmManager.
	 */
	private AlarmManager getAlarmManager(Context context)
	{
		return (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
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
	public void scheduleNext(NacAlarm alarm)
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
		this.update(alarm, next);
	}

	/**
	 * Update the scheduler.
	 */
	public void update(NacAlarm alarm)
	{
		this.cancel(alarm);
		this.add(alarm);
	}

	/**
	 * Update the scheduler.
	 */
	public void update(NacAlarm alarm, Calendar calendar)
	{
		this.cancel(alarm, calendar);
		this.add(alarm, calendar);
	}

}

package com.nfcalarmclock;

import android.app.AlarmManager;
import android.app.AlarmManager.AlarmClockInfo;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import java.util.Calendar;
import java.util.List;

/**
 * The alarm scheduler.
 */
public class NacScheduler
{

	/**
	 * Application context.
	 */
	private Context mContext;

	/**
	 * Alarm manager.
	 */
	private AlarmManager mAlarmManager;

	/**
	 */
	public NacScheduler(Context context)
	{
		this.mContext = context;
		this.mAlarmManager = (AlarmManager) context.getSystemService(
			Context.ALARM_SERVICE);
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

		List<Calendar> calendars = NacCalendar.nextCalendars(alarm);

		if (calendars.isEmpty())
		{
			calendars.add(NacCalendar.toCalendarTodayOrTomorrow(alarm));
		}

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

		int id = alarm.getId(calendar);
		long millis = calendar.getTimeInMillis();
		PendingIntent receiver = this.getReceiverPendingIntent(id, alarm);
		PendingIntent show = this.getShowPendingIntent(id);
		AlarmClockInfo clock = new AlarmClockInfo(millis, show);

		this.getAlarmManager().setAlarmClock(clock, receiver);
	}

	/**
	 * Cancel all matching alarms.
	 */
	public void cancel(NacAlarm alarm)
	{
		Calendar calendar = Calendar.getInstance();

		for (NacCalendar.Day d : NacCalendar.WEEK)
		{
			calendar.set(Calendar.DAY_OF_WEEK, NacCalendar.toCalendarDay(d));
			this.cancel(alarm, calendar);
		}
	}

	/**
	 * Cancel the matching alarm.
	 */
	public void cancel(NacAlarm alarm, Calendar calendar)
	{
		int id = alarm.getId(calendar);
		PendingIntent pending = this.getReceiverPendingIntent(id);

		if (this.contains(pending))
		{
			this.mAlarmManager.cancel(pending);
		}
	}

	/**
	 * Check if scheduler contains a matching alarm.
	 */
	public boolean contains(int id)
	{
		PendingIntent pending = this.getReceiverPendingIntent(id);

		return this.contains(pending);
	}

	/**
	 * @see contains()
	 */
	public boolean contains(PendingIntent pendingIntent)
	{
		return (pendingIntent != null);
	}

	/**
	 * @return The AlarmManager.
	 */
	private AlarmManager getAlarmManager()
	{
		return this.mAlarmManager;
	}

	/**
	 * @return The context.
	 */
	private Context getContext()
	{
		return this.mContext;
	}

	/**
	 * @return The next alarm clock scheduled.
	 */
	public AlarmClockInfo getNext()
	{
		return this.getAlarmManager().getNextAlarmClock();
	}

	/**
	 * @return The intent that will receive the alarm broadcast from the
	 *         AlarmManager.
	 */
	public Intent getReceiverIntent()
	{
		return new Intent(this.getContext(), NacBroadcastReceiver.class);
	}

	/**
	 * @see getReceiverIntent
	 *
	 * @param  alarm  The alarm to add to the receiver intent as extra data.
	 */
	public Intent getReceiverIntent(NacAlarm alarm)
	{
		Intent intent = this.getReceiverIntent();
		Bundle bundle = new Bundle();
		NacAlarmParcel parcel = new NacAlarmParcel(alarm);

		bundle.putParcelable("parcel", parcel);
		intent.putExtra("bundle", bundle);

		return intent;
	}

	/**
	 * @param  id  The ID corresponding to the alarm, offset by the day the
	 *             alarm is supposed to go off.
	 * @param  alarm  The alarm.
	 *
	 * @return The PendingIntent for the intent that will receive the
	 *         AlarmManager broadcast. This will cancel any existing, matching
	 *         PendingIntents.
	 */
	public PendingIntent getReceiverPendingIntent(int id, NacAlarm alarm)
	{
		Context context = this.getContext();
		Intent intent = this.getReceiverIntent(alarm);

		return PendingIntent.getBroadcast(context, id, intent,
			PendingIntent.FLAG_CANCEL_CURRENT);
	}

	/**
	 * @param  id  The ID corresponding to the alarm, offset by the day the
	 *             alarm is supposed to go off.
	 *
	 * @return The PendingIntent for the intent that will receive the
	 *         AlarmManager broadcast. This will not create a PendingIntent if
	 *         an existing, matching, PendingIntent does not already exist.
	 */
	public PendingIntent getReceiverPendingIntent(int id)
	{
		Context context = this.getContext();
		Intent intent = this.getReceiverIntent();

		return PendingIntent.getBroadcast(context, id, intent,
			PendingIntent.FLAG_NO_CREATE);
	}


	/**
	 * @return The intent to show when the alarm is selected in the
	 *         notification shade (the thing that is shown when you swipe down
	 *         on the top notification bar).
	 */
	public Intent getShowIntent()
	{
		return new Intent(this.getContext(), NacMainActivity.class);
	}

	/**
	 * @param  id  The ID corresponding to the alarm, offset by the day the
	 *             alarm is supposed to go off.
	 *
	 * @return The PendingIntent that will run the NacMainActivity.
	 */
	public PendingIntent getShowPendingIntent(int id)
	{
		Context context = this.getContext();
		Intent intent = this.getShowIntent();

		return PendingIntent.getActivity(context, id, intent, 0);
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

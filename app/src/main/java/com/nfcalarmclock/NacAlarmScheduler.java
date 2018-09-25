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
 * @brief The alarm scheduler.
 */
public class NacAlarmScheduler
{

	/**
	 * @brief Application context.
	 */
	private Context mContext = null;

	/**
	 * @brief Alarm manager.
	 */
	private AlarmManager mAlarmManager = null;

	/**
	 */
	public NacAlarmScheduler(Context c)
	{
		this.mContext = c;
		this.mAlarmManager = (AlarmManager) c.getSystemService(
			Context.ALARM_SERVICE);
	}

	/**
	 * @brief Update scheduler.
	 */
	public void update(Alarm a)
	{
		NacUtility.print("Updating all scheduled alarms!");
		a.print();

		this.cancel(a);

		if (a.getEnabled())
		{
			this.add(a);
		}
	}

	/**
	 * @brief Update scheduler.
	 */
	public void update(Alarm a, Calendar c)
	{
		NacUtility.print("Updating a scheduled alarm!");
		a.print();

		this.cancel(a, c);

		if (a.getEnabled())
		{
			this.add(a, c);
		}
	}

	/**
	 * @brief Add the alarm to the scheduler.
	 */
	public void add(Alarm a)
	{
		List<Calendar> cals = a.getNextCalendars();

		for (Calendar c : cals)
		{
			this.add(a, c);
		}
	}

	/**
	 * @brief Add the alarm to the scheduler.
	 */
	public void add(Alarm a, Calendar c)
	{
		NacUtility.printf("Scheduled alarm : %s", c.getTime().toString());	
		int id = a.getId(c);
		long millis = c.getTimeInMillis();
		Intent operationintent = this.getOperationIntent(a);
		Intent showintent = this.getShowIntent();

		PendingIntent operationpending = PendingIntent.getBroadcast(
			this.mContext, id, operationintent,
			PendingIntent.FLAG_CANCEL_CURRENT);
		PendingIntent showpending = PendingIntent.getActivity(this.mContext,
			id, showintent, 0);
		AlarmClockInfo clock = new AlarmClockInfo(millis, showpending);

		this.mAlarmManager.setAlarmClock(clock, operationpending);
	}

	/**
	 * @brief Cancel the matching alarm.
	 */
	public void cancel(Alarm a)
	{
		List<Calendar> cals = a.getCalendars();

		for (Calendar c : cals)
		{
			this.cancel(a, c);
		}
	}

	/**
	 * @brief Cancel the matching alarm.
	 */
	public void cancel(Alarm a, Calendar c)
	{
		int id = a.getId(c);
		Intent intent = this.getOperationIntent();
		PendingIntent pending = PendingIntent.getBroadcast(mContext, id,
			intent, PendingIntent.FLAG_NO_CREATE);

		if (this.contains(pending))
		{
			NacUtility.printf("CANCELLED ALARM : %s", c.getTime().toString());	
			this.mAlarmManager.cancel(pending);
		}
	}

	/**
	 * @brief Check if scheduler contains a matching alarm.
	 */
	public boolean contains(int id)
	{
		Intent intent = this.getOperationIntent();
		PendingIntent pending = PendingIntent.getBroadcast(mContext, id,
			intent, PendingIntent.FLAG_NO_CREATE);

		return this.contains(pending);
	}

	/**
	 * @see contains()
	 */
	public boolean contains(PendingIntent p)
	{
		return (p != null);
	}

	/**
	 * @return The next alarm clock scheduled.
	 */
	public AlarmClockInfo getNext()
	{
		return this.mAlarmManager.getNextAlarmClock();
	}

	/**
	 * @return The operation intent.
	 */
	public Intent getOperationIntent()
	{
		return new Intent(this.mContext, NacAlarmReceiver.class);
	}

	/**
	 * @return The operation intent with the added alarm as a Parcel.
	 */
	public Intent getOperationIntent(Alarm a)
	{
		Intent intent = this.getOperationIntent();
		Bundle bundle = new Bundle();
		NacAlarmParcel parcel = new NacAlarmParcel(a);

		bundle.putParcelable("parcel", parcel);
		intent.putExtra("bundle", bundle);

		return intent;
	}

	/**
	 * @return The show intent when the alarm is selected.
	 */
	public Intent getShowIntent()
	{
		return new Intent(this.mContext, MainActivity.class);
	}

}

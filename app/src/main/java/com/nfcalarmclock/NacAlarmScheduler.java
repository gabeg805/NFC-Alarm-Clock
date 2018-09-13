package com.nfcalarmclock;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
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
		List<Calendar> cals = a.getCalendars();

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
		AlarmManager.AlarmClockInfo clock = new
			AlarmManager.AlarmClockInfo(millis, showpending);

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
		NacAlarmParcel parcel = new NacAlarmParcel(a);

		intent.putExtra("Alarm", parcel);

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

		//NacUtility.print("On Manage in the Adapter was called");
		//int id = alarm.getId();
		//Intent i = new Intent(mContext, NacAlarmReceiver.class);
		//PendingIntent p = PendingIntent.getBroadcast(mContext, id, i,
		//	PendingIntent.FLAG_NO_CREATE);

		//if (p != null)
		//{
		//	NacUtility.print("Cancelling in the alarm manager.");
		//	mAlarmManager.cancel(p);
		//}

		//if (alarm.getEnabled())
		//{
		//	NacAlarmParcel parcel = new NacAlarmParcel(alarm);
		//	Intent intent = new Intent(mContext, NacAlarmReceiver.class);
		//	Intent act = new Intent(mContext, MainActivity.class);
		//	
		//	intent.putExtra("Alarm", parcel);

		//	PendingIntent pending = PendingIntent.getBroadcast(mContext, id,
		//		intent, PendingIntent.FLAG_CANCEL_CURRENT);
		//	PendingIntent actp = PendingIntent.getActivity(mContext, id, act,
		//		0);
		//	Calendar cal = alarm.getCalendar();

		//	AlarmManager.AlarmClockInfo clock = new
		//		AlarmManager.AlarmClockInfo(cal.getTimeInMillis(), null);
		//	mAlarmManager.setAlarmClock(clock, pending);

		//	//mAlarmManager.setExact(AlarmManager.RTC_WAKEUP,
		//	//	cal.getTimeInMillis(), pending);
		//	//mAlarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
		//	//	cal.getTimeInMillis(), pending);
		//}

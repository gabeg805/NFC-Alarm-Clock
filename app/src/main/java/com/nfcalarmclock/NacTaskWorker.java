package com.nfcalarmclock;

import android.content.Context;

/**
 * Work on alarm tasks.
 */
public class NacTaskWorker
{

	/**
	 * Add an alarm to the database, and schedule it to run.
	 */
	public static void addAlarm(Context context, NacAlarm alarm)
	{
		if (alarm == null)
		{
			return;
		}

		NacDatabase db = new NacDatabase(context);
		NacSharedPreferences shared = new NacSharedPreferences(context);
		int id = alarm.getId();

		db.add(alarm);
		db.close();
		shared.editSnoozeCount(id, 0);
		NacScheduler.update(context, alarm);
	}

	/**
	 * Delete an alarm from the database, and cancel it so that it does not run.
	 */
	public static void deleteAlarm(Context context, NacAlarm alarm)
	{
		if (alarm == null)
		{
			return;
		}

		NacDatabase db = new NacDatabase(context);
		NacSharedPreferences shared = new NacSharedPreferences(context);
		int id = alarm.getId();

		db.delete(alarm);
		db.close();
		shared.editSnoozeCount(id, 0);
		NacScheduler.cancel(context, alarm);
	}

	/**
	 * Update an alarm in the database, and update the next time it is scheduled
	 * to run.
	 */
	public static void updateAlarm(Context context, NacAlarm alarm)
	{
		if (alarm == null)
		{
			return;
		}

		NacDatabase db = new NacDatabase(context);

		db.update(alarm);
		db.close();
		NacScheduler.update(context, alarm);
	}

}

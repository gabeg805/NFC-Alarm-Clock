package com.nfcalarmclock;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

/**
 * Execute schedule and database updates off of the UI main thread.
 */
// To-do: Change this to JobIntentService?
public class NacService
	extends IntentService
{

	/**
	 */
	public NacService()
	{
		super("NacService");
	}

	/**
	 * Add an alarm.
	 */
	public static void addAlarm(Context context, NacAlarm alarm)
	{
		NacDatabase db = new NacDatabase(context);
		NacScheduler scheduler = new NacScheduler(context);
		NacSharedPreferences shared = new NacSharedPreferences(context);
		int id = alarm.getId();

		db.add(alarm);
		db.close();
		scheduler.update(alarm);
		shared.editSnoozeCount(id, 0);
	}

	/**
	 * Delete an alarm.
	 */
	public static void deleteAlarm(Context context, NacAlarm alarm)
	{
		NacDatabase db = new NacDatabase(context);
		NacScheduler scheduler = new NacScheduler(context);
		NacSharedPreferences shared = new NacSharedPreferences(context);
		int id = alarm.getId();

		db.delete(alarm);
		db.close();
		scheduler.cancel(alarm);
		shared.editSnoozeCount(id, 0);
	}

	/**
	 */
	@Override
	protected void onHandleIntent(Intent intent)
	{
		String key = intent.getDataString();

		if (key.equals("swap"))
		{
			NacAlarm[] alarms = NacIntent.getAlarms(intent);

			NacService.swapAlarms(this, alarms[0], alarms[1]);
		}
		else
		{
			NacAlarm alarm = NacIntent.getAlarm(intent);

			if (key.equals("add"))
			{
				NacService.addAlarm(this, alarm);
			}
			else if (key.equals("delete"))
			{
				NacService.deleteAlarm(this, alarm);
			}
			else if (key.equals("update"))
			{
				NacService.updateAlarm(this, alarm);
			}
		}
	}

	/**
	 * Swap two alarms.
	 */
	public static void swapAlarms(Context context, NacAlarm fromAlarm,
		NacAlarm toAlarm)
	{
		NacDatabase db = new NacDatabase(context);
		NacScheduler scheduler = new NacScheduler(context);
		NacSharedPreferences shared = new NacSharedPreferences(context);
		int fromId = fromAlarm.getId();
		int toId = toAlarm.getId();
		int fromSnoozeCount = shared.getSnoozeCount(fromId);
		int toSnoozeCount = shared.getSnoozeCount(toId);

		scheduler.cancel(fromAlarm);
		scheduler.cancel(toAlarm);
		db.swap(fromAlarm, toAlarm);
		db.close();
		scheduler.add(fromAlarm);
		scheduler.add(toAlarm);
		shared.editSnoozeCount(fromId, toSnoozeCount);
		shared.editSnoozeCount(toId, fromSnoozeCount);
	}

	/**
	 * Update an alarm.
	 */
	public static void updateAlarm(Context context, NacAlarm alarm)
	{
		NacDatabase db = new NacDatabase(context);
		NacScheduler scheduler = new NacScheduler(context);

		db.update(alarm);
		db.close();
		scheduler.update(alarm);
	}

}

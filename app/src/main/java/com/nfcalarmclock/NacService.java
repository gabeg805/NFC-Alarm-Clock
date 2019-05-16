package com.nfcalarmclock;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

/**
 * Execute schedule and database updates off of the UI main thread.
 */
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
	 */
	@Override
	protected void onHandleIntent(Intent intent)
	{
		String key = intent.getDataString();
		NacDatabase db = new NacDatabase(this);
		NacScheduler scheduler = new NacScheduler(this);
		NacSharedPreferences shared = new NacSharedPreferences(this);

		if (key.equals("swap"))
		{
			NacAlarm[] alarms = NacIntent.getAlarms(intent);
			NacAlarm fromAlarm = alarms[0];
			NacAlarm toAlarm = alarms[1];
			int fromId = fromAlarm.getId();
			int toId = toAlarm.getId();
			int fromSnoozeCount = shared.getSnoozeCount(fromId);
			int toSnoozeCount = shared.getSnoozeCount(toId);

			scheduler.cancel(fromAlarm);
			scheduler.cancel(toAlarm);
			db.swap(fromAlarm, toAlarm);
			scheduler.add(fromAlarm);
			scheduler.add(toAlarm);
			shared.editSnoozeCount(fromId, toSnoozeCount);
			shared.editSnoozeCount(toId, fromSnoozeCount);
		}
		else
		{
			NacAlarm alarm = NacIntent.getAlarm(intent);
			int id = alarm.getId();

			if (key.equals("add"))
			{
				db.add(alarm);
				scheduler.update(alarm);
				shared.editSnoozeCount(id, 0);
			}
			else if (key.equals("delete"))
			{
				db.delete(alarm);
				scheduler.cancel(alarm);
				shared.editSnoozeCount(id, 0);
			}
			else if (key.equals("change"))
			{
				db.update(alarm);
				scheduler.update(alarm);
			}
		}

		db.close();
	}

}

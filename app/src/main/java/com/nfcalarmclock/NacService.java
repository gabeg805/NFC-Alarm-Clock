package com.nfcalarmclock;

import android.app.IntentService;
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
		NacAlarm alarm = NacIntent.getAlarm(intent);
		NacSound sound = NacIntent.getSound(intent);

		if (alarm != null)
		{
			NacUtility.printf("Doing %s to alarm", key);
			NacDatabase db = new NacDatabase(this);
			NacScheduler scheduler = new NacScheduler(this);
			alarm.print();

			if (key.equals("add"))
			{
				db.add(alarm);
				scheduler.update(alarm);
			}
			else if (key.equals("delete"))
			{
				db.delete(alarm);
				scheduler.cancel(alarm);
			}
			else if (key.equals("change"))
			{
				db.update(alarm);
				scheduler.update(alarm);
			}
			//else if (data.equals())
			//{
			//	scheduler.cancel(fromAlarm);
			//	scheduler.cancel(toAlarm);
			//	db.swap(fromAlarm, toAlarm);
			//	scheduler.add(fromAlarm);
			//	scheduler.add(toAlarm);
			//}

			db.close();
		}
		else if (sound != null)
		{
			NacUtility.printf("Doing %s to sound", key);
			NacSharedPreferences shared = new NacSharedPreferences(this);

			shared.getInstance().edit().putString(key, sound.getPath()).apply();
		}
		else
		{
			NacUtility.printf("Not doing shit with %s", key);
		}
	}

}

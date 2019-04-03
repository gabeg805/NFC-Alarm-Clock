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
		String data = intent.getDataString();
		Bundle bundle = NacAlarmParcel.getExtra(intent);
		NacAlarm alarm = NacAlarmParcel.getAlarm(bundle);
		NacDatabase db = new NacDatabase(this);
		NacScheduler scheduler = new NacScheduler(this);

		if (data.equals("add"))
		{
			db.add(alarm);
			scheduler.update(alarm);
		}
		else if (data.equals("delete"))
		{
			db.delete(alarm);
			scheduler.cancel(alarm);
		}
		else if (data.equals("change"))
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

}

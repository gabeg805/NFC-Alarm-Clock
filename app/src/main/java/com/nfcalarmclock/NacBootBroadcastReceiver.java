package com.nfcalarmclock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import java.util.ArrayList;
import java.util.List;

/**
 * Restore alarms on boot up.
 */
public class NacBootBroadcastReceiver
	extends BroadcastReceiver
{

	/**
	 */
	@Override
	public void onReceive(final Context context, Intent intent)
	{
		NacDatabase db = new NacDatabase(context);
		NacScheduler scheduler = new NacScheduler(context);
		List<NacAlarm> alarms = db.read();

		for (NacAlarm a : alarms)
		{
			scheduler.update(a);
		}
	}

}

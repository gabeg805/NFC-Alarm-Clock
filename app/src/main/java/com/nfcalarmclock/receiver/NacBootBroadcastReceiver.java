package com.nfcalarmclock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import java.util.List;

/**
 * Restore alarms on boot up.
 */
public class NacBootBroadcastReceiver
	extends BroadcastReceiver
{

	/**
	 * It is possible for another actor to send a spoofed intent with no
	 * action string or a different action string and cause undesired behavior.
	 * Ensure that the received Intent's action string matches the expected
	 * value before restoring alarms.
	 */
	@Override
	public void onReceive(final Context context, Intent intent)
	{
		if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED"))
		{
			NacDatabase db = new NacDatabase(context);
			NacScheduler scheduler = new NacScheduler(context);
			List<NacAlarm> alarms = db.read();

			scheduler.update(alarms);
		}
	}

}

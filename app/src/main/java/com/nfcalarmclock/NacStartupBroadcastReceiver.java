package com.nfcalarmclock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Restore alarms on startup.
 */
public class NacStartupBroadcastReceiver
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
		String action = intent.getAction();

		if (action.equals(Intent.ACTION_BOOT_COMPLETED))
		{
			NacScheduler.updateAll(context);
		}
	}

}

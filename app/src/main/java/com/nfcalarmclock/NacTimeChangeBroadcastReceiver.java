package com.nfcalarmclock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Receive this signal from AlarmManager and start the foreground service.
 */
public class NacTimeChangeBroadcastReceiver
	extends BroadcastReceiver
{
 
	/**
	 * It is possible for another actor to send a spoofed intent with no
	 * action string or a different action string and cause undesired behavior.
	 * Ensure that the received Intent's action string matches the expected
	 * value before updating alarms.
	 */
	@Override
	public void onReceive(final Context context, Intent intent)
	{
		String action = intent.getAction();

		if (action.equals(Intent.ACTION_DATE_CHANGED)
			|| action.equals(Intent.ACTION_TIME_CHANGED)
			|| action.equals(Intent.ACTION_TIMEZONE_CHANGED)
			|| action.equals(Intent.ACTION_LOCALE_CHANGED))
		{
			NacScheduler.updateAll(context);
		}
	}

}

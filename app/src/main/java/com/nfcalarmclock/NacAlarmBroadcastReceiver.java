package com.nfcalarmclock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

/**
 * Receive the signal from the AlarmManager that it's time for the alarm to go
 * off, which in turn start the NacAlarmActivity.
 */
public class NacAlarmBroadcastReceiver
	extends BroadcastReceiver
{
 
	/**
	 */
	@Override
	public void onReceive(final Context context, Intent intent)
	{
		Bundle bundle = NacIntent.getAlarmBundle(intent);
		Intent startIntent = new Intent(
			NacForegroundService.ACTION_START_SERVICE, null, context,
			NacForegroundService.class);
		startIntent = NacIntent.addAlarm(startIntent, bundle);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
		{
			context.startForegroundService(startIntent);
		}
		else
		{
			context.startService(startIntent);
		}
	}

}

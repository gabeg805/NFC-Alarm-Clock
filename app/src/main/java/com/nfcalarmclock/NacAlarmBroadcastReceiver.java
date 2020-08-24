package com.nfcalarmclock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

/**
 * Receive this signal from AlarmManager and start the foreground service.
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
		Intent activityIntent = NacIntent.createAlarmActivity(context, bundle);
		Intent startIntent = NacIntent.createForegroundService(context, bundle);

		context.startActivity(activityIntent);

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

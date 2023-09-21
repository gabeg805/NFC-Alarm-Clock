package com.nfcalarmclock.activealarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.nfcalarmclock.util.NacContext;
import com.nfcalarmclock.util.NacIntent;

/**
 * Receive this signal from AlarmManager and start the foreground service.
 */
public class NacActiveAlarmBroadcastReceiver
	extends BroadcastReceiver
{
 
	/**
	 */
	@Override
	public void onReceive(final Context context, Intent intent)
	{
		Bundle bundle = NacIntent.getAlarmBundle(intent);

		NacContext.startAlarm(context, bundle);
	}

}

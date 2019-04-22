package com.nfcalarmclock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
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
	public void onReceive(Context context, Intent intent)
	{
		Bundle bundle = NacIntent.getAlarmBundle(intent);
		Intent newIntent = NacIntent.createAlarmActivity(context, bundle);

		context.startActivity(newIntent);
	}

}

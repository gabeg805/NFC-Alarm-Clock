package com.nfcalarmclock.permission;

import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.nfcalarmclock.scheduler.NacScheduler;

/**
 * Refresh all alarms when the Schedule Exact Alarm Permission is granted.
 *
 * TODO: Test this
 */
public class NacScheduleExactAlarmPermissionChangedBroadcastReceiver
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

		// Make sure the intent action corresponds to the permission being changed
		if (action.equals(AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED))
		{
			AlarmManager alarmManager = (AlarmManager) context.getSystemService(
				Context.ALARM_SERVICE);

			// Make sure the permission was changed such that exact alarms can be
			// scheduled
			if (alarmManager.canScheduleExactAlarms())
			{
				// Refresh all alarms
				NacScheduler.refreshAll(context);
			}
		}
	}

}

package com.nfcalarmclock.permission.scheduleexactalarm

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.nfcalarmclock.scheduler.NacScheduler

/**
 * Refresh all alarms when the Schedule Exact Alarm Permission is granted.
 */
class NacScheduleExactAlarmPermissionChangedBroadcastReceiver
	: BroadcastReceiver()
{

	/**
	 * It is possible for another actor to send a spoofed intent with no
	 * action string or a different action string and cause undesired behavior.
	 * Ensure that the received Intent's action string matches the expected
	 * value before restoring alarms.
	 */
	override fun onReceive(context: Context, intent: Intent)
	{
		// Do not do anything if the Android version is not correct
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S)
		{
			return
		}

		// Make sure the intent action corresponds to the permission being changed
		if (intent.action == AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED)
		{
			val alarmManager = context.getSystemService(
				Context.ALARM_SERVICE) as AlarmManager

			// Make sure the permission was changed such that exact alarms can be
			// scheduled
			if (alarmManager.canScheduleExactAlarms())
			{
				// Refresh all alarms
				NacScheduler.refreshAll(context)
			}
		}
	}

}
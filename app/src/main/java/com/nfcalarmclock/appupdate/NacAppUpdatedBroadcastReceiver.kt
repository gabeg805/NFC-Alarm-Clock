package com.nfcalarmclock.appupdate

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.nfcalarmclock.scheduler.NacScheduler

/**
 * After the app is updated, reapply the alarms.
 *
 * When the app is updated, any alarms that were set are lost. This will attempt to restore those
 * alarms.
 */
class NacAppUpdatedBroadcastReceiver : BroadcastReceiver()
{

	/**
	 * It is possible for another actor to send a spoofed intent with no
	 * action string or a different action string and cause undesired behavior.
	 * Ensure that the received Intent's action string matches the expected
	 * value before restoring alarms.
	 */
	override fun onReceive(context: Context, intent: Intent)
	{
		// Check that the action is correct
		if (intent.action == Intent.ACTION_MY_PACKAGE_REPLACED)
		{
			NacScheduler.updateAll(context)
		}
	}

}
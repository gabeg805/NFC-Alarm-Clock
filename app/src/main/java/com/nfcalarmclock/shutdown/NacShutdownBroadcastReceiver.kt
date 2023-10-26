package com.nfcalarmclock.shutdown

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.nfcalarmclock.scheduler.NacScheduler

/**
 * Remove any active alarms on shutdown.
 */
class NacShutdownBroadcastReceiver : BroadcastReceiver()
{

	/**
	 * It is possible for another actor to send a spoofed intent with no
	 * action string or a different action string and cause undesired behavior.
	 * Ensure that the received Intent's action string matches the expected
	 * value before restoring alarms.
	 */
	override fun onReceive(context: Context, intent: Intent)
	{
		val action = intent.action

		// Check that the action is correct
		if (action == Intent.ACTION_SHUTDOWN)
		{
			NacScheduler.cancelAllActive(context)
		}
	}

}
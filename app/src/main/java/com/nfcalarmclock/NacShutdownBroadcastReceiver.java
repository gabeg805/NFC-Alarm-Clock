package com.nfcalarmclock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import java.util.List;

/**
 * Remove any active alarms on shutdown.
 */
public class NacShutdownBroadcastReceiver
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

		if (action.equals(Intent.ACTION_SHUTDOWN))
		{
			NacDatabase db = new NacDatabase(context);
			NacSharedPreferences shared = new NacSharedPreferences(context);
			List<NacAlarm> activeAlarms = db.findActiveAlarms();

			for (NacAlarm a : activeAlarms)
			{
				a.setIsActive(false);

				if (!a.shouldRepeat())
				{
					NacScheduler.toggleAlarm(a);
				}
				else
				{
					NacScheduler.scheduleNext(context, a);
				}

				db.update(a);
				shared.editSnoozeCount(a.getId(), 0);
			}

			db.close();
		}
	}

}

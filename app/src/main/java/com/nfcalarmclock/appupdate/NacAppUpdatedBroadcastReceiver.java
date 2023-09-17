package com.nfcalarmclock.appupdate;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.nfcalarmclock.scheduler.NacScheduler;

/**
 * After the app is updated, reapply the alarms.
 * <p>
 * When the app is updated, any alarms that were set are lost. This will attempt to restore those
 * alarms.
 */
public class NacAppUpdatedBroadcastReceiver
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

        if (action.equals(Intent.ACTION_MY_PACKAGE_REPLACED))
        {
            NacScheduler.updateAll(context);
        }
    }

}

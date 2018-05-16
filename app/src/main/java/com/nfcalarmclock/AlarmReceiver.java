package com.nfcalarmclock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class AlarmReceiver
    extends BroadcastReceiver
{

    @Override
    public void onReceive(Context context, Intent intent)
    {
        Toast.makeText(context, "On receive the alarm receiver", 
                       Toast.LENGTH_LONG).show();
        // String state = intent.getExtras().getString("extra");
        // // Log.e("MyActivity", "In the receiver with " + state);

        // Intent serviceIntent = new Intent(context,RingtonePlayingService.class);
        // serviceIntent.putExtra("extra", state);

        // context.startService(serviceIntent);
    }
}

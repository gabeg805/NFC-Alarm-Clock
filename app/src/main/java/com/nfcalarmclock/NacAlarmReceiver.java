package com.nfcalarmclock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/**
 * @brief Alarm receiver.
 */
public class NacAlarmReceiver
	extends BroadcastReceiver
{
 
	/**
	 * @brief Method called when broadcast signal is received.
	 *
	 * @param  c  Application context.
	 * @param  i  Intent.
	 */
	@Override
	public void onReceive(final Context c, Intent i)
	{
		//Toast.makeText(c, "On Receive!!!", Toast.LENGTH_SHORT).show();
		NacUtility.print("On Receive!!!");
		i.setExtrasClassLoader(NacAlarmParcel.class.getClassLoader());

		NacAlarmParcel parcel = (NacAlarmParcel) i.getParcelableExtra("Alarm");
		Intent intent = new Intent(c.getApplicationContext(), NacAlarmActivity.class);

		intent.putExtra("Alarm", parcel);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		c.startActivity(intent);
	}

}

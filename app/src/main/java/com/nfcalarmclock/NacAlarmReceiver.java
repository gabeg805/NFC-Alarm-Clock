package com.nfcalarmclock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

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
		Bundle bundle = (Bundle) i.getBundleExtra("bundle");
		NacAlarmParcel parcel = (NacAlarmParcel)
			bundle.getParcelable("parcel");
		Intent intent = new Intent(c.getApplicationContext(),
			NacAlarmActivity.class);

		intent.putExtra("bundle", bundle);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		c.startActivity(intent);
	}

}

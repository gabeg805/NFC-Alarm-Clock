package com.nfcalarmclock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import android.app.PendingIntent;
import android.os.PowerManager;
import android.app.AlarmManager;
import java.util.Calendar;

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
	public void onReceive(final Context context, Intent intent)
	{
		//NacUtility.printf("Doing alarmio stuff!");
		//AlarmManager manager = (AlarmManager) context.getSystemService(
		//	Context.ALARM_SERVICE);

		//NacAlarm alarm = NacIntent.getAlarm(intent);
		//Calendar cal = NacCalendar.getNext(alarm);
		//Intent getIntent = NacIntent.toIntent(context,
		//	NacAlarmBroadcastReceiver.class, alarm);
		//PendingIntent pi = PendingIntent.getBroadcast(context, alarm.getId(cal),
		//	getIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		////Intent intent = new Intent(context, AlarmReceiver.class);
		////intent.putExtra(AlarmReceiver.EXTRA_ALARM_ID, id);
		////PendingIntent pi = PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		//manager.setAlarmClock(
		//	new AlarmManager.AlarmClockInfo(
		//		cal.getTimeInMillis(),
		//		PendingIntent.getActivity(context, 0, new Intent(context, NacMainActivity.class), 0)),
		//		pi);

		Bundle bundle = NacIntent.getAlarmBundle(intent);
		Intent newIntent = NacIntent.createAlarmActivity(context, bundle);

		context.startActivity(newIntent);

		//NacUtility.printf("Going async!");
		//final PendingResult result = goAsync();
		//NacUtility.printf("Getting powermanager!");
		//PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		//final PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "NacAlarmWakeLock");
		//NacUtility.printf("Acquireing wakelock!");
		//wl.acquire();
		//NacUtility.printf("Starting alarm activity!");
		//context.startActivity(newIntent);
		//NacUtility.printf("Posting async task!");
		//AsyncHandler.postDelayed(new Runnable()
		//{
		//	@Override
		//	public void run()
		//	{
		//		//NacUtility.printf("Starting alarm activity!");
		//		//context.startActivity(newIntent);
		//		NacUtility.printf("Finishing async task!");
		//		result.finish();
		//		NacUtility.printf("Releasing wakelock!");
		//		wl.release();
		//	}
		//}, 3000);
	}

}

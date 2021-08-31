package com.nfcalarmclock.system;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import com.nfcalarmclock.alarm.NacAlarm;
import com.nfcalarmclock.util.NacUtility;
import com.nfcalarmclock.nfc.NacNfc;
import com.nfcalarmclock.shared.NacSharedConstants;

/**
 * Context.
 */
public class NacContext
{

	/**
	 * Check that the NFC tag scanned matches the ID of the one required by the
	 * alarm.
	 *
	 * @param  context  A context.
	 * @param  intent  An intent.
	 * @param  alarm  An alarm.
	 *
	 * @return True if the NFC tag scanned matches the one required by the alarm,
	 *     and False otherwise.
	 */
	public static boolean checkNfcScan(Context context, Intent intent,
		NacAlarm alarm)
	{
		if ((intent == null) || (alarm == null))
		{
			return false;
		}

		if (NacNfc.doIdsMatch(alarm, intent))
		{
			return true;
		}
		else
		{
			NacSharedConstants cons = new NacSharedConstants(context);
			NacUtility.quickToast(context, cons.getErrorMessageNfcMismatch());
			return false;
		}
	}

	/**
	 * Dismiss the alarm activity for the given alarm.
	 *
	 * If alarm is null, it will stop the currently active alarm activity.
	 */
	public static void dismissAlarmActivity(Context context, NacAlarm alarm)
	{
		Intent intent = NacIntent.dismissAlarmActivity(context, alarm);
		context.startActivity(intent);
	}

	/**
	 * Dismiss the alarm activity for the given alarm due with NFC.
	 *
	 * If alarm is null, it will stop the currently active alarm activity.
	 */
	public static void dismissAlarmActivityWithNfc(Context context,
		Intent nfcIntent, NacAlarm alarm)
	{
		// TODO: Can I just have *WithNfc, instead of this method?
		Intent intent = NacIntent.dismissAlarmActivity(context, alarm);

		intent.setAction(nfcIntent.getAction());
		context.startActivity(intent);
	}

	/**
	 * Dismiss the foreground service for the given alarm.
	 *
	 * If alarm is null, it will stop the currently active foreground service.
	 */
	public static void dismissForegroundService(Context context, NacAlarm alarm)
	{
		Intent intent = NacIntent.dismissForegroundService(context, alarm);
		context.startService(intent);
	}

	/**
	 * Dismiss the foreground service for the given alarm with NFC.
	 *
	 * If alarm is null, it will stop the currently active foreground service.
	 */
	public static void dismissForegroundServiceWithNfc(Context context, NacAlarm alarm)
	{
		Intent intent = NacIntent.dismissForegroundServiceWithNfc(context, alarm);
		context.startService(intent);
	}

	///**
	// * Dismiss the foreground service for the given alarm due to an NFC tag being
	// * scanned.
	// *
	// * @return True if decided to dismiss the foreground service, and False if
	// *         unable to due to null values or NFC tag IDs not matching.
	// */
	//public static boolean dismissForegroundServiceFromNfcScan(Context context,
	//	Intent intent, NacAlarm alarm)
	//{
	//	if (NacContext.checkNfcScan(context, intent, alarm))
	//	{
	//		NacContext.dismissForegroundServiceWithNfc(context, alarm);
	//		return false;
	//	}

	//	return true;
	//}

	/**
	 * Snooze the foreground service for the given alarm.
	 *
	 * The alarm cannot be null, unlike the dismissForegroundService() method.
	 */
	public static void snoozeForegroundService(Context context, NacAlarm alarm)
	{
		Intent intent = NacIntent.snoozeForegroundService(context, alarm);
		context.startService(intent);
	}

	/**
	 * Stop the alarm activity for the given alarm.
	 *
	 * If alarm is null, it will stop the currently active alarm activity.
	 */
	public static void stopAlarmActivity(Context context, NacAlarm alarm)
	{
		Intent intent = NacIntent.stopAlarmActivity(alarm);
		context.sendBroadcast(intent);
	}

	/**
	 * Start the running the alarm activity and service.
	 *
	 * @param  context  A context.
	 * @param  bundle  A bundle, typically with an alarm inside.
	 */
	public static void startAlarm(Context context, Bundle bundle)
	{
		Intent activityIntent = NacIntent.createAlarmActivity(context, bundle);
		Intent serviceIntent = NacIntent.createForegroundService(context, bundle);

		context.startActivity(activityIntent);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
		{
			context.startForegroundService(serviceIntent);
		}
		else
		{
			context.startService(serviceIntent);
		}
	}

	/**
	 * @see NacContext#startAlarm(Context, Bundle)
	 */
	public static void startAlarm(Context context, NacAlarm alarm)
	{
		Bundle bundle = NacBundle.toBundle(alarm);
		NacContext.startAlarm(context, bundle);
	}

	/**
	 * Start the main activity.
	 *
	 * @param  context  A context.
	 */
	public static void startMainActivity(Context context)
	{
		Intent intent = NacIntent.createMainActivity(context);
		context.startActivity(intent);
	}

}

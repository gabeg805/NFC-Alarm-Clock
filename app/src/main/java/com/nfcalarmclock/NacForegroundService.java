package com.nfcalarmclock;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import java.util.Calendar;

/**
 */
public class NacForegroundService
	extends Service
	implements NacWakeUpAction.OnAutoDismissListener
{

	/**
	 * Action to start the service.
	 */
	public static final String ACTION_START_SERVICE = "ACTION_START_SERVICE";

	/**
	 * Action to stop the service.
	 */
	public static final String ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE";

	/**
	 * Action to snooze the alarm.
	 */
	public static final String ACTION_SNOOZE_ALARM = "ACTION_SNOOZE_ALARM";

	/**
	 * Action to dismiss the alarm.
	 */
	public static final String ACTION_DISMISS_ALARM = "ACTION_DISMISS_ALARM";

	/**
	 * Wake up action.
	 */
	private NacWakeUpAction mWakeUp;

	/**
	 * Shared preferences.
	 */
	private NacSharedPreferences mSharedPreferences;

	/**
	 * Alarm.
	 */
	private NacAlarm mAlarm;

	/**
	 * Wakelock.
	 */
	private WakeLock mWakeLock;

	/**
	 * Dismiss the alarm.
	 */
	private void dismiss()
	{
		NacSharedPreferences shared = this.getSharedPreferences();
		NacAlarm alarm = this.getAlarm();

		if (alarm != null)
		{
			NacDatabase db = new NacDatabase(this);
			int id = alarm.getId();

			if (!alarm.getRepeat())
			{
				alarm.toggleToday();

				if (!alarm.areDaysSelected())
				{
					alarm.setEnabled(false);
				}

				db.update(alarm);
			}
			else
			{
				NacScheduler scheduler = new NacScheduler(this);
				NacAlarm actualAlarm = db.findAlarm(id);

				scheduler.scheduleNext(actualAlarm);
			}

			shared.editSnoozeCount(alarm.getId(), 0);
			db.close();
		}

		NacUtility.quickToast(this, "Alarm dismissed");
		this.finish();
	}

	/**
	 */
	public void finish()
	{
		NacWakeUpAction wakeup = this.getWakeUp();

		if (wakeup != null)
		{
			wakeup.cleanup();
		}

		super.stopForeground(true);
		super.stopSelf();
	}

	/**
	 * @return The alarm.
	 */
	private NacAlarm getAlarm()
	{
		return this.mAlarm;
	}

	/**
	 * @return The shared preferences.
	 */
	private NacSharedPreferences getSharedPreferences()
	{
		return this.mSharedPreferences;
	}

	/**
	 * @return The wake up actions.
	 */
	private NacWakeUpAction getWakeUp()
	{
		return this.mWakeUp;
	}

	/**
	 * Automatically dismiss the alarm.
	 */
	@Override
	public void onAutoDismiss(NacAlarm alarm)
	{
		NacSharedPreferences shared = this.getSharedPreferences();

		if (shared.getMissedAlarmNotification())
		{
			NacMissedAlarmNotification notification =
				new NacMissedAlarmNotification(this);

			notification.show(alarm);
		}

		this.dismiss();
	}

	/**
	 */
	@Override
	public void onCreate()
	{
		super.onCreate();

		this.mWakeLock = null;
	}

	/**
	 */
	@Override
	public void onDestroy()
	{
		super.onDestroy();

		NacWakeUpAction wakeup = this.getWakeUp();

		if (wakeup != null)
		{
			wakeup.shutdown();
		}

		if (this.mWakeLock != null)
		{
			this.mWakeLock.release();
		}
	}

	/**
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		if ((intent == null) || (intent.getAction() == null)
			|| intent.getAction().equals(ACTION_STOP_SERVICE))
		{
			this.finish();
		}
		else if (intent.getAction().equals(ACTION_START_SERVICE))
		{
			this.mSharedPreferences = new NacSharedPreferences(this);
			this.mAlarm = NacIntent.getAlarm(intent);

			this.setupWakeLock();
			this.showNotification();
			this.setupActiveAlarm();

			//return START_STICKY;
			return START_REDELIVER_INTENT;
		}
		else if (intent.getAction().equals(ACTION_SNOOZE_ALARM))
		{
			this.snooze();
		}
		else if (intent.getAction().equals(ACTION_DISMISS_ALARM))
		{
			this.dismiss();
		}

		return START_STICKY;
		//return START_NOT_STICKY;
	}

	/**
	 */
	@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}

	/**
	 * Setup the active alarm.
	 */
	public void setupActiveAlarm()
	{
		NacAlarm alarm = this.getAlarm();

		if (alarm == null)
		{
			return;
		}

		NacWakeUpAction wakeUp = new NacWakeUpAction(this, alarm);
		NacScheduler scheduler = new NacScheduler(this);
		this.mWakeUp = wakeUp;

		scheduler.scheduleNext(alarm);
		wakeUp.setOnAutoDismissListener(this);
		wakeUp.start();
	}

	/**
	 * Setup the wake lock so that the screen remains on.
	 */
	public void setupWakeLock()
	{
		NacAlarm alarm = this.getAlarm();

		if (alarm == null)
		{
			return;
		}

		String tag = "NFC Alarm Clock:NacForegroundService";
		PowerManager pm = (PowerManager) getSystemService(
			Context.POWER_SERVICE);
		this.mWakeLock = pm.newWakeLock(
			PowerManager.SCREEN_BRIGHT_WAKE_LOCK|PowerManager.ACQUIRE_CAUSES_WAKEUP,
			tag);

		this.mWakeLock.acquire();
	}

	/**
	 * Show the notification.
	 */
	public void showNotification()
	{
		NacAlarm alarm = this.getAlarm();

		if (alarm == null)
		{
			return;
		}

		NacActiveAlarmNotification notification =
			new NacActiveAlarmNotification(this, alarm);

		startForeground(notification.ID, notification.build());
	}

	/**
	 * Snooze the alarm.
	 */
	public void snooze()
	{
		NacSharedPreferences shared = this.getSharedPreferences();
		NacAlarm alarm = this.getAlarm();
		int id = alarm.getId();
		int snoozeCount = shared.getSnoozeCount(id) + 1;
		int maxSnoozeCount = shared.getMaxSnoozeValue();

		if ((snoozeCount > maxSnoozeCount) && (maxSnoozeCount >= 0))
		{
			NacUtility.quickToast(this, "Unable to snooze the alarm");
			return;
		}

		NacScheduler scheduler = new NacScheduler(this);
		Calendar snooze = Calendar.getInstance();

		snooze.add(Calendar.MINUTE, shared.getSnoozeDurationValue());
		alarm.setHour(snooze.get(Calendar.HOUR_OF_DAY));
		alarm.setMinute(snooze.get(Calendar.MINUTE));
		scheduler.update(alarm, snooze);
		shared.editSnoozeCount(id, snoozeCount);

		NacUtility.quickToast(this, "Alarm snoozed");
		this.finish();
	}

}

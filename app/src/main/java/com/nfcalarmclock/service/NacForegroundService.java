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
	implements NacWakeupProcess.OnAutoDismissListener
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
	private NacWakeupProcess mWakeup;

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
	 * Run cleanup.
	 */
	private void cleanup()
	{
		NacAlarm alarm = this.getAlarm();
		NacWakeupProcess wakeup = this.getWakeup();
		WakeLock wakeLock = this.getWakeLock();

		if (wakeup != null)
		{
			wakeup.cleanup();
		}

		if ((wakeLock != null) && wakeLock.isHeld())
		{
			wakeLock.release();
		}

		this.mWakeup = null;
		this.mWakeLock = null;
	}

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
			NacAlarm actualAlarm = db.findAlarm(id);

			if (!actualAlarm.getRepeat())
			{
				if (!actualAlarm.areDaysSelected())
				{
					actualAlarm.setEnabled(false);
				}
				else
				{
					actualAlarm.toggleToday();
				}

				db.update(actualAlarm);
			}
			else
			{
				NacScheduler.scheduleNext(this, actualAlarm);
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
		this.cleanup();
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
	private NacWakeupProcess getWakeup()
	{
		return this.mWakeup;
	}

	/**
	 * @return The wake lock.
	 */
	private WakeLock getWakeLock()
	{
		return this.mWakeLock;
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
		this.cleanup();
	}

	/**
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		this.prepareAlarm(intent);

		if ((intent == null) || (intent.getAction() == null)
			|| intent.getAction().equals(ACTION_STOP_SERVICE)
			|| (this.getAlarm() == null))
		{
			this.finish();
		}
		else if (intent.getAction().equals(ACTION_START_SERVICE))
		{
			this.setupWakeLock();
			this.showNotification();
			this.setupActiveAlarm();

			return START_STICKY;
		}
		else if (intent.getAction().equals(ACTION_SNOOZE_ALARM))
		{
			this.snooze();
		}
		else if (intent.getAction().equals(ACTION_DISMISS_ALARM))
		{
			this.dismiss();
		}

		return START_NOT_STICKY;
	}

	/**
	 */
	@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}

	/**
	 * Prepare the alarm information.
	 */
	private void prepareAlarm(Intent intent)
	{
		if (this.mSharedPreferences == null)
		{
			this.mSharedPreferences = new NacSharedPreferences(this);
		}

		if (this.mAlarm == null)
		{
			this.mAlarm = NacIntent.getAlarm(intent);
		}
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

		NacWakeupProcess wakeup = new NacWakeupProcess(this, alarm);
		NacScheduler scheduler = new NacScheduler(this);
		this.mWakeup = wakeup;

		scheduler.scheduleNext(alarm);
		wakeup.setOnAutoDismissListener(this);
		wakeup.start();
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

		NacSharedPreferences shared = this.getSharedPreferences();
		String tag = "NFC Alarm Clock:NacForegroundService";
		PowerManager pm = (PowerManager) getSystemService(
			Context.POWER_SERVICE);
		this.mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, tag);
		long timeout = shared.getAutoDismissTime() * 59 * 1000;

		this.mWakeLock.acquire(timeout);
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

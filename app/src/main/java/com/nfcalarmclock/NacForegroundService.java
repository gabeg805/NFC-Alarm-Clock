package com.nfcalarmclock;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 */
public class NacForegroundService
	extends Service
	implements NacWakeupProcess.OnAutoDismissListener
{

	/**
	 * Action to start the service.
	 */
	public static final String ACTION_START_SERVICE =
		"com.nfcalarmclock.ACTION_START_SERVICE";

	/**
	 * Action to stop the service.
	 */
	public static final String ACTION_STOP_SERVICE =
		"com.nfcalarmclock.ACTION_STOP_SERVICE";

	/**
	 * Action to snooze the alarm.
	 */
	public static final String ACTION_SNOOZE_ALARM =
		"com.nfcalarmclock.ACTION_SNOOZE_ALARM";

	/**
	 * Action to dismiss the alarm.
	 */
	public static final String ACTION_DISMISS_ALARM =
		"com.nfcalarmclock.ACTION_DISMISS_ALARM";

	/**
	 * Wakelock.
	 */
	private WakeLock mWakeLock;

	/**
	 * Wake up processes (only has multiple if more than one alarms are running
	 * at once).
	 */
	private List<NacWakeupProcess> mAllWakeups;

	/**
	 * Alarm.
	 */
	private NacAlarm mAlarm;

	/**
	 * Shared preferences.
	 */
	private NacSharedPreferences mSharedPreferences;

	/**
	 * Run cleanup.
	 */
	private void cleanup()
	{
		this.cleanupWakeupProcess();
		this.cleanupWakeLock();
	}

	/**
	 * Cleanup the wake lock.
	 */
	private void cleanupWakeLock()
	{
		WakeLock wakeLock = this.getWakeLock();

		if ((wakeLock != null) && wakeLock.isHeld())
		{
			wakeLock.release();
		}

		this.mWakeLock = null;
	}

	/**
	 * Cleanup the wake lock.
	 */
	private void cleanupWakeupProcess()
	{
		List<NacWakeupProcess> allWakeups = this.getAllWakeups();
		NacAlarm alarm = this.getAlarm();
		int index = this.indexOfWakeup(alarm);
		NacWakeupProcess wakeup = this.getWakeup(index);

		if (wakeup != null)
		{
			wakeup.cleanup();
		}

		if (allWakeups != null)
		{
			if (index >= 0)
			{
				allWakeups.remove(index);
			}
		}
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

		NacSharedConstants cons = new NacSharedConstants(this);
		NacUtility.quickToast(this, cons.getDismissedAlarm());
		this.finish();
	}

	/**
	 */
	public void finish()
	{
		this.cleanup();
		this.stopAlarmActivity();
		super.stopForeground(true);

		NacWakeupProcess nextWakeup = this.getNextWakeup();

		if (nextWakeup != null)
		{
			nextWakeup.start();
			this.mAlarm = nextWakeup.getAlarm();
			this.showNotification();
			return;
		}

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
	 * @return All wakeup processes.
	 */
	private List<NacWakeupProcess> getAllWakeups()
	{
		return this.mAllWakeups;
	}

	/**
	 * @return The next wakeup process.
	 */
	private NacWakeupProcess getNextWakeup()
	{
		return this.getWakeup(0);
	}

	/**
	 * @return The shared preferences.
	 */
	private NacSharedPreferences getSharedPreferences()
	{
		return this.mSharedPreferences;
	}

	/**
	 * @return The index of the corresponding wakeup process.
	 */
	private int indexOfWakeup(NacAlarm alarm)
	{
		List<NacWakeupProcess> allWakeups = this.getAllWakeups();

		if ((allWakeups == null) || (alarm == null))
		{
			return -1;
		}

		int id = alarm.getId();
		int i = 0;

		for (NacWakeupProcess p : allWakeups)
		{
			NacAlarm a = p.getAlarm();
			if (a.getId() == id)
			{
				return i;
			}

			i++;
		}

		return -2;
	}

	/**
	 * @return The index of the corresponding wakeup process.
	 */
	private int indexOfWakeup(NacWakeupProcess wakeup)
	{
		NacAlarm alarm = (wakeup != null) ? wakeup.getAlarm() : null;
		return this.indexOfWakeup(alarm);
	}

	/**
	 * @return The wakeup process.
	 */
	private NacWakeupProcess getWakeup(NacAlarm alarm)
	{
		int index = this.indexOfWakeup(alarm);
		return this.getWakeup(index);
	}

	private NacWakeupProcess getWakeup(int index)
	{
		List<NacWakeupProcess> allWakeups = this.getAllWakeups();
		int size = (allWakeups != null) ? allWakeups.size() : 0;

		return ((index >= 0) && (index < size)) ? allWakeups.get(index) : null;
	}

	private NacWakeupProcess getWakeup()
	{
		NacAlarm alarm = this.getAlarm();
		return this.getWakeup(alarm);
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
		this.mAllWakeups = null;
		this.mAlarm = null;
		this.mSharedPreferences = null;
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
		String action = NacIntent.getAction(intent);

		if (action.isEmpty() || action.equals(ACTION_STOP_SERVICE)
			|| (this.getAlarm() == null))
		{
			this.finish();
		}
		else if (action.equals(ACTION_START_SERVICE))
		{
			this.setupWakeLock();
			this.showNotification();
			this.setupActiveAlarm();
			return START_STICKY;
		}
		else if (action.equals(ACTION_SNOOZE_ALARM))
		{
			this.snooze();
		}
		else if (action.equals(ACTION_DISMISS_ALARM))
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
		String action = NacIntent.getAction(intent);
		NacAlarm intentAlarm = NacIntent.getAlarm(intent);
		NacAlarm currentAlarm = this.getAlarm();

		if (this.mSharedPreferences == null)
		{
			this.mSharedPreferences = new NacSharedPreferences(this);
		}

		if (intentAlarm != null)
		{
			if (!intentAlarm.equals(currentAlarm)
				&& action.equals(ACTION_START_SERVICE))
			{
				//this.cleanup();
				this.stopCurrentWakeupProcess();
			}

			this.mAlarm = intentAlarm;
		}
	}

	/**
	 * Setup the active alarm.
	 */
	public void setupActiveAlarm()
	{
		List<NacWakeupProcess> allWakeups = this.getAllWakeups();
		NacAlarm alarm = this.getAlarm();

		if (alarm == null)
		{
			return;
		}

		if (allWakeups == null)
		{
			allWakeups = new ArrayList<>();
			this.mAllWakeups = allWakeups;
		}

		NacWakeupProcess wakeup = new NacWakeupProcess(this, alarm);
		NacScheduler.scheduleNext(this, alarm);
		wakeup.setOnAutoDismissListener(this);
		wakeup.start();
		allWakeups.add(0, wakeup);
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

		this.cleanupWakeLock();

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

		//startForeground(notification.ID, notification.build());
		startForeground(alarm.getId(), notification.build());
	}

	/**
	 * Snooze the alarm.
	 */
	public void snooze()
	{
		NacSharedConstants cons = new NacSharedConstants(this);
		NacSharedPreferences shared = this.getSharedPreferences();
		NacAlarm alarm = this.getAlarm();
		int id = alarm.getId();
		int snoozeCount = shared.getSnoozeCount(id) + 1;
		int maxSnoozeCount = shared.getMaxSnoozeValue();

		if ((snoozeCount > maxSnoozeCount) && (maxSnoozeCount >= 0))
		{
			NacUtility.quickToast(this, cons.getSnoozeError());
			return;
		}

		Calendar snooze = Calendar.getInstance();

		snooze.add(Calendar.MINUTE, shared.getSnoozeDurationValue());
		alarm.setHour(snooze.get(Calendar.HOUR_OF_DAY));
		alarm.setMinute(snooze.get(Calendar.MINUTE));
		NacScheduler.update(this, alarm, snooze);
		shared.editSnoozeCount(id, snoozeCount);

		NacUtility.quickToast(this, cons.getSnoozedAlarm());
		this.finish();
	}

	/**
	 * Stop the current wakeup process.
	 */
	private void stopCurrentWakeupProcess()
	{
		List<NacWakeupProcess> allWakeups = this.getAllWakeups();
		NacWakeupProcess wakeup = this.getWakeup();

		if (wakeup != null)
		{
			wakeup.stop();
		}

		this.stopAlarmActivity();

		//this.mWakeup = null;
	}

	/**
	 * Stop the alarm activity.
	 */
	private void stopAlarmActivity()
	{
		NacAlarm alarm = this.getAlarm();
		Intent intent = new Intent(NacAlarmActivity.ACTION_STOP_ACTIVITY);
		intent = NacIntent.addAlarm(intent, alarm);

		sendBroadcast(intent);
	}

}

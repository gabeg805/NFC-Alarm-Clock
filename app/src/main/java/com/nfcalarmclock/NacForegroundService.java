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
		this.unmarkActiveAlarm();
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
		NacAlarm actualAlarm = NacDatabase.findAlarm(this, alarm);

		if (alarm != null)
		{
			// Change this next line, and the else. THis is already done when you
			// start the service.
			actualAlarm.setIsActive(false);

			if (!actualAlarm.getRepeat())
			{
				NacScheduler.toggleAlarm(this, actualAlarm);
			}
			else
			{
				NacScheduler.scheduleNext(this, actualAlarm);
			}

			shared.editSnoozeCount(actualAlarm.getId(), 0);
			shared.editShouldRefreshMainActivity(true);
		}

		NacSharedConstants cons = new NacSharedConstants(this);
		NacUtility.quickToast(this, cons.getMessageAlarmDismiss());
		this.finish();
	}

	/**
	 */
	public void finish()
	{
		this.cleanup();
		NacContext.stopAlarmActivity(this, this.getAlarm());
		super.stopForeground(true);

		// Will this even work after stopForeground()?
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
	 * Mark the active alarm in the database.
	 */
	public void markActiveAlarm()
	{
		NacAlarm alarm = this.getAlarm();
		if (alarm == null)
		{
			return;
		}

		NacDatabase db = new NacDatabase(this);
		NacAlarm actualAlarm = db.findAlarm(alarm);

		actualAlarm.setIsActive(true);
		db.update(actualAlarm);
		db.close();
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
			notification.setAlarm(alarm);
			notification.show();
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
			this.dismiss();
		}
		else if (action.equals(ACTION_START_SERVICE))
		{
			this.setupWakeLock();
			this.showNotification();
			this.setupWakeupProcess();
			this.scheduleNextAlarm();
			this.markActiveAlarm();
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
	 * Schedule the next alarm.
	 */
	public void scheduleNextAlarm()
	{
		NacAlarm alarm = this.getAlarm();
		if (alarm == null)
		{
			return;
		}

		NacScheduler.scheduleNext(this, alarm);
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
	 * Setup the wakeup process.
	 */
	public void setupWakeupProcess()
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
		wakeup.setOnAutoDismissListener(this);
		wakeup.start();
		allWakeups.add(0, wakeup);
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
			new NacActiveAlarmNotification(this);
		notification.setAlarm(alarm);
		startForeground(alarm.getId(), notification.builder().build());
	}

	/**
	 * Snooze the alarm.
	 */
	public void snooze()
	{
		NacSharedConstants cons = new NacSharedConstants(this);
		NacSharedPreferences shared = this.getSharedPreferences();
		NacAlarm alarm = this.getAlarm();

		if (!alarm.canSnooze(shared))
		{
			NacUtility.quickToast(this, cons.getErrorMessageSnooze());
			return;
		}

		Calendar cal = Calendar.getInstance();
		int snoozeCount = alarm.getSnoozeCount(shared);
		int id = alarm.getId();

		cal.add(Calendar.MINUTE, shared.getSnoozeDurationValue());
		alarm.setHour(cal.get(Calendar.HOUR_OF_DAY));
		alarm.setMinute(cal.get(Calendar.MINUTE));
		NacScheduler.update(this, alarm, cal);
		shared.editSnoozeCount(id, snoozeCount+1);
		shared.editShouldRefreshMainActivity(true);

		NacUtility.quickToast(this, cons.getMessageAlarmSnooze());
		this.finish();
	}

	/**
	 * Stop the current wakeup process.
	 */
	private void stopCurrentWakeupProcess()
	{
		List<NacWakeupProcess> allWakeups = this.getAllWakeups();
		NacWakeupProcess wakeup = this.getWakeup();
		NacAlarm alarm = this.getAlarm();

		if (wakeup != null)
		{
			wakeup.stop();
		}

		NacContext.stopAlarmActivity(this, alarm);
	}

	/**
	 * Unmark the active alarm in the database.
	 */
	public void unmarkActiveAlarm()
	{
		NacAlarm alarm = this.getAlarm();
		if (alarm == null)
		{
			return;
		}

		NacDatabase db = new NacDatabase(this);
		NacAlarm actualAlarm = db.findAlarm(alarm);

		actualAlarm.setIsActive(false);
		db.update(actualAlarm);
		db.close();
	}

}

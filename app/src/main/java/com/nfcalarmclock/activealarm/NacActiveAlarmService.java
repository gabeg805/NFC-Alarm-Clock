package com.nfcalarmclock.activealarm;

import android.app.Application;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

import com.nfcalarmclock.NacUtility;
import com.nfcalarmclock.alarm.NacAlarm;
import com.nfcalarmclock.alarm.NacAlarmRepository;
import com.nfcalarmclock.missedalarm.NacMissedAlarmNotification;
import com.nfcalarmclock.shared.NacSharedConstants;
import com.nfcalarmclock.shared.NacSharedPreferences;
import com.nfcalarmclock.statistics.NacAlarmStatisticRepository;
import com.nfcalarmclock.system.NacContext;
import com.nfcalarmclock.system.NacIntent;
import com.nfcalarmclock.system.NacScheduler;

import java.lang.System;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

/**
 */
public class NacActiveAlarmService
	extends Service
	implements Runnable
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
	 * Action to dismiss the alarm with NFC.
	 */
	public static final String ACTION_DISMISS_ALARM_WITH_NFC =
		"com.nfcalarmclock.ACTION_DISMISS_ALARM_WITH_NFC";

	/**
	 * Shared preferences.
	 */
	private NacSharedPreferences mSharedPreferences;

	/**
	 * Alarm repository.
	 */
	private NacAlarmRepository mAlarmRepository;

	/**
	 * Alarm.
	 */
	private NacAlarm mAlarm;

	/**
	 * Wakeup process, that plays music, vibrates the phone, etc.
	 */
	private NacWakeupProcess mWakeupProcess;

	/**
	 * Wakelock.
	 */
	private WakeLock mWakeLock;

	/**
	 * Automatically dismiss the alarm in case it does not get dismissed.
	 */
	private Handler mAutoDismissHandler;

	/**
	 * Time that the service was started, in milliseconds.
	 */
	private long mStartTime;

	/**
	 * Automatically dismiss the alarm.
	 *
	 * This will finish the service.
	 */
	private void autoDismiss()
	{
		this.doDismiss();
		this.saveMissedStatistic();
		this.finish();
	}

	/**
	 * Run cleanup.
	 */
	private void cleanup()
	{
		this.setIsAlarmActive(false);
		this.updateAlarm();
		this.cleanupAlarmActivity();
		this.cleanupWakeupProcess();
		this.cleanupWakeLock();
		this.cleanupAutoDismiss();
	}

	/**
	 * Cleanup the alarm activity.
	 *
	 * The alarm activity is stopped even if the alarm is null because it needs to
	 * be stopped, regardless.
	 */
	private void cleanupAlarmActivity()
	{
		NacAlarm alarm = this.getAlarm();

		NacContext.stopAlarmActivity(this, alarm);
	}

	/**
	 * Cleanup the auto dismiss handler.
	 */
	private void cleanupAutoDismiss()
	{
		Handler autoDismissHandler = this.getAutoDismissHandler();

		if (autoDismissHandler != null)
		{
			autoDismissHandler.removeCallbacksAndMessages(null);
		}
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
		NacWakeupProcess wakeup = this.getWakeupProcess();

		if (wakeup != null)
		{
			wakeup.cleanup();
		}

		this.mWakeupProcess = null;
	}

	/**
	 * Dismiss the alarm.
	 *
	 * This will finish the service.
	 */
	private void dismiss()
	{
		this.doDismiss();
		this.saveDismissedStatistic(false);
		this.finish();
	}

	/**
	 * Dismiss the alarm with NFC.
	 *
	 * This will finish the service.
	 */
	private void dismissWithNfc()
	{
		this.doDismiss();
		this.saveDismissedStatistic(true);
		this.finish();
	}

	/**
	 * Dismiss the alarm.
	 *
	 * This does not finish the service.
	 */
	private void doDismiss()
	{
		NacSharedPreferences shared = this.getSharedPreferences();
		NacSharedConstants cons = this.getSharedConstants();
		NacAlarm alarm = this.getAlarm();
		NacAlarmRepository repo = this.getAlarmRepository();

		if (alarm != null)
		{
			shared.editShouldRefreshMainActivity(true);
			alarm.dismiss();
			repo.update(alarm);
			NacScheduler.update(this, alarm);
		}

		NacUtility.quickToast(this, cons.getMessageAlarmDismiss());
	}

	/**
	 */
	public void finish()
	{
		this.cleanup();
		NacContext.startMainActivity(this);
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
	 * @return The alarm repository.
	 */
	private NacAlarmRepository getAlarmRepository()
	{
		return this.mAlarmRepository;
	}

	/**
	 * @return The auto dismiss handler.
	 */
	private Handler getAutoDismissHandler()
	{
		return this.mAutoDismissHandler;
	}

	/**
	 * @return The shared constants.
	 */
	private NacSharedConstants getSharedConstants()
	{
		return this.getSharedPreferences().getConstants();
	}

	/**
	 * @return The shared preferences.
	 */
	private NacSharedPreferences getSharedPreferences()
	{
		return this.mSharedPreferences;
	}

	/**
	 * Get the time the service was started, in milliseconds.
	 *
	 * @return The time the service was started.
	 */
	private long getStartTime()
	{
		return this.mStartTime;
	}

	/**
	 * @return The wakeup process.
	 */
	private NacWakeupProcess getWakeupProcess()
	{
		return this.mWakeupProcess;
	}

	/**
	 * @return The wake lock.
	 */
	private WakeLock getWakeLock()
	{
		return this.mWakeLock;
	}

	/**
	 * Check if a new service, with a different alarm, was started.
	 *
	 * @param  intent An Intent.
	 *
	 * @return True if a new service was started, and False otherwise.
	 */
	private boolean isNewServiceStarted(Intent intent)
	{
		NacAlarm alarm = this.getAlarm();
		NacAlarm intentAlarm = NacIntent.getAlarm(intent);
		String action = NacIntent.getAction(intent);

		return (intentAlarm != null) && !intentAlarm.equals(alarm)
			&& action.equals(ACTION_START_SERVICE);
	}

	/**
	 * Automatically dismiss the alarm.
	 */
	@Override
	public void run()
	{
		NacSharedPreferences shared = this.getSharedPreferences();
		NacAlarm alarm = this.getAlarm();

		if (shared.getMissedAlarmNotification())
		{
			NacMissedAlarmNotification notification =
				new NacMissedAlarmNotification(this);
			notification.setAlarm(alarm);
			notification.show();
		}

		this.autoDismiss();
	}

	/**
	 */
	@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}

	/**
	 */
	@Override
	public void onCreate()
	{
		super.onCreate();

		Application app = getApplication();
		this.mSharedPreferences = new NacSharedPreferences(this);
		// TODO: See if this needs to be in here or can be localized
		this.mAlarmRepository = new NacAlarmRepository(app);
		this.mAlarm = null;
		this.mWakeupProcess = null;
		this.mWakeLock = null;
		//this.mStartTime = System.currentTimeMillis();
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
		if (this.isNewServiceStarted(intent))
		{
			this.updateTimeActive();
			this.updateAlarm();
			this.cleanupAlarmActivity();
			this.cleanupWakeupProcess();
			this.cleanupAutoDismiss();
		}

		String action = NacIntent.getAction(intent);
		NacAlarm alarm = NacIntent.getAlarm(intent);
		this.mAlarm = alarm;

		if ((alarm == null) || action.isEmpty()
			|| action.equals(ACTION_STOP_SERVICE)
			|| action.equals(ACTION_DISMISS_ALARM))
		{
			this.dismiss();
		}
		else if (action.equals(ACTION_DISMISS_ALARM_WITH_NFC))
		{
			this.dismissWithNfc();
		}
		else if (action.equals(ACTION_SNOOZE_ALARM))
		{
			this.snooze();
		}
		else if (action.equals(ACTION_START_SERVICE))
		{
			this.showNotification();
			this.setupWakeLock();
			this.setupWakeupProcess();
			// Might not need schedule next, since doing one day, per alarm, at a time.
			//this.scheduleNextAlarm();
			this.setIsAlarmActive(true);
			this.updateAlarm();
			this.waitForAutoDismiss();
			return START_STICKY;
		}

		return START_NOT_STICKY;
	}

	/**
	 * Save the dismissed statistic to the database.
	 *
	 * @param  usedNfc  Whether NFC was used to dismiss the alarm or not.
	 */
	private void saveDismissedStatistic(boolean usedNfc)
	{
		Application app = getApplication();
		NacAlarmStatisticRepository repo = new NacAlarmStatisticRepository(app);
		NacAlarm alarm = this.getAlarm();

		repo.insertDismissed(alarm, usedNfc);
	}

	/**
	 * Save the missed statistic to the database.
	 */
	private void saveMissedStatistic()
	{
		Application app = getApplication();
		NacAlarmStatisticRepository repo = new NacAlarmStatisticRepository(app);
		NacAlarm alarm = this.getAlarm();

		repo.insertMissed(alarm);
	}

	/**
	 * Save the snoozed statistic to the database.
	 */
	private void saveSnoozedStatistic()
	{
		Application app = getApplication();
		NacAlarmStatisticRepository repo = new NacAlarmStatisticRepository(app);
		NacSharedPreferences shared = this.getSharedPreferences();
		NacAlarm alarm = this.getAlarm();
		long duration = 60L * shared.getSnoozeDurationValue();

		repo.insertSnoozed(alarm, duration);
	}

	/**
	 * Set whether the alarm is active or not.
	 *
	 * @param  isActive  Whether the alarm is active or not.
	 */
	private void setIsAlarmActive(boolean isActive)
	{
		NacAlarm alarm = this.getAlarm();

		if (alarm != null)
		{
			alarm.setIsActive(isActive);
		}
	}

	/**
	 * Setup the wake lock so that the screen remains on.
	 */
	public void setupWakeLock()
	{
		this.cleanupWakeLock();

		NacSharedPreferences shared = this.getSharedPreferences();
		String tag = "NFC Alarm Clock:NacForegroundService";
		PowerManager pm = (PowerManager) getSystemService(
			Context.POWER_SERVICE);
		this.mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, tag);
		long timeout = shared.getAutoDismissTime() * 60 * 1000;

		this.mWakeLock.acquire(timeout);
	}

	/**
	 * Setup the wakeup process.
	 */
	public void setupWakeupProcess()
	{
		NacAlarm alarm = this.getAlarm();
		NacWakeupProcess wakeup = new NacWakeupProcess(this, alarm);
		this.mWakeupProcess = wakeup;

		wakeup.start();
	}

	/**
	 * Show the notification.
	 */
	public void showNotification()
	{
		NacAlarm alarm = this.getAlarm();
		NacActiveAlarmNotification notification =
			new NacActiveAlarmNotification(this);

		notification.setAlarm(alarm);
		startForeground((int)alarm.getId(), notification.builder().build());
	}

	/**
	 * Snooze the alarm.
	 *
	 * This will finish the service.
	 */
	public void snooze()
	{
		NacSharedPreferences shared = this.getSharedPreferences();
		NacSharedConstants cons = this.getSharedConstants();
		NacAlarm alarm = this.getAlarm();
		Calendar cal = alarm.snooze(shared);

		if (cal != null)
		{
			shared.editShouldRefreshMainActivity(true);
			this.updateTimeActive();
			this.updateAlarm();
			this.saveSnoozedStatistic();
			NacScheduler.update(this, alarm, cal);

			NacUtility.quickToast(this, cons.getMessageAlarmSnooze());
			this.finish();
		}
		else
		{
			NacUtility.quickToast(this, cons.getErrorMessageSnooze());
		}
	}

	/**
	 * Update the alarm in the repository.
	 */
	private void updateAlarm()
	{
		NacAlarmRepository repo = this.getAlarmRepository();
		NacAlarm alarm = this.getAlarm();

		if (alarm != null)
		{
			repo.update(alarm);
		}
	}

	/**
	 * Update the time the alarm was active.
	 */
	private void updateTimeActive()
	{
		NacAlarm alarm = this.getAlarm();
		long timeActive = System.currentTimeMillis() - this.getStartTime();

		if (alarm != null)
		{
			alarm.addToTimeActive(timeActive);
		}
	}

	/**
	 * Wait in the background until the activity needs to auto dismiss the
	 * alarm.
	 *
	 * Auto dismiss a bit early to avoid the race condition between a new alarm
	 * starting at the same time that the alarm will auto-dismiss.
	 */
	public void waitForAutoDismiss()
	{
		NacSharedPreferences shared = this.getSharedPreferences();
		NacAlarm alarm = this.getAlarm();
		Handler handler = null;
		int autoDismiss = shared.getAutoDismissTime();
		long delay = TimeUnit.MINUTES.toMillis(autoDismiss) - alarm.getTimeActive() - 2000;

		if (autoDismiss != 0)
		{
			handler = new Handler();
			handler.postDelayed(this, delay);
		}

		this.mAutoDismissHandler = handler;
		this.mStartTime = System.currentTimeMillis();
	}

}

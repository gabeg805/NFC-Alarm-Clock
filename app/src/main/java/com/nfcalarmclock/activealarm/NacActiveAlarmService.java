package com.nfcalarmclock.activealarm;

import android.app.Application;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

import com.nfcalarmclock.alarm.NacAlarm;
import com.nfcalarmclock.alarm.NacAlarmRepository;
import com.nfcalarmclock.missedalarm.NacMissedAlarmNotification;
import com.nfcalarmclock.shared.NacSharedConstants;
import com.nfcalarmclock.shared.NacSharedPreferences;
import com.nfcalarmclock.statistics.NacAlarmStatisticRepository;
import com.nfcalarmclock.system.NacContext;
import com.nfcalarmclock.system.NacIntent;
import com.nfcalarmclock.scheduler.NacScheduler;
import com.nfcalarmclock.util.NacUtility;

import java.lang.System;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

/**
 */
public class NacActiveAlarmService
	extends Service
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
		Handler handler = this.getAutoDismissHandler();

		if (handler != null)
		{
			handler.removeCallbacksAndMessages(null);
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
		NacSharedConstants cons = this.getSharedConstants();
		NacAlarm alarm = this.getAlarm();

		if (alarm != null)
		{
			alarm.dismiss();
			this.updateAlarm();
			this.setupRefreshMainActivity();
			NacScheduler.update(this, alarm);
		}

		NacUtility.quickToast(this, cons.getMessageAlarmDismiss());
	}

	/**
	 * Snooze the alarm.
	 *
	 * This does not finish the service.
	 */
	private boolean doSnooze()
	{
		NacSharedPreferences shared = this.getSharedPreferences();
		NacSharedConstants cons = this.getSharedConstants();
		NacAlarm alarm = this.getAlarm();
		Calendar cal = alarm.snooze(shared);

		if (cal != null)
		{
			this.updateTimeActive();
			this.updateAlarm();
			this.setupRefreshMainActivity();
			NacScheduler.update(this, alarm, cal);

			NacUtility.quickToast(this, cons.getMessageAlarmSnooze());
			return true;
		}
		else
		{
			NacUtility.quickToast(this, cons.getErrorMessageSnooze());
			return false;
		}
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
		// TODO: See if this needs to be in here or can be localized
		NacAlarmRepository repo = this.mAlarmRepository;

		if (repo == null)
		{
			Application app = getApplication();
			repo = new NacAlarmRepository(app);
			this.mAlarmRepository = repo;
		}

		return repo;
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

		return (alarm != null) && (intentAlarm != null)
			&& !intentAlarm.equals(alarm)
			&& action.equals(ACTION_START_SERVICE);
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
		//super.onCreate();

		Application app = getApplication();
		Context context = getApplicationContext();

		this.mSharedPreferences = new NacSharedPreferences(context);
		this.mAlarmRepository = new NacAlarmRepository(app);
		this.mAlarm = null;
		this.mWakeupProcess = new NacWakeupProcess(this);
		this.mWakeLock = null;
		this.mAutoDismissHandler = new Handler(context.getMainLooper());
		//this.mStartTime = System.currentTimeMillis();
	}

	/**
	 */
	@Override
	public void onDestroy()
	{
		//super.onDestroy();
		this.cleanup();
	}

	/**
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		String action = NacIntent.getAction(intent);

		this.setupService(intent);

		if ((this.getAlarm() == null) || action.isEmpty()
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
			this.setIsAlarmActive(true);
			this.updateAlarm();
			this.waitForAutoDismiss();

			return START_STICKY;
		}

		return START_NOT_STICKY;
	}

	/**
	 * Prepare the new service that was started.
	 */
	public void prepareNewService()
	{
		this.updateTimeActive();
		this.updateAlarm();
		this.cleanupAlarmActivity();
		this.cleanupWakeupProcess();
		this.cleanupAutoDismiss();
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
	 * Setup refreshing the main activity.
	 */
	private void setupRefreshMainActivity()
	{
		NacSharedPreferences shared = this.getSharedPreferences();

		shared.editShouldRefreshMainActivity(true);
	}

	/**
	 * Setup the service.
	 */
	private void setupService(Intent intent)
	{
		NacAlarm intentAlarm = NacIntent.getAlarm(intent);

		// Prepare a new service
		if (this.isNewServiceStarted(intent))
		{
			this.prepareNewService();
		}

		// Define the new alarm for this service
		if (intentAlarm != null)
		{
			this.mAlarm = intentAlarm;
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
		long timeout = (long) shared.getAutoDismissTime() * 60L * 1000L;

		this.mWakeLock.acquire(timeout);
	}

	/**
	 * Setup the wakeup process.
	 */
	public void setupWakeupProcess()
	{
		NacWakeupProcess wakeup = this.getWakeupProcess();
		NacAlarm alarm = this.getAlarm();

		wakeup.start(alarm);
	}

	/**
	 * Show the notification.
	 */
	public void showNotification()
	{
		NacAlarm alarm = this.getAlarm();
		NacActiveAlarmNotification notification =
			new NacActiveAlarmNotification(this);

		// Set the alarm to be part of the notification
		notification.setAlarm(alarm);

		// Start the service in the foreground
		startForeground(NacActiveAlarmNotification.ID,
			notification.builder().build());
	}

	/**
	 * Snooze the alarm.
	 *
	 * This will finish the service.
	 */
	public void snooze()
	{
		if (this.doSnooze())
		{
			this.saveSnoozedStatistic();
			this.finish();
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
		Handler handler = this.getAutoDismissHandler();

		// Amount of time until the alarm is automatically dismissed
		int autoDismiss = shared.getAutoDismissTime();
		long delay = TimeUnit.MINUTES.toMillis(autoDismiss) - alarm.getTimeActive() - 2000;

		// There is an auto dismiss time set
		if (autoDismiss != 0)
		{
			// Cleanup the auto dismiss handler, in case it is already set
			this.cleanupAutoDismiss();

			// Automatically dismiss the alarm.
			handler.postDelayed(() -> {

				// Show the missed alarm notification
				if (shared.getMissedAlarmNotification())
				{
					NacMissedAlarmNotification notification =
						new NacMissedAlarmNotification(NacActiveAlarmService.this);
					notification.setAlarm(alarm);
					notification.show();
				}

				// Auto dismiss
				autoDismiss();

			}, delay);
		}

		this.mStartTime = System.currentTimeMillis();
	}

}

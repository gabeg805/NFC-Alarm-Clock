package com.nfcalarmclock;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
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
	 * Run cleanup.
	 */
	private void cleanup()
	{
		this.writeIsAlarmActive(false);
		this.cleanupAlarmActivity();
		this.cleanupWakeupProcess();
		this.cleanupWakeLock();
	}

	/**
	 * Cleanup the alarm activity.
	 */
	private void cleanupAlarmActivity()
	{
		NacAlarm alarm = this.getAlarm();

		if (alarm != null)
		{
			NacUtility.printf("Foreground service -- Stop alarm activity!");
			NacContext.stopAlarmActivity(this, alarm);
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
			NacUtility.printf("Foreground service -- Stop wakeup process!");
			wakeup.cleanup();
		}

		this.mWakeupProcess = null;
	}

	/**
	 * Dismiss the alarm.
	 */
	private void dismiss()
	{
		NacSharedPreferences shared = this.getSharedPreferences();
		NacSharedConstants cons = this.getSharedConstants();
		NacAlarm alarm = this.getAlarm();
		NacAlarmRepository repo = this.getAlarmRepository();

		NacUtility.printf("Dismissing the alarm in the service? %b || %d", alarm != null, (alarm != null) ? alarm.getId() : -1);

		if (alarm != null)
		{
			alarm.print();
			NacUtility.printf("YOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO");
			alarm.dismiss();
			repo.update(alarm);
			NacScheduler.update(this, alarm);
			shared.editShouldRefreshMainActivity(true);
		}

		NacUtility.quickToast(this, cons.getMessageAlarmDismiss());
		this.finish();
	}

	/**
	 */
	public void finish()
	{
		NacUtility.printf("Finishing foreground service!");
		this.cleanup();
		// Should I call cleanupAlarmActivity() even if alarm is null?
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

		this.mSharedPreferences = new NacSharedPreferences(this);
		this.mAlarmRepository = new NacAlarmRepository(getApplication());
		this.mAlarm = null;
		this.mWakeupProcess = null;
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
		if (this.isNewServiceStarted(intent))
		{
			NacUtility.printf("Stopping the current process due to the intent alarm!");
			this.cleanupWakeupProcess();
			this.cleanupAlarmActivity();
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
		else if (action.equals(ACTION_SNOOZE_ALARM))
		{
			this.snooze();
		}
		else if (action.equals(ACTION_START_SERVICE))
		{
			this.setupWakeLock();
			this.showNotification();
			this.setupWakeupProcess();
			// Might not need schedule next, since doing one day, per alarm, at a time.
			//this.scheduleNextAlarm();
			this.writeIsAlarmActive(true);
			return START_STICKY;
		}

		return START_NOT_STICKY;
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
		long timeout = shared.getAutoDismissTime() * 59 * 1000;

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

		wakeup.setOnAutoDismissListener(this);
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
	 */
	public void snooze()
	{
		NacSharedPreferences shared = this.getSharedPreferences();
		NacSharedConstants cons = this.getSharedConstants();
		NacAlarmRepository repo = this.getAlarmRepository();
		NacAlarm alarm = this.getAlarm();
		Calendar cal = alarm.snooze(shared);

		if (cal != null)
		{
			shared.editShouldRefreshMainActivity(true);
			repo.update(alarm);
			NacScheduler.update(this, alarm, cal);

			NacUtility.quickToast(this, cons.getMessageAlarmSnooze());
			this.finish();
		}
		else
		{
			NacUtility.quickToast(this, cons.getErrorMessageSnooze());
			return;
		}
	}

	/**
	 * Write to the database whether the alarm is active or not.
	 *
	 * @param  isActive  Whether the alarm is active or not.
	 */
	public void writeIsAlarmActive(boolean isActive)
	{
		NacAlarmRepository repo = this.getAlarmRepository();
		NacAlarm alarm = this.getAlarm();

		alarm.setIsActive(isActive);
		repo.update(alarm);
	}

}

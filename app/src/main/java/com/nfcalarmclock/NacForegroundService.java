package com.nfcalarmclock;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
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
	 * Flag if alarm was dismissed or not.
	 */
	private boolean mWasDismissed;

	/**
	 * Shared preferences.
	 */
	private NacSharedPreferences mSharedPreferences;

	/**
	 * Alarm.
	 */
	private NacAlarm mAlarm;


	/**
	 * Dismiss the alarm.
	 */
	private void dismiss()
	{
		NacSharedPreferences shared = this.getSharedPreferences();
		NacAlarm alarm = this.getAlarm();
		this.mWasDismissed = true;

		if (alarm != null)
		{
			if (!alarm.getRepeat())
			{
				alarm.toggleToday();

				if (!alarm.areDaysSelected())
				{
					alarm.setEnabled(false);
				}

				NacDatabase db = new NacDatabase(this);
				//NacNotification notification = new NacNotification(this);

				//notification.hide(alarm);
				db.update(alarm);
				db.close();
			}

			shared.editSnoozeCount(alarm.getId(), 0);
		}

		NacUtility.quickToast(this, "Alarm dismissed");
		this.finish();
	}

	/**
	 */
	public void finish()
	{
		this.getWakeUp().cleanup();
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
	public void onDestroy()
	{
		super.onDestroy();
		this.getWakeUp().shutdown();
	}

	/**
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		NacUtility.printf("onStartCommand! Intent = %b | Flags = %d | StartId = %d",
			intent != null, flags, startId);

		if (intent == null)
		{
			NacUtility.printf("onStartCommand! intent is null");
		}

		if ((intent != null) && (intent.getAction() == null))
		{
			NacUtility.printf("onStartCommand! intent ACTION is null");
		}

		if (intent.getAction().equals(ACTION_START_SERVICE))
		{
			NacUtility.printf("Received Start Foreground Intent ");
			this.mSharedPreferences = new NacSharedPreferences(this);
			this.mAlarm = NacIntent.getAlarm(intent);

			this.showNotification();
			this.setupActiveAlarm();
		}
		else if (intent.getAction().equals(ACTION_STOP_SERVICE))
		{
			NacUtility.printf("Received Stop Foreground Intent");
			this.finish();
		}
		else if (intent.getAction().equals(ACTION_SNOOZE_ALARM))
		{
			NacUtility.printf("Clicked Previous");
			this.snooze();
		}
		else if (intent.getAction().equals(ACTION_DISMISS_ALARM))
		{
			NacUtility.printf("Clicked Play");
			this.dismiss();
		}

		return START_STICKY;
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
		this.mWasDismissed = false;

		scheduler.scheduleNext(alarm);
		wakeUp.setOnAutoDismissListener(this);
		wakeUp.start();
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
		this.mWasDismissed = true;

		snooze.add(Calendar.MINUTE, shared.getSnoozeDurationValue());
		alarm.setHour(snooze.get(Calendar.HOUR_OF_DAY));
		alarm.setMinute(snooze.get(Calendar.MINUTE));
		scheduler.update(alarm, snooze);
		shared.editSnoozeCount(id, snoozeCount);

		NacUtility.quickToast(this, "Alarm snoozed");
		this.finish();
	}

	/**
	 * @return True if the alarm was dismissed, and False otherwise.
	 */
	public boolean wasDismissed()
	{
		return this.mWasDismissed;
	}

}

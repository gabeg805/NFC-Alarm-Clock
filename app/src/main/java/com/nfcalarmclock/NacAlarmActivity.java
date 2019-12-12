package com.nfcalarmclock;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import java.util.Calendar;

/**
 * Activity to dismiss/snooze the alarm.
 */
public class NacAlarmActivity
	extends Activity
	implements View.OnClickListener,
		NacWakeUpAction.OnAutoDismissListener
{

	/**
	 * Shared preferences.
	 */
	private NacSharedPreferences mSharedPreferences;

	/**
	 * Actions to take upon waking up, such as enabling NFC, playing music, etc.
	 */
	private NacWakeUpAction mWakeUp;

	/**
	 * Alarm.
	 */
	private NacAlarm mAlarm;

	/**
	 * Flag indicating alarm was dismissed.
	 */
	private boolean mWasDismissed;

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

		finish();
	}

	/**
	 */
	@Override
	public void finish()
	{
		this.getWakeUp().cleanup();
		super.finish();
	}

	/**
	 * @return The alarm.
	 */
	private NacAlarm getAlarm()
	{
		return this.mAlarm;
	}

	/**
	 * @return The wake up actions.
	 */
	private NacWakeUpAction getWakeUp()
	{
		return this.mWakeUp;
	}

	/**
	 * @return The shared preferences.
	 */
	private NacSharedPreferences getSharedPreferences()
	{
		return this.mSharedPreferences;
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
	 * Do not let the user back out of the activity.
	 */
	@Override
	public void onBackPressed()
	{
	}

	/**
	 */
	@Override
	public void onClick(View view)
	{
		NacSharedPreferences shared = this.getSharedPreferences();
		int id = view.getId();

		if ((id == R.id.snooze) || ((id == R.id.act_alarm)
			&& shared.getEasySnooze()))
		{
			if (this.snooze())
			{
				NacUtility.quickToast(this, "Alarm snoozed");
				finish();
			}
			else
			{
				NacUtility.quickToast(this, "Unable to snooze the alarm");
			}

		}
		else if (id == R.id.dismiss)
		{
			NacUtility.quickToast(this, "Alarm dismissed");
			this.dismiss();
		}
	}

	/**
	 * Create the activity.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.act_alarm);
		this.setAlarm(savedInstanceState);

		NacAlarm alarm = this.getAlarm();
		NacWakeUpAction wakeUp = new NacWakeUpAction(this, alarm);
		NacScheduler scheduler = new NacScheduler(this);
		this.mSharedPreferences = new NacSharedPreferences(this);
		this.mWakeUp = wakeUp;
		this.mWasDismissed = false;

		if (alarm == null)
		{
			return;
		}

		scheduler.scheduleNext(alarm);
		this.setupShowWhenLocked();
		this.setupAlarmButtons();
		this.setupAlarmInfo();
		wakeUp.setOnAutoDismissListener(this);
		wakeUp.start();
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
	 * NFC tag discovered so dismiss the dialog.
	 *
	 * Note: Parent method must be called last. Causes issues with
	 * setSnoozeCount.
	 */
	@Override
	protected void onNewIntent(Intent intent)
	{
		this.dismiss();
		super.onNewIntent(intent);
	}

	/**
	 * Disable tag discovery.
	 */
	@Override
	public void onPause()
	{
		super.onPause();
		this.getWakeUp().pause();
	}

	/**
	 * Enable tag discovery.
	 */
	@Override
	public void onResume()
	{
		super.onResume();
		this.getWakeUp().resume();
	}

	/**
	 * Save the alarm before the activity is killed.
	 */
	@Override
	public void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);

		NacAlarm alarm = this.getAlarm();

		if (alarm != null)
		{
			outState.putParcelable(NacBundle.ALARM_PARCEL_NAME, alarm);
		}
	}

	@Override
	public void onStop()
	{
		super.onStop();

		if (!this.mWasDismissed)
		{
			Intent intent = getIntent();

			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			this.getWakeUp().cleanup();
			startActivity(intent);
		}
	}

	/**
	 * Set the alarm.
	 */
	public void setAlarm(Bundle savedInstanceState)
	{
		NacAlarm alarm = NacIntent.getAlarm(getIntent());

		if (alarm == null)
		{
			alarm = NacBundle.getAlarm(savedInstanceState);
		}

		if (alarm == null)
		{
			NacDatabase db = new NacDatabase(this);
			alarm = db.findAlarm(Calendar.getInstance());
			db.close();
		}

		if (alarm == null)
		{
			super.finish();
		}

		this.mAlarm = alarm;
	}

	/**
	 * Setup the snooze and dismiss buttons.
	 */
	public void setupAlarmButtons()
	{
		NacSharedPreferences shared = this.getSharedPreferences();
		NacAlarm alarm = this.getAlarm();
		RelativeLayout layout = (RelativeLayout) findViewById(R.id.act_alarm);
		Button snoozeButton = (Button) findViewById(R.id.snooze);
		Button dismissButton = (Button) findViewById(R.id.dismiss);

		if ((alarm != null) && NacNfc.exists(this) && alarm.getUseNfc())
		{
			dismissButton.setVisibility(View.GONE);
		}
		else
		{
			dismissButton.setVisibility(View.VISIBLE);
		}

		snoozeButton.setTextColor(shared.getThemeColor());
		dismissButton.setTextColor(shared.getThemeColor());
		layout.setOnClickListener(this);
		snoozeButton.setOnClickListener(this);
		dismissButton.setOnClickListener(this);
	}

	/**
	 * Setup the informational message at the bottom of the screen.
	 */
	public void setupAlarmInfo()
	{
		NacSharedPreferences shared = this.getSharedPreferences();
		NacAlarm alarm = this.getAlarm();

		if ((alarm != null) && shared.getShowAlarmInfo())
		{
			TextView name = (TextView) findViewById(R.id.name);
			String alarmName = alarm.getName();

			name.setText(alarmName);
			name.setSelected(true);
		}
	}

	/**
	 * Show the activity when the phone is locked.
	 */
	 @SuppressWarnings("deprecation")
	 public void setupShowWhenLocked()
	 {
	 	NacAlarm alarm = this.getAlarm();

		if (alarm.getUseNfc())
		{
			return;
		}

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1)
		{
			setTurnScreenOn(true);
			setShowWhenLocked(true);
		}
		else
		{
			Window window = getWindow();

			window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
			window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
		}
	 }

	/**
	 * Snooze the alarm.
	 */
	public boolean snooze()
	{
		NacSharedPreferences shared = this.getSharedPreferences();
		NacAlarm alarm = this.getAlarm();
		int id = alarm.getId();
		int snoozeCount = shared.getSnoozeCount(id) + 1;
		int maxSnoozeCount = shared.getMaxSnoozeValue();

		if ((snoozeCount > maxSnoozeCount) && (maxSnoozeCount >= 0))
		{
			return false;
		}

		NacScheduler scheduler = new NacScheduler(this);
		Calendar snooze = Calendar.getInstance();

		snooze.add(Calendar.MINUTE, shared.getSnoozeDurationValue());
		alarm.setHour(snooze.get(Calendar.HOUR_OF_DAY));
		alarm.setMinute(snooze.get(Calendar.MINUTE));
		scheduler.update(alarm, snooze);
		shared.editSnoozeCount(id, snoozeCount);

		return true;
	}

}

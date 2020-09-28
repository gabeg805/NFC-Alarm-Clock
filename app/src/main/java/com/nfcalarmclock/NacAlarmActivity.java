package com.nfcalarmclock;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import java.util.Calendar;

/**
 * Activity to dismiss/snooze the alarm.
 */
public class NacAlarmActivity
	extends Activity
	implements View.OnClickListener
{

	/**
	 * Receiver to stop the activity when the foreground service is done.
	 */
	private class StopActivityReceiver
		extends BroadcastReceiver
	{

		/**
		 */
		@Override
		public void onReceive(Context context, Intent intent)
		{
			String action = NacIntent.getAction(intent);
			NacAlarm intentAlarm = NacIntent.getAlarm(intent);

			if (action.equals(NacAlarmActivity.ACTION_STOP_ACTIVITY))
			{
				finish();
			}
		}

	}

	/**
	 * Shared preferences.
	 */
	private NacSharedPreferences mSharedPreferences;

	/**
	 * Alarm.
	 */
	private NacAlarm mAlarm;

	/**
	 * Stop the activity action.
	 */
	public static final String ACTION_STOP_ACTIVITY =
		"com.nfcalarmclock.ACTION_STOP_ALARM_ACTIVITY";

	/**
	 * Receiver to stop the activity.
	 */
    private StopActivityReceiver mStopReceiver;

	/**
	 * Cleanup NFC.
	 */
	private void cleanupNfc()
	{
		NacNfc.stop(this);
	}

	/**
	 * Cleanup the Stop receiver.
	 */
	private void cleanupStopReceiver()
	{
		StopActivityReceiver receiver = this.getStopReceiver();
		unregisterReceiver(receiver);
	}

	/**
	 * Dismiss the alarm.
	 */
	public void dismiss()
	{
		NacAlarm alarm = this.getAlarm();
		Intent dismissIntent =  new Intent(
			NacForegroundService.ACTION_DISMISS_ALARM, null, this,
			NacForegroundService.class);
		dismissIntent = NacIntent.addAlarm(dismissIntent, alarm);

		startService(dismissIntent);
		finish();
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
	 * @return The stop receiver.
	 */
	private StopActivityReceiver getStopReceiver()
	{
		return this.mStopReceiver;
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
			this.snooze();

		}
		else if (id == R.id.dismiss)
		{
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
		this.mSharedPreferences = new NacSharedPreferences(this);
		this.mStopReceiver = new StopActivityReceiver();

		if (NacNfc.wasScanned(this, getIntent()))
		{
			this.dismiss();
			return;
		}

		this.setupShowWhenLocked();
		this.setupAlarmButtons();
		this.setupAlarmInfo();
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
		//super.onNewIntent(intent);
	}

	/**
	 */
	@Override
	public void onPause()
	{
		super.onPause();
		this.cleanupNfc();
		this.cleanupStopReceiver();
	}

	/**
	 */
	@Override
	public void onResume()
	{
		super.onResume();
		this.setupNfc();
		this.setupAlarmInstructions();
		this.setupStopReceiver();
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
			alarm = NacDatabase.findAlarm(this, Calendar.getInstance());
		}

		if (alarm == null)
		{
			finish();
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
			String alarmName = alarm.getNameNormalized();

			name.setText(alarmName);
			name.setSelected(true);
		}
	}

	/**
	 * Setup the instruction message that appears just above the Snooze and
	 * Dismiss buttons.
	 */
	public void setupAlarmInstructions()
	{
		if (!NacNfc.isEnabled(this))
		{
			TextView instructions = (TextView) findViewById(R.id.instructions);
			instructions.setVisibility(View.GONE);
		}
	}

	/**
	 * Setup NFC.
	 */
	private void setupNfc()
	{
		if (!NacNfc.isEnabled(this))
		{
			if (this.shouldUseNfc())
			{
				NacNfc.prompt(this);
			}
			else
			{
				return;
			}
		}

		if (NacNfc.exists(this))
		{
			Intent intent = new Intent(this, NacAlarmActivity.class)
				.addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);
			NacNfc.start(this, intent);
		}
		else
		{
			NacSharedConstants cons = new NacSharedConstants(this);
			NacUtility.quickToast(this,
				cons.getErrorMessageNfcUnsupported());
		}
	}

	/**
	 * Show the activity when the phone is locked.
	 */
	 @SuppressWarnings("deprecation")
	 public void setupShowWhenLocked()
	 {
	 	NacAlarm alarm = this.getAlarm();
		Window window = getWindow();

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1)
		{
			setTurnScreenOn(true);

			if ((alarm != null) && !alarm.getUseNfc())
			{
				setShowWhenLocked(true);
			}
		}
		else
		{

			window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

			if ((alarm != null) && !alarm.getUseNfc())
			{
				window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
			}
		}

		window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	 }

	/**
	 * Setup the receiver for the Stop signal.
	 */
	private void setupStopReceiver()
	{
		StopActivityReceiver receiver = this.getStopReceiver();
		IntentFilter filter = new IntentFilter(
			NacAlarmActivity.ACTION_STOP_ACTIVITY);
		registerReceiver(receiver, filter);
	}

	/**
	 * @return True if should use NFC, and False otherwise.
	 */
	public boolean shouldUseNfc()
	{
		NacAlarm alarm = this.getAlarm();
		return (alarm != null) && alarm.getUseNfc();
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
			NacSharedConstants cons = new NacSharedConstants(this);
			NacUtility.quickToast(this, cons.getErrorMessageSnooze());
			return;
		}

		Intent snoozeIntent = new Intent(
			NacForegroundService.ACTION_SNOOZE_ALARM, null, this,
			NacForegroundService.class);
		snoozeIntent = NacIntent.addAlarm(snoozeIntent, alarm);

		startService(snoozeIntent);
		finish();
	}

}

package com.nfcalarmclock;

import android.app.Activity;
import android.content.Intent;
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
		super.onNewIntent(intent);
	}

	/**
	 * Disable tag discovery.
	 */
	@Override
	public void onPause()
	{
		super.onPause();
		NacNfc.disable(this);
	}

	/**
	 * Enable tag discovery.
	 */
	@Override
	public void onResume()
	{
		super.onResume();

		if (NacNfc.exists(this))
		{
			NacNfc.enable(this);
		}
		else
		{
			if (this.shouldUseNfc())
			{
				NacUtility.quickToast(this, "Your device doesn't support NFC");
			}
		}
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
			NacDatabase db = new NacDatabase(this);
			alarm = db.findAlarm(Calendar.getInstance());
			db.close();
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
	 * @return True if should use NFC, and False otherwise.
	 */
	public boolean shouldUseNfc()
	{
		NacAlarm alarm = this.getAlarm();

		return ((alarm != null) && alarm.getUseNfc());
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

		Intent snoozeIntent = new Intent(
			NacForegroundService.ACTION_SNOOZE_ALARM, null, this,
			NacForegroundService.class);
		snoozeIntent = NacIntent.addAlarm(snoozeIntent, alarm);

		startService(snoozeIntent);
		finish();
	}

}
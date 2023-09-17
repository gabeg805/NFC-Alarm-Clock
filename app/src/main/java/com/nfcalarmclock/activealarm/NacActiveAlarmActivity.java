package com.nfcalarmclock.activealarm;

import android.annotation.SuppressLint;
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
import androidx.annotation.NonNull;
import com.nfcalarmclock.R;
import com.nfcalarmclock.alarm.NacAlarm;
import com.nfcalarmclock.nfc.NacNfc;
import com.nfcalarmclock.nfc.NacNfcTag;
import com.nfcalarmclock.shared.NacSharedConstants;
import com.nfcalarmclock.shared.NacSharedPreferences;
import com.nfcalarmclock.system.NacBundle;
import com.nfcalarmclock.system.NacIntent;
import com.nfcalarmclock.util.NacUtility;

/**
 * Activity to dismiss/snooze the alarm.
 */
@SuppressWarnings("RedundantSuppression")
public class NacActiveAlarmActivity
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
			if (action.equals(NacActiveAlarmActivity.ACTION_STOP_ACTIVITY))
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
	 * Dismiss the activity action.
	 */
	public static final String ACTION_DISMISS_ACTIVITY =
		"com.nfcalarmclock.ACTION_DISMISS_ALARM_ACTIVITY";

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
		NacActiveAlarmService.dismissService(this, alarm);
	}

	/**
	 * Dismiss due to an NFC tag being scanned, and only if the NFC tag ID
	 * matches the saved alarm NFC tag ID. The exception to this is if the
	 * saved alarm NFC tag ID is empty.
	 * <p>
	 * The finish() method is not called because if the ACTION_DISMISS_ALARM
	 * intent is sent to the foreground service, then the foreground service will
	 * finish this activity.
	 */
	private void dismissFromNfcScan()
	{
		Intent intent = getIntent();
		NacAlarm alarm = this.getAlarm();
		NacNfcTag tag = new NacNfcTag(alarm, intent);

		if (tag.check(this))
		{
			NacActiveAlarmService.dismissServiceWithNfc(this, alarm);
		}
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

		// Snooze
		if ((id == R.id.snooze) || ((id == R.id.act_alarm)
			&& shared.getEasySnooze()))
		{
			this.snooze();
		}
		// Dismiss
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

		this.mSharedPreferences = new NacSharedPreferences(this);
		this.mStopReceiver = new StopActivityReceiver();

		// NFC tag was scanned
		if (this.wasNfcScanned())
		{
			this.dismissFromNfcScan();
		}

		// Check if alarm should be dismissed
		if (this.shouldDismissAlarm())
		{
			this.dismiss();
		}

		// Setup
		this.setupScreen();
		this.setupAlarmButtons();
		this.setupAlarmInfo();
	}

	/**
	 * NFC tag discovered so dismiss the dialog.
	 * <p>
	 * Note: Parent method must be called last. Causes issues with
	 * setSnoozeCount.
	 */
	@Override
	protected void onNewIntent(Intent intent)
	{
		setIntent(intent);
		this.dismissFromNfcScan();
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
	public void onSaveInstanceState(@NonNull Bundle outState)
	{
		super.onSaveInstanceState(outState);

		NacAlarm alarm = this.getAlarm();

		// Save the alarm to the save instance state
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

		// Attempt to get the alarm from the saved instance state
		if (alarm == null)
		{
			alarm = NacBundle.getAlarm(savedInstanceState);
		}

		// Alarm is still null, finish the activity
		if (alarm == null)
		{
			finish();
		}

		// Set the alarm
		this.mAlarm = alarm;
	}

	/**
	 * Setup the snooze and dismiss buttons.
	 */
	public void setupAlarmButtons()
	{
		NacSharedPreferences shared = this.getSharedPreferences();
		NacAlarm alarm = this.getAlarm();
		RelativeLayout layout = findViewById(R.id.act_alarm);
		Button snoozeButton = findViewById(R.id.snooze);
		Button dismissButton = findViewById(R.id.dismiss);

		// NFC should be used so remove the dismiss button
		if (NacNfc.shouldUseNfc(this, alarm))
		{
			dismissButton.setVisibility(View.GONE);
		}
		// Show the dismiss button
		else
		{
			dismissButton.setVisibility(View.VISIBLE);
		}

		// Setup the buttons
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

		// Alarm is present and the user wants to see alarm info
		if ((alarm != null) && shared.getShowAlarmInfo())
		{
			TextView name = findViewById(R.id.name);
			String alarmName = alarm.getNameNormalized();

			// Show alarm info
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
		// Show NFC instructions
		if (!NacNfc.isEnabled(this))
		{
			TextView instructions = findViewById(R.id.instructions);
			instructions.setVisibility(View.GONE);
		}
	}

	/**
	 * Setup NFC.
	 */
	private void setupNfc()
	{
		NacAlarm alarm = this.getAlarm();

		// NFC is not enabled
		if (!NacNfc.isEnabled(this))
		{
			// NFC should be used, so prompt the user
			if (NacNfc.shouldUseNfc(this, alarm))
			{
				NacNfc.prompt(this);
			}
			// NFC does not need to be used. Return out of the method
			else
			{
				return;
			}
		}

		// NFC exists on the device. The device is NFC capable
		if (NacNfc.exists(this))
		{
			Intent intent = new Intent(this, NacActiveAlarmActivity.class)
				.addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);

			// Start NFC
			NacNfc.start(this, intent);
		}
		// Unable to use NFC on the device
		else
		{
			NacSharedConstants cons = new NacSharedConstants(this);

			NacUtility.quickToast(this,
				cons.getErrorMessageNfcUnsupported());
		}
	}

	/**
	 * Setup the screen and handle the case when the device is locked.
	 */
	@SuppressWarnings("deprecation")
	public void setupScreen()
	{
		NacAlarm alarm = this.getAlarm();
		Window window = getWindow();
		boolean showWhenLocked = (alarm != null) && !alarm.shouldUseNfc();

		// Use updated method calls to control screen for APK >= 27
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1)
		{
			setTurnScreenOn(true);
			setShowWhenLocked(showWhenLocked);
			//setShowWhenLocked(false);
		}
		else
		{
			window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

			// Add flag to show when locked
			if (showWhenLocked)
			{
				window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
			}
			// Clear flag so app is not shown when locked
			else
			{
				window.clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
			}
		}

		// Keep screen on
		window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		// Allow lock screen when screen is turned on
		//window.addFlags(WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
	}

	/**
	 * Setup the receiver for the Stop signal.
	 */
	@SuppressLint("UnspecifiedRegisterReceiverFlag")
	private void setupStopReceiver()
	{
		StopActivityReceiver receiver = this.getStopReceiver();
		IntentFilter filter = new IntentFilter(
			NacActiveAlarmActivity.ACTION_STOP_ACTIVITY);

		// Check if app needs to set the exported flag in order to indicate
		// that this app does not expect broadcasts from other apps on the
		// device
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
		{
			int flags = Context.RECEIVER_NOT_EXPORTED;

			// Register to listen for the STOP broadcast for the activity
			registerReceiver(receiver, filter, flags);
		}
		else
		{
			// Register to listen for the STOP broadcast for the activity
			registerReceiver(receiver, filter);
		}
	}

	/**
	 * @return True if the alarm should be dismissed, and False otherwise.
	 */
	public boolean shouldDismissAlarm()
	{
		NacAlarm alarm = this.getAlarm();
		Intent intent = getIntent();
		String action = NacIntent.getAction(intent);

		// Check to see if the alarm should be dismissed and NFC does not need to be used
		return action.equals(NacActiveAlarmActivity.ACTION_DISMISS_ACTIVITY)
			&& !NacNfc.shouldUseNfc(this, alarm);
	}

	/**
	 * Snooze the alarm.
	 */
	public void snooze()
	{
		NacSharedPreferences shared = this.getSharedPreferences();
		NacAlarm alarm = this.getAlarm();

		// Unable to snooze. Show a toast indicating this to the user
		if (!alarm.canSnooze(shared))
		{
			NacSharedConstants cons = new NacSharedConstants(this);
			NacUtility.quickToast(this, cons.getErrorMessageSnooze());
			return;
		}

		// Snooze the alarm service
		NacActiveAlarmService.snoozeService(this, alarm);
	}

	/**
	 * @return True if an NFC tag was scanned, and False otherwise.
	 */
	public boolean wasNfcScanned()
	{
		Intent intent = getIntent();
		return NacNfc.wasScanned(intent);
	}

}

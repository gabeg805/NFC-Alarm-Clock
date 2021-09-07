package com.nfcalarmclock.activealarm;

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

import com.nfcalarmclock.alarm.NacAlarm;
import com.nfcalarmclock.nfc.NacNfcTag;
import com.nfcalarmclock.system.NacBundle;
import com.nfcalarmclock.system.NacContext;
import com.nfcalarmclock.system.NacIntent;
import com.nfcalarmclock.shared.NacSharedConstants;
import com.nfcalarmclock.shared.NacSharedPreferences;
import com.nfcalarmclock.util.NacUtility;
import com.nfcalarmclock.R;
import com.nfcalarmclock.nfc.NacNfc;

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
		NacContext.dismissForegroundService(this, alarm);
		//finish();
	}

	/**
	 * Dismiss due to an NFC tag being scanned, and only if the NFC tag ID
	 * matches the saved alarm NFC tag ID. The exception to this is if the
	 * saved alarm NFC tag ID is empty.
	 * 
	 * The finish() method is not called because if the ACTION_DISMISS_ALARM
	 * intent is sent to the foreground service, then the foreground service will
	 * finish this activity.
	 */
	private void dismissFromNfcScan()
	{
		Intent intent = getIntent();
		NacAlarm alarm = this.getAlarm();
		NacNfcTag tag = new NacNfcTag(alarm, intent);

		//if (NacContext.checkNfcScan(this, intent, alarm))
		if (tag.check(this))
		{
			NacUtility.quickToast(this, "Dismiss service with NFC");
			NacContext.dismissForegroundServiceWithNfc(this, alarm);
			//NacContext.dismissForegroundServiceFromNfcScan(this, intent, alarm);
		}
		else
		{
			NacUtility.quickToast(this, "NOT dismiss service with NFC");
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

		this.mSharedPreferences = new NacSharedPreferences(this);
		this.mStopReceiver = new StopActivityReceiver();

		if (this.wasNfcScanned())
		{
			this.dismissFromNfcScan();
		}

		if (this.shouldDismissAlarm())
		{
			this.dismiss();
		}

		this.setupScreen();
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

		//if (alarm == null)
		//{
		//	alarm = NacDatabase.findAlarm(this, Calendar.getInstance());
		//}

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
		RelativeLayout layout = findViewById(R.id.act_alarm);
		Button snoozeButton = findViewById(R.id.snooze);
		Button dismissButton = findViewById(R.id.dismiss);

		if (NacNfc.shouldUseNfc(this, alarm))
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
			TextView name = findViewById(R.id.name);
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

		if (!NacNfc.isEnabled(this))
		{
			if (NacNfc.shouldUseNfc(this, alarm))
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
			Intent intent = new Intent(this, NacActiveAlarmActivity.class)
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
	 * Setup the screen and handle the case when the device is locked.
	 */
	@SuppressWarnings("deprecation")
	public void setupScreen()
	{
		NacAlarm alarm = this.getAlarm();
		Window window = getWindow();
		boolean showWhenLocked = (alarm != null) && !alarm.shouldUseNfc();

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1)
		{
			setTurnScreenOn(true);
			setShowWhenLocked(false);
			//setShowWhenLocked(showWhenLocked);

			//if ((alarm != null) && !alarm.shouldUseNfc())
			//{
			//	setShowWhenLocked(true);
			//}
		}
		else
		{
			window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

			//if ((alarm != null) && !alarm.shouldUseNfc())
			//if (showWhenLocked)
			//{
			//	window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
			//}
			//else
			//{
				window.clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
			//}
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
			NacActiveAlarmActivity.ACTION_STOP_ACTIVITY);
		registerReceiver(receiver, filter);
	}

	/**
	 * @return True if the alarm should be dismissed, and False otherwise.
	 */
	public boolean shouldDismissAlarm()
	{
		NacAlarm alarm = this.getAlarm();
		Intent intent = getIntent();
		String action = NacIntent.getAction(intent);

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

		if (!alarm.canSnooze(shared))
		{
			NacSharedConstants cons = new NacSharedConstants(this);
			NacUtility.quickToast(this, cons.getErrorMessageSnooze());
			return;
		}

		NacContext.snoozeForegroundService(this, alarm);
		//finish();
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

package com.nfcalarmclock;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Activity to dismiss/snooze the alarm.
 */
public class NacAlarmActivity
	extends Activity
	implements View.OnClickListener,
		NacWakeUpAction.OnAutoDismissListener
{

	/**
	 * Actions to take upon waking up, such as enabling NFC, playing music, etc.
	 */
	private NacWakeUpAction mWakeUp;

	/**
	 * Alarm.
	 */
	private NacAlarm mAlarm;

	/**
	 * Dismiss the alarm.
	 */
	private void dismiss()
	{
		NacSharedPreferences shared = new NacSharedPreferences(this);
		NacAlarm alarm = this.getAlarm();
		int id = alarm.getId();

		if (alarm.isOneTimeAlarm())
		{
			NacDatabase db = new NacDatabase(this);

			alarm.setEnabled(false);
			db.update(alarm);
			db.close();
		}

		shared.editSnoozeCount(id, 0);
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
	 * Automatically dismiss the alarm.
	 */
	@Override
	public void onAutoDismiss()
	{
		NacUtility.toast(this, "Auto-dismissed the alarm");
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
		NacSharedPreferences shared = new NacSharedPreferences(this);
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

		NacAlarm alarm  = NacIntent.getAlarm(getIntent());
		this.mAlarm = alarm;
		this.mWakeUp = new NacWakeUpAction(this, alarm);

		this.scheduleNextAlarm();
		this.setupAlarmButtons();

		this.getWakeUp().setOnAutoDismissListener(this);
		this.getWakeUp().start();
	}

	/**
	 */
	@Override
	public void onDestroy()
	{
		super.onDestroy();
		NacUtility.printf("onDestroy!");
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
		// Add toast here? Or i think there already is one in dismiss()
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
		NacUtility.printf("onPause!");
		this.getWakeUp().pause();
	}

	/**
	 * Enable tag discovery.
	 */
	@Override
	public void onResume()
	{
		super.onResume();
		NacUtility.printf("onResume!");
		this.getWakeUp().resume();
	}

	/**
	 * Schedule the next alarm.
	 */
	public void scheduleNextAlarm()
	{
		NacAlarm alarm = this.getAlarm();

		if (alarm.isOneTimeAlarm())
		{
			return;
		}

		NacScheduler scheduler = new NacScheduler(this);
		Calendar next = Calendar.getInstance();

		next.set(Calendar.HOUR_OF_DAY, alarm.getHour());
		next.set(Calendar.MINUTE, alarm.getMinute());
		next.set(Calendar.SECOND, 0);
		next.set(Calendar.MILLISECOND, 0);
		next.add(Calendar.DAY_OF_MONTH, 7);
		scheduler.update(alarm, next);
	}

	/**
	 * Setup the snooze and dismiss buttons.
	 */
	public void setupAlarmButtons()
	{
		NacSharedPreferences shared = new NacSharedPreferences(this);
		LinearLayout layout = (LinearLayout) findViewById(R.id.act_alarm);
		Button snoozeButton = (Button) findViewById(R.id.snooze);
		Button dismissButton = (Button) findViewById(R.id.dismiss);

		if (NacNfc.exists(this) || !shared.getRequireNfc())
		{
			dismissButton.setVisibility(View.VISIBLE);
		}
		else
		{
			dismissButton.setVisibility(View.GONE);
		}

		snoozeButton.setTextColor(shared.getThemeColor());
		dismissButton.setTextColor(shared.getThemeColor());
		layout.setOnClickListener(this);
		snoozeButton.setOnClickListener(this);
		dismissButton.setOnClickListener(this);
	}

	/**
	 * Snooze the alarm.
	 */
	public boolean snooze()
	{
		NacSharedPreferences shared = new NacSharedPreferences(this);
		NacAlarm alarm = this.getAlarm();
		int id = alarm.getId();
		int snoozeCount = shared.getSnoozeCount(id) + 1;
		int maxSnoozeCount = shared.getMaxSnooze();

		if ((snoozeCount > maxSnoozeCount) && (maxSnoozeCount >= 0))
		{
			return false;
		}

		NacScheduler scheduler = new NacScheduler(this);
		Calendar snooze = Calendar.getInstance();

		snooze.add(Calendar.MINUTE, shared.getSnoozeDuration());
		alarm.setHour(snooze.get(Calendar.HOUR_OF_DAY));
		alarm.setMinute(snooze.get(Calendar.MINUTE));
		scheduler.update(alarm, snooze);
		shared.editSnoozeCount(id, snoozeCount);

		return true;
	}

}

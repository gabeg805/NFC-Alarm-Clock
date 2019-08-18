package com.nfcalarmclock;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.PowerManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.Window;
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
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		NacSharedPreferences shared = this.getSharedPreferences();
		Calendar calendar = Calendar.getInstance();
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int minute = calendar.get(Calendar.MINUTE) + 1;
		String name = alarm.getName();
		String time = NacCalendar.Time.getFullTime(this, hour, minute);
		String message = "Auto-dismissed \""+name+"\" at "+time;

		if (pm.isInteractive())
		{
			NacUtility.toast(this, message);
			shared.editAutoDismissMessage("");
		}
		else
		{
			shared.editAutoDismissMessage(message);
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
		NacWakeUpAction wakeUp = new NacWakeUpAction(this, alarm);
		NacScheduler scheduler = new NacScheduler(this);
		this.mSharedPreferences = new NacSharedPreferences(this);
		this.mAlarm = alarm;
		this.mWakeUp = wakeUp;

		scheduler.scheduleNext(alarm);
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
	 * Setup the snooze and dismiss buttons.
	 */
	public void setupAlarmButtons()
	{
		NacSharedPreferences shared = new NacSharedPreferences(this);
		//LinearLayout layout = (LinearLayout) findViewById(R.id.act_alarm);
		RelativeLayout layout = (RelativeLayout) findViewById(R.id.act_alarm);
		Button snoozeButton = (Button) findViewById(R.id.snooze);
		Button dismissButton = (Button) findViewById(R.id.dismiss);

		if (NacNfc.exists(this) && shared.getRequireNfc())
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
		NacSharedPreferences shared = new NacSharedPreferences(this);
		NacAlarm alarm = this.getAlarm();
		TextView nametime = (TextView) findViewById(R.id.nametime);
		String alarmName = alarm.getName();
		String alarmTime = alarm.getTime(this);
		String alarmMeridian = alarm.getMeridian(this);

		if (alarmName.isEmpty())
		{
			return;
		}

		int nameColor = shared.getNameColor();
		int timeColor = shared.getTimeColor();
		int meridianColor = shared.getMeridianColor(alarmMeridian);
		Spannable nameColorized = new SpannableString(alarmName);
		Spannable timeColorized = new SpannableString(alarmTime);
		Spannable meridianColorized = new SpannableString(alarmMeridian);

		nameColorized.setSpan(new ForegroundColorSpan(nameColor), 0,
			alarmName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		timeColorized.setSpan(new ForegroundColorSpan(timeColor), 0,
			alarmTime.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		meridianColorized.setSpan(new ForegroundColorSpan(meridianColor), 0,
			alarmMeridian.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

		nametime.setText(nameColorized);
		nametime.append("  @  ");
		nametime.append(timeColorized);
		nametime.append(" ");
		nametime.append(meridianColorized);

		// Ellipsize name if it's too long
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

package com.nfcalarmclock;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.View;
import android.view.Window;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

/**
 * Activity to dismiss/snooze the alarm.
 */
public class NacAlarmActivity
	extends Activity
	implements NacDialog.OnDismissListener,NacDialog.OnShowListener,NacDialog.OnCancelListener
{

	/**
	 * Alarm.
	 */
	private NacAlarm mAlarm;

	/**
	 * Shared preference information.
	 *
	 * Contains information such as: snooze duration, max snoozes, and auto
	 * dismiss time.
	 */
	private NacSharedPreferences mShared;

	/**
	 * Dialog to display activity in.
	 */
	private NacAlarmDialog mDialog;

	/**
	 * Media player.
	 */
	private NacMediaPlayer mPlayer;

	/**
	 * Vibrate the phone.
	 *
	 * Will be canceled once the activity is done.
	 */
	private NacVibrator mVibrator;

	/**
	 * NFC adapter.
	 */
	private NfcAdapter mNfcAdapter;

	/**
	 * Disable NFC dispatch, so the app does not waste battery when it does not
	 * need to discover NFC tags.
	 */
	private void disableNfc()
	{
		if (this.mNfcAdapter == null)
		{
			return;
		}

		this.mNfcAdapter.disableForegroundDispatch(this);
	}

	/**
	 * Enable NFC dispatch, so that the app can discover NFC tags.
	 */
	private void enableNfc()
	{
		if (this.mNfcAdapter == null)
		{
			return;
		}

		if (!this.mNfcAdapter.isEnabled())
		{
			NacUtility.toast(this, "Please enable NFC to dismiss the alarm");
			startActivity(new Intent(Settings.ACTION_NFC_SETTINGS));
		}

		Intent intent = new Intent(this, NacAlarmActivity.class)
			.addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);
		PendingIntent pending = PendingIntent.getActivity(this, 0, intent, 0);
		IntentFilter[] filter = new IntentFilter[]{};

		this.mNfcAdapter.enableForegroundDispatch(this, pending, filter, null);
	}

	/**
	 * @return The auto dismiss time (in minutes).
	 */
	private int getAutoDismissTime()
	{
		return this.mShared.autoDismiss;
	}

	/**
	 * @return The max snooze count.
	 */
	private int getMaxSnoozeCount()
	{
		return this.mShared.maxSnoozes;
	}

	/**
	 * @return The snooze count.
	 */
	private int getSnoozeCount()
	{
		String key = this.getSnoozeCountKey();

		return this.mShared.instance.getInt(key, 0);
	}

	/**
	 * @return The snooze count key.
	 */
	private String getSnoozeCountKey()
	{
		return "snoozeCount" + String.valueOf(this.mAlarm.getId());
	}

	/**
	 * Called when the dialog is canceled.
	 */
	@Override
	public boolean onCancelDialog(NacDialog dialog)
	{
		if (this.snooze())
		{
			finish();
			return true;
		}

		//Intent intent = getIntent();
		//finish();
		//startActivity(intent);
		recreate();

		return false;
	}

	/**
	 * Create the activity.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		Intent intent = getIntent();
		Bundle bundle = (Bundle) intent.getBundleExtra("bundle");
		NacAlarmParcel parcel = (NacAlarmParcel)
			bundle.getParcelable("parcel");

		this.mAlarm = parcel.toAlarm();
		this.mShared = new NacSharedPreferences(this);
		this.mDialog = new NacAlarmDialog();
		this.mPlayer = new NacMediaPlayer(this);
		this.mVibrator = new NacVibrator(this);
		this.mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

		scheduleNextAlarm();
	}

	/**
	 * Called when the dialog is dismissed.
	 */
	@Override
	public boolean onDismissDialog(NacDialog dialog)
	{
		NacUtility.toast(this, "Alarm dismissed");
		this.setSnoozeCount(0);
		finish();

		return true;
	}

	/**
	 * NFC tag discovered so dismiss the dialog.
	 */
	@Override
	protected void onNewIntent(Intent intent)
	{
		// What happens if I reverse these two lines?
		this.mDialog.dismiss();
		super.onNewIntent(intent);
	}

	/**
	 * Disable tag discovery.
	 */
	@Override
	public void onPause()
	{
		super.onPause();

		this.disableNfc();
	}

	/**
	 * Enable tag discovery.
	 */
	@Override
	public void onResume()
	{
		super.onResume();

		if (this.mNfcAdapter == null)
		{
			//NacUtility.toast(this, "This device doesn't support NFC");
			//finish();
			return;
		}

		this.enableNfc();
	}

	/**
	 * Called when the dialog is shown.
	 */
	@Override
	public void onShowDialog(NacDialog dialog, View root)
	{
		int autoDismiss = this.getAutoDismissTime();
		long delay = TimeUnit.MINUTES.toMillis(autoDismiss);

		if (autoDismiss == 0)
		{
			return;
		}

		new Handler().postDelayed(new Runnable()
		{
			@Override
			public void run()
			{
				Context context = getApplicationContext();

				NacUtility.toast(context, "Automatically dismissed alarm");
				finish();
			}
		}, delay);
	}

	/**
	 */
	@Override
	public void onStart()
	{
		super.onStart();

		this.mDialog.build(this, R.layout.act_alarm);
		this.mDialog.addOnCancelListener(this);
		this.mDialog.addOnDismissListener(this);
		this.mDialog.addOnShowListener(this);
		this.mDialog.show();
		this.playMusic();
		this.vibrate();
	}

	/**
	 */
	@Override
	public void onStop()
	{
		super.onStop();

		if (this.mVibrator != null)
		{
			this.mVibrator.cancel(true);
		}

		if (this.mPlayer != null)
		{
			this.mPlayer.stop();
		}

		// This is done in onPause
		//this.disableNfc();
	}

	/**
	 * Play music.
	 */
	private void playMusic()
	{
		this.mPlayer.play(this.mAlarm.getSound(), true);
	}

	/**
	 * Schedule the next alarm.
	 */
	public void scheduleNextAlarm()
	{
		if (!this.mAlarm.getRepeat())
		{
			NacDatabase db = new NacDatabase(this);

			this.mAlarm.toggleToday();
			db.update(this.mAlarm);
			return;
		}

		NacAlarmScheduler scheduler = new NacAlarmScheduler(this);
		Calendar next = Calendar.getInstance();

		next.set(Calendar.HOUR_OF_DAY, this.mAlarm.getHour());
		next.set(Calendar.MINUTE, this.mAlarm.getMinute());
		next.set(Calendar.SECOND, 0);
		next.set(Calendar.MILLISECOND, 0);
		next.add(Calendar.DAY_OF_MONTH, 7);
		scheduler.update(this.mAlarm, next);
	}

	/**
	 * Set the snooze count.
	 *
	 * @param  count  The snooze count.
	 */
	private void setSnoozeCount(int count)
	{
		// Apply vs commit
		String key = this.getSnoozeCountKey();

		this.mShared.instance.edit().putInt(key, count).apply();
	}

	/**
	 * Snooze the alarm.
	 */
	public boolean snooze()
	{
		int snoozeCount = this.getSnoozeCount() + 1;
		int maxSnoozeCount = this.getMaxSnoozeCount();

		if ((snoozeCount > maxSnoozeCount) && (maxSnoozeCount >= 0))
		{
			NacUtility.quickToast(this, "Unable to snooze the alarm");
			return false;
		}

		NacAlarmScheduler scheduler = new NacAlarmScheduler(this);
		Calendar snooze = Calendar.getInstance();

		NacUtility.toast(this, "Alarm snoozed");
		snooze.add(Calendar.MINUTE, this.mShared.snoozeDuration);
		this.mAlarm.setHour(snooze.get(Calendar.HOUR));
		this.mAlarm.setMinute(snooze.get(Calendar.MINUTE));
		scheduler.update(this.mAlarm, snooze);
		this.setSnoozeCount(snoozeCount);

		return true;
	}

	/**
	 * Vibrate the phone repeatedly until the alarm is dismissed.
	 */
	public void vibrate()
	{
		if (this.mAlarm.getVibrate())
		{
			long duration = 500;

			this.mVibrator.execute(duration);
		}
	}

}

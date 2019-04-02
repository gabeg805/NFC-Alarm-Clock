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
	implements Runnable,
		NacDialog.OnShowListener,
		NacDialog.OnCancelListener,
		NacDialog.OnDismissListener
{

	/**
	 * Alarm.
	 */
	private NacAlarm mAlarm;

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
	 * Automatically dismiss the alarm in case it does not get dismissed.
	 */
	private Handler mHandler;

	/**
	 * Cleanup various alarm objects.
	 */
	public void cleanup()
	{
		if (this.mVibrator != null)
		{
			this.mVibrator.cancel(true);
		}

		if (this.mPlayer != null)
		{
			this.mPlayer.release();
		}

		if (this.mHandler != null)
		{
			this.mHandler.removeCallbacksAndMessages(null);
		}
	}

	/**
	 * Disable NFC dispatch, so the app does not waste battery when it does not
	 * need to discover NFC tags.
	 */
	private void disableNfc()
	{
		NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);

		if (nfcAdapter == null)
		{
			return;
		}

		nfcAdapter.disableForegroundDispatch(this);
	}

	/**
	 * Dismiss the alarm.
	 */
	private void dismiss()
	{
		NacSharedPreferences shared = new NacSharedPreferences(this);

		NacUtility.quickToast(this, "Alarm dismissed");
		this.setSnoozeCount(shared, 0);
		this.cleanup();
		finish();
	}

	/**
	 * Enable NFC dispatch, so that the app can discover NFC tags.
	 */
	private void enableNfc()
	{
		NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);

		if (nfcAdapter == null)
		{
			NacUtility.toast(this, "Your device doesn't support NFC");
			return;
		}

		if (!nfcAdapter.isEnabled())
		{
			NacUtility.toast(this, "Please enable NFC to dismiss the alarm");
			startActivity(new Intent(Settings.ACTION_NFC_SETTINGS));
		}

		Intent intent = new Intent(this, NacAlarmActivity.class)
			.addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);
		PendingIntent pending = PendingIntent.getActivity(this, 0, intent, 0);
		IntentFilter[] filter = new IntentFilter[]{};

		nfcAdapter.enableForegroundDispatch(this, pending, filter, null);
	}

	/**
	 * @return The alarm.
	 */
	private NacAlarm getAlarm()
	{
		return this.mAlarm;
	}

	/**
	 * @return The snooze count.
	 */
	private int getSnoozeCount(NacSharedPreferences shared)
	{
		String key = this.getSnoozeCountKey();

		return shared.getInstance().getInt(key, 0);
	}

	/**
	 * @return The snooze count key.
	 */
	private String getSnoozeCountKey()
	{
		return "snoozeCount" + String.valueOf(this.getAlarm().getId());
	}

	/**
	 * Called when the dialog is canceled.
	 */
	@Override
	public boolean onCancelDialog(NacDialog dialog)
	{
		this.snooze("Alarm snoozed");
		return true;
	}

	/**
	 * Create the activity.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		this.mAlarm = NacAlarmParcel.getAlarm(getIntent());
		this.mPlayer = new NacMediaPlayer(this);
		this.mVibrator = new NacVibrator(this);
		this.mHandler = new Handler();

		this.scheduleNextAlarm();
		this.playMusic();
		this.vibrate();
	}

	/**
	 * Called when the dialog is dismissed.
	 */
	@Override
	public boolean onDismissDialog(NacDialog dialog)
	{
		this.dismiss();
		return true;
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
		this.disableNfc();
	}

	/**
	 * Enable tag discovery.
	 */
	@Override
	public void onResume()
	{
		super.onResume();
		this.enableNfc();
	}

	/**
	 * Called when the dialog is shown.
	 */
	@Override
	public void onShowDialog(NacDialog dialog, View root)
	{
		NacSharedPreferences shared = new NacSharedPreferences(this);
		int autoDismiss = shared.getAutoDismiss();
		long delay = TimeUnit.MINUTES.toMillis(autoDismiss);

		if (autoDismiss == 0)
		{
			return;
		}

		this.mHandler.postDelayed(this, delay);
	}

	/**
	 */
	@Override
	public void onStart()
	{
		super.onStart();

		NacAlarmDialog dialog = new NacAlarmDialog();
		NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);

		dialog.build(this, R.layout.act_alarm);

		if (nfcAdapter == null)
		{
			dialog.setPositiveButton("Dismiss");
		}

		dialog.addOnCancelListener(this);
		dialog.addOnDismissListener(this);
		dialog.addOnShowListener(this);
		dialog.show();
	}

	/**
	 * Play music.
	 */
	private void playMusic()
	{
		if (this.mPlayer != null)
		{
			this.mPlayer.stop();
			this.mPlayer.play(this.getAlarm().getSound(), true);
		}
	}

	/**
	 * Automatically snooze the alarm.
	 */
	@Override
	public void run()
	{
		this.snooze("Auto-dismissed the alarm");
	}

	/**
	 * Schedule the next alarm.
	 */
	public void scheduleNextAlarm()
	{
		NacAlarm alarm = this.getAlarm();

		if (!alarm.getRepeat() || alarm.getDays().isEmpty())
		{
			NacDatabase db = new NacDatabase(this);

			alarm.setEnabled(false);
			db.update(alarm);

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
	 * Set the snooze count.
	 *
	 * @param  count  The snooze count.
	 */
	private void setSnoozeCount(NacSharedPreferences shared, int count)
	{
		String key = this.getSnoozeCountKey();

		shared.getInstance().edit().putInt(key, count).apply();
	}

	/**
	 * Snooze the alarm.
	 */
	public boolean snooze()
	{
		NacSharedPreferences shared = new NacSharedPreferences(this);
		int snoozeCount = this.getSnoozeCount(shared) + 1;
		int maxSnoozeCount = shared.getMaxSnooze();

		if ((snoozeCount > maxSnoozeCount) && (maxSnoozeCount >= 0))
		{
			return false;
		}

		NacAlarm alarm = this.getAlarm();
		NacScheduler scheduler = new NacScheduler(this);
		Calendar snooze = Calendar.getInstance();

		snooze.add(Calendar.MINUTE, shared.getSnoozeDuration());
		alarm.setHour(snooze.get(Calendar.HOUR));
		alarm.setMinute(snooze.get(Calendar.MINUTE));
		scheduler.update(alarm, snooze);
		this.setSnoozeCount(shared, snoozeCount);

		return true;
	}

	/**
	 * Snooze the alarm.
	 */
	public boolean snooze(String message)
	{
		this.cleanup();

		if (this.snooze())
		{
			NacUtility.quickToast(this, message);
			this.cleanup();
			finish();
			return true;
		}
		else
		{
			NacUtility.quickToast(this, "Unable to snooze the alarm");
			recreate();
			return false;
		}
	}

	/**
	 * Vibrate the phone repeatedly until the alarm is dismissed.
	 */
	public void vibrate()
	{
		if (this.getAlarm().getVibrate() && (this.mVibrator != null))
		{
			long duration = 500;

			this.mVibrator.execute(duration);
		}
	}

}

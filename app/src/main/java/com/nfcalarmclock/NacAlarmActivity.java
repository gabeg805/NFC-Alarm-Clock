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
import android.widget.Toast;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

/**
 * Activity to dismiss/snooze the alarm.
 */
public class NacAlarmActivity
	extends Activity
	implements NacDialog.OnDismissListener,NacDialog.OnShowListener,NacDialog.OnCancelListener
	//implements NacDialog.OnDismissListener,NacDialog.OnShowListener,NacDialog.OnNeutralActionListener
{

	/**
	 * NFC adapter.
	 */
	private NfcAdapter mNfcAdapter;

	/**
	 * Dialog to display activity in.
	 */
	private NacAlarmDialog mDialog;

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
	 * Shared preference information.
	 *
	 * Contains information such as: snooze duration, max snoozes, and auto
	 * dismiss time.
	 */
	private NacSharedPreferences mShared;

	/**
	 * Count the number of snoozes that have occurred for a given alarm.
	 */
	private int mSnoozeCount;

	/**
	 * Create the activity.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		NacUtility.printf("onCreate!");

		setupAlarm();
		setupNfc();
		setupDialog();
	}

	/**
	 * Called when the dialog is canceled.
	 */
	@Override
	public boolean onCancelDialog(NacDialog dialog)
	//public boolean onNeutralActionDialog(NacDialog dialog)
	{
		NacUtility.printf("onCancelDialog!");
		if (this.snooze())
		{
			finish();
			return true;
		}

		Intent intent = getIntent();
		finish();
		startActivity(intent);

		return false;
	}

	/**
	 * Called when the dialog is dismissed.
	 */
	@Override
	public boolean onDismissDialog(NacDialog dialog)
	{
		NacUtility.printf("onDismissDialog!");
		this.mPlayer.reset();
		this.mShared.instance.edit().putInt("snoozeCount"+String.valueOf(this.mAlarm.getId()), 0).commit();
		finish();

		return true;
	}

	/**
	 * NFC tag discovered so dismiss the dialog.
	 */
	@Override
	protected void onNewIntent(Intent intent)
	{
		NacUtility.printf("onNewIntent!");
		Toast.makeText(this, "Alarm dismissed.", Toast.LENGTH_LONG)
			.show();
		this.mDialog.dismiss();
		super.onNewIntent(intent);
	}

	/**
	 * Disable tag discovery for the foreground app (this app).
	 *
	 * @see onResume for more information on reader mode.
	 */
	@Override
	public void onPause()
	{
		super.onPause();
		NacUtility.printf("onPause!");

		if (this.mVibrator != null)
		{
			this.mVibrator.cancel(true);
		}

		if (this.mPlayer != null)
		{
			this.mPlayer.stop();
		}

		if (this.mNfcAdapter != null)
		{
			this.mNfcAdapter.disableForegroundDispatch(this);
		}
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		NacUtility.printf("onDestroy!");
	}

	@Override
	public void onRestart()
	{
		super.onRestart();
		NacUtility.printf("onRestart!");
	}

	/**
	 * Enable the foreground app (this app) to discover NFC tags.
	 */
	@Override
	public void onResume()
	{
		super.onResume();
		NacUtility.printf("onReusme!");

		if (this.mNfcAdapter == null)
		{
			//finish();
			return;
		}

		if (!this.mNfcAdapter.isEnabled())
		{
			Toast.makeText(this, "Please enable NFC", Toast.LENGTH_LONG)
				.show();
			startActivity(new Intent(Settings.ACTION_NFC_SETTINGS));
		}

		Intent intent = new Intent(this, NacAlarmActivity.class)
			.addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);
		PendingIntent pending = PendingIntent.getActivity(this, 0, intent, 0);
		IntentFilter[] filter = new IntentFilter[]{};

		this.mNfcAdapter.enableForegroundDispatch(this, pending, filter, null);
	}

	/**
	 * Called when the dialog is shown.
	 */
	@Override
	public void onShowDialog(NacDialog dialog, View root)
	{
		NacUtility.printf("onShowDialog!");
		if (this.mShared.autoDismiss == 0)
		{
			return;
		}

		long delay = TimeUnit.MINUTES.toMillis(this.mShared.autoDismiss);

		new Handler().postDelayed(new Runnable()
		{
			@Override
			public void run()
			{
				Toast.makeText(getApplicationContext(),
					"Auto-dismissed alarm.", Toast.LENGTH_LONG);
				finish();
			}
		}, delay);
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

		NacUtility.printf("Next alarm : %s", next.getTime().toString());	
		// Maybe do a snackbar message here?
		scheduler.update(this.mAlarm, next);
	}

	/**
	 * Setup the alarm.
	 */
	private void setupAlarm()
	{
		Intent intent = getIntent();
		Bundle bundle = (Bundle) intent.getBundleExtra("bundle");
		NacAlarmParcel parcel = (NacAlarmParcel)
			bundle.getParcelable("parcel");

		this.mAlarm = parcel.toAlarm();
		this.mShared = new NacSharedPreferences(this);
		//this.mShared.instance.edit().putInt("snoozeCount"+String.valueOf(this.mAlarm.getId()), 0).commit();
		this.mPlayer = new NacMediaPlayer(this);
		this.mVibrator = null;
		this.mSnoozeCount = this.mShared.instance.getInt("snoozeCount"+String.valueOf(this.mAlarm.getId()), 0);

		NacUtility.printf("Setting up alarm! Snooze count : %d / %d", this.mSnoozeCount, this.mShared.maxSnoozes);
		NacUtility.printf("Alarm Id : %d", this.mAlarm.getId());

		this.scheduleNextAlarm();
		this.playMusic();
		this.vibrate();
	}

	/**
	 * Setup the dialog that will be displayed.
	 */
	public void setupDialog()
	{
		NacUtility.printf("setupDialog! %b", (mDialog == null));
		this.mDialog = new NacAlarmDialog();

		this.mDialog.build(this, R.layout.act_alarm);
		this.mDialog.addOnCancelListener(this);
		this.mDialog.addOnDismissListener(this);
		this.mDialog.addOnShowListener(this);
		this.mDialog.show();
	}

	/**
	 * Setup NFC discovery.
	 */
	private void setupNfc()
	{
		this.mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

		if (this.mNfcAdapter == null)
		{
			Toast.makeText(this, "This device doesn't support NFC.",
				Toast.LENGTH_LONG).show();
			// Remove comment when done.
			//finish();
		}
	}

	/**
	 * Snooze the alarm.
	 */
	public boolean snooze()
	{
		NacUtility.printf("Snoozing alarm! Snooze count : %d / %d", this.mSnoozeCount, this.mShared.maxSnoozes);

		if ((this.mSnoozeCount > this.mShared.maxSnoozes)
			&& (this.mShared.maxSnoozes >= 0))
		{
			Toast.makeText(this, "Unable to snooze the alarm.", Toast.LENGTH_LONG)
				.show();
			return false;
		}

		NacAlarmScheduler scheduler = new NacAlarmScheduler(this);
		Calendar snooze = Calendar.getInstance();
		this.mSnoozeCount += 1;

		Toast.makeText(this, "Alarm snoozed.", Toast.LENGTH_LONG)
			.show();
		this.mPlayer.reset();
		snooze.add(Calendar.MINUTE, this.mShared.snoozeDuration);
		this.mAlarm.setHour(snooze.get(Calendar.HOUR));
		this.mAlarm.setMinute(snooze.get(Calendar.MINUTE));
		NacUtility.printf("Next alarm : %s", snooze.getTime().toString());	
		scheduler.update(this.mAlarm, snooze);
		this.mShared.instance.edit().putInt("snoozeCount"+String.valueOf(this.mAlarm.getId()), this.mSnoozeCount)
			.apply();

		NacUtility.printf("Post Snoozing alarm! Snooze count : %d / %d", this.mSnoozeCount, this.mShared.maxSnoozes);

		return true;
	}

	/**
	 * Vibrate the phone repeatedly until the alarm is dismissed.
	 */
	public void vibrate()
	{
		if (this.mAlarm.getVibrate())
		{
			this.mVibrator = new NacVibrator(this);
			long duration = 500;

			this.mVibrator.execute(duration);
		}
	}

}

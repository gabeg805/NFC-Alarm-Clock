package com.nfcalarmclock;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.Toast;
import java.util.Calendar;

import android.view.LayoutInflater;
import android.content.DialogInterface;
import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.view.animation.AlphaAnimation;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ImageView;

/**
 * Activity to dismiss/snooze the alarm.
 */
public class NacAlarmActivity
	extends Activity
	implements NacDialog.OnDismissListener,NacDialog.OnCancelListener,NacDialog.OnShowListener
{

	private NacAlarmDialog mDialog;
	private NfcAdapter mNfcAdapter;
	private NacMediaPlayer mPlayer;
	private NacAlarm mAlarm;
	private NacSharedPreferences mShared;
	private int mSnoozeCount;

	/**
	 * Create the activity.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setupAlarm();
		setupNfc();
		setupDialog();
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

		return false;
	}

	/**
	 * Called when the dialog is dismissed.
	 */
	@Override
	public boolean onDismissDialog(NacDialog dialog)
	{
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

		if (this.mNfcAdapter == null)
		{
			finish();
		}

		this.mNfcAdapter.disableForegroundDispatch(this);
	}

	/**
	 * Enable the foreground app (this app) to discover NFC tags.
	 */
	@Override
	public void onResume()
	{
		super.onResume();

		if (this.mNfcAdapter == null)
		{
			finish();
		}

		if (!this.mNfcAdapter.isEnabled())
		{
			Toast.makeText(this, "Please enable NFC.", Toast.LENGTH_LONG)
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
		if (this.mShared.autoDismiss == 0)
		{
			return;
		}

		new Handler().postDelayed(new Runnable()
		{
			@Override
			public void run()
			{
				finish();
			}
		}, this.mShared.autoDismiss);
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
		scheduler.update(this.mAlarm, next);
	}

	/**
	 * Setup the alarm.
	 */
	private void setupAlarm()
	{
		this.mShared = new NacSharedPreferences(this);

		Intent intent = getIntent();
		Bundle bundle = (Bundle) intent.getBundleExtra("bundle");
		NacAlarmParcel parcel = (NacAlarmParcel)
			bundle.getParcelable("parcel");
		this.mAlarm = parcel.toAlarm();
		this.mPlayer = new NacMediaPlayer(this);
		this.mSnoozeCount = this.mShared.instance.getInt("snoozeCount"+String.valueOf(this.mAlarm.getId()), 0);

		NacUtility.printf("Alarm Id : %d", this.mAlarm.getId());
		this.scheduleNextAlarm();
		this.mPlayer.play(this.mAlarm.getSound(), true);
		this.vibrate();
	}

	/**
	 * Setup the dialog that will be displayed.
	 */
	public void setupDialog()
	{
		this.mDialog = new NacAlarmDialog();

		this.mDialog.build(this, R.layout.stuff);
		this.mDialog.addCancelListener(this);
		this.mDialog.addDismissListener(this);
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
			finish();
		}
	}

	/**
	 * Snooze the alarm.
	 */
	public boolean snooze()
	{
		if ((this.mSnoozeCount >= this.mShared.maxSnoozes)
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

		return true;
	}

	/**
	 * Vibrate the phone.
	 */
	public void vibrate()
	{
		if (!this.mAlarm.getVibrate())
		{
			return;
		}

		Vibrator vibrator = (Vibrator) getSystemService(
			Context.VIBRATOR_SERVICE);
		long duration = 500;

		if (vibrator.hasVibrator())
		{
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
			{
				vibrator.vibrate(VibrationEffect.createOneShot(duration,
					VibrationEffect.DEFAULT_AMPLITUDE));
			}
			else
			{
				vibrator.vibrate(duration);
			}
		}
	}

}

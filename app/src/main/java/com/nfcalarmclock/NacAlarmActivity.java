package com.nfcalarmclock;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
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
	implements NfcAdapter.ReaderCallback,NacDialog.OnDismissedListener,NacDialog.OnCanceledListener
{

	private NacAlarmDialog mDialog;
	//private AlertDialog mDialog;
	private NfcAdapter mNfcAdapter;
	private NacMediaPlayer mPlayer;
	private Alarm mAlarm;

	/**
	 * Initialize the activity.
	 */
	public void init()
	{
		this.scheduleNextAlarm();
		this.vibrate();

		this.mPlayer = new NacMediaPlayer(this);
		this.mDialog = new NacAlarmDialog();

		this.mPlayer.play(this.mAlarm.getSound(), false);
		this.mDialog.build(this, R.layout.stuff);
		this.mDialog.addCancelListener(this);
		this.mDialog.addDismissListener(this);
		this.mDialog.show();
	}

	/**
	 * Create the activity.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		NacUtility.printf("onCreate() in NacAlarmActivity");

		Intent intent = getIntent();
		Bundle bundle = (Bundle) intent.getBundleExtra("bundle");
		NacAlarmParcel parcel = (NacAlarmParcel)
			bundle.getParcelable("parcel");
		this.mAlarm = parcel.toAlarm();
		this.mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

		if (mNfcAdapter == null)
		{
			Toast.makeText(this, "This device doesn't support NFC.", Toast.LENGTH_LONG).show();
			//finish();
			//return;
		}
		else
		{
			if (!mNfcAdapter.isEnabled())
			{
				Toast.makeText(this, "NFC is not enabled..", Toast.LENGTH_LONG).show();
				return;
			}
		}

		init();
	}

	/**
	 * Called when the dialog is canceled.
	 */
	@Override
	public void onDialogCanceled(NacDialog dialog)
	{
		this.snooze();
		finish();
	}

	/**
	 * Called when the dialog is dismissed.
	 */
	@Override
	public void onDialogDismissed(NacDialog dialog)
	{
		NacUtility.printf("Dialog dismissed.");
		this.mPlayer.reset();
		finish();
	}

	/**
	 * Disable reader mode.
	 *
	 * @see onResume for more information on reader mode.
	 */
	@Override
	public void onPause()
	{
		super.onPause();
		NacUtility.print("onPause() in NacAlarmActivity");

		if (this.mNfcAdapter != null)
		{
			this.mNfcAdapter.disableReaderMode(this);
		}
	}

	/**
	 * Enable reader mode, which means the NFC controller will only act as an
	 * NFC tag reader/writer, thus disabling any peer-to-peer (Android Beam)
	 * and card-emulation modes
	 */
	@Override
	public void onResume()
	{
		super.onResume();
		NacUtility.print("onResume() in NacAlarmActivity");


		if (this.mNfcAdapter != null)
		{
			int flags = NfcAdapter.FLAG_READER_NFC_A
				| NfcAdapter.FLAG_READER_NFC_B
				| NfcAdapter.FLAG_READER_NFC_F
				| NfcAdapter.FLAG_READER_NFC_V
				| NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK;

			this.mNfcAdapter.enableReaderMode(this, this, flags, null);
		}
	}

	/**
	 * Discover an NFC tag.
	 */
	@Override
	public void onTagDiscovered(Tag tag)
	{
		Toast.makeText(this, "Tag has been discovered.", Toast.LENGTH_LONG).show();
		NacUtility.print("Tag has been discovered!");
		this.mDialog.dismiss();
		//IsoDep dep = IsoDep.get(tag);

		//dep.connect();

		//byte[] response = dep.transceive(Utils.hexStringToByteArray("00A4040007A0000002471001"));
		////runOnUiThread { textView.append("\nCard Response: "+ Utils.toHex(response)) }
		//Toast.makeText(this, "Card Response: "+Utils.toHex(response), Toast.LENGTH_LONG).show();

		//dep.close();
	}

	/**
	 * Schedule the next alarm.
	 */
	public void scheduleNextAlarm()
	{
		NacUtility.printf("scheduleNextAlarm()");
		if (!this.mAlarm.getRepeat())
		{
			NacDatabase db = new NacDatabase(this);

			this.mAlarm.toggleToday();
			db.update(this.mAlarm);
			NacUtility.printf("returning from scheduleNextAlarm()");
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
	 * Snooze the alarm.
	 */
	public void snooze()
	{
		NacUtility.printf("Snoozing alarm");
		this.mPlayer.reset();

		NacAlarmScheduler scheduler = new NacAlarmScheduler(this);
		Calendar snooze = Calendar.getInstance();

		snooze.add(Calendar.MINUTE, 1);

		this.mAlarm.setHour(snooze.get(Calendar.HOUR));
		this.mAlarm.setMinute(snooze.get(Calendar.MINUTE));

		NacUtility.printf("Next alarm : %s", snooze.getTime().toString());	
		scheduler.update(this.mAlarm, snooze);
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

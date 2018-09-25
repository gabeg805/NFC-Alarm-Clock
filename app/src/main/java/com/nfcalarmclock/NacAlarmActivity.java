package com.nfcalarmclock;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.animation.Animation;
import android.view.animation.AlphaAnimation;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.Toast;
import java.util.Calendar;

/**
 * @brief Activity to dismiss/snooze the alarm.
 */
public class NacAlarmActivity
	extends Activity
	implements DialogInterface.OnClickListener,DialogInterface.OnShowListener,NfcAdapter.ReaderCallback
{

	private AlertDialog mDialog = null;
	private NfcAdapter mNfcAdapter = null;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		//setContentView(R.layout.stuff);

		mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
		Context context = this.getApplicationContext();
		Intent intent = this.getIntent();
		Bundle bundle = (Bundle) intent.getBundleExtra("bundle");
		NacAlarmParcel parcel = (NacAlarmParcel)
			bundle.getParcelable("parcel");
		Alarm alarm = parcel.toAlarm();

		alarm.print();

		//if (mNfcAdapter == null)
		//{
		//	// Stop here, we definitely need NFC
		//	Toast.makeText(this, "This device doesn't support NFC.", Toast.LENGTH_LONG).show();
		//	finish();
		//	return;

		//}

		//if (!mNfcAdapter.isEnabled())
		//{
		//	Toast.makeText(this, "NFC is not enabled..", Toast.LENGTH_LONG).show();
		//	return;
		//}

		vibrate(context, alarm);
		playRingtone(context, alarm);
		scheduleNextAlarm(context, alarm);
		showDialog();
	}

	@Override
	public void onResume()
	{
		NacUtility.print("RESUME has been on!");
		super.onResume();

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

	@Override
	public void onPause()
	{
		NacUtility.print("PAUSE has been on!");
		super.onPause();

		if (this.mNfcAdapter != null)
		{
			this.mNfcAdapter.disableReaderMode(this);
		}
	}

	@Override
	public void onTagDiscovered(Tag t)
	{
		NacUtility.print("Tag has been discovered!");
		//IsoDep dep = IsoDep.get(tag)

		//dep.connect()

		////val response = dep.transceive(Utils.hexStringToByteArray(
		////		"00A4040007A0000002471001"))
		////runOnUiThread { textView.append("\nCard Response: "
		////		+ Utils.toHex(response)) }

		//dep.close()
	}

	public void showDialog()
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		//LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
		//View v = inflater.inflate(R.layout.stuff, (ViewGroup)null);

		//builder.setView(v);
		builder.setView(R.layout.stuff);
		builder.setTitle("Dismiss Alarm");
		builder.setIcon(R.mipmap.ic_launcher);
		builder.setPositiveButton("Snooze", this);
		//builder.setCancelable(false);

		this.mDialog = builder.create();

		this.mDialog.setCancelable(false);
		this.mDialog.setCanceledOnTouchOutside(false);
		this.mDialog.setOnShowListener(this);
		this.mDialog.show();
	}

	/**
	 * @brief Schedule the next alarm.
	 *
	 * @param  c  Application context.
	 * @param  a  Alarm.
	 */
	public void scheduleNextAlarm(Context c, Alarm a)
	{
		NacUtility.printf("scheduleNextAlarm()");
		if (!a.getRepeat())
		{
			NacDatabase db = new NacDatabase(this);

			a.toggleToday();
			db.update(a);
			NacUtility.printf("returning from scheduleNextAlarm()");
			return;
		}

		NacAlarmScheduler scheduler = new NacAlarmScheduler(c);
		Calendar next = Calendar.getInstance();

		next.set(Calendar.HOUR_OF_DAY, a.getHour());
		next.set(Calendar.MINUTE, a.getMinute());
		next.set(Calendar.SECOND, 0);
		next.set(Calendar.MILLISECOND, 0);
		next.add(Calendar.DAY_OF_MONTH, 7);

		NacUtility.printf("Next alarm : %s", next.getTime().toString());	
		scheduler.update(a, next);
	}

	/**
	 * @brief Play the alarm ringtone.
	 *
	 * @param  c  Application context.
	 * @param  i  Intent.
	 */
	public void playRingtone(Context c, Alarm a)
	{
		String sound = a.getSound();

		if (sound.isEmpty())
		{
			return;
		}

		// Change to looping once complete
		Uri uri = Uri.parse(sound);
		NacMediaPlayer player = new NacMediaPlayer(c);
		player.play(uri, false);
	}

	/**
	 * @brief Vibrate the phone.
	 */
	public void vibrate(Context c, Alarm a)
	{
		if (!a.getVibrate())
		{
			return;
		}

		Vibrator v = (Vibrator) c.getSystemService(Context.VIBRATOR_SERVICE);
		long duration = 500;

		if (v.hasVibrator())
		{
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
			{
				v.vibrate(VibrationEffect.createOneShot(duration,
					VibrationEffect.DEFAULT_AMPLITUDE));
			}
			else
			{
				v.vibrate(duration);
			}
		}
	}

	@Override
	public void onShow(DialogInterface dialog)
	{
		ImageView icon = (ImageView) mDialog.findViewById(R.id.nac_nfc_icon);
		AlphaAnimation animation = new AlphaAnimation(0.1f, 1f);
		ViewGroup.LayoutParams params = icon.getLayoutParams();
		int width = mDialog.getWindow().getDecorView().getWidth();
		int height = mDialog.getWindow().getDecorView().getHeight();
		int duration = 2000;
		//params.width = width/3;
		//params.height = height/3;

		NacUtility.printf("Dialog WxH : %d x %d", width, height);
		NacUtility.printf("Icon WxH   : %d x %d", params.width, params.height);

		icon.setLayoutParams(params);
		animation.setDuration(duration);
		animation.setRepeatMode(ValueAnimator.REVERSE);
		animation.setRepeatCount(ValueAnimator.INFINITE);
		icon.startAnimation(animation);
	}

	@Override
	public void onClick(DialogInterface dialog, int which)
	{
		NacAlarmActivity.this.finish();
	}

}

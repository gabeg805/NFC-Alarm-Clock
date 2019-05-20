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
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;
import java.util.List;

import android.speech.tts.TextToSpeech;
import java.util.Locale;

/**
 * Activity to dismiss/snooze the alarm.
 */
public class NacAlarmActivity
	extends Activity
	implements Runnable,
		View.OnClickListener
{

	/**
	 * Text to speech.
	 */
	public class NacTextToSpeech
		implements TextToSpeech.OnInitListener
	{

		/**
		 * The context.
		 */
		private Context mContext;

		/**
		 * The speech engine.
		 */
		private TextToSpeech mSpeech;

		/**
		 * Message buffer to speak.
		 */
		private String mBuffer;

		/**
		 * Check if speech engine is initialized.
		 */
		private boolean mInitialized;

		/**
		 */
		public NacTextToSpeech(Context context)
		{
			this.mContext = context;
			this.mSpeech = null;
			this.mBuffer = "";
			this.mInitialized = false;
		}

		/**
		 * @return The speech buffer.
		 */
		private String getBuffer()
		{
			return this.mBuffer;
		}

		/**
		 * @return The context.
		 */
		private Context getContext()
		{
			return this.mContext;
		}

		/**
		 * @return The speech engine.
		 */
		private TextToSpeech getTextToSpeech()
		{
			return this.mSpeech;
		}

		/**
		 * @return True if there is a buffer present and False otherwise.
		 */
		public boolean hasBuffer()
		{
			String buffer = this.getBuffer();

			return ((buffer != null) && (!buffer.isEmpty()));
		}

		/**
		 * @return True if the speech engine is initialized and False otherwise.
		 */
		public boolean isInitialized()
		{
			return (this.mInitialized && (this.mSpeech != null));
		}

		/**
		 */
		@Override
		public void onInit(int status)
		{
			NacUtility.printf("onInit! %d", status);
			this.mInitialized = (status == TextToSpeech.SUCCESS);

			if (this.isInitialized())
			{
				NacUtility.printf("Initialized!");
				TextToSpeech speech = this.getTextToSpeech();

				speech.setLanguage(Locale.US);

				if (this.hasBuffer())
				{
					String buffer = this.getBuffer();
					NacUtility.printf("Buffer is Present on initialisation!");

					this.speak(buffer);
					this.setBuffer("");
				}
			}
		}

		/**
		 * Set the speech message buffer.
		 */
		private void setBuffer(String buffer)
		{
			NacUtility.printf("setting Buffer! '%s'", buffer);
			this.mBuffer = buffer;
		}

		/**
		 * Shutdown the speech engine.
		 */
		public void shutdown()
		{
			//TextToSpeech speech = this.getTextToSpeech();

			if (this.mSpeech != null)
			{
				this.mSpeech.shutdown();

				this.mSpeech = null;
			}
		}

		/**
		 * Speak the given text.
		 */
		public void speak(String message)
		{
			Context context = this.getContext();
			TextToSpeech speech = this.getTextToSpeech();
			NacUtility.printf("Speaking! %s", message);

			if (speech == null)
			{
				NacUtility.printf("Speech is null so need to create it!");
				this.mSpeech = new TextToSpeech(context, this);
				speech = this.mSpeech;
			}

			if (this.isInitialized())
			{
				NacUtility.printf("Speech is already initialized! Speaking!");
				speech.speak(message, TextToSpeech.QUEUE_FLUSH, null, "AlarmTime");
			}
			else
			{
				NacUtility.printf("Speech is NOT already initialized! Need to set buffer");
				this.setBuffer(message);
			}
		}

		/**
		 * Stop the speech engine.
		 */
		public void stop()
		{
			TextToSpeech speech = this.getTextToSpeech();

			if (speech != null)
			{
				speech.stop();
			}
		}

	}

	/**
	 * The text-to-speech engine.
	 */
	private NacTextToSpeech mSpeech;

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
		NacVibrator vibrator = this.getVibrator();
		NacMediaPlayer player = this.getMediaPlayer();
		Handler handler = this.getHandler();

		if (vibrator != null)
		{
			vibrator.cancel(true);
		}

		if (player != null)
		{
			player.release();
		}

		if (handler != null)
		{
			handler.removeCallbacksAndMessages(null);
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
		this.cleanup();
		finish();
	}

	/**
	 * @see dismiss
	 */
	private void dismiss(String message)
	{
		if (!message.isEmpty())
		{
			NacUtility.quickToast(this, message);
		}

		this.dismiss();
	}

	/**
	 * Enable NFC dispatch, so that the app can discover NFC tags.
	 */
	private void enableNfc()
	{
		NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
		NacSharedPreferences shared = new NacSharedPreferences(this);

		if (!shared.getRequireNfc())
		{
			return;
		}
		else if (nfcAdapter == null)
		{
			NacUtility.quickToast(this, "Your device doesn't support NFC");
			return;
		}
		else
		{
			if (!nfcAdapter.isEnabled())
			{
				NacUtility.toast(this, "Please enable NFC to dismiss the alarm");
				startActivity(new Intent(Settings.ACTION_NFC_SETTINGS));
			}

			Intent intent = new Intent(this, NacAlarmActivity.class)
				.addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);
			PendingIntent pending = PendingIntent.getActivity(this, 0, intent,
				0);
			IntentFilter[] filter = new IntentFilter[]{};

			nfcAdapter.enableForegroundDispatch(this, pending, filter, null);
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
	 * @return The handler.
	 */
	private Handler getHandler()
	{
		return this.mHandler;
	}

	/**
	 * @return The media player.
	 */
	private NacMediaPlayer getMediaPlayer()
	{
		return this.mPlayer;
	}

	/**
	 * @return The phone vibrator.
	 */
	private NacVibrator getVibrator()
	{
		return this.mVibrator;
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
		int id = view.getId();

		if (id == R.id.act_alarm)
		{
			NacSharedPreferences shared = new NacSharedPreferences(this);

			if (shared.getEasySnooze())
			{
				this.snooze("Alarm snoozed");
			}
		}
		else if (id == R.id.snooze)
		{
			this.snooze("Alarm snoozed");
		}
		else if (id == R.id.dismiss)
		{
			this.dismiss("Alarm dismissed");
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

		this.mAlarm = NacIntent.getAlarm(getIntent());
		this.mPlayer = new NacMediaPlayer(this);
		this.mVibrator = new NacVibrator(this);
		this.mHandler = new Handler();
		this.mSpeech = new NacTextToSpeech(this);

		this.scheduleNextAlarm();
		this.setupAlarmButtons();
		//this.playMusic();
		//this.vibrate();
		this.waitForAutoDismiss();
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
		NacUtility.printf("onPause!");
		this.mSpeech.stop();

		//if (this.mSpeech != null)
		//{
		//	NacUtility.printf("Stopping the speech engine!");
		//	this.mSpeech.stop();
		//}
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		NacUtility.printf("onDestroy!");
		//this.mSpeech.shutdown();

		//if (this.mSpeech != null)
		//{
		//	NacUtility.printf("Shutting down the speech engine!");
		//	this.mSpeech.shutdown();

		//	this.mSpeech = null;
		//}
	}

	/**
	 * Enable tag discovery.
	 */
	@Override
	public void onResume()
	{
		super.onResume();
		this.enableNfc();
		NacUtility.printf("onResume!");
		this.mSpeech.speak("The time, is, 8, O-Clock, AM.");

		//if (this.mSpeech == null)
		//{
		//	this.mSpeech = new TextToSpeech(this, this);
		//}
		//else
		//{
		//	this.mSpeech.speak("The time is 8 O-Clock.", TextToSpeech.QUEUE_FLUSH, null);
		//}
	}

	/**
	 * Play music.
	 */
	private void playMusic()
	{
		NacSharedPreferences shared = new NacSharedPreferences(this);
		NacMediaPlayer player = this.getMediaPlayer();
		NacAlarm alarm = this.getAlarm();
		String path = alarm.getSoundPath();
		int type = alarm.getSoundType();
		boolean repeat = true;
		boolean shuffle = shared.getShuffle();

		if (player == null)
		{
			return;
		}

		player.reset();

		if (NacSound.isFilePlaylist(type))
		{
			player.playPlaylist(path, repeat, shuffle);
		}
		else
		{
			player.play(path, repeat);
		}
	}

	/**
	 * Automatically dismiss the alarm.
	 */
	@Override
	public void run()
	{
		this.dismiss("Auto-dismissed the alarm");
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
		NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
		LinearLayout layout = (LinearLayout) findViewById(R.id.act_alarm);
		Button snoozeButton = (Button) findViewById(R.id.snooze);
		Button dismissButton = (Button) findViewById(R.id.dismiss);

		if ((nfcAdapter == null) || !shared.getRequireNfc())
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

	/**
	 * Snooze the alarm.
	 */
	public boolean snooze(String message)
	{
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
			return false;
		}
	}

	/**
	 * Vibrate the phone repeatedly until the alarm is dismissed.
	 */
	public void vibrate()
	{
		NacAlarm alarm = this.getAlarm();
		NacVibrator vibrator = this.getVibrator();
		long duration = 500;

		if (alarm.getVibrate() && (vibrator != null))
		{
			vibrator.execute(duration);
		}
	}

	/**
	 * Wait in the background until the activity needs to auto dismiss the
	 * alarm.
	 *
	 * Auto dismiss a bit early to avoid the race condition between a new alarm
	 * starting at the same time that the alarm will auto-dismiss.
	 */
	public void waitForAutoDismiss()
	{
		NacSharedPreferences shared = new NacSharedPreferences(this);
		int autoDismiss = shared.getAutoDismissTime();
		long delay = TimeUnit.MINUTES.toMillis(autoDismiss) - 2000;

		if (autoDismiss != 0)
		{
			this.mHandler.postDelayed(this, delay);
		}
	}

}

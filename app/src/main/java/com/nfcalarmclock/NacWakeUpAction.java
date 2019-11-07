package com.nfcalarmclock;

// CHANGE NAME TO NACWAKEUPPROCESS?
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Vibrator;
import android.os.VibrationEffect;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

/**
 * Actions to take upon waking up, such as enabling NFC, playing music, etc.
 */
public class NacWakeUpAction
	implements Runnable,
		NacTextToSpeech.OnSpeakingListener
{

	/**
	 * Auto dismiss listener interface.
	 */
	public interface OnAutoDismissListener
	{
		public void onAutoDismiss(NacAlarm alarm);
	}

	/**
	 * The application context.
	 */
	private Context mContext;

	/**
	 * Alarm.
	 */
	private NacAlarm mAlarm;

	/**
	 * Shared preferences.
	 */
	private NacSharedPreferences mSharedPreferences;

	/**
	 * Media player.
	 */
	private NacMediaPlayer mPlayer;

	/**
	 * Vibrate the phone.
	 */
	//private NacVibrator mVibrator;
	private Vibrator mVibrator;

	/**
	 * The text-to-speech engine.
	 */
	private NacTextToSpeech mSpeech;

	/**
	 * Automatically dismiss the alarm in case it does not get dismissed.
	 */
	private Handler mAutoDismissHandler;

	/**
	 * Say the current time at user specified intervals.
	 */
	private Handler mSpeakHandler;

	/**
	 * On auto dismiss listener.
	 */
	private OnAutoDismissListener mListener;

	/**
	 */
	public NacWakeUpAction(Context context, NacAlarm alarm)
	{
		this.mContext = context;
		this.mAlarm = alarm;
		this.mSharedPreferences = new NacSharedPreferences(context);
		this.mPlayer = new NacMediaPlayer(context);
		//this.mVibrator = null;
		this.mVibrator = (Vibrator) context.getSystemService(
			Context.VIBRATOR_SERVICE);
		this.mSpeech = new NacTextToSpeech(context, this);
		this.mAutoDismissHandler = new Handler();
		this.mSpeakHandler = new Handler();
		this.mListener = null;
		//this.setupVibrate();
	}

	/**
	 * Cleanup various alarm objects.
	 */
	public void cleanup()
	{
		Context context = this.getContext();
		Handler autoDismissHandler = this.getAutoDismissHandler();
		Handler speakHandler = this.getSpeakHandler();

		// Add a thing for Nfc to dismiss its activity if it is still running
		this.stopVibrate();
		this.stopPlayer();

		if (autoDismissHandler != null)
		{
			autoDismissHandler.removeCallbacksAndMessages(null);
		}

		if (speakHandler != null)
		{
			speakHandler.removeCallbacksAndMessages(null);
		}

		NacNfc.finish(context);
	}

	/**
	 * @return The alarm.
	 */
	private NacAlarm getAlarm()
	{
		return this.mAlarm;
	}

	/**
	 * @return The auto dismiss handler.
	 */
	private Handler getAutoDismissHandler()
	{
		return this.mAutoDismissHandler;
	}

	/**
	 * @return The context.
	 */
	private Context getContext()
	{
		return this.mContext;
	}

	/**
	 * @return The auto dismiss listener.
	 */
	private OnAutoDismissListener getOnAutoDismissListener()
	{
		return this.mListener;
	}

	/**
	 * @return The media player.
	 */
	private NacMediaPlayer getMediaPlayer()
	{
		return this.mPlayer;
	}

	/**
	 * @return The shared preferences.
	 */
	private NacSharedPreferences getSharedPreferences()
	{
		return this.mSharedPreferences;
	}

	/**
	 * @return The handler to say the current time periodically.
	 */
	private Handler getSpeakHandler()
	{
		return this.mSpeakHandler;
	}

	/**
	 * @return Text-to-speech engine.
	 */
	private NacTextToSpeech getTextToSpeech()
	{
		return this.mSpeech;
	}

	/**
	 * @return The time to say.
	 */
	private String getTimeToSay()
	{
		Context context = this.getContext();
		Calendar calendar = Calendar.getInstance();
		boolean format = NacCalendar.Time.is24HourFormat(context);
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int minute = calendar.get(Calendar.MINUTE);
		String[] time = NacCalendar.Time.getClockTime(hour, minute, format)
			.split(":");
		String ampm = (!format) ? ((hour < 12) ? ", AM" : ", PM") : "";

		if (time[1].charAt(0) == '0')
		{
			if (time[1].charAt(1) == '0')
			{
				time[1] = "";
			}
			else
			{
				time[1] = "O, " + time[1].charAt(1);
			}
		}

		if (!time[1].isEmpty())
		{
			time[1] = ", " + time[1];
		}

		NacUtility.printf("The time, is, "+time[0]+time[1]+ampm);

		return "The time, is, " + time[0] + time[1] + ampm;
	}

	/**
	 * @return The phone vibrator.
	 */
	//private NacVibrator getVibrator()
	private Vibrator getVibrator()
	{
		return this.mVibrator;
	}

	/**
	 */
	@Override
	public void onDoneSpeaking()
	{
		this.playMusic();
		this.vibrate();
	}

	/**
	 */
	@Override
	public void onStartSpeaking()
	{
		this.stopVibrate();
	}

	/**
	 * Pause the wake up process.
	 */
	public void pause()
	{
		Context context = this.getContext();
		NacAlarm alarm = this.getAlarm();

		if ((alarm != null) && alarm.getUseNfc())
		{
			NacNfc.disable(context);
		}

		this.getTextToSpeech().stop();
		//this.getTextToSpeech().shutdown();
	}

	/**
	 * Play music.
	 */
	private void playMusic()
	{
		NacSharedPreferences shared = this.getSharedPreferences();
		NacMediaPlayer player = this.getMediaPlayer();
		NacAlarm alarm = this.getAlarm();

		if ((alarm == null) || (player == null) || player.isPlaying()
			|| player.wasPlaying())
		{
			return;
		}

		player.reset();
		player.play(alarm, true, shared.getShuffle());
	}

	/**
	 * Resume the wake up process.
	 */
	public void resume()
	{
		Context context = this.getContext();
		NacAlarm alarm = this.getAlarm();
		//NacSharedPreferences shared = this.getSharedPreferences();

		if ((alarm != null) && alarm.getUseNfc())
		{
			NacNfc.enable(context);
		}

		//if (shared.getSpeakToMe())
		//{
		//	this.speak();
		//}
	}

	/**
	 * Automatically dismiss the alarm.
	 */
	@Override
	public void run()
	{
		if (this.getOnAutoDismissListener() != null)
		{
			NacAlarm alarm = this.getAlarm();

			this.getOnAutoDismissListener().onAutoDismiss(alarm);
		}
	}

	/**
	 * Set the auto dismiss listener.
	 */
	public void setOnAutoDismissListener(OnAutoDismissListener listener)
	{
		this.mListener = listener;
	}

	/**
	 * @return Setup a new vibrator object.
	 */
	private void setupVibrate()
	{
		this.stopVibrate();

		//Activity activity = (Activity) this.getContext();
		//Context context = this.getContext();
		//NacAlarm alarm = this.getAlarm();
		//this.mVibrator = ((alarm == null) || alarm.getVibrate())
		//	? new NacVibrator(context) : null;
	}

	/**
	 * Shutdown the wakeup actions.
	 */
	public void shutdown()
	{
		//this.getMediaPlayer().release();
		this.getTextToSpeech().shutdown();
	}

	/**
	 * Speak at the desired frequency, specified in the shared preference.
	 */
	private void speak()
	{
		NacSharedPreferences shared = this.getSharedPreferences();
		final Context context = this.getContext();
		final long freq = shared.getSpeakFrequency() * 60L * 1000L;
		final Handler handler = this.getSpeakHandler();
		final Runnable sayTime = new Runnable()
			{
				@Override
				public void run()
				{
					NacTextToSpeech speech = getTextToSpeech();

					if (speech.isSpeaking() || speech.hasBuffer())
					{
						return;
					}

					String text = getTimeToSay();
					NacAlarm alarm = getAlarm();
					NacAudio.Attributes attrs = new NacAudio.Attributes(
						context, alarm);

					speech.speak(text, attrs);

					if (freq != 0)
					{
						handler.postDelayed(this, freq);
					}
				}
			};

		handler.post(sayTime);
	}

	/**
	 * Start the wake up process.
	 */
	public void start()
	{
		NacSharedPreferences shared = this.getSharedPreferences();

		if (shared.getSpeakToMe())
		{
			this.speak();
		}
		else
		{
			this.playMusic();
			this.vibrate();
		}

		this.waitForAutoDismiss();
	}

	/**
	 * Stop the media player.
	 */
	private void stopPlayer()
	{
		Context context = this.getContext();
		NacMediaPlayer player = this.getMediaPlayer();

		if (player == null)
		{
			player = new NacMediaPlayer(context);
		}

		player.resetWrapper();
		player.stopWrapper();
		player.release();
	}

	/**
	 * Stop vibrating the phone.
	 */
	private void stopVibrate()
	{
		//NacVibrator vibrator = this.getVibrator();
		Vibrator vibrator = this.getVibrator();

		if (vibrator == null)
		{
			Context context = this.getContext();
			vibrator = (Vibrator) context.getSystemService(
				Context.VIBRATOR_SERVICE);
			this.mVibrator = vibrator;
		}

		if (vibrator != null)
		{
			vibrator.cancel();
		}

		//if (vibrator != null)
		//{
		//	if (!vibrator.isFinished())
		//	{
		//		vibrator.cancel(true);
		//	}
		//}
		//else
		//{
		//	Context context = this.getContext();
		//	Vibrator v = (Vibrator) context.getSystemService(
		//		Context.VIBRATOR_SERVICE);

		//	v.cancel();
		//}
	}

	/**
	 * Vibrate the phone repeatedly until the alarm is dismissed.
	 */
	@SuppressWarnings("deprecation")
	@TargetApi(Build.VERSION_CODES.O)
	public void vibrate()
	{
		//this.setupVibrate();
		this.stopVibrate();

		//NacVibrator vibrator = this.getVibrator();
		NacAlarm alarm = this.getAlarm();
		Vibrator vibrator = this.getVibrator();
		long duration = 500;
		long[] pattern = {0, duration, 2*duration};

		if ((vibrator != null) && (alarm != null) && alarm.getVibrate())
		{
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
			{
				//vibrator.vibrate(VibrationEffect.createOneShot(duration,
				//	VibrationEffect.DEFAULT_AMPLITUDE));
				vibrator.vibrate(VibrationEffect.createWaveform(pattern, 0));
			}
			else
			{
				vibrator.vibrate(pattern, 0);
			}

			//vibrator.execute(duration);
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
		Context context = this.getContext();
		NacSharedPreferences shared = this.getSharedPreferences();
		int autoDismiss = shared.getAutoDismissTime();
		long delay = TimeUnit.MINUTES.toMillis(autoDismiss) - 2000;

		if (autoDismiss != 0)
		{
			this.getAutoDismissHandler().postDelayed(this, delay);
		}
	}

}

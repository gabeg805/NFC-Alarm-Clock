package com.nfcalarmclock;

// CHANGE NAME TO NACWAKEUPPROCESS?
import android.app.Activity;
import android.content.Context;
import android.os.Handler;
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
		public void onAutoDismiss();
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
	 * The text-to-speech engine.
	 */
	private NacTextToSpeech mSpeech;

	/**
	 * Automatically dismiss the alarm in case it does not get dismissed.
	 */
	private Handler mHandler;

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
		this.mPlayer = new NacMediaPlayer(context);
		this.mSpeech = new NacTextToSpeech(context, this);
		this.mHandler = new Handler();
		this.mListener = null;

		this.setupVibrator();
	}

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
	 * @return The alarm.
	 */
	private NacAlarm getAlarm()
	{
		return this.mAlarm;
	}

	/**
	 * @return The context.
	 */
	private Context getContext()
	{
		return this.mContext;
	}

	/**
	 * @return The handler.
	 */
	private Handler getHandler()
	{
		return this.mHandler;
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
	 * @return Text-to-speech engine.
	 */
	private NacTextToSpeech getTextToSpeech()
	{
		return this.mSpeech;
	}

	/**
	 * @return The phone vibrator.
	 */
	private NacVibrator getVibrator()
	{
		return this.mVibrator;
	}

	/**
	 */
	@Override
	public void onDoneSpeaking()
	{
		NacUtility.printf("onDoneSpeaking!");
		// Will play music cause issues?
		this.playMusic();
		this.vibrate();
	}

	/**
	 */
	@Override
	public void onStartSpeaking()
	{
		NacUtility.printf("onStartSpeaking!");

		this.stopVibrate();
	}

	/**
	 * Pause the wake up process.
	 */
	public void pause()
	{
		Context context = this.getContext();

		NacNfc.disable(context);
		this.getTextToSpeech().stop();
		//this.getTextToSpeech().shutdown();
	}

	/**
	 * Play music.
	 */
	private void playMusic()
	{
		Context context = this.getContext();
		NacAlarm alarm = this.getAlarm();
		NacMediaPlayer player = this.getMediaPlayer();
		NacSharedPreferences shared = new NacSharedPreferences(context);
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
	 * Resume the wake up process.
	 */
	public void resume()
	{
		Context context = this.getContext();

		NacNfc.enable(context);
		this.speak();
	}

	/**
	 * Automatically dismiss the alarm.
	 */
	@Override
	public void run()
	{
		if (this.getOnAutoDismissListener() != null)
		{
			this.getOnAutoDismissListener().onAutoDismiss();
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
	private void setupVibrator()
	{
		this.stopVibrate();

		Activity activity = (Activity) this.getContext();
		NacAlarm alarm = this.getAlarm();
		this.mVibrator = alarm.getVibrate() ? new NacVibrator(activity) : null;
	}

	/**
	 * Shutdown the wakeup actions.
	 */
	public void shutdown()
	{
		//this.getTextToSpeech().shutdown();
	}

	/**
	 * Speak the current time.
	 */
	private void speak()
	{
		NacTextToSpeech speech = this.getTextToSpeech();

		if (speech.isSpeaking() || speech.hasBuffer())
		{
			NacUtility.printf("Speaking is already occurring!");
			return;
		}

		NacUtility.printf("Running speak from WakeUpAction!");
		Calendar calendar = Calendar.getInstance();
		String hour = String.valueOf(calendar.get(Calendar.HOUR));
		String minute = String.valueOf(calendar.get(Calendar.MINUTE));
		String ampm = (calendar.get(Calendar.AM_PM) == 0) ? "AM" : "PM";

		speech.speak("The time, is, "+hour+", "+minute+", "+ampm);
	}

	/**
	 * Start the wake up process.
	 */
	public void start()
	{
		NacUtility.printf("Starting wake up process!");
		this.speak();
		this.waitForAutoDismiss();
	}

	/**
	 * Stop vibrating the phone.
	 */
	private void stopVibrate()
	{
		NacVibrator vibrator = this.getVibrator();

		if (vibrator != null)
		{
			if (!vibrator.isFinished())
			{
				NacUtility.printf("Canceling vibrate.");
				vibrator.cancel(true);
			}
		}
	}

	/**
	 * Vibrate the phone repeatedly until the alarm is dismissed.
	 */
	public void vibrate()
	{
		NacUtility.printf("Prepping vibrate.");
		this.setupVibrator();

		NacVibrator vibrator = this.getVibrator();
		long duration = 500;

		NacUtility.printf("Starting vibrate.");
		if (vibrator != null)
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
		Context context = this.getContext();
		NacSharedPreferences shared = new NacSharedPreferences(context);
		int autoDismiss = shared.getAutoDismissTime();
		long delay = TimeUnit.MINUTES.toMillis(autoDismiss) - 2000;

		if (autoDismiss != 0)
		{
			this.getHandler().postDelayed(this, delay);
		}
	}

}

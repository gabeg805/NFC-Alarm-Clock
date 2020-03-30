package com.nfcalarmclock;

import android.annotation.TargetApi;
import android.content.Context;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.os.Vibrator;
import android.os.VibrationEffect;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

/**
 * Actions to take upon waking up, such as enabling NFC, playing music, etc.
 */
public class NacWakeupProcess
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
	public NacWakeupProcess(Context context, NacAlarm alarm)
	{
		this.mContext = context;
		this.mAlarm = alarm;
		this.mSharedPreferences = new NacSharedPreferences(context);
		this.mPlayer = new NacMediaPlayer(context,
			AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
		this.mVibrator = (Vibrator) context.getSystemService(
			Context.VIBRATOR_SERVICE);
		this.mSpeech = new NacTextToSpeech(context, this);
		this.mAutoDismissHandler = new Handler();
		this.mSpeakHandler = new Handler();
		this.mListener = null;

		NacTextToSpeech speech = this.getTextToSpeech();
		speech.getAudioAttributes().merge(alarm);
	}

	/**
	 * @return True if music can be played, and False otherwise.
	 */
	private boolean canPlayMusic()
	{
		NacMediaPlayer player = this.getMediaPlayer();
		NacAlarm alarm = this.getAlarm();

		// Might want to override whats playing when waking up.
		return ((alarm != null) && (player != null) && alarm.hasMedia());
	}

	/**
	 * @return True if the user wants to use to text-to-speech, and False
	 *         otherwise.
	 */
	private boolean canUseTts()
	{
		NacSharedPreferences shared = this.getSharedPreferences();
		return ((shared != null) && shared.getSpeakToMe());
	}

	/**
	 * Cleanup various alarm objects.
	 */
	public void cleanup()
	{
		Context context = this.getContext();
		Handler autoDismissHandler = this.getAutoDismissHandler();
		Handler speakHandler = this.getSpeakHandler();

		this.cleanupVibrate();
		this.cleanupPlayer();

		if (autoDismissHandler != null)
		{
			autoDismissHandler.removeCallbacksAndMessages(null);
		}

		if (speakHandler != null)
		{
			speakHandler.removeCallbacksAndMessages(null);
		}

		this.getTextToSpeech().shutdown();
	}

	/**
	 * Cleanup the media player.
	 */
	private void cleanupPlayer()
	{
		Context context = this.getContext();
		NacMediaPlayer player = this.getMediaPlayer();

		if (player == null)
		{
			player = new NacMediaPlayer(context);
		}

		player.resetWrapper();
		player.releaseWrapper();
	}

	/**
	 * Cleanup vibrating the phone.
	 */
	private void cleanupVibrate()
	{
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

		String say = String.format(", , The time, is, %1$s %2$s %3$s", time[0],
			time[1], ampm);

		return say;
	}

	/**
	 * @return The phone vibrator.
	 */
	private Vibrator getVibrator()
	{
		return this.mVibrator;
	}

	/**
	 */
	@Override
	public void onDoneSpeaking(NacTextToSpeech tts, NacAudio.Attributes attrs)
	{
		this.playMusic();
		this.vibrate();
	}

	/**
	 */
	@Override
	public void onStartSpeaking(NacTextToSpeech tts, NacAudio.Attributes attrs)
	{
		this.cleanupVibrate();
	}

	/**
	 * Pause the wake up process.
	 */
	public void pause()
	{
		this.getTextToSpeech().stop();
	}

	/**
	 * Play music.
	 */
	private void playMusic()
	{
		NacSharedPreferences shared = this.getSharedPreferences();
		NacMediaPlayer player = this.getMediaPlayer();
		NacAlarm alarm = this.getAlarm();

		if (!this.canPlayMusic())
		{
			return;
		}

		if (!player.wasPlaying())
		{
			player.reset();
			player.play(alarm, true, shared.getShuffle());
		}
		else
		{
			player.startWrapper();
		}
	}

	/**
	 * Resume the wake up process.
	 */
	public void resume()
	{
		NacSharedPreferences shared = this.getSharedPreferences();

		if (shared.getSpeakToMe())
		{
			this.speak();
		}
	}

	/**
	 * Automatically dismiss the alarm.
	 */
	@Override
	public void run()
	{
		OnAutoDismissListener listener = this.getOnAutoDismissListener();
		NacAlarm alarm = this.getAlarm();

		if (listener != null)
		{
			listener.onAutoDismiss(alarm);
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
	 * Setup the volume for the music player and text-to-speech engine.
	 */
	private void setupVolume()
	{
		NacAlarm alarm = this.getAlarm();
		NacMediaPlayer player = this.getMediaPlayer();
		NacAudio.Attributes attrs = player.getAudioAttributes();

		attrs.merge(alarm).setVolume();
	}

	/**
	 * Speak at the desired frequency, specified in the shared preference.
	 */
	private void speak()
	{
		NacSharedPreferences shared = this.getSharedPreferences();
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
					speech.speak(text);

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
		this.setupVolume();

		if (this.canUseTts())
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
	 * Vibrate the phone repeatedly until the alarm is dismissed.
	 */
	@SuppressWarnings("deprecation")
	@TargetApi(Build.VERSION_CODES.O)
	public void vibrate()
	{
		this.cleanupVibrate();

		NacAlarm alarm = this.getAlarm();
		Vibrator vibrator = this.getVibrator();
		long duration = 500;
		long[] pattern = {0, duration, duration};

		if ((vibrator != null) && (alarm != null) && alarm.getVibrate())
		{
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
			{
				vibrator.vibrate(VibrationEffect.createWaveform(pattern, 0));
			}
			else
			{
				vibrator.vibrate(pattern, 0);
			}
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

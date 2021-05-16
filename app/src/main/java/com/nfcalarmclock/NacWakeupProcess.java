package com.nfcalarmclock;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Vibrator;
import android.os.VibrationEffect;
import java.util.concurrent.TimeUnit;

/**
 * Actions to take upon waking up, such as enabling NFC, playing music, etc.
 */
@SuppressWarnings({"RedundantSuppression", "UnnecessaryInterfaceModifier"})
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
	private final Context mContext;

	/**
	 * Alarm.
	 */
	private final NacAlarm mAlarm;

	/**
	 * Shared preferences.
	 */
	private final NacSharedPreferences mSharedPreferences;

	/**
	 * Automatically dismiss the alarm in case it does not get dismissed.
	 */
	private final Handler mAutoDismissHandler;

	/**
	 * On auto dismiss listener.
	 */
	private OnAutoDismissListener mAutoDismissListener;

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
	 * Say the current time at user specified intervals.
	 */
	private Handler mSpeakHandler;

	/**
	 */
	public NacWakeupProcess(Context context, NacAlarm alarm)
	{
		this.mContext = context;
		this.mAlarm = alarm;
		this.mSharedPreferences = new NacSharedPreferences(context);
		this.mAutoDismissHandler = new Handler();
		this.mAutoDismissListener = null;

		// Setup vibrate
		if (this.canVibrate())
		{
			this.mVibrator = (Vibrator) context.getSystemService(
				Context.VIBRATOR_SERVICE);
		}

		// Setup music player
		if (this.canPlayMusic())
		{
			this.mPlayer = new NacMediaPlayer(context);
		}

		// Setup TTS
		if (this.canUseTts())
		{
			NacTextToSpeech speech = new NacTextToSpeech(context, this);
			speech.getAudioAttributes().merge(alarm);

			this.mSpeech = speech;
			this.mSpeakHandler = new Handler();
		}
	}

	/**
	 * @return True if music can be played, and False otherwise.
	 */
	private boolean canPlayMusic()
	{
		NacAlarm alarm = this.getAlarm();

		return (alarm != null) && alarm.hasMedia();
	}

	/**
	 * @return True if the user wants to use to text-to-speech, and False
	 *         otherwise.
	 */
	private boolean canUseTts()
	{
		NacSharedPreferences shared = this.getSharedPreferences();
		return (shared != null) && shared.getSpeakToMe();
	}

	/**
	 * @return True if the phone can be vibrated, and False otherwise.
	 */
	private boolean canVibrate()
	{
		NacAlarm alarm = this.getAlarm();
		return (alarm != null) && alarm.shouldVibrate();
	}

	/**
	 * Cleanup various alarm objects.
	 */
	public void cleanup()
	{
		this.cleanupVibrate();
		this.cleanupPlayer();
		this.cleanupTextToSpeech();
		this.cleanupAutoDismiss();
	}

	/**
	 * Cleanup the auto dismiss handler.
	 */
	private void cleanupAutoDismiss()
	{
		Handler autoDismissHandler = this.getAutoDismissHandler();

		if (autoDismissHandler != null)
		{
			autoDismissHandler.removeCallbacksAndMessages(null);
		}
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
		this.mPlayer = null;
	}

	/**
	 * Cleanup the text-to-speech engine.
	 */
	private void cleanupTextToSpeech()
	{
		NacTextToSpeech speech = this.getTextToSpeech();
		Handler speakHandler = this.getSpeakHandler();

		if (speakHandler != null)
		{
			speakHandler.removeCallbacksAndMessages(null);
		}

		if (speech != null)
		{
			speech.shutdown();
		}

		this.mSpeech = null;
		this.mSpeakHandler = null;
	}

	/**
	 * Cleanup vibrating the phone.
	 */
	private void cleanupVibrate()
	{
		Vibrator vibrator = this.getVibrator();

		if (vibrator != null)
		{
			vibrator.cancel();
		}
	}

	/**
	 * Check if one wakeup process equals another.
	 */
	@SuppressWarnings("unused")
	public boolean equals(NacWakeupProcess process)
	{
		NacAlarm thisAlarm = this.getAlarm();
		NacAlarm procAlarm = process.getAlarm();
		return thisAlarm.equals(procAlarm);
	}

	/**
	 * @return The alarm.
	 */
	public NacAlarm getAlarm()
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
		return this.mAutoDismissListener;
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
		NacSharedConstants cons = new NacSharedConstants(context);
		return cons.getSpeakToMe(context);
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
		this.startNormal();
	}

	/**
	 */
	@Override
	public void onStartSpeaking(NacTextToSpeech tts, NacAudio.Attributes attrs)
	{
		this.cleanupVibrate();
	}

	/**
	 * Play music.
	 */
	private void playMusic()
	{
		NacMediaPlayer player = this.getMediaPlayer();
		if ((player == null) || !this.canPlayMusic())
		{
			return;
		}

		NacSharedPreferences shared = this.getSharedPreferences();
		NacAlarm alarm = this.getAlarm();

		// TODO: Might want to override whats playing when waking up.
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
		this.mAutoDismissListener = listener;
	}

	/**
	 * Set the volume for the music player and text-to-speech engine.
	 */
	private void setVolume()
	{
		NacAlarm alarm = this.getAlarm();
		NacMediaPlayer player = this.getMediaPlayer();

		if (player != null)
		{
			NacAudio.Attributes attrs = player.getAudioAttributes();
			attrs.merge(alarm).setVolume();
		}
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
		if (this.canUseTts() || this.canPlayMusic())
		{
			this.setVolume();
		}

		this.startTts();
		this.waitForAutoDismiss();
	}

	/**
	 * Start the normal wake up process.
	 */
	public void startNormal()
	{
		if (this.canPlayMusic())
		{
			this.playMusic();
		}

		if (this.canVibrate())
		{
			this.vibrate();
		}
	}

	/**
	 * Start the wake up process with TTS, if possible.
	 */
	public void startTts()
	{
		if (this.canUseTts())
		{
			this.speak();
		}
		else
		{
			this.startNormal();
		}
	}

	/**
	 * Vibrate the phone repeatedly until the alarm is dismissed.
	 */
	@SuppressWarnings("deprecation")
	@TargetApi(Build.VERSION_CODES.O)
	public void vibrate()
	{
		if (!this.canVibrate())
		{
			return;
		}

		Vibrator vibrator = this.getVibrator();
		long duration = 500;
		long[] pattern = {0, duration, duration};

		if (vibrator != null)
		{
			vibrator.cancel();

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
		NacSharedPreferences shared = this.getSharedPreferences();
		int autoDismiss = shared.getAutoDismissTime();
		long delay = TimeUnit.MINUTES.toMillis(autoDismiss) - 2000;

		if (autoDismiss != 0)
		{
			this.getAutoDismissHandler().postDelayed(this, delay);
		}
	}

}

package com.nfcalarmclock.activealarm;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.os.VibrationEffect;

import com.nfcalarmclock.alarm.NacAlarm;
import com.nfcalarmclock.media.NacAudioAttributes;
import com.nfcalarmclock.media.NacMediaPlayer;
import com.nfcalarmclock.shared.NacSharedConstants;
import com.nfcalarmclock.shared.NacSharedPreferences;
import com.nfcalarmclock.tts.NacTextToSpeech;

/**
 * Actions to take upon waking up, such as enabling NFC, playing music, etc.
 */
@SuppressWarnings({"RedundantSuppression", "UnnecessaryInterfaceModifier"})
public class NacWakeupProcess
	implements NacTextToSpeech.OnSpeakingListener
{

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

		this.setupVibrator(context);
		this.setupMusicPlayer(context);
		this.setupTextToSpeech(context, alarm);
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
		//NacSharedPreferences shared = this.getSharedPreferences();
		//return (shared != null) && shared.getSpeakToMe();
		NacAlarm alarm = this.getAlarm();
		return (alarm != null) && alarm.shouldUseTts();
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
	}

	/**
	 * Cleanup the media player.
	 */
	private void cleanupPlayer()
	{
		NacMediaPlayer player = this.getMediaPlayer();

		if (player != null)
		{
			player.resetWrapper();
			player.releaseWrapper();
		}

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

		this.mVibrator = null;
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
	 * @return The context.
	 */
	private Context getContext()
	{
		return this.mContext;
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
		NacSharedPreferences shared = this.getSharedPreferences();
		NacSharedConstants cons = shared.getConstants();

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
	public void onDoneSpeaking(NacTextToSpeech tts, NacAudioAttributes attrs)
	{
		this.startNormal();
	}

	/**
	 */
	@Override
	public void onStartSpeaking(NacTextToSpeech tts, NacAudioAttributes attrs)
	{
		Vibrator vibrator = this.getVibrator();

		if (vibrator != null)
		{
			vibrator.cancel();
		}
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
	 * Setup the music player.
	 */
	private void setupMusicPlayer(Context context)
	{
		if (this.canPlayMusic())
		{
			this.mPlayer = new NacMediaPlayer(context);
		}
	}

	/**
	 * Setup the text-to-speech engine.
	 */
	private void setupTextToSpeech(Context context, NacAlarm alarm)
	{
		if (this.canUseTts())
		{
			NacTextToSpeech speech = new NacTextToSpeech(context, this);
			speech.getAudioAttributes().merge(alarm);

			this.mSpeech = speech;
			this.mSpeakHandler = new Handler(Looper.getMainLooper());
		}
	}

	/**
	 * Setup the phone vibrator.
	 */
	private void setupVibrator(Context context)
	{
		if (this.canVibrate())
		{
			this.mVibrator = (Vibrator) context.getSystemService(
				Context.VIBRATOR_SERVICE);
		}
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
			NacAudioAttributes attrs = player.getAudioAttributes();
			attrs.merge(alarm).setVolume();
		}
	}

	/**
	 * Speak at the desired frequency, specified in the shared preference.
	 */
	private void speak()
	{
		NacAlarm alarm = this.getAlarm();
		final long freq = alarm.getTtsFrequency() * 60L * 1000L;
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

}

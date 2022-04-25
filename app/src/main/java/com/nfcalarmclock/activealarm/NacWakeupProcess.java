package com.nfcalarmclock.activealarm;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.os.VibrationEffect;

import com.nfcalarmclock.alarm.NacAlarm;
import com.nfcalarmclock.media.NacAudioAttributes;
import com.nfcalarmclock.media.NacMedia;
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
	 * Vibrate handler, to vibrate the phone at periodic intervals.
	 */
	private Handler mVibrateHandler;

	/**
	 */
	public NacWakeupProcess(Context context, NacAlarm alarm)
	{
		this.mContext = context;
		this.mAlarm = alarm;
		this.mSharedPreferences = new NacSharedPreferences(context);
		this.mVibrateHandler = new Handler(context.getMainLooper());
		// TODO: Can the handler for the media player in onDoneSpeaking() be created
		// here? This way it is only done once?

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
			player.release();
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
		Handler vibrateHandler = this.getVibrateHandler();

		// Stop any current vibrations
		if (vibrator != null)
		{
			vibrator.cancel();
		}

		// Stop any future vibrations from occuring
		if (vibrateHandler != null)
		{
			vibrateHandler.removeCallbacksAndMessages(null);
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
	 * @return The handler to vibrate the phone.
	 */
	private Handler getVibrateHandler()
	{
		return this.mVibrateHandler;
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
		Context context = this.getContext();
		Looper looper = context.getMainLooper();
		Handler handler = new Handler(looper);

		// Need to execute media player operations on the main thread
		handler.post(() -> startNormal());
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

		// Unable to play music
		if ((player == null) || !this.canPlayMusic())
		{
			return;
		}

		// Continue playing what was being played before
		if (player.wasPlaying())
		{
			player.play();
		}
		// TODO: Might want to override whats playing when waking up.
		else
		{
			NacSharedPreferences shared = this.getSharedPreferences();
			NacAlarm alarm = this.getAlarm();

			// Set shuffle mode (can be set or not set) based on the preference
			if (NacMedia.isDirectory(alarm.getMediaType()))
			{
				player.getMediaPlayer().setShuffleModeEnabled(shared.getShuffle());
			}

			// TODO: Maybe call reset by NOT default
			//player.getMediaPlayer().stop();
			player.playAlarm(alarm);
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
			Looper looper = context.getMainLooper();

			speech.getAudioAttributes().merge(alarm);

			this.mSpeech = speech;
			this.mSpeakHandler = new Handler(looper);
		}
	}

	/**
	 * Setup the phone vibrator.
	 */
	@SuppressWarnings("deprecation")
	private void setupVibrator(Context context)
	{
		if (this.canVibrate())
		{
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
			{
				VibratorManager manager = (VibratorManager) context.getSystemService(
					Context.VIBRATOR_MANAGER_SERVICE);
				this.mVibrator = manager.getDefaultVibrator();
			}
			else
			{
				this.mVibrator = (Vibrator) context.getSystemService(
					Context.VIBRATOR_SERVICE);
			}
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

			player.removePlaybackListener();
			attrs.merge(alarm).setVolume();
			player.setPlaybackListener();
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
		Vibrator vibrator = this.getVibrator();
		long duration = 500;
		long[] pattern = {0, duration, 2*duration};
		//long[] pattern = {duration, duration, duration};
		//long[] pattern = {0, duration, duration};

		// Unable to vibrate
		if (!this.canVibrate() || (vibrator == null))
		{
			return;
		}

		// Cancel the previous vibration, if any
		vibrator.cancel();

		// Vibrate
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
		{
			VibrationEffect effect = VibrationEffect.createOneShot(duration,
				VibrationEffect.DEFAULT_AMPLITUDE);
			//VibrationEffect effect = VibrationEffect.createWaveform(pattern, 0);

			vibrator.vibrate(effect);
			//vibrator.vibrate(VibrationEffect.createWaveform(pattern, 0));
		}
		else
		{
			vibrator.vibrate(duration);
			//vibrator.vibrate(pattern, 0);
		}

		// Vibrate the phone after 1 sec
		//Handler handler = this.getVibrateHandler();
		this.getVibrateHandler().postDelayed(() -> vibrate(), 2*duration);
	}

}

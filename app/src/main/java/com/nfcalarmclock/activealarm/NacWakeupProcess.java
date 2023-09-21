package com.nfcalarmclock.activealarm;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.os.VibrationEffect;
import com.google.android.exoplayer2.Player;
import com.nfcalarmclock.alarm.db.NacAlarm;
import com.nfcalarmclock.media.NacAudioAttributes;
import com.nfcalarmclock.media.NacMedia;
import com.nfcalarmclock.media.NacMediaPlayer;
import com.nfcalarmclock.shared.NacSharedConstants;
import com.nfcalarmclock.shared.NacSharedPreferences;
import com.nfcalarmclock.alarm.options.tts.NacTextToSpeech;
import com.nfcalarmclock.util.NacUtility;
import java.lang.SecurityException;

/**
 * Actions to take upon waking up, such as enabling NFC, playing music, etc.
 */
@SuppressWarnings({"RedundantSuppression", "UnnecessaryInterfaceModifier"})
public class NacWakeupProcess
	implements NacTextToSpeech.OnSpeakingListener,
		Player.Listener
{

	/**
	 * The application context.
	 */
	private final Context mContext;

	/**
	 * Shared preferences.
	 */
	private final NacSharedPreferences mSharedPreferences;

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
	 */
	private Vibrator mVibrator;

	/**
	 * The text-to-speech engine.
	 */
	private NacTextToSpeech mSpeech;

	/**
	 * Audio attributes.
	 */
	private NacAudioAttributes mAudioAttributes;

	/**
	 * Vibrate handler, to vibrate the phone at periodic intervals.
	 */
	private final Handler mVibrateHandler;

	/**
	 * Say the current time at user specified intervals.
	 */
	private final Handler mSpeakHandler;

	/**
	 * Gradually increase the volume.
	 */
	private final Handler mGraduallyIncreaseVolumeHandler;

	/**
	 * Volume level to restrict any volume changes to.
	 * <p>
	 * This is not just the alarm volume, since if the user wants to gradually
	 * increase the volume, the restricted volume in that case should be lower
	 * than the alarm volume.
	 */
	private int mVolumeToRestrictChangeTo;

	/**
	 * Flag indicating whether to ignore the next volume change or not.
	 */
	private boolean mIgnoreNextVolumeChange;

	/**
	 */
	public NacWakeupProcess(Context context)
	{
		Looper looper = context.getMainLooper();

		this.mContext = context;
		this.mAlarm = null;
		//this.mAlarm = alarm;
		this.mSharedPreferences = new NacSharedPreferences(context);
		this.mVibrateHandler = new Handler(looper);
		this.mSpeakHandler = new Handler(looper);
		this.mGraduallyIncreaseVolumeHandler = new Handler(looper);
		this.mIgnoreNextVolumeChange = false;
		this.mVolumeToRestrictChangeTo = -1;

		// TODO: Can the handler for the media player in onDoneSpeaking() be created
		// here? This way it is only done once?
	}

	/**
	 * Cleanup various alarm objects.
	 */
	public void cleanup()
	{
		this.cleanupVibrate();
		this.cleanupPlayer();
		this.cleanupTextToSpeech();
		this.cleanupGraduallyIncreaseVolume();
	}

	/**
	 * Cleanup gradually increasing the volume.
	 */
	private void cleanupGraduallyIncreaseVolume()
	{
		Handler handler = this.getGraduallyIncreaseVolumeHandler();

		// Stop the volume from gradually increasing
		if (handler != null)
		{
			handler.removeCallbacksAndMessages(null);
		}
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

		//this.mPlayer = null;
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
	 * @return The audio attributes.
	 */
	public NacAudioAttributes getAudioAttributes()
	{
		return this.mAudioAttributes;
	}

	/**
	 * @return The context.
	 */
	private Context getContext()
	{
		return this.mContext;
	}

	/**
	 * @return The handler to gradually increase the volume.
	 */
	private Handler getGraduallyIncreaseVolumeHandler()
	{
		return this.mGraduallyIncreaseVolumeHandler;
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
	 * @return The volume to restrict any changes to.
	 */
	private int getVolumeToRestrictChangeTo()
	{
		return this.mVolumeToRestrictChangeTo;
	}

	/**
	 * Gradually increase the volume.
	 */
	private void graduallyIncreaseVolume()
	{
		Context context = this.getContext();
		NacAlarm alarm = this.getAlarm();
		NacAudioAttributes attrs = this.getAudioAttributes();

		int currentVolume = attrs.getStreamVolume();
		int alarmVolume = attrs.toStreamVolume();
		int newVolume = currentVolume + 1;

		// Do not change the volume. It is already at the alarm volume or greater
		if (currentVolume >= alarmVolume)
		{
			return;
		}

		// Gradually increase the volume by one step
		this.setIgnoreNextVolumeChange(true);
		this.setVolumeToRestrictChangeTo(newVolume);
		attrs.setStreamVolume(newVolume);

		// Wait for a period of time before increasing the volume again
		Handler handler = this.getGraduallyIncreaseVolumeHandler();
		handler.postDelayed(this::graduallyIncreaseVolume, 5000);
	}

	/**
	 * Called when the device volume is changed.
	 */
	@Override
	public void onDeviceVolumeChanged(int volume, boolean muted)
	{
		NacAlarm alarm = this.getAlarm();

		// Do not handle the volume change. Alarm is not set yet, or volume does not
		// need to be restricted, or this volume change can be ignored.
		if ((alarm == null) || !alarm.getShouldRestrictVolume()
			|| this.shouldIgnoreNextVolumeChange())
		{
			this.setIgnoreNextVolumeChange(false);
			return;
		}

		// Restrict changing the volume
		// TODO: setDeviceMuted() in case muted=True?
		NacMediaPlayer player = this.getMediaPlayer();
		int restrictVolume = this.getVolumeToRestrictChangeTo();

		try
		{
			player.getMediaPlayer().setDeviceVolume(restrictVolume);
		}
		catch (SecurityException e)
		{
			Context context = this.getContext();
			NacSharedPreferences shared = this.getSharedPreferences();
			NacSharedConstants cons = shared.getConstants();

			// Show a toast indicating that the volume could not be restricted
			NacUtility.quickToast(context, cons.getErrorMessageRestrictVolumeChange());
		}
	}

	/**
	 */
	@Override
	public void onDoneSpeaking(NacTextToSpeech tts)
	{
		Context context = this.getContext();
		Looper looper = context.getMainLooper();
		Handler handler = new Handler(looper);

		// Need to execute media player operations on the main thread
		handler.post(this::start);
	}

	/**
	 * Called when the text-to-speech engine has started.
	 */
	@Override
	public void onStartSpeaking(NacTextToSpeech tts)
	{
		// Stop any vibration when TTS is playing
		this.cleanupVibrate();
	}

	/**
	 * Play music.
	 */
	private void playMusic()
	{
		NacMediaPlayer player = this.getMediaPlayer();
		NacAlarm alarm = this.getAlarm();

		// Unable to play music
		if ((player == null) || (alarm == null) || !alarm.hasMedia())
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
	 * Set the alarm to use during wakeup.
	 *
	 * @param  alarm  The alarm.
	 */
	private void setAlarm(NacAlarm alarm)
	{
		this.mAlarm = alarm;
	}

	/**
	 * Set whether the next volume change should be ignored or not.
	 *
	 * @param  ignore  Whether or not to ignore the next volume change.
	 */
	private void setIgnoreNextVolumeChange(boolean ignore)
	{
		this.mIgnoreNextVolumeChange = ignore;
	}

	/**
	 * Set the volume for the music player and text-to-speech engine.
	 */
	private void setVolume()
	{
		NacAlarm alarm = this.getAlarm();
		NacMediaPlayer player = this.getMediaPlayer();

		// Unable to set volume. Alarm is not set yet or player is not setup yet.
		if ((alarm == null) || (player == null))
		{
			return;
		}

		// Get the audio attributes
		// TODO: Update the player and its audio attributes
		//NacAudioAttributes attrs = player.getAudioAttributes();
		NacAudioAttributes attrs = this.getAudioAttributes();

		// Set the volume to the alarm volume
		this.setIgnoreNextVolumeChange(true);
		attrs.setVolume();
		//attrs.merge(alarm).setVolume();

		// Set the volume to restrict to, if any changes occur
		if (alarm.getShouldRestrictVolume())
		{
			this.setVolumeToRestrictChangeTo(attrs.getStreamVolume());
		}
	}

	/**
	 * Set the volume to restrict any changes to.
	 *
	 * @param  volume  The volume level.
	 */
	private void setVolumeToRestrictChangeTo(int volume)
	{
		this.mVolumeToRestrictChangeTo = volume;
	}

	/**
	 * Setup the audio attributes.
	 */
	private void setupAudioAttributes()
	{
		Context context = this.getContext();
		NacAlarm alarm = this.getAlarm();

		// Unablet to setup the audio attributes. The alarm is not set yet
		if (alarm == null)
		{
			return;
		}

		// Create the audio attributes object
		this.mAudioAttributes = new NacAudioAttributes(context, alarm);
	}

	/**
	 * Setup gradually increasing the volume.
	 */
	private void setupGraduallyIncreaseVolume()
	{
		Handler handler = this.getGraduallyIncreaseVolumeHandler();
		NacAudioAttributes attrs = this.getAudioAttributes();
		long freq = 5000;

		// Set the volume to 0 to start with
		this.setIgnoreNextVolumeChange(true);
		this.setVolumeToRestrictChangeTo(0);
		attrs.setStreamVolume(0);

		// Periodically increase the volume
		handler.postDelayed(this::graduallyIncreaseVolume, freq);
	}

	/**
	 * Setup the music player.
	 */
	private void setupMusicPlayer()
	{
		Context context = this.getContext();
		NacAlarm alarm = this.getAlarm();

		// Alarm is not set yet or does not have media to play. Unable to setup music
		// player
		if ((alarm == null) || !alarm.hasMedia())
		{
			return;
		}

		// Create the media player
		NacMediaPlayer player = new NacMediaPlayer(context);

		// Set the listener for any changes. Only the volume change method is
		// handled
		player.getMediaPlayer().addListener(this);

		// Set the member variable
		this.mPlayer = player;
	}

	/**
	 * Setup the text-to-speech engine.
	 */
	private void setupTextToSpeech()
	{
		Context context = this.getContext();
		NacAlarm alarm = this.getAlarm();

		// Unable to setup the text-to-speech engine. Alarm is not set yet, or should
		// not use TTS
		if ((alarm == null) || !alarm.shouldUseTts())
		{
			return;
		}

		// Create the TTS engine
		this.mSpeech = new NacTextToSpeech(context, this);
	}

	/**
	 * Setup the phone vibrator.
	 */
	@SuppressWarnings("deprecation")
	private void setupVibrator()
	{
		Context context = this.getContext();
		NacAlarm alarm = this.getAlarm();

		// Unable to setup the vibrator. Alarm is not set yet or should not vibrate
		if ((alarm == null) || !alarm.shouldVibrate())
		{
			return;
		}

		// Setup the vibrator
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

	/**
	 * @return True if the next volume change should be ignored, and False
	 *         otherwise.
	 */
	private boolean shouldIgnoreNextVolumeChange()
	{
		return this.mIgnoreNextVolumeChange;
	}

	/**
	 * Speak at the desired frequency, specified in the shared preference.
	 */
	private void speak()
	{
		NacTextToSpeech speech = this.getTextToSpeech();
		NacAlarm alarm = this.getAlarm();

		// Unable to speak via TTS. The engine is not set yet, or is already
		// speaking, or there is something in the buffer, or the alarm is not set
		// yet, or the alarm should not use TTS
		if ((speech == null) || speech.isSpeaking() || speech.hasBuffer()
			|| (alarm == null) || !alarm.shouldUseTts())
		{
			return;
		}

		// Speak via TTS
		NacAudioAttributes attrs = getAudioAttributes();
		String text = getTimeToSay();

		speech.speak(text, attrs);

		// Wait for some period of time before speaking through TTS again
		Handler handler = this.getSpeakHandler();
		long freq = alarm.getTtsFrequencyMillis();

		if (freq != 0)
		{
			handler.postDelayed(this::speak, freq);
		}
	}

	/**
	 * Start the wake up process.
	 *
	 * @param  alarm  The alarm to use during wake up.
	 */
	public void start(NacAlarm alarm)
	{
		// Unable to start the wakeup process. Alarm is not set
		if (alarm == null)
		{
			return;
		}

		// Set the alarm
		this.setAlarm(alarm);

		// Setup the different services needed during wakeup
		this.setupAudioAttributes();
		this.setupMusicPlayer();
		this.setupTextToSpeech();
		this.setupVibrator();

		// Set the volume (if going to use text-to-speech or play music)
		// TODO: Make this into a method?
		if (alarm.shouldUseTts() || alarm.hasMedia())
		{
			// Start to gradually increase the alarm volume
			if (alarm.getShouldGraduallyIncreaseVolume())
			{
				this.setupGraduallyIncreaseVolume();
			}
			// Set the alarm volume
			else
			{
				this.setVolume();
			}
		}

		// Start text-to-speech
		if (alarm.shouldUseTts())
		{
			this.speak();
		}
		// Start the normal wake up process
		else
		{
			this.start();
		}
	}

	/**
	 * Start the normal wake up process.
	 */
	public void start()
	{
		NacAlarm alarm = this.getAlarm();

		// Unable to start the wakeup process. Alarm is not set yet
		if (alarm == null)
		{
			return;
		}

		// Play music
		if (alarm.hasMedia())
		{
			this.playMusic();
		}

		// Vibrate the phone
		if (alarm.shouldVibrate())
		{
			this.vibrate();
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
		NacAlarm alarm = this.getAlarm();
		long duration = 500;
		long waitTime = 1000 + duration;

		// Unable to vibrate. Vibrator is not set yet, or alarm is not set yet, or
		// alarm should not vibrate
		if ((vibrator == null) || (alarm == null) || !alarm.shouldVibrate())
		{
			return;
		}

		// Cancel the previous vibration, if any
		this.cleanupVibrate();

		// Vibrate
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
		{
			VibrationEffect effect = VibrationEffect.createOneShot(duration,
				VibrationEffect.DEFAULT_AMPLITUDE);

			vibrator.vibrate(effect);
		}
		else
		{
			vibrator.vibrate(duration);
		}

		// Wait for a period of time before vibrating the phone again
		Handler handler = this.getVibrateHandler();
		handler.postDelayed(this::vibrate, waitTime);
	}

}

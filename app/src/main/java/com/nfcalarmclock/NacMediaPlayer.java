package com.nfcalarmclock;

import android.content.Context;
import android.database.Cursor;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.MediaStore;
import java.io.IOException;

import android.media.AudioDeviceInfo;
import android.os.Build;
import android.media.AudioFocusRequest;

/**
 * Wrapper for the MediaPlayer class.
 */
public class NacMediaPlayer
	extends MediaPlayer
	implements MediaPlayer.OnCompletionListener,
		AudioManager.OnAudioFocusChangeListener
{

	/**
	 * Application context.
	 */
	private Context mContext;

	/**
	 * Audio manager to request/abandon audio focus.
	 */
	private AudioManager mAudioManager;

	/**
	 * Stream volume.
	 */
	private int mVolume;

	/**
	 * Result values.
	 */
	public static final int RESULT_SUCCESS = 0;
	public static final int RESULT_ILLEGAL_ARGUMENT_EXCEPTION = -1;
	public static final int RESULT_ILLEGAL_STATE_EXCEPTION = -2;
	public static final int RESULT_IO_EXCEPTION = -3;
	public static final int RESULT_SECURITY_EXCEPTION = -4;

	/**
	 * Set the context.
	 */
	public NacMediaPlayer(Context context)
	{
		super();

		this.mContext = context;
		this.mAudioManager = null;
		this.mVolume = 0;
	}

	/**
	 * Abandon audio focus.
	 */
	@SuppressWarnings("deprecation")
	public int abandonAudioFocus()
	{
		AudioManager am = this.getAudioManager();
		int result = 0;

		//if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
		//{
		//	AudioFocusRequest request = this.getAudioFocusRequest();
		//	result = am.abandonAudioFocus(request);
		//}
		//else
		//{
			result = am.abandonAudioFocus(this);
		//}

		return result;
	}

	/**
	 * @return The audio attributes.
	 */
	private AudioAttributes getAudioAttributes()
	{
		return new AudioAttributes.Builder()
			.setLegacyStreamType(AudioManager.STREAM_MUSIC)
			.setUsage(AudioAttributes.USAGE_MEDIA)
			.build();
	}

	/**
	 * @return The audio focus request.
	 */
	private AudioFocusRequest getAudioFocusRequest()
	{
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
		{
			AudioAttributes attrs = this.getAudioAttributes();

			return new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
				.setAudioAttributes(attrs)
				.setOnAudioFocusChangeListener(this)
				.build();
		}
		else
		{
			return null;
		}
	}

	/**
	 * @return The audio manager.
	 */
	private AudioManager getAudioManager()
	{
		return this.mAudioManager;
	}

	/**
	 * @return The context.
	 */
	private Context getContext()
	{
		return this.mContext;
	}

	/**
	 * @return The current stream volume.
	 */
	private int getStreamVolume()
	{
		return this.mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
	}

	/**
	 * Change media state when audio focus changes.
	 */
	@Override
	public void onAudioFocusChange(int focusChange)
	{
		String change = "UNKOWN";

		if (focusChange == AudioManager.AUDIOFOCUS_GAIN)
		{
			change = "GAIN";
			try
			{
				setVolume(this.mVolume, this.mVolume);
			}
			catch (IllegalStateException e)
			{
				NacUtility.printf("Unable to set volume on audio gain.");
			}
			this.startWrapper();
		}
		else if (focusChange == AudioManager.AUDIOFOCUS_LOSS)
		{
			change = "LOSS";
			this.stopWrapper();
		}
		else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT)
		{
			change = "LOSS_TRANSIENT";
			this.pauseWrapper();
		}
		else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK)
		{
			change = "LOSS_TRANSIENT_CAN_DUCK";
			try
			{
				this.mVolume = this.getStreamVolume();
				setVolume(this.mVolume/2, this.mVolume/2);
			}
			catch (IllegalStateException e)
			{
				NacUtility.printf("Unable to set volume on audio loss transient can duck.");
			}
		}

		NacUtility.printf("NacMediaPlayer : onAudioFocusChange : AUDIOFOCUS_%s",
			change);
	}

	@Override
	public void onCompletion(MediaPlayer mp)
	{
		this.stopWrapper();

		if (!isLooping())
		{
			this.resetWrapper();
			this.abandonAudioFocus();
		}
		else
		{
			this.prepareWrapper();
			this.startWrapper();
		}
	}

	/**
	 * Pause the media player.
	 */
	private int pauseWrapper()
	{
		try
		{
			pause();
			return this.RESULT_SUCCESS;
		}
		catch (IllegalStateException e)
		{
			NacUtility.printf("NacMediaPlayer : IllegalStateException caught in pause()");
			return this.RESULT_ILLEGAL_STATE_EXCEPTION;
		}
	}

	/**
	 * @see play
	 */
	public void play(String media)
	{
		this.play(media, false);
	}

	/**
	 * @see play
	 */
	public void play(String media, boolean loop)
	{
		if (media.isEmpty())
		{
			return;
		}

		if(!this.requestAudioFocus())
		{
			NacUtility.printf("Audio Focus NOT Granted!");
			return;
		}

		Context context = this.getContext();
		AudioAttributes attrs = this.getAudioAttributes();
		String path = NacMedia.getMediaPath(context, media);

		// Can log each step for better granularity in case error occurrs.
		try
		{
			if (isPlaying())
			{
				reset();
			}

			setDataSource(path);
			setLooping(loop);
			setAudioAttributes(attrs);
			setOnCompletionListener(this);
			prepare();
			start();
		}
		catch (IllegalStateException | IOException | IllegalArgumentException | SecurityException e)
		{
			NacUtility.quickToast(context, "Unable to play selected file");
		}
	}

	/**
	 * Prepare the media player
	 */
	private int prepareWrapper()
	{
		try
		{
			prepare();
			return this.RESULT_SUCCESS;
		}
		catch (IllegalStateException e)
		{
			NacUtility.printf("NacMediaPlayer : IllegalStateException caught in prepare()");
			return this.RESULT_ILLEGAL_STATE_EXCEPTION;
		}
		catch (IOException e)
		{
			NacUtility.printf("NacMediaPlayer : IOException caught in prepare()");
			return this.RESULT_IO_EXCEPTION;
		}
	}

	/**
	 * Request audio focus.
	 */
	@SuppressWarnings("deprecation")
	public boolean requestAudioFocus()
	{
		Context context = this.getContext();
		AudioManager am = (AudioManager) context.getSystemService(
			Context.AUDIO_SERVICE);
		int result = 0;

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
		{
			AudioFocusRequest request = this.getAudioFocusRequest();
			result = am.requestAudioFocus(request);
		}
		else
		{
			result = am.requestAudioFocus(this, AudioManager.STREAM_MUSIC,
				AudioManager.AUDIOFOCUS_GAIN);
		}

		this.mAudioManager = am;
		this.mVolume = this.getStreamVolume();

		return (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED);
	}

	/**
	 * Reset the media player.
	 */
	private int resetWrapper()
	{
		try
		{
			reset();
			return this.RESULT_SUCCESS;
		}
		catch (IllegalStateException e)
		{
			NacUtility.printf("NacMediaPlayer : IllegalStateException caught in reset()");
			return this.RESULT_ILLEGAL_STATE_EXCEPTION;
		}
	}

	/**
	 * Start the media player.
	 */
	private int startWrapper()
	{
		try
		{
			start();
			return this.RESULT_SUCCESS;
		}
		catch (IllegalStateException e)
		{
			NacUtility.printf("NacMediaPlayer : IllegalStateException caught in start()");
			return this.RESULT_ILLEGAL_STATE_EXCEPTION;
		}
	}

	/**
	 * Stop the media player
	 */
	public int stopWrapper()
	{
		try
		{
			stop();
			return this.RESULT_SUCCESS;
		}
		catch (IllegalStateException e)
		{
			NacUtility.printf("NacMediaPlayer : IllegalStateException caught in stop()");
			return this.RESULT_ILLEGAL_STATE_EXCEPTION;
		}
	}

}

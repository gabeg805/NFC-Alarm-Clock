package com.nfcalarmclock;

import android.content.Context;
import android.database.Cursor;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.MediaStore;
import java.io.IOException;

/**
 * Wrapper for the MediaPlayer class.
 */
public class NacMediaPlayer
	extends MediaPlayer
	implements MediaPlayer.OnCompletionListener,AudioManager.OnAudioFocusChangeListener
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
	public void abandonAudioFocus()
	{
		AudioManager am = this.getAudioManager();

		am.abandonAudioFocus(this);
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
	 * @return The path to the ringtone/music file.
	 *
	 * @param  media  The path to the media file.
	 */
	public String getMediaPath(String media)
	{
		if (!media.startsWith("content://"))
		{
			return media;
		}

		Context context = this.getContext();
		Uri uri = Uri.parse(media);
		Cursor cursor = context.getContentResolver().query(uri,
			new String[] { MediaStore.Audio.Media.DATA }, null, null, null);
		cursor.moveToFirst();

		String path = cursor.getString(cursor.getColumnIndexOrThrow(
			MediaStore.Audio.Media.DATA));

		cursor.close();

		return path;
	}

	/**
	 * @return The current stream volume.
	 */
	private int getStreamVolume()
	{
		return this.mAudioManager.getStreamVolume(AudioManager.STREAM_ALARM);
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
			this.setVolume(this.mVolume, this.mVolume);
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
			this.mVolume = this.getStreamVolume();
			this.setVolume(this.mVolume/2, this.mVolume/2);
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
		//this.play(media, true);
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

		String path = this.getMediaPath(media);
		AudioAttributes attrs = new AudioAttributes.Builder()
			.setLegacyStreamType(AudioManager.STREAM_ALARM)
			.setUsage(AudioAttributes.USAGE_ALARM)
			.build();

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
			NacUtility.quickToast(this.getContext(),
				"Unable to play selected file");
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
	public boolean requestAudioFocus()
	{
		Context context = this.getContext();
		AudioManager am = (AudioManager) context.getSystemService(
			Context.AUDIO_SERVICE);
		int result = am.requestAudioFocus(this, AudioManager.STREAM_ALARM,
			AudioManager.AUDIOFOCUS_GAIN);
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

package com.nfcalarmclock;

import android.content.Context;
import android.database.Cursor;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.MediaStore;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
	 * Playlist object.
	 */
	public static class Playlist
	{

		/**
		 * List of music files.
		 */
		private List<NacSound> mPlaylist;

		/**
		 * Index corresponding to the current place in the playlist.
		 */
		private int mIndex;

		/**
		 * Repeat the playlist.
		 */
		private boolean mRepeat;

		/**
		 */
		public Playlist(Context context, String path)
		{
			this(context, path, false, false);
		}

		/**
		 */
		public Playlist(Context context, String path, boolean repeat,
			boolean shuffle)
		{
			this.mPlaylist = NacSound.getFiles(context, path);
			this.mIndex = 0;
			this.mRepeat = repeat;

			if (shuffle)
			{
				this.shuffle();
			}
		}

		/**
		 * @return The playlist index.
		 */
		public int getIndex()
		{
			return this.mIndex;
		}

		/**
		 * @return The next playlist track.
		 */
		public NacSound getNextTrack()
		{
			int size = this.getSize();
			int index = this.getIndex();
			int nextIndex = this.repeat() ? (index+1) % size : index+1;

			if ((size == 0) || (nextIndex >= size))
			{
				return null;
			}

			this.setIndex(nextIndex);

			return this.getTrack();
		}

		/**
		 * @return The playlist.
		 */
		public List<NacSound> getPlaylist()
		{
			return this.mPlaylist;
		}

		/**
		 * @return The size of the playlist.
		 */
		public int getSize()
		{
			List<NacSound> playlist = this.getPlaylist();

			return (playlist == null) ? 0 : playlist.size();
		}

		/**
		 * @return The current playlist track.
		 */
		public NacSound getTrack()
		{
			return this.getTrack(this.getIndex());
		}

		/**
		 * @return The playlist track.
		 */
		public NacSound getTrack(int index)
		{
			List<NacSound> playlist = this.getPlaylist();
			int size = this.getSize();

			return ((size > 0) && (index < size)) ? playlist.get(index) : null;
		}

		/**
		 * @return True if the playlist should be repeated and False otherwise.
		 */
		public boolean repeat()
		{
			return this.mRepeat;
		}

		/**
		 * Set the playlist index.
		 */
		public void setIndex(int index)
		{
			this.mIndex = index;
		}

		/**
		 * @return True if the playlist is shuffled and False otherwise.
		 */
		public void shuffle()
		{
			Collections.shuffle(this.mPlaylist);
		}

	}

	/**
	 * Application context.
	 */
	private Context mContext;

	/**
	 * Audio manager to request/abandon audio focus.
	 */
	private AudioManager mAudioManager;

	/**
	 * Playlist container.
	 */
	private Playlist mPlaylist;

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
		this.mPlaylist = null;
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
	 * @return The playlist.
	 */
	private Playlist getPlaylist()
	{
		return this.mPlaylist;
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
		NacUtility.printf("onAudioFocusChange! %d", focusChange);
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

	/**
	 */
	@Override
	public void onCompletion(MediaPlayer mp)
	{
		this.stopWrapper();

		Playlist playlist = this.getPlaylist();
		NacSound track = (playlist != null) ? playlist.getNextTrack() : null;

		if (track != null)
		{
			this.resetWrapper();
			this.play(track, playlist.repeat());
			return;
		}
		else
		{
			this.mPlaylist = null;
		}

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
	public void play(NacSound sound)
	{
		this.play(sound, false);
	}

	/**
	 * @see play
	 */
	public void play(NacSound sound, boolean repeat)
	{
		this.play(sound.getPath(), repeat);
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
	public void play(String media, boolean repeat)
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
		String path = NacSound.getPath(context, media);

		// Can log each step for better granularity in case error occurrs.
		try
		{
			if (isPlaying())
			{
				reset();
			}

			setDataSource(path);
			setLooping(repeat);
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
	 * Play a playlist.
	 */
	public void playPlaylist(String path, boolean repeat, boolean shuffle)
	{
		Context context = this.getContext();
		this.mPlaylist = new Playlist(context, path, repeat, shuffle);
		Playlist playlist = this.getPlaylist();
		NacSound track = playlist.getTrack();

		if (track != null)
		{
			this.play(track, repeat);
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

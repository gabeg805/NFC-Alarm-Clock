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
	 * Playlist container.
	 */
	private Playlist mPlaylist;

	/**
	 * Audio focus state.
	 */
	private int mAudioFocusState;

	/**
	 * Check if player was playing (caused by losing audio focus).
	 */
	private boolean mWasPlaying;

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
		this.mPlaylist = null;
		this.mAudioFocusState = 0;
		this.mWasPlaying = false;
	}

	/**
	 * Abandon audio focus.
	 */
	@SuppressWarnings("deprecation")
	public static int abandonAudioFocus(Context context,
		AudioManager.OnAudioFocusChangeListener listener)
	{
		AudioManager am = NacMediaPlayer.getAudioManager(context);
		int result = 0;

		//if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
		//{
		//	AudioFocusRequest request = this.getAudioFocusRequest();
		//	result = am.abandonAudioFocus(request);
		//}
		//else
		//{
			result = am.abandonAudioFocus(listener);
		//}

		return result;
	}

	/**
	 * @return The audio attributes.
	 */
	public static AudioAttributes getAudioAttributes()
	{
		return new AudioAttributes.Builder()
			.setLegacyStreamType(AudioManager.STREAM_MUSIC)
			.setUsage(AudioAttributes.USAGE_MEDIA)
			.build();
	}

	/**
	 * @return The audio focus request.
	 */
	public static AudioFocusRequest getAudioFocusRequest(
		AudioManager.OnAudioFocusChangeListener listener, int focus)
	{
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
		{
			AudioAttributes attrs = NacMediaPlayer.getAudioAttributes();

			return new AudioFocusRequest.Builder(focus)
				.setAudioAttributes(attrs)
				.setOnAudioFocusChangeListener(listener)
				.build();
		}
		else
		{
			return null;
		}
	}

	/**
	 * @return The audio focus state.
	 */
	private int getAudioFocusState()
	{
		return this.mAudioFocusState;
	}

	/**
	 * @see getAudioManager
	 */
	public AudioManager getAudioManager()
	{
		Context context = this.getContext();

		return NacMediaPlayer.getAudioManager(context);
	}

	/**
	 * @return The audio manager.
	 */
	public static AudioManager getAudioManager(Context context)
	{
		return (AudioManager) context.getSystemService(
			Context.AUDIO_SERVICE);
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
		return this.getAudioManager().getStreamVolume(
			AudioManager.STREAM_MUSIC);
	}

	/**
	 * Check if the media player is playing.
	 */
	public boolean isPlayingWrapper()
	{
		try
		{
			return isPlaying();
		}
		catch (IllegalStateException e)
		{
			NacUtility.printf("NacMediaPlayer : IllegalStateException caught in isPlaying()");
			return false;
		}
	}

	/**
	 * Change media state when audio focus changes.
	 */
	@Override
	public void onAudioFocusChange(int focusChange)
	{
		this.zPrintInfo(focusChange);
		this.mAudioFocusState = focusChange;
		this.mWasPlaying = this.isPlayingWrapper();

		if (focusChange == AudioManager.AUDIOFOCUS_GAIN)
		{
			this.mWasPlaying = true;
			this.setVolume();
			this.startWrapper();
		}
		else if (focusChange == AudioManager.AUDIOFOCUS_LOSS)
		{
			this.stopWrapper();
		}
		else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT)
		{
			this.pauseWrapper();
		}
		else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK)
		{
			this.setVolumeDucking();
		}
	}

	/**
	 */
	@Override
	public void onCompletion(MediaPlayer mp)
	{
		this.stopWrapper();

		Context context = this.getContext();
		Playlist playlist = this.getPlaylist();
		NacSound track = (playlist != null) ? playlist.getNextTrack() : null;

		if (track != null)
		{
			this.resetWrapper();
			this.play(track);
			return;
		}
		else
		{
			this.mPlaylist = null;
		}

		if (!isLooping())
		{
			this.resetWrapper();
			NacMediaPlayer.abandonAudioFocus(context, this);
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
		Context context = this.getContext();

		if (media.isEmpty())
		{
			return;
		}

		if(!NacMediaPlayer.requestAudioFocus(context, this))
		{
			NacUtility.printf("Audio Focus NOT Granted!");
			return;
		}

		AudioAttributes attrs = NacMediaPlayer.getAudioAttributes();
		String path = NacSound.getPath(context, media);

		// Can log each step for better granularity in case errors occur.
		try
		{
			if (this.isPlayingWrapper())
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
			this.play(track);
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
	 * @see requestAudioFocus
	 */
	public static boolean requestAudioFocus(Context context,
		AudioManager.OnAudioFocusChangeListener listener)
	{
		return NacMediaPlayer.requestAudioFocus(context, listener,
			AudioManager.AUDIOFOCUS_GAIN);
	}

	/**
	 * @see requestAudioFocus
	 */
	public static boolean requestAudioFocusTransient(Context context,
		AudioManager.OnAudioFocusChangeListener listener)
	{
		return NacMediaPlayer.requestAudioFocus(context, listener,
			AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
	}

	/**
	 * Request audio focus.
	 */
	@SuppressWarnings("deprecation")
	public static boolean requestAudioFocus(Context context,
		AudioManager.OnAudioFocusChangeListener listener, int focus)
	{
		AudioManager am = NacMediaPlayer.getAudioManager(context);
		int result = 0;

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
		{
			AudioFocusRequest request = NacMediaPlayer
				.getAudioFocusRequest(listener, focus);
			result = am.requestAudioFocus(request);
		}
		else
		{
			result = am.requestAudioFocus(listener, AudioManager.STREAM_MUSIC,
				focus);
		}

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
	 * Set the volume to it's original level.
	 */
	private void setVolume()
	{
		int volume = this.getStreamVolume();

		if (this.getAudioFocusState() ==
			AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK)
		{
			volume *= 2;
		}

		this.setVolume(volume);
	}

	/**
	 * Set the volume.
	 */
	private void setVolume(int level)
	{
		try
		{
			setVolume(level, level);
		}
		catch (IllegalStateException e)
		{
			NacUtility.printf("Unable to set volume : %d", level);
		}
	}

	/**
	 * Set the volume when ducking.
	 */
	private void setVolumeDucking()
	{
		int volume = this.getStreamVolume();

		this.setVolume(volume/2);
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

	/**
	 * @return True if the player was playing before losing audio focus, and
	 *         False otherwise.
	 */
	public boolean wasPlaying()
	{
		return this.mWasPlaying;
	}

	// Temp method
	private void zPrintInfo(int focusChange)
	{
		NacUtility.printf("onAudioFocusChange! %d", focusChange);
		String change = "UNKOWN";

		if (focusChange == AudioManager.AUDIOFOCUS_GAIN)
		{
			change = "GAIN";
		}
		else if (focusChange == AudioManager.AUDIOFOCUS_LOSS)
		{
			change = "LOSS";
		}
		else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT)
		{
			change = "LOSS_TRANSIENT";
		}
		else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK)
		{
			change = "LOSS_TRANSIENT_CAN_DUCK";
		}

		NacUtility.printf("NacMediaPlayer : onAudioFocusChange : AUDIOFOCUS_%s",
			change);
	}

}

package com.nfcalarmclock.audio.media;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;

import com.nfcalarmclock.audio.NacAudio;
import com.nfcalarmclock.NacUtility;
import com.nfcalarmclock.alarm.NacAlarm;
import com.nfcalarmclock.shared.NacSharedConstants;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Wrapper for the MediaPlayer class.
 */
@SuppressWarnings({"RedundantSuppression", "UnusedReturnValue"})
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
		private final List<Uri> mPlaylist;

		/**
		 * Index corresponding to the current place in the playlist.
		 */
		private int mIndex;

		/**
		 * Repeat the playlist.
		 */
		private final boolean mRepeat;

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
			this.mPlaylist = NacMedia.getFiles(context, path);
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
		public Uri getNextTrack()
		{
			int size = this.getSize();
			int index = this.getIndex();
			boolean repeat = this.getRepeat();
			int nextIndex = repeat ? (index+1) % size : index+1;

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
		public List<Uri> getPlaylist()
		{
			return this.mPlaylist;
		}

		/**
		 * @return True if the playlist should be repeated, and False otherwise.
		 */
		public boolean getRepeat()
		{
			return this.mRepeat;
		}

		/**
		 * @return The size of the playlist.
		 */
		public int getSize()
		{
			List<Uri> playlist = this.getPlaylist();
			return (playlist == null) ? 0 : playlist.size();
		}

		/**
		 * @return The current playlist track.
		 */
		public Uri getTrack()
		{
			return this.getTrack(this.getIndex());
		}

		/**
		 * @return The playlist track.
		 */
		public Uri getTrack(int index)
		{
			List<Uri> playlist = this.getPlaylist();
			int size = this.getSize();

			return ((size > 0) && (index < size)) ? playlist.get(index) : null;
		}

		/**
		 * Set the playlist index.
		 */
		public void setIndex(int index)
		{
			this.mIndex = index;
		}

		/**
		 * Shuffle the playlist list.
		 */
		public void shuffle()
		{
			Collections.shuffle(this.mPlaylist);
		}

	}

	/**
	 * Application context.
	 */
	private final Context mContext;

	/**
	 * Playlist container.
	 */
	private Playlist mPlaylist;

	/**
	 * Audio attributes.
	 */
	private final NacAudio.Attributes mAttributes;

	/**
	 * Handler to add some delay if looping media.
	 */
	private final Handler mHandler;

	/**
	 * Check if player was playing (caused by losing audio focus).
	 */
	private boolean mWasPlaying;

	/**
	 * Result values.
	 */
	public static final int RESULT_SUCCESS = 0;
	public static final int RESULT_ILLEGAL_STATE_EXCEPTION = -2;
	public static final int RESULT_IO_EXCEPTION = -3;

	/**
	 */
	public NacMediaPlayer(Context context)
	{
		super();

		this.mContext = context;
		this.mPlaylist = null;
		this.mAttributes = new NacAudio.Attributes(context);
		this.mHandler = new Handler();
		this.mWasPlaying = false;
	}

	/**
	 */
	public NacMediaPlayer(Context context, int focus)
	{
		this(context);
		this.getAudioAttributes().setFocus(focus);
	}

	/**
	 * Abandon audio focus.
	 */
	public void abandonAudioFocus()
	{
		Context context = this.getContext();
		NacAudio.abandonAudioFocus(context, this);
	}

	/**
	 * Cleanup the handler.
	 */
	private void cleanupHandler()
	{
		Handler handler = this.getHandler();

		if (handler != null)
		{
			handler.removeCallbacksAndMessages(null);
		}
	}

	/**
	 * @return The audio attributes.
	 */
	public NacAudio.Attributes getAudioAttributes()
	{
		return this.mAttributes;
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
	 * @return The playlist.
	 */
	private Playlist getPlaylist()
	{
		return this.mPlaylist;
	}

	/**
	 * @return True if a playlist has been created, and False otherwise.
	 */
	public boolean hasPlaylist()
	{
		return (this.getPlaylist() != null);
	}

	/**
	 * @return True if the media player is playing, and False otherwise.
	 */
	public boolean isPlayingWrapper()
	{
		try
		{
			return isPlaying();
		}
		catch (IllegalStateException e)
		{
			NacUtility.printf("NacMediaPlayer : IllegalStateException : isPlaying()");
			return false;
		}
	}

	/**
	 * Change media state when audio focus changes.
	 */
	@Override
	public void onAudioFocusChange(int focusChange)
	{
		NacUtility.printf("NacMediaPlayer : onAudioFocusChange! %b %d",
			this.mWasPlaying, focusChange);
		NacAudio.Attributes attrs = this.getAudioAttributes();

		attrs.setFocus(focusChange);
		attrs.revertDucking();

		if (focusChange == AudioManager.AUDIOFOCUS_GAIN)
		{
			//NacUtility.printf("NacMediaPlayer : GAIN!");
			this.mWasPlaying = true;
			if (!attrs.isStreamVolumeAlreadySet())
			{
				attrs.setVolume();
			}
			this.startWrapper();
		}
		else if (focusChange == AudioManager.AUDIOFOCUS_LOSS)
		{
			//NacUtility.printf("NacMediaPlayer : LOSS!");
			this.mWasPlaying = false;
			//attrs.revertVolume();
			this.stopWrapper();
		}
		else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT)
		{
			this.mWasPlaying = this.isPlayingWrapper();
			//NacUtility.printf("NacMediaPlayer : LOSS TRANSIENT! %b", this.mWasPlaying);
			//attrs.revertVolume();
			this.pauseWrapper();
		}
		else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK)
		{
			this.mWasPlaying = this.isPlayingWrapper();
			//NacUtility.printf("NacMediaPlayer : LOSS TRANSIENT DUCK! %b", this.mWasPlaying);
			attrs.duckVolume();
		}
	}

	/**
	 */
	@Override
	public void onCompletion(MediaPlayer mp)
	{
		if (this.hasPlaylist())
		{
			if (this.playNextTrack() == 0)
			{
				return;
			}
		}
		else if (this.shouldRepeat())
		{
			this.repeatTrack();
			return;
		}

		Context context = this.getContext();
		this.resetWrapper();
		NacAudio.abandonAudioFocus(context, this);
	}

	/**
	 * Pause the media player.
	 */
	public int pauseWrapper()
	{
		try
		{
			pause();
			return RESULT_SUCCESS;
		}
		catch (IllegalStateException e)
		{
			NacUtility.printf("NacMediaPlayer : IllegalStateException : pause()");
			return RESULT_ILLEGAL_STATE_EXCEPTION;
		}
	}

	/**
	 * Play the media associated with the given alarm.
	 *
	 * This can play an entire directory (playlist) or a single media file.
	 *
	 * @param  alarm   The alarm to get the media path from.
	 * @param  repeat  Whether the media should be repeated or not.
	 * @param  shuffle  Whether the media should be shuffled or not. This only
	 *     applies to directories (playlists).
	 */
	public void play(NacAlarm alarm, boolean repeat, boolean shuffle)
	{
		int type = alarm.getMediaType();
		String path = alarm.getMediaPath();
		Uri track = NacMedia.toUri(path);

		this.getAudioAttributes().merge(alarm);

		if (NacMedia.isDirectory(type))
		{
			this.playPlaylist(path, repeat, shuffle);
		}
		else
		{
			this.play(track, repeat);
		}
	}

	/**
	 * Play the media with the given Uri.
	 *
	 * @param  contentUri  The Uri of the content to play.
	 * @param  repeat      Whether the media should be repeated or not.
	 */
	public void play(Uri contentUri, boolean repeat)
	{
		Context context = this.getContext();
		NacAudio.Attributes attrs = this.getAudioAttributes();
		NacSharedConstants cons = new NacSharedConstants(context);

		if(!NacAudio.requestAudioFocus(context, this, attrs))
		{
			NacUtility.printf("Unable to gain audio focus.");
			NacUtility.quickToast(context, cons.getErrorMessagePlayAudio());
			return;
		}

		AudioAttributes audioAttributes = attrs.getAudioAttributes();

		try
		{
			this.resetWrapper();
			attrs.setRepeat(repeat);
			setDataSource(context, contentUri);
			setLooping(false);
			setAudioAttributes(audioAttributes);
			setOnCompletionListener(this);
			this.prepareWrapper();
			start();
		}
		catch (IllegalStateException | IOException | IllegalArgumentException | SecurityException e)
		{
			NacUtility.printf("NacMediaPlayer : play : %s", e.toString());
			NacUtility.quickToast(context, cons.getErrorMessagePlayFile());
		}
	}

	/**
	 * Play the next track in a playlist.
	 */
	public int playNextTrack()
	{
		Playlist playlist = this.getPlaylist();
		Uri track = (playlist != null) ? playlist.getNextTrack() : null;
		boolean repeat = playlist.getRepeat();

		if (track != null)
		{
			this.resetWrapper();
			this.play(track, repeat);
			return 0;
		}
		else
		{
			this.mPlaylist = null;
			return -1;
		}
	}

	/**
	 * Play a playlist.
	 */
	public void playPlaylist(String path, boolean repeat, boolean shuffle)
	{
		Context context = this.getContext();
		Playlist playlist = new Playlist(context, path, repeat, shuffle);
		Uri track = playlist.getTrack();
		this.mPlaylist = playlist;

		if (track != null)
		{
			this.play(track, repeat);
		}
	}

	/**
	 * Prepare the media player
	 */
	public int prepareWrapper()
	{
		try
		{
			prepare();
			return RESULT_SUCCESS;
		}
		catch (IllegalStateException e)
		{
			NacUtility.printf("NacMediaPlayer : IllegalStateException : prepare()");
			return RESULT_ILLEGAL_STATE_EXCEPTION;
		}
		catch (IOException e)
		{
			NacUtility.printf("NacMediaPlayer : IOException : prepare()");
			return RESULT_IO_EXCEPTION;
		}
	}

	/**
	 * Release the media player.
	 */
	public void releaseWrapper()
	{
		this.getAudioAttributes().revertVolume();
		this.abandonAudioFocus();
		this.cleanupHandler();
		release();
	}

	/**
	 * Repeat the currently playing track.
	 */
	public void repeatTrack()
	{
		this.cleanupHandler();
		this.getHandler().postDelayed(new Runnable() {
				@Override
				public void run()
				{
					startWrapper();
				}
			}, 500);
	}

	/**
	 * Reset the media player.
	 */
	public int resetWrapper()
	{
		try
		{
			//this.getAudioAttributes().revertVolume();
			this.cleanupHandler();
			reset();
			return RESULT_SUCCESS;
		}
		catch (IllegalStateException e)
		{
			NacUtility.printf("NacMediaPlayer : IllegalStateException : reset()");
			return RESULT_ILLEGAL_STATE_EXCEPTION;
		}
	}

	/**
	 * Seek to a position in the song.
	 */
	@SuppressWarnings("unused")
	public int seekToWrapper(int position)
	{
		try
		{
			seekTo(position);
			return RESULT_SUCCESS;
		}
		catch (IllegalStateException e)
		{
			NacUtility.printf("NacMediaPlayer : IllegalStateException : seekToWrapper()");
			return RESULT_ILLEGAL_STATE_EXCEPTION;
		}
	}

	/**
	 * @return True if the media player should repeat, and False otherwise.
	 */
	public boolean shouldRepeat()
	{
		NacAudio.Attributes attrs = this.getAudioAttributes();
		return (attrs != null) && attrs.getRepeat();
	}

	/**
	 * Start the media player.
	 */
	public int startWrapper()
	{
		try
		{
			start();
			return RESULT_SUCCESS;
		}
		catch (IllegalStateException e)
		{
			NacUtility.printf("NacMediaPlayer : IllegalStateException : start()");
			return RESULT_ILLEGAL_STATE_EXCEPTION;
		}
	}

	/**
	 * Stop the media player
	 */
	public int stopWrapper()
	{
		try
		{
			this.abandonAudioFocus();
			this.cleanupHandler();
			stop();
			return RESULT_SUCCESS;
		}
		catch (IllegalStateException e)
		{
			NacUtility.printf("NacMediaPlayer : IllegalStateException : stop()");
			return RESULT_ILLEGAL_STATE_EXCEPTION;
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

}

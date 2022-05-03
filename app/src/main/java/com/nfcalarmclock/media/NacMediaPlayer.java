package com.nfcalarmclock.media;

import android.content.Context;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.MediaItem;

import com.nfcalarmclock.util.NacUtility;
import com.nfcalarmclock.alarm.NacAlarm;
import com.nfcalarmclock.media.NacMedia;
import com.nfcalarmclock.shared.NacSharedConstants;

import java.util.List;

/**
 * Wrapper for the MediaPlayer class.
 */
@SuppressWarnings({"RedundantSuppression", "UnusedReturnValue"})
public class NacMediaPlayer
	implements AudioManager.OnAudioFocusChangeListener
{

	/**
	 * Application context.
	 */
	private final Context mContext;

	/**
	 * Audio attributes.
	 */
	private final NacAudioAttributes mAttributes;

	/**
	 * Handler to add some delay if looping media.
	 */
	private final Handler mHandler;

	/**
	 * Media player.
	 */
	private ExoPlayer mMediaPlayer;

	/**
	 * Check if player was playing (caused by losing audio focus).
	 */
	private boolean mWasPlaying;

	/**
	 * Flag indicating whether to gain transient audio focus, when requesting
	 * audio focus, or to gain regular focus.
	 */
	private boolean mShouldGainTransientAudioFocus;

	/**
	 */
	public NacMediaPlayer(Context context)
	{
		super();

		Looper looper = context.getMainLooper();
		this.mContext = context;
		this.mMediaPlayer = new ExoPlayer.Builder(context)
			.setLooper(looper)
			.build();
		this.mAttributes = new NacAudioAttributes(context);
		this.mHandler = new Handler(looper);
		this.mWasPlaying = false;
		this.mShouldGainTransientAudioFocus = false;
	}

	/**
	 * Abandon audio focus.
	 */
	public void abandonAudioFocus()
	{
		Context context = this.getContext();
		NacAudioManager.abandonFocus(context, this);
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
	public NacAudioAttributes getAudioAttributes()
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
	 * @return The media player.
	 */
	public ExoPlayer getMediaPlayer()
	{
		return this.mMediaPlayer;
	}

	/**
	 * Change media state when audio focus changes.
	 */
	@Override
	public void onAudioFocusChange(int focusChange)
	{
		NacUtility.printf("NacMediaPlayer : onAudioFocusChange! %b %d",
			this.mWasPlaying, focusChange);
		NacAudioAttributes attrs = this.getAudioAttributes();

		attrs.revertDucking();

		if (focusChange == AudioManager.AUDIOFOCUS_GAIN)
		{
			NacUtility.printf("NacMediaPlayer : GAIN!");
			this.mWasPlaying = true;
			if (!attrs.isStreamVolumeAlreadySet())
			{
				attrs.setVolume();
			}
			this.play();
		}
		else if (focusChange == AudioManager.AUDIOFOCUS_LOSS)
		{
			NacUtility.printf("NacMediaPlayer : LOSS!");
			this.mWasPlaying = false;
			//attrs.revertVolume();

			//this.stopWrapper(); Shown below
			//this.abandonAudioFocus(); Do I even need to do this? Wouldn't it already be abandoned?
			//this.cleanupHandler();
			this.getMediaPlayer().stop();
			this.getMediaPlayer().clearMediaItems();
			//this.getMediaPlayer().reset();
		}
		else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT)
		{
			this.mWasPlaying = this.getMediaPlayer().isPlaying();
			NacUtility.printf("NacMediaPlayer : LOSS TRANSIENT! %b", this.mWasPlaying);
			//attrs.revertVolume();
			this.getMediaPlayer().pause();
		}
		else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK)
		{
			this.mWasPlaying = this.getMediaPlayer().isPlaying();
			NacUtility.printf("NacMediaPlayer : LOSS TRANSIENT DUCK! %b", this.mWasPlaying);
			attrs.duckVolume();
		}
	}

	/**
	 * Request audio focus.
	 *
	 * @return True if the audio focus request was granted, and False otherwise.
	 */
	protected boolean requestAudioFocus()
	{
		Context context = this.getContext();
		NacAudioAttributes attrs = this.getAudioAttributes();
		boolean request;

		// Gain transient audio focus
		if (this.shouldGainTransientAudioFocus())
		{
			request = NacAudioManager.requestFocusGainTransient(context, this, attrs);
		}
		// Gain regular audio focus
		else
		{
			request = NacAudioManager.requestFocusGain(context, this, attrs);
		}

		// Unable to gain audio focus
		if(!request)
		{
			NacSharedConstants cons = new NacSharedConstants(context);

			NacUtility.printf("Unable to gain audio focus.");
			NacUtility.quickToast(context, cons.getErrorMessagePlayAudio());
		}

		return request;
	}

	/**
	 * Play the media item(s) that are already set.
	 */
	public void play()
	{
		NacAudioAttributes attrs = this.getAudioAttributes();

		// Unable to gain audio focus
		if (!this.requestAudioFocus())
		{
			return;
		}

		// Prepare to play the media
		this.getMediaPlayer().setAudioAttributes(attrs.getAudioAttributes(), false);
		this.getMediaPlayer().setRepeatMode(Player.REPEAT_MODE_ALL);
		this.getMediaPlayer().prepare();
		this.getMediaPlayer().play();
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
	public void playAlarm(NacAlarm alarm)
	{
		NacAudioAttributes attrs = this.getAudioAttributes();
		int type = alarm.getMediaType();
		String path = alarm.getMediaPath();
		Uri uri = Uri.parse(path);

		// Merge alarm with audio attributes
		attrs.merge(alarm);

		// Play the directory as a playlist
		if (NacMedia.isDirectory(type))
		{
			this.playDirectory(path);
		}
		// Play the media
		else
		{
			this.playUri(uri);
		}
	}

	/**
	 * Play a directory as a playlist.
	 *
	 * @param  path  Path to a directory.
	 */
	public void playDirectory(String path)
	{
		Context context = this.getContext();
		List<MediaItem> items = NacMedia.buildMediaItemsFromDirectory(context, path);

		this.playMediaItems(items);
	}

	/**
	 * Play a media item.
	 *
	 * @param  item  A media item.
	 */
	public void playMediaItem(MediaItem item)
	{
		//this.getMediaPlayer().stop();
		this.getMediaPlayer().setMediaItem(item);
		this.play();
	}

	/**
	 * Play a list of media items.
	 *
	 * @param  items  List of media items.
	 */
	public void playMediaItems(List<MediaItem> items)
	{
		//this.getMediaPlayer().stop();
		this.getMediaPlayer().setMediaItems(items);
		this.play();
	}

	/**
	 * Play the media with the given Uri.
	 *
	 * @param  uri  The Uri of the content to play.
	 * @param  repeat      Whether the media should be repeated or not.
	 */
	public void playUri(Uri uri)
	{
		Context context = this.getContext();
		MediaItem item = NacMedia.buildMediaItemFromFile(context, uri);

		this.playMediaItem(item);
	}

	/**
	 * Play a list of Uris as a playlist.
	 *
	 * @param  uris  List of files that are part of the playlist.
	 */
	public void playUris(List<Uri> uris)
	{
		Context context = this.getContext();
		List<MediaItem> items = NacMedia.buildMediaItemsFromFiles(context, uris);

		this.playMediaItems(items);
	}

	/**
	 * Release the media player.
	 */
	public void release()
	{
		this.getAudioAttributes().revertVolume();
		this.abandonAudioFocus();
		this.cleanupHandler();
		this.getMediaPlayer().release();
	}

	/**
	 * Set the flag indicating whether to gain transient audio focus, when
	 * requesting audio focus, or to gain regular focus.
	 *
	 * @param  shouldGainTransient  Whether to gain transient audio focus or not.
	 */
	public void setGainTransientAudioFocus(boolean shouldGainTransient)
	{
		this.mShouldGainTransientAudioFocus = shouldGainTransient;
	}

	/**
	 * @return Whether to gain transient audio focus, when requesting audio focus,
	 * or to gain regular focus.
	 */
	public boolean shouldGainTransientAudioFocus()
	{
		return this.mShouldGainTransientAudioFocus;
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

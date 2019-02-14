package com.nfcalarmclock;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.widget.Toast;

/**
 * Wrapper for the MediaPlayer class.
 */
public class NacMediaPlayer
	implements MediaPlayer.OnCompletionListener
{

	/**
	 * Application context.
	 */
	private Context mContext;

	/**
	 * Media player.
	 */
	private MediaPlayer mPlayer;

	/**
	 * User supplied OnCompletion listener.
	 */
	private MediaPlayer.OnCompletionListener mListener;

	/**
	 * Set the context.
	 */
	public NacMediaPlayer(Context context)
	{
		this.mContext = context;
		this.mPlayer = null;
		this.mListener = this;
	}

	/**
	 * Run after media player has completed playing the sound.
	 */
	@Override
	public void onCompletion(MediaPlayer mp)
	{
		this.reset();
	}

	/**
	 * @see play
	 */
	public void play(String media)
	{
		if (media.isEmpty())
		{
			return;
		}

		this.play(media, true);
	}

	/**
	 * @see play
	 */
	public void play(Uri uri)
	{
		this.play(uri, true);
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

		this.play(Uri.parse(media), loop);
	}

	/**
	 * Run the media player.
	 *
	 * @param  media  The media to play.
	 * @param  loop  Whether or not to loop the song.
	 */
	public void play(Uri uri, boolean loop)
	{
		this.reset();

		this.mPlayer = MediaPlayer.create(this.mContext, uri);

		if (this.mPlayer == null)
		{
			Toast.makeText(this.mContext, "Unable to play the selected file.",
				Toast.LENGTH_SHORT).show();
			return;
		}

		this.mPlayer.setLooping(loop);
		this.mPlayer.setOnCompletionListener(this.mListener);
		this.mPlayer.start();
	}

	/**
	 * Reset the media player.
	 */
	public void reset()
	{
		if (this.mPlayer != null)
		{
			this.mPlayer.stop();
			this.mPlayer.reset();
			this.mPlayer.release();
			this.mPlayer = null;
		}
	}

	/**
	 * Set the OnCompletion callback.
	 */
	public void setOnCompletionListener(MediaPlayer.OnCompletionListener
		listener)
	{
		this.mListener = listener;
	}

}

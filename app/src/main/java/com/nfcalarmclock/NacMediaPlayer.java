package com.nfcalarmclock;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;

/**
 * @brief Wrapper for the MediaPlayer class.
 */
public class NacMediaPlayer
	implements MediaPlayer.OnCompletionListener
{

	/**
	 * @brief Media player.
	 */
	private MediaPlayer mPlayer = null;

	/**
	 * @brief Application context.
	 */
	private Context mContext = null;

	/**
	 * @brief User supplied OnCompletion listener.
	 */
	private MediaPlayer.OnCompletionListener mListener = null;

	/**
	 * @brief Set the context.
	 */
	public NacMediaPlayer(Context c)
	{
		this.mContext = c;
		this.mListener = this;
	}

	/**
	 * @brief Run the media player.
	 */
	public void play(Uri uri)
	{
		this.play(uri, true);
	}

	/**
	 * @brief Run the media player.
	 */
	public void play(Uri uri, boolean loop)
	{
		this.reset();

		this.mPlayer = MediaPlayer.create(mContext, uri);

		this.mPlayer.setLooping(loop);
		this.mPlayer.setOnCompletionListener(this.mListener);
		this.mPlayer.start();
	}

	/**
	 * @see run()
	 */
	public void play(String str)
	{
		Uri uri = Uri.parse(str);

		this.play(uri);
	}

	/**
	 * @brief Reset the media player.
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
	 * @return The context.
	 */
	public Context getContext()
	{
		return this.mContext;
	}

	/**
	 * @brief Set the OnCompletion callback.
	 */
	public void setOnCompletionListener(MediaPlayer.OnCompletionListener
		listener)
	{
		this.mListener = listener;
	}

	/**
	 * @brief Run after media player has completed playing the sound.
	 */
	@Override
	public void onCompletion(MediaPlayer mp)
	{
		this.reset();
	}

}

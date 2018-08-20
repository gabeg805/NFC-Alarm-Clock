package com.nfcalarmclock;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;

/**
 * @brief Wrapper for the MediaPlayer class.
 */
public class NacCardMediaPlayer
{

	/**
	 * @brief The media player.
	 */
	private MediaPlayer mPlayer = null;

	/**
	 * @brief The context.
	 */
	private Context mContext = null;

	/**
	 * @brief Set the context.
	 */
	public NacCardMediaPlayer(Context c)
	{
		this.mContext = c;
	}

	/**
	 * @brief Run the media player.
	 */
	public void run(Uri uri)
	{
		this.mPlayer = MediaPlayer.create(mContext, uri);
		this.mPlayer.setLooping(true);
		this.mPlayer.start();
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

}

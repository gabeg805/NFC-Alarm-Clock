package com.nfcalarmclock.activealarm;

import android.content.Context;

import android.support.v4.media.session.MediaSessionCompat;
import androidx.media.VolumeProviderCompat;
import android.support.v4.media.session.PlaybackStateCompat;

/**
 * Control the volume level and keep it fixed.
 */
public class NacFixedVolumeController
{

	/**
	 * Tag for the class.
	 */
	public static final String TAG = "NacVolumeButtonHandlerService";

	/**
	 * Media session.
	 */
	private MediaSessionCompat mMediaSession;

	/**
	 * Volume provider to keep the volume level fixed.
	 */
	public static class NacVolumeProvider
		extends VolumeProviderCompat
	{
		/**
		 */
		public NacVolumeProvider()
		{
			super(VolumeProviderCompat.VOLUME_CONTROL_FIXED, 100, 50);
		}

	}

	/**
	 */
	public NacFixedVolumeController(Context context)
	{
		MediaSessionCompat mediaSession = new MediaSessionCompat(context, TAG);
		PlaybackStateCompat playbackState = new PlaybackStateCompat.Builder()
			.setState(PlaybackStateCompat.STATE_PLAYING, 0, 0)
			.build();

		// Simulate a player which plays something.
		mediaSession.setPlaybackState(playbackState);

		// Keep the volume level fixed
		NacVolumeProvider volumeProvider = new NacVolumeProvider();
			//VolumeProviderCompat.VOLUME_CONTROL_FIXED, 100, 50) {
			//	@Override
			//	public void onAdjustVolume(int direction) { }

		mediaSession.setPlaybackToRemote(volumeProvider);
		mediaSession.setActive(true);

		this.mMediaSession = mediaSession;
	}

	/**
	 * @return The media session.
	 */
	private MediaSessionCompat getMediaSession()
	{
		return this.mMediaSession;
	}

	/**
	 * Release control on the volume.
	 */
	public void release()
	{
		MediaSessionCompat mediaSession = this.getMediaSession();

		if (mediaSession != null)
		{
			mediaSession.release();
		}
	}

}


package com.nfcalarmclock.activealarm;

import android.content.Context;

import android.content.ComponentName;
import android.content.Intent;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import androidx.media.session.MediaButtonReceiver;
import androidx.media.VolumeProviderCompat;

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
		ComponentName mediaButtonReceiver = new ComponentName(context,
			MediaButtonReceiver.class);
		MediaSessionCompat mediaSession = new MediaSessionCompat(context, TAG,
			mediaButtonReceiver, null);
		PlaybackStateCompat playbackState = new PlaybackStateCompat.Builder()
			.setState(PlaybackStateCompat.STATE_PLAYING, 0, 0)
			.build();
		NacVolumeProvider volumeProvider = new NacVolumeProvider();

		mediaSession.setFlags(
			MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
			MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

		// Simulate a player which plays something.
		mediaSession.setPlaybackState(playbackState);

		// Keep the volume level fixed
		mediaSession.setPlaybackToRemote(volumeProvider);

		// Set the session as active
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
	 * Handle any media button receiver intents.
	 */
	public void handleIntent(Intent intent)
	{
		MediaSessionCompat mediaSession = this.getMediaSession();

		MediaButtonReceiver.handleIntent(mediaSession, intent);
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


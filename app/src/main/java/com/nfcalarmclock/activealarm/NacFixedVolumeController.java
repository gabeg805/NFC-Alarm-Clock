package com.nfcalarmclock.activealarm;

import android.content.Context;

import android.content.ComponentName;
import android.content.Intent;
//import android.support.v4.media.session.MediaSessionCompat;
//import android.support.v4.media.session.PlaybackStateCompat;
import androidx.media.VolumeProviderCompat;
//import androidx.mediarouter.media.MediaRouter;

import androidx.media2.session.MediaSession;
import androidx.media2.session.SessionCommand;
import androidx.media2.session.SessionResult;
import com.nfcalarmclock.util.NacUtility;
import androidx.media2.player.MediaPlayer;
import androidx.media2.common.SessionPlayer;
import androidx.media2.session.RemoteSessionPlayer;

/**
 * Control the volume level and keep it fixed.
 */
public class NacFixedVolumeController
{

	/**
	 * Tag for the class.
	 */
	public static final String TAG = "NacFixedVolumeController";

	/**
	 * Media session.
	 */
	private MediaSession mMediaSession;
	//private MediaSessionCompat mMediaSession;

	/**
	 * Volume provider to keep the volume level fixed.
	 */
	//public static class NacVolumeProvider
	//	extends VolumeProviderCompat
	//{
	//	/**
	//	 */
	//	public NacVolumeProvider()
	//	{
	//		super(VolumeProviderCompat.VOLUME_CONTROL_FIXED, 100, 50);
	//	}

	//}

	/**
	 */
	public static class MediaSessionCallback
		extends MediaSession.SessionCallback
	{

		@Override
		public int onCommandRequest (MediaSession session,
			MediaSession.ControllerInfo controller, SessionCommand command)
		{
			NacUtility.printf("onCommandRequest! %s || %d",
				command.getCustomAction(),
				command.getCommandCode());

			return SessionResult.RESULT_SUCCESS;
		}

	}

	/**
	 */
	//public NacFixedVolumeController(Context context, MediaPlayer mediaPlayer)
	public NacFixedVolumeController(Context context, SessionPlayer mediaPlayer)
	{
		//MediaRouter mediaRouter = MediaRouter.getInstance(context);
		//MediaSessionCompat mediaSession = new MediaSessionCompat(context, TAG);

		MediaSession mediaSession = new MediaSession.Builder(context, mediaPlayer)
			.setSessionCallback(context.getMainExecutor(), new MediaSessionCallback())
			.build();

		//PlaybackStateCompat playbackState = new PlaybackStateCompat.Builder()
		//	.setState(PlaybackStateCompat.STATE_PLAYING, 0, 0)
		//	.build();
		//NacVolumeProvider volumeProvider = new NacVolumeProvider();

		//mediaSession.setFlags(
		//	MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
		//	MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

		// Simulate a player which plays something.
		//mediaSession.setPlaybackState(playbackState);

		// Set the session as active
		//mediaSession.setActive(true);

		// Set the media session
		//mediaRouter.setMediaSessionCompat(mediaSession);

		// Keep the volume level fixed
		//mediaSession.setPlaybackToRemote(volumeProvider);

		this.mMediaSession = mediaSession;
	}

	/**
	 * @return The media session.
	 */
	//private MediaSessionCompat getMediaSession()
	public MediaSession getMediaSession()
	{
		return this.mMediaSession;
	}

	/**
	 * Release control on the volume.
	 */
	public void release()
	{
		//MediaRouter mediaRouter = MediaRouter.getInstance(context);
		//MediaSessionCompat mediaSession = this.getMediaSession();
		MediaSession mediaSession = this.getMediaSession();

		if (mediaSession != null)
		{
			mediaSession.getPlayer().close();
			mediaSession.close();
			//mediaSession.release();
		}

		//mediaRouter.setMediaSessionCompat(null);
	}

}


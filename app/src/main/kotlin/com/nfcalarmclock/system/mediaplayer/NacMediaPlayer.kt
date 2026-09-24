package com.nfcalarmclock.system.mediaplayer

import android.content.Context
import android.media.AudioManager
import android.net.Uri
import androidx.core.net.toUri
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Player.COMMAND_SET_REPEAT_MODE
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.nfcalarmclock.R
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.log.NacLog
import com.nfcalarmclock.shared.NacSharedPreferences
import com.nfcalarmclock.system.isUserUnlocked
import com.nfcalarmclock.system.media.NacAudioAttributes
import com.nfcalarmclock.system.media.NacMedia
import com.nfcalarmclock.system.media.abandonFocus
import com.nfcalarmclock.system.media.findFirstValidLocalMedia
import com.nfcalarmclock.system.media.getSafeStreamVolume
import com.nfcalarmclock.system.media.isMediaDirectory
import com.nfcalarmclock.system.media.isMediaValid
import com.nfcalarmclock.system.media.requestFocusGain
import com.nfcalarmclock.system.media.requestFocusGainTransient
import com.nfcalarmclock.system.media.setStreamVolume
import com.nfcalarmclock.view.quickToast
import java.io.File

/**
 * Wrapper for the ExoPlayer class.
 *
 * @param context Application context.
 * @param listener ExoPlayer listener.
 * @param audioAttributes Audio attributes.
 * @param shouldGainTransientAudioFocus Whether to gain transient audio focus when the audio focus request occurs. If not regular audio focus will be gained.
 */
@UnstableApi
class NacMediaPlayer(
	context: Context,
	listener: Player.Listener? = null,
	val audioAttributes: NacAudioAttributes = NacAudioAttributes(context),
	private val shouldGainTransientAudioFocus: Boolean = false
)
{

	/**
	 * Audio focus change listener.
	 */
	interface OnAudioFocusChangeListener
	{

		/**
		 * Audio should be ducked.
		 */
		fun onAudioFocusDuck(mediaPlayer: NacMediaPlayer)
		{
			NacLog.i("Default focus change: duck")
		}

		/**
		 * Audio focus is gained.
		 */
		fun onAudioFocusGain(context: Context, mediaPlayer: NacMediaPlayer)
		{
			NacLog.i("Default focus change: gain")
		}

		/**
		 * Audio focus is lost.
		 */
		fun onAudioFocusLoss(mediaPlayer: NacMediaPlayer)
		{
			NacLog.i("Default focus change: loss")
		}

		/**
		 * Audio focus is lost, but is transient.
		 */
		fun onAudioFocusLossTransient(mediaPlayer: NacMediaPlayer)
		{
			NacLog.i("Default focus change: loss transient")
		}

	}

	/**
	 * Phone media player.
	 */
	val exoPlayer: ExoPlayer = ExoPlayer.Builder(context)
		.setLooper(context.mainLooper)
		.setWakeMode(C.WAKE_MODE_LOCAL)
		.build()

	/**
	 * Audio manager.
	 */
	val audioManager: AudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

	/**
	 * Shared preferences.
	 */
	private val sharedPreferences: NacSharedPreferences = NacSharedPreferences(context)

	/**
	 * Whether the player is paused or not.
	 */
	var isPaused: Boolean = false
		private set

	/**
	 * Whether to show toasts or not.
	 */
	var shouldShowToasts: Boolean = true

	/**
	 * Listener for any audio focus changes.
	 */
	var onAudioFocusChangeListener: OnAudioFocusChangeListener =
		object: OnAudioFocusChangeListener {}

	/**
	 * Listener for audio focus request when audio focus changes.
	 */
	val onAudioFocusRequestChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->

		NacLog.i("Requesting audio focus=$focusChange | stream=${audioAttributes.stream} | volume=${audioManager.getSafeStreamVolume(audioAttributes.stream)} | wasDucking=${audioAttributes.wasDucking} | prevVolume=${sharedPreferences.previousVolume} | prevBluetoothVolume=${sharedPreferences.previousBluetoothVolume}")

		// Revert ducking
		if (audioAttributes.wasDucking)
		{
			NacLog.i("Changing stream volume to previous volume due to duck")

			// Reset the ducking flag
			audioAttributes.wasDucking = false

			// Revert the volume back to what it was
			audioManager.setStreamVolume(audioAttributes.stream, sharedPreferences.previousVolume)
		}

		// Check what type of focus change occurred
		when (focusChange)
		{
			// Gain audio focus
			AudioManager.AUDIOFOCUS_GAIN -> onAudioFocusChangeListener.onAudioFocusGain(context, this)

			// Loss of audio focus
			AudioManager.AUDIOFOCUS_LOSS -> onAudioFocusChangeListener.onAudioFocusLoss(this)

			// Transient loss of audio focus
			AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> onAudioFocusChangeListener.onAudioFocusLossTransient(this)

			// Transient lose audio focus but can duck audio
			AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> onAudioFocusChangeListener.onAudioFocusDuck(this)
		}

	}

	/**
	 * Constructor.
	 */
	init
	{
		// Add the analytics listener for logging any issues
		exoPlayer.addAnalyticsListener(NacMediaLogger())

		// Set the listener
		if (listener != null)
		{
			exoPlayer.addListener(listener)
		}
	}

	/**
	 * Build media items to play.
	 */
	fun buildMediaItemsToPlay(context: Context, alarm: NacAlarm): List<MediaItem>
	{
		// Check if file/directory exists
		var uri: Uri? = alarm.mediaPath.toUri()
		NacLog.i("Building media items to play. mediaPath=${alarm.mediaPath}")

		// Check if the media can be accessed. Most of the times when it cannot be acessed, it is
		// because the alarm went off in direct boot mode (when the device rebooted and the user
		// has not unlocked it yet) or because the media was moved/removed
		//
		// Note: This may require device protected storage context to work
		if (uri!!.isMediaValid(context) && isUserUnlocked(context))
		{
			NacLog.i("Media is valid and device is unlocked")

			// Play the directory as a playlist
			if (alarm.mediaType.isMediaDirectory())
			{
				NacLog.i("Building directory media items")

				return NacMedia.buildMediaItemsFromDirectory(
					context,
					alarm.mediaPath,
					recursive = alarm.shouldRecursivelyPlayMedia,
					shuffle = alarm.shouldShuffleMedia)
			}
		}
		else
		{
			// Get the local media path
			val localUri = alarm.localMediaPath.toUri()
			val localFile = File(alarm.localMediaPath)

			NacLog.i("Using the local media path. localMediaPath=${alarm.localMediaPath}")

			// Check if this local path can be accessed. If this cannot be accessed, it
			// could be that the original media is a directory, which would not have any
			// local media to play, so the expected behavior here would be to just play
			// a random song in the local files directory
			if (localUri.isMediaValid(context) && localFile.exists())
			{
				uri = localUri
			}
			else
			{
				NacLog.w("Local uri was not valid media or the file did not exist. exists=${localFile.exists()}")
				return emptyList()
			}
		}

		// Build media item from file
		return listOf(NacMedia.buildMediaItemFromFile(context, uri))
	}

	/**
	 * Pause the media player.
	 */
	fun pause()
	{
		NacLog.i("Pausing media player")

		// Set the flag
		isPaused = true

		// Pause the media player
		exoPlayer.pause()
	}

	/**
	 * Prepare the player before it is played.
	 */
	private fun prepare()
	{
		NacLog.i("Preparing media player")

		// Set the repeat mode if the command is available
		if (exoPlayer.isCommandAvailable(COMMAND_SET_REPEAT_MODE))
		{
			exoPlayer.repeatMode = Player.REPEAT_MODE_ALL
		}

		// Prepare the media
		exoPlayer.setAudioAttributes(audioAttributes.audioAttributesMedia3, false)
		exoPlayer.prepare()
	}

	/**
	 * Play the media item(s) that are already set.
	 *
	 * @param context Context.
	 */
	fun play(context: Context)
	{
		NacLog.i("Playing media player. stream=${audioAttributes.stream} | volume=${audioManager.getSafeStreamVolume(audioAttributes.stream)}")

		// Unable to gain audio focus
		if (!requestAudioFocus(context))
		{
			return
		}

		// Set the flag
		isPaused = false

		// Play the media
		exoPlayer.play()
	}

	/**
	 * Play the media associated with the given alarm.
	 *
	 * This can play an entire directory (playlist) or a single media file.
	 *
	 * @param context Context.
	 * @param alarm The alarm to get the media path from.
	 */
	fun playAlarm(context: Context, alarm: NacAlarm): List<MediaItem>
	{
		NacLog.i("Playing alarm with the media player")

		// TODO: Check if this is really necessary since playDirectory() shuffles the media items as well
		//// Set shuffle mode (can be true or false) when playing a media directory
		//if (alarm.mediaType.isMediaDirectory())
		//{
		//	exoPlayer.shuffleModeEnabled = alarm.shouldShuffleMedia
		//}

		// Build list of media item(s) to play from the alarm
		val mediaItems = buildMediaItemsToPlay(context, alarm)

		// Play media items
		if (mediaItems.isNotEmpty())
		{
			playMediaItems(context, mediaItems)
		}
		// Media item(s) could not be played for some reason
		else
		{
			NacLog.i("Attempting to find a random song to play in local files directory")

			// Find a random song to play in the local files directory
			// TODO: Should I use Settings.System.DEFAULT_ALARM_ALERT_URI?
			val uri = findFirstValidLocalMedia(context, alarm.localMediaPath.toUri())

			// Play the random song
			if (uri != null)
			{
				playUri(context, uri)
			}
			// Unable to find a random song to play
			else if (shouldShowToasts)
			{
				NacLog.e("Unable to find a random song to play")
				quickToast(context, R.string.error_message_play_audio)
			}
		}

		return mediaItems
	}

	/**
	 * Play a media item.
	 *
	 * @param context Context.
	 * @param  item  A media item.
	 */
	fun playMediaItem(context: Context, item: MediaItem)
	{
		NacLog.i("Playing media item. mediaId=${item.mediaId}")

		try
		{
			// Set the media item
			exoPlayer.setMediaItem(item)
		}
		catch (e: IllegalStateException)
		{
			NacLog.e("NacMediaPlayer : playMediaItem : ${e.toString()}", throwable = e)
			return
		}

		// Play the media item
		prepare()
		play(context)
	}

	/**
	 * Play a list of media items.
	 *
	 * @param context Context.
	 * @param  items  List of media items.
	 */
	fun playMediaItems(context: Context, items: List<MediaItem>)
	{
		NacLog.i("Playing list of media items. count=${items.size}")

		try
		{
			// Set the media items
			exoPlayer.setMediaItems(items)
		}
		catch (e: IllegalStateException)
		{
			NacLog.e("NacMediaPlayer : playMediaItems : ${e.toString()}", throwable = e)
			return
		}

		// Play the media items
		prepare()
		play(context)
	}

	/**
	 * Play the media with the given Uri.
	 *
	 * @param context Context.
	 * @param uri The Uri of the content to play.
	 */
	fun playUri(context: Context, uri: Uri)
	{
		NacLog.i("Playing uri=$uri")

		// Convert the URI to a media item
		val item = NacMedia.buildMediaItemFromFile(context, uri)

		// Play the media item
		playMediaItem(context, item)
	}

	/**
	 * Release the media player.
	 */
	fun release()
	{
		NacLog.i("Releasing media player")

		// Abandon audio focus
		audioManager.abandonFocus(audioAttributes)

		// Release the media player resources
		exoPlayer.release()
	}

	/**
	 * Request audio focus.
	 *
	 * @param context Context.
	 *
	 * @return True if the audio focus request was granted, and False otherwise.
	 */
	private fun requestAudioFocus(context: Context): Boolean
	{
		// Request to gain audio focus
		val request: Boolean = if (shouldGainTransientAudioFocus)
		{
			// Gain transient
			NacLog.i("Requesting audio focus gain transient. stream=${audioAttributes.stream} | usage=${audioAttributes.audioUsage} | volume=${audioManager.getSafeStreamVolume(audioAttributes.stream)}")
			audioManager.requestFocusGainTransient(onAudioFocusRequestChangeListener, audioAttributes)
		}
		else
		{
			// Gain
			NacLog.i("Requesting audio focus gain. stream=${audioAttributes.stream} | usage=${audioAttributes.audioUsage} | volume=${audioManager.getSafeStreamVolume(audioAttributes.stream)}")
			audioManager.requestFocusGain(onAudioFocusRequestChangeListener, audioAttributes)
		}

		// Unable to gain audio focus
		if (!request)
		{
			NacLog.e("Unable to request audio focus. stream=${audioAttributes.stream} | usage=${audioAttributes.audioUsage} | volume=${audioManager.getSafeStreamVolume(audioAttributes.stream)} | shouldGainTransient=$shouldGainTransientAudioFocus")

			// Show toast with error message
			if (shouldShowToasts)
			{
				quickToast(context, R.string.error_message_play_audio)
			}
		}

		return request
	}

	/**
	 * Stop the media player.
	 */
	fun stop()
	{
		NacLog.i("Stopping media player")

		// Set the flag
		isPaused = false

		// Stop the media player
		exoPlayer.stop()

		// Clear all media items
		exoPlayer.clearMediaItems()
	}

}
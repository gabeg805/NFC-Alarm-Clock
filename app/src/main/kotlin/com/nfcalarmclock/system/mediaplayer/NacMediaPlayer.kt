package com.nfcalarmclock.system.mediaplayer

import android.content.Context
import android.media.AudioManager
import android.net.Uri
import android.os.Handler
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Player.COMMAND_SET_REPEAT_MODE
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.nfcalarmclock.R
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.util.NacUtility.quickToast
import com.nfcalarmclock.system.isUserUnlocked
import com.nfcalarmclock.util.media.NacAudioAttributes
import com.nfcalarmclock.util.media.NacAudioManager
import com.nfcalarmclock.util.media.NacMedia
import com.nfcalarmclock.util.media.findFirstValidLocalMedia
import com.nfcalarmclock.util.media.isMediaDirectory
import com.nfcalarmclock.util.media.isMediaValid
import java.io.File
import androidx.core.net.toUri

/**
 * Wrapper for the MediaPlayer class.
 */
@UnstableApi
class NacMediaPlayer(

	/**
	 * Application context.
	 */
	private val context: Context,

	/**
	 * Exo player listener.
	 */
	listener: Player.Listener? = null

	// Interface
) : AudioManager.OnAudioFocusChangeListener
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
			mediaPlayer.duck()
		}

		/**
		 * Audio focus is gained.
		 */
		fun onAudioFocusGain(mediaPlayer: NacMediaPlayer)
		{
			mediaPlayer.play()
		}

		/**
		 * Audio focus is lost.
		 */
		fun onAudioFocusLoss(mediaPlayer: NacMediaPlayer)
		{
			mediaPlayer.stop()
		}

		/**
		 * Audio focus is lost, but is transient.
		 */
		fun onAudioFocusLossTransient(mediaPlayer: NacMediaPlayer)
		{
			mediaPlayer.pause()
		}

	}

	/**
	 * Handler to add some delay if looping media.
	 */
	private val handler: Handler = Handler(context.mainLooper)

	/**
	 * Media player.
	 */
	val exoPlayer: ExoPlayer = ExoPlayer.Builder(context)
		.setLooper(context.mainLooper)
		.build()

	/**
	 * Audio attributes.
	 */
	val audioAttributes: NacAudioAttributes = NacAudioAttributes(context)

	/**
	 * Flag indicating whether to gain transient audio focus, when requesting
	 * audio focus, or to gain regular focus.
	 */
	var shouldGainTransientAudioFocus: Boolean = false

	/**
	 * Check if the player was playing.
	 */
	var wasPlaying: Boolean = false
		private set

	/**
	 * Whether to show toasts or not.
	 */
	var shouldShowToasts: Boolean = true

	/**
	 * Listener for any audio focus changes.
	 */
	var onAudioFocusChangeListener: OnAudioFocusChangeListener =
		object: OnAudioFocusChangeListener
		{}

	/**
	 * Constructor.
	 */
	init
	{
		// Check if the listener is not null
		if (listener != null)
		{
			// Set the listener
			exoPlayer.addListener(listener)
		}
	}

	/**
	 * Abandon audio focus.
	 */
	private fun abandonAudioFocus()
	{
		NacAudioManager.abandonFocus(context, this)
	}

	/**
	 * Duck the media player.
	 */
	fun duck()
	{
		// Set the was playing flag
		wasPlaying = exoPlayer.isPlaying

		// Duck the volume
		audioAttributes.duckVolume()
	}

	/**
	 * Cleanup the handler.
	 */
	private fun cleanupHandler()
	{
		handler.removeCallbacksAndMessages(null)
	}

	/**
	 * Change media state when audio focus changes.
	 */
	override fun onAudioFocusChange(focusChange: Int)
	{
		// Revert ducking
		audioAttributes.revertDucking()

		// Check what type of focus change occurred
		when (focusChange)
		{

			// Gain audio focus
			AudioManager.AUDIOFOCUS_GAIN ->
			{
				onAudioFocusChangeListener.onAudioFocusGain(this)
			}

			// Loss of audio focus
			AudioManager.AUDIOFOCUS_LOSS ->
			{
				onAudioFocusChangeListener.onAudioFocusLoss(this)
			}

			// Transient loss of audio focus
			AudioManager.AUDIOFOCUS_LOSS_TRANSIENT ->
			{
				onAudioFocusChangeListener.onAudioFocusLossTransient(this)
			}

			// Transient lose audio focus but can duck audio
			AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK ->
			{
				onAudioFocusChangeListener.onAudioFocusDuck(this)
			}

		}
	}

	/**
	 * Request audio focus.
	 *
	 * @return True if the audio focus request was granted, and False otherwise.
	 */
	private fun requestAudioFocus(): Boolean
	{
		// Request to gain audio focus
		val request: Boolean = if (shouldGainTransientAudioFocus)
		{
			// Gain transient
			NacAudioManager.requestFocusGainTransient(context, this, audioAttributes)
		}
		else
		{
			// Gain
			NacAudioManager.requestFocusGain(context, this, audioAttributes)
		}

		// Unable to gain audio focus
		if (!request)
		{
			// Show toast with error message
			if (shouldShowToasts)
			{
				quickToast(context, R.string.error_message_play_audio)
			}
		}

		return request
	}

	/**
	 * Pause the media player.
	 */
	fun pause()
	{
		// Set the was playing flag
		wasPlaying = exoPlayer.isPlaying

		// Pause the media player
		exoPlayer.pause()
	}

	/**
	 * Play the media item(s) that are already set.
	 */
	fun play()
	{
		// Set the was playing flag
		wasPlaying = true

		// Unable to gain audio focus
		if (!requestAudioFocus())
		{
			return
		}

		// Set the repeat mode if the command is available
		if (exoPlayer.isCommandAvailable(COMMAND_SET_REPEAT_MODE))
		{
			exoPlayer.repeatMode = Player.REPEAT_MODE_ALL
		}

		// Prepare to play the media
		exoPlayer.setAudioAttributes(audioAttributes.audioAttributes, false)
		exoPlayer.prepare()
		exoPlayer.play()
	}

	/**
	 * Play the media associated with the given alarm.
	 *
	 *
	 * This can play an entire directory (playlist) or a single media file.
	 *
	 * @param  alarm   The alarm to get the media path from.
	 */
	fun playAlarm(alarm: NacAlarm): Uri?
	{
		// Merge alarm with audio attributes
		audioAttributes.merge(alarm)

		// Check if file/directory exists
		var uri: Uri? = alarm.mediaPath.toUri()

		// Check if the media can be accessed. Most of the times when it cannot be
		// acessed, it is because the alarm went off in direct boot mode (when the
		// device rebooted and the user has not unlocked it yet) or because the media
		// was moved/removed
		if (uri!!.isMediaValid(context) && isUserUnlocked(context))
		{
			// Directory
			if (alarm.mediaType.isMediaDirectory())
			{
				// Play the directory as a playlist and if the recursive flag is
				// set, it will also include the media in its subdirectories as
				// part of the playlist
				playDirectory(alarm.mediaPath, recursive = alarm.shouldRecursivelyPlayMedia)
				return uri
			}
		}
		else
		{
			// Get the local media path
			val localUri = alarm.localMediaPath.toUri()
			val localFile = File(alarm.localMediaPath)

			// Check if this local path can be accessed. If this cannot be accessed, it
			// could be that the original media is a directory, which would not have any
			// local media to play, so the expected behavior here would be to just play
			// a random song in the local files directory
			//uri = if (localUri.canAccessMedia(context))
			uri = if (localUri.isMediaValid(context) && localFile.exists())
			{
				localUri
			}
			else
			{
				findFirstValidLocalMedia(context, localUri)
			}
		}

		// Check if the uri is valid
		if (uri != null)
		{
			// Play the file
			playUri(uri)
			return uri
		}
		else
		{
			// Show toast saying unable to play audio
			if (shouldShowToasts)
			{
				quickToast(context, R.string.error_message_play_audio)
			}
			return null
		}
	}

	/**
	 * Play the media in a directory as a playlist.
	 *
	 * @param path Path to a directory.
	 * @param recursive Whether to recursively search a directory or not.
	 */
	private fun playDirectory(path: String, recursive: Boolean = false)
	{
		// Convert the path to media items
		val items = NacMedia.buildMediaItemsFromDirectory(context, path,
			recursive = recursive)

		// Play the media items
		//items.forEach { println(it.mediaId) }
		playMediaItems(items)
	}

	/**
	 * Play a media item.
	 *
	 * @param  item  A media item.
	 */
	private fun playMediaItem(item: MediaItem)
	{
		try
		{
			// Set the media item
			exoPlayer.setMediaItem(item)
		}
		catch (e: IllegalStateException)
		{
			println("NacMediaPlayer : playMediaItem() : ${e.toString()}")
		}

		// Play the media item
		play()
	}

	/**
	 * Play a list of media items.
	 *
	 * @param  items  List of media items.
	 */
	private fun playMediaItems(items: List<MediaItem>)
	{
		try
		{
			// Set the media items
			exoPlayer.setMediaItems(items)
		}
		catch (e: IllegalStateException)
		{
			println("NacMediaPlayer : playMediaItems() : ${e.toString()}")
		}

		// Play the media items
		play()
	}
 /**
	 * Play the media with the given Uri.
	 *
	 * @param  uri  The Uri of the content to play.
	 */
	fun playUri(uri: Uri)
	{
		// Convert the URI to a media item
		val item = NacMedia.buildMediaItemFromFile(context, uri)

		// Play the media item
		playMediaItem(item)
	}

	/**
	 * Release the media player.
	 */
	fun release()
	{
		// Abandon audio focus
		abandonAudioFocus()

		// Cleanup the handler
		cleanupHandler()

		// Release the media player resources
		exoPlayer.release()
	}

	/**
	 * Stop the media player.
	 */
	fun stop()
	{
		// Set the was playing flag
		wasPlaying = false

		// Stop the media player
		exoPlayer.stop()

		// Clear all media items
		exoPlayer.clearMediaItems()
	}

}
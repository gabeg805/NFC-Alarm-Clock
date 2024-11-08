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
import com.nfcalarmclock.util.media.NacAudioAttributes
import com.nfcalarmclock.util.media.NacAudioManager
import com.nfcalarmclock.util.media.NacMedia
import com.nfcalarmclock.util.NacUtility.quickToast

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
			quickToast(context, R.string.error_message_play_audio)
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
	fun playAlarm(alarm: NacAlarm)
	{
		// Merge alarm with audio attributes
		audioAttributes.merge(alarm)

		// Check if the media is a directory
		if (NacMedia.isDirectory(alarm.mediaType))
		{
			// Play the directory as a playlist and if the recursive flag is
			// set, it will also include the media in its subdirectories as
			// part of the playlist
			playDirectory(alarm.mediaPath, recursive = alarm.recursivelyPlayMedia)
		}
		// Media is a file
		else
		{
			// Get the media URI
			val uri = Uri.parse(alarm.mediaPath)

			// Play the file
			playUri(uri)
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
		playMediaItems(items)
	}

	/**
	 * Play a media item.
	 *
	 * @param  item  A media item.
	 */
	private fun playMediaItem(item: MediaItem)
	{
		// Set the media item
		exoPlayer.setMediaItem(item)

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
		// Set the media items
		exoPlayer.setMediaItems(items)

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
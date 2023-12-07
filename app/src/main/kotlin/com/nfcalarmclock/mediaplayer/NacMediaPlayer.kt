package com.nfcalarmclock.mediaplayer

import android.content.Context
import android.media.AudioManager
import android.media.AudioManager.OnAudioFocusChangeListener
import android.net.Uri
import android.os.Handler
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Player.COMMAND_SET_REPEAT_MODE
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.nfcalarmclock.R
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.media.NacAudioAttributes
import com.nfcalarmclock.media.NacAudioManager
import com.nfcalarmclock.media.NacMedia
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
) : OnAudioFocusChangeListener
{

	/**
	 * Audio attributes.
	 */
	val audioAttributes: NacAudioAttributes = NacAudioAttributes(context)

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
	 * Check if the player was playing.
	 */
	var wasPlaying: Boolean = false
		private set

	/**
	 * Flag indicating whether to gain transient audio focus, when requesting
	 * audio focus, or to gain regular focus.
	 */
	var shouldGainTransientAudioFocus: Boolean = false

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
		println("NacMediaPlayer : onAudioFocusChange! $wasPlaying | $focusChange")

		// Revert ducking
		audioAttributes.revertDucking()

		// Gain audio focus
		if (focusChange == AudioManager.AUDIOFOCUS_GAIN)
		{
			println("NacMediaPlayer : GAIN!")
			wasPlaying = true

			// Play
			play()
		}
		// Lose audio focus
		else if (focusChange == AudioManager.AUDIOFOCUS_LOSS)
		{
			println("NacMediaPlayer : LOSS!")

			wasPlaying = false
			//attrs.revertVolume();
			//this.stopWrapper(); Shown below
			//this.abandonAudioFocus(); Do I even need to do this? Wouldn't it already be abandoned?
			//this.cleanupHandler();

			// Stop the media player
			exoPlayer.stop()

			// Clear all media items
			exoPlayer.clearMediaItems()

			//this.getMediaPlayer().reset();
		}
		// Transient lose audio focus
		else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT)
		{
			wasPlaying = exoPlayer.isPlaying

			println("NacMediaPlayer : LOSS TRANSIENT! $wasPlaying")

			//attrs.revertVolume();

			// Pause the media player
			exoPlayer.pause()
		}
		// Transient lose audio focus but can duck audio
		else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK)
		{
			wasPlaying = exoPlayer.isPlaying

			println("NacMediaPlayer : LOSS TRANSIENT DUCK! $wasPlaying")

			// Duck the volume
			audioAttributes.duckVolume()
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
	 * Play the media item(s) that are already set.
	 */
	fun play()
	{
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
		// Get the media path and URI
		val path = alarm.mediaPath
		val uri = Uri.parse(path)

		// Merge alarm with audio attributes
		audioAttributes.merge(alarm)

		// Check if the media is a directory
		if (NacMedia.isDirectory(alarm.mediaType))
		{
			// Play the directory as a playlist and if the recursive flag is
			// set, it will also include the media in its subdirectories as
			// part of the playlist
			playDirectory(path, recursive = alarm.recursivelyPlayMedia)
		}
		// Media is a file
		else
		{
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
		val items = NacMedia.buildMediaItemsFromDirectory(context, path, recursive = recursive)

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

}

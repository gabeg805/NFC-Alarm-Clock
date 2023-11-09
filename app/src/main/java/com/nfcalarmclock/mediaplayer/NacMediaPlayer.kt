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
import com.nfcalarmclock.util.NacUtility.printf
import com.nfcalarmclock.util.NacUtility.quickToast

/**
 * Wrapper for the MediaPlayer class.
 */
@UnstableApi
class NacMediaPlayer(

	/**
	 * Application context.
	 */
	private val context: Context

) : OnAudioFocusChangeListener
{

	/**
	 * Audio attributes.
	 */
	private val audioAttributes: NacAudioAttributes = NacAudioAttributes(context)

	/**
	 * Handler to add some delay if looping media.
	 */
	private val handler: Handler = Handler(context.mainLooper)

	/**
	 * Media player.
	 */
	// TODO: setWakeMode, setDevicVolumeControlEnabled
	val mediaPlayer: ExoPlayer = ExoPlayer.Builder(context)
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
		printf("NacMediaPlayer : onAudioFocusChange! %b %d", wasPlaying, focusChange)

		// Revert ducking
		audioAttributes.revertDucking()

		// Gain audio focus
		if (focusChange == AudioManager.AUDIOFOCUS_GAIN)
		{
			printf("NacMediaPlayer : GAIN!")
			wasPlaying = true

			// Set the volume if not already set
			if (!audioAttributes.isStreamVolumeAlreadySet)
			{
				audioAttributes.setVolume()
			}

			// Play
			play()
		}
		// Lose audio focus
		else if (focusChange == AudioManager.AUDIOFOCUS_LOSS)
		{
			printf("NacMediaPlayer : LOSS!")

			wasPlaying = false
			//attrs.revertVolume();
			//this.stopWrapper(); Shown below
			//this.abandonAudioFocus(); Do I even need to do this? Wouldn't it already be abandoned?
			//this.cleanupHandler();

			// Stop the media player
			mediaPlayer.stop()

			// Clear all media items
			mediaPlayer.clearMediaItems()

			//this.getMediaPlayer().reset();
		}
		// Transient lose audio focus
		else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT)
		{
			wasPlaying = mediaPlayer.isPlaying

			printf("NacMediaPlayer : LOSS TRANSIENT! %b", wasPlaying)

			//attrs.revertVolume();

			// Pause the media player
			mediaPlayer.pause()
		}
		// Transient lose audio focus but can duck audio
		else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK)
		{
			wasPlaying = mediaPlayer.isPlaying

			printf("NacMediaPlayer : LOSS TRANSIENT DUCK! %b", wasPlaying)

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
			val message = context.getString(R.string.error_message_play_audio)

			// Show toast with error message
			quickToast(context, message)
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
		if (mediaPlayer.isCommandAvailable(COMMAND_SET_REPEAT_MODE))
		{
			mediaPlayer.repeatMode = Player.REPEAT_MODE_ALL
		}

		// Prepare to play the media
		mediaPlayer.setAudioAttributes(audioAttributes.audioAttributes, false)
		mediaPlayer.prepare()
		mediaPlayer.play()
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
			// Play the directory as a playlist
			playDirectory(path)
		}
		// Media is a file
		else
		{
			// Play the file
			playUri(uri)
		}
	}

	/**
	 * Play a directory as a playlist.
	 *
	 * @param  path  Path to a directory.
	 */
	private fun playDirectory(path: String)
	{
		// Convert the path to media items
		val items = NacMedia.buildMediaItemsFromDirectory(context, path)

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
		//this.getMediaPlayer().stop();

		// Set the media item
		mediaPlayer.setMediaItem(item)

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
		//this.getMediaPlayer().stop();

		// Set the media items
		mediaPlayer.setMediaItems(items)

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
		// Revert the volume
		audioAttributes.revertVolume()

		// Abandon audio focus
		abandonAudioFocus()

		// Cleanup the handler
		cleanupHandler()

		// Release the media player resources
		mediaPlayer.release()
	}

}
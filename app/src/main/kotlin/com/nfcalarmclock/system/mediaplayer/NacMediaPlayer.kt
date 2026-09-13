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
import com.nfcalarmclock.system.media.saveCurrentVolume
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
			NacLog.i("Focus change: duck")
			mediaPlayer.duck()
		}

		/**
		 * Audio focus is gained.
		 */
		fun onAudioFocusGain(context: Context, mediaPlayer: NacMediaPlayer)
		{
			NacLog.i("Focus change: gain")
			mediaPlayer.play(context)
		}

		/**
		 * Audio focus is lost.
		 */
		fun onAudioFocusLoss(mediaPlayer: NacMediaPlayer)
		{
			NacLog.i("Focus change: loss")
			mediaPlayer.stop()
		}

		/**
		 * Audio focus is lost, but is transient.
		 */
		fun onAudioFocusLossTransient(mediaPlayer: NacMediaPlayer)
		{
			NacLog.i("Focus change: loss transient")
			mediaPlayer.pause()
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
		object: OnAudioFocusChangeListener {}

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
	 * Duck the media player.
	 */
	fun duck()
	{
		NacLog.i("Ducking the media player")

		// Set the was playing flag
		wasPlaying = exoPlayer.isPlaying

		// Get stream and current volume
		val stream = audioAttributes.stream
		val currentVolume = audioManager.getSafeStreamVolume(stream)

		// Save current volume
		audioManager.saveCurrentVolume(sharedPreferences, stream)

		// Duck the volume
		audioManager.setStreamVolume(stream, currentVolume/2)
		audioAttributes.wasDucking = true
	}

	/**
	 * Pause the media player.
	 */
	fun pause()
	{
		NacLog.i("Pausing the media player")

		// Set the was playing flag
		wasPlaying = exoPlayer.isPlaying

		// Pause the media player
		exoPlayer.pause()
	}

	/**
	 * Play the media item(s) that are already set.
	 *
	 * @param context Context.
	 */
	fun play(context: Context)
	{
		NacLog.i("Playing the media player")

		// Set the was playing flag
		// TODO: Can this move after audio focus request?
		wasPlaying = true

		// Unable to gain audio focus
		if (!requestAudioFocus(context, audioAttributes))
		{
			return
		}

		// Set the repeat mode if the command is available
		if (exoPlayer.isCommandAvailable(COMMAND_SET_REPEAT_MODE))
		{
			exoPlayer.repeatMode = Player.REPEAT_MODE_ALL
		}

		// Play the media
		exoPlayer.setAudioAttributes(audioAttributes.audioAttributesMedia3, false)
		exoPlayer.prepare()
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
	fun playAlarm(context: Context, alarm: NacAlarm): Uri?
	{
		NacLog.i("Playing alarm with the media player")

		// TODO: Check if this is really necessary since playDirectory() shuffles the media items as well
		//// Set shuffle mode (can be true or false) when playing a media directory
		//if (alarm.mediaType.isMediaDirectory())
		//{
		//	exoPlayer.shuffleModeEnabled = alarm.shouldShuffleMedia
		//}

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
				// Play the directory as a playlist.
				// If the recursive flag is set, it will also include the media in
				// subdirectories as. Similarly, if shuffle is set, it will shuffle the
				// media
				playDirectory(
					context,
					alarm.mediaPath,
					recursive = alarm.shouldRecursivelyPlayMedia,
					shuffle = alarm.shouldShuffleMedia)
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
			playUri(context, uri)
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
	 * @param context Context.
	 * @param path Path to a directory.
	 * @param recursive Whether to recursively search a directory or not.
	 * @param shuffle Whether to shuffle the list of songs when playing or not.
	 */
	private fun playDirectory(
		context: Context,
		path: String,
		recursive: Boolean = false,
		shuffle: Boolean = false)
	{
		NacLog.i("Playing directory with the media player")

		// Convert the path to media items
		val items = NacMedia.buildMediaItemsFromDirectory(context, path,
			recursive = recursive, shuffle = shuffle)

		// Play the media items
		playMediaItems(context, items)
	}

	/**
	 * Play a media item.
	 *
	 * @param context Context.
	 * @param  item  A media item.
	 */
	fun playMediaItem(context: Context, item: MediaItem)
	{
		NacLog.i("Playing media item")

		try
		{
			// Set the media item
			exoPlayer.setMediaItem(item)
		}
		catch (e: IllegalStateException)
		{
			NacLog.e("NacMediaPlayer : playMediaItem : ${e.toString()}", throwable = e)
		}

		// Play the media item
		play(context)
	}

	/**
	 * Play a list of media items.
	 *
	 * @param context Context.
	 * @param  items  List of media items.
	 */
	private fun playMediaItems(context: Context, items: List<MediaItem>)
	{
		NacLog.i("Playing list of media items")

		try
		{
			// Set the media items
			exoPlayer.setMediaItems(items)
		}
		catch (e: IllegalStateException)
		{
			NacLog.e("NacMediaPlayer : playMediaItems : ${e.toString()}", throwable = e)
		}

		// Play the media items
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
		NacLog.i("Playing uri")

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
		NacLog.i("Releasing the media player")

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
	private fun requestAudioFocus(context: Context, attrs: NacAudioAttributes, shouldDuck: Boolean = true): Boolean
	{
		// Listener for when audio focus changes
		val listener = AudioManager.OnAudioFocusChangeListener { focusChange ->

			NacLog.i("Requesting audio focus=$focusChange | wasDucking=${attrs.wasDucking} | prevVolume=${sharedPreferences.previousVolume}")

			// Revert ducking
			if (shouldDuck && attrs.wasDucking)
			{
				// Reset the ducking flag
				attrs.wasDucking = false

				// Revert the volume back to what it was
				audioManager.setStreamVolume(attrs.stream, sharedPreferences.previousVolume)
			}

			// Check what type of focus change occurred
			when (focusChange)
			{

				// Gain audio focus
				AudioManager.AUDIOFOCUS_GAIN ->
				{
					onAudioFocusChangeListener.onAudioFocusGain(context, this@NacMediaPlayer)
				}

				// Loss of audio focus
				AudioManager.AUDIOFOCUS_LOSS ->
				{
					onAudioFocusChangeListener.onAudioFocusLoss(this@NacMediaPlayer)
				}

				// Transient loss of audio focus
				AudioManager.AUDIOFOCUS_LOSS_TRANSIENT ->
				{
					onAudioFocusChangeListener.onAudioFocusLossTransient(this@NacMediaPlayer)
				}

				// Transient lose audio focus but can duck audio
				AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK ->
				{
					onAudioFocusChangeListener.onAudioFocusDuck(this@NacMediaPlayer)
				}

			}

		}

		// Request to gain audio focus
		val request: Boolean = if (shouldGainTransientAudioFocus)
		{
			// Gain transient
			NacLog.i("Requesting audio focus gain transient. usage=${attrs.audioUsage}")
			audioManager.requestFocusGainTransient(listener, attrs)
		}
		else
		{
			// Gain
			NacLog.i("Requesting audio focus gain. usage=${attrs.audioUsage}")
			audioManager.requestFocusGain(listener, attrs)
		}

		// Unable to gain audio focus
		if (!request)
		{
			NacLog.e("Unable to request audio focus. usage=${attrs.audioUsage} | shouldGainTransient=$shouldGainTransientAudioFocus")

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

		// Set the was playing flag
		wasPlaying = false

		// Stop the media player
		exoPlayer.stop()

		// Clear all media items
		exoPlayer.clearMediaItems()
	}

}
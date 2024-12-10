package com.nfcalarmclock.alarm.options.mediapicker

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.media3.common.util.UnstableApi
import com.nfcalarmclock.R
import com.nfcalarmclock.alarm.NacAlarmViewModel
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.system.mediaplayer.NacMediaPlayer
import com.nfcalarmclock.system.scheduler.NacScheduler
import com.nfcalarmclock.shared.NacSharedPreferences
import com.nfcalarmclock.util.NacBundle
import com.nfcalarmclock.util.NacUtility
import com.nfcalarmclock.util.addMediaInfo
import com.nfcalarmclock.util.getDeviceProtectedStorageContext
import com.nfcalarmclock.util.getMediaArtist
import com.nfcalarmclock.util.getMediaPath
import com.nfcalarmclock.util.getMediaTitle
import com.nfcalarmclock.util.getMediaType
import com.nfcalarmclock.util.getRecursivelyPlayMedia
import com.nfcalarmclock.util.getShuffleMedia
import com.nfcalarmclock.util.media.NacMedia
import dagger.hilt.android.AndroidEntryPoint

/**
 * Media fragment for ringtones and music files.
 */
@UnstableApi
@AndroidEntryPoint
open class NacMediaPickerFragment
	: Fragment()
{

	/**
	 * Alarm view model.
	 */
	private val alarmViewModel: NacAlarmViewModel by viewModels()

	/**
	 * Alarm.
	 */
	private var alarm: NacAlarm? = null

	/**
	 * Media player.
	 */
	var mediaPlayer: NacMediaPlayer? = null

	/**
	 * Media path.
	 */
	var mediaPath: String = ""
		get()
		{
			return alarm?.mediaPath ?: field
		}
		set(value)
		{
			if (alarm != null)
			{
				alarm!!.mediaPath = value
				//alarm!!.setMedia(requireContext(), value)
			}
			else
			{
				field = value
			}
		}

	/**
	 * Media artist.
	 */
	var mediaArtist: String = ""
		get()
		{
			return alarm?.mediaArtist ?: field
		}
		set(value)
		{
			if (alarm != null)
			{
				alarm!!.mediaArtist = value
			}
			else
			{
				field = value
			}
		}

	/**
	 * Media title.
	 */
	private var mediaTitle: String = ""
		get()
		{
			return alarm?.mediaTitle ?: field
		}
		set(value)
		{
			if (alarm != null)
			{
				alarm!!.mediaTitle = value
			}
			else
			{
				field = value
			}
		}

	/**
	 * Media type.
	 */
	var mediaType: Int = NacMedia.TYPE_NONE
		get()
		{
			return alarm?.mediaType ?: field
		}
		set(value)
		{
			if (alarm != null)
			{
				alarm!!.mediaType = value
			}
			else
			{
				field = value
			}
		}

	/**
	 * Whether to shuffle the media.
	 */
	var shuffleMedia: Boolean = false
		get()
		{
			return alarm?.shuffleMedia ?: field
		}
		set(value)
		{
			if (alarm != null)
			{
				alarm!!.shuffleMedia = value
			}
			else
			{
				field = value
			}
		}

	/**
	 * Whether to recursively play the media in a directory.
	 */
	var recursivelyPlayMedia: Boolean = false
		get()
		{
			return alarm?.recursivelyPlayMedia ?: field
		}
		set(value)
		{
			if (alarm != null)
			{
				alarm!!.recursivelyPlayMedia = value
			}
			else
			{
				field = value
			}
		}

	/**
	 * Called when the Cancel button is clicked.
	 */
	open fun onCancelClicked()
	{
		requireActivity().finish()
	}

	/**
	 * Called when the Clear button is clicked.
	 */
	@UnstableApi
	open fun onClearClicked()
	{
		// Clear the media that is being used
		mediaPath = ""
		mediaArtist = ""
		mediaTitle = ""
		mediaType = NacMedia.TYPE_NONE

		// Stop any media that is already playing
		mediaPlayer?.exoPlayer?.stop()
	}

	/**
	 * Called when the fragment is created.
	 */
	@UnstableApi
	override fun onCreate(savedInstanceState: Bundle?)
	{
		// Super
		super.onCreate(savedInstanceState)

		// Get the bundle
		val bundle = arguments ?: Bundle()

		// Set the alarm
		alarm = NacBundle.getAlarm(arguments)

		// Check if the alarm was not set
		if (alarm == null)
		{
			// Set the media info
			mediaPath = bundle.getMediaPath()
			mediaArtist = bundle.getMediaArtist()
			mediaTitle = bundle.getMediaTitle()
			mediaType = bundle.getMediaType()
			shuffleMedia = bundle.getShuffleMedia()
			recursivelyPlayMedia = bundle.getRecursivelyPlayMedia()
		}

		// Create the media player
		val context = requireContext()
		mediaPlayer = NacMediaPlayer(context)

		// Gain transient audio focus
		mediaPlayer!!.shouldGainTransientAudioFocus = true
	}

	/**
	 * Called when the fragment is destroyed.
	 */
	@UnstableApi
	override fun onDestroy()
	{
		// Super
		super.onDestroy()

		// Cleanup the media player
		mediaPlayer?.release()
	}

	/**
	 * Called when the Ok button is clicked.
	 */
	open fun onOkClicked()
	{
		val activity = requireActivity()
		val deviceContext = getDeviceProtectedStorageContext(activity)

		println("Hello : $mediaPath")

		// Get the directory for app specific files and the name of the file to create
		val resolver = deviceContext.contentResolver
		val directory = deviceContext.filesDir

		// Get the URI from the path
		val uri = Uri.parse(mediaPath)
		val artist = NacMedia.getArtist(activity, uri)
		val title = NacMedia.getTitle(activity, uri)
		val type = NacMedia.getType(activity, mediaPath)
		val name = if (artist.isNotEmpty()) "$artist - $title" else title
		val newPath = "$directory/$name"
		println("New path : $newPath")
		println("Name     : $name")

		// TODO: What to do if directory is selected because it returns and never gets here?
		// TODO: When copying a ringtone, it thinks it is on external storage on the device.
		// Need to maybe save the original path too for comparison's sake?
		// TODO: Remove the file too when an alarm is deleted

		// Copy the file
		deviceContext.openFileOutput(name, Context.MODE_PRIVATE).use { fileOutput ->

			// Copy the file to the local file dir (for the app)
			resolver.openInputStream(uri).use { inputStream ->
				inputStream?.copyTo(fileOutput, 1024)
			}

		}

		//mediaPath = newPath

		// Check if alarm is set
		if (alarm != null)
		{
			// Update the alarm for the activity
			alarmViewModel.update(alarm!!)

			// Reschedule the alarm
			NacScheduler.update(activity, alarm!!)
		}
		// The media must be set
		else
		{
			// Create an intent with the media
			val intent = Intent()
				.addMediaInfo(mediaPath, artist, title, type,
					shuffleMedia, recursivelyPlayMedia)

			// Set the result of the activity with the media path as part of
			// the intent
			activity.setResult(Activity.RESULT_OK, intent)
		}

		// Finish the activity
		activity.finish()
	}

	/**
	 * Play audio from the media player.
	 *
	 * @param  uri  The Uri of the content to play.
	 */
	@UnstableApi
	protected fun play(uri: Uri)
	{
		val path = uri.toString()

		// Invalid URI path since it does not start with "content://"
		if (!path.startsWith("content://"))
		{
			// Show an error toast
			NacUtility.quickToast(requireContext(), R.string.error_message_play_audio)
			return
		}

		// Set the path of the media that is going to play
		mediaPath = path
		mediaArtist = NacMedia.getArtist(requireContext(), path)
		mediaTitle = NacMedia.getTitle(requireContext(), path)
		mediaType = NacMedia.getType(requireContext(), path)

		// Stop any media that is already playing
		mediaPlayer?.exoPlayer?.stop()

		// Save the current volume
		mediaPlayer!!.audioAttributes.saveCurrentVolume()

		// Play the media
		mediaPlayer!!.playUri(uri)
	}

	/**
	 * Setup action buttons.
	 */
	@UnstableApi
	protected fun setupActionButtons(root: View)
	{
		val shared = NacSharedPreferences(requireContext())
		val clear = root.findViewById<Button>(R.id.clear)
		val cancel = root.findViewById<Button>(R.id.cancel)
		val ok = root.findViewById<Button>(R.id.ok)

		// Set the color of the buttons
		clear.setTextColor(shared.themeColor)
		cancel.setTextColor(shared.themeColor)
		ok.setTextColor(shared.themeColor)

		// Set the Clear on click listener
		clear.setOnClickListener {
			onClearClicked()
		}

		// Set the Cancel on click listener
		cancel.setOnClickListener {
			onCancelClicked()
		}

		// Set the Ok on click listener
		ok.setOnClickListener {
			onOkClicked()
		}
	}

	///**
	// * Setup the media player.
	// */
	//@UnstableApi
	//private fun setupMediaPlayer()
	//{
	//	// Get the context
	//	val context = requireContext()

	//	// Create the media player
	//	mediaPlayer = NacMediaPlayer(context)

	//	// Gain transient audio focus
	//	mediaPlayer!!.shouldGainTransientAudioFocus = true
	//}

}

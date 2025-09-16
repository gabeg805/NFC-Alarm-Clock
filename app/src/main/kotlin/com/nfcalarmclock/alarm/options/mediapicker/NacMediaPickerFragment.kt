package com.nfcalarmclock.alarm.options.mediapicker

import android.app.Activity
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
import com.nfcalarmclock.shared.NacSharedPreferences
import com.nfcalarmclock.system.mediaplayer.NacMediaPlayer
import com.nfcalarmclock.system.scheduler.NacScheduler
import com.nfcalarmclock.util.NacUtility
import com.nfcalarmclock.system.addMediaInfo
import com.nfcalarmclock.system.getAlarm
import com.nfcalarmclock.system.getMediaArtist
import com.nfcalarmclock.system.getMediaPath
import com.nfcalarmclock.system.getMediaTitle
import com.nfcalarmclock.system.getMediaType
import com.nfcalarmclock.system.getRecursivelyPlayMedia
import com.nfcalarmclock.system.getShuffleMedia
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
	var mediaTitle: String = ""
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
	 * Local media path.
	 */
	var localMediaPath: String = ""
		get()
		{
			return alarm?.localMediaPath ?: field
		}
		set(value)
		{
			if (alarm != null)
			{
				alarm!!.localMediaPath = value
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
			return alarm?.shouldShuffleMedia ?: field
		}
		set(value)
		{
			if (alarm != null)
			{
				alarm!!.shouldShuffleMedia = value
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
			return alarm?.shouldRecursivelyPlayMedia ?: field
		}
		set(value)
		{
			if (alarm != null)
			{
				alarm!!.shouldRecursivelyPlayMedia = value
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
		alarm = bundle.getAlarm()

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
		// Get the activity
		val activity = requireActivity()

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
				.addMediaInfo(mediaPath, mediaArtist, mediaTitle, mediaType,
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

		// Set the path for the media that is going to play. Do not do the other
		// information yet until the user is ready
		mediaPath = path

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
		val clear: Button = root.findViewById(R.id.clear)
		val cancel: Button = root.findViewById(R.id.cancel)
		val ok: Button = root.findViewById(R.id.ok)

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

}

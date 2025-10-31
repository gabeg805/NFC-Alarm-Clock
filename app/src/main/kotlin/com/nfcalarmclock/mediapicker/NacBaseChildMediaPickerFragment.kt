package com.nfcalarmclock.mediapicker

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.annotation.OptIn
import androidx.fragment.app.Fragment
import androidx.media3.common.util.UnstableApi
import androidx.navigation.fragment.findNavController
import com.nfcalarmclock.R
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.shared.NacSharedPreferences
import com.nfcalarmclock.system.addMediaInfo
import com.nfcalarmclock.system.getMediaArtist
import com.nfcalarmclock.system.getMediaPath
import com.nfcalarmclock.system.getMediaTitle
import com.nfcalarmclock.system.getMediaType
import com.nfcalarmclock.system.getRecursivelyPlayMedia
import com.nfcalarmclock.system.getShuffleMedia
import com.nfcalarmclock.system.media.NacMedia
import com.nfcalarmclock.system.media.copyMediaToDeviceEncryptedStorage
import com.nfcalarmclock.system.media.doesDeviceHaveFreeSpace
import com.nfcalarmclock.system.mediaplayer.NacMediaPlayer
import com.nfcalarmclock.view.quickToast

/**
 * Media fragment for ringtones and music files.
 */
@OptIn(UnstableApi::class)
abstract class NacBaseChildMediaPickerFragment<T: NacAlarm>
	: Fragment()
{

	/**
	 * Listener when OK is clicked.
	 */
	fun interface OnOkClickedListener
	{
		fun onOkClicked(bundle: Bundle)
	}

	/**
	 * Item.
	 */
	protected var item: T? = null

	/**
	 * Listener when OK is clicked.
	 */
	var onOkClickedListener: OnOkClickedListener? = null

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
			return item?.mediaPath ?: field
		}
		set(value)
		{
			if (item != null)
			{
				item!!.mediaPath = value
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
			return item?.mediaArtist ?: field
		}
		set(value)
		{
			if (item != null)
			{
				item!!.mediaArtist = value
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
			return item?.mediaTitle ?: field
		}
		set(value)
		{
			if (item != null)
			{
				item!!.mediaTitle = value
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
			return item?.mediaType ?: field
		}
		set(value)
		{
			if (item != null)
			{
				item!!.mediaType = value
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
			return item?.localMediaPath ?: field
		}
		set(value)
		{
			if (item != null)
			{
				item!!.localMediaPath = value
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
			return item?.shouldShuffleMedia ?: field
		}
		set(value)
		{
			if (item != null)
			{
				item!!.shouldShuffleMedia = value
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
			return item?.shouldRecursivelyPlayMedia ?: field
		}
		set(value)
		{
			if (item != null)
			{
				item!!.shouldRecursivelyPlayMedia = value
			}
			else
			{
				field = value
			}
		}

	/**
	 * Copy the media to device encrypted storage.
	 */
	fun copyMediaToDeviceEncryptedStorage(deviceContext: Context)
	{
		// Device has enough free space
		if (doesDeviceHaveFreeSpace(deviceContext))
		{
			// Copy the media to the local files/ directory, in device protected storage
			copyMediaToDeviceEncryptedStorage(
				deviceContext, mediaPath, mediaArtist,
				mediaTitle, mediaType
			)
		}
		// Not enough space
		else
		{
			println("Not enough space to make a backup!")
		}
	}

	/**
	 * Cancel button is clicked.
	 */
	open fun onCancelClicked()
	{
		findNavController().popBackStack()
	}

	/**
	 * Clear button is clicked.
	 */
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
	 * Fragment is created.
	 */
	override fun onCreate(savedInstanceState: Bundle?)
	{
		// Super
		super.onCreate(savedInstanceState)

		// Get the bundle
		val bundle = arguments ?: Bundle()

		// Set the fragment item
		setFragmentItem(bundle)

		// Item was not set
		if (item == null)
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
	 * Fragment is destroyed.
	 */
	override fun onDestroy()
	{
		// Super
		super.onDestroy()

		// Cleanup the media player
		mediaPlayer?.release()
	}

	/**
	 * Ok button is clicked.
	 */
	open fun onOkClicked()
	{
		// Get the nav controller
		val navController = findNavController()

		// Item is set
		if (item != null)
		{
			saveFragmentItem()
		}
		// Media must be set
		else
		{
			// Create an bundle with the media
			val bundle = Bundle()
				.addMediaInfo(mediaPath, mediaArtist, mediaTitle, mediaType,
					shuffleMedia, recursivelyPlayMedia)

			// Save the result
			navController.previousBackStackEntry?.savedStateHandle?.set("YOYOYO", bundle)

			// Call the listener
			onOkClickedListener?.onOkClicked(bundle)
		}

		// Go back to the previous fragment
		navController.popBackStack()
	}

	/**
	 * Play audio from the media player.
	 *
	 * @param uri The Uri of the content to play.
	 */
	protected fun play(uri: Uri)
	{
		val path = uri.toString()

		// Invalid URI path since it does not start with "content://"
		if (!path.startsWith("content://"))
		{
			// Show an error toast
			quickToast(requireContext(), R.string.error_message_play_audio)
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
	 * Save the fragment item.
	 */
	protected abstract fun saveFragmentItem()

	/**
	 * Set the fragment item.
	 */
	abstract fun setFragmentItem(bundle: Bundle)

	/**
	 * Setup action buttons.
	 */
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
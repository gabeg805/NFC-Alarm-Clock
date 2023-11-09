package com.nfcalarmclock.mediapicker

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.nfcalarmclock.R
import com.nfcalarmclock.alarm.NacAlarmViewModel
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.mediaplayer.NacMediaPlayer
import com.nfcalarmclock.shared.NacSharedPreferences
import com.nfcalarmclock.util.NacBundle.getAlarm
import com.nfcalarmclock.util.NacBundle.getMedia
import com.nfcalarmclock.util.NacIntent.toIntent
import com.nfcalarmclock.util.NacUtility.toast

//import androidx.media2.player.MediaPlayer;
// TODO: Create the MediaPlayer object, and only call release (cleanup) in
// onDestroy.
/**
 * Media fragment for ringtones and music files.
 *
 * TODO: Make this class better
 */
open class NacMediaFragment
	: Fragment()
{

	/**
	 * Alarm.
	 */
	private var alarm: NacAlarm? = null

	/**
	 * Media path.
	 */
	private var mMediaPath: String? = null

	/**
	 * Media player.
	 */
	private var mediaPlayer: NacMediaPlayer? = null

	/**
	 * The initial selection flag, if this is the first time the fragment is being selected.
	 */
	private var isInitialSelection = true

	/**
	 * Cleanup the media player.
	 */
	private fun cleanupMediaPlayer()
	{
		// Release the media player resources
		if (mediaPlayer != null)
		{
			mediaPlayer!!.release()
		}

		// Null the media player
		mediaPlayer = null
	}

	/**
	 * The alarm media.
	 *
	 * Use the NacAlarm when editing an alarm card, and use the media path when
	 * editing a preference.
	 */
	protected var media: String?
		get() = mMediaPath
		set(media)
		{
			if (alarm != null)
			{
				alarm!!.setMedia(requireContext(), media!!)
			}
			else
			{
				mMediaPath = media
			}
		}

	/**
	 * The media path.
	 */
	protected val mediaPath: String
		get()
		{
			return alarm?.mediaPath ?: (media ?: "")
		}

	/**
	 * Check if the media path matches the given path.
	 *
	 * @return True if the media path matches the given path, and False
	 *         otherwise.
	 */
	protected fun isSelectedPath(path: String): Boolean
	{
		return mediaPath.isNotEmpty() && (mediaPath == path)
	}

	/**
	 * Called when the fragment is created.
	 */
	override fun onCreate(savedInstanceState: Bundle?)
	{
		// Super
		super.onCreate(savedInstanceState)

		// Set the member variables
		alarm = getAlarm(arguments)
		mMediaPath = getMedia(arguments)
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
	open fun onClearClicked()
	{
		// Clear the media that is being used
		media = ""

		// Reset the media player
		safeReset()
	}

	/**
	 * Called when the Ok button is clicked.
	 */
	open fun onOkClicked()
	{
		val activity = requireActivity()

		// Check if alarm is set
		if (alarm != null)
		{
			// Get the view model
			val viewModel = ViewModelProvider(activity)
				.get(NacAlarmViewModel::class.java)

			// Update the alarm for the activity
			viewModel.update(activity, alarm)
		}
		// Check if the media is set
		else if (media != null)
		{
			// Create an intent with the media
			val intent = toIntent(media)

			// Set the result of the activity with the media path as part of
			// the intent
			activity.setResult(Activity.RESULT_OK, intent)
		}

		// Finish the activity
		activity.finish()
	}

	/**
	 * Called when the fragment is paused.
	 */
	override fun onPause()
	{
		// Cleanup the media player
		cleanupMediaPlayer()

		// Super
		super.onPause()
	}

	/**
	 * Called when the fragment is selected by the user.
	 */
	open fun onSelected()
	{
		// Toggle the flag if this is the initial selection
		if (isInitialSelection)
		{
			isInitialSelection = false
		}
	}

	/**
	 * Called when the fragment is started.
	 */
	override fun onStart()
	{
		// Super
		super.onStart()

		// Setup the media player
		setupMediaPlayer()
	}

	/**
	 * Play audio from the media player safely.
	 *
	 * @param  uri  The Uri of the content to play.
	 */
	protected fun safePlay(uri: Uri): Boolean
	{
		val path = uri.toString()

		// Invalid URI path since it does not start with "content://"
		if (!path.startsWith("content://"))
		{
			return false
		}

		// Set the path of the media that is going to play
		media = path

		// Reset the media player
		safeReset()

		// Check if the media player is null
		if (mediaPlayer == null)
		{
			// Setup the media player
			setupMediaPlayer()
		}

		// Play the media
		mediaPlayer!!.playUri(uri)

		return true
	}

	/**
	 * Reset the media player safely.
	 */
	protected fun safeReset()
	{
		// Check if the media player is null
		if (mediaPlayer == null)
		{
			// Setup the media player
			setupMediaPlayer()
		}

		// Stop any media that is already playing
		mediaPlayer!!.mediaPlayer.stop()
	}

	/**
	 * Setup action buttons.
	 */
	protected fun setupActionButtons(root: View)
	{
		val shared = NacSharedPreferences(context)
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

	/**
	 * Setup the media player.
	 */
	private fun setupMediaPlayer()
	{
		// Get the context
		val context = requireContext()

		// Create the media player
		mediaPlayer = NacMediaPlayer(context)

		// Gain transient audio focus
		mediaPlayer!!.shouldGainTransientAudioFocus = true
	}

	/**
	 * Show an error message in a Toast indicating that audio was unable to be
	 * played.
	 */
	fun showErrorPlayingAudio()
	{
		val message = getString(R.string.error_message_play_audio)

		toast(requireContext(), message)
	}

}
package com.nfcalarmclock.mediapicker.music

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.util.UnstableApi
import com.nfcalarmclock.R
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.file.NacFile
import com.nfcalarmclock.filebrowser.NacFileBrowser
import com.nfcalarmclock.filebrowser.NacFileBrowser.OnBrowserClickedListener
import com.nfcalarmclock.media.NacMedia
import com.nfcalarmclock.mediapicker.NacMediaFragment
import com.nfcalarmclock.permission.readmediaaudio.NacReadMediaAudioPermission
import com.nfcalarmclock.util.NacBundle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * Display a browser for the user to browse for music files.
 */
class NacMusicFragment

	// Constructor
	: NacMediaFragment(),

	// Interfaces
	OnBrowserClickedListener
{

	/**
	 * Scroll view containing all the file browser contents.
	 */
	private var scrollView: ScrollView? = null

	/**
	 * Text view showing the current directory.
	 */
	private var directoryTextView: TextView? = null

	/**
	 * File browser.
	 */
	var fileBrowser: NacFileBrowser? = null
		private set

	/**
	 * Determine the starting directory and file name that should be selected.
	 */
	private fun getInitialFileBrowserLocation(): Pair<String, String>
	{
		val context = requireContext()
		var dir = ""
		var name = ""

		// Check if the media is a file
		if (NacMedia.isFile(context, mediaPath))
		{
			// Get the URI
			val uri = Uri.parse(mediaPath)

			// Set the directory and name
			dir = NacMedia.getRelativePath(context, uri)
			name = NacMedia.getName(context, uri)
		}
		// Check if the media is a directory
		else if (NacMedia.isDirectory(mediaPath))
		{
			dir = mediaPath
		}

		return Pair(dir, name)
	}

	/**
	 * Called when the Clear button is clicked.
	 */
	@UnstableApi
	override fun onClearClicked()
	{
		// Super
		super.onClearClicked()

		// De-select whatever is selected
		fileBrowser?.deselect(requireContext())
	}

	/**
	 * Called when the view is being created.
	 */
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?): View?
	{
		return inflater.inflate(R.layout.frg_music, container, false)
	}

	/**
	 * Called when a directory is clicked in the file browser.
	 */
	@UnstableApi
	override fun onDirectoryClicked(browser: NacFileBrowser, path: String)
	{
		val locale = Locale.getDefault()

		// Build the path to show in the directory text view
		val textPath = if (path.isNotEmpty())
		{
			String.format(locale, "${path}/")
		}
		else
		{
			""
		}

		// Set the alarm media path
		mediaPath = if(browser.previousDirectory.isNotEmpty())
		{
			String.format(locale, "${textPath}${browser.previousDirectory}")
		}
		else
		{
			path
		}

		// Set the text path
		directoryTextView!!.text = textPath

		// Show the contents of the directory
		browser.show(path)
	}

	/**
	 * Called when a directory is done being shown in the file browser.
	 */
	override fun onDoneShowing(browser: NacFileBrowser)
	{
		lifecycleScope.launch {

			// Delay a little bit
			delay(50)

			// Check if the previous directory was clicked
			if (browser.previousDirectory.isNotEmpty())
			{
				val context = requireContext()

				// Select the view
				browser.select(context, browser.previousDirectory)

				// Make sure the selected view has been set
				if (browser.selectedView != null)
				{
					// Get the location and offset of the view
					val loc = IntArray(2)
					val offset = 4 * directoryTextView!!.height

					browser.selectedView!!.getLocationOnScreen(loc)

					// Calculate the Y location
					val y = if (offset <= loc[1]) loc[1] - offset else 0

					// Scroll to the view's location
					scrollView?.scrollTo(0, y)
				}
			}
			else
			{
				// Scroll to the top
				scrollView?.fullScroll(View.FOCUS_UP)
			}

		}
	}

	/**
	 * Called when a file is clicked in the file browser.
	 */
	@UnstableApi
	override fun onFileClicked(browser: NacFileBrowser, metadata: NacFile.Metadata)
	{
		val uri = metadata.toExternalUri()

		// File was selected
		if (browser.isSelected)
		{
			 // Play the file
			 play(uri)
		}
		// File was deselected
		else
		{
			// Stop any media that is already playing
			mediaPlayer?.exoPlayer?.stop()
		}
	}

	/**
	 * Called when the Ok button is clicked.
	 */
	override fun onOkClicked()
	{
		// Check if the a directory was selected. If so, show a warning.
		// The path has already been set in onBrowserClicked() so nothing
		// further needs to be done
		if (NacMedia.isDirectory(mediaPath))
		{
			showWarningDirectorySelected(view)
			return
		}

		// Super
		super.onOkClicked()
	}

	/**
	 * Prompt the user to enable read permissions.
	 */
	override fun onSelected()
	{
	}

	/**
	 * Called after the view is created.
	 */
	@UnstableApi
	override fun onViewCreated(view: View, savedInstanceState: Bundle?)
	{
		// Setup the action buttons
		setupActionButtons(view)

		// Check if the user has read media permissions
		if (!NacReadMediaAudioPermission.hasPermission(requireContext()))
		{
			return
		}

		// Set the scrollview
		scrollView = view.findViewById(R.id.scrollview)

		// Set the textview with the directory path
		directoryTextView = view.findViewById(R.id.path)

		// Setup the file browser
		setupFileBrowser(view)
	}

	/**
	 * Setup the file browser.
	 */
	private fun setupFileBrowser(root: View)
	{
		// Create and set the file browser
		val container: LinearLayout = root.findViewById(R.id.container)
		fileBrowser = NacFileBrowser(this, container)

		// Directory to show in the textview and the ame of the file to select
		// in the file browser
		val (dir, name) = getInitialFileBrowserLocation()

		// Set the text with the path to the directory
		directoryTextView!!.text = dir

		// Setup the file browser
		fileBrowser!!.onBrowserClickedListener = this
		fileBrowser!!.show(dir) {

			// Select the item once it is done being shown
			fileBrowser!!.select(requireContext(), name)

		}
	}

	/**
	 * Show a warning indicating that a music directory was selected.
	 */
	private fun showWarningDirectorySelected(view: View?)
	{
		// Create the dialog
		val dialog = NacDirectorySelectedWarningDialog()

		// Setup the dialog
		dialog.defaultShouldShuffleMedia = shuffleMedia
		dialog.defaultShouldRecursivelyPlayMedia = recursivelyPlayMedia

		// Listener for when the user has confirmed that they want to select a directory
		dialog.onDirectoryConfirmedListener = NacDirectorySelectedWarningDialog.OnDirectoryConfirmedListener { shuffleMedia, recursivelyPlayMedia ->

			// Set the shuffle and recursive play media attributes
			this.shuffleMedia = shuffleMedia
			this.recursivelyPlayMedia = recursivelyPlayMedia

			// Emulate OK click
			super.onOkClicked()

		}

		// Show the dialog
		dialog.show(childFragmentManager, NacDirectorySelectedWarningDialog.TAG)
	}

	companion object
	{

		/**
		 * Read request callback success result.
		 */
		const val READ_REQUEST_CODE = 1

		/**
		 * Create a new instance of this fragment.
		 */
		fun newInstance(alarm: NacAlarm?): Fragment
		{
			val fragment: Fragment = NacMusicFragment()
			val bundle = NacBundle.alarmToBundle(alarm)
			fragment.arguments = bundle

			return fragment
		}

		/**
		 * Create a new instance of this fragment.
		 */
		fun newInstance(
			mediaPath: String,
			shuffleMedia: Boolean,
			recursivelyPlayMedia: Boolean
		): Fragment
		{
			val fragment: Fragment = NacMusicFragment()
			val bundle = NacBundle.mediaInfoToBundle(mediaPath, shuffleMedia,
				recursivelyPlayMedia)
			fragment.arguments = bundle

			return fragment
		}

	}

}
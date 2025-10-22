package com.nfcalarmclock.alarm.options.mediapicker.music

import android.content.ActivityNotFoundException
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.util.UnstableApi
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.nfcalarmclock.R
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.alarm.options.mediapicker.NacMediaPickerFragment
import com.nfcalarmclock.shared.NacSharedPreferences
import com.nfcalarmclock.system.file.NacFile
import com.nfcalarmclock.system.file.browser.NacFileBrowser
import com.nfcalarmclock.system.file.browser.NacFileBrowser.OnBrowserClickedListener
import com.nfcalarmclock.system.permission.readmediaaudio.NacReadMediaAudioPermission
import com.nfcalarmclock.view.quickToast
import com.nfcalarmclock.system.addMediaInfo
import com.nfcalarmclock.system.file.basename
import com.nfcalarmclock.system.getDeviceProtectedStorageContext
import com.nfcalarmclock.system.media.NacMedia
import com.nfcalarmclock.system.media.copyDocumentToDeviceEncryptedStorageAndCheckMetadata
import com.nfcalarmclock.system.media.directQueryMediaMetadata
import com.nfcalarmclock.system.media.doesDeviceHaveFreeSpace
import com.nfcalarmclock.system.media.getMediaName
import com.nfcalarmclock.system.media.getMediaRelativePath
import com.nfcalarmclock.system.media.isLocalMediaPath
import com.nfcalarmclock.system.media.isMediaDirectory
import com.nfcalarmclock.system.media.isMediaFile
import com.nfcalarmclock.system.toBundle
import com.nfcalarmclock.system.media.buildLocalMediaPath
import com.nfcalarmclock.system.media.getMediaArtist
import com.nfcalarmclock.system.media.getMediaTitle
import com.nfcalarmclock.view.setupThemeColor
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Display a browser for the user to browse for music files.
 */
@UnstableApi
class NacMusicPickerFragment

	// Constructor
	: NacMediaPickerFragment(),

	// Interfaces
	OnBrowserClickedListener
{

	/**
	 * File chooser content.
	 */
	@RequiresApi(Build.VERSION_CODES.Q)
	private val fileChooserContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->

		// Get the context
		val context = getDeviceProtectedStorageContext(requireContext())

		// Check if the uri is null
		if (uri == null)
		{
			return@registerForActivityResult
		}

		// Get the media uri
		var mediaUri = try
		{
			// Attempt to convert the document uri to a media uri
			MediaStore.getMediaUri(context, uri)
		}
		catch (_: IllegalArgumentException)
		{
			null
		}
		catch (_: IllegalStateException)
		{
			null
		}

		// Check if the media uri is invalid
		if (mediaUri == null)
		{
			// Check if there is enough free space
			if (!doesDeviceHaveFreeSpace(context))
			{
				quickToast(context, R.string.error_message_not_enough_free_space)
				return@registerForActivityResult
			}

			// Attempt to copy media to local files/ directory and then check the
			// metadata. This seems to only be necessary if the selected document is from
			// the Downloads/ directory, or if the file is not a media file
			mediaUri = copyDocumentToDeviceEncryptedStorageAndCheckMetadata(context, uri)

			// Final check if the media uri is invalid
			if (mediaUri == null)
			{
				quickToast(context, R.string.error_message_unable_to_get_media_information_from_file)
				return@registerForActivityResult
			}
		}

		// Set the media path of the selected file
		mediaPath = mediaUri.toString()

		// Select the file
		super.onOkClicked()

	}

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
	@OptIn(UnstableApi::class)
	private fun getInitialFileBrowserLocation(): Pair<String, String>
	{
		val context = getDeviceProtectedStorageContext(requireContext())
		var dir = ""
		var name = ""

		// Get the uri
		val uri = mediaPath.toUri()

		// Check if local media path
		if (uri.isLocalMediaPath(context))
		{
			// Do nothing
		}
		// File
		else if (uri.isMediaFile(context))
		{
			// Set the directory and name
			dir = uri.getMediaRelativePath(context)
			name = uri.getMediaName(context)
		}
		// Directory
		else if (uri.isMediaDirectory())
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
		// Build the path to show in the directory text view
		val textPath = if (path.isNotEmpty())
		{
			"${path}/"
		}
		else
		{
			""
		}

		// Set the alarm media path
		mediaPath = if(browser.previousDirectory.isNotEmpty())
		{
			"${textPath}${browser.previousDirectory}"
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
		// Get the media path as a uri
		val uri = mediaPath.toUri()

		// Check if the a directory was selected. If so, show a warning.
		// The path has already been set in onBrowserClicked() so nothing
		// further needs to be done
		if (uri.isMediaDirectory())
		{
			// Show the warning dialog
			showWarningDirectorySelected()

			// Set the artist, title, and type
			mediaArtist = ""
			mediaTitle = mediaPath.basename()
			mediaType = NacMedia.TYPE_DIRECTORY
			localMediaPath = ""

			return
		}

		// Get the activity and the device protected storage context
		val activity = requireActivity()
		val deviceContext = getDeviceProtectedStorageContext(activity)

		// Content uri
		if (uri.scheme == "content")
		{
			// Query the content for metadata
			mediaArtist = uri.getMediaArtist(deviceContext)
			mediaTitle = uri.getMediaTitle(deviceContext)
		}
		// File uri
		else
		{
			// Query the file for metadata
			val (artist, title) = uri.directQueryMediaMetadata()
			mediaArtist = artist
			mediaTitle = title
		}

		// Set the media information
		mediaType = NacMedia.TYPE_FILE
		localMediaPath = buildLocalMediaPath(deviceContext, mediaArtist, mediaTitle, mediaType)

		// Copy the file to device encrypted storage
		copyMediaToDeviceEncryptedStorage(deviceContext)

		// Super
		super.onOkClicked()
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
		setupFloatingActionButton(view)
	}

	/**
	 * Setup the file browser.
	 */
	private fun setupFileBrowser(root: View)
	{
		println("setupFileBrowser()")
		// Create and set the file browser
		val container: LinearLayout = root.findViewById(R.id.container)
		fileBrowser = NacFileBrowser(this, container)
		println("After creating the file browser")

		// Directory to show in the textview and the ame of the file to select
		// in the file browser
		val (dir, name) = getInitialFileBrowserLocation()

		// Set the text with the path to the directory
		directoryTextView!!.text = dir

		// Setup the file browser
		fileBrowser!!.onBrowserClickedListener = this
		println("Fragment show : $dir")
		fileBrowser!!.show(dir) {

			// Select the item once it is done being shown
			println("Fragment select : $name")
			fileBrowser!!.select(requireContext(), name)

		}
	}

	/**
	 * Setup the floating action button.
	 */
	private fun setupFloatingActionButton(root: View)
	{
		// Check if the Android version is correct
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q)
		{
			return
		}

		// Get the views
		val shared = NacSharedPreferences(requireContext())
		val fab: FloatingActionButton = root.findViewById(R.id.fab_launch_file_browser)

		// Setup the floating action button
		fab.visibility = View.VISIBLE
		fab.setupThemeColor(shared)

		// Set the click listener
		fab.setOnClickListener {

			try
			{
				// Launch the file chooser
				fileChooserContent.launch("audio/*")
			}
			catch (_: ActivityNotFoundException)
			{
				// Show error toast
				quickToast(requireContext(), R.string.error_message_unable_to_launch_media_picker)
			}

		}

		// Set the scroll listener
		scrollView?.setOnScrollChangeListener { _, _, scrollY, _, oldScrollY ->

			// Scrolling down
			if (scrollY >= oldScrollY)
			{
				fab.hide()
			}
			// Scrolling up
			else
			{
				fab.show()
			}
		}
	}

	/**
	 * Show a warning indicating that a music directory was selected.
	 */
	private fun showWarningDirectorySelected()
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
			// Create the fragment
			val fragment: Fragment = NacMusicPickerFragment()

			// Add the bundle to the fragment
			fragment.arguments = alarm?.toBundle() ?: Bundle()

			return fragment
		}

		/**
		 * Create a new instance of this fragment.
		 */
		fun newInstance(
			mediaPath: String,
			mediaArtist: String,
			mediaTitle: String,
			mediaType: Int,
			shuffleMedia: Boolean,
			recursivelyPlayMedia: Boolean
		): Fragment
		{
			// Create the fragment
			val fragment: Fragment = NacMusicPickerFragment()

			// Add the bundle to the fragment
			fragment.arguments = Bundle().addMediaInfo(mediaPath, mediaArtist, mediaTitle,
				mediaType, shuffleMedia, recursivelyPlayMedia)

			return fragment
		}

	}

}
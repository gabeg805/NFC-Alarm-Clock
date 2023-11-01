package com.nfcalarmclock.mediapicker.music

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.nfcalarmclock.R
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.file.NacFile
import com.nfcalarmclock.filebrowser.NacFileBrowser
import com.nfcalarmclock.filebrowser.NacFileBrowser.OnBrowserClickedListener
import com.nfcalarmclock.media.NacMedia
import com.nfcalarmclock.mediapicker.NacMediaFragment
import com.nfcalarmclock.permission.readmediaaudio.NacReadMediaAudioPermission
import com.nfcalarmclock.util.NacBundle
import java.util.Locale

/**
 * Display a browser for the user to browse for music files.
 */
class NacMusicFragment
	: NacMediaFragment(),
	OnBrowserClickedListener,
	NacDirectorySelectedWarningDialog.OnDirectoryConfirmedListener
{

	/**
	 * File browser.
	 */
	var fileBrowser: NacFileBrowser? = null
		private set

	/**
	 * Text view showing the current directory.
	 */
	private var directoryTextView: TextView? = null

	/**
	 * Called when the file browser is clicked.
	 */
	override fun onBrowserClicked(browser: NacFileBrowser,
		metadata: NacFile.Metadata, path: String, name: String)
	{
		// Directory was clicked
		if (metadata.isDirectory)
		{
			val locale = Locale.getDefault()
			val textPath = if (path.isEmpty()) "" else String.format(locale, "%1\$s/", path)

			// Set the alarm media path to the directory
			this.media = path
			directoryTextView!!.text = textPath

			// Show the contents of the directory
			browser.show(path)
		}
		// File was clicked
		else if (metadata.isFile)
		{
			val uri = metadata.toExternalUri()

			// Play the media file
			if (browser.isSelected)
			{
				// Unable to play the media
				if (!safePlay(uri))
				{
					// Show an error toast
					showErrorPlayingAudio()
				}
			}
			// File was deselected
			else
			{
				// Reset the media player
				safeReset()
			}
		}
	}

	/**
	 * Called when the Clear button is clicked.
	 */
	override fun onClearClicked()
	{
		// Super
		super.onClearClicked()

		// De-select whatever is selected
		fileBrowser?.deselect()
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
	 * Called when the view is being created.
	 */
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?): View?
	{
		return inflater.inflate(R.layout.frg_music, container, false)
	}

	/**
	 * Called when the user has confirmed that they want to select a directory.
	 */
	override fun onDirectoryConfirmed(view: View)
	{
		// Super
		super.onOkClicked()
		//super.onClick(view)
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
	override fun onViewCreated(view: View, savedInstanceState: Bundle?)
	{
		// Setup the action buttons
		setupActionButtons(view)

		// Check if the user has read media permissions
		if (!NacReadMediaAudioPermission.hasPermission(requireContext()))
		{
			return
		}

		// Setup the file browser
		setupFileBrowser(view)
	}

	/**
	 * Setup the file browser.
	 */
	private fun setupFileBrowser(root: View)
	{
		// Create and set the file browser
		fileBrowser = NacFileBrowser(this, root, R.id.container)

		// Set the textview with the directory path
		directoryTextView = root.findViewById(R.id.path)

		// Directory to show in the textview
		var dir: String? = ""

		// Name of the file to select in the file browser
		var name: String? = ""

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

		// Set the text with the path to the directory
		directoryTextView!!.text = dir

		// Setup the file browser
		fileBrowser!!.setOnBrowserClickedListener(this)
		fileBrowser!!.show(dir)
		fileBrowser!!.select(name)
	}

	/**
	 * Show a warning indicating that a music directory was selected.
	 */
	fun showWarningDirectorySelected(view: View?)
	{
		// Create the dialog
		val dialog = NacDirectorySelectedWarningDialog()

		// Setup the dialog
		dialog.selectedView = view
		dialog.onDirectoryConfirmedListener = this

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
		@JvmStatic
		fun newInstance(alarm: NacAlarm?): Fragment
		{
			val fragment: Fragment = NacMusicFragment()
			val bundle = NacBundle.toBundle(alarm)
			fragment.arguments = bundle

			return fragment
		}

		/**
		 * Create a new instance of this fragment.
		 */
		@JvmStatic
		fun newInstance(media: String?): Fragment
		{
			val fragment: Fragment = NacMusicFragment()
			val bundle = NacBundle.toBundle(media)
			fragment.arguments = bundle

			return fragment
		}

	}

}
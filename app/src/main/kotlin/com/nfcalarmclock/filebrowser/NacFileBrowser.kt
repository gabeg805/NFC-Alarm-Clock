package com.nfcalarmclock.filebrowser

import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.nfcalarmclock.R
import com.nfcalarmclock.file.NacFile
import kotlinx.coroutines.launch

/**
 * A file browser.
 */
class NacFileBrowser(

	/**
	 * Fragment.
	 */
	fragment: Fragment,

	/**
	 * File browser container.
	 */
	private val container: LinearLayout

	// Interface
) : View.OnClickListener
{

	/**
	 * Click listener for the file browser.
	 */
	interface OnBrowserClickedListener
	{
		fun onDirectoryClicked(browser: NacFileBrowser, path: String)
		fun onFileClicked(browser: NacFileBrowser, metadata: NacFile.Metadata)
		fun onDoneShowing(browser: NacFileBrowser)
	}

	/**
	 * View model for the file browser.
	 */
	private val viewModel: NacFileBrowserViewModel =
		ViewModelProvider(fragment)
			.get(NacFileBrowserViewModel::class.java)

	/**
	 * Currently selected view.
	 */
	var selectedView: RelativeLayout? = null
		private set

	/**
	 * The previous directory that was clicked. This is only populated when the
	 * (Previous directory) button is clicked, otherwise it will be an empty
	 * string.
	 */
	var previousDirectory: String = ""

	/**
	 * File browser on click listener.
	 */
	var onBrowserClickedListener: OnBrowserClickedListener? = null

	/**
	 * Check if at the root level of the file tree or not.
	 */
	val isAtRoot: Boolean
		get()
		{
			// Get the first child view in this container. If this fails, then
			// unable to get the first child at this level, so cannot determine
			// if at the root level or not
			val entry = container.getChildAt(0) ?: return false

			// Get the metadata of the first child
			val metadata = getFileMetadata(entry)

			// Ensure that metadata is in fact an object and it does not equal the
			// previous directory string ".."
			return (metadata != null) && (metadata.name != NacFile.PREVIOUS_DIRECTORY)
		}

	/**
	 * Check if something is selected.
	 */
	val isSelected: Boolean
		get() = selectedView != null

	/**
	 * Constructor.
	 */
	init
	{
		setupViewModelObserver(fragment)
	}

	/**
	 * Add a directory entry to the file browser.
	 *
	 * TODO Count number of songs in subdirectories and make that the annotation.
	 */
	private fun addDirectory(inflater: LayoutInflater, container: LinearLayout?,
		metadata: NacFile.Metadata): View
	{
		// Create the file entry view
		val entry = inflater.inflate(R.layout.nac_file_entry, container, false)
		val imageView = entry.findViewById<ImageView>(R.id.image)
		val titleView = entry.findViewById<TextView>(R.id.title)
		//TextView annotationView = entry.findViewById(R.id.annotation);

		// Set the image and text of the file entry
		imageView.setImageResource(R.drawable.folder)
		titleView.text = metadata.extra as String

		return entry
	}

	/**
	 * Add a music file entry to the file browser.
	 */
	private fun addFile(inflater: LayoutInflater, container: LinearLayout?,
		metadata: NacFile.Metadata): View
	{
		// Get the extra data (title, artist, duration)
		val extra = metadata.extra as Array<String>
		val title = extra[0]
		val artist = extra[1]
		val duration = extra[2]

		// Create the file entry view
		val entry = inflater.inflate(R.layout.nac_file_entry, container, false)
		val imageView = entry.findViewById<ImageView>(R.id.image)
		val titleView = entry.findViewById<TextView>(R.id.title)
		val subtitleView = entry.findViewById<TextView>(R.id.subtitle)
		val annotationView = entry.findViewById<TextView>(R.id.annotation)

		// Set the image and text of the file entry
		imageView.setImageResource(R.drawable.play)
		titleView.text = title
		subtitleView.text = artist
		subtitleView.visibility = View.VISIBLE
		annotationView.text = duration

		return entry
	}

	/**
	 * Change directory.
	 */
	private fun changeDirectory(metadata: NacFile.Metadata)
	{
		// Check if the previous directory button was clicked
		previousDirectory = if (metadata.name == NacFile.PREVIOUS_DIRECTORY)
		{
			val removeDots = metadata.path.replace(NacFile.PREVIOUS_DIRECTORY, "")
			val stripDots = NacFile.strip(removeDots)

			// Get the name of the directory
			NacFile.basename(stripDots)
		}
		else
		{
			// Empty string
			""
		}

		// Change directory
		val path = viewModel.cd(metadata)

		// Call the listener for when an item is clicked in the file browser
		onBrowserClickedListener?.onDirectoryClicked(this, path)
	}

	/**
	 * Clear all views in the file browser.
	 */
	fun clear()
	{
		// Clear all views from the file browser
		container.removeAllViews()
	}

	/**
	 * Deselect the currently selected item from the file browser.
	 */
	fun deselect(context: Context)
	{
		this.select(context, null as View?)
	}

	/**
	 * Deselect the desired view.
	 */
	private fun deselectView(context: Context, view: View?)
	{
		// Check if view is null
		if (view == null)
		{
			return
		}

		// Create a typed value
		val tv = TypedValue()

		// Resolve the background attribute
		context.theme.resolveAttribute(android.R.attr.selectableItemBackground, tv, true)

		// Check if the resource ID was set
		if (tv.resourceId != 0)
		{
			// Set the background resource
			view.setBackgroundResource(tv.resourceId)
		}
		// Resource ID was not set, so use something else
		else
		{
			// Set the background resource
			view.setBackgroundColor(tv.data)
		}
	}

	/**
	 * Get the file metadata object contained in the view.
	 *
	 * @return The file metadata object contained in the view.
	 */
	private fun getFileMetadata(view: View?): NacFile.Metadata?
	{
		// Unable to get the metadata object from the view
		return if (view == null)
		{
			null
		}
		// Get the metadata object via the tag of the view
		else
		{
			view.tag as NacFile.Metadata
		}
	}

	/**
	 * Check if the given path matches the currently selected path.
	 *
	 * @return True if the given path matches the currently selected path, and
	 *         False otherwise.
	 */
	private fun isSelected(path: String): Boolean
	{
		// Unable to determine if the file at the path is selected or not
		if (path.isEmpty())
		{
			return false
		}

		// Get the metadata
		val metadata = selectedView?.tag as NacFile.Metadata?

		// Check if the path of the metadata object of the selected view is
		// equal to the path that was passed in
		//
		// If the metadata object is null, then this will be faalse
		return metadata?.path == path
	}

	/**
	 * Called when a view is clicked.
	 */
	override fun onClick(view: View)
	{
		// Get the metadata from the view
		val metadata = view.tag as NacFile.Metadata

		// Unable to get a path from the view. Do not continue
		if (metadata.path.isEmpty())
		{
			return
		}
		// File
		else if (metadata.isFile)
		{
			toggleFile(view, metadata)
		}
		// Directory
		else if (metadata.isDirectory)
		{
			changeDirectory(metadata)
		}
	}

	/**
	 * Change directory to previous ("../") directory.
	 */
	fun previousDirectory()
	{
		// Get the first child view in this container or return if the
		// container does not have children
		val entry = container.getChildAt(0) ?: return

		// Get the metadata from the first child
		val metadata = entry.tag as NacFile.Metadata

		// Go to the previous directory if metadata equals the previous
		// directory string ".."
		if (metadata.name == NacFile.PREVIOUS_DIRECTORY)
		{
			// Simulate a click
			onClick(entry)
		}
	}

	/**
	 * @see .select
	 */
	fun select(context: Context, name: String)
	{
		// Check if name is empty
		if (name.isEmpty())
		{
			return
		}

		// Iterate over each child
		for (i in 0 until container.childCount)
		{
			// Get the child view
			val view = container.getChildAt(i)

			// Get the metadata of the child
			val metadata = view.tag as NacFile.Metadata

			// Check if the name's match
			if (metadata.name == name)
			{
				// Select the view
				select(context, view)
				return
			}
		}
	}

	/**
	 * Set the currently selected file.
	 *
	 * @param  view  The view to highlight.
	 */
	fun select(context: Context, view: View?)
	{
		// Deselect the currently selected view
		deselectView(context, selectedView)

		// Select the specified file
		selectView(context, view)

		// Set the new view as the currently selected view
		selectedView = view as RelativeLayout?
	}

	/**
	 * Select the desired view.
	 */
	private fun selectView(context: Context, view: View?)
	{
		// Get the color that represents a file is selected
		val color = ContextCompat.getColor(context, R.color.gray_light)

		// Set the background color of the selected view
		view?.setBackgroundColor(color)
	}

	/**
	 * Setup the view model observer.
	 */
	private fun setupViewModelObserver(lifecycleOwner: LifecycleOwner)
	{
		// Get the layout inflater
		val inflater = LayoutInflater.from(container.context)

		// Observe the view model data
		lifecycleOwner.lifecycleScope.launch {

			 viewModel.currentMetadata.collect { metadata ->

				  // Check if metadata is null
				  if (metadata == null)
				  {
					  // Clear the listing and then stop
					  clear()
					  return@collect
				  }

				  // Define an entry
				  val entry: View = if (metadata.isDirectory)
				  {
					  // Add a directory
					  addDirectory(inflater, container, metadata)
				  }
				  else if (metadata.isFile)
				  {
					  // Add a file
					  addFile(inflater, container, metadata)
				  }
				  else
				  {
					  // Entry is not defined so skip to the next item in the listing
					  return@collect
				  }

				  // Add metadata to the view and set the click listener
				  entry.tag = metadata
				  entry.setOnClickListener(this@NacFileBrowser)

				  // Add the entry to the file browser
				  container.addView(entry)

			 }

		}
	}

	/**
	 * Show the directory contents at the given path.
	 *
	 * @param  dir  The path of the directory to show.
	 */
	fun show(dir: String, unit: () -> Unit = {})
	{
		// Clear the listing
		viewModel.clear()

		// Show the listing at the new directory
		viewModel.show(dir) {

			// Call the unit
			unit()

			// Call the listener
			onBrowserClickedListener?.onDoneShowing(this)

		}
	}

	/**
	 * Toggle a file. If it is selected, deselect it, and if it is not
	 * selected, select it.
	 */
	private fun toggleFile(view: View, metadata: NacFile.Metadata)
	{
		// Get the context
		val context = view.context

		// The file is already selected
		if (isSelected(metadata.path))
		{
			// Deselect it
			deselect(context)
		}
		// Select the file
		else
		{
			select(context, view)
		}

		// Call the listener for when an item is clicked in the file browser
		onBrowserClickedListener?.onFileClicked(this, metadata)
	}

}
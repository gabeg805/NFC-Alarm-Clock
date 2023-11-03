package com.nfcalarmclock.filebrowser

import android.content.Context
import android.util.TypedValue
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.nfcalarmclock.R
import com.nfcalarmclock.file.NacFile

/**
 * A file browser.
 */
class NacFileBrowser(

	/**
	 * Life cycle owner.
	 */
	lifecycleOwner: LifecycleOwner,

	/**
	 * Root view.
	 */
	root: View,

	/**
	 * Group ID.
	 */
	groupId: Int

	// Interface
) : View.OnClickListener
{

	/**
	 * Click listener for the file browser.
	 */
	interface OnBrowserClickedListener
	{
		fun onDirectoryClicked(browser: NacFileBrowser, metadata: NacFile.Metadata,
			path: String)
		fun onFileClicked(browser: NacFileBrowser, metadata: NacFile.Metadata)
	}

	/**
	 * Context.
	 */
	private val context: Context = root.context

	/**
	 * The container view for the directory/file buttons.
	 */
	private val container: LinearLayout = root.findViewById(groupId)

	/**
	 * Currently selected view.
	 */
	private var selectedView: RelativeLayout? = null

	/**
	 * File browser on click listener.
	 */
	var onBrowserClickedListener: OnBrowserClickedListener? = null

	/**
	 * View model for the file browser.
	 */
	private val viewModel: NacFileBrowserViewModel =
		ViewModelProvider((context as ViewModelStoreOwner))
			.get(NacFileBrowserViewModel::class.java)

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
			return (metadata != null) && (metadata.name != "..")
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
		setupViewModelObserver(lifecycleOwner)
	}

	/**
	 * Change directory.
	 */
	private fun changeDirectory(metadata: NacFile.Metadata)
	{
		val tree = viewModel.repository.fileTree
		var path = metadata.path

		// Change directory to the directory that was clicked
		tree.cd(metadata.name)

		// Determine the path of the directory that was clicked
		path = if (metadata.name == "..")
		{
			tree.directoryPath
		}
		else
		{
			path
		}

		// Call the listener for when an item is clicked in the file browser
		onBrowserClickedListener?.onDirectoryClicked(this, metadata, path)
	}

	/**
	 * Deselect the currently selected item from the file browser.
	 */
	fun deselect()
	{
		this.select(null as View?)
	}

	/**
	 * Deselect the desired view.
	 */
	private fun deselectView(view: View?)
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
		if (metadata.name == "..")
		{
			// Simulate a click
			onClick(entry)
		}
	}

	/**
	 * @see .select
	 */
	fun select(name: String)
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
				select(view)
				return
			}
		}
	}

	/**
	 * Set the currently selected file.
	 *
	 * @param  view  The view to highlight.
	 */
	fun select(view: View?)
	{
		// Deselect the currently selected view
		deselectView(selectedView)

		// Select the specified file
		selectView(view)

		// Set the new view as the currently selected view
		selectedView = view as RelativeLayout?
	}

	/**
	 * Select the desired view.
	 */
	private fun selectView(view: View?)
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
		// Observe the view model data
		viewModel.listingLiveData.observe(lifecycleOwner) { listing: List<NacFile.Metadata> ->

			// Repopulate the list of views
			viewModel.repopulate(container, listing, this@NacFileBrowser)

		}
	}

	/**
	 * Show the directory contents at the given path.
	 *
	 * @param  dir  The path of the directory to show.
	 */
	fun show(dir: String?)
	{
		viewModel.show(dir)
	}

	/**
	 * Toggle a file. If it is selected, deselect it, and if it is not
	 * selected, select it.
	 */
	private fun toggleFile(view: View, metadata: NacFile.Metadata)
	{
		// The file is already selected
		if (isSelected(metadata.path))
		{
			// Deselect it
			deselect()
		}
		// Select the file
		else
		{
			select(view)
		}

		// Call the listener for when an item is clicked in the file browser
		onBrowserClickedListener?.onFileClicked(this, metadata)
	}

}
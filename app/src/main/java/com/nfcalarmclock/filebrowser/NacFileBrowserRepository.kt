package com.nfcalarmclock.filebrowser

import android.content.Context
import android.os.Handler
import androidx.lifecycle.MutableLiveData
import com.nfcalarmclock.R
import com.nfcalarmclock.file.NacFile
import com.nfcalarmclock.file.NacFileTree
import com.nfcalarmclock.media.NacMedia
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * File browser repository.
 */
class NacFileBrowserRepository(context: Context)
{

	/**
	 * File tree of media files.
	 */
	val fileTree: NacFileTree = NacFileTree("")

	/**
	 * Listing of file metadata.
	 */
	val listingLiveData: MutableLiveData<MutableList<NacFile.Metadata>> =
		MutableLiveData(ArrayList())

	/**
	 * Flag to check if is scanning the file tree or not.
	 */
	var isScanning: Boolean = true
		private set

	/**
	 * Flag to check if is show a file listing or not.
	 */
	var isShowing: Boolean = true
		private set

	/**
	 * Check if the repository is busy scanning or showing a file listing.
	 */
	val isBusy: Boolean
		get() = isScanning || isShowing

	/**
	 * Constructor.
	 */
	init
	{
		// Prepare a handler
		val looper = context.mainLooper
		val handler = Handler(looper)

		// Scan the file tree in the background
		handler.post { this.scan(context) }
	}

	/**
	 * Add a directory entry to the file listing.
	 *
	 * TODO Count number of songs in subdirectories and make that the
	 * annotation.
	 */
	fun addDirectory(context: Context, metadata: NacFile.Metadata)
	{
		// Determine what the extra data will be. This will be the name shown to the
		// user
		val extra = if (metadata.name == "..")
		{
			// Get the name of the previous folder
			val locale = Locale.getDefault()
			val previousFolder = context.getString(R.string.action_previous_folder)

			// Format the string
			String.format(locale, "(%1\$s)", previousFolder)
		}
		else
		{
			metadata.name
		}

		// Set the extra data
		metadata.extra = extra

		// Add to the listing
		listingLiveData.value!!.add(metadata)
	}

	/**
	 * Add a music file entry to the file listing.
	 */
	fun addFile(context: Context, metadata: NacFile.Metadata)
	{
		// Determine the extra data. This will be shown to the user
		val title = NacMedia.getTitle(context, metadata)
		val artist = NacMedia.getArtist(context, metadata)
		val duration = NacMedia.getDuration(context, metadata)

		// No title so there is nothing to show the user. Exit here
		if (title.isEmpty())
		{
			return
		}

		// Set the extra data
		metadata.extra = arrayOf(title, artist, duration)

		// Add to the listing
		listingLiveData.value!!.add(metadata)
	}

	/**
	 * Scan the file tree.
	 */
	fun scan(context: Context)
	{
		// Set scanning flag
		isScanning = true

		// Scan the file tree
		fileTree.scan(context)

		// Disable scanning flag
		isScanning = false
	}

	/**
	 * Show the contents of the file listing and tree.
	 */
	fun show(context: Context, path: String)
	{
		// Set the showing flag
		isShowing = true

		// Wait until scanning is complete
		while (isScanning)
		{
			try
			{
				TimeUnit.MILLISECONDS.sleep(50)
			}
			catch (ignored: InterruptedException)
			{
			}
		}

		val listing = listingLiveData.value!!

		// Clear the file listing
		listing.clear()

		// Not at the root level so add the previous directory to the listing.
		// Note: An empty path indicates the root level
		if (!path.isEmpty())
		{
			val metadata = NacFile.Metadata(path, "..", -1)

			addDirectory(context, metadata)
		}

		// Iterate over each file at the given path
		for (metadata in fileTree.lsSort(path))
		{

			// Add a directory
			if (metadata.isDirectory)
			{
				addDirectory(context, metadata)
			}
			// Add a file
			else if (metadata.isFile)
			{
				addFile(context, metadata)
			}
		}

		// Change directory to the new path
		fileTree.cd(path)

		// Notify observers of change
		listingLiveData.postValue(listing)

		// Disable the showing flag
		isShowing = false
	}

}
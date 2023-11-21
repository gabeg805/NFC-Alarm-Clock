package com.nfcalarmclock.filebrowser

import android.content.Context
import com.nfcalarmclock.R
import com.nfcalarmclock.file.NacFile
import com.nfcalarmclock.file.NacFileTree
import com.nfcalarmclock.media.NacMedia
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * File browser repository.
 */
class NacFileBrowserRepository
{

	/**
	 * File tree of media files.
	 */
	val fileTree: NacFileTree = NacFileTree("")

	/**
	 * Current metadata.
	 */
	private val _currentMetadata: MutableSharedFlow<NacFile.Metadata?> =
		MutableSharedFlow(replay = 1)

	// Public metadata that is not modifiable
	val currentMetadata: SharedFlow<NacFile.Metadata?> = _currentMetadata

	/**
	 * Flag to check if is scanning the file tree or not.
	 */
	private var isScanning: Boolean = true

	/**
	 * Add a directory entry to the file listing.
	 *
	 * TODO Count number of songs in subdirectories and make that the
	 * annotation.
	 */
	private suspend fun addDirectory(context: Context, metadata: NacFile.Metadata)
	{
		// Determine what the extra data will be. This will be the name shown to the
		// user
		val extra = if (metadata.name == "..")
		{
			// Get the name of the previous folder
			val locale = Locale.getDefault()
			val previousFolder = context.getString(R.string.action_previous_folder)

			// Format the string
			String.format(locale, "($previousFolder)")
		}
		else
		{
			metadata.name
		}

		// Set the extra data
		metadata.extra = extra

		// Add to the listing
		_currentMetadata.emit(metadata)
	}

	/**
	 * Add a music file entry to the file listing.
	 */
	private suspend fun addFile(context: Context, metadata: NacFile.Metadata)
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
		_currentMetadata.emit(metadata)
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
	 * Clear the file listing.
	 */
	suspend fun clear()
	{
		// Clear the file listing
		_currentMetadata.emit(null)
	}

	/**
	 * Show the contents of the file listing and tree.
	 */
	suspend fun show(context: Context, path: String)
	{
		// Wait until scanning is complete
		while (isScanning)
		{
			try
			{
				withContext(Dispatchers.IO) {
					TimeUnit.MILLISECONDS.sleep(50)
				}
			}
			catch (ignored: InterruptedException)
			{
			}
		}

		// Not at the root level so add the previous directory to the listing.
		// Note: An empty path indicates the root level
		if (path.isNotEmpty())
		{
			val metadata = NacFile.Metadata(path, NacFile.PREVIOUS_DIRECTORY)

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
	}

}
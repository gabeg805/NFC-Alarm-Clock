package com.nfcalarmclock.system.file.browser

import android.content.Context
import com.nfcalarmclock.R
import com.nfcalarmclock.system.file.NacFile
import com.nfcalarmclock.system.file.NacFileTree
import com.nfcalarmclock.system.media.getMediaDuration
import com.nfcalarmclock.system.media.getMediaArtist
import com.nfcalarmclock.system.media.getMediaTitle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.withContext

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
	 * Current metadata that is private so that it cannot be modified outside of this
	 * class.
	 */
	private val _currentMetadata: MutableSharedFlow<NacFile.Metadata?> =
		MutableSharedFlow(replay = 1)

	/**
	 * Current metadata (public).
	 */
	val currentMetadata: SharedFlow<NacFile.Metadata?> = _currentMetadata

	/**
	 * Flag to check if is scanning the file tree or not.
	 */
	private var isScanning: Boolean = true

	/**
	 * Add a directory entry to the file listing.
	 */
	private suspend fun addDirectory(context: Context, metadata: NacFile.Metadata)
	{
		// Determine what the extra data will be. This will be the name shown to the
		// user
		val extra = if (metadata.name == NacFile.PREVIOUS_DIRECTORY)
		{
			// Get the name of the previous folder
			val previousFolder = context.getString(R.string.action_previous_folder)

			// Format the string
			"($previousFolder)"
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
		withContext(Dispatchers.IO) {

			// Get the URI from the metadata object
			val uri = metadata.toExternalUri()

			// Determine the extra data. This will be shown to the user
			val title = uri.getMediaTitle(context)
			val artist = uri.getMediaArtist(context)
			val duration = uri.getMediaDuration(context)

			// No title so there is nothing to show the user. Exit here
			if (title.isEmpty())
			{
				return@withContext
			}

			// Set the extra data
			metadata.extra = arrayOf(title, artist, duration)

			// Add to the listing
			_currentMetadata.emit(metadata)

		}
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
	 * Scan the file tree.
	 */
	suspend fun scan(context: Context)
	{
		withContext(Dispatchers.IO)
		{
			// Set scanning flag
			isScanning = true

			// Scan the file tree
			fileTree.scan(context)

			// Disable scanning flag
			isScanning = false
		}
	}

	/**
	 * Show the contents of the file listing and tree.
	 */
	suspend fun show(context: Context, path: String)
	{
		withContext(Dispatchers.IO) {

			// Wait until scanning is complete
			while (isScanning)
			{
				try
				{
					delay(100)
				}
				catch (_: InterruptedException)
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

}
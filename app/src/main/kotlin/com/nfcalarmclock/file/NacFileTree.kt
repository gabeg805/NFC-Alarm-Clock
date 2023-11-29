package com.nfcalarmclock.file

import android.annotation.TargetApi
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore

/**
 * File tree of all media on the device.
 */
class NacFileTree(path: String)
	: NacFile.Tree(path)
{

	/**
	 * Get the names of the columns that will be returned from the query.
	 *
	 * @return The names of the columns that will be returned from the query.
	 */
	@get:TargetApi(Build.VERSION_CODES.Q)
	private val queryColumns: Array<String>
		get()
		{
			// Get the columns
			val idColumn = MediaStore.Audio.Media._ID
			var pathColumn = MediaStore.Audio.Media.DATA
			val nameColumn = MediaStore.Audio.Media.DISPLAY_NAME

			// Check if the correct API is being used
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
			{
				// Get a differnt name for the path column
				pathColumn = MediaStore.Audio.Media.RELATIVE_PATH
			}

			// Return the columns
			return arrayOf(idColumn, pathColumn, nameColumn)
		}

	/**
	 * Get the cursor that will be returned by the query.
	 *
	 * @param  context  The application context.
	 * @param  columns  Array of columns to return from the query.
	 *
	 * @return The cursor that will be returned by the query.
	 */
	private fun getQueryCursor(context: Context, columns: Array<String>): Cursor?
	{
		val sortOrder = "_display_name"
		var c: Cursor? = null

		// TODO: Iterate over all volumes? This would go into getContentUri() below
		//for (String v : MediaStore.getExternalVolumeNames(context))
		//{
		//	NacUtility.printf("Volume : %s", v);
		//}

		// Define which table, containing the collection of media, to query
		val collection: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
		{
			MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
		}
		else
		{
			MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
		}

		// Query the table and get the cursor
		try
		{
			c = context.contentResolver.query(collection, columns, null, null, sortOrder)
		}
		// Unable to query
		catch (e: IllegalArgumentException)
		{
			// Try to query again without sorting?
			try
			{
				c = context.contentResolver.query(collection, columns, null, null, null)
			}
			// Unable to query
			catch (ignored: IllegalArgumentException)
			{
			}
		}

		// Return the cursor
		return c
	}

	/**
	 * @see .scan
	 */
	fun scan(context: Context)
	{
		this.scan(context, false)
	}

	/**
	 * Scan the media table for available media to play, filtering by the
	 * current directory if specified, and create a file tree out of the
	 * output.
	 *
	 * @param  context  The application context.
	 * @param  filter   Whether the media files that are found should be filtered
	 * by comparing the media path with the current directory.
	 */
	@TargetApi(Build.VERSION_CODES.Q)
	fun scan(context: Context, filter: Boolean)
	{
		// Get the query cursor or return if unable to do so
		val c = getQueryCursor(context, queryColumns) ?: return

		// TODO: Shouldn't this be a NacFile object?
		val currentDir = this.directory
		val currentPath = NacFile.toRelativePath(this.directoryPath)

		// Get the column indices
		val idIndex = c.getColumnIndex(queryColumns[0])
		val pathIndex = c.getColumnIndex(queryColumns[1])
		val nameIndex = c.getColumnIndex(queryColumns[2])

		// Iterate over each scanned media file
		while (c.moveToNext())
		{
			// Get the values of each columns
			val id = c.getLong(idIndex)
			var path = NacFile.strip(c.getString(pathIndex))
			val name = c.getString(nameIndex)

			// Get the directory name from the path?
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q)
			{
				path = NacFile.toRelativeDirname(path)
			}

			// Filter out media that is not from the current path
			if (filter && currentPath != path)
			{
				continue
			}

			// Split up the path by directory
			val splitPath = path.replace(currentPath, "")
				.split("/".toRegex())
				.dropLastWhile { it.isEmpty() }
				.toTypedArray()

			// Iterate over each directory and add it to the file tree
			for (dir in splitPath)
			{
				this.add(dir)
				this.cd(dir)
			}

			// Add the current name and change to the current directory
			this.add(name, id)
			this.cd(currentDir)
		}

		// Close the cursor
		c.close()
	}

	companion object
	{

		/**
		 * Get a list of content Uris under the given path.
		 *
		 * Note: They are assumed to be external URIs
		 *
		 * @return A list of content Uris under the given path.
		 */
		fun getFiles(
			context: Context,
			filePath: String?,
			recursive: Boolean = false
		): List<Uri>?
		{
			// File path is empty
			if (filePath.isNullOrEmpty())
			{
				return null
			}

			// Create a file tree from the path
			val tree = NacFileTree(filePath)
			val mediaPaths: MutableList<Uri> = ArrayList()

			// Scan the tree
			tree.scan(context, true)

			// Get the files
			val allFiles = if (recursive)
			{
				tree.recursiveLs()
			}
			else
			{
				tree.lsSort()
			}

			// Iterate over each item found
			for (metadata in allFiles)
			{
				// Skip directories
				if (metadata.isDirectory)
				{
					continue
				}

				// Get the URI of the item
				val uri = metadata.toExternalUri()

				// Add the URI to the media path
				mediaPaths.add(uri)
			}

			// Return all found media paths
			return mediaPaths
		}

	}

}
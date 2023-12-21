package com.nfcalarmclock.file

import android.annotation.TargetApi
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.nfcalarmclock.file.NacFile.splitPath

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
	 * Get the proper directory of the media item.
	 */
	private fun getMediaDirectory(rawDirectory: String): String
	{
		// Strip out an extra slash at the end if it is there
		val directory = NacFile.strip(rawDirectory)

		// Remove main /storage... or /sdcard parts of the directory
		return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q)
		{
			NacFile.toRelativeDirname(directory)
		}
		// No need to remove that stuff from the directory
		else
		{
			directory
		}
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
	 * Scan the media table for available media to play, filtering by the
	 * current directory if specified, and create a file tree out of the
	 * output.
	 *
	 * @param context The application context.
	 */
	@TargetApi(Build.VERSION_CODES.Q)
	fun scan(context: Context)
	{
		// Get the query cursor or return if unable to do so
		val c = getQueryCursor(context, queryColumns) ?: return
		val origDirectory = directory
		val origPath = NacFile.toRelativePath(directoryPath)

		// Get the column indices
		val idIndex = c.getColumnIndex(queryColumns[0])
		val pathIndex = c.getColumnIndex(queryColumns[1])
		val nameIndex = c.getColumnIndex(queryColumns[2])

		// Change directory to top most root directory
		cd(this)

		// Iterate over each scanned media file
		while (c.moveToNext())
		{
			// Get the values of each columns
			val mediaId = c.getLong(idIndex)
			val mediaRawDirectory = c.getString(pathIndex) ?: continue
			val mediaDirectory = getMediaDirectory(mediaRawDirectory)
			val mediaName = c.getString(nameIndex) ?: continue

			// Check if the directory of the current media item matches the
			// original path. The media item directory will also count as
			// starting with the original path if it is empty
			if (!mediaDirectory.startsWith(origPath))
			{
				// Skip this item because the paths do not match
				continue
			}

			// Iterate over each directory in the path
			for (d in splitPath(mediaDirectory))
			{
				// Add the directory, and then change directory to the
				// newly added directory, so that we are now one level
				// deeper
				add(d)
				cd(d)
			}

			// Add the media name and ID to the current directory
			add(mediaName, mediaId)

			// Change directory to the top most root directory
			cd(this)
		}

		// Change directory back to the original directory
		cd(origDirectory)

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
		): List<Uri>
		{
			// File path is empty
			if (filePath.isNullOrEmpty())
			{
				return emptyList()
			}

			// Create a file tree from the path
			val tree = NacFileTree(filePath)

			// Scan the tree
			tree.scan(context)

			// Get the files
			val allFiles = if (recursive)
			{
				tree.recursiveLs()
			}
			else
			{
				tree.lsSort()
			}

			// Iterate over each file and filter to only get files, then map to
			// transform the Metadata object to an external URI
			return allFiles
				.filter { it.isFile }
				.map { it.toExternalUri() }
		}

	}

}
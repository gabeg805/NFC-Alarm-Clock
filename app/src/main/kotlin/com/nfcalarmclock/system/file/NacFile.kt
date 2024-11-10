package com.nfcalarmclock.system.file

import android.content.ContentUris
import android.net.Uri
import android.provider.MediaStore
import java.util.UnknownFormatConversionException

/**
 * Generic file object.
 */
object NacFile
{

	/**
	 * Name for the previous directory.
	 */
	const val PREVIOUS_DIRECTORY = ".."

	/**
	 * Get the basename of a file path.
	 *
	 * @return The basename of a file path.
	 */
	fun basename(path: String): String
	{
		// Check if the path is empty
		if (path.isEmpty())
		{
			return ""
		}

		// Split the path on "/"
		val items = path.split("/".toRegex())
			.dropLastWhile { it.isEmpty() }
			.toTypedArray()

		// Get the last item
		return if (items.isNotEmpty())
		{
			items[items.size - 1]
		}
		// Unable to determine the basename
		else
		{
			""
		}
	}

	/**
	 * @see .basename
	 */
	fun basename(uri: Uri): String
	{
		val path = uri.toString()

		return basename(path)
	}

	/**
	 * Get the dirname of a file path.
	 *
	 * @return The dirname of a file path.
	 */
	private fun dirname(path: String): String
	{
		// Check if the path is empty
		if (path.isEmpty())
		{
			return ""
		}

		// Get the basename
		val basename = basename(path)

		// Remove the basename from the path
		return path.substring(0, path.length - basename.length)
	}

	/**
	 * Split the path by the forward slash, "/", character.
	 */
	fun splitPath(path: String): Array<String>
	{
		return path.split("/".toRegex())
			.dropLastWhile { it.isEmpty() }
			.toTypedArray()
	}

	/**
	 * Strip away any trailing '/' characters.
	 */
	fun strip(path: String): String
	{
		// Check if path is empty
		if (path.isEmpty())
		{
			return ""
		}

		// Check if the last character is a slash
		if (path.last() == '/')
		{
			// Get everything except the last character
			return path.take(path.length-1)
		}

		// Return the path
		return path
	}

	/**
	 * Convert a path to a relative path's directory name
	 *
	 * @param  path  Path to a file or directory.
	 */
	fun toRelativeDirname(path: String?): String
	{
		// Get the relative path
		val relativePath = toRelativePath(path)

		// Get the directory name of the relative path
		val relativeDirname = dirname(relativePath)

		// Strip trailing slash character
		return strip(relativeDirname)
	}

	/**
	 * @see .toRelativeDirname
	 */
	fun toRelativeDirname(uri: Uri): String
	{
		// Get the path
		val path = uri.toString()

		// Convert it to a relative directory name
		return toRelativeDirname(path)
	}

	/**
	 * Convert a path to a relative path.
	 *
	 * @param  path  Path to a file or directory.
	 */
	fun toRelativePath(path: String?): String
	{
		// Check if path is empty
		if (path.isNullOrEmpty())
		{
			return ""
		}

		// Define fixed paths
		val emulated = "/storage/emulated/0"
		val sdcard = "/sdcard"
		//val sdcard = Environment.getExternalStorageDirectory().path

		// Initialize relative path
		var relativePath: String = path

		// Path starts with the emulated path
		if (path.startsWith(emulated))
		{
			// Remove the emulated path from the path
			relativePath = path.substring(emulated.length)
		}
		// Path starts with the sdcard path
		else if (path.startsWith(sdcard))
		{
			// Remove the sdcard path from the path
			relativePath = path.substring(sdcard.length)
		}

		// Check if the first character in the path is a slash
		if (relativePath.isNotEmpty() && relativePath.first() == '/')
		{
			// Remove the slash
			relativePath = relativePath.substring(1)
		}

		// Return the path
		return relativePath
	}

	/**
	 * Metadata of a file.
	 */
	class Metadata(directory: String, name: String, id: Long = -1)
	{

		/**
		 * Directory the file resides in.
		 */
		var directory: String = ""

		/**
		 * File name.
		 */
		var name: String = ""

		/**
		 * File ID.
		 */
		var id: Long = 0

		/**
		 * Extra object.
		 */
		var extra: Any? = null

		/**
		 * The file path.
		 */
		val path: String
			get()
			{
				// Directory is empty
				return if (directory.isEmpty())
				{
					// Return the name
					name
				}
				// Directory is present
				else
				{
					try
					{
						// Return the path with the directory and name
						"${directory}/${name}"
					}
					catch (e: UnknownFormatConversionException)
					{
						// Unable to convert the path so return an empty string
						""
					}
				}
			}

		/**
		 * Check if this is a directory.
		 */
		val isDirectory: Boolean
			get() = id == -1L

		/**
		 * Check if this is a file.
		 */
		val isFile: Boolean
			get() = id != -1L

		/**
		 * Constructor.
		 */
		init
		{
			this.directory = directory
			this.name = name
			this.id = id
		}

		/**
		 * Check if two Metadata objects are equal.
		 *
		 * @return True if the two objects are equal, and False otherwise.
		 */
		@Suppress("CovariantEquals")
		fun equals(metadata: Metadata) : Boolean
		{
			return (directory == metadata.directory)
				&& (name == metadata.name)
				&& (id == metadata.id)
				&& (extra == metadata.extra)
		}

		/**
		 * Convert the input to an external Uri.
		 */
		fun toExternalUri(): Uri
		{
			return ContentUris.withAppendedId(
				MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
		}

		///**
		// * Convert the input to an internal Uri.
		// */
		//fun toInternalUri(): Uri
		//{
		//	return ContentUris.withAppendedId(
		//		MediaStore.Audio.Media.INTERNAL_CONTENT_URI, id)
		//}

	}

	/**
	 * Organize files in a tree structure.
	 */
	open class Tree(path: String)
		: NacTreeNode<String>(path)
	{

		/**
		 * The current directory.
		 */
		@Suppress("LeakingThis")
		var directory: NacTreeNode<String> = this

		/**
		 * The path of the current directory.
		 */
		val directoryPath: String
			get()
			{
				return getPath(directory)
			}

		/**
		 * The home directory.
		 */
		@Suppress("MemberVisibilityCanBePrivate")
		protected val home: String
			get() = key

		init
		{
			// Clear the key
			key = ""

			// Iterate over each directory in the path
			for (d in splitPath(path))
			{
				// Add the directory and then change directory to it so that
				// each directory goes one level deeper
				add(d)
				cd(d)
			}
		}

		/**
		 * Add a file/folder to the given directory in the NacTree structure.
		 *
		 * @param  name  The name of the file or directory to add.
		 * @param  id    The content ID, used to create the content Uri.
		 *
		 * @see .add
		 */
		fun add(name: String, id: Long = -1)
		{
			// Name is empty so the key would not be unique
			if (name.isEmpty())
			{
				return
			}

			// Add the child
			directory.addChild(name, id)
		}

		/**
		 * Change directory to the given NacTree node.
		 */
		fun cd(dir: NacTreeNode<String>?)
		{
			// Set the directory or do nothing if null
			directory = dir ?: return
		}

		/**
		 * @see .cd
		 */
		fun cd(path: String)
		{
			val fromDir = directory
			val name = basename(path)

			// Previous directory
			if (name == PREVIOUS_DIRECTORY)
			{
				// cd to the previous directory
				cd(fromDir.root)
			}
			// Subdirectory
			else
			{

				// Remove the directory path from the path
				val newDir = path.replace(directoryPath, "")

				// Split the path on slash character(s)
				val splitPath = stripHome(newDir)
					.split("/".toRegex())
					.dropLastWhile { it.isEmpty() }
					.toTypedArray()

				// Set the initial directory to start with
				var toDir: NacTreeNode<String>? = fromDir

				// Iterate over each subdirectory
				for (d in splitPath)
				{
					// Check if the subdirectory is empty
					if (d.isEmpty())
					{
						continue
					}

					// Get the child of the directory
					// Break out of the loop if unable to get a child
					toDir = toDir?.getChild(d) ?: break

					// cd to the new directory, which corresponds to the child
					cd(toDir)
				}

			}
		}

		/**
		 * Get the path that leads to the desired node.
		 *
		 * @return The path that leads to the desired node.
		 */
		private fun getPath(node: NacTreeNode<String>): String
		{
			var ref: NacTreeNode<String>? = node
			var path = ""

			// Iterate over the directory and its parent directories
			while (ref != null)
			{

				// Check if the path is empty
				if (path.isEmpty())
				{
					// Set the path to the key, which I think is the name of
					// the file
					path = ref.key
				}
				// The key is not empty and (it is implied) that the path is
				// not empty
				else if (ref.key.isNotEmpty())
				{
					// Build the path out of the key and path. Traversing up so
					// the name goes before the path
					path = "${ref.key}/${path}"
				}

				// Go up to the parent directory
				ref = ref.root

			}

			// Return the path
			return path
		}

		/**
		 * Check if the path corresponds to the current directory.
		 *
		 * @return True if the path corresponds to the current directory, and
		 *         False otherwise.
		 */
		private fun isCurrentPath(path: String): Boolean
		{
			// Get the key of the path
			val pathKey = basename(path)

			// Check if path corresponds to the current directory
			return (pathKey == directory.key || path == home)
		}

		/**
		 * @see .ls
		 */
		private fun ls(): List<Metadata>
		{
			// Create an empty listing
			val listing: MutableList<Metadata> = ArrayList()

			// Iterate over each child
			for (child in directory.children)
			{
				// Create the metadata object
				val metadata = Metadata(directoryPath, child.key, child.value as Long)

				// Add it to the listing
				listing.add(metadata)
			}

			return listing
		}

		/**
		 * List the contents of the given path.
		 *
		 * @param  path  The path to list the contents of.
		 *
		 * @return The list of files/directories at the given path.
		 */
		private fun ls(path: String): List<Metadata>
		{
			// Path corresponds to the current directory
			return if (isCurrentPath(path))
			{
				// ls current directory and return it
				ls()
			}
			// Path corresponds to different directory
			else
			{
				// Save the original directory
				val origDir = directory

				// Change directory to the new path
				cd(path)

				// Create a new listing of the current directory
				val listing = ls()

				// Reset the directory back to the original
				directory = origDir

				// Return the listing
				listing

			}
		}

		/**
		 * @see .lsSort
		 */
		fun lsSort(): List<Metadata>
		{
			// Get the listing
			val listing = ls()

			// Return the sorted listing
			return sortListing(listing)
		}

		/**
		 * List the contents of the given path and sort the output.
		 *
		 * @param  path  The path to list the contents of.
		 *
		 * @return The sorted list of files/directories at the given path.
		 */
		fun lsSort(path: String): List<Metadata>
		{
			// Get the listing
			val listing = ls(path)

			// Return the sorted listing
			return sortListing(listing)
		}

		/**
		 * List the contents of the given path recursively so that
		 * subdirectories and their contents will also be included.
		 *
		 * @return The list of files/directories at the given path.
		 */
		fun recursiveLs(): List<Metadata>
		{
			return recursiveLs(directoryPath)
		}

		/**
		 * List the contents of the given path recursively so that
		 * subdirectories and their contents will also be included.
		 *
		 * @param  path  The path to list the contents of.
		 *
		 * @return The list of files/directories at the given path.
		 */
		private fun recursiveLs(path: String): List<Metadata>
		{
			val listing = lsSort(path).toMutableList()

			return recursiveLs(listing)
		}

		/**
		 * List the contents of the given path recursively so that
		 * subdirectories and their contents will also be included.
		 *
		 * @param listing A directory listing.
		 *
		 * @return The list of files/directories at the given path.
		 */
		private fun recursiveLs(listing: MutableList<Metadata>): List<Metadata>
		{
			// The offset of where to insert child listings into the main
			// listing
			var offset = 0

			// Iterate over each item in the listing
			for (index in listing.indices)
			{
				// Get the current metadata item
				val metadata = listing[index+offset]

				// Metadata is not a directory, so skip it
				if (!metadata.isDirectory)
				{
					continue
				}

				// Get the listing of the child
				val childListing = lsSort(metadata.path).toMutableList()

				// Recurse the listing of the child
				recursiveLs(childListing)

				// Add it to the listing
				listing.addAll(index+offset+1, childListing)

				// Calculate the new offset
				offset += childListing.size

			}

			return listing

		}

		/**
		 * Sort a listing of files.
		 *
		 * @param listing Listing of files.
		 */
		private fun sortListing(listing: List<Metadata>): List<Metadata>
		{
			val directories: MutableList<Metadata> = ArrayList()
			val files: MutableList<Metadata> = ArrayList()
			var list: MutableList<Metadata>

			// Iterate over the listing at the current path
			for (metadata in listing)
			{
				// Check the type of metadata
				list = if (metadata.isDirectory)
				{
					// Directory
					directories
				}
				else if (metadata.isFile)
				{
					// File
					files
				}
				else
				{
					// Unknown so skip it
					continue
				}

				val name = metadata.name
				var i = 0

				// Iterate over the file/directory list
				while (i < list.size)
				{
					// Check if the current name should be inserted before the
					// item at the current index
					if (name <= list[i].name)
					{
						break
					}

					i++
				}

				// Add the metadata of this file/directory to the list.
				// The list is generic and could be the file or the directory
				// but either way, it will be added to the correct onee
				list.add(i, metadata)
			}

			// Add all the files after the directories
			directories.addAll(files)

			// Return the sorted listing
			return directories
		}

		/**
		 * Strip the home directory away from a path.
		 *
		 * TODO: This method seems like I'm already doing it somewhere else
		 */
		@Suppress("MemberVisibilityCanBePrivate")
		protected fun stripHome(path: String): String
		{
			val reducedPath = path.replace(home, "")
			var strippedPath = strip(reducedPath)

			// Check if the stripped path is empty
			if (strippedPath.isEmpty())
			{
				// Return nothing
				return ""
			}

			// Check if the first character is a slah
			if (strippedPath.first() == '/')
			{
				// Get everything after the slash
				strippedPath = strippedPath.substring(1)
			}

			return strippedPath
		}

	}

}
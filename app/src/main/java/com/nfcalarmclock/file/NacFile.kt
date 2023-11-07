package com.nfcalarmclock.file

import android.content.ContentUris
import android.net.Uri
import android.provider.MediaStore
import java.util.Locale

/**
 * Generic file object.
 */
object NacFile
{

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
	@JvmStatic
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
	 * Strip away any trailing '/' characters.
	 */
	@JvmStatic
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
	@JvmStatic
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
	@JvmStatic
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

		companion object
		{

			/**
			 * Name for previous directory.
			 */
			const val PREVIOUS_DIRECTORY = ".."

		}

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
				val locale = Locale.getDefault()

				// Directory is empty
				return if (directory.isEmpty())
				{
					// Return the name
					name
				}
				// Directory is present
				else
				{
					// Return the path with the directory and name
					String.format(locale, "%1\$s/%2\$s", directory, name)
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
		: NacTreeNode<String>(null, path, null)
	{

		/**
		 * The current directory.
		 */
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
		private val home: String
			get() = key

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
			if (name == "..")
			{
				// cd to the previous directory
				this.cd(fromDir.root)
			}
			// Subdirectory
			else
			{

				// Remove the directory path from the path
				val newDir = path.replace(directoryPath, "")

				// Split the path on slash character(s)
				val splitPath = this.strip(newDir)
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
					this.cd(toDir)
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
			val locale = Locale.getDefault()
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
					path = String.format(locale, "%1\$s/%2\$s", ref.key, path)
				}

				// Go up to the parent directory
				ref = ref.root

			}

			// Return the path
			return path
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
		 * @return The list of files/directories at the given path.
		 *
		 * @param  path  The path to list the contents of.
		 */
		private fun ls(path: String): List<Metadata>
		{
			// Get the key of the path
			val pathKey = basename(path)

			// Path corresponds to the current directory
			return if (pathKey == directory.key || path == home)
			{
				// ls current directory and return it
				this.ls()
			}
			// Path corresponds to different directory
			else
			{

				val dir = directory
				var newDir: NacTreeNode<String>? = dir

				// TODO: Can't I just cd() to the path and then cd back to the orig directory?

				// Split the path on slash character(s)
				val splitPath = this.strip(path)
					.split("/".toRegex())
					.dropLastWhile { it.isEmpty() }
					.toTypedArray()

				// Iterate over the path
				for (d in splitPath)
				{
					// Directory is empty
					if (d.isEmpty())
					{
						continue
					}

					// Get the child of the directory
					// Break out of the loop if unable to get a child
					newDir = newDir?.getChild(d) ?: break

					// Set the new directory
					directory = newDir
				}

				// Create a new listing of the cuurrent directory
				val listing = this.ls()

				// Set the directory
				directory = dir

				// Return the listing
				listing

			}
		}

		/**
		 * @see .lsSort
		 */
		fun lsSort(): List<Metadata>
		{

			val directories: MutableList<Metadata> = ArrayList()
			val files: MutableList<Metadata> = ArrayList()
			var list: MutableList<Metadata>

			// Iterate over the listing at the current path
			for (metadata in this.ls())
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
		 * List the contents of the given path and sort the output.
		 *
		 * @return The sorted list of files/directories at the given path.
		 *
		 * @param  path  The path to list the contents of.
		 */
		fun lsSort(path: String): List<Metadata>
		{
			val directories: MutableList<Metadata> = ArrayList()
			val files: MutableList<Metadata> = ArrayList()
			var list: MutableList<Metadata>

			// Iterate over the listing at the path
			for (metadata in this.ls(path))
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
		private fun strip(path: String): String
		{
			val reducedPath = path.replace(home, "")
			var strippedPath = NacFile.strip(reducedPath)

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
package com.nfcalarmclock.media

import android.annotation.TargetApi
import android.content.Context
import android.database.Cursor
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.nfcalarmclock.R
import com.nfcalarmclock.file.NacFile
import com.nfcalarmclock.file.NacFile.basename
import com.nfcalarmclock.file.NacFile.strip
import com.nfcalarmclock.file.NacFile.toRelativeDirname
import com.nfcalarmclock.file.NacFileTree.Companion.getFiles
import java.util.Locale
import java.util.TreeMap
import java.util.concurrent.TimeUnit

/**
 * Media helper object.
 */
object NacMedia
{

	/**
	 * Type of sound for unintialized sound.
	 */
	const val TYPE_NONE = 0

	/**
	 * Type of sound for a ringtone.
	 */
	const val TYPE_RINGTONE = 1

	/**
	 * Type of sound for a music file.
	 */
	const val TYPE_FILE = 2

	/**
	 * Type of sound for a music file.
	 */
	const val TYPE_DIRECTORY = 5

	/**
	 * Build a media item from a file.
	 *
	 * @param  context  Application context.
	 * @param  uri  File URI.
	 */
	fun buildMediaItemFromFile(context: Context, uri: Uri): MediaItem
	{
		val path = uri.toString()

		// Get media information
		val artist = getArtist(context, uri)
		//val duration = getRawDuration(context, uri)
		val displayName = getName(context, uri)
		val title = getTitle(context, uri)

		// Build metadata
		val metadata = MediaMetadata.Builder()
			.setArtist(artist)
			.setDisplayTitle(displayName)
			.setIsPlayable(true)
			.setTitle(title)
			.build()
		//.putLong(MediaMetadata.METADATA_KEY_DURATION, duration)
		//.putLong(MediaMetadata.METADATA_KEY_NUM_TRACKS)

		// Build the media item
		return MediaItem.Builder()
			.setMediaId(path)
			.setMediaMetadata(metadata)
			.setUri(uri)
			.build()
	}

	/**
	 * Build a list of media items from a directory path.
	 *
	 * @param context Application context.
	 * @param path Path of a directory.
	 * @param recursive Whether to recursively search a directory or not.
	 */
	fun buildMediaItemsFromDirectory(
		context: Context,
		path: String,
		recursive: Boolean = false
	): List<MediaItem>
	{
		// Get all the files in the directory
		println("Build media item : $recursive")
		val files = getFiles(context, path, recursive = recursive)
		println("DONE")

		// Build list of media items and return it
		return buildMediaItemsFromFiles(context, files)
	}

	/**
	 * Build media item from a list of files.
	 *
	 * @param  context  Application context.
	 * @param  uris  List of files.
	 */
	private fun buildMediaItemsFromFiles(context: Context, uris: List<Uri>): List<MediaItem>
	{
		// Create empty list of media items
		val mediaItems: MutableList<MediaItem> = ArrayList()

		// Create a media item from each file
		for (u in uris)
		{
			// Create a media item from a URI
			val m = buildMediaItemFromFile(context, u)

			// Add the item to the list
			mediaItems.add(m)
		}

		return mediaItems
	}

	/**
	 * Get the name of the artist.
	 *
	 * @return The name of the artist.
	 */
	fun getArtist(context: Context, uri: Uri): String
	{
		// Get the artist from the column
		val column = MediaStore.Audio.Artists.ARTIST
		var artist = getColumnFromCursor(context, uri, column)

		// Unable to determine artist
		if (artist.isEmpty() || artist == "<unknown>")
		{
			// Get string to show that the artist is unknown
			artist = context.getString(R.string.state_unknown)
		}

		return artist
	}

	/**
	 * @see .getArtist
	 */
	fun getArtist(context: Context, metadata: NacFile.Metadata): String
	{
		// Get the URI from the metadata object
		val uri = metadata.toExternalUri()

		// Get the artist
		return getArtist(context, uri)
	}

	/**
	 * Get the requested column in the cursor object.
	 *
	 * @return The requested column in the cursor object.
	 */
	private fun getColumnFromCursor(context: Context, uri: Uri, column: String): String
	{
		val queryColumns = arrayOf(column)
		val resolver = context.contentResolver
		var value = ""

		// Attempt to get the content resolver and cursor
		val c: Cursor? = try
		{
			resolver.query(uri, queryColumns, null, null, null)
		}
		// Something happened. Last time this occured, it said
		// "Volume external_primary not found"
		catch (e: IllegalArgumentException)
		{
			e.printStackTrace()
			return value
		}
		// Security exception
		catch (e: SecurityException)
		{
			e.printStackTrace()
			return value
		}

		// Check if cursor is null
		if (c == null)
		{
			return value
		}
		// Unable to move to first item in cursor
		else if (!c.moveToFirst())
		{
			// Close the cursor
			c.close()

			return value
		}

		// Find the index of the string and get the string from the cursor
		try
		{
			// Get the value from the column
			val index = c.getColumnIndexOrThrow(column)
			value = c.getString(index) ?: ""

		}
		// Something happened, unable to get the string
		catch (e: IllegalArgumentException)
		{
			println("NacMedia : getColumnFromCursor : IllegalArgumentException!")
			e.printStackTrace()
		}

		// ANR could be due to having to load lots of files?
		c.close()

		return value
	}

	/**
	 * Get the duration of the track.
	 *
	 * @return The duration of the track.
	 */
	@TargetApi(Build.VERSION_CODES.Q)
	fun getDuration(context: Context, uri: Uri): String
	{
		// Check if API is too old or does not start with "content://"
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ||
			!uri.toString().startsWith("content://"))
		{
			return ""
		}

		// Get the duration from the column
		val column = MediaStore.Audio.Media.DURATION
		val duration = getColumnFromCursor(context, uri, column)

		// Parse the duration
		return parseDuration(duration)
	}

	/**
	 * @see .getDuration
	 */
	fun getDuration(context: Context, metadata: NacFile.Metadata): String
	{
		// Get the URI from the metadata object
		val uri = metadata.toExternalUri()

		// Get the duration
		return getDuration(context, uri)
	}

	/**
	 * Get the name of the file.
	 *
	 * @return The name of the file.
	 */
	fun getName(context: Context, uri: Uri): String
	{
		// Check if the URI does not start with "content://"
		if (!uri.toString().startsWith("content://"))
		{
			// Return the basename of the URI
			return basename(uri)
		}

		// Get the name from the column
		val column = MediaStore.Audio.Media.DISPLAY_NAME

		return getColumnFromCursor(context, uri, column)
	}

	/**
	 * Get the duration of the track.
	 *
	 * @return The duration of the track.
	 */
	@TargetApi(Build.VERSION_CODES.Q)
	fun getRawDuration(context: Context, uri: Uri): Long
	{
		// Check if the API is too old or does not start with "content://"
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ||
			!uri.toString().startsWith("content://"))
		{
			return -1
		}

		// Get the duration from the column
		val column = MediaStore.Audio.Media.DURATION
		val duration = getColumnFromCursor(context, uri, column)

		// Convert the duration to a number
		return if (duration.isNotEmpty())
		{
			duration.toLong()
		}
		else
		{
			0
		}
	}

	/**
	 * Get the relative path.
	 *
	 * @return The relative path.
	 */
	@TargetApi(Build.VERSION_CODES.Q)
	fun getRelativePath(context: Context, uri: Uri): String
	{
		// Check if the URI does not start with "content://"
		if (!uri.toString().startsWith("content://"))
		{
			// Convert the URI to a relative directory name
			return toRelativeDirname(uri)
		}

		// Check if can query relative path
		val canQueryRelativePath = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

		// Get the appropriate relative path column
		val column = if (canQueryRelativePath)
		{
			MediaStore.Audio.Media.RELATIVE_PATH
		}
		else
		{
			MediaStore.Audio.Media.DATA
		}

		// Get the relative path from the column
		var path = getColumnFromCursor(context, uri, column)

		// Check if unable to query relative path
		if (!canQueryRelativePath)
		{
			// Conver the path to a directory name
			path = toRelativeDirname(path)
		}

		return strip(path)
	}

	/**
	 * Get all alarm ringtones on the device.
	 *
	 * @param  context  The application context.
	 *
	 * @return All alarm ringtones on the device
	 */
	fun getRingtones(context: Context): TreeMap<String, String>
	{
		val locale = Locale.getDefault()
		val ringtones = TreeMap<String, String>()

		// Get the cursor for the ringtones or return an empty tree map
		val c = getRingtonesCursor(context) ?: return ringtones

		// Iterate over the cursor
		while (c.moveToNext())
		{
			// Get attributes of the media
			val title = c.getString(RingtoneManager.TITLE_COLUMN_INDEX)
			val id = c.getString(RingtoneManager.ID_COLUMN_INDEX)
			val dir = c.getString(RingtoneManager.URI_COLUMN_INDEX)

			// Build the path
			val path = String.format(locale, "%1\$s/%2\$s", dir, id)

			// Check if the ringtone already contains the title
			if (ringtones.containsKey(title))
			{
				// Skip
				continue
			}

			// Add the path to the tree map
			ringtones[title] = path
		}

		// Close the cursor
		c.close()

		return ringtones
	}

	/**
	 * Get the cursor for the alarm ringtones.
	 *
	 * @return The cursor for the alarm ringtones.
	 */
	fun getRingtonesCursor(context: Context): Cursor?
	{
		// Setup the ringtone manager
		val ringtoneManager = RingtoneManager(context)

		ringtoneManager.setType(RingtoneManager.TYPE_ALARM)

		// Get the cursor from the ringtone manager
		return try
		{
			ringtoneManager.cursor
		}
		catch (ignored: IllegalArgumentException)
		{
			null
		}
		catch (ignored: NullPointerException)
		{
			null
		}
	}

	/**
	 * Get the title of the track.
	 *
	 * @return The title of the track.
	 */
	fun getTitle(context: Context, uri: Uri): String
	{
		// Check if the URI does not start with "content://"
		if (!uri.toString().startsWith("content://"))
		{
			// Get the basename of the URI
			return basename(uri)
		}

		// Get the title from the column
		val column = MediaStore.Audio.Media.TITLE
		var title = getColumnFromCursor(context, uri, column)

		// Unable to determine the title
		if (title.isEmpty() || title == "<unknown>")
		{
			// Get string to show that the title is unknown
			title = context.getString(R.string.state_unknown)
		}

		return title
	}

	/**
	 * @see .getTitle
	 */
	fun getTitle(context: Context, metadata: NacFile.Metadata): String
	{
		// Get the URI from the metadata object
		val uri = metadata.toExternalUri()

		// Get the title
		return getTitle(context, uri)
	}

	/**
	 * @see .getTitle
	 */
	@JvmStatic
	fun getTitle(context: Context, path: String?): String
	{
		// Get the URI from the path
		val uri = Uri.parse(path)

		// Get the title
		return getTitle(context, uri)
	}

	/**
	 * Get the sound type.
	 *
	 * @return The sound type.
	 */
	fun getType(context: Context, path: String): Int
	{
		return if (isNone(path))
		{
			TYPE_NONE
		}
		else if (isFile(context, path))
		{
			TYPE_FILE
		}
		else if (isRingtone(context, path))
		{
			TYPE_RINGTONE
		}
		else if (isDirectory(path))
		{
			TYPE_DIRECTORY
		}
		else
		{
			TYPE_NONE
		}
	}

	/**
	 * Get the volume name.
	 *
	 * @return The volume name.
	 */
	@TargetApi(Build.VERSION_CODES.Q)
	fun getVolumeName(context: Context, uri: Uri): String
	{
		// Check if the URI does not start with "content://"
		if (!uri.toString().startsWith("content://"))
		{
			return ""
		}

		// Check if the API is too old
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q)
		{
			// Parse the name of the volume
			return parseVolumeName(uri)
		}

		// Get the name of the volume from the column
		val column = MediaStore.Audio.Media.VOLUME_NAME

		return getColumnFromCursor(context, uri, column)
	}

	/**
	 * @see .getVolumeName
	 */
	fun getVolumeName(context: Context, path: String?): String
	{
		// Get the URI from the path
		val uri = Uri.parse(path)

		// Get the name of the volume
		return getVolumeName(context, uri)
	}

	/**
	 * Check if the given type represents a directory.
	 *
	 * @param  type  The type to check
	 *
	 * @return True if the given type represents a directory, and False otherwise.
	 */
	@JvmStatic
	fun isDirectory(type: Int): Boolean
	{
		return type == TYPE_DIRECTORY
	}

	/**
	 * @return True if the given path is a directory, and False otherwise.
	 *
	 * @param  path  The path to check.
	 */
	fun isDirectory(path: String): Boolean
	{
		return path.isNotEmpty() && !path.startsWith("content://")
	}

	/**
	 * @return True if the given type represents a file, and False otherwise.
	 *
	 * @param  type  The type to check
	 */
	fun isFile(type: Int): Boolean
	{
		return type == TYPE_FILE
	}

	/**
	 * @return True if the given path is a file, and False otherwise.
	 *
	 * @param  context  The application context.
	 * @param  path     The path to check.
	 */
	fun isFile(context: Context, path: String?): Boolean
	{
		val uri = Uri.parse(path)
		val volumeName = getVolumeName(context, path)
		val relativePath = getRelativePath(context, uri)

		//return ((volumeName != null) && volumeName.startsWith("external")
		return volumeName.isNotEmpty() && relativePath.isNotEmpty()
	}

	/**
	 * Check if the given type represents an empty path.
	 *
	 * @param  type  The type to check.
	 *
	 * @return True if the given type represents an empty path, and False otherwise.
	 */
	fun isNone(type: Int): Boolean
	{
		return type == TYPE_NONE
	}

	/**
	 * Check if the given path is empty.
	 *
	 * @param  path  The path to check.
	 *
	 * @return True if the given path is empty, and False otherwise.
	 */
	fun isNone(path: String?): Boolean
	{
		return path.isNullOrEmpty()
	}

	/**
	 * Check if the given type corresponds to a ringtone.
	 */
	fun isRingtone(type: Int): Boolean
	{
		return type == TYPE_RINGTONE
	}

	/**
	 * Check if the given path is to a ringtone.
	 *
	 * @param  context  The application context.
	 * @param  path     The path of the ringtone to check.
	 *
	 * @return True if the given path is to a ringtone, and False otherwise.
	 */
	fun isRingtone(context: Context, path: String?): Boolean
	{
		val uri = Uri.parse(path)
		val volumeName = getVolumeName(context, path)
		val relativePath = getRelativePath(context, uri)

		// Changed this when converting to Kotlin on 11/06/23
		//return volumeName == "internal" && relativePath == null
		return volumeName == "internal" && relativePath.isEmpty()
	}

	/**
	 * Parse the duration string returned from the MediaStore query.
	 */
	fun parseDuration(millis: String): String
	{
		// Check if an empty string was provided
		if (millis.isEmpty())
		{
			return ""
		}

		// Get the locale
		val locale = Locale.getDefault()

		return try
		{
			// Convert the string to a long
			val value = millis.toLong()

			// Get the constituent hours/minutes/seconds
			val rounded = (value + 500) / 1000
			val hours = TimeUnit.SECONDS.toHours(rounded) % 24
			val minutes = TimeUnit.SECONDS.toMinutes(rounded) % 60
			val seconds = rounded % 60

			// Check if hours is 0
			if (hours == 0L)
			{
				String.format(locale, "%1$02d:%2$02d", minutes,
					seconds)
			}
			// Has hours
			else
			{
				String.format(locale, "%1$02d:%2$02d:%3$02d", hours,
					minutes, seconds)
			}
		}
		catch (e: NumberFormatException)
		{
			println("NacMedia : getDuration : NumberFormatException!")
			""
		}
	}

	/**
	 * @see .parseVolumeName
	 */
	fun parseVolumeName(uri: Uri): String
	{
		// Get the path from a URI
		val path = uri.toString()

		// Parse the name of the volume
		return parseVolumeName(path)
	}

	/**
	 * Parse the volume name from a path.
	 *
	 * This should only be done on any version before Q.
	 */
	fun parseVolumeName(contentPath: String): String
	{
		// Remove the prefix and split the path on forward slashes, "/"
		val contentPrefix = "content://"
		val items = contentPath.replace(contentPrefix, "")
			.split("/".toRegex())
			.dropLastWhile { it.isEmpty() }
			.toTypedArray()

		var index = 0

		// Check if no items in the path
		if (items.isEmpty())
		{
			return ""
		}

		// Check if the first index is empty
		if (items[0].isEmpty())
		{
			index++
		}

		// Check if the next index is equal to "media"
		if (items[index] == "media")
		{
			index++
		}

		// Return the last index
		return items[index]
	}

}
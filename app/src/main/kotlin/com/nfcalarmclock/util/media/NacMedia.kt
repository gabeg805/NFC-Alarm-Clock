package com.nfcalarmclock.util.media

import android.content.Context
import android.database.Cursor
import android.media.MediaMetadataRetriever
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.storage.StorageManager
import android.provider.MediaStore
import android.provider.OpenableColumns
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.nfcalarmclock.R
import com.nfcalarmclock.system.file.NacFile.basename
import com.nfcalarmclock.system.file.NacFile.strip
import com.nfcalarmclock.system.file.NacFile.toRelativeDirname
import com.nfcalarmclock.system.file.NacFileTree.Companion.getFiles
import com.nfcalarmclock.util.NacUtility.quickToast
import com.nfcalarmclock.system.getDeviceProtectedStorageContext
import com.nfcalarmclock.util.media.NacMedia.TYPE_DIRECTORY
import com.nfcalarmclock.util.media.NacMedia.TYPE_FILE
import com.nfcalarmclock.util.media.NacMedia.TYPE_NONE
import com.nfcalarmclock.util.media.NacMedia.TYPE_RINGTONE
import java.io.File
import java.io.FileNotFoundException
import java.util.Locale
import java.util.TreeMap
import java.util.concurrent.TimeUnit

/**
 * Copy the document to device encrypted storage so that the metadata can be checked and
 * verified that this is a media file.
 *
 * This must be done in case a document Uri is inaccessible by the app, or its
 * information is not found in the MediaStore table, in which case the metadata has to be
 * queried more directly.
 *
 * @return The Uri of the copied file, located in the local files/ directory, or null if
 *         it was unable to be copied or it was not a media file.
 */
fun copyDocumentToDeviceEncryptedStorageAndCheckMetadata(context: Context, documentUri: Uri): Uri?
{
	// Get the name of the document and its title (name without the file extension)
	val documentName = documentUri.getDocumentName(context)
	val documentTitle = File(documentName).nameWithoutExtension

	// Check if document does not have name
	if (documentName.isEmpty())
	{
		quickToast(context, R.string.error_message_unable_to_select_file)
		return null
	}

	// Copy the media to a temporary file
	val tmpName = "tmp"
	val tmpUri = copyMediaToDeviceEncryptedStorage(context, documentUri, tmpName)
	val tmpFile = File(tmpUri.toString())

	// Get metadata from file
	val (metadataArtist, metadataTitle, metadataHasAudio) = tmpFile.queryMediaMetadata()

	// Check if the selected file does not have audio
	if (metadataHasAudio?.isNotEmpty() != true)
	{
		// Delete temporary file
		tmpFile.delete()

		// Show error toast
		quickToast(context, R.string.error_message_unable_to_get_media_information_from_file)
		return null
	}

	// Get the local media file and path
	val artist = metadataArtist ?: ""
	val title = metadataTitle ?: documentTitle
	val localMediaPath = buildLocalMediaPath(context, artist, title, TYPE_FILE)
	val localMediaFile = File(localMediaPath)

	// Move temporary file to real file
	val status = tmpFile.renameTo(localMediaFile)

	// Delete temporary file
	tmpFile.delete()

	// Check rename status
	return if (status)
	{
		// Successful
		localMediaPath.toUri()
	}
	else
	{
		// Show error toast
		quickToast(context, R.string.error_message_unable_to_select_file)
		return null
	}
}

/**
 * Check if there is enough space on the device to create a file.
 *
 * https://developer.android.com/training/data-storage/app-specific#query-free-space
 *
 * @return True if there is enough space, and False otherwise.
 */
fun doesDeviceHaveFreeSpace(deviceContext: Context, bytesNeeded: Long = 1024*1024 *10L): Boolean
{
	// Check if correct build
	if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
	{
		return true
	}

	// Get the storage service
	val storageManager = deviceContext.getSystemService(Context.STORAGE_SERVICE) as StorageManager

	// Get the UUID of the device encrypted files/ directory
	val uuid = storageManager.getUuidForPath(deviceContext.filesDir)

	// Calculate the available bytes
	val availableBytes = storageManager.getAllocatableBytes(uuid)

	return (availableBytes >= bytesNeeded)
}

/**
 * Query a file for relavant media metadata.
 *
 * @return Media metadata.
 */
fun File.queryMediaMetadata(): Triple<String?, String?, String?>
{
	// Build the metadata retriever
	val metadataRetriever = MediaMetadataRetriever()
		.apply { setDataSource(this@queryMediaMetadata.path) }

	// Get metadata from file
	val artist = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
	val title = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
	val hasAudio = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_AUDIO)

	return Triple(artist, title, hasAudio)
}

/**
 * Directly query for relavant media metadata.
 *
 * @return Media metadata.
 */
fun Uri.directQueryMediaMetadata(): Pair<String, String>
{
	// Query the file for metadata
	val file = File(this.toString())
	val (artist, title, _) = file.queryMediaMetadata()

	return Pair(
		artist ?: "",
		title ?: file.nameWithoutExtension)
}

/**
 * Get the name of a file from a document provider.
 *
 * @return The name of a file from a document provider.
 */
fun Uri.getDocumentName(context: Context): String
{
	// Query the content resolver for the name
	context.contentResolver.query(this, null, null, null, null)
		.use { cursor ->

			// Get the index of the name column
			val nameIndex = cursor?.getColumnIndex(OpenableColumns.DISPLAY_NAME) ?: return ""

			// Move to the first item
			cursor.moveToFirst()

			// Get the name
			return cursor.getString(nameIndex)

	}
}

/**
 * Build the local media path.
 *
 * @return The local media path.
 */
fun buildLocalMediaPath(
	context: Context,
	artist: String,
	title: String,
	type: Int
): String
{
	// Check if the type is a directory
	if (type.isMediaDirectory())
	{
		// Return an empty local media path
		return ""
	}

	// Get the device protected storage context and files directory to that storage area
	val deviceContext = getDeviceProtectedStorageContext(context)
	val directory = deviceContext.filesDir

	// Build the name of the file
	val name = if (artist.isNotEmpty()) "$artist - $title" else title

	// Build the path
	return "$directory/$name"
}

/**
 * Copy the media to the local files/ directory in device encrypted storage.
 *
 * This is used as a failsafe, in case the device gets rebooted and an alarm needs to be
 * run before the user can unlock their device. In this case, the device would be in
 * direct boot mode and special considerations need to take place so that the alarm is
 * run as expected.
 */
fun copyMediaToDeviceEncryptedStorage(
	context: Context,
	srcUri: Uri,
	dstName: String,
): Uri
{
	// Get the device protected storage context
	val deviceContext = getDeviceProtectedStorageContext(context)

	// Build the destination uri
	val dstUri = "${deviceContext.filesDir}/$dstName".toUri()

	try
	{
		// Copy the file to the local media path
		deviceContext.openFileOutput(dstName, Context.MODE_PRIVATE).use { fileOutput ->

			// Copy the file to the local file dir (for the app)
			context.contentResolver.openInputStream(srcUri).use { inputStream ->
				inputStream?.copyTo(fileOutput, 1024)
			}

		}
	}
	catch (_: FileNotFoundException)
	{
		// Unable to copy the file. Do nothing and hope the user does not happen to need
		// it. This has only happened so far when the filename was too long
	}

	return dstUri
}

/**
 * Copy the media to the local files/ directory in device encrypted storage.
 *
 * This is used as a failsafe, in case the device gets rebooted and an alarm needs to be
 * run before the user can unlock their device. In this case, the device would be in
 * direct boot mode and special considerations need to take place so that the alarm is
 * run as expected.
 */
fun copyMediaToDeviceEncryptedStorage(
	context: Context,
	path: String,
	artist: String,
	title: String,
	type: Int
)
{
	// Get the device protected storage context
	val deviceContext = getDeviceProtectedStorageContext(context)

	// Get the source and destination files
	val localMediaPath = buildLocalMediaPath(deviceContext, artist, title, type)
	val srcUri = path.toUri()
	val dstFile = File(localMediaPath)

	// Check if the file already exists or if the media is a type that should not be copied
	if (dstFile.exists() || (!type.isMediaFile() && !type.isMediaRingtone()))
	{
		// Do nothing
		return
	}

	// Copy the file
	copyMediaToDeviceEncryptedStorage(deviceContext, srcUri, dstFile.name)
}

/**
 * Find the first Uri that is valid in the local files/ directory.
 *
 * It must not match [localUri] as this is an invalid Uri.
 *
 * @return Uri of the first Uri that is valid in the local files/ directory, or null.
 */
fun findFirstValidLocalMedia(deviceContext: Context, localUri: Uri): Uri?
{
	val uri = deviceContext.filesDir.listFiles()
		?.map { it.path.toUri() }
		?.find { (it.compareTo(localUri) != 0) && it.isMediaValid(deviceContext) }

	return uri
}

/**
 * Parse the duration string returned from the MediaStore query.
 */
private fun parseMediaDuration(millis: String): String
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
	catch (_: NumberFormatException)
	{
		println("NacMedia : getDuration : NumberFormatException!")
		""
	}
}

/**
 * Get the name of the artist.
 *
 * @return The name of the artist.
 */
fun Uri.getMediaArtist(
	context: Context,
	default: String = ""
): String
{
	// Get the artist from the column
	val column = MediaStore.Audio.Artists.ARTIST
	var artist = this.queryColumn(context, column)

	// Unable to determine artist
	if (artist.isEmpty() || artist == "<unknown>")
	{
		// Get string to show that the artist is unknown
		artist = default
	}

	return artist
}

/**
 * Get the duration of the track.
 *
 * @return The duration of the track.
 */
fun Uri.getMediaDuration(context: Context): String
{
	// Check if API is too old or does not start with "content://"
	if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ||
		!this.toString().startsWith("content://"))
	{
		return ""
	}

	// Get the duration from the column
	val column = MediaStore.Audio.Media.DURATION
	val duration = this.queryColumn(context, column)

	// Parse the duration
	return parseMediaDuration(duration)
}

/**
 * Get the name of the file.
 *
 * @return The name of the file.
 */
fun Uri.getMediaName(context: Context): String
{
	// Check if the URI does not start with "content://"
	if (!this.toString().startsWith("content://"))
	{
		// Return the basename of the URI
		return basename(this)
	}

	// Get the name from the column
	val column = MediaStore.Audio.Media.DISPLAY_NAME

	return this.queryColumn(context, column)
}

/**
 * Get the relative path of the media.
 *
 * @return The relative path of the media.
 */
fun Uri.getMediaRelativePath(context: Context): String
{
	// Check if the URI does not start with "content://"
	if (!this.toString().startsWith("content://"))
	{
		// Convert the URI to a relative directory name
		return toRelativeDirname(this)
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
	var path = this.queryColumn(context, column)

	// Check if unable to query relative path
	if (!canQueryRelativePath)
	{
		// Conver the path to a directory name
		path = toRelativeDirname(path)
	}

	return strip(path)
}

/**
 * Get the title of the track.
 *
 * @return The title of the track.
 */
fun Uri.getMediaTitle(
	context: Context,
	default: String = context.getString(R.string.state_unknown)
): String
{
	// Check if the URI does not start with "content://"
	if (!this.toString().startsWith("content://"))
	{
		// Get the basename of the URI
		return basename(this)
	}

	// Get the title from the column
	val column = MediaStore.Audio.Media.TITLE
	var title = this.queryColumn(context, column)

	// Unable to determine the title
	if (title.isEmpty() || (title == "<unknown>"))
	{
		// Get string to show that the title is unknown
		title = default
	}

	return title
}

/**
 * Get the media type.
 *
 * @return The media type.
 */
fun Uri.getMediaType(context: Context): Int
{
	return if (this.isMediaNone())
	{
		TYPE_NONE
	}
	else if (this.isMediaFile(context))
	{
		TYPE_FILE
	}
	else if (this.isMediaRingtone(context))
	{
		TYPE_RINGTONE
	}
	else if (this.isMediaDirectory())
	{
		TYPE_DIRECTORY
	}
	else
	{
		TYPE_NONE
	}
}

/**
 * Get the volume name of the media.
 *
 * @return The volume name of the media.
 */
fun Uri.getMediaVolumeName(context: Context): String
{
	// Check if the URI does not start with "content://"
	if (!this.toString().startsWith("content://"))
	{
		return ""
	}

	// Check if the API is too old
	if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q)
	{
		// Parse the name of the volume
		return this.parseVolumeName()
	}

	// Get the name of the volume from the column
	val column = MediaStore.Audio.Media.VOLUME_NAME

	return this.queryColumn(context, column)
}

/**
 * Check if the Uri corresponds to a local media path.
 *
 * @return True if the Uri corresponds to a local media path.
 */
fun Uri.isLocalMediaPath(deviceContext: Context): Boolean
{
	return this.toString().startsWith(deviceContext.filesDir.path)
}

/**
 * Check if the Uri corresponds to a directory.
 *
 * @return True if the Uri is a directory, and False otherwise.
 */
fun Uri.isMediaDirectory(): Boolean
{
	// Get the uri as a string
	val string = this.toString()

	// Check the attributes
	return string.isNotEmpty() && !string.startsWith("content://")
}

/**
 * Check if the Uri corresponds to a file.
 *
 * @param  context  The application context.
 *
 * @return True if the Uri is a file, and False otherwise.
 */
fun Uri.isMediaFile(context: Context): Boolean
{
	// Get the volume name and relative path
	val volumeName = this.getMediaVolumeName(context)
	val relativePath = this.getMediaRelativePath(context)

	// Check the attributes
	//return ((volumeName != null) && volumeName.startsWith("external")
	return volumeName.isNotEmpty() && relativePath.isNotEmpty()
}

/**
 * Check if the Uri is empty.
 *
 * @return True if the Uri is empty, and False otherwise.
 */
private fun Uri.isMediaNone(): Boolean
{
	return this.toString().isEmpty()
}

/**
 * Check if the Uri corresponds to a ringtone.
 *
 * @param  context  The application context.
 *
 * @return True if the Uri is to a ringtone, and False otherwise.
 */
private fun Uri.isMediaRingtone(context: Context): Boolean
{
	// Get the volume name and relative path
	val volumeName = this.getMediaVolumeName(context)
	val relativePath = this.getMediaRelativePath(context)

	// Check the attributes
	return volumeName == "internal" && relativePath.isEmpty()
}

/**
 * Check if the media is valid.
 *
 * This will check the type, name, and artist. If all are none, then the media is
 * invalid.
 *
 * @return True if the media is valid, and False otherwise.
 */
fun Uri.isMediaValid(context: Context): Boolean
{
	// Check if the uri is empty
	if (this.isMediaNone())
	{
		return false
	}

	// Most media should have a name. Use this as a test
	val artist = this.getMediaArtist(context)
	val name = this.getMediaName(context)

	// Check the artist and name
	return artist.isNotEmpty() || name.isNotEmpty()
}

/**
 * Parse the volume name from a path.
 *
 * This should only be done on any version before Q.
 */
private fun Uri.parseVolumeName(): String
{
	// Get the path from a URI
	val path = this.toString()

	// Remove the prefix and split the path on forward slashes, "/"
	val contentPrefix = "content://"
	val items = path.replace(contentPrefix, "")
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

/**
 * Query the requested column from the content resolver.
 *
 * @return The requested column.
 */
private fun Uri.queryColumn(context: Context, column: String): String
{
	val queryColumns = arrayOf(column)
	val resolver = context.contentResolver
	var value = ""

	// Attempt to get the content resolver and cursor
	val c: Cursor? = try
	{
		resolver.query(this, queryColumns, null, null, null)
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
 * Check if the type corresponds to a directory.
 *
 * @return True if the type is a directory, and False otherwise.
 */
fun Int.isMediaDirectory(): Boolean
{
	return this == TYPE_DIRECTORY
}

/**
 * Check if the type corresponds to a file.
 *
 * @return True if the type is a file, and False otherwise.
 */
fun Int.isMediaFile(): Boolean
{
	return this == TYPE_FILE
}

/**
 * Check if the type corresponds to none.
 *
 * @return True if the type is none, and False otherwise.
 */
fun Int.isMediaNone(): Boolean
{
	return this == TYPE_NONE
}

/**
 * Check if the type corresponds to a ringtone.
 *
 * @return True if the type is a ringtone, and False otherwise.
 */
fun Int.isMediaRingtone(): Boolean
{
	return this == TYPE_RINGTONE
}

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
		val artist = uri.getMediaArtist(context)
		//val duration = getRawDuration(context, uri)
		val displayName = uri.getMediaName(context)
		val title = uri.getMediaTitle(context)

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
		val files = getFiles(context, path, recursive = recursive)

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
	 * Get all alarm ringtones on the device.
	 *
	 * @param  context  The application context.
	 *
	 * @return All alarm ringtones on the device
	 */
	fun getRingtones(context: Context): TreeMap<String, String>
	{
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

			// Check if the ringtone already contains the title
			if (ringtones.containsKey(title))
			{
				// Skip
				continue
			}

			// Add the path to the tree map
			ringtones[title] = "$dir/$id"
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
	private fun getRingtonesCursor(context: Context): Cursor?
	{
		// Setup the ringtone manager
		val ringtoneManager = RingtoneManager(context)

		ringtoneManager.setType(RingtoneManager.TYPE_ALARM)

		// Get the cursor from the ringtone manager
		return try
		{
			ringtoneManager.cursor
		}
		catch (_: IllegalArgumentException)
		{
			null
		}
		catch (_: NullPointerException)
		{
			null
		}
	}

}
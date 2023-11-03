package com.nfcalarmclock.media;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.MediaMetadata;
import com.nfcalarmclock.R;
import com.nfcalarmclock.file.NacFileTree;
import com.nfcalarmclock.file.NacFile;
import com.nfcalarmclock.util.NacUtility;
import java.lang.Long;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;

/**
 */
@SuppressWarnings("RedundantSuppression")
public class NacMedia
{

	/**
	 * Type of sound for unintialized sound.
	 */
	public static final int TYPE_NONE = 0;

	/**
	 * Type of sound for a ringtone.
	 */
	public static final int TYPE_RINGTONE = 1;

	/**
	 * Type of sound for a music file.
	 */
	public static final int TYPE_FILE = 2;

	/**
	 * Type of sound for spotify.
	 */
	public static final int TYPE_SPOTIFY = 3;

	/**
	 * Type of sound for a music file.
	 */
	public static final int TYPE_DIRECTORY = 5;

	/**
	 * Build a media item from a file.
	 *
	 * @param  context  Application context.
	 * @param  uri  File URI.
	 */
	public static MediaItem buildMediaItemFromFile(Context context, Uri uri)
	{
		String path = uri.toString();

		// Get media information
		String artist = NacMedia.getArtist(context, uri);
		long duration = NacMedia.getRawDuration(context, uri);
		String displayName = NacMedia.getName(context, uri);
		String title = NacMedia.getTitle(context, uri);

		// Build metadata
		MediaMetadata metadata = new MediaMetadata.Builder()
			.setArtist(artist)
			.setDisplayTitle(displayName)
			.setIsPlayable(true)
			.setTitle(title)
			.build();
			//.putLong(MediaMetadata.METADATA_KEY_DURATION, duration)
			//.putLong(MediaMetadata.METADATA_KEY_NUM_TRACKS)

		// Build the media item
		return new MediaItem.Builder()
			.setMediaId(path)
			.setMediaMetadata(metadata)
			.setUri(uri)
			.build();
	}

	/**
	 * Build a list of media items from a directory path.
	 *
	 * @param  context  Application context.
	 * @param  path  Path of a directory.
	 */
	public static List<MediaItem> buildMediaItemsFromDirectory(Context context,
		String path)
	{
		// Get all the files in the directory
		List<Uri> files = NacFileTree.getFiles(context, path);

		return NacMedia.buildMediaItemsFromFiles(context, files);
	}

	/**
	 * Build media item from a list of files.
	 *
	 * @param  context  Application context.
	 * @param  uris  List of files.
	 */
	public static List<MediaItem> buildMediaItemsFromFiles(Context context,
		List<Uri> uris)
	{
		List<MediaItem> mediaItems = new ArrayList<>();

		// Create a media item from each file
		for (Uri u : uris)
		{
			MediaItem m = NacMedia.buildMediaItemFromFile(context, u);
			mediaItems.add(m);
		}

		return mediaItems;
	}

	/**
	 * @return The name of the artist.
	 */
	public static String getArtist(Context context, Uri uri)
	{
		String column = MediaStore.Audio.Artists.ARTIST;
		String artist = NacMedia.getColumnFromCursor(context, uri, column);

		// Unable to determine artist
		if ((artist == null) || artist.isEmpty() || artist.equals("<unknown>"))
		{
			// Get string to show that the artist is unknown
			artist = context.getString(R.string.state_unknown);
		}

		return artist;
	}

	/**
	 * @see #getArtist(Context, Uri)
	 */
	public static String getArtist(Context context, NacFile.Metadata metadata)
	{
		Uri uri = metadata.toExternalUri();
		return NacMedia.getArtist(context, uri);
	}

	/**
	 * @see #getArtist(Context, Uri)
	 */
	@SuppressWarnings("unused")
	public static String getArtist(Context context, String path)
	{
		Uri uri = Uri.parse(path);
		return NacMedia.getArtist(context, uri);
	}

	/**
	 * @return The requested column in the cursor object.
	 */
	public static String getColumnFromCursor(Context context, Uri uri,
		String column)
	{
		String[] queryColumns = new String[] { column };
		ContentResolver resolver = context.getContentResolver();
		String value = "";
		Cursor c;

		// Attempt to get the content resolver and cursor
		try
		{
			c = resolver.query(uri, queryColumns, null, null, null);
		}
		// Something happened. Last time this occured, it said
		// "Volume external_primary not found"
		catch (IllegalArgumentException | SecurityException e)
		{
			e.printStackTrace();
			return value;
		}

		// Null cursor
		if (c == null)
		{
			return value;
		}
		// Empty cursor
		else if (!c.moveToFirst())
		{
			c.close();
			return value;
		}

		// Find the index of the string and get the string from the cursor
		try
		{
			int index = c.getColumnIndexOrThrow(column);
			value = c.getString(index);

			// Check if the returned string is null
			if (value == null)
			{
				// Set it to an empty string for simplicity
				value = "";
			}
		}
		// Something happened, unable to get the string
		catch (IllegalArgumentException e)
		{
			NacUtility.printf("NacMedia : getColumnFromCursor : IllegalArgumentException!");
			e.printStackTrace();
		}

		// ANR could be due to having to load lots of files?
		c.close();
		return value;
	}

	/**
	 * @return The duration of the track.
	 */
	@TargetApi(Build.VERSION_CODES.Q)
	public static String getDuration(Context context, Uri uri)
	{
		if ((Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) ||
			!uri.toString().startsWith("content://"))
		{
			return "";
		}

		String column = MediaStore.Audio.Media.DURATION;
		String duration = NacMedia.getColumnFromCursor(context, uri, column);

		return NacMedia.parseDuration(duration);
	}

	/**
	 * @see #getDuration(Context, Uri)
	 */
	public static String getDuration(Context context, NacFile.Metadata metadata)
	{
		Uri uri = metadata.toExternalUri();
		return NacMedia.getDuration(context, uri);
	}

	/**
	 * @see #getDuration(Context, Uri)
	 */
	@SuppressWarnings("unused")
	public static String getDuration(Context context, String path)
	{
		Uri uri = Uri.parse(path);
		return NacMedia.getDuration(context, uri);
	}

	/**
	 * @return The name of the file.
	 */
	public static String getName(Context context, Uri uri)
	{
		if (!uri.toString().startsWith("content://"))
		{
			return NacFile.basename(uri);
		}

		String column = MediaStore.Audio.Media.DISPLAY_NAME;
		return NacMedia.getColumnFromCursor(context, uri, column);
	}

	/**
	 * @see #getName(Context, Uri)
	 */
	@SuppressWarnings("unused")
	public static String getName(Context context, NacFile.Metadata metadata)
	{
		Uri uri = metadata.toExternalUri();
		return NacMedia.getName(context, uri);
	}

	/**
	 * @see #getName(Context, Uri)
	 */
	@SuppressWarnings("unused")
	public static String getName(Context context, String path)
	{
		Uri uri = Uri.parse(path);
		return NacMedia.getName(context, uri);
	}

	/**
	 * @return The duration of the track.
	 */
	@TargetApi(Build.VERSION_CODES.Q)
	public static long getRawDuration(Context context, Uri uri)
	{
		if ((Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) ||
			!uri.toString().startsWith("content://"))
		{
			return -1;
		}

		String column = MediaStore.Audio.Media.DURATION;
		String duration = NacMedia.getColumnFromCursor(context, uri, column);

		return (duration != null) && !duration.isEmpty() ? Long.parseLong(duration) : 0;
	}

	/**
	 * @return The relative path.
	 */
	@SuppressWarnings("deprecation")
	@TargetApi(Build.VERSION_CODES.Q)
	public static String getRelativePath(Context context, Uri uri)
	{
		if (!uri.toString().startsWith("content://"))
		{
			return NacFile.toRelativeDirname(uri);
		}

		boolean canQueryRelativePath =
			(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q);
		String column = canQueryRelativePath ?
			MediaStore.Audio.Media.RELATIVE_PATH :
			MediaStore.Audio.Media.DATA;
		String path = NacMedia.getColumnFromCursor(context, uri, column);

		if (!canQueryRelativePath)
		{
			path = NacFile.toRelativeDirname(path);
		}

		return NacFile.strip(path);
	}

	/**
	 * @see #getRelativePath(Context, Uri)
	 */
	@SuppressWarnings("unused")
	public static String getRelativePath(Context context,
		NacFile.Metadata metadata)
	{
		Uri uri = metadata.toExternalUri();
		return NacMedia.getRelativePath(context, uri);
	}

	/**
	 * @see #getRelativePath(Context, Uri)
	 */
	@SuppressWarnings("unused")
	public static String getRelativePath(Context context, String path)
	{
		Uri uri = Uri.parse(path);
		return NacMedia.getRelativePath(context, uri);
	}

	/**
	 * Get all alarm ringtones on the device.
	 *
	 * @param  context  The application context.
	 *
	 * @return All alarm ringtones on the device
	 */
	public static TreeMap<String,String> getRingtones(Context context)
	{
		TreeMap<String,String> ringtones = new TreeMap<>();
		Locale locale = Locale.getDefault();
		Cursor c = NacMedia.getRingtonesCursor(context);

		if (c == null)
		{
			return ringtones;
		}

		while (c.moveToNext())
		{
			String title = c.getString(RingtoneManager.TITLE_COLUMN_INDEX);
			String id = c.getString(RingtoneManager.ID_COLUMN_INDEX);
			String dir = c.getString(RingtoneManager.URI_COLUMN_INDEX);
			String path = String.format(locale, "%1$s/%2$s", dir, id);

			if (ringtones.containsKey(title))
			{
				continue;
			}

			ringtones.put(title, path);
		}

		c.close();

		return ringtones;
	}

	/**
	 * Get the cursor for the alarm ringtones.
	 */
	public static Cursor getRingtonesCursor(Context context)
	{
		RingtoneManager manager = new RingtoneManager(context);
		Cursor c = null;

		manager.setType(RingtoneManager.TYPE_ALARM);

		try
		{
			c = manager.getCursor();
		}
		catch (IllegalArgumentException | NullPointerException ignored)
		{
		}

		return c;
	}

	/**
	 * @return The title of the track.
	 */
	public static String getTitle(Context context, Uri uri)
	{
		if (!uri.toString().startsWith("content://"))
		{
			return NacFile.basename(uri);
		}

		String column = MediaStore.Audio.Media.TITLE;
		String title = NacMedia.getColumnFromCursor(context, uri, column);

		// Unable to determine the title
		if ((title == null) || title.isEmpty() || title.equals("<unknown>"))
		{
			// Get string to show that the title is unknown
			title = context.getString(R.string.state_unknown);
		}

		return title;
	}

	/**
	 * @see #getTitle(Context, Uri)
	 */
	public static String getTitle(Context context, NacFile.Metadata metadata)
	{
		Uri uri = metadata.toExternalUri();
		return NacMedia.getTitle(context, uri);
	}

	/**
	 * @see #getTitle(Context, Uri)
	 */
	public static String getTitle(Context context, String path)
	{
		Uri uri = Uri.parse(path);
		return NacMedia.getTitle(context, uri);
	}

	/**
	 * @return The sound type.
	 */
	public static int getType(Context context, String path)
	{
		if (NacMedia.isNone(path))
		{
			return TYPE_NONE;
		}
		else if (NacMedia.isFile(context, path))
		{
			return TYPE_FILE;
		}
		else if (NacMedia.isRingtone(context, path))
		{
			return TYPE_RINGTONE;
		}
		else if (NacMedia.isDirectory(path))
		{
			return TYPE_DIRECTORY;
		}
		else if (NacMedia.isSpotify(path))
		{
			return TYPE_SPOTIFY;
		}
		else
		{
			return TYPE_NONE;
		}
	}

	/**
	 * @return The volume name.
	 */
	@TargetApi(Build.VERSION_CODES.Q)
	public static String getVolumeName(Context context, Uri uri)
	{
		// Combine these two checks
		if (!uri.toString().startsWith("content://"))
		{
			return "";
		}

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q)
		{
			return NacMedia.parseVolumeName(uri);
		}

		String column = MediaStore.Audio.Media.VOLUME_NAME;
		return NacMedia.getColumnFromCursor(context, uri, column);
	}

	/**
	 * @see #getVolumeName(Context, Uri)
	 */
	public static String getVolumeName(Context context, String path)
	{
		Uri uri = Uri.parse(path);
		return NacMedia.getVolumeName(context, uri);
	}

	/**
	 * @return True if the given type represents a directory, and False otherwise.
	 *
	 * @param  type  The type to check
	 */
	public static boolean isDirectory(int type)
	{
		return (type == TYPE_DIRECTORY);
	}

	/**
	 * @return True if the given path is a directory, and False otherwise.
	 *
	 * @param  path  The path to check.
	 */
	public static boolean isDirectory(String path)
	{
		return !path.isEmpty() && !path.startsWith("content://");
	}

	/**
	 * @return True if the given type represents a file, and False otherwise.
	 *
	 * @param  type  The type to check
	 */
	public static boolean isFile(int type)
	{
		return (type == TYPE_FILE);
	}

	/**
	 * @return True if the given path is a file, and False otherwise.
	 *
	 * @param  context  The application context.
	 * @param  path     The path to check.
	 */
	public static boolean isFile(Context context, String path)
	{
		Uri uri = Uri.parse(path);
		String volumeName = NacMedia.getVolumeName(context, path);
		String relativePath = NacMedia.getRelativePath(context, uri);

		//return ((volumeName != null) && volumeName.startsWith("external")
		return ((volumeName != null) && !volumeName.isEmpty()
			&& (relativePath != null) && !relativePath.isEmpty());
	}

	/**
	 * @return True if the given type represents an empty path, and False
	 *     otherwise.
	 *
	 * @param  type  The type to check.
	 */
	public static boolean isNone(int type)
	{
		return (type == TYPE_NONE);
	}

	/**
	 * @return True if the given path is empty, and False otherwise.
	 *
	 * @param  path  The path to check.
	 */
	public static boolean isNone(String path)
	{
		return ((path == null) || (path.isEmpty()));
	}

	/**
	 * Check if the given type corresponds to a ringtone.
	 */
	public static boolean isRingtone(int type)
	{
		return (type == TYPE_RINGTONE);
	}

	/**
	 * @return True if the given path is to a ringtone, and False otherwise.
	 *
	 * @param  context  The application context.
	 * @param  path     The path of the ringtone to check.
	 */
	public static boolean isRingtone(Context context, String path)
	{
		Uri uri = Uri.parse(path);
		String volumeName = NacMedia.getVolumeName(context, path);
		String relativePath = NacMedia.getRelativePath(context, uri);

		return (volumeName.equals("internal") && (relativePath == null));
	}

	/**
	 * @return True if the type corresponds to Spotify, and False otherwise.
	 *
	 * @param  type  The type to check.
	 */
	public static boolean isSpotify(int type)
	{
		return (type == TYPE_SPOTIFY);
	}

	/**
	 * @return True if the given path is to a ringtone, and False otherwise.
	 *
	 * @param  path  The path of the ringtone to check.
	 */
	public static boolean isSpotify(String path)
	{
		return path.startsWith("spotify");
	}

	/**
	 * Parse the duration string returned from the MediaStore query.
	 */
	public static String parseDuration(String millis)
	{
		if ((millis == null) || millis.isEmpty())
		{
			return "";
		}

		String duration = "";

		try
		{
			long value = Long.parseLong(millis);
			long rounded = (value+500) / 1000;
			long hours = TimeUnit.SECONDS.toHours(rounded) % 24;
			long minutes = TimeUnit.SECONDS.toMinutes(rounded) % 60;
			long seconds = rounded % 60;
			Locale locale = Locale.getDefault();

			if (hours == 0)
			{
				duration = String.format(locale, "%1$02d:%2$02d", minutes,
					seconds);
			}
			else
			{
				duration = String.format(locale, "%1$02d:%2$02d:%3$02d", hours,
					minutes, seconds);
			}
		}
		catch (NumberFormatException e)
		{
			NacUtility.printf("NacMedia : getDuration : NumberFormatException!");
		}

		return duration;
	}

	/**
	 * @see #parseVolumeName(String)
	 */
	public static String parseVolumeName(Uri uri)
	{
		String path = uri.toString();
		return NacMedia.parseVolumeName(path);
	}

	/**
	 * Parse the volume name from a path.
	 * <p>
	 * This should only be done on any version before Q.
	 */
	public static String parseVolumeName(String contentPath)
	{
		String contentPrefix = "content://";
		String[] items = contentPath.replace(contentPrefix, "").split("/");
		int index = 0;

		if (items.length == 0)
		{
			return "";
		}

		if (items[0].isEmpty())
		{
			index++;
		}

		if (items[index].equals("media"))
		{
			index++;
		}

		return items[index];
	}

}

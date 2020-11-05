package com.nfcalarmclock;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import java.lang.Long;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.TreeMap;
import java.util.List;
import java.util.Locale;

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
	 */
	public static class Tree
		extends NacFile.Tree
	{

		public Tree(String path)
		{
			super(path);
		}

		/**
		 * @see #scan(Context, boolean)
		 */
		public void scan(Context context)
		{
			this.scan(context, false);
		}

		/**
		 * Scan the media table for available media to play, filtering by the
		 * current directory if specified, and create a file tree out of the
		 * output.
		 *
		 * @param  context  The application context.
		 * @param  filter   Whether the media files that are found should be filtered
		 *     by comparing the media path with the current directory.
		 */
		@SuppressWarnings("deprecation")
		@TargetApi(Build.VERSION_CODES.Q)
		public void scan(Context context, boolean filter)
		{
			NacTreeNode<String> currentDir = this.getDirectory();
			String currentPath = NacFile.toRelativePath(this.getDirectoryPath());
			boolean canQueryRelativePath = NacMedia.canQueryRelativePath();
			Uri baseUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
			String[] columns = new String[] {
				MediaStore.Audio.Media._ID,
					canQueryRelativePath ?
						MediaStore.Audio.Media.RELATIVE_PATH :
						MediaStore.Audio.Media.DATA,
				MediaStore.Audio.Media.DISPLAY_NAME,
				};
			Cursor c = context.getContentResolver().query(baseUri, columns,
				null, null, "_display_name");
			int idIndex = c.getColumnIndex(columns[0]);
			int pathIndex = c.getColumnIndex(columns[1]);
			int nameIndex = c.getColumnIndex(columns[2]);

			while (c.moveToNext())
			{
				long id = c.getLong(idIndex);
				String path = NacFile.strip(c.getString(pathIndex));
				String name = c.getString(nameIndex);

				if (!canQueryRelativePath)
				{
					path = NacMedia.parseRelativePath(path);
				}

				if (filter && !currentPath.equals(path))
				{
					continue;
				}

				String[] splitPath = path.replace(currentPath, "").split("/");
				for (String dir : splitPath)
				{
					this.add(dir);
					this.cd(dir);
				}

				this.add(name, id);
				this.cd(currentDir);
			}

			c.close();
		}

	}

	/**
	 * @return True if the current build can query the media duration, and False
	 *     otherwise.
	 */
	public static boolean canQueryDuration(Uri uri)
	{
		return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
			&& NacMedia.canQueryUri(uri);
	}

	/**
	 * @return True if the current build can query the relative path, and False
	 *     otherwise.
	 */
	public static boolean canQueryRelativePath()
	{
		return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q);
	}

	/**
	 * @return True if can query the Uri, and False otherwise.
	 */
	public static boolean canQueryUri(Uri uri)
	{
		String path = uri.toString();
		return path.startsWith("content://");
	}

	/**
	 * @return True if the current build can query the volume name, and False
	 *     otherwise.
	 */
	public static boolean canQueryVolumeName()
	{
		return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q);
	}

	/**
	 * @return The requested column in the cursor object.
	 */
	public static String getColumnFromCursor(Context context, Uri uri,
		String column)
	{
		String[] queryColumns = new String[] { column };
		ContentResolver resolver = context.getContentResolver();
		Cursor c = resolver.query(uri, queryColumns, null, null, null);
		String value = "";

		if (c == null)
		{
			return value;
		}
		else if (!c.moveToFirst())
		{
			c.close();
			return value;
		}

		try
		{
			int index = c.getColumnIndexOrThrow(column);
			value = c.getString(index);
		}
		catch (IllegalArgumentException e)
		{
			NacUtility.printf("NacMedia : getColumnFromCursor : IllegalArgumentException!");
		}

		c.close();
		return value;
	}

	/**
	 * @return The name of the artist.
	 */
	public static String getArtist(Context context, Uri uri)
	{
		String column = MediaStore.Audio.Artists.ARTIST;
		String artist = NacMedia.getColumnFromCursor(context, uri, column);

		if (artist.isEmpty() || artist.equals("<unknown>"))
		{
			NacSharedConstants cons = new NacSharedConstants(context);
			artist = cons.getStateUnknown();
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
	 * @return The duration of the track.
	 */
	@TargetApi(Build.VERSION_CODES.Q)
	public static String getDuration(Context context, Uri uri)
	{
		if (!NacMedia.canQueryDuration(uri))
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
	 * @return A list of content Uris under the given path. They are assumed to
	 *         be external Uris.
	 */
	public static List<Uri> getFiles(Context context, String filePath)
	{
		if ((filePath == null) || (filePath.isEmpty()))
		{
			return null;
		}

		NacMedia.Tree tree = new NacMedia.Tree(filePath);
		List<Uri> mediaPaths = new ArrayList<>();

		tree.scan(context, true);

		for (NacFile.Metadata metadata : tree.lsSort())
		{
			if (metadata.isDirectory())
			{
				continue;
			}

			Uri uri = metadata.toExternalUri();
			mediaPaths.add(uri);
		}

		return mediaPaths;
	}

	/**
	 * @return The name of the file.
	 */
	public static String getName(Context context, Uri uri)
	{
		if (!NacMedia.canQueryUri(uri))
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
	 * @return The relative path.
	 */
	@SuppressWarnings("deprecation")
	@TargetApi(Build.VERSION_CODES.Q)
	public static String getRelativePath(Context context, Uri uri)
	{
		if (!NacMedia.canQueryUri(uri))
		{
			return NacMedia.parseRelativePath(uri);
		}

		boolean canQueryRelativePath = NacMedia.canQueryRelativePath();
		String column = canQueryRelativePath ?
			MediaStore.Audio.Media.RELATIVE_PATH :
			MediaStore.Audio.Media.DATA;
		String path = NacMedia.getColumnFromCursor(context, uri, column);

		if (!canQueryRelativePath)
		{
			path = NacMedia.parseRelativePath(path);
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
	 * @param  context  The application context.
	 *
	 * @return A list of alarm ringtones.
	 */
	public static TreeMap<String,String> getRingtones(Context context)
	{
		RingtoneManager manager = new RingtoneManager(context);
		TreeMap<String,String> ringtones = new TreeMap<>();

		manager.setType(RingtoneManager.TYPE_ALARM);

		Cursor c = manager.getCursor();
		Locale locale = Locale.getDefault();

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

		return ringtones;
	}

	/**
	 * @return The title of the track.
	 */
	public static String getTitle(Context context, Uri uri)
	{
		if (!NacMedia.canQueryUri(uri))
		{
			return NacFile.basename(uri);
		}

		String column = MediaStore.Audio.Media.TITLE;
		String title = NacMedia.getColumnFromCursor(context, uri, column);

		if (title.isEmpty() || title.equals("<unknown>"))
		{
			NacSharedConstants cons = new NacSharedConstants(context);
			title = cons.getStateUnknown();
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
		if (!NacMedia.canQueryUri(uri))
		{
			return "";
		}

		if (!NacMedia.canQueryVolumeName())
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
	 * @see #parseRelativePath(String)
	 */
	public static String parseRelativePath(Uri uri)
	{
		String path = uri.toString();
		return NacMedia.parseRelativePath(path);
	}

	/**
	 * Parse the relative path from a path retrieved by querying for
	 * MediaStore.Audio.Media.DATA.
	 *
	 * Note: This should only be done on any version before Q.
	 */
	public static String parseRelativePath(String filePath)
	{
		String relativeFilePath = NacFile.toRelativePath(filePath);
		String relativePath = NacFile.dirname(relativeFilePath);
		NacUtility.printf("File path         : %s", filePath);
		NacUtility.printf("Relative fle path : %s", relativeFilePath);
		NacUtility.printf("Relative path     : %s", relativePath);
		NacUtility.printf("");

		return NacFile.strip(relativePath);
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
	 *
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

	/**
	 * @return Convert a path to a Uri.
	 *
	 * @param  path  The path to convert.
	 */
	public static Uri toUri(String path)
	{
		return Uri.parse(path);
	}

}

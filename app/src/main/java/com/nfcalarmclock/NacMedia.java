package com.nfcalarmclock;

import android.annotation.TargetApi;
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
	 * Type of sound for a ringtone.
	 */
	public static final int TYPE_RINGTONE_RANDOM = 4;

	/**
	 * Type of sound for a music file.
	 */
	public static final int TYPE_DIRECTORY = 5;

	/**
	 * Path for a random ringtone.
	 */
	public static final String RANDOM_RINGTONE_PATH = "TYPE_RINGTONE_RANDOM";

	/**
	 * Name for a random ringtone.
	 */
	public static final String RANDOM_RINGTONE_NAME = "Ringtone Playlist";

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
		 * Scan the media table for available media to play, filtering by the
		 * current directory if specified, and create a file tree out of the
		 * output.
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

				String[] items = path.replace(currentPath, "").split("/");

				for (int i=0; i < items.length; i++)
				{
					String dir = items[i];

					this.add(dir);
					this.cd(dir);
				}

				this.add(name, id);
				this.cd(currentDir);
			}

			c.close();
		}

		/**
		 * @see scan
		 */
		public void scan(Context context)
		{
			this.scan(context, false);
		}

	}

	/**
	 * @return True if the current build can query the media duration, and False
	 *         otherwise.
	 */
	public static boolean canQueryDuration()
	{
		return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q);
	}

	/**
	 * @return True if the current build can query the relative path, and False
	 *         otherwise.
	 */
	public static boolean canQueryRelativePath()
	{
		return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q);
	}

	/**
	 * @return True if the current build can query the volume name, and False
	 *         otherwise.
	 */
	public static boolean canQueryVolumeName()
	{
		return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q);
	}

	/**
	 * @return The name of the artist.
	 */
	public static String getArtist(Context context, NacFile.Metadata metadata)
	{
		Uri contentUri = metadata.toExternalUri();
		String[] columns = new String[] { MediaStore.Audio.Artists.ARTIST };
		Cursor c = context.getContentResolver().query(contentUri, columns, null,
			null, null);
		String artist = "";

		c.moveToFirst();

		try
		{
			int artistIndex = c.getColumnIndexOrThrow(
				MediaStore.Audio.Artists.ARTIST);
			artist = c.getString(artistIndex);

			if ((artist == null) || artist.equals("<unknown>"))
			{
				NacSharedConstants cons = new NacSharedConstants(context);
				artist = cons.getStateUnknown();
			}
		}
		catch (IllegalArgumentException e)
		{
			NacUtility.printf("NacMedia : getArtist : IllegalArgumentException!");
		}

		c.close();

		return artist;
	}

	/**
	 * @return The duration of the track.
	 */
	@TargetApi(Build.VERSION_CODES.Q)
	public static String getDuration(Context context, Uri contentUri)
	{
		String contentPath = contentUri.toString();

		if (!NacMedia.canQueryDuration()
			|| !contentPath.startsWith("content://"))
		{
			return "";
		}

		String[] columns = new String[] { MediaStore.Audio.Media.DURATION };
		Cursor c = context.getContentResolver().query(contentUri, columns, null,
			null, null);
		String millis = "";

		c.moveToFirst();

		try
		{
			int durationIndex = c.getColumnIndexOrThrow(
				MediaStore.Audio.Media.DURATION);
			millis = c.getString(durationIndex);
		}
		catch (IllegalArgumentException e)
		{
			NacUtility.printf("NacMedia : getDuration : IllegalArgumentException!");
		}

		c.close();

		return NacMedia.parseDuration(millis);
	}

	/**
	 * @see getDuration
	 */
	public static String getDuration(Context context, NacFile.Metadata metadata)
	{
		Uri contentUri = metadata.toExternalUri();

		return NacMedia.getDuration(context, contentUri);
	}

	/**
	 * @see getDuration
	 */
	public static String getDuration(Context context, String path)
	{
		Uri contentUri = Uri.parse(path);

		return NacMedia.getDuration(context, contentUri);
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
	public static String getName(Context context, Uri contentUri)
	{
		String contentPath = contentUri.toString();

		if (!contentPath.startsWith("content://"))
		{
			return NacFile.basename(contentPath);
		}

		String[] columns = new String[] { MediaStore.Audio.Media.DISPLAY_NAME };
		Cursor c = context.getContentResolver().query(contentUri, columns, null,
			null, null);
		String name = "";

		c.moveToFirst();

		try
		{
			int nameIndex = c.getColumnIndexOrThrow(
				MediaStore.Audio.Media.DISPLAY_NAME);
			name = c.getString(nameIndex);
		}
		catch (IllegalArgumentException e)
		{
			NacUtility.printf("NacMedia : getName : IllegalArgumentException!");
		}

		c.close();

		return name;
	}

	/**
	 * @see getName
	 */
	public static String getName(Context context, NacFile.Metadata metadata)
	{
		Uri contentUri = metadata.toExternalUri();

		return NacMedia.getName(context, contentUri);
	}

	/**
	 * @see getName
	 */
	public static String getName(Context context, String path)
	{
		Uri contentUri = Uri.parse(path);

		return NacMedia.getName(context, contentUri);
	}

	/**
	 * Return the sound file filter.
	 */
	//public static FilenameFilter getFilter()
	//{
	//	return new FilenameFilter()
	//	{
	//		public boolean accept(File dir, String name)
	//		{
	//			Locale locale = Locale.getDefault();
	//			String lower = name.toLowerCase(locale);
	//			String[] extensions = { ".mp3", ".ogg" };
	//			//String[] extensions = {".3gp", ".mp4", ".m4a", ".aac",
	//			//	".ts", ".flac", ".mid", ".xmf", ".mxmf", ".rtttl",
	//			//	".rtx", ".ota", ".imy", ".mp3", ".mkv", ".wav" };

	//			if (dir.isDirectory())
	//			{
	//				return true;
	//			}

	//			for (String e : extensions)
	//			{
	//				if (lower.endsWith(e))
	//				{
	//					return true;
	//				}
	//			}

	//			return false;
	//		}
	//	};
	//}

	/**
	 * @return The relative path.
	 */
	@SuppressWarnings("deprecation")
	@TargetApi(Build.VERSION_CODES.Q)
	public static String getRelativePath(Context context, Uri contentUri)
	{
		String contentPath = contentUri.toString();
		boolean canQueryRelativePath = NacMedia.canQueryRelativePath();

		if (!contentPath.startsWith("content://"))
		{
			return NacMedia.parseRelativePath(contentPath);
		}

		String[] columns = new String[] {
				canQueryRelativePath ?
					MediaStore.Audio.Media.RELATIVE_PATH :
					MediaStore.Audio.Media.DATA,
				};
		Cursor c = context.getContentResolver().query(contentUri, columns, null,
			null, null);
		String path = "";

		c.moveToFirst();

		try
		{
			int pathIndex = c.getColumnIndexOrThrow(columns[0]);
			path = c.getString(pathIndex);

			if (!canQueryRelativePath)
			{
				path = NacMedia.parseRelativePath(path);
			}
		}
		catch (IllegalArgumentException e)
		{
			NacUtility.printf("NacMedia : getRelativePath : IllegalArgumentException!");
		}

		c.close();

		return path;
	}

	/**
	 * @see getRelativePath
	 */
	public static String getRelativePath(Context context, String path)
	{
		Uri contentUri = Uri.parse(path);

		return NacMedia.getRelativePath(context, contentUri);
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
	public static String getTitle(Context context, Uri contentUri)
	{
		String contentPath = contentUri.toString();

		if (!contentPath.startsWith("content://"))
		{
			return NacFile.basename(contentPath);
		}

		String[] columns = new String[] { MediaStore.Audio.Media.TITLE };
		Cursor c = context.getContentResolver().query(contentUri, columns, null,
			null, null);
		String title = "";

		c.moveToFirst();

		try
		{
			int titleIndex = c.getColumnIndexOrThrow(
				MediaStore.Audio.Media.TITLE);
			title = c.getString(titleIndex);
		}
		catch (IllegalArgumentException e)
		{
			NacUtility.printf("NacMedia : getTitle : IllegalArgumentException!");
		}

		c.close();

		return title;
	}

	/**
	 * @see getTitle
	 */
	public static String getTitle(Context context, NacFile.Metadata metadata)
	{
		Uri contentUri = metadata.toExternalUri();

		return NacMedia.getTitle(context, contentUri);
	}

	/**
	 * @see getTitle
	 */
	public static String getTitle(Context context, String path)
	{
		Uri contentUri = Uri.parse(path);

		return NacMedia.getTitle(context, contentUri);
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
		else if (NacMedia.isRandomRingtone(path))
		{
			return TYPE_RINGTONE_RANDOM;
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
	@SuppressWarnings("deprecation")
	@TargetApi(Build.VERSION_CODES.Q)
	public static String getVolumeName(Context context, Uri contentUri)
	{
		String contentPrefix = "content://";
		String contentPath = contentUri.toString();

		if (!contentPath.startsWith(contentPrefix))
		{
			return "";
		}

		if (!NacMedia.canQueryVolumeName())
		{
			return NacMedia.parseVolumeName(contentPath);
		}

		String[] columns = new String[] { MediaStore.Audio.Media.VOLUME_NAME };
		Cursor c = context.getContentResolver().query(contentUri, columns, null,
			null, null);
		String volume = "";

		c.moveToFirst();

		try
		{
			int volumeIndex = c.getColumnIndexOrThrow(columns[0]);
			volume = c.getString(volumeIndex);
		}
		catch (IllegalArgumentException e)
		{
			NacUtility.printf("NacMedia : getVolumeName : IllegalArgumentException!");
		}

		c.close();

		return volume;
	}

	/**
	 * @see getVolumeName
	 */
	public static String getVolumeName(Context context, String path)
	{
		Uri contentUri = Uri.parse(path);

		return NacMedia.getVolumeName(context, contentUri);
	}

	/**
	 * Check if the given type is for a directory.
	 */
	public static boolean isDirectory(int type)
	{
		return (type == TYPE_DIRECTORY);
	}

	/**
	 * @see isDirectory
	 */
	public static boolean isDirectory(String path)
	{
		return !path.isEmpty() && !path.startsWith("content://");
	}

	/**
	 * Check if the given type is for a music file.
	 */
	public static boolean isFile(int type)
	{
		return (type == TYPE_FILE);
	}

	/**
	 * @see isFile
	 */
	public static boolean isFile(Context context, String path)
	{
		String volumeName = NacMedia.getVolumeName(context, path);
		String relativePath = NacMedia.getRelativePath(context, path);

		return ((volumeName != null) && volumeName.startsWith("external")
			&& (relativePath != null) && !relativePath.isEmpty());
	}

	/**
	 * Check if the given type corresponds to no type.
	 */
	public static boolean isNone(int type)
	{
		return (type == TYPE_NONE);
	}

	/**
	 * @see isNone
	 */
	public static boolean isNone(String path)
	{
		return ((path == null) || (path.isEmpty()));
	}

	/**
	 * Check if the given type corresponds to a random ringtone.
	 */
	public static boolean isRandomRingtone(int type)
	{
		return (type == TYPE_RINGTONE_RANDOM);
	}

	/**
	 * @see isRandomRingtone
	 */
	public static boolean isRandomRingtone(String path)
	{
		return path.equals(RANDOM_RINGTONE_PATH);
	}

	/**
	 * Check if the given type corresponds to a ringtone.
	 */
	public static boolean isRingtone(int type)
	{
		return (type == TYPE_RINGTONE);
	}

	/**
	 * @see isRingtone
	 */
	public static boolean isRingtone(Context context, String path)
	{
		String volumeName = NacMedia.getVolumeName(context, path);
		String relativePath = NacMedia.getRelativePath(context, path);

		return (volumeName.equals("internal") && (relativePath == null));
	}

	/**
	 * Check if the given type corresponds to a spotify file.
	 */
	public static boolean isSpotify(int type)
	{
		return (type == TYPE_SPOTIFY);
	}

	/**
	 * @see isSpotify
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
	 * Parse the relative path from a path retrieved by querying for
	 * MediaStore.Audio.Media.DATA
	 *
	 * This should only be done on any version before Q.
	 */
	public static String parseRelativePath(String filePath)
	{
		String relativeFilePath = NacFile.toRelativePath(filePath);
		String relativePath = NacFile.dirname(relativeFilePath);

		return NacFile.strip(relativePath);
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
	 * @see toUri
	 */
	public static Uri toUri(String path)
	{
		return Uri.parse(path);
	}

}

package com.nfcalarmclock;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.provider.MediaStore;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

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
		 * Scan the media table for available media to play, and create a file
		 * tree out of the output.
		 */
		public void scan(Context context)
		{
			NacTreeNode<String> currentDir = this.getDirectory();
			String home = NacFileBrowser.getHome();
			Uri baseUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
			String[] columns = new String[] {
				MediaStore.Audio.Media._ID,
				MediaStore.Audio.Media.RELATIVE_PATH,
				MediaStore.Audio.Media.DISPLAY_NAME,
				};
			Cursor c = context.getContentResolver().query(baseUri, columns,
				null, null, "_display_name");

			while (c.moveToNext())
			{
				int idIndex = c.getColumnIndex(MediaStore.Audio.Media._ID);
				int pathIndex = c.getColumnIndex(
					MediaStore.Audio.Media.RELATIVE_PATH);
				int nameIndex = c.getColumnIndex(
					MediaStore.Audio.Media.DISPLAY_NAME);
				long id = c.getLong(idIndex);
				String path = c.getString(pathIndex);
				String name = c.getString(nameIndex);
				String[] items = path.split("/");

				for (int i=0; i < items.length; i++)
				{
					String dir = items[i];

					this.add(dir);
					this.cd(dir);
				}

				this.add(name, id);
				this.cd(currentDir);
			}
		}

	}

	/**
	 * @return The name of the artist.
	 */
	public static String getArtist(Context context, NacFile.Metadata metadata)
	{
		long id = metadata.getId();
		Uri contentUri = ContentUris.withAppendedId(
			MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
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
		}
		catch (IllegalArgumentException e)
		{
			NacUtility.printf("NacSound : getArtist : IllegalArgumentException!");
		}

		c.close();

		return artist;
	}

	/**
	 * @return A list of NacSound objects corresponding to the files in the
	 *         given path.
	 */
	// To-do: Remove the usage of NacSound
	public static List<NacSound> getFiles(Context context, String filePath)
	{
		if ((filePath == null) || (filePath.isEmpty()))
		{
			return null;
		}


		NacMedia.Tree tree = new NacMedia.Tree(filePath);
		List<NacSound> soundFiles = new ArrayList<>();

		for (NacFile.Metadata metadata : tree.lsSort())
		{
			if (metadata.isDirectory())
			{
				continue;
			}

			String path = metadata.getPath();
			NacSound sound = new NacSound(context, path);

			NacUtility.printf("NacMedia : getFiles : %s", path);
			soundFiles.add(sound);
		}

		return soundFiles.isEmpty() ? null : soundFiles;
	}

	/**
	 * @return The name of the file.
	 */
	public static String getName(Context context, Uri contentUri)
	{
		String contentPath = contentUri.toString();

		if (!contentPath.startsWith("content://"))
		{
			NacUtility.printf("NacMedia : getOtherName : %s", NacFile.basename(contentPath));
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
			NacUtility.printf("NacMedia : getName : %s", name);
		}
		catch (IllegalArgumentException e)
		{
			NacUtility.printf("NacSound : getName : IllegalArgumentException!");
		}

		c.close();

		return name;
	}

	/**
	 * @see getName
	 */
	public static String getName(Context context, NacFile.Metadata metadata)
	{
		long id = metadata.getId();
		Uri contentUri = ContentUris.withAppendedId(
			MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);

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
	public static String getRelativePath(Context context, Uri contentUri)
	{
		String contentPath = contentUri.toString();

		if (!contentPath.startsWith("content://"))
		{
			return NacFile.dirname(contentPath);
		}

		String[] columns = new String[] { MediaStore.Audio.Media.RELATIVE_PATH };
		Cursor c = context.getContentResolver().query(contentUri, columns, null,
			null, null);
		String path = "";

		c.moveToFirst();

		try
		{
			int pathIndex = c.getColumnIndexOrThrow(
				MediaStore.Audio.Media.RELATIVE_PATH);
			path = c.getString(pathIndex);
			NacUtility.printf("NacMedia : getRelativePath : %s", path);
		}
		catch (IllegalArgumentException e)
		{
			NacUtility.printf("NacSound : getRelativePath : IllegalArgumentException!");
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
	public static List<String> getRingtones(Context context)
	{
		RingtoneManager manager = new RingtoneManager(context);
		List<String> ringtonePaths = new ArrayList<>();
		List<String> ringtoneTitles = new ArrayList<>();

		manager.setType(RingtoneManager.TYPE_ALARM);

		Cursor c = manager.getCursor();

		while (c.moveToNext())
		{
			String title = c.getString(RingtoneManager.TITLE_COLUMN_INDEX);
			String id = c.getString(RingtoneManager.ID_COLUMN_INDEX);
			String dir = c.getString(RingtoneManager.URI_COLUMN_INDEX);
			String path = String.format("%s/%s", dir, id);

			if (ringtoneTitles.contains(title))
			{
				continue;
			}

			ringtonePaths.add(path);
			ringtoneTitles.add(title);
		}

		return ringtonePaths;
	}

	/**
	 * @return The title of the track.
	 */
	public static String getTitle(Context context, Uri contentUri)
	{
		String contentPath = contentUri.toString();

		if (!contentPath.startsWith("content://"))
		{
			NacUtility.printf("NacMedia : getOtherTitle : %s", NacFile.basename(contentPath));
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
			NacUtility.printf("NacMedia : getTitle : %s", title);
		}
		catch (IllegalArgumentException e)
		{
			NacUtility.printf("NacSound : getTitle : IllegalArgumentException!");
		}

		c.close();

		return title;
	}

	/**
	 * @see getTitle
	 */
	public static String getTitle(Context context, NacFile.Metadata metadata)
	{
		long id = metadata.getId();
		Uri contentUri = ContentUris.withAppendedId(
			MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);

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
	public static String getVolumeName(Context context, Uri contentUri)
	{
		String contentPath = contentUri.toString();

		if (!contentPath.startsWith("content://"))
		{
			return "";
		}

		String[] columns = new String[] { MediaStore.Audio.Media.VOLUME_NAME };
		Cursor c = context.getContentResolver().query(contentUri, columns, null,
			null, null);
		String volume = "";

		c.moveToFirst();

		try
		{
			int volumeIndex = c.getColumnIndexOrThrow(
				MediaStore.Audio.Media.VOLUME_NAME);
			volume = c.getString(volumeIndex);
			NacUtility.printf("NacMedia : getVolumeName : %s", volume);
		}
		catch (IllegalArgumentException e)
		{
			NacUtility.printf("NacSound : getVolumeName : IllegalArgumentException!");
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
		if (path.startsWith("/"))
		{
			File file = new File(path);

			return file.isDirectory();
		}

		return false;
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
	 * Convert the input to a Uri.
	 */
	public static Uri toUri(NacFile.Metadata metadata)
	{
		return ContentUris.withAppendedId(
			MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, metadata.getId());
	}

	/**
	 * @see toUri
	 */
	public static Uri toUri(String path)
	{
		return Uri.parse(path);
	}

}

package com.nfcalarmclock;

import android.content.Context;
import android.database.Cursor;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Media utility class.
 */
public class NacMedia
{

	/**
	 * Pair of media path and name.
	 *
	 * Mainly used when determining a list of files to display and the user
	 * needs to store these two pieces of information quickly and easily.
	 */
	public static class Pair
	{

		/**
		 * Path to the media file.
		 */
		public String mPath;

		/**
		 * Name of the media file.
		 */
		public String mName;

		/**
		 */
		public Pair(String path, String name)
		{
			this.mPath = path;
			this.mName = name;
		}

		/**
		 * @return The name of the media file.
		 */
		public String getName()
		{
			return this.mName;
		}

		/**
		 * @return The path to the media file.
		 */
		public String getPath()
		{
			return this.mPath;
		}

	}

	/**
	 * @return The sound name.
	 */
	public static String getMediaName(Context context, String path)
	{
		if (path.isEmpty())
		{
			return "";
		}

		Uri uri = Uri.parse(path);
		Ringtone ringtone = RingtoneManager.getRingtone(context, uri);
		String name = ringtone.getTitle(context);

		ringtone.stop();

		return name;
	}

	/**
	 * @return The path to the ringtone/music file.
	 *
	 * @param  context  The activity context.
	 * @param  media  The path to the media file.
	 */
	public static String getMediaPath(Context context, String media)
	{
		String path = media;

		if (media.startsWith("content://"))
		{
			Uri uri = Uri.parse(media);
			Cursor cursor = context.getContentResolver().query(uri,
				new String[] { MediaStore.Audio.Media.DATA }, null, null, null);
			cursor.moveToFirst();

			path = cursor.getString(cursor.getColumnIndexOrThrow(
				MediaStore.Audio.Media.DATA));

			cursor.close();
		}
		else
		{
			// Do other checks for media here
		}

		return path;
	}

	/**
	 * @param  context  The application context.
	 *
	 * @return A list of alarm ringtones.
	 */
	public static List<Pair> getRingtones(Context context)
	{
		RingtoneManager manager = new RingtoneManager(context);
		List<Pair> list = new ArrayList<>();

		manager.setType(RingtoneManager.TYPE_ALARM);

		Cursor cursor = manager.getCursor();

		for (boolean skip=false; cursor.moveToNext(); skip=false)
		{
			String name = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX);
			String id = cursor.getString(RingtoneManager.ID_COLUMN_INDEX);
			String dir = cursor.getString(RingtoneManager.URI_COLUMN_INDEX);

			for (Pair p : list)
			{
				if (p.getName().equals(name))
				{
					skip = true;
					break;
				}
			}

			if (skip)
			{
				continue;
			}

			list.add(new Pair(dir+"/"+id, name));
		}

		return list;
	}

	/**
	 * Remove extension from file name.
	 */
	public static String removeExtension(String name)
	{
		return (name.contains(".")) ?
			name.substring(0, name.lastIndexOf('.')) : name;
	}

	/**
	 * @param  file  The File object.
	 *
	 * @return The MediaMetadataRetriever object, but check for errors in case
	 *         any exceptions were thrown.
	 */
	public static MediaMetadataRetriever getMediaMetadataRetriever(File file)
	{
		MediaMetadataRetriever retriever = new MediaMetadataRetriever();

		try
		{
			retriever.setDataSource(file.getAbsolutePath());
		}
		catch (RuntimeException e)
		{
			NacUtility.printf("Something wrong with file '%s'.", file.getAbsolutePath());
			retriever.release();
			return null;
		}

		return retriever;
	}

	/**
	 * @param  file  The File object.
	 *
	 * @return The artist name.
	 */
	public static String getArtist(File file)
	{
		MediaMetadataRetriever retriever = NacMedia.getMediaMetadataRetriever(file);
		String artist = retriever.extractMetadata(
			MediaMetadataRetriever.METADATA_KEY_ARTIST);
		String name = file.getName();
		String defaultArtist = "Unknown";

		retriever.release();

		if (artist == null)
		{
			artist = defaultArtist;

			if (name.contains(" - "))
			{
				String[] parts = name.split(" - ");

				try
				{
					int test = Integer.parseInt(parts[0]);
				}
				catch (NumberFormatException nfe)
				{
					artist = parts[0].trim();
				}
			}
		}

		return artist;
	}

	/**
	 * @return The title of the track.
	 */
	public static String getTitle(File file)
	{
		MediaMetadataRetriever retriever = NacMedia.getMediaMetadataRetriever(file);
		String title = retriever.extractMetadata(
			MediaMetadataRetriever.METADATA_KEY_TITLE);
		String name = file.getName();
		String defaultTitle = NacMedia.removeExtension(name);

		retriever.release();

		if (title == null)
		{
			title = defaultTitle;

			if (name.contains(" - "))
			{
				String[] parts = name.split(" - ");
				title = NacMedia.removeExtension(parts[1].trim());
			}
		}

		return title;
	}

	/**
	 * Return the sound file filter.
	 */
	public static FilenameFilter getFilter()
	{
		return new FilenameFilter()
		{
			public boolean accept(File dir, String name)
			{
				Locale locale = Locale.getDefault();
				String lower = name.toLowerCase(locale);
				String[] extensions = { ".mp3", ".ogg" };
				//String[] extensions = {".3gp", ".mp4", ".m4a", ".aac",
				//	".ts", ".flac", ".mid", ".xmf", ".mxmf", ".rtttl",
				//	".rtx", ".ota", ".imy", ".mp3", ".mkv", ".wav" };

				if (dir.isDirectory())
				{
					return true;
				}

				for (String e : extensions)
				{
					if (lower.endsWith(e))
					{
						return true;
					}
				}

				return false;
			}
		};
	}

}

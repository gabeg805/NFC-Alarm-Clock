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
 * Sound utility class.
 */
public class NacSound
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
	public static final int TYPE_FILE_RANDOM = 5;

	/**
	 * Type of sound for spotify.
	 */
	public static final int TYPE_SPOTIFY_RANDOM = 6;

	/**
	 * Type of sound file.
	 */
	private int mType;

	/**
	 * Path to the sound file.
	 */
	private String mPath;

	/**
	 * Name of the sound file.
	 */
	private String mName;

	/**
	 */
	public NacSound(int type, String path, String name)
	{
		this.mType = type;
		this.mPath = path;
		this.mName = name;
	}

	/**
	 * @return The name of the sound file.
	 */
	public String getName()
	{
		return this.mName;
	}

	/**
	 * @return The path to the sound file.
	 */
	public String getPath()
	{
		return this.mPath;
	}

	/**
	 * @return The type of sound file.
	 */
	public int getType()
	{
		return this.mType;
	}

	/**
	 * @param  file  The File object.
	 *
	 * @return The artist name.
	 */
	public static String getArtist(File file)
	{
		MediaMetadataRetriever retriever = NacSound.getMediaMetadataRetriever(file);

		if (retriever == null)
		{
			return "";
		}

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
	 * @return The sound name.
	 */
	public static String getName(Context context, String path)
	{
		if ((path == null) || path.isEmpty())
		{
			return "";
		}

		Uri uri = NacSound.isRingtone(path) ? Uri.parse(path)
			: Uri.fromFile(new File(path));
		Ringtone ringtone = RingtoneManager.getRingtone(context, uri);
		String name = ringtone.getTitle(context);

		ringtone.stop();

		return name;
	}

	/**
	 * @return The path to the ringtone/music file.
	 *
	 * @param  context  The activity context.
	 * @param  path  The path to the sound file.
	 */
	public static String getPath(Context context, String path)
	{
		if ((path == null) || path.isEmpty())
		{
			return "";
		}
		else if (NacSound.isRingtone(path))
		{
			Uri uri = Uri.parse(path);
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
	public static List<NacSound> getRingtones(Context context)
	{
		RingtoneManager manager = new RingtoneManager(context);
		List<NacSound> list = new ArrayList<>();

		manager.setType(RingtoneManager.TYPE_ALARM);

		Cursor cursor = manager.getCursor();

		for (boolean skip=false; cursor.moveToNext(); skip=false)
		{
			String name = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX);
			String id = cursor.getString(RingtoneManager.ID_COLUMN_INDEX);
			String dir = cursor.getString(RingtoneManager.URI_COLUMN_INDEX);

			for (NacSound s : list)
			{
				if (s.getName().equals(name))
				{
					skip = true;
					break;
				}
			}

			if (skip)
			{
				continue;
			}

			list.add(new NacSound(TYPE_RINGTONE, dir+"/"+id, name));
		}

		return list;
	}

	/**
	 * @return The title of the track.
	 */
	public static String getTitle(File file)
	{
		MediaMetadataRetriever retriever = NacSound.getMediaMetadataRetriever(file);

		if (retriever == null)
		{
			return "";
		}

		String title = retriever.extractMetadata(
			MediaMetadataRetriever.METADATA_KEY_TITLE);
		String name = file.getName();
		String defaultTitle = NacSound.removeExtension(name);

		retriever.release();

		if (title == null)
		{
			title = defaultTitle;

			if (name.contains(" - "))
			{
				String[] parts = name.split(" - ");
				title = NacSound.removeExtension(parts[1].trim());
			}
		}

		return title;
	}

	/**
	 * @return The sound type.
	 */
	public static int getType(String path)
	{
		if (NacSound.isRingtone(path))
		{
			return TYPE_RINGTONE;
		}
		else if (NacSound.isFile(path))
		{
			return TYPE_FILE;
		}
		else if (NacSound.isSpotify(path))
		{
			return TYPE_SPOTIFY;
		}
		else
		{
			return TYPE_NONE;
		}
	}

	/**
	 * Check if the given path corresponds to a music file.
	 */
	public static boolean isFile(String path)
	{
		return path.startsWith("/");
	}

	/**
	 * Check if the given type is a music file.
	 */
	public static boolean isFile(int type)
	{
		return ((type == TYPE_FILE) || (type == TYPE_FILE_RANDOM));
	}

	/**
	 * Check if the given path corresponds to no type.
	 */
	public static boolean isNone(String path)
	{
		return ((path == null) || (path.isEmpty()));
	}

	/**
	 * Check if the given type corresponds to no type.
	 */
	public static boolean isNone(int type)
	{
		return (type == TYPE_NONE);
	}

	/**
	 * Check if the given path corresponds to a ringtone.
	 */
	public static boolean isRingtone(String path)
	{
		return path.startsWith("content://");
	}

	/**
	 * Check if the given type corresponds to a ringtone.
	 */
	public static boolean isRingtone(int type)
	{
		return ((type == TYPE_RINGTONE) || (type == TYPE_RINGTONE_RANDOM));
	}

	/**
	 * Check if the given path corresponds to a spotify file.
	 */
	public static boolean isSpotify(String path)
	{
		return path.startsWith("spotify");
	}

	/**
	 * Check if the given type corresponds to a spotify file.
	 */
	public static boolean isSpotify(int type)
	{
		return ((type == TYPE_SPOTIFY) || (type == TYPE_SPOTIFY_RANDOM));
	}

	/**
	 * Remove extension from file name.
	 */
	public static String removeExtension(String name)
	{
		return (name.contains(".")) ?
			name.substring(0, name.lastIndexOf('.')) : name;
	}

}

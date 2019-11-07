package com.nfcalarmclock;

import android.content.Context;
import android.database.Cursor;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Sound utility class.
 */
public class NacSound
	implements Parcelable
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
	 * Path for a random ringtone.
	 */
	public static final String RANDOM_RINGTONE_PATH = "TYPE_RINGTONE_RANDOM";

	/**
	 * Name for a random ringtone.
	 */
	public static final String RANDOM_RINGTONE_NAME = "Ringtone Playlist";

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
	 * Extra data the user may want to save.
	 */
	private String mData;

	/**
	 * Repeat the media.
	 */
	private boolean mRepeat;

	/**
	 */
	public NacSound(Context context, String path)
	{
		this.set(context, path);
		this.setData("");
		this.setRepeat(false);
	}

	/**
	 */
	public NacSound(int type)
	{
		this.setType(type);

		if (NacSound.isRandomRingtone(type))
		{
			this.setPath(RANDOM_RINGTONE_PATH);
			this.setName(RANDOM_RINGTONE_NAME);
		}

		this.setRepeat(false);
	}

	/**
	 */
	public NacSound(int type, String path, String name)
	{
		this(type, path, name, false);
	}

	/**
	 */
	public NacSound(int type, String path, String name, boolean repeat)
	{
		this.setType(type);
		this.setPath(path);
		this.setName(name);
		this.setData("");
		this.setRepeat(repeat);
	}

	/**
	 * Populate values with input parcel.
	 */
	public NacSound(Parcel input)
	{
		this.setType(input.readInt());
		this.setPath(input.readString());
		this.setName(input.readString());
		this.setData(input.readString());
		this.setRepeat((input.readInt() != 0));
	}

	/**
	 * Describe contents (required for Parcelable).
	 */
	@Override
	public int describeContents()
	{
		return 0;
	}

	/**
	 * @return The extra data.
	 */
	public String getData()
	{
		return this.mData;
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
	 * @return True if the media should be repeated, and False otherwise.
	 */
	public boolean getRepeat()
	{
		return this.mRepeat;
	}

	/**
	 * @return The type of sound file.
	 */
	public int getType()
	{
		return this.mType;
	}

	/**
	 * Print the contents of the object.
	 */
	public void print()
	{
		NacUtility.printf("Type   : %d", this.getType());
		NacUtility.printf("Name   : %s", this.getName());
		NacUtility.printf("Path   : %s", this.getPath());
		NacUtility.printf("Data   : %s", this.getData());
		NacUtility.printf("Repeat : %b", this.getRepeat());
	}

	/**
	 * Set all attributes of the sound, from the given path.
	 */
	public void set(Context context, String path)
	{
		this.setType(NacSound.getType(path));
		this.setPath(path);
		this.setName(NacSound.getName(context, path));
	}

	/**
	 * Set the data.
	 */
	public void setData(String data)
	{
		this.mData = data;
	}

	/**
	 * Set the sound name.
	 */
	public void setName(String name)
	{
		this.mName = name;
	}

	/**
	 * Set the sound path.
	 */
	public void setPath(String path)
	{
		this.mPath = path;
	}

	/**
	 * Set the repeat flag.
	 */
	public void setRepeat(boolean repeat)
	{
		this.mRepeat = repeat;
	}

	/**
	 * Set the sound type.
	 */
	public void setType(int type)
	{
		this.mType = type;
	}

	/**
	 * Write data into parcel (required for Parcelable).
	 */
	@Override
	public void writeToParcel(Parcel output, int flags)
	{
		output.writeInt(this.getType());
		output.writeString(this.getPath());
		output.writeString(this.getName());
		output.writeString(this.getData());
		output.writeInt(this.getRepeat() ? 1 : 0);
	}

	/**
	 * Generate parcel (required for Parcelable).
	 */
	public static final Parcelable.Creator<NacSound> CREATOR = new
		Parcelable.Creator<NacSound>()
	{
		public NacSound createFromParcel(Parcel input)
		{
			return new NacSound(input);
		}

		public NacSound[] newArray(int size)
		{
			return new NacSound[size];
		}
	};



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
	 * @return A list of NacSound objects corresponding to the files in the
	 *         given path.
	 */
	public static List<NacSound> getFiles(Context context, String path)
	{
		if ((path == null) || (path.isEmpty()))
		{
			return null;
		}

		File directory = new File(path);
		List<NacSound> files = new ArrayList<>();

		if (!directory.isDirectory())
		{
			return null;
		}

		for (File file : NacFileBrowser.listing(path))
		{
			if (file.isDirectory())
			{
				continue;
			}

			try
			{
				String canonicalPath = file.getCanonicalPath();
				NacUtility.printf("Path : %s", canonicalPath);
				files.add(new NacSound(context, canonicalPath));
			}
			catch (IOException e)
			{
				NacUtility.printf("NacSound : getFiles : IOException occurred when trying to getCanonicalPath().");
			}
		}

		return files;
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
		String path = file.getAbsolutePath();

		if (path == null)
		{
			return null;
		}

		MediaMetadataRetriever retriever = new MediaMetadataRetriever();

		try
		{
			retriever.setDataSource(path);
		}
		//catch (RuntimeException | IllegalArgumentException e)
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
		String name = "";

		if (ringtone != null)
		{
			name = ringtone.getTitle(context);

			ringtone.stop();
		}

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
		if (NacSound.isNone(path))
		{
			return TYPE_NONE;
		}
		else if (NacSound.isRingtone(path))
		{
			return TYPE_RINGTONE;
		}
		else if (NacSound.isFile(path))
		{
			return TYPE_FILE;
		}
		else if (NacSound.isRandomRingtone(path))
		{
			return TYPE_RINGTONE_RANDOM;
		}
		else if (NacSound.isFilePlaylist(path))
		{
			return TYPE_FILE_RANDOM;
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
	 * Check if the given type is for a music file.
	 */
	public static boolean isFile(int type)
	{
		return (type == TYPE_FILE);
	}

	/**
	 * Check if the given path corresponds to a music file.
	 */
	public static boolean isFile(String path)
	{
		if (path.startsWith("/"))
		{
			File file = new File(path);

			return file.isFile();
		}

		return false;
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
	 * Check if the given type is for a random music file.
	 */
	public static boolean isFilePlaylist(int type)
	{
		return (type == TYPE_FILE_RANDOM);
	}

	/**
	 * Check if the given path corresponds to a music playlist.
	 */
	public static boolean isFilePlaylist(String path)
	{
		if (path.startsWith("/"))
		{
			File file = new File(path);

			return file.isDirectory();
		}

		return false;
	}

	/**
	 * Check if the given type corresponds to a random ringtone.
	 */
	public static boolean isRandomRingtone(int type)
	{
		return (type == TYPE_RINGTONE_RANDOM);
	}

	/**
	 * Check if the given path corresponds to a random ringtone.
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
	 * Check if the given path corresponds to a ringtone.
	 */
	public static boolean isRingtone(String path)
	{
		return path.startsWith("content://");
	}

	/**
	 * Check if the given type corresponds to a spotify file.
	 */
	public static boolean isSpotify(int type)
	{
		return (type == TYPE_SPOTIFY);
	}

	/**
	 * Check if the given path corresponds to a spotify file.
	 */
	public static boolean isSpotify(String path)
	{
		return path.startsWith("spotify");
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

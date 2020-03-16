package com.nfcalarmclock;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import java.util.ArrayList;
import java.util.List;

/**
 */
public class NacMedia
{

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
	 * @return The title of the track.
	 */
	public static String getTitle(Context context, NacFile.Metadata metadata)
	{
		long id = metadata.getId();
		Uri contentUri = ContentUris.withAppendedId(
			MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
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
			NacUtility.printf("NacSound : getTitle : IllegalArgumentException!");
		}

		c.close();

		return title;
	}

}

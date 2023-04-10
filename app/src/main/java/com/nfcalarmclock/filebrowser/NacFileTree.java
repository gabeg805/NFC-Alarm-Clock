package com.nfcalarmclock.filebrowser;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import com.nfcalarmclock.util.file.NacFile;
import com.nfcalarmclock.util.file.NacTreeNode;
import com.nfcalarmclock.util.NacUtility;

import java.util.ArrayList;
import java.util.List;

/**
 * File tree of all media on the device.
 */
public class NacFileTree
	extends NacFile.Tree
{

	public NacFileTree(String path)
	{
		super(path);
	}

	/**
	 * Get the names of the columns that will be returned from the query.
	 *
	 * @return The names of the columns that will be returned from the query.
	 */
	@TargetApi(Build.VERSION_CODES.Q)
	private String[] getQueryColumns()
	{
		String idColumn = MediaStore.Audio.Media._ID;
		String pathColumn = MediaStore.Audio.Media.DATA;
		String nameColumn = MediaStore.Audio.Media.DISPLAY_NAME;

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
		{
			pathColumn = MediaStore.Audio.Media.RELATIVE_PATH;
		}

		return new String[] { idColumn, pathColumn,  nameColumn };
	}

	/**
	 * Get the cursor that will be returned by the query.
	 *
	 * @param  context  The application context.
	 * @param  columns  Array of columns to return from the query.
	 *
	 * @return The cursor that will be returned by the query.
	 */
	private Cursor getQueryCursor(Context context, String[] columns)
	{
		ContentResolver resolver = context.getContentResolver();
		String sortOrder = "_display_name";
		Cursor c = null;
		Uri collection;

		// TODO: Iterate over all volumes? This would go into getContentUri() below
		//for (String v : MediaStore.getExternalVolumeNames(context))
		//{
		//	NacUtility.printf("Volume : %s", v);
		//}

		// Define which table, containing the collection of media, to query
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
		{
			collection = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
		}
		else
		{
			collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
		}

		// Query the table and get the cursor
		try
		{
			c = resolver.query(collection, columns, null, null, sortOrder);
		}
		catch (IllegalArgumentException e)
		{
			try
			{
				c = resolver.query(collection, columns, null, null, null);
			}
			catch (IllegalArgumentException f)
			{
			}
		}

		// Return the cursor
		return c;
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
	@TargetApi(Build.VERSION_CODES.Q)
	public void scan(Context context, boolean filter)
	{
		String[] columns = this.getQueryColumns();
		Cursor c = this.getQueryCursor(context, columns);

		// Unable to get the query cursor
		if (c == null)
		{
			return;
		}

		// TODO: Shouldn't this be a NacFile object?
		NacTreeNode<String> currentDir = this.getDirectory();
		String currentPath = NacFile.toRelativePath(this.getDirectoryPath());
		int idIndex = c.getColumnIndex(columns[0]);
		int pathIndex = c.getColumnIndex(columns[1]);
		int nameIndex = c.getColumnIndex(columns[2]);

		// Iterate over each scanned media file
		while (c.moveToNext())
		{
			long id = c.getLong(idIndex);
			String path = NacFile.strip(c.getString(pathIndex));
			String name = c.getString(nameIndex);

			// Get the directory name from the path?
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q)
			{
				path = NacFile.toRelativeDirname(path);
			}

			// Filter out media that is not from the current path
			if (filter && !currentPath.equals(path))
			{
				continue;
			}

			// Split up the path by directory
			String[] splitPath = path.replace(currentPath, "").split("/");

			// Iterate over each directory and add it to the file tree
			for (String dir : splitPath)
			{
				this.add(dir);
				this.cd(dir);
			}

			// Add the current name and change to the current directory
			this.add(name, id);
			this.cd(currentDir);
		}

		c.close();
	}

	/**
	 * @return A list of content Uris under the given path. They are assumed to
	 *     be external Uris.
	 */
	public static List<Uri> getFiles(Context context, String filePath)
	{
		if ((filePath == null) || (filePath.isEmpty()))
		{
			return null;
		}

		NacFileTree tree = new NacFileTree(filePath);
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

}

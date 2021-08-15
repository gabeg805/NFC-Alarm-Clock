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
	@SuppressWarnings("deprecation")
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
		Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
		String sortOrder = "_display_name";
		Cursor c = null;

		try
		{
			c = resolver.query(uri, columns, null, null, sortOrder);
		}
		catch (IllegalArgumentException e)
		{
			try
			{
				c = resolver.query(uri, columns, null, null, null);
			}
			catch (IllegalArgumentException f)
			{
			}
		}

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
	@SuppressWarnings("deprecation")
	@TargetApi(Build.VERSION_CODES.Q)
	public void scan(Context context, boolean filter)
	{
		String[] columns = this.getQueryColumns();
		Cursor c = this.getQueryCursor(context, columns);

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

		while (c.moveToNext())
		{
			long id = c.getLong(idIndex);
			String path = NacFile.strip(c.getString(pathIndex));
			String name = c.getString(nameIndex);

			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q)
			{
				path = NacFile.toRelativeDirname(path);
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

package com.nfcalarmclock;

import android.content.Context;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A music file browser.
 */
public class NacFileBrowser
	implements View.OnClickListener
{

	/**
	 * Click listener for the file browser.
	 */
	public interface OnClickListener
	{
		public void onClick(NacFileBrowser browser, File file, String path,
			String name);
	}

	/**
	 * The path view.
	 */
	private TextView mPathView;

	/**
	 * The container view for the directory/file buttons.
	 */
	private NacButtonGroup mContainer;

	/**
	 * File browser click listener.
	 */
	private OnClickListener mListener;

	/**
	 */
	public NacFileBrowser(View root, int pathId, int groupId)
	{
		this.mPathView = (TextView) root.findViewById(pathId);
		this.mContainer = (NacButtonGroup) root.findViewById(groupId);

		this.mContainer.removeAllViews();
	}

	/**
	 * Add a directory entry to the file browser.
	 */
	public void addDirectory(File file)
	{
		NacButtonGroup container = this.getContainer();

		if (container == null)
		{
			NacUtility.printf("NacFileBrowser : addDirectory : Container is null.");
			return;
		}

		Context context = container.getContext();
		NacImageTextButton entry = new NacImageTextButton(context);
		String name = file.getName();

		container.add(entry);
		entry.setText(name.equals("..") ? "(Previous folder)" : name);
		entry.setImageBackground(R.mipmap.folder);
		entry.setTag(file);
		entry.setOnClickListener(this);
	}

	/**
	 * Add an entry.
	 */
	public void addEntry(File file)
	{
		if (file.isDirectory())
		{
			this.addDirectory(file);
		}
		else if (file.isFile())
		{
			this.addFile(file);
		}
	}

	/**
	 * Add all files under the given path.
	 */
	public void addFileListing(String path)
	{
		for (File file : this.fileListing(path))
		{
			this.addEntry(file);
		}
	}

	/**
	 * Add a music file entry to the file browser.
	 */
	public void addFile(File file)
	{
		NacButtonGroup container = this.getContainer();

		if (file.length() == 0)
		{
			return;
		}

		if (container == null)
		{
			NacUtility.printf("NacFileBrowser : addFile : Container is null.");
			return;
		}

		Context context = container.getContext();
		NacImageSubTextButton entry = new NacImageSubTextButton(context);
		String title = NacSound.getTitle(file);
		String artist = NacSound.getArtist(file);

		if (title.isEmpty())
		{
			return;
		}

		container.add(entry);
		entry.setTextTitle(title);
		entry.setTextSubtitle(artist);
		entry.setImageBackground(R.mipmap.play);
		entry.setTag(file);
		entry.setOnClickListener(this);
	}

	/**
	 * Clear the views out of the browser.
	 */
	private void clearEntries()
	{
		NacButtonGroup container = this.getContainer();

		container.removeAllViews();
	}

	/**
	 * @return A listing of music files and directories under a given path.
	 *         Directories will be listed first, before files.
	 */
	public List<File> fileListing(String path)
	{
		File[] listing = new File(path).listFiles(NacSound.getFilter());
		List<File> directories = new ArrayList<>();
		List<File> files = new ArrayList<>();
		String home = this.getHome();

		if (!path.equals(home))
		{
			directories.add(new File(path + "/.."));
		}

		for (int i=0; (listing != null) && (i < listing.length); i++)
		{
			if (listing[i].isDirectory())
			{
				directories.add(listing[i]);
			}
			else if (listing[i].isFile())
			{
				files.add(listing[i]);
			}
		}

		Collections.sort(directories);
		Collections.sort(files);
		directories.addAll(files);

		return directories;
	}

	/**
	 * @return The container view.
	 */
	private NacButtonGroup getContainer()
	{
		return this.mContainer;
	}

	/**
	 * @return The home directory.
	 */
	private String getHome()
	{
		return Environment.getExternalStorageDirectory().toString();
	}

	/**
	 * @return The OnClickListener.
	 */
	private OnClickListener getListener()
	{
		return this.mListener;
	}

	/**
	 */
	@Override
	public void onClick(View view)
	{
		OnClickListener listener = this.getListener();

		if (listener == null)
		{
			return;
		}

		File file = (File) view.getTag();
		String name = file.getName();
		String path;

		try
		{
			path = file.getCanonicalPath();
		}
		catch (IOException e)
		{
			NacUtility.printf("NacFileBrowser : onClick : IOException occurred when trying to getCanonicalPath().");
			return;
		}

		listener.onClick(this, file, path, name);
	}

	/**
	 * Populate views into the browser.
	 */
	private void populateEntries(String entryPath)
	{
		String path = entryPath.isEmpty() ? getHome() : entryPath;

		this.mPathView.setText(path);
		this.addFileListing(path);
	}

	/**
	 * Set the file browser on click listener.
	 */
	public void setOnClickListener(OnClickListener listener)
	{
		this.mListener = listener;
	}

	/**
	 * Show the home directory.
	 */
	public void show()
	{
		this.show("");
	}

	/**
	 * Show the directory contents at the given path.
	 */
	public void show(String path)
	{
		this.clearEntries();
		this.populateEntries(path);
	}

}

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
	 * Currently selected view.
	 */
	private NacImageSubTextButton mSelected;

	/**
	 * Current directory.
	 */
	private String mCurrentDirectory;

	/**
	 */
	public NacFileBrowser(View root, int pathId, int groupId)
	{
		this.mPathView = (TextView) root.findViewById(pathId);
		this.mContainer = (NacButtonGroup) root.findViewById(groupId);
		this.mSelected = null;
		this.mCurrentDirectory = NacFileBrowser.getHome();

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
	public void addListing(String path)
	{
		for (File file : NacFileBrowser.listing(path))
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
	 * Deselect the currently selected item from the file browser.
	 */
	public void deselect()
	{
		this.select((View)null);
	}

	/**
	 * @return The container view.
	 */
	private NacButtonGroup getContainer()
	{
		return this.mContainer;
	}

	/**
	 * @return The current directory path.
	 */
	public String getCurrentDirectory()
	{
		return this.mCurrentDirectory;
	}

	/**
	 * @return The home directory.
	 */
	public static String getHome()
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
	 * @return The name of the file represented by the view.
	 */
	public String getName(View view)
	{
		if (view == null)
		{
			return "";
		}

		File file = (File) view.getTag();

		return file.getName();
	}

	/**
	 * @return The file path repesented by the view.
	 */
	public String getPath(View view)
	{
		if (view == null)
		{
			return "";
		}

		File file = (File) view.getTag();
		String name = file.getName();

		try
		{
			return file.getCanonicalPath();
		}
		catch (IOException e)
		{
			NacUtility.printf("NacFileBrowser : getSelectedPath : IOException occurred when trying to getCanonicalPath().");
			return "";
		}
	}

	/**
	 * @return The currently selected view.
	 */
	public NacImageSubTextButton getSelected()
	{
		return this.mSelected;
	}

	/**
	 * @return The currently selected name.
	 */
	public String getSelectedName()
	{
		return this.getName(this.getSelected());
	}

	/**
	 * @return The path of the currently selected view.
	 */
	public String getSelectedPath()
	{
		return this.getPath(this.getSelected());
	}

	/**
	 * @return True if something is selected and False otherwise.
	 */
	public boolean isSelected()
	{
		return (this.getSelected() != null);
	}

	/**
	 * @return True if the given path matches the currently selected path, and
	 *         False otherwise.
	 */
	public boolean isSelected(String path)
	{
		String selectedPath = this.getSelectedPath();

		return (!path.isEmpty() && (path.equals(selectedPath)));
	}

	/**
	 * @return A listing of music files and directories under a given path.
	 *         Directories will be listed first, before files.
	 */
	public static List<File> listing(String path)
	{
		File[] listing = new File(path).listFiles(NacSound.getFilter());
		List<File> directories = new ArrayList<>();
		List<File> files = new ArrayList<>();
		String home = NacFileBrowser.getHome();

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
		String name = this.getName(view);
		String path = this.getPath(view);

		if (path.isEmpty())
		{
			return;
		}

		if (file.isFile())
		{
			if (this.isSelected(path))
			{
				this.deselect();
			}
			else
			{
				this.select(view);
			}
		}
		else
		{
			this.mCurrentDirectory = path;
		}

		listener.onClick(this, file, path, name);
	}

	/**
	 * Populate views into the browser.
	 */
	private void populateEntries(String showPath)
	{
		this.mPathView.setText(showPath);
		NacButtonGroup container = this.getContainer();

		if (container == null)
		{
			Context context = container.getContext();
			NacUtility.quickToast(context, "Populating entries at : "+showPath);
		}

		this.addListing(showPath);
	}

	/**
	 * Change directory to previous ("../") directory.
	 */
	public boolean previousDirectory()
	{
		String home = NacFileBrowser.getHome();
		String currentDirectory = this.getCurrentDirectory();

		if (home.equals(currentDirectory))
		{
			return false;
		}

		try
		{
			File parentFile = new File(currentDirectory + "/..");
			String parentPath = parentFile.getCanonicalPath();

			this.show(parentPath);
			return true;
		}
		catch (IOException e)
		{
			NacUtility.printf("NacFileBrowser : previousDirectory : IOException occurred when trying to getCanonicalPath().");
			return false;
		}
	}

	/**
	 * Set the file browser on click listener.
	 */
	public void setOnClickListener(OnClickListener listener)
	{
		this.mListener = listener;
	}

	/**
	 * @see select
	 */
	public void select(String path)
	{
		File file = new File(path);

		if (path.isEmpty() || !file.isFile())
		{
			return;
		}

		NacButtonGroup container = this.getContainer();
		int count = container.getChildCount();

		for (int i=0; i < count; i++)
		{
			View view = container.getChildAt(i);
			String viewPath = this.getPath(view);

			if (path.equals(viewPath))
			{
				this.select(view);
				return;
			}
		}
	}

	/**
	 * Set the currently selected file.
	 */
	public void select(View view)
	{
		if (this.getSelected() != null)
		{
			this.getSelected().unselect();
		}

		this.mSelected = (NacImageSubTextButton) view;

		if (this.getSelected() != null)
		{
			this.getSelected().select();
		}
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
		String showPath = path.isEmpty() ? NacFileBrowser.getHome() : path;
		this.mCurrentDirectory = showPath;

		this.clearEntries();
		this.populateEntries(showPath);
	}

}

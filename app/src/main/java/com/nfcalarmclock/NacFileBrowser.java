package com.nfcalarmclock;

import android.content.Context;
import android.database.Cursor;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

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
		public void onClick(NacFileBrowser browser, NacFile.Metadata metadata,
			String path, String name);
	}

	private Context mContext;

	/**
	 * File tree of media files.
	 */
	private NacMedia.Tree mFileTree;

	/**
	 * The path view.
	 */
	private TextView mPathView;

	/**
	 * The container view for the directory/file buttons.
	 */
	private LinearLayout mContainer;

	/**
	 * Currently selected view.
	 */
	private NacImageSubTextButton mSelected;

	/**
	 * File browser click listener.
	 */
	private OnClickListener mListener;

	/**
	 */
	public NacFileBrowser(View root, int pathId, int groupId)
	{
		String home = NacFileBrowser.getHome();
		Context context = root.getContext();
		NacMedia.Tree tree = new NacMedia.Tree(home);
		this.mContext = context;
		this.mFileTree = tree;
		this.mPathView = (TextView) root.findViewById(pathId);
		this.mContainer = (LinearLayout) root.findViewById(groupId);
		this.mSelected = null;
		this.mListener = null;

		tree.scan(context);
	}

	/**
	 * Add a directory entry to the file browser.
	 */
	public void addDirectory(NacFile.Metadata metadata)
	{
		NacButtonGroup container = this.getContainer();

		if (container == null)
		{
			NacUtility.printf("NacFileBrowser : addDirectory : Container is null.");
			return;
		}

		Context context = this.getContext();
		NacImageTextButton entry = new NacImageTextButton(context);
		String name = metadata.getName();

		container.add(entry);
		entry.setText(name.equals("..") ? "(Previous folder)" : name);
		entry.setImageBackground(R.mipmap.folder);
		entry.setTag(metadata);
		entry.setOnClickListener(this);
	}

	/**
	 * Add an entry.
	 */
	public void addEntry(NacFile.Metadata metadata)
	{
		if (metadata.isDirectory())
		{
			this.addDirectory(metadata);
		}
		else if (metadata.isFile())
		{
			this.addFile(metadata);
		}
	}

	/**
	 * Add a music file entry to the file browser.
	 */
	public void addFile(NacFile.Metadata metadata)
	{
		NacButtonGroup container = this.getContainer();

		//if (file.length() == 0)
		//{
		//	return;
		//}

		if (container == null)
		{
			NacUtility.printf("NacFileBrowser : addFile : Container is null.");
			return;
		}

		Context context = this.getContext();
		NacImageSubTextButton entry = new NacImageSubTextButton(context);
		String title = NacMedia.getTitle(context, metadata);
		String artist = NacMedia.getArtist(context, metadata);

		if (title.isEmpty())
		{
			return;
		}

		container.add(entry);
		entry.setTextTitle(title);
		entry.setTextSubtitle(artist);
		entry.setImageBackground(R.mipmap.play);
		entry.setTag(metadata);
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
		Context context = this.getContext();
		LinearLayout container = this.mContainer;
		int count = container.getChildCount();

		if (count == 0)
		{
			NacButtonGroup group = (NacButtonGroup) LayoutInflater.from(context)
				.inflate(R.layout.nac_file_browser, null);
			container.addView(group);
			return group;
		}
		else
		{
			for (int i=0; i < count; i++)
			{
				View view = container.getChildAt(i);

				if (view.getVisibility() == View.VISIBLE)
				{
					return (NacButtonGroup) view;
				}
			}
		}

		return null;
	}

	/**
	 * @return The context.
	 */
	private Context getContext()
	{
		return this.mContext;
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

		NacFile.Metadata metadata = (NacFile.Metadata) view.getTag();

		return metadata.getName();
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

		NacFile.Metadata metadata = (NacFile.Metadata) view.getTag();

		return metadata.getPath();
	}

	/**
	 * @return The view displaying the current file path.
	 */
	public TextView getPathView()
	{
		return this.mPathView;
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
	 * @return The file tree.
	 */
	public NacMedia.Tree getTree()
	{
		return this.mFileTree;
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
	 */
	@Override
	public void onClick(View view)
	{
		NacFile.Metadata metadata = (NacFile.Metadata) view.getTag();
		String name = metadata.getName();
		String path = metadata.getPath();

		if (path.isEmpty())
		{
			return;
		}

		if (metadata.isFile())
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
		else if (metadata.isDirectory())
		{
			NacMedia.Tree tree = this.getTree();

			tree.cd(name);

			path = name.equals("..") ? tree.getDirectoryPath() : path;
		}

		if (this.getListener() != null)
		{
			this.getListener().onClick(this, metadata, path, name);
		}
	}

	/**
	 * Populate views into the browser.
	 */
	// To-do: The previous directory has an extra '/' in the path.
	private void populateEntries(String path)
	{
		String home = NacFileBrowser.getHome();
		NacMedia.Tree tree = this.getTree();

		this.getPathView().setText(path);

		if (!path.equals(home))
		{
			NacFile.Metadata metadata = new NacFile.Metadata(path, "..", -1);

			this.addDirectory(metadata);
		}

		for (NacFile.Metadata metadata : tree.lsSort(path))
		{
			this.addEntry(metadata);
		}
	}

	/**
	 * Change directory to previous ("../") directory.
	 */
	public boolean previousDirectory()
	{
		NacMedia.Tree tree = this.getTree();

		tree.cd("..");

		String path = tree.getDirectoryPath();

		this.show(path);
		return true;
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
	 *
	 * To-do: Change this so that when it defaults to an already selected music
	 * item. It won't mess up.
	 */
	public void select(String selectPath)
	{
		if (selectPath.isEmpty())
		{
			return;
		}

		NacButtonGroup container = this.getContainer();
		int count = container.getChildCount();
		NacMedia.Tree tree = this.getTree();
		String dir = NacFile.dirname(selectPath);
		String name = NacFile.basename(selectPath);
		String absolutePath = tree.relativeToAbsolutePath(dir, name);

		for (int i=0; i < count; i++)
		{
			View view = container.getChildAt(i);
			String viewPath = this.getPath(view);

			if (absolutePath.equals(viewPath))
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
	 * Show the directory contents at the given path and select the file.
	 */
	public void show(String directoryPath, String filePath)
	{
		this.clearEntries();
		this.populateEntries(directoryPath);
		this.getTree().cd(directoryPath);

		if ((filePath != null) && !filePath.isEmpty())
		{
			this.select(filePath);
		}
	}

	/**
	 * @see show
	 */
	public void show(String directoryPath)
	{
		this.show(directoryPath, null);
	}

	/**
	 * @see show
	 */
	public void show()
	{
		this.show(NacFileBrowser.getHome());
	}

}

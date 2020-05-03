package com.nfcalarmclock;

import android.content.Context;
import android.database.Cursor;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import java.util.Locale;

/**
 * A file browser.
 *
 * TODO Selected file gets deselected when moving directories.
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
	 * The container view for the directory/file buttons.
	 */
	private LinearLayout mContainer;

	/**
	 * Currently selected view.
	 */
	private RelativeLayout mSelected;

	/**
	 * File browser click listener.
	 */
	private OnClickListener mListener;

	/**
	 * Layout inflater.
	 */
	private LayoutInflater mInflater;

	/**
	 */
	public NacFileBrowser(View root, int groupId)
	{
		Context context = root.getContext();
		NacMedia.Tree tree = new NacMedia.Tree("");
		this.mContext = context;
		this.mFileTree = tree;
		this.mContainer = (LinearLayout) root.findViewById(groupId);
		this.mSelected = null;
		this.mListener = null;
		this.mInflater = (LayoutInflater) context.getSystemService(
			Context.LAYOUT_INFLATER_SERVICE);

		tree.scan(context);
	}

	/**
	 * Add a directory entry to the file browser.
	 *
	 * @TODO Count number of songs in subdirectories and make that the
	 *       annotation.
	 */
	public void addDirectory(NacFile.Metadata metadata)
	{
		Context context = this.getContext();
		LinearLayout container = this.getContainer();
		LayoutInflater inflater = this.getLayoutInflater();

		if ((container == null) || (inflater == null))
		{
			NacUtility.printf("NacFileBrowser : addDirectory : Container null? %b",
				container == null);
			NacUtility.printf("NacFileBrowser : addDirectory : Inflater null? %b",
				inflater == null);
			return;
		}

		NacSharedConstants cons = new NacSharedConstants(context);
		Locale locale = Locale.getDefault();
		View entry = inflater.inflate(R.layout.nac_file_entry, container, false);
		String name = metadata.getName();
		ImageView imageView = entry.findViewById(R.id.image);
		TextView titleView = entry.findViewById(R.id.title);
		TextView subtitleView = entry.findViewById(R.id.subtitle);
		TextView annotationView = entry.findViewById(R.id.annotation);

		container.addView(entry);
		imageView.setImageResource(R.mipmap.folder);
		titleView.setText(name.equals("..")
			? String.format(locale, "(%1$s)", cons.getActionPreviousFolder())
			: name);
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
		Context context = this.getContext();
		LinearLayout container = this.getContainer();
		LayoutInflater inflater = this.getLayoutInflater();

		//if (file.length() == 0)
		//{
		//	return;
		//}

		if ((container == null) || (inflater == null))
		{
			NacUtility.printf("NacFileBrowser : addFile : Container null? %b",
				container == null);
			NacUtility.printf("NacFileBrowser : addFile : Inflater null? %b",
				inflater == null);
			return;
		}

		View entry = inflater.inflate(R.layout.nac_file_entry, container, false);
		String title = NacMedia.getTitle(context, metadata);
		String artist = NacMedia.getArtist(context, metadata);
		String duration = NacMedia.getDuration(context, metadata);

		if (title.isEmpty())
		{
			return;
		}

		ImageView imageView = entry.findViewById(R.id.image);
		TextView titleView = entry.findViewById(R.id.title);
		TextView subtitleView = entry.findViewById(R.id.subtitle);
		TextView annotationView = entry.findViewById(R.id.annotation);

		container.addView(entry);
		imageView.setImageResource(R.mipmap.play);
		titleView.setText(title);
		subtitleView.setText(artist);
		subtitleView.setVisibility(View.VISIBLE);
		annotationView.setText(duration);
		entry.setTag(metadata);
		entry.setOnClickListener(this);
	}

	/**
	 * Clear the views out of the browser.
	 */
	private void clearEntries()
	{
		LinearLayout container = this.getContainer();
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
	 * Deselect the desired view.
	 */
	private void deselectView(View view)
	{
		if (view == null)
		{
			return;
		}

		Context context = this.getContext();
		TypedValue tv = new TypedValue();
		boolean success = context.getTheme().resolveAttribute(
			android.R.attr.selectableItemBackground, tv, true);

		if (tv.resourceId != 0)
		{
			view.setBackgroundResource(tv.resourceId);
		}
		else
		{
			view.setBackgroundColor(tv.data);
		}
	}

	/**
	 * @return The container view.
	 */
	private LinearLayout getContainer()
	{
		return this.mContainer;
	}

	/**
	 * @return The context.
	 */
	private Context getContext()
	{
		return this.mContext;
	}

	/**
	 * @return The layout inflater.
	 */
	private LayoutInflater getLayoutInflater()
	{
		return this.mInflater;
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
	 * @return The currently selected view.
	 */
	public RelativeLayout getSelected()
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
	private void populateEntries(String path)
	{
		NacMedia.Tree tree = this.getTree();

		if (!path.isEmpty())
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
	 */
	public void select(String selectPath)
	{
		if (selectPath.isEmpty())
		{
			return;
		}

		LinearLayout container = this.getContainer();
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
		RelativeLayout selected = this.getSelected();

		this.deselectView(selected);
		this.selectView(view);

		this.mSelected = (RelativeLayout) view;
	}

	/**
	 * Select the desired view.
	 */
	private void selectView(View view)
	{
		if (view == null)
		{
			return;
		}

		Context context = this.getContext();
		int color = ContextCompat.getColor(context, R.color.gray_light);

		view.setBackgroundColor(color);
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
		this.show("");
	}

}

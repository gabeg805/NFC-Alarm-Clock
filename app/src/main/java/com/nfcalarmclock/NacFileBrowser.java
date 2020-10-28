package com.nfcalarmclock;

import android.content.Context;
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
 */
public class NacFileBrowser
	implements View.OnClickListener
{

	/**
	 * Click listener for the file browser.
	 */
	public interface OnBrowserClickedListener
	{
		public void onBrowserClicked(NacFileBrowser browser,
			NacFile.Metadata metadata, String path, String name);
	}

	/**
	 * Context.
	 */
	private final Context mContext;

	/**
	 * File tree of media files.
	 */
	private final NacMedia.Tree mFileTree;

	/**
	 * The container view for the directory/file buttons.
	 */
	private final LinearLayout mContainer;

	/**
	 * Currently selected view.
	 */
	private RelativeLayout mSelectedView;

	/**
	 * File browser click listener.
	 */
	private OnBrowserClickedListener mOnBrowserClickedListener;

	/**
	 */
	public NacFileBrowser(View root, int groupId)
	{
		Context context = root.getContext();
		NacMedia.Tree tree = new NacMedia.Tree("");
		this.mContext = context;
		this.mFileTree = tree;
		this.mContainer = root.findViewById(groupId);
		this.mSelectedView = null;
		this.mOnBrowserClickedListener = null;

		tree.scan(context);
	}

	/**
	 * Add a directory entry to the file browser.
	 *
	 * TODO Count number of songs in subdirectories and make that the
	 *      annotation.
	 */
	public void addDirectory(LayoutInflater inflater, NacFile.Metadata metadata)
	{
		Context context = this.getContext();
		LinearLayout container = this.getContainer();

		if (container == null)
		{
			NacUtility.printf("NacFileBrowser : addDirectory : Container is null");
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
	 * Add a music file entry to the file browser.
	 */
	public void addFile(LayoutInflater inflater, NacFile.Metadata metadata)
	{
		Context context = this.getContext();
		LinearLayout container = this.getContainer();

		if (container == null)
		{
			NacUtility.printf("NacFileBrowser : addFile : Container is null");
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
	 * Call the on browser clicked listener.
	 */
	private void callOnBrowserClickedListener(NacFile.Metadata metadata,
		String path, String name)
	{
		OnBrowserClickedListener listener = this.getOnBrowserClickedListener();
		if (listener != null)
		{
			listener.onBrowserClicked(this, metadata, path, name);
		}
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
	 * @return The OnBrowserClickedListener.
	 */
	private OnBrowserClickedListener getOnBrowserClickedListener()
	{
		return this.mOnBrowserClickedListener;
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
	 * @return The file metadata of the currently selected view.
	 */
	public NacFile.Metadata getSelectedMetadata()
	{
		View view = this.getSelectedView();
		return (view != null) ? (NacFile.Metadata) view.getTag() : null;
	}

	/**
	 * @return The currently selected view.
	 */
	public RelativeLayout getSelectedView()
	{
		return this.mSelectedView;
	}

	/**
	 * @return The file tree.
	 */
	public NacMedia.Tree getTree()
	{
		return this.mFileTree;
	}

	/**
	 * @return True if in the same directory as the selected view, and False
	 *         otherwise.
	 */
	public boolean inSelectedDirectory(String directoryPath)
	{
		NacFile.Metadata metadata = this.getSelectedMetadata();
		return (metadata != null) && metadata.getDirectory().equals(directoryPath);
	}

	/**
	 * @return True if something is selected and False otherwise.
	 */
	public boolean isSelected()
	{
		return (this.getSelectedView() != null);
	}

	/**
	 * @return True if the given path matches the currently selected path, and
	 *         False otherwise.
	 */
	public boolean isSelected(String path)
	{
		NacFile.Metadata metadata = this.getSelectedMetadata();
		if (metadata == null)
		{
			return false;
		}

		String selectedPath = metadata.getPath();
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

		this.callOnBrowserClickedListener(metadata, path, name);
	}

	/**
	 * Populate views into the browser.
	 */
	private void populateEntries(String path)
	{
		Context context = this.getContext();
		NacMedia.Tree tree = this.getTree();
		LayoutInflater inflater = LayoutInflater.from(context);

		if (!path.isEmpty())
		{
			NacFile.Metadata metadata = new NacFile.Metadata(path, "..", -1);
			this.addDirectory(inflater, metadata);
		}

		for (NacFile.Metadata metadata : tree.lsSort(path))
		{
			if (metadata.isDirectory())
			{
				this.addDirectory(inflater, metadata);
			}
			else if (metadata.isFile())
			{
				this.addFile(inflater, metadata);
			}
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
	public void setOnBrowserClickedListener(OnBrowserClickedListener listener)
	{
		this.mOnBrowserClickedListener = listener;
	}

	/**
	 * @see #select(View)
	 */
	public void select(String selectPath)
	{
		if ((selectPath == null) || selectPath.isEmpty())
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
	 *
	 * @param  view  The view to highlight.
	 */
	public void select(View view)
	{
		RelativeLayout selected = this.getSelectedView();

		this.deselectView(selected);
		this.selectView(view);

		this.mSelectedView = (RelativeLayout) view;
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
	 * @see #show(String)
	 */
	public void show()
	{
		this.show("");
	}

	/**
	 * @see #show(String, String)
	 */
	public void show(String directoryPath)
	{
		NacFile.Metadata metadata = this.getSelectedMetadata();
		String filePath = null;
		
		if (this.inSelectedDirectory(directoryPath))
		{
			filePath = metadata.getPath();
		}

		this.show(directoryPath, filePath);
	}

	/**
	 * Show the directory contents at the given path and select the file.
	 *
	 * @param  directoryPath  The path of the directory to show.
	 * @param  filePath       The path of the file to highlight.
	 */
	public void show(String directoryPath, String filePath)
	{
		this.clearEntries();
		this.populateEntries(directoryPath);
		this.getTree().cd(directoryPath);
		this.select(filePath);
	}

}

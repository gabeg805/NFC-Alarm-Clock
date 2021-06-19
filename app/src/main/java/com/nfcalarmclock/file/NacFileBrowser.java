package com.nfcalarmclock.file;

import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.core.content.ContextCompat;

import com.nfcalarmclock.NacUtility;
import com.nfcalarmclock.R;
import com.nfcalarmclock.audio.media.NacMedia;
import com.nfcalarmclock.shared.NacSharedConstants;

import java.util.Locale;

/**
 * A file browser.
 */
@SuppressWarnings({"RedundantSuppression", "UnnecessaryInterfaceModifier"})
public class NacFileBrowser
	implements View.OnClickListener
{

	/**
	 * Click listener for the file browser.
	 */
	public interface OnBrowserClickedListener
	{
		@SuppressWarnings("unused")
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
	 *     annotation.
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
		//TextView annotationView = entry.findViewById(R.id.annotation);

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
		Resources.Theme theme = context.getTheme();

		theme.resolveAttribute(android.R.attr.selectableItemBackground,
			tv, true);

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
	 * @return The file metadata object contained in the view.
	 */
	public NacFile.Metadata getFileMetadata(View view)
	{
		if (view == null)
		{
			return null;
		}

		return (NacFile.Metadata) view.getTag();
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
	 * @return true if in the same directory as the selected view, and false
	 *     otherwise.
	 */
	@SuppressWarnings("unused")
	public boolean inSelectedDirectory(String dir)
	{
		View view = this.getSelectedView();
		NacFile.Metadata metadata = this.getFileMetadata(view);

		return (metadata != null) && metadata.getDirectory().equals(dir);
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
		View view = this.getSelectedView();
		NacFile.Metadata metadata = this.getFileMetadata(view);

		if ((metadata == null) || path.isEmpty())
		{
			return false;
		}

		return metadata.getPath().equals(path);
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
	public void previousDirectory()
	{
		LinearLayout container = this.getContainer();
		View entry = container.getChildAt(0);
		NacFile.Metadata metadata = this.getFileMetadata(entry);

		if ((entry != null) && metadata.getName().equals(".."))
		{
			this.onClick(entry);
		}
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
	public void select(String name)
	{
		//if ((selectPath == null) || selectPath.isEmpty())
		if (NacFile.isEmpty(name))
		{
			return;
		}

		LinearLayout container = this.getContainer();
		int count = container.getChildCount();

		for (int i=0; i < count; i++)
		{
			View view = container.getChildAt(i);
			NacFile.Metadata metadata = this.getFileMetadata(view);

			if (metadata.getName().equals(name))
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
	 * Show the directory contents at the given path.
	 *
	 * @param  dir  The path of the directory to show.
	 */
	public void show(String dir)
	{
		this.clearEntries();
		this.populateEntries(dir);
		this.getTree().cd(dir);
	}

}

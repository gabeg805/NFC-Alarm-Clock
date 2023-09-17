package com.nfcalarmclock.file.browser;

import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import com.nfcalarmclock.R;
import com.nfcalarmclock.file.NacFile;
import com.nfcalarmclock.file.NacFileTree;

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
	 * View model for the file browser.
	 */
	private final NacFileBrowserViewModel mViewModel;

	/**
	 */
	public NacFileBrowser(LifecycleOwner lifecycleOwner, View root, int groupId)
	{
		Context context = root.getContext();
		this.mContext = context;
		this.mContainer = root.findViewById(groupId);
		this.mSelectedView = null;
		this.mOnBrowserClickedListener = null;
		this.mViewModel = new ViewModelProvider((ViewModelStoreOwner) context).get(
			NacFileBrowserViewModel.class);

		// Setup the view model observeR
		this.setupViewModelObserver(lifecycleOwner);
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
		// Unable to get the metadata object from the view
		if (view == null)
		{
			return null;
		}

		// Get the metadata object via the tag of the view
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
	 * Get the file browser view model.
	 *
	 * @return The file browser view model.
	 */
	private NacFileBrowserViewModel getViewModel()
	{
		return this.mViewModel;
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
	 * Check if at the root level of the file tree or not.
	 *
	 * @return True if at the root level of the file tree, and False otherwise.
	 */
	public boolean isAtRoot()
	{
		// Get the first child view in this container
		LinearLayout container = this.getContainer();
		View entry = container.getChildAt(0);

		// Unable to get the first child at this level, so cannot determine if
		// at the root level or not
		if (entry == null)
		{
			return false;
		}

		// Get the metadata of the first child
		NacFile.Metadata metadata = this.getFileMetadata(entry);

		// Ensure that metadata is in fact an object and it does not equal the
		// previous directory string ".."
		return (metadata != null) && !metadata.getName().equals("..");
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

		// Unable to determine if the file at the path is selected or not
		// because the metadata object was unable to be resolved or the path was empty
		if ((metadata == null) || path.isEmpty())
		{
			return false;
		}

		// Check if the path of the metadata object of the selected view is
		// equal to the path that was passed in
		return metadata.getPath().equals(path);
	}

	/**
	 */
	@Override
	public void onClick(View view)
	{
		NacFile.Metadata metadata = this.getFileMetadata(view);

		// Unable to get the metadata object from the view. Do not continue
		if (metadata == null)
		{
			return;
		}

		// Get the name and filepath of the file that is represented by the view
		String name = metadata.getName();
		String path = metadata.getPath();

		// Unable to get a path from the view. Do not continue
		if (path.isEmpty())
		{
			return;
		}

		// A file was clicked
		if (metadata.isFile())
		{
			// The file is already selected, so deselect it
			if (this.isSelected(path))
			{
				this.deselect();
			}
			// The file has not been selected yet, so select it
			else
			{
				this.select(view);
			}
		}
		// A directory was clicked
		else if (metadata.isDirectory())
		{
			// Change directory to the directory that was clicked
			NacFileTree tree = this.getViewModel().getRepository().getFileTree();

			tree.cd(name);

			// Determine the path of the directory that was clicked
			path = name.equals("..") ? tree.getDirectoryPath() : path;
		}

		// Call the listener for when an item is clicked in the file browser
		this.callOnBrowserClickedListener(metadata, path, name);
	}

	/**
	 * Change directory to previous ("../") directory.
	 */
	public void previousDirectory()
	{
		// Get the first child view in this container
		LinearLayout container = this.getContainer();
		View entry = container.getChildAt(0);

		// Unable to get the first child at this level, so cannot determine if
		// at the root level or not
		if (entry == null)
		{
			return;
		}

		// Get the metadata of the first child
		NacFile.Metadata metadata = this.getFileMetadata(entry);

		// Go to the previous directory if metadata equals the previous
		// directory string ".."
		if ((metadata != null) && metadata.getName().equals(".."))
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
	 * Setup the view model observer.
	 */
	public void setupViewModelObserver(LifecycleOwner lifecycleOwner)
	{
		NacFileBrowserViewModel viewModel = this.getViewModel();

		// Observe the view model data
		viewModel.getListingLiveData().observe(lifecycleOwner,
			listing ->
				{
					// Get the container
					LinearLayout container = getContainer();

					// Repopulate the list of views
					viewModel.repopulate(container, listing, NacFileBrowser.this);
				});
	}

	/**
	 * Show the directory contents at the given path.
	 *
	 * @param  dir  The path of the directory to show.
	 */
	public void show(String dir)
	{
		this.getViewModel().show(dir);
	}

}

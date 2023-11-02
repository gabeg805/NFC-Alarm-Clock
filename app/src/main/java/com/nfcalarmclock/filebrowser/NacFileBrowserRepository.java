package com.nfcalarmclock.filebrowser;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import androidx.lifecycle.MutableLiveData;
import com.nfcalarmclock.R;
import com.nfcalarmclock.file.NacFile;
import com.nfcalarmclock.file.NacFileTree;
import com.nfcalarmclock.media.NacMedia;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.List;
import java.util.Locale;

/**
 * File browser repository.
 */
public class NacFileBrowserRepository
{

	/**
	 * File tree of media files.
	 */
	private final NacFileTree mFileTree;

	/**
	 * Listing of file metadata.
	 */
	private final MutableLiveData<List<NacFile.Metadata>> mListingLiveData;

	/**
	 * Flag to check if is scanning the file tree or not.
	 */
	private boolean mIsScanning;

	/**
	 * Flag to check if is show a file listing or not.
	 */
	private boolean mIsShowing;

	/**
	 */
	public NacFileBrowserRepository(Context context)
	{
		// Set member variable
		this.mFileTree = new NacFileTree("");
		this.mListingLiveData = new MutableLiveData<>(new ArrayList<>());
		this.mIsScanning = true;
		this.mIsShowing = true;

		// Prepare a handler
		Looper looper = context.getMainLooper();
		Handler handler = new Handler(looper);

		// Scan the file tree in the background
		handler.post(() -> this.scan(context));
	}

	/**
	 * Add a directory entry to the file listing.
	 * <p>
	 * TODO Count number of songs in subdirectories and make that the
	 *     annotation.
	 */
	public void addDirectory(Context context, NacFile.Metadata metadata)
	{
		List<NacFile.Metadata> listing = this.getListingLiveData().getValue();
		Locale locale = Locale.getDefault();
		String name = metadata.getName();
		String previousFolder = context.getString(R.string.action_previous_folder);

		// Determine what the extra data will be. This will be the name shown to the
		// user
		String extra = name.equals("..")
			? String.format(locale, "(%1$s)", previousFolder)
			: name;

		// Set the extra data
		metadata.setExtra(extra);

		// Add to the listing
		listing.add(metadata);
	}

	/**
	 * Add a music file entry to the file listing.
	 */
	public void addFile(Context context, NacFile.Metadata metadata)
	{
		List<NacFile.Metadata> listing = this.getListingLiveData().getValue();

		// Determine the extra data. This will be shown to the user
		String title = NacMedia.getTitle(context, metadata);
		String artist = NacMedia.getArtist(context, metadata);
		String duration = NacMedia.getDuration(context, metadata);

		// No title so there is nothing to show the user. Exit here
		if (title.isEmpty())
		{
			return;
		}

		// Set the extra data
		String[] extra = new String[] { title, artist, duration };

		metadata.setExtra(extra);

		// Add to the listing
		listing.add(metadata);
	}

	/**
	 * @return The file tree.
	 */
	public NacFileTree getFileTree()
	{
		return this.mFileTree;
	}

	/**
	 * @return The listing of file metadata.
	 */
	public MutableLiveData<List<NacFile.Metadata>> getListingLiveData()
	{
		return this.mListingLiveData;
	}

	/**
	 * Check if the repository is busy scanning or showing a file listing.
	 *
	 * @return True if the repository is busy, and False otherwise.
	 */
	public boolean isBusy()
	{
		return this.isScanning() || this.isShowing();
	}

	/**
	 * Check if the file tree is being scanned or not.
	 *
	 * @return True if the repository is being scanned, and False otherwise.
	 */
	public boolean isScanning()
	{
		return this.mIsScanning;
	}

	/**
	 * Check if a file listing is being shown.
	 *
	 * @return True if a file listing is being shown, and False otherwise.
	 */
	public boolean isShowing()
	{
		return this.mIsShowing;
	}

	/**
	 * Scan the file tree.
	 */
	public void scan(Context context)
	{
		// Set scanning flag
		this.mIsScanning = true;

		// Scan the file tree
		this.getFileTree().scan(context);

		// Disable scanning flag
		this.mIsScanning = false;
	}

	/**
	 * Show the contents of the file listing and tree.
	 */
	public void show(Context context, String path)
	{
		// Set the showing flag
		this.mIsShowing = true;

		// Wait until scanning is complete
		while (this.isScanning())
		{
			try
			{
				TimeUnit.MILLISECONDS.sleep(50);
			}
			catch (InterruptedException ignored)
			{
			}
		}

		NacFileTree tree = this.getFileTree();
		List<NacFile.Metadata> listing = this.getListingLiveData().getValue();

		// Clear the file listing
		listing.clear();

		// Not at the root level so add the previous directory to the listing.
		// Note: An empty path indicates the root level
		if (!path.isEmpty())
		{
			NacFile.Metadata metadata = new NacFile.Metadata(path, "..", -1);

			this.addDirectory(context, metadata);
		}

		// Iterate over each file at the given path
		for (NacFile.Metadata metadata : tree.lsSort(path))
		{

			// Add a directory
			if (metadata.isDirectory())
			{
				this.addDirectory(context, metadata);
			}
			// Add a file
			else if (metadata.isFile())
			{
				this.addFile(context, metadata);
			}

		}

		// Change directory to the new path
		this.getFileTree().cd(path);

		// Notify observers of change
		this.getListingLiveData().postValue(listing);

		// Disable the showing flag
		this.mIsShowing = false;
	}

}


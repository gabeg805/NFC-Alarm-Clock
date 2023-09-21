package com.nfcalarmclock.filebrowser;

import android.app.Application;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import com.nfcalarmclock.R;
import com.nfcalarmclock.file.NacFile;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * File browser view model.
 */
public class NacFileBrowserViewModel
	extends AndroidViewModel
{

	/**
	 * Repository of file browser information.
	 */
	private final NacFileBrowserRepository mRepository;

	/**
	 */
	public NacFileBrowserViewModel(@NonNull Application app)
	{
		super(app);

		this.mRepository = new NacFileBrowserRepository(app);
	}

	/**
	 * Add a directory entry to the file browser.
	 * <p>
	 * TODO Count number of songs in subdirectories and make that the
	 *     annotation.
	 */
	public View addDirectory(LayoutInflater inflater, LinearLayout container,
		NacFile.Metadata metadata)
	{
		String extra = (String) metadata.getExtra();

		// Create the file entry view
		View entry = inflater.inflate(R.layout.nac_file_entry, container, false);
		ImageView imageView = entry.findViewById(R.id.image);
		TextView titleView = entry.findViewById(R.id.title);
		//TextView annotationView = entry.findViewById(R.id.annotation);

		// Set the image and text of the file entry
		imageView.setImageResource(R.mipmap.folder);
		titleView.setText(extra);

		return entry;
	}

	/**
	 * Add a music file entry to the file browser.
	 */
	public View addFile(LayoutInflater inflater, LinearLayout container,
		NacFile.Metadata metadata)
	{
		String[] extra = (String[]) metadata.getExtra();
		String title = extra[0];
		String artist = extra[1];
		String duration = extra[2];

		// Create the file entry view
		View entry = inflater.inflate(R.layout.nac_file_entry, container, false);
		ImageView imageView = entry.findViewById(R.id.image);
		TextView titleView = entry.findViewById(R.id.title);
		TextView subtitleView = entry.findViewById(R.id.subtitle);
		TextView annotationView = entry.findViewById(R.id.annotation);

		// Set the image and text of the file entry
		imageView.setImageResource(R.mipmap.play);
		titleView.setText(title);
		subtitleView.setText(artist);
		subtitleView.setVisibility(View.VISIBLE);
		annotationView.setText(duration);

		return entry;
	}

	/**
	 * Add views from the file listing into the file browser.
	 */
	public void addToFileBrowser(LinearLayout container,
		List<NacFile.Metadata> listing, View.OnClickListener listener)
	{
		// Container is null. Exit here
		if (container == null)
		{
			return;
		}

		Context context = getApplication();
		LayoutInflater inflater = LayoutInflater.from(context);
		View entry = null;

		// Iterate over each file at the given path
		for (NacFile.Metadata metadata : listing)
		{

			// Add a directory
			if (metadata.isDirectory())
			{
				entry = this.addDirectory(inflater, container, metadata);
			}
			// Add a file
			else if (metadata.isFile())
			{
				entry = this.addFile(inflater, container, metadata);
			}

			// Entry is not defined so skip to the next item in the listing
			if (entry == null)
			{
				continue;
			}

			// Add metadata to the view and set the click listener
			entry.setTag(metadata);
			entry.setOnClickListener(listener);

			// Add the entry to the file browser
			container.addView(entry);
		}
	}

	/**
	 * Clear all views in the file browser.
	 */
	public void clearFileBrowser(LinearLayout container)
	{
		// Container is null. Exit here
		if (container == null)
		{
			return;
		}

		// Clear all views from the file browser
		container.removeAllViews();
	}

	/**
	 * Get the directory listing.
	 */
	public MutableLiveData<List<NacFile.Metadata>> getListingLiveData()
	{
		return this.getRepository().getListingLiveData();
	}

	/**
	 * @return The file browser repository.
	 */
	public NacFileBrowserRepository getRepository()
	{
		return this.mRepository;
	}

	/**
	 * Repopulate the views in the file browser.
	 */
	public void repopulate(LinearLayout container, List<NacFile.Metadata> listing,
		View.OnClickListener listener)
	{
		// Container is null. Exit here
		if (container == null)
		{
			return;
		}

		// Clear everything in the file browser
		this.clearFileBrowser(container);

		// Add listing to the file browser
		this.addToFileBrowser(container, listing, listener);
	}

	/**
	 * Show the listing of files and directories at the given path.
	 */
	public void show(String path)
	{
		Context context = getApplication();
		NacFileBrowserRepository repo = this.getRepository();

		// Refresh the listing of files and directories asynchronously
		ExecutorService executor = Executors.newSingleThreadExecutor();

		executor.submit(() -> repo.show(context, path));
	}

}


package com.nfcalarmclock.filebrowser

import android.app.Application
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.nfcalarmclock.R
import com.nfcalarmclock.file.NacFile
import java.util.concurrent.Executors

/**
 * File browser view model.
 */
class NacFileBrowserViewModel(app: Application)
	: AndroidViewModel(app)
{

	/**
	 * Repository of file browser information.
	 */
	val repository: NacFileBrowserRepository

	/**
	 * The directory listing.
	 */
	val listingLiveData: MutableLiveData<MutableList<NacFile.Metadata>>
		get() = repository.listingLiveData

	/**
	 * Constructor.
	 */
	init
	{
		repository = NacFileBrowserRepository(app)
	}

	/**
	 * Add a directory entry to the file browser.
	 *
	 * TODO Count number of songs in subdirectories and make that the
	 * annotation.
	 */
	fun addDirectory(inflater: LayoutInflater, container: LinearLayout?,
		metadata: NacFile.Metadata): View
	{
		// Create the file entry view
		val entry = inflater.inflate(R.layout.nac_file_entry, container, false)
		val imageView = entry.findViewById<ImageView>(R.id.image)
		val titleView = entry.findViewById<TextView>(R.id.title)
		//TextView annotationView = entry.findViewById(R.id.annotation);

		// Set the image and text of the file entry
		imageView.setImageResource(R.mipmap.folder)
		titleView.text = metadata.extra as String

		return entry
	}

	/**
	 * Add a music file entry to the file browser.
	 */
	fun addFile(inflater: LayoutInflater, container: LinearLayout?,
		metadata: NacFile.Metadata): View
	{
		// Get the extra data (title, artist, duration)
		val extra = metadata.extra as Array<String>
		val title = extra[0]
		val artist = extra[1]
		val duration = extra[2]

		// Create the file entry view
		val entry = inflater.inflate(R.layout.nac_file_entry, container, false)
		val imageView = entry.findViewById<ImageView>(R.id.image)
		val titleView = entry.findViewById<TextView>(R.id.title)
		val subtitleView = entry.findViewById<TextView>(R.id.subtitle)
		val annotationView = entry.findViewById<TextView>(R.id.annotation)

		// Set the image and text of the file entry
		imageView.setImageResource(R.mipmap.play)
		titleView.text = title
		subtitleView.text = artist
		subtitleView.visibility = View.VISIBLE
		annotationView.text = duration

		return entry
	}

	/**
	 * Add views from the file listing into the file browser.
	 */
	fun addToFileBrowser(container: LinearLayout?,
		listing: List<NacFile.Metadata>, listener: View.OnClickListener?)
	{
		// Container is null. Exit here
		if (container == null)
		{
			return
		}

		// Check if the repo is busy scanning or showing a new file listing
		// in which this this method will just be called again via the observer
		if (repository.isBusy)
		{
			return
		}

		// Get stuff needed to inflate the views
		val context: Context = getApplication()
		val inflater = LayoutInflater.from(context)
		var entry: View? = null

		// Iterate over each file at the given path
		for (metadata in listing)
		{
			// Add a directory
			if (metadata.isDirectory)
			{
				entry = addDirectory(inflater, container, metadata)
			}
			else if (metadata.isFile)
			{
				entry = addFile(inflater, container, metadata)
			}

			// Entry is not defined so skip to the next item in the listing
			if (entry == null)
			{
				continue
			}

			// Add metadata to the view and set the click listener
			entry.tag = metadata
			entry.setOnClickListener(listener)

			// Add the entry to the file browser
			container.addView(entry)

			// Same repo busy check as above
			if (repository.isBusy)
			{
				return
			}
		}
	}

	/**
	 * Clear all views in the file browser.
	 */
	fun clearFileBrowser(container: LinearLayout?)
	{
		// Container is null. Exit here
		if (container == null)
		{
			return
		}

		// Clear all views from the file browser
		container.removeAllViews()
	}

	/**
	 * Repopulate the views in the file browser.
	 */
	fun repopulate(container: LinearLayout?, listing: List<NacFile.Metadata>,
		listener: View.OnClickListener?)
	{
		// Container is null. Exit here
		if (container == null)
		{
			return
		}

		// Clear everything in the file browser
		clearFileBrowser(container)

		// Add listing to the file browser
		addToFileBrowser(container, listing, listener)
	}

	/**
	 * Show the listing of files and directories at the given path.
	 */
	fun show(path: String?)
	{
		val context: Context = getApplication()

		// Refresh the listing of files and directories asynchronously
		val executor = Executors.newSingleThreadExecutor()
		executor.submit { repository.show(context, path!!) }
	}

}
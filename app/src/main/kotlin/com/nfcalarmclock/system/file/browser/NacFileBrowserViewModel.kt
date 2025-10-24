package com.nfcalarmclock.system.file.browser

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nfcalarmclock.system.file.NacFile
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

/**
 * File browser view model.
 */
class NacFileBrowserViewModel(app: Application)
	: AndroidViewModel(app)
{

	/**
	 * Repository of file browser information.
	 */
	private val repository: NacFileBrowserRepository = NacFileBrowserRepository()

	/**
	 * Current metadata added to the repository.
	 */
	val currentMetadata: SharedFlow<NacFile.Metadata?> = repository.currentMetadata

	/**
	 * Constructor.
	 */
	init
	{
		// Scan the repository
		viewModelScope.launch {
			repository.scan(app)
		}
	}

	/**
	 * Change directory.
	 */
	fun cd(metadata: NacFile.Metadata) : String
	{
		// Change directory
		repository.fileTree.cd(metadata.name)

		// Determine the path of the directory that was clicked
		return if (metadata.name == NacFile.PREVIOUS_DIRECTORY)
		{
			// Previous directory
			repository.fileTree.directoryPath
		}
		else
		{
			// Current directory
			metadata.path
		}
	}

	/**
	 * Clear the listing.
	 */
	fun clear()
	{
		viewModelScope.launch {
			repository.clear()
		}
	}

	/**
	 * Show the listing at the given path.
	 */
	fun show(path: String, unit: () -> Unit = {})
	{
		val context: Context = getApplication()

		viewModelScope.launch {

			// Show the listing
			repository.show(context, path)

			// Call the unit
			unit()

		}
	}

}
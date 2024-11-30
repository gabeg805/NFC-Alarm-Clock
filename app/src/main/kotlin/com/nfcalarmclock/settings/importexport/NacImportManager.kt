package com.nfcalarmclock.settings.importexport

import android.content.Context
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.lifecycleScope
import com.nfcalarmclock.R
import com.nfcalarmclock.db.NacAlarmDatabase
import com.nfcalarmclock.shared.NacSharedPreferences
import com.nfcalarmclock.system.file.unzipFile
import com.nfcalarmclock.util.NacUtility
import java.io.File
import java.io.InputStream

/**
 * Import a zip file containg a shared preference file, as a csv, and a database file.
 */
class NacImportManager(fragment: Fragment)
{

	/**
	 * Import the shared preferences and database files from a zip file.
	 */
	private val importContent = fragment.registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->

		// Get the context
		val context = fragment.requireContext()

		// Get the input stream for the zip file
		val inputStream = getImportStream(context, uri) ?: return@registerForActivityResult

		// Import the zip file
		import(context, inputStream, fragment.lifecycleScope)

	}

	/**
	 * Get the stream for an import.
	 */
	private fun getImportStream(context: Context, uri: Uri?): InputStream?
	{
		// Check if URI is populated
		if (uri == null)
		{
			return null
		}

		// Return the input stream
		return context.contentResolver.openInputStream(uri).also {

			// Check if stream is not valid and show message if it is not
			if (it == null)
			{
				NacUtility.quickToast(context, R.string.error_message_unable_to_open_import_export_stream)
			}

		}
	}

	/**
	 * Import the shared preferences and database files from a zip file.
	 */
	fun import(
		context: Context,
		inputStream: InputStream,
		lifecycleCoroutineScope: LifecycleCoroutineScope
	)
	{
		// Get the shared preferences
		val sharedPreferences = NacSharedPreferences(context)

		// Unzip the files and iterate over each one
		unzipFile(inputStream, context.filesDir).forEach {

			// Create a file object
			val file = File(it)

			// CSV file
			if (it.endsWith(".csv"))
			{
				// Copy data from the imported csv file and then delete the file
				sharedPreferences.copyFromCsv(context, file)
				file.delete()

				// Set the refresh main activity setting so that colors can be redrawn
				sharedPreferences.shouldRefreshMainActivity = true
			}
			// Database file
			else if (it.endsWith(".db"))
			{
				// Copy data from the imported database. Use regular context so that the
				// imported database can be opened from the regular context filesDir
				NacAlarmDatabase.copyFromDb(context, file, lifecycleCoroutineScope)
			}

		}
	}

	/**
	 * Launch the import process.
	 */
	fun launch()
	{
		importContent.launch("application/zip")
	}

}
package com.nfcalarmclock.settings.importexport

import android.content.Context
import android.content.Intent
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.lifecycleScope
import com.nfcalarmclock.R
import com.nfcalarmclock.db.NacAlarmDatabase
import com.nfcalarmclock.shared.NacSharedPreferences
import com.nfcalarmclock.system.NacLifecycleService
import com.nfcalarmclock.system.file.unzipFile
import com.nfcalarmclock.view.quickToast
import kotlinx.coroutines.launch
import java.io.File
import java.io.InputStream

/**
 * Service for importing a zip file containing shared preferences and a database.
 */
class NacImportService
	: NacLifecycleService()
{

	/**
	 * Called when the service is started.
	 */
	override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int
	{
		// Super
		super.onStartCommand(intent, flags, startId)

		// Get the Uri
		val uri = intent?.data

		// Uri is valid
		if (uri != null)
		{
			// Open the Uri
			val inputStream = contentResolver.openInputStream(uri)

			// Import the file
			if (inputStream != null)
			{
				quickToast(this, "Importing...")
				import(this, inputStream, lifecycleScope)
				return START_NOT_STICKY
			}
			// Unable to open the Uri
			else
			{
				quickToast(this, R.string.error_message_unable_to_open_import_export_stream)
			}

		}

		// Stop the service
		stopThisService()

		return START_NOT_STICKY
	}

	/**
	 * Import the shared preferences and database files from a zip file.
	 */
	fun import(
		context: Context,
		inputStream: InputStream,
		coroutineScope: LifecycleCoroutineScope
	)
	{
		// Flags for the two types of files that can be found
		var wasCsvFound = false
		var wasDbFound = false

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

				// Set the refresh main activity flag
				sharedPreferences.shouldRefreshMainActivity = true
				wasCsvFound = true
			}
			// Database file
			else if (it.endsWith(".db"))
			{
				// Copy data from the imported database. Use regular context so that the
				// imported database can be opened from the regular context filesDir
				coroutineScope.launch {
					NacAlarmDatabase.copyFromDb(context, file)
					file.delete()
					stopThisService()
				}

				// Set the refresh main activity flag
				sharedPreferences.shouldRefreshMainActivity = true
				wasDbFound = true
			}

		}

		// Stop the service if only the csv file was found
		if (wasCsvFound && !wasDbFound)
		{
			stopThisService()
		}
	}

}
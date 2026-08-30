package com.nfcalarmclock.settings.importexport

import android.content.Context
import android.content.Intent
import androidx.lifecycle.lifecycleScope
import com.nfcalarmclock.R
import com.nfcalarmclock.db.NacAlarmDatabase
import com.nfcalarmclock.log.NacLog
import com.nfcalarmclock.shared.NacSharedPreferences
import com.nfcalarmclock.system.NacLifecycleService
import com.nfcalarmclock.system.file.unzipFile
import com.nfcalarmclock.view.quickToast
import com.nfcalarmclock.view.toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream

/**
 * Service for importing a zip file containing shared preferences and a database.
 */
class NacImportService
	: NacLifecycleService()
{

	/**
	 * Delay finishing the service to ensure the database has enough time to import.
	 */

	/**
	 * Service is started.
	 */
	override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int
	{
		// Super
		super.onStartCommand(intent, flags, startId)

		NacLog.i("Starting import service")

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
				quickToast(this, R.string.message_import_started)
				lifecycleScope.launch {
					import(this@NacImportService, inputStream)
				}
				return START_NOT_STICKY
			}
			// Unable to open the Uri
			else
			{
				NacLog.e("Unable to import files")
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
	suspend fun import(context: Context, inputStream: InputStream)
	{
		// Get the shared preferences
		val sharedPreferences = NacSharedPreferences(context)

		// Keep track of the number of the number of alarms/timers that have their media cleared
		// because the file does not exist on this device
		var clearedMediaNum = 0

		NacLog.i("Unzipping import file")

		// Unzip the files and iterate over each one
		unzipFile(inputStream, context.filesDir).forEach {

			// Create a file object
			val file = File(it)

			// CSV file
			if (it.endsWith(".csv"))
			{
				NacLog.i("Importing shared preferences")

				// Copy data from the imported csv file and then delete the file
				sharedPreferences.copyFromCsv(context, file)
				file.delete()

				// Set the refresh main activity flag
				sharedPreferences.shouldRefreshMainActivity = true
			}
			// Database file
			else if (it.endsWith(".db"))
			{
				NacLog.i("Importing database")

				// Copy data from the imported database. Use regular context so that the
				// imported database can be opened from the regular context filesDir
				clearedMediaNum = NacAlarmDatabase.copyFromDb(context, file)
				file.delete()

				// Set the refresh main activity flag
				sharedPreferences.shouldRefreshMainActivity = true
			}

		}

		NacLog.i("Import completed")

		// Show success message
		withContext(Dispatchers.Main) {

			// Toast number of alarms/timers had media cleared
			if (clearedMediaNum > 0)
			{
				val message = resources.getQuantityString(
					R.plurals.import_cleared_media_due_to_not_found,
					clearedMediaNum,
					clearedMediaNum)
				toast(context, message)
			}

			// Toast import completed
			quickToast(context, R.string.message_import_completed)
		}

		// Done with the import so stop the service
		stopThisService()
	}

}
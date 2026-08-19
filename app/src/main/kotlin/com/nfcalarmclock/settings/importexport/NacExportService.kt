package com.nfcalarmclock.settings.importexport

import android.content.Context
import android.content.Intent
import androidx.lifecycle.lifecycleScope
import androidx.sqlite.db.SimpleSQLiteQuery
import com.nfcalarmclock.R
import com.nfcalarmclock.db.NacAlarmDatabase
import com.nfcalarmclock.shared.NacSharedPreferences
import com.nfcalarmclock.system.NacLifecycleService
import com.nfcalarmclock.system.file.zipFiles
import com.nfcalarmclock.system.getDeviceProtectedStorageContext
import com.nfcalarmclock.view.quickToast
import kotlinx.coroutines.launch
import java.io.File
import java.io.OutputStream

/**
 * Service for exporting the shared preferences and database files to a zip file.
 */
class NacExportService
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
			val outputStream = contentResolver.openOutputStream(uri)

			// Export data to a zip file
			if (outputStream != null)
			{
				quickToast(this, R.string.message_export_started)
				export(this, outputStream)
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
	 * Export the shared preferences and database files to a zip file.
	 */
	private fun export(context: Context, outputStream: OutputStream)
	{
		// Get the context depending on if the device can use direct boot or not
		val deviceContext = getDeviceProtectedStorageContext(context)

		// Get the shared preferences and csv file
		val sharedPreferences = NacSharedPreferences(deviceContext)
		val csvFile = File("${context.filesDir}/shared_preferences.csv")

		// Get the database files
		val dbFile = NacAlarmDatabase.getPath(deviceContext)
		val dbShm = File("${dbFile.path}-shm")
		val dbWal = File("${dbFile.path}-wal")

		// Build the list of files to zip
		val files = listOf(csvFile, dbFile, dbShm, dbWal)

		// Write the shared preferences to a csv file
		sharedPreferences.writeToCsv(context, csvFile)

		lifecycleScope.launch {

			// Checkpoint the database so that it does not need to be closed
			NacAlarmDatabase.getInstance(deviceContext)
				.alarmDao()
				.checkpoint(SimpleSQLiteQuery("pragma wal_checkpoint(full)"))

			// Zip the files
			zipFiles(outputStream, files)

			// Show success message
			quickToast(context, R.string.message_export_completed)

			// Stop the service
			stopThisService()

		}
	}

}
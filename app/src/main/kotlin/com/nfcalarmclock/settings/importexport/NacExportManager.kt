package com.nfcalarmclock.settings.importexport

import android.content.Context
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.lifecycleScope
import androidx.sqlite.db.SimpleSQLiteQuery
import com.nfcalarmclock.R
import com.nfcalarmclock.db.NacAlarmDatabase
import com.nfcalarmclock.shared.NacSharedPreferences
import com.nfcalarmclock.system.file.zipFiles
import com.nfcalarmclock.util.NacCalendar
import com.nfcalarmclock.util.NacUtility
import com.nfcalarmclock.util.getDeviceProtectedStorageContext
import kotlinx.coroutines.launch
import java.io.File
import java.io.OutputStream

/**
 * Export shared preferences, in a csv file, and a database file, all packaged in a zip.
 */
class NacExportManager(fragment: Fragment)
{

	/**
	 * Export the shared preferences and database files to a zip file.
	 */
	private val exportContent = fragment.registerForActivityResult(ActivityResultContracts.CreateDocument("application/zip")) { uri ->

		// Get the context
		val context = fragment.requireContext()

		// Get the output stream for the zip file
		val outputStream = getExportStream(context, uri) ?: return@registerForActivityResult

		// Export data to a zip file
		export(context, outputStream, fragment.lifecycleScope)

	}

	/**
	 * Export the shared preferences and database files to a zip file.
	 */
	private fun export(
		context: Context,
		outputStream: OutputStream,
		lifecycleCoroutineScope: LifecycleCoroutineScope)
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

		lifecycleCoroutineScope.launch {

			// Checkpoint the database so that it does not need to be closed
			NacAlarmDatabase.getInstance(deviceContext)
				.alarmDao()
				.checkpoint(SimpleSQLiteQuery("pragma wal_checkpoint(full)"))

			// Zip the files
			zipFiles(outputStream, files)

			// Show success message
			NacUtility.quickToast(context, R.string.message_export_completed)

		}
	}

	/**
	 * Get the stream for an export.
	 */
	private fun getExportStream(context: Context, uri: Uri?): OutputStream?
	{
		// Check if URI is populated
		if (uri == null)
		{
			return null
		}

		// Return the output stream
		return context.contentResolver.openOutputStream(uri).also {

			// Check if stream is not valid and show message if it is not
			if (it == null)
			{
				NacUtility.quickToast(context, R.string.error_message_unable_to_open_import_export_stream)
			}

		}
	}

	/**
	 * Launch the export process.
	 */
	fun launch(fragment: Fragment)
	{
		// Get the app name
		val appName = fragment.resources.getString(R.string.app_name)
			.lowercase()
			.replace(" ", "_")

		// Get the current timestamp
		val timestamp = NacCalendar.getTimestamp("yyyy-MM-dd HH:mm:SS")
			.replace(" ", "_")
			.replace(":", "")

		// Get the filename
		val filename = "${appName}_${timestamp}.zip"

		// Launch the file chooser
		exportContent.launch(filename)
	}

}
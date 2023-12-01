package com.nfcalarmclock.statistics

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.nfcalarmclock.R
import com.nfcalarmclock.alarm.NacAlarmViewModel
import com.nfcalarmclock.shared.NacSharedPreferences
import com.nfcalarmclock.statistics.db.NacAlarmStatistic
import com.nfcalarmclock.util.NacCalendar
import com.nfcalarmclock.util.NacUtility.quickToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Statistics fragment.
 * <p>
 * TODO: Highlighting the weekends in the dismiss/miss/snooze plot would be dope
 */
@AndroidEntryPoint
class NacStatisticsSettingFragment
	: Fragment(R.layout.frg_statistics)
{

	/**
	 * Alarm view model.
	 */
	private val alarmViewModel: NacAlarmViewModel by viewModels()

	/**
	 * Statistic view model.
	 */
	private val statisticViewModel: NacAlarmStatisticViewModel by viewModels()

	/**
	 * Cleanup the CSV files.
	 */
	private fun cleanupCsvFiles(allFiles: List<String>)
	{
		// Get the directory where files are created
		val directory = requireContext().filesDir

		// Iterate over each file
		for (filename in allFiles)
		{
			// Build the path to the file name
			val path = "${directory}/${filename}"
			val file = File(path)

			// Delete the file
			file.delete()
		}
	}

	/**
	 * Export all statistics to files.
	 */
	private suspend fun exportAllStatistics(timestamp: String): List<String>
	{
		// Get all the statistics
		val allCreated = statisticViewModel.getAllCreatedStatistics()
		val allDeleted = statisticViewModel.getAllDeletedStatistics()
		val allDismissed = statisticViewModel.getAllDismissedStatistics()
		val allMissed = statisticViewModel.getAllMissedStatistics()
		val allSnoozed = statisticViewModel.getAllSnoozedStatistics()

		// Created
		val createdFilename = writeToFile(
			R.string.label_created_statistic,
			R.string.label_created_statistic_header,
			timestamp,
			allCreated)

		// Deleted
		val deletedFilename = writeToFile(
			R.string.label_deleted_statistic,
			R.string.label_deleted_statistic_header,
			timestamp,
			allDeleted)

		// Dismissed
		val dismissedFilename = writeToFile(
			R.string.label_dismissed_statistic,
			R.string.label_dismissed_statistic_header,
			timestamp,
			allDismissed)

		// Missed
		val missedFilename = writeToFile(
			R.string.label_missed_statistic,
			R.string.label_missed_statistic_header,
			timestamp,
			allMissed)

		// Snoozed
		val snoozedFilename = writeToFile(
			R.string.label_snoozed_statistic,
			R.string.label_snoozed_statistic_header,
			timestamp,
			allSnoozed)

		// Return the list of all files that were created
		return listOf(createdFilename, deletedFilename, dismissedFilename,
			missedFilename, snoozedFilename)
	}

	/**
	 * Called when the view is created.
	 */
	override fun onViewCreated(root: View, savedInstanceState: Bundle?)
	{
		// Super
		super.onViewCreated(root, savedInstanceState)

		// Setup all the statistics
		lifecycleScope.launch {
			setupDismissedAlarms(root)
			setupSnoozedAlarms(root)
			setupMissedAlarms(root)
			setupCreatedAlarms(root)
			setupDeletedAlarms(root)
			setupCurrentAlarms(root)
			setupStartedOnDate(root)
		}

		// Setup the buttons
		setupResetButton(root)
		setupEmailButton(root)

		// Setup all the views that need to use the theme color
		setupViewsWithThemeColor(root)
	}

	/**
	 * Reset statistics.
	 */
	private fun resetStatistics()
	{
		// Delete all statistics
		statisticViewModel.deleteAllCreated()
		statisticViewModel.deleteAllDeleted()
		statisticViewModel.deleteAllDismissed()
		statisticViewModel.deleteAllMissed()
		statisticViewModel.deleteAllSnoozed()

		// Change the text of when statistics started
		lifecycleScope.launch {

			// Get all the current alarms
			val allAlarms = alarmViewModel.getAllAlarms()

			// Iterate over each alarm
			for (alarm in allAlarms)
			{
				// Add a created statistic. Use the repository so that
				// everything is sequential
				statisticViewModel.statisticRepository.insertCreated()
			}

			// Get the root view
			val root = requireView()

			// Setup the date that statistics started on
			setupDismissedAlarms(root)
			setupSnoozedAlarms(root)
			setupMissedAlarms(root)
			setupCreatedAlarms(root)
			setupDeletedAlarms(root)
			setupCurrentAlarms(root)
			setupStartedOnDate(root)

		}
	}

	/**
	 * Setup the created alarm statistics.
	 */
	private suspend fun setupCreatedAlarms(root: View)
	{
		// Get the text view
		val textview = root.findViewById<TextView>(R.id.created_alarms_number)

		// Determine how many alarms were created
		val numCreated = statisticViewModel.createdCount()

		// Set the text in the textview
		textview.text = numCreated.toString()
	}

	/**
	 * Setup the current alarm statistics.
	 */
	private suspend fun setupCurrentAlarms(root: View)
	{
		// Get the text view
		val textview = root.findViewById<TextView>(R.id.current_alarms_number)

		// Determine the current number of alarms
		val numCreated = statisticViewModel.createdCount()
		val numDeleted = statisticViewModel.deletedCount()
		val numCurrent = numCreated - numDeleted

		// Set the text in the textview
		textview.text = numCurrent.toString()
	}

	/**
	 * Setup the deleted alarm statistics.
	 */
	private suspend fun setupDeletedAlarms(root: View)
	{
		// Get the text view
		val textview = root.findViewById<TextView>(R.id.deleted_alarms_number)

		// Determine how many alarms were deleted
		val numDeleted = statisticViewModel.deletedCount()

		// Set the text in the textview
		textview.text = numDeleted.toString()
	}

	/**
	 * Setup the dismissed alarm statistics.
	 */
	private suspend fun setupDismissedAlarms(root: View)
	{
		// Get the text view
		val locale = Locale.getDefault()
		val textview = root.findViewById<TextView>(R.id.dismissed_alarms_number)

		// Determine how many alarms were dismissed and how many with NFC
		val numDismissedTotal = statisticViewModel.dismissedCount()
		val numDismissedWithNfc = statisticViewModel.dismissedWithNfcCount()

		// Determine the text to show in the textview
		val text = String.format(locale, "%1\$s (%2\$s NFC)", numDismissedTotal,
			numDismissedWithNfc)

		// Set the text in the textview
		textview.text = text
	}

	/**
	 * Setup the missed alarm statistics.
	 */
	private suspend fun setupMissedAlarms(root: View)
	{
		// Get the text view
		val textview = root.findViewById<TextView>(R.id.missed_alarms_number)

		// Determine how many alarms were missed
		val numMissed = statisticViewModel.missedCount()

		// Set the text in the textview
		textview.text = numMissed.toString()
	}

	/**
	 * Setup the email button.
	 */
	private fun setupEmailButton(root: View)
	{
		// Get the button
		val emailButton = root.findViewById<MaterialButton>(R.id.email_button)

		// Set the listener
		emailButton.setOnClickListener {

			lifecycleScope.launch {

				// Get the current timestamp
				val timestamp = NacCalendar.getTimestamp("yyyy-MM-dd_HHmmSS")

				// Export all files
				val allFiles = exportAllStatistics(timestamp)

				// Zip all files into a nice email attachment
				val attachment = zipFiles(timestamp, allFiles)

				// Cleanup the CSV files
				cleanupCsvFiles(allFiles)

				// Send the email
				sendEmail(attachment)

			}

		}
	}

	/**
	 * Setup the reset button.
	 */
	private fun setupResetButton(root: View)
	{
		// Get the button
		val resetButton = root.findViewById<MaterialButton>(R.id.reset_button)

		// Set the listener
		resetButton.setOnClickListener {

			// Create the dialog
			val dialog = AreYouSureResetStatisticsDialog()

			// Setup the dialog
			dialog.onResetStatisticsListener = AreYouSureResetStatisticsDialog.OnResetStatisticsListener {

				// Reset statistics
				resetStatistics()

			}

			// Show the dialog
			dialog.show(childFragmentManager, AreYouSureResetStatisticsDialog.TAG)

		}
	}

	/**
	 * Setup the snoozed alarm statistics.
	 */
	private suspend fun setupSnoozedAlarms(root: View)
	{
		// Get the text view
		val locale = Locale.getDefault()
		val textview = root.findViewById<TextView>(R.id.snoozed_alarms_number)

		// Determine how many alarms were snoozed and how much time that was
		val numSnoozed = statisticViewModel.snoozedCount()
		val snoozeDuration = statisticViewModel.snoozedTotalDuration() / 60

		// Determine the text to show in the textview
		val text = String.format(locale, "%1\$s (%2\$s min)", numSnoozed,
			snoozeDuration)

		// Set the text in the textview
		textview.text = text
	}

	/**
	 * Setup the date that statistics started on.
	 */
	private suspend fun setupStartedOnDate(root: View)
	{
		// Get the created first timestamp
		val timestamp = statisticViewModel.createdFirstTimestamp()

		// Determine the text to show as the date the statistics started on
		val text: String =
			// Timestamp is a valid date
			if (timestamp > 0)
			{
				val locale = Locale.getDefault()
				val startedOnMessage = getString(R.string.message_statistics_started_on)

				// Determine the format the date should be shown in
				val dateStarted = Date(timestamp)
				val dateFormat: DateFormat =
					SimpleDateFormat("yyyy-MM-dd HH:mm z", locale)
				val dateText = dateFormat.format(dateStarted)

				// Set the text that will be shown in the textview
				String.format(locale, "%1\$s %2\$s", startedOnMessage, dateText)
			}
			else
			{
				// Set the text that will be shown in the textview
				""
			}

		// Setup the textview that will show the date statistics were started
		// on, or that there were none found
		val textview = root.findViewById<TextView>(R.id.statistics_started_on_date)

		textview.text = text
	}

	/**
	 * Setup views in the fragment so that they are using the correct theme color.
	 */
	private fun setupViewsWithThemeColor(root: View)
	{
		val shared = NacSharedPreferences(requireContext())

		// Get the views
		val divider1 = root.findViewById<View>(R.id.divider1)
		val divider2 = root.findViewById<View>(R.id.divider2)
		val emailButton = root.findViewById<MaterialButton>(R.id.email_button)

		// Get the theme color
		val themeColor = shared.themeColor

		// Set the color of the dividers to the theme color
		divider1.setBackgroundColor(themeColor)
		divider2.setBackgroundColor(themeColor)

		// Set the color of the reset button to the theme color
		emailButton.setBackgroundColor(themeColor)
	}

	/**
	 * Send an email.
	 */
	private fun sendEmail(attachment: Uri)
	{
		// TODO: To allow other apps to access files stored in this
		//  directory within internal storage, use a FileProvider
		//  with the FLAG_GRANT_READ_URI_PERMISSION attribute.

		val packageManager = requireContext().packageManager
		val subject = getString(R.string.message_statistics_email_subject)

		// Build the intent
		val intent = Intent(Intent.ACTION_SEND).apply {

			data = Uri.parse("mailto:")
			putExtra(Intent.EXTRA_SUBJECT, subject)
			putExtra(Intent.EXTRA_STREAM, attachment)
			addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

		}

		// Check if can resolve the package manager? Not sure what this really does
		if (intent.resolveActivity(packageManager) != null)
		{
			// Start the activity
			startActivity(intent)
		}
		else
		{
			// Show toast error message
			quickToast(requireContext(), "Unable to start email app")
		}
	}

	/**
	 * Write data to a file.
	 */
	private fun <T: NacAlarmStatistic> writeToFile(
		nameId: Int,
		headerId: Int,
		timestamp: String,
		rows: List<T>
	): String
	{
		// Build the filename
		val name = getString(nameId)
		val filename = "${name}_${timestamp}.csv"

		// Creating the python file
		requireContext().openFileOutput(filename, Context.MODE_PRIVATE).use { output ->

			// Get the header
			val header = getString(headerId)

			// Check if there are rows of data
			if (rows.isNotEmpty())
			{
				// Get the header line
				val line = "${header}\n"

				// Write the header to the file
				output.write(line.toByteArray())
			}

			// Iterate over each statistic
			for (stat in rows)
			{
				// Convert the statistic to a CSV format and add a newline to
				// the end
				val line = "${stat.toCsvFormat()}\n"

				// Write to the file
				output.write(line.toByteArray())
			}

		}

		// Return the file name
		return filename
	}

	/**
	 * Zip files.
	 */
	private fun zipFiles(timestamp: String, allFiles: List<String>): Uri
	{
		// Get the directory for app specific files
		val directory = requireContext().filesDir

		// Build the zip file name
		val subject = getString(R.string.message_statistics_email_subject)
		val name = subject.lowercase().replace(" ", "_")
		val zipFileName = "${name}_${timestamp}.zip"

		// Create the zip file
		requireContext().openFileOutput(zipFileName, Context.MODE_PRIVATE).use { fileOutput ->

			// Start the zip process
			ZipOutputStream(BufferedOutputStream(fileOutput)).use { zipOutput ->

				// Iterate over each file
				for (filename in allFiles)
				{
					// Build the path to the file name
					val path = "${directory}/${filename}"
					val file = File(path)

					// Check if the path exists
					if (!file.exists())
					{
						continue
					}

					// Create a zip entry for a file
					val zipEntry = ZipEntry(filename)

					// Add the zip entry to the zip file
					zipOutput.putNextEntry(zipEntry)

					// Copy the contents of the file to the zip file, which I
					// believe will be written to the previously set zip entry
					BufferedInputStream(FileInputStream(file)).use { inputStream ->
						inputStream.copyTo(zipOutput, 1024)
					}
				}

			}

		}

		// Create the URI to the path
		return Uri.parse("${directory}/${zipFileName}")
	}

}
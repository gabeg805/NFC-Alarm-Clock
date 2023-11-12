package com.nfcalarmclock.statistics

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.nfcalarmclock.R
import com.nfcalarmclock.shared.NacSharedPreferences
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
	 * Statistic view model.
	 */
	private val statisticViewModel: NacAlarmStatisticViewModel by viewModels()

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

		// Setup the reset button
		setupResetButton(root)

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
			val root = requireView()

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
	 * Setup the reset button.
	 */
	private fun setupResetButton(root: View)
	{
		// Get the button
		val resetButton = root.findViewById<MaterialButton>(R.id.reset_button)

		// Set the listener
		// TODO: Show an "Are you sure?" dialog
		resetButton.setOnClickListener { resetStatistics() }
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
		val resetButton = root.findViewById<MaterialButton>(R.id.reset_button)

		// Get the theme color
		val themeColor = shared.themeColor

		// Set the color of the dividers to the theme color
		divider1.setBackgroundColor(themeColor)
		divider2.setBackgroundColor(themeColor)

		// Set the color of the reset button to the theme color
		resetButton.setBackgroundColor(themeColor)
	}

}
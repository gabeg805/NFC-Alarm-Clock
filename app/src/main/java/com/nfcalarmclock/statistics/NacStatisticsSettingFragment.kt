package com.nfcalarmclock.statistics

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.nfcalarmclock.R
import com.nfcalarmclock.shared.NacSharedConstants
import com.nfcalarmclock.shared.NacSharedPreferences
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Statistics fragment.
 * <p>
 * TODO: Highlighting the weekends in the dismiss/miss/snooze plot would be dope
 */
class NacStatisticsSettingFragment
	: Fragment(R.layout.frg_statistics)
{

	/**
	 * Alarm statistics repository.
	 */
	private var alarmStatisticsRepository: NacAlarmStatisticRepository? = null

	/**
	 * Called when the view is created.
	 */
	override fun onViewCreated(root: View, savedInstanceState: Bundle?)
	{
		// Super
		super.onViewCreated(root, savedInstanceState)

		// Create the repository
		val context = requireContext();
		val repo = NacAlarmStatisticRepository(context)
		alarmStatisticsRepository = repo

		// Setup all the statistics
		setupDismissedAlarms(repo, root)
		setupSnoozedAlarms(repo, root)
		setupMissedAlarms(repo, root)
		setupCreatedAlarms(repo, root)
		setupDeletedAlarms(repo, root)
		setupCurrentAlarms(repo, root)
		setupStartedOnDate(repo, root)

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
		// Get the repo or return if it is null
		val repo = alarmStatisticsRepository
			?: return

		// Delete all statistics
		repo.doDeleteAllCreated()
		repo.doDeleteAllDeleted()
		repo.doDeleteAllDismissed()
		repo.doDeleteAllMissed()
		repo.doDeleteAllSnoozed()

		// Change the text of when statistics started
		val root = requireView()

		setupStartedOnDate(repo, root)
	}

	/**
	 * Setup the created alarm statistics.
	 */
	private fun setupCreatedAlarms(repo: NacAlarmStatisticRepository, root: View)
	{
		val textview = root.findViewById<TextView>(R.id.created_alarms_number)
		val numCreated = repo.createdCount

		// Set the text in the textview
		textview.text = numCreated.toString()
	}

	/**
	 * Setup the current alarm statistics.
	 */
	private fun setupCurrentAlarms(repo: NacAlarmStatisticRepository, root: View)
	{
		val textview = root.findViewById<TextView>(R.id.current_alarms_number)
		val numCreated = repo.createdCount
		val numDeleted = repo.deletedCount
		val numCurrent = numCreated - numDeleted

		// Set the text in the textview
		textview.text = numCurrent.toString()
	}

	/**
	 * Setup the deleted alarm statistics.
	 */
	private fun setupDeletedAlarms(repo: NacAlarmStatisticRepository, root: View)
	{
		val textview = root.findViewById<TextView>(R.id.deleted_alarms_number)
		val numDeleted = repo.deletedCount

		// Set the text in the textview
		textview.text = numDeleted.toString()
	}

	/**
	 * Setup the dismissed alarm statistics.
	 */
	private fun setupDismissedAlarms(repo: NacAlarmStatisticRepository, root: View)
	{
		val textview = root.findViewById<TextView>(R.id.dismissed_alarms_number)
		val numDismissedTotal = repo.dismissedCount
		val numDismissedWithNfc = repo.dismissedWithNfcCount
		val locale = Locale.getDefault()

		// Determine the text to show in the textview
		val text = String.format(locale, "%1\$s (%2\$s NFC)", numDismissedTotal,
			numDismissedWithNfc)

		// Set the text in the textview
		textview.text = text
	}

	/**
	 * Setup the missed alarm statistics.
	 */
	private fun setupMissedAlarms(repo: NacAlarmStatisticRepository, root: View)
	{
		val textview = root.findViewById<TextView>(R.id.missed_alarms_number)
		val numMissed = repo.missedCount

		// Set the text in the textview
		textview.text = numMissed.toString()
	}

	/**
	 * Setup the reset button.
	 */
	private fun setupResetButton(root: View)
	{
		val resetButton = root.findViewById<MaterialButton>(R.id.reset_button)

		// Set the on click listener on the reset button
		// TODO: Show an "Are you sure?" dialog
		resetButton.setOnClickListener { resetStatistics() }
	}

	/**
	 * Setup the snoozed alarm statistics.
	 */
	private fun setupSnoozedAlarms(repo: NacAlarmStatisticRepository, root: View)
	{
		val textview = root.findViewById<TextView>(R.id.snoozed_alarms_number)
		val numSnoozed = repo.snoozedCount
		val snoozeDuration = repo.snoozedTotalDuration / 60
		val locale = Locale.getDefault()

		// Determine the text to show in the textview
		val text = String.format(locale, "%1\$s (%2\$s min)", numSnoozed,
			snoozeDuration)

		// Set the text in the textview
		textview.text = text
	}

	/**
	 * Setup the date that statistics started on.
	 */
	private fun setupStartedOnDate(repo: NacAlarmStatisticRepository, root: View)
	{
		val cons = NacSharedConstants(context)
		val timestamp = repo.createdFirstTimestamp

		// Determine the text to show as the date the statistics started on
		val text: String =
			// Timestamp is a valid date
			if (timestamp > 0)
			{
				val locale = Locale.getDefault()
				val startedOnMessage = cons.messageStatisticsStartedOn

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
		val context = context
		val shared = NacSharedPreferences(context)

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
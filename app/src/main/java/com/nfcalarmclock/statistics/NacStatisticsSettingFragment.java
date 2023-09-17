package com.nfcalarmclock.statistics;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.google.android.material.button.MaterialButton;
import com.nfcalarmclock.R;
import com.nfcalarmclock.shared.NacSharedConstants;
import com.nfcalarmclock.shared.NacSharedPreferences;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Statistics fragment.
 * <p>
 * TODO: Highlighting the weekends in the dismiss/miss/snooze plot would be dope
 */
public class NacStatisticsSettingFragment
	extends Fragment
{

	/**
	 * Alarm statistics repository.
	 */
	private NacAlarmStatisticRepository mAlarmStatisticsRepository;

	/**
	 */
	public NacStatisticsSettingFragment()
	{
		super(R.layout.frg_statistics);

		this.mAlarmStatisticsRepository = null;
	}

	/**
	 */
	@Override
	public void onViewCreated(@NonNull View root, Bundle savedInstanceState)
	{
		super.onViewCreated(root, savedInstanceState);

		Context context = getContext();
		NacAlarmStatisticRepository repo = new NacAlarmStatisticRepository(context);
		this.mAlarmStatisticsRepository = repo;

		// Setup all the statistics
		this.setupDismissedAlarms(repo, root);
		this.setupSnoozedAlarms(repo, root);
		this.setupMissedAlarms(repo, root);
		this.setupCreatedAlarms(repo, root);
		this.setupDeletedAlarms(repo, root);
		this.setupCurrentAlarms(repo, root);
		this.setupStartedOnDate(repo, root);

		// Setup the reset button
		this.setupResetButton(root);

		// Setup all the views that need to use the theme color
		this.setupViewsWithThemeColor(root);
	}

	/**
	 * Get the alarm statistics repository.
	 *
	 * @return The alarm statistics repository.
	 */
	private NacAlarmStatisticRepository getAlarmStatisticsRepository()
	{
		return this.mAlarmStatisticsRepository;
	}

	/**
	 * Reset statistics.
	 */
	private void resetStatistics(View view)
	{
		View root = getView();
		NacAlarmStatisticRepository repo = this.getAlarmStatisticsRepository();

		// Delete all statistics
		repo.doDeleteAllCreated();
		repo.doDeleteAllDeleted();
		repo.doDeleteAllDismissed();
		repo.doDeleteAllMissed();
		repo.doDeleteAllSnoozed();

		// Change the text of when statistics started
		this.setupStartedOnDate(repo, root);
	}

	/**
	 * Setup the created alarm statistics.
	 */
	private void setupCreatedAlarms(NacAlarmStatisticRepository repo, View root)
	{
		long numCreated = repo.getCreatedCount();
		String text = String.valueOf(numCreated);

		TextView textview = root.findViewById(R.id.created_alarms_number);
		textview.setText(text);
	}

	/**
	 * Setup the current alarm statistics.
	 */
	private void setupCurrentAlarms(NacAlarmStatisticRepository repo, View root)
	{
		long numCreated = repo.getCreatedCount();
		long numDeleted = repo.getDeletedCount();
		long numCurrent = numCreated - numDeleted;
		String text = String.valueOf(numCurrent);

		TextView textview = root.findViewById(R.id.current_alarms_number);
		textview.setText(text);
	}

	/**
	 * Setup the deleted alarm statistics.
	 */
	private void setupDeletedAlarms(NacAlarmStatisticRepository repo, View root)
	{
		long numDeleted = repo.getDeletedCount();
		String text = String.valueOf(numDeleted);

		TextView textview = root.findViewById(R.id.deleted_alarms_number);
		textview.setText(text);
	}

	/**
	 * Setup the dismissed alarm statistics.
	 */
	private void setupDismissedAlarms(NacAlarmStatisticRepository repo, View root)
	{
		long numDismissedTotal = repo.getDismissedCount();
		long numDismissedWithNfc = repo.getDismissedWithNfcCount();

		Locale locale = Locale.getDefault();
		String text = String.format(locale, "%1$s (%2$s NFC)", numDismissedTotal,
			numDismissedWithNfc);

		TextView textview = root.findViewById(R.id.dismissed_alarms_number);
		textview.setText(text);
	}

	/**
	 * Setup the missed alarm statistics.
	 */
	private void setupMissedAlarms(NacAlarmStatisticRepository repo, View root)
	{
		long numMissed = repo.getMissedCount();
		String text = String.valueOf(numMissed);

		TextView textview = root.findViewById(R.id.missed_alarms_number);
		textview.setText(text);
	}

	/**
	 * Setup the reset button.
	 */
	private void setupResetButton(View root)
	{
		MaterialButton resetButton = root.findViewById(R.id.reset_button);

		resetButton.setOnClickListener(this::resetStatistics);
	}

	/**
	 * Setup the snoozed alarm statistics.
	 */
	private void setupSnoozedAlarms(NacAlarmStatisticRepository repo, View root)
	{
		long numSnoozed = repo.getSnoozedCount();
		long snoozeDuration = repo.getSnoozedTotalDuration() / 60;

		Locale locale = Locale.getDefault();
		String text = String.format(locale, "%1$s (%2$s min)", numSnoozed,
			snoozeDuration);

		TextView textview = root.findViewById(R.id.snoozed_alarms_number);
		textview.setText(text);
	}

	/**
	 * Setup the date that statistics started on.
	 */
	private void setupStartedOnDate(NacAlarmStatisticRepository repo,
		View root)
	{
		Context context = getContext();
		NacSharedConstants cons = new NacSharedConstants(context);
		long timestamp = repo.getCreatedFirstTimestamp();
		String text;

		// Timestamp is a valid date
		if (timestamp > 0)
		{
			Locale locale = Locale.getDefault();
			String startedOnMessage = cons.getMessageStatisticsStartedOn();

			// Determine the format the date should be shown in
			Date dateStarted = new Date(timestamp);
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm z", locale);
			String dateText = dateFormat.format(dateStarted);

			// Set the text that will be shown in the textview
			text = String.format(locale, "%1$s %2$s", startedOnMessage, dateText);
		}
		// Empty or invalid timestamp, probably because statistics were reset
		else
		{
			// Set the text that will be shown in the textview
			text = "";
		}

		// Setup the textview that will show the date statistics were started
		// on, or that there were none found
		TextView textview = root.findViewById(R.id.statistics_started_on_date);

		textview.setText(text);
	}

	/**
	 * Setup views in the fragment so that they are using the correct theme color.
	 */
	private void setupViewsWithThemeColor(View root)
	{
		Context context = getContext();
		NacSharedPreferences shared = new NacSharedPreferences(context);

		// Get the views
		View divider1 = root.findViewById(R.id.divider1);
		View divider2 = root.findViewById(R.id.divider2);
		MaterialButton resetButton = root.findViewById(R.id.reset_button);

		// Get the theme color
		int themeColor = shared.getThemeColor();

		// Set the color of the dividers to the theme color
		divider1.setBackgroundColor(themeColor);
		divider2.setBackgroundColor(themeColor);

		// Set the color of the reset button to the theme color
		resetButton.setBackgroundColor(themeColor);
	}

}

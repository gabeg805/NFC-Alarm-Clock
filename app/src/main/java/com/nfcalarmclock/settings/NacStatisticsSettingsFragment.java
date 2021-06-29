package com.nfcalarmclock.settings;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.nfcalarmclock.R;
import com.nfcalarmclock.shared.NacSharedConstants;
import com.nfcalarmclock.shared.NacSharedPreferences;
import com.nfcalarmclock.statistics.NacAlarmStatisticRepository;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Statistics fragment.
 *
 * TODO: Highlighting the weekends in the dismiss/miss/snooze plot would be dope
 */
public class NacStatisticsSettingsFragment
	extends Fragment
{

	/**
	 */
	public NacStatisticsSettingsFragment()
	{
		super(R.layout.frg_statistics);
	}

	/**
	 */
	@Override
	public void onViewCreated(@NonNull View root, Bundle savedInstanceState)
	{
		super.onViewCreated(root, savedInstanceState);

		Context context = getContext();
		NacAlarmStatisticRepository repo = new NacAlarmStatisticRepository(context);
		NacSharedPreferences shared = new NacSharedPreferences(context);

		this.setupDismissedAlarms(repo, root);
		this.setupSnoozedAlarms(repo, root);
		this.setupMissedAlarms(repo, root);
		this.setupCreatedAlarms(repo, root);
		this.setupDeletedAlarms(repo, root);
		this.setupCurrentAlarms(repo, root);
		this.setupStartedOnDate(repo, shared, root);
		this.setupThemeColor(shared, root);
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
		long numDismissedNormal = repo.getDismissedCount();
		long numDismissedWithNfc = repo.getDismissedWithNfcCount();
		long numDismissed = numDismissedNormal + numDismissedWithNfc;

		Locale locale = Locale.getDefault();
		String text = String.format(locale, "%1$s (%2$s NFC)", numDismissed,
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
		NacSharedPreferences shared, View root)
	{
		NacSharedConstants cons = shared.getConstants();
		String startedOnText = cons.getMessageStatisticsStartedOn();

		Date dateStarted = repo.getCreatedFirstDate();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm z");
		String dateText = dateFormat.format(dateStarted);

		Locale locale = Locale.getDefault();
		String text = String.format(locale, "%1$s %2$s", startedOnText, dateText);

		TextView textview = root.findViewById(R.id.statistics_started_on_date);
		textview.setText(text);
	}

	/**
	 * Setup theme color.
	 */
	private void setupThemeColor(NacSharedPreferences shared, View root)
	{
		int themeColor = shared.getThemeColor();
		View divider1 = root.findViewById(R.id.divider1);
		View divider2 = root.findViewById(R.id.divider2);

		divider1.setBackgroundColor(themeColor);
		divider2.setBackgroundColor(themeColor);
	}

}

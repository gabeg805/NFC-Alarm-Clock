package com.nfcalarmclock.settings;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.nfcalarmclock.R;
import com.nfcalarmclock.shared.NacSharedPreferences;
import com.nfcalarmclock.statistics.NacAlarmStatisticRepository;

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
	public void onViewCreated(@NonNull View view, Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);

		Context context = getContext();
		NacAlarmStatisticRepository repo = new NacAlarmStatisticRepository(context);
		NacSharedPreferences shared = new NacSharedPreferences(context);
		Locale locale = Locale.getDefault();

		int themeColor = shared.getThemeColor();
		long numCreated = repo.getCreatedCount();
		long numDeleted = repo.getDeletedCount();
		long numDismissed = repo.getDismissedCount();
		long numDismissedWithNfc = repo.getDismissedWithNfcCount();
		long numMissed = repo.getMissedCount();
		long numSnoozed = repo.getSnoozedCount();
		long snoozeDuration = repo.getSnoozedTotalDuration() / 60;

		View divider1 = view.findViewById(R.id.divider1);
		View divider2 = view.findViewById(R.id.divider2);
		TextView current = view.findViewById(R.id.current_alarms_number);
		TextView created = view.findViewById(R.id.created_alarms_number);
		TextView deleted = view.findViewById(R.id.deleted_alarms_number);
		TextView dismissed = view.findViewById(R.id.dismissed_alarms_number);
		TextView missed = view.findViewById(R.id.missed_alarms_number);
		TextView snoozed = view.findViewById(R.id.snoozed_alarms_number);

		divider1.setBackgroundColor(themeColor);
		divider2.setBackgroundColor(themeColor);
		current.setText(String.valueOf(numCreated-numDeleted));
		created.setText(String.valueOf(numCreated));
		deleted.setText(String.valueOf(numDeleted));
		dismissed.setText(String.format(locale, "%1$s / %2$s NFC", numDismissed,
			numDismissedWithNfc));
		//dismissed.setText(String.valueOf(numDismissed));
		missed.setText(String.valueOf(numMissed));
		snoozed.setText(String.format(locale, "%1$s / %2$s min", numSnoozed,
			snoozeDuration));
		//snoozed.setText(String.valueOf(numSnoozed));
	}

}

package com.nfcalarmclock;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import androidx.preference.PreferenceManager;

/**
 * Miscellaneous fragment.
 */
public class NacMiscellaneousSettingsFragment
	extends NacSettingsFragment
{

	/**
	 */
	@Override
	public void onCreatePreferences(Bundle savedInstanceState, String rootKey)
	{
		addPreferencesFromResource(R.xml.miscellaneous_preferences);
		PreferenceManager.setDefaultValues(getContext(),
			R.xml.miscellaneous_preferences, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);

		Context context = getContext();
		NacSharedKeys keys = new NacSharedKeys(context);
		NacCheckboxPreference speak = (NacCheckboxPreference) findPreference(keys.getSpeakToMe());
		NacSpeakFrequencyPreference speakFreq = (NacSpeakFrequencyPreference) findPreference(keys.getSpeakFrequency());
		boolean state = speak.getChecked();

		speak.notifyDependencyChange(!state);
	}

}

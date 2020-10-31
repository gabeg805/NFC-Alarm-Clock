package com.nfcalarmclock;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
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
	public void onViewCreated(@NonNull View view, Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);

		Context context = getContext();
		NacSharedKeys keys = new NacSharedKeys(context);
		NacCheckboxPreference speak = findPreference(keys.getSpeakToMe());
		//NacSpeakFrequencyPreference speakFreq = findPreference(keys.getSpeakFrequency());
		boolean state = speak.getChecked();

		speak.notifyDependencyChange(!state);
	}

}

package com.nfcalarmclock;

import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import androidx.preference.PreferenceManager;

/**
 * Miscellaneous fragment.
 */
public class NacMiscellaneousSettings
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

		Resources res = getContext().getResources();
		String speakKey= res.getString(R.string.pref_speak_to_me_key);
		String speakFreqKey = res.getString(R.string.pref_speak_frequency_key);
		NacCheckboxPreference speak = (NacCheckboxPreference) findPreference(speakKey);
		NacSpeakFrequencyPreference speakFreq = (NacSpeakFrequencyPreference) findPreference(speakFreqKey);

		boolean state = speak.getChecked();

		speak.notifyDependencyChange(!state);
	}

}

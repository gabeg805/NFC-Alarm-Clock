package com.nfcalarmclock;

import android.os.Bundle;
//import android.preference.PreferenceManager;

import androidx.preference.PreferenceManager;

/**
 * General settings fragment.
 */
public class NacGeneralSettings
	extends NacSettingsFragment
{

	/**
	 */
	@Override
	public void onCreatePreferences(Bundle savedInstanceState, String rootKey)
	{
		//super.onCreatePreferences(savedInstanceState, rootKey);
		addPreferencesFromResource(R.xml.general_preferences);
		PreferenceManager.setDefaultValues(getContext(),
			R.xml.general_preferences, false);
	}

}

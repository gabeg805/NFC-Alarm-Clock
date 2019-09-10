package com.nfcalarmclock;

import android.os.Bundle;
//import android.preference.PreferenceManager;

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
		//super.onCreatePreferences(savedInstanceState, rootKey);
		addPreferencesFromResource(R.xml.miscellaneous_preferences);
		PreferenceManager.setDefaultValues(getContext(),
			R.xml.miscellaneous_preferences, false);
	}

}

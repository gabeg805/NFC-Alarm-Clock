package com.nfcalarmclock;

import android.os.Bundle;
import android.preference.PreferenceManager;

/**
 * General settings fragment.
 */
public class NacGeneralSettings
	extends NacSettingsFragment
{

	/**
	 */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.general_preferences);
		PreferenceManager.setDefaultValues(getContext(),
			R.xml.general_preferences, false);
	}

}

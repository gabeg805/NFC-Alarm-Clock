package com.nfcalarmclock;

import android.os.Bundle;
//import android.preference.PreferenceManager;

/**
 * Miscellaneous fragment.
 */
public class NacMiscellaneousSettings
	extends NacSettingsFragment
{

	/**
	 */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.miscellaneous_preferences);
		//PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
	}

}

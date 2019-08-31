package com.nfcalarmclock;

import android.os.Bundle;
import android.preference.PreferenceManager;

/**
 * About fragment.
 */
public class NacAboutSettings
	extends NacSettingsFragment
{

	/**
	 */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.about_preferences);
	}

}

package com.nfcalarmclock.settings;

import android.os.Bundle;

import com.nfcalarmclock.R;

/**
 * About fragment.
 */
public class NacAboutSettingsFragment
	extends NacSettingsFragment
{

	/**
	 */
	@Override
	public void onCreatePreferences(Bundle savedInstanceState, String rootKey)
	{
		addPreferencesFromResource(R.xml.about_preferences);
	}

}

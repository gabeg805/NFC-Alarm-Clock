package com.nfcalarmclock;

import android.os.Bundle;
import androidx.preference.PreferenceManager;

/**
 * About fragment.
 */
public class NacAboutSettings
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

package com.nfcalarmclock;

import android.app.FragmentManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;

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
		//PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
	}

}

package com.nfcalarmclock.settings;

import android.os.Bundle;
import androidx.preference.PreferenceManager;

import com.nfcalarmclock.R;

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

}

package com.nfcalarmclock.settings;

import android.os.Bundle;
import androidx.preference.Preference;

import com.nfcalarmclock.BuildConfig;
import com.nfcalarmclock.R;
import com.nfcalarmclock.shared.NacSharedKeys;
import com.nfcalarmclock.whatsnew.NacWhatsNewDialog;

/**
 * Fragment to show the About preferences.
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

		// Get the version preference
		NacSharedKeys keys = this.getSharedKeys();
		Preference version = findPreference(keys.getVersionPreference());

		// Set the version name as the summary
		version.setSummary(BuildConfig.VERSION_NAME);
	}

	/**
	 */
	@Override
	public boolean onPreferenceTreeClick(Preference preference)
	{
		NacSharedKeys keys = this.getSharedKeys();
		String preferenceKey = preference.getKey();

		// Version preference was clicked
		if (preferenceKey.equals(keys.getVersionPreference()))
		{
			// Show the What's New dialog
			NacWhatsNewDialog dialog = new NacWhatsNewDialog();

			dialog.show(getChildFragmentManager(), NacWhatsNewDialog.TAG);
		}

		// Default return
		return super.onPreferenceTreeClick(preference);
	}

}

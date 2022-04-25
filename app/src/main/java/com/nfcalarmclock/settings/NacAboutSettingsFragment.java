package com.nfcalarmclock.settings;

import android.os.Bundle;
import androidx.preference.Preference;

import com.nfcalarmclock.R;
import com.nfcalarmclock.shared.NacSharedKeys;
import com.nfcalarmclock.whatsnew.NacWhatsNewDialog;

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

	/**
	 */
	@Override
	public boolean onPreferenceTreeClick(Preference preference)
	{
		NacSharedKeys keys = this.getSharedKeys();
		String preferenceKey = preference.getKey();

		if (preferenceKey.equals(keys.getVersionPreference()))
		{
			NacWhatsNewDialog dialog = new NacWhatsNewDialog();

			dialog.show(getChildFragmentManager(), NacWhatsNewDialog.TAG);
		}

		return super.onPreferenceTreeClick(preference);
	}

}

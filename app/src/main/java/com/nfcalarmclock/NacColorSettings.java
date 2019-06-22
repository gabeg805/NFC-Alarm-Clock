package com.nfcalarmclock;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;

/**
 * Color fragment.
 */
public class NacColorSettings
	extends NacSettingsFragment
	implements Preference.OnPreferenceChangeListener
{

	/**
	 * Initialize the color settings fragment.
	 */
	private void init()
	{
		addPreferencesFromResource(R.xml.color_preferences);
		PreferenceManager.setDefaultValues(getContext(),
			R.xml.color_preferences, false);

		NacSharedKeys keys = this.getKeys();
		Preference theme = findPreference(keys.getThemeColor());

		theme.setOnPreferenceChangeListener(this);
	}

	/**
	 */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.init();
	}

	/**
	 * Reset the screen when the theme color is changed, so that checkboxes,
	 * etc. change color as well.
	 */
	@Override
	public boolean onPreferenceChange(Preference pref, Object newVal)
	{
		setPreferenceScreen(null);
		this.init();

		return true;
	}

}

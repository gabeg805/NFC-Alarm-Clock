package com.nfcalarmclock;

import android.content.Context;
import android.os.Bundle;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;

/**
 * Appearance fragment.
 */
public class NacAppearanceSettingsFragment
	extends NacSettingsFragment
	implements Preference.OnPreferenceChangeListener
{

	/**
	 * Initialize the color settings fragment.
	 */
	private void init()
	{
		Context context = getContext();

		addPreferencesFromResource(R.xml.appearance_preferences);
		PreferenceManager.setDefaultValues(context, R.xml.appearance_preferences,
			false);

		this.setupThemeColorPreference();
		this.setupDayButtonStylePreference();
	}

	/**
	 */
	@Override
	public void onCreatePreferences(Bundle savedInstanceState, String rootKey)
	{
		this.init();
	}

	/**
	 * Reset the screen when the theme color is changed, so that checkboxes,
	 * etc. change color as well.
	 */
	@Override
	public boolean onPreferenceChange(Preference pref, Object newVal)
	{
		NacSharedKeys keys = this.getKeys();
		String prefKey = pref.getKey();
		String themeKey = keys.getThemeColor();
		String dayButtonStyleKey = keys.getDayButtonStyle();

		if (prefKey.equals(themeKey))
		{
			setPreferenceScreen(null);
			this.init();
		}
		else if (prefKey.equals(dayButtonStyleKey))
		{
			NacSharedPreferences shared = this.getSharedPreferences();
			shared.editShouldRefreshMainActivity(true);
		}

		return true;
	}

	/**
	 * Setup the day button style preference.
	 */
	private void setupDayButtonStylePreference()
	{
		NacSharedKeys keys = this.getKeys();
		String dayButtonStyleKey = keys.getDayButtonStyle();
		Preference dayButtonStylePref = findPreference(dayButtonStyleKey);

		dayButtonStylePref.setOnPreferenceChangeListener(this);
	}

	/**
	 * Setup the theme color preference.
	 */
	private void setupThemeColorPreference()
	{
		NacSharedKeys keys = this.getKeys();
		String themeKey = keys.getThemeColor();
		Preference themePref = findPreference(themeKey);

		themePref.setOnPreferenceChangeListener(this);
	}

}

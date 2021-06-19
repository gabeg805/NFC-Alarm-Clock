package com.nfcalarmclock.settings;

import android.content.Context;
import android.os.Bundle;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;

import com.nfcalarmclock.R;
import com.nfcalarmclock.shared.NacSharedKeys;
import com.nfcalarmclock.shared.NacSharedPreferences;

import java.util.List;

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

		this.setupColorPreferences();
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
		NacSharedPreferences shared = this.getSharedPreferences();
		NacSharedKeys keys = this.getSharedKeys();

		List<String> colorKeys = keys.getColorKeys();
		String themeKey = keys.getThemeColor();
		String dayButtonStyleKey = keys.getDayButtonStyle();
		String prefKey = pref.getKey();

		if (colorKeys.contains(prefKey) || prefKey.equals(dayButtonStyleKey))
		{
			shared.editShouldRefreshMainActivity(true);

			if (prefKey.equals(themeKey))
			{
				setPreferenceScreen(null);
				this.init();
			}
		}

		return true;
	}

	/**
	 * Setup the color preferences.
	 */
	private void setupColorPreferences()
	{
		NacSharedKeys keys = this.getSharedKeys();
		List<String> colorKeys = keys.getColorKeys();

		for (String k : colorKeys)
		{
			Preference pref = findPreference(k);
			pref.setOnPreferenceChangeListener(this);
		}
	}

	/**
	 * Setup the day button style preference.
	 */
	private void setupDayButtonStylePreference()
	{
		NacSharedKeys keys = this.getSharedKeys();
		String dayButtonStyleKey = keys.getDayButtonStyle();
		Preference dayButtonStylePref = findPreference(dayButtonStyleKey);

		dayButtonStylePref.setOnPreferenceChangeListener(this);
	}

}

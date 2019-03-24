package com.nfcalarmclock;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.TextView;

/**
 * Display all the configurable settings for the app.
 */
public class NacSettingsActivity
	extends NacActivity
{

	/**
	 * @see SettingsFragment
	 *
	 * Use a fragment to display the settings in order to allow the back button
	 * in the action bar to be used (from AppCompatActivity). This allows the
	 * user to go back to the main activity.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
		FragmentManager manager = getFragmentManager();
		FragmentTransaction trans = manager.beginTransaction();

		trans.replace(android.R.id.content, new SettingsFragment());
		trans.commit();
	}

	/**
	 * Settings fragment.
	 */
	public static class SettingsFragment
		extends PreferenceFragment
		implements Preference.OnPreferenceChangeListener
	{

		/**
		 */
		@Override
		public void onCreate(Bundle savedInstanceState)
		{
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.preferences);
			this.setThemeColorChangeListener();
		}

		/**
		 */
		@Override
		public boolean onPreferenceChange(Preference pref, Object newVal)
		{
			setPreferenceScreen(null);
			addPreferencesFromResource(R.xml.preferences);
			this.setThemeColorChangeListener();
			return true;
		}

		/**
		 * When the theme color is changed, refresh the whole activity so the
		 * user can see how the new theme color looks.
		 */
		private void setThemeColorChangeListener()
		{
			String key = getResources().getString(R.string.pref_theme_color_key);
			final Preference themeColorPreference = findPreference(key);

			themeColorPreference.setOnPreferenceChangeListener(this);
		}

	}

}

package com.nfcalarmclock.settings

import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceManager
import com.nfcalarmclock.R
import com.nfcalarmclock.nextalarmformat.NacNextAlarmFormatDialog
import com.nfcalarmclock.nextalarmformat.NacNextAlarmFormatPreference
import com.nfcalarmclock.startweekon.NacStartWeekOnPreference

/**
 * Appearance fragment.
 */
class NacAppearanceSettingFragment
	: NacGenericSettingFragment(),
	Preference.OnPreferenceChangeListener
{

	/**
	 * Initialize the color settings fragment.
	 */
	private fun init()
	{
		// Inflate the XML file and add the hierarchy to the current preference
		addPreferencesFromResource(R.xml.appearance_preferences)

		// Set the default values in the XML
		PreferenceManager.setDefaultValues(requireContext(),
			R.xml.appearance_preferences, false)

		// Setup color and styles
		setupColorPreferences()
		setupDayButtonStylePreference()

		// Setup on click listeners
		setupStartWeekOnClickListener()
		setupNexAlarmFormatOnClickListener()
	}

	/**
	 * Called when the preferences are created.
	 */
	override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?)
	{
		// Initialize the color settings
		init()
	}

	/**
	 * Reset the screen when the theme color is changed, so that checkboxes,
	 * etc. change color as well.
	 */
	override fun onPreferenceChange(pref: Preference, newVal: Any): Boolean
	{
		val colorKeys = sharedKeys!!.colorKeys
		val themeKey = sharedKeys!!.themeColor
		val dayButtonStyleKey = sharedKeys!!.dayButtonStyle
		val prefKey = pref.key

		// Check if the color keys match the prefernece key or that the preference is
		// for day button styles
		if (colorKeys.contains(prefKey) || (prefKey == dayButtonStyleKey))
		{
			// Set flag to refresh the main activity
			sharedPreferences!!.editShouldRefreshMainActivity(true)

			// Preference key is for the theme
			if (prefKey == themeKey)
			{
				// Reset the screen
				preferenceScreen = null

				// Reinitialize the colors
				init()
			}
		}

		return true
	}

	/**
	 * Setup the color preferences.
	 */
	private fun setupColorPreferences()
	{
		// Iterate over each color key
		for (k in sharedKeys!!.colorKeys)
		{
			// Get the preference
			val pref = findPreference<Preference>(k)

			// Set the listener for when the prference is changed
			pref!!.onPreferenceChangeListener = this
		}
	}

	/**
	 * Setup the day button style preference.
	 */
	private fun setupDayButtonStylePreference()
	{
		// Get the preference
		val dayButtonStyleKey = sharedKeys!!.dayButtonStyle
		val dayButtonStylePref = findPreference<Preference>(dayButtonStyleKey)

		// Set the listener for when the preference is changed
		dayButtonStylePref!!.onPreferenceChangeListener = this
	}

	/**
	 * Setup the listener for when the next alarm format preference is clicked.
	 */
	private fun setupNexAlarmFormatOnClickListener()
	{
		// Get the preference
		val nextAlarmFormatPref = findPreference<NacNextAlarmFormatPreference>(sharedKeys!!.nextAlarmFormat)

		// Set the on click listener
		nextAlarmFormatPref!!.onPreferenceClickListener = Preference.OnPreferenceClickListener { pref ->

			// Show the dialog
			(pref as NacNextAlarmFormatPreference).showDialog(childFragmentManager)

			// Return
			true

		}
	}

	/**
	 * Setup the listener for when the start week on preference is clicked.
	 */
	private fun setupStartWeekOnClickListener()
	{
		// Get the preference
		val startWeekOnPref = findPreference<NacStartWeekOnPreference>(sharedKeys!!.startWeekOn)

		// Set the on click listener
		startWeekOnPref!!.onPreferenceClickListener = Preference.OnPreferenceClickListener { pref ->

			// Show the dialog
			(pref as NacStartWeekOnPreference).showDialog(childFragmentManager)

			// Return
			true
		}
	}

}
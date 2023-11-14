package com.nfcalarmclock.settings

import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceManager
import com.nfcalarmclock.R
import com.nfcalarmclock.nextalarmformat.NacNextAlarmFormatPreference
import com.nfcalarmclock.startweekon.NacStartWeekOnPreference
import com.nfcalarmclock.view.colorpicker.NacColorPickerPreference

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
		setupColorPickerOnClickListeners()
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
		// Get the keys
		val themeKey = getString(R.string.theme_color_key)
		val nameKey = getString(R.string.name_color_key)
		val dayKey = getString(R.string.days_color_key)
		val timeKey = getString(R.string.time_color_key)
		val amKey = getString(R.string.am_color_key)
		val pmKey = getString(R.string.pm_color_key)

		// Put the color keys in a list
		val colorKeys = arrayOf(themeKey, nameKey, dayKey, timeKey, amKey, pmKey)

		// Get the day button style key
		val dayButtonStyleKey = getString(R.string.day_button_style_key)

		// Check if the color keys match the prefernece key or that the preference is
		// for day button styles
		if (colorKeys.contains(pref.key) || (pref.key == dayButtonStyleKey))
		{
			// Set flag to refresh the main activity
			sharedPreferences!!.editShouldRefreshMainActivity(true)

			// Preference key is for the theme
			if (pref.key == themeKey)
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
	 * Setup the listeners for when a color picker preference is clicked.
	 */
	private fun setupColorPickerOnClickListeners()
	{
		// Get the keys
		val themeKey = getString(R.string.theme_color_key)
		val nameKey = getString(R.string.name_color_key)
		val dayKey = getString(R.string.days_color_key)
		val timeKey = getString(R.string.time_color_key)
		val amKey = getString(R.string.am_color_key)
		val pmKey = getString(R.string.pm_color_key)

		// Get the color preferences
		val themePref = findPreference<NacColorPickerPreference>(themeKey)
		val namePref = findPreference<NacColorPickerPreference>(nameKey)
		val daysPref = findPreference<NacColorPickerPreference>(dayKey)
		val timePref = findPreference<NacColorPickerPreference>(timeKey)
		val amPref = findPreference<NacColorPickerPreference>(amKey)
		val pmPref = findPreference<NacColorPickerPreference>(pmKey)

		// Create list of all color preferences
		val allPrefs = listOf(themePref, namePref, daysPref, timePref, amPref, pmPref)

		// Iterate over each color preference
		for (p in allPrefs)
		{
			// Set the on click listener
			p!!.onPreferenceClickListener = Preference.OnPreferenceClickListener { pref ->

				// Show the dialog
				(pref as NacColorPickerPreference).showDialog(childFragmentManager)

				// Return
				true

			}
		}
	}

	/**
	 * Setup the color preferences.
	 */
	private fun setupColorPreferences()
	{
		// Get the keys
		val themeKey = getString(R.string.theme_color_key)
		val nameKey = getString(R.string.name_color_key)
		val dayKey = getString(R.string.days_color_key)
		val timeKey = getString(R.string.time_color_key)
		val amKey = getString(R.string.am_color_key)
		val pmKey = getString(R.string.pm_color_key)

		// Put the color keys in a list
		val colorKeys = arrayOf(themeKey, nameKey, dayKey, timeKey, amKey, pmKey)

		// Iterate over each color key
		for (k in colorKeys)
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
		val dayButtonStyleKey = getString(R.string.day_button_style_key)
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
		val key = getString(R.string.next_alarm_format_key)
		val pref = findPreference<NacNextAlarmFormatPreference>(key)

		// Set the on click listener
		pref!!.onPreferenceClickListener = Preference.OnPreferenceClickListener { p ->

			// Show the dialog
			(p as NacNextAlarmFormatPreference).showDialog(childFragmentManager)

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
		val key = getString(R.string.start_week_on_key)
		val pref = findPreference<NacStartWeekOnPreference>(key)

		// Set the on click listener
		pref!!.onPreferenceClickListener = Preference.OnPreferenceClickListener { p ->

			// Show the dialog
			(p as NacStartWeekOnPreference).showDialog(childFragmentManager)

			// Return
			true
		}
	}

}
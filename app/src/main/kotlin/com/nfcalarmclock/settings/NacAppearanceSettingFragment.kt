package com.nfcalarmclock.settings

import android.os.Build
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceManager
import com.nfcalarmclock.R
import com.nfcalarmclock.alarm.options.nextalarmformat.NacNextAlarmFormatPreference
import com.nfcalarmclock.alarm.options.startweekon.NacStartWeekOnPreference
import com.nfcalarmclock.util.getDeviceProtectedStorageContext
import com.nfcalarmclock.view.colorpicker.NacColorPickerPreference

/**
 * Appearance fragment.
 */
class NacAppearanceSettingFragment
	: NacGenericSettingFragment()
{

	/**
	 * Initialize the color settings fragment.
	 */
	private fun init()
	{
		// Get the device protected storage context, if available
		val deviceContext = getDeviceProtectedStorageContext(requireContext())

		// Inflate the XML file and add the hierarchy to the current preference
		addPreferencesFromResource(R.xml.appearance_preferences)

		// Set the default values in the XML
		PreferenceManager.setDefaultValues(deviceContext, R.xml.appearance_preferences, false)

		// Setup color and styles
		setupColorPreferences()
		setupShowHideButtonPreferences()
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
		// Check if should set device protected storage as the storage location to use
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
		{
			preferenceManager.setStorageDeviceProtected()
		}

		// Initialize the color settings
		init()
	}

	/**
	 * Setup the listeners for when a color picker preference is clicked.
	 */
	private fun setupColorPickerOnClickListeners()
	{
		// Get the keys
		val themeKey = getString(R.string.key_color_theme)
		val nameKey = getString(R.string.key_color_name)
		val dayKey = getString(R.string.key_color_days)
		val timeKey = getString(R.string.key_color_time)
		val amKey = getString(R.string.key_color_am)
		val pmKey = getString(R.string.key_color_pm)
		val deleteAfterDismissedKey = getString(R.string.key_color_delete_after_dismissed)
		val skipNextAlarmKey = getString(R.string.key_color_skip_next_alarm)

		// Get the color preferences
		val themePref = findPreference<NacColorPickerPreference>(themeKey)
		val namePref = findPreference<NacColorPickerPreference>(nameKey)
		val daysPref = findPreference<NacColorPickerPreference>(dayKey)
		val timePref = findPreference<NacColorPickerPreference>(timeKey)
		val amPref = findPreference<NacColorPickerPreference>(amKey)
		val pmPref = findPreference<NacColorPickerPreference>(pmKey)
		val deleteAfterDismissedPref = findPreference<NacColorPickerPreference>(deleteAfterDismissedKey)
		val skipNextAlarmPref = findPreference<NacColorPickerPreference>(skipNextAlarmKey)

		// Create list of all color preferences
		val allPrefs = listOf(themePref, namePref, daysPref, timePref, amPref, pmPref,
			deleteAfterDismissedPref, skipNextAlarmPref)

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
		val themeKey = getString(R.string.key_color_theme)
		val nameKey = getString(R.string.key_color_name)
		val dayKey = getString(R.string.key_color_days)
		val timeKey = getString(R.string.key_color_time)
		val amKey = getString(R.string.key_color_am)
		val pmKey = getString(R.string.key_color_pm)
		val deleteAfterDismissedKey = getString(R.string.key_color_delete_after_dismissed)
		val skipNextAlarmKey = getString(R.string.key_color_skip_next_alarm)

		// Put the keys in a list
		val allKeys = arrayOf(themeKey, nameKey, dayKey, timeKey, amKey, pmKey,
			deleteAfterDismissedKey, skipNextAlarmKey)

		// Iterate over each color key
		for (k in allKeys)
		{
			// Get the preference
			val pref = findPreference<Preference>(k)

			// Set the listener for when the prference is changed
			pref!!.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { p, _ ->

				// Set flag to refresh the main activity
				sharedPreferences!!.shouldRefreshMainActivity = true

				// Preference key is for the theme
				if (p.key == themeKey)
				{
					// Reset the screen
					preferenceScreen = null

					// Reinitialize the colors
					init()
				}

				// Return
				true

			}
		}
	}

	/**
	 * Setup the day button style preference.
	 */
	private fun setupDayButtonStylePreference()
	{
		// Get the preference
		val dayButtonStyleKey = getString(R.string.key_style_day_button)
		val dayButtonStylePref = findPreference<Preference>(dayButtonStyleKey)

		// Set the listener for when the preference is changed
		dayButtonStylePref!!.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, _ ->

			// Set flag to refresh the main activity
			sharedPreferences!!.shouldRefreshMainActivity = true

			// Return
			true

		}
	}

	/**
	 * Setup the listener for when the next alarm format preference is clicked.
	 */
	private fun setupNexAlarmFormatOnClickListener()
	{
		// Get the preference
		val key = getString(R.string.key_tweak_next_alarm_format)
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
	 * Setup the show/hide buttons.
	 */
	private fun setupShowHideButtonPreferences()
	{
		// Get the keys
		val vibrateKey = getString(R.string.key_show_hide_vibrate_button)
		val nfcKey = getString(R.string.key_show_hide_nfc_button)
		val flashlightKey = getString(R.string.key_show_hide_flashlight_button)

		// Put the keys in a list
		val allKeys = arrayOf(vibrateKey, nfcKey, flashlightKey)

		// Iterate over each color key
		for (k in allKeys)
		{
			// Get the preference
			val pref = findPreference<Preference>(k)

			// Set the listener for when the prference is changed
			pref!!.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, _ ->

				// Set flag to refresh the main activity
				sharedPreferences!!.shouldRefreshMainActivity = true

				// Return
				true

			}
		}
	}

	/**
	 * Setup the listener for when the start week on preference is clicked.
	 */
	private fun setupStartWeekOnClickListener()
	{
		// Get the preference
		val key = getString(R.string.key_style_start_week_on)
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
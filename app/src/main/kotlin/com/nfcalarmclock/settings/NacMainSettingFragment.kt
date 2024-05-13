package com.nfcalarmclock.settings

import android.animation.AnimatorInflater
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.preference.Preference
import com.nfcalarmclock.R
import com.nfcalarmclock.nfc.NacNfcTagSettingFragment
import com.nfcalarmclock.nfc.NacNfcTagViewModel
import com.nfcalarmclock.statistics.NacStatisticsSettingFragment
import com.nfcalarmclock.support.NacSupportSetting
import com.nfcalarmclock.util.NacUtility.quickToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Main setting fragment.
 */
@AndroidEntryPoint
class NacMainSettingFragment
	: NacGenericSettingFragment()
{

	/**
	 * NFC tag view model.
	 */
	private val nfcTagViewModel: NacNfcTagViewModel by viewModels()

	/**
	 * Setup the Support preference icon.
	 */
	@Suppress("deprecation")
	private fun animateSupportIcon()
	{
		// Get the preference
		val preference = findPreference<Preference>(getString(R.string.support_setting_key))

		// Inflate the animator
		val context = requireContext()
		val animator = AnimatorInflater.loadAnimator(context, R.animator.support_development)

		// Animate the drawable
		animator.setTarget(preference!!.icon!!)
		animator.start()
	}

	/**
	 * Called when the preferences are created.
	 */
	override fun onCreatePreferences(savedInstanceState: Bundle?,
		rootKey: String?)
	{
		// Inflate the XML file and add the hierarchy to the current preference
		addPreferencesFromResource(R.xml.main_preferences)

		// Setup the manage NFC tags preference
		setupManageNfcTags()

		// Setup the support icon
		setupSupportIcon()
	}

	/**
	 * A preference in the tree was clicked.
	 */
	override fun onPreferenceTreeClick(preference: Preference): Boolean
	{
		val preferenceKey = preference.key
		val fragment: Fragment
		val title: String

		// Keys for all settings
		val generalKey = getString(R.string.general_setting_key)
		val appearanceKey = getString(R.string.appearance_setting_key)
		val statisticsKey = getString(R.string.stats_setting_key)
		val manageNfcTagsKey = getString(R.string.manage_nfc_tags_setting_key)
		val aboutKey = getString(R.string.about_setting_key)
		val supportKey = getString(R.string.support_setting_key)

		// Check the preference key
		when (preferenceKey)
		{
			// General
			generalKey ->
			{
				fragment = NacGeneralSettingFragment()
				title = getString(R.string.general_setting)
			}

			// Appearance
			appearanceKey ->
			{
				fragment = NacAppearanceSettingFragment()
				title = getString(R.string.appearance_setting)
			}

			// Statistics
			statisticsKey ->
			{
				fragment = NacStatisticsSettingFragment()
				title = getString(R.string.stats_setting)
			}

			// Manage NFC tags
			manageNfcTagsKey ->
			{
				fragment = NacNfcTagSettingFragment()
				title = getString(R.string.manage_nfc_tags_setting)
			}

			// About
			aboutKey ->
			{
				fragment = NacAboutSettingFragment()
				title = getString(R.string.about_setting)
			}

			// Other
			else ->
			{
				// Support
				if (preferenceKey == supportKey)
				{
					// Show the support flow
					showSupportFlow()
				}

				// Default return
				return super.onPreferenceTreeClick(preference)
			}
		}

		// Show the fragment that was selected above
		parentFragmentManager.beginTransaction()
			.replace(android.R.id.content, fragment)
			.addToBackStack(title)
			.commit()

		// Default return
		return super.onPreferenceTreeClick(preference)
	}

	/**
	 * Setup the Manage NFC tags preference.
	 */
	private fun setupManageNfcTags()
	{
		// Prepare the preference
		val preference = findPreference<Preference>(getString(R.string.manage_nfc_tags_setting_key))

		// Set whether to show the managee NFC tags preference
		preference?.isVisible = sharedPreferences?.shouldShowManageNfcTagsPreference == true
	}

	/**
	 * Setup the Support preference icon.
	 */
	private fun setupSupportIcon()
	{
		// Check if the user has not shown their support
		if (!sharedPreferences!!.wasAppSupported)
		{
			// Do nothing
			return
		}

		// Prepare the preference
		val context = requireContext()
		val preference = findPreference<Preference>(getString(R.string.support_setting_key))
		val color = ContextCompat.getColor(context, R.color.red)

		// Change the color of the icon to show that the user has shown their support
		preference!!.icon!!.setTint(color)
	}

	/**
	 * Show the support flow.
	 */
	private fun showSupportFlow()
	{
		val fragmentActivity = requireActivity()
		val support = NacSupportSetting(fragmentActivity)

		support.onSupportEventListener = NacSupportSetting.OnSupportEventListener {

			// Make sure the following things are run on the UI thread
			lifecycleScope.launch {

				// Check if app has not been supported yet
				if (!sharedPreferences!!.wasAppSupported)
				{

					 // Show a toast saying thank you
					 quickToast(fragmentActivity, R.string.message_support_thank_you)

					// Save that the app was supported in shared preferences
					sharedPreferences!!.editWasAppSupported(true)

					// Re-draw the support icon
					animateSupportIcon()
				}

			}

		}

		// Start the support flow
		support.start()
	}
}

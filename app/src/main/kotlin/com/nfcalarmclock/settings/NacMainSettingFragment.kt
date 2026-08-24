package com.nfcalarmclock.settings

import android.animation.AnimatorInflater
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import com.nfcalarmclock.R
import com.nfcalarmclock.settings.importexport.NacExportService
import com.nfcalarmclock.settings.importexport.NacImportExportDialog
import com.nfcalarmclock.settings.importexport.NacImportService
import com.nfcalarmclock.support.NacSupportSetting
import com.nfcalarmclock.system.NacCalendar
import com.nfcalarmclock.view.quickToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Main setting fragment.
 */
@AndroidEntryPoint
class NacMainSettingFragment
	: NacBaseSettingFragment()
{

	/**
	 * Import the selected zip file.
	 */
	private val importContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
		val intent = Intent(Intent.ACTION_DEFAULT, uri, context, NacImportService::class.java)
		requireContext().startService(intent)
	}

	/**
	 * Export the shared preferences and database files to a zip file.
	 */
	private val exportContent = registerForActivityResult(ActivityResultContracts.CreateDocument("application/zip")) { uri ->
		val intent = Intent(Intent.ACTION_DEFAULT, uri, context, NacExportService::class.java)
		requireContext().startService(intent)
	}

	/**
	 * Setup the Support preference icon.
	 */
	private fun animateSupportIcon()
	{
		// Get the preference
		val preference = findPreference<Preference>(getString(R.string.key_settings_support))

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

		// Keys for all settings
		val generalKey = getString(R.string.key_settings_general)
		val appearanceKey = getString(R.string.key_settings_appearance)
		val statisticsKey = getString(R.string.key_settings_statistics)
		val manageNfcTagsKey = getString(R.string.key_settings_manage_nfc_tags)
		val aboutKey = getString(R.string.key_settings_about)
		val supportKey = getString(R.string.key_settings_support)
		val importExportKey = getString(R.string.key_settings_import_export)

		var destinationId: Int

		// Check the preference key
		when (preferenceKey)
		{
			// General
			generalKey -> destinationId = R.id.action_nacMainSettingFragment_to_nacGeneralSettingFragment

			// Appearance
			appearanceKey -> destinationId = R.id.action_nacMainSettingFragment_to_nacAppearanceSettingFragment

			// Manage NFC tags
			manageNfcTagsKey -> destinationId = R.id.action_nacMainSettingFragment_to_nacNfcTagSettingFragment

			// Statistics
			statisticsKey -> destinationId = R.id.action_nacMainSettingFragment_to_nacStatisticsSettingFragment

			// About
			aboutKey -> destinationId = R.id.action_nacMainSettingFragment_to_nacAboutSettingFragment

			// Import/export
			importExportKey ->
			{
				// Create the dialog and import/export manager
				val dialog = NacImportExportDialog()

				// Set the import listener
				dialog.onImportListener = NacImportExportDialog.OnImportListener {

					// Launch the file chooser
					importContent.launch("application/zip")

				}

				// Set the export listener
				dialog.onExportListener = NacImportExportDialog.OnExportListener {

					// Get the app name
					val appName = resources.getString(R.string.app_name)
						.lowercase()
						.replace(" ", "_")

					// Get the current timestamp
					val timestamp = NacCalendar.getTimestamp("yyyy-MM-dd HH:mm:SS")
						.replace(" ", "_")
						.replace(":", "")

					// Get the filename
					val filename = "${appName}_${timestamp}.zip"

					// Launch the file chooser
					exportContent.launch(filename)

				}

				// Show the dialog
				dialog.show(parentFragmentManager, NacImportExportDialog.TAG)

				// Default return
				return super.onPreferenceTreeClick(preference)
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
		findNavController().navigate(destinationId)

		// Default return
		return super.onPreferenceTreeClick(preference)
	}

	/**
	 * Setup the Manage NFC tags preference.
	 */
	private fun setupManageNfcTags()
	{
		// Prepare the preference
		val preference = findPreference<Preference>(getString(R.string.key_settings_manage_nfc_tags))

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
		val preference = findPreference<Preference>(getString(R.string.key_settings_support))
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
					sharedPreferences!!.wasAppSupported = true

					// Re-draw the support icon
					animateSupportIcon()
				}

			}

		}

		// Start the support flow
		support.start()
	}
}

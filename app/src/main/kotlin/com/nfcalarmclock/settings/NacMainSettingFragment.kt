package com.nfcalarmclock.settings

import android.animation.AnimatorInflater
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.preference.Preference
import com.nfcalarmclock.R
import com.nfcalarmclock.settings.nfc.NacNfcTagSettingFragment
import com.nfcalarmclock.settings.importexport.NacExportManager
import com.nfcalarmclock.settings.importexport.NacImportExportDialog
import com.nfcalarmclock.settings.importexport.NacImportManager
import com.nfcalarmclock.statistics.NacStatisticsSettingFragment
import com.nfcalarmclock.support.NacSupportSetting
import com.nfcalarmclock.view.quickToast
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
	 * Import manager. This will only register the activity result, but otherwise will do
	 * nothing if it is not used.
	 */
	private val importManager = NacImportManager(this)

	/**
	 * Export manager. This will only register the activity result, but otherwise will do
	 * nothing if it is not used.
	 */
	private val exportManager = NacExportManager(this)

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
		val fragment: Fragment
		val title: String

		// Keys for all settings
		val generalKey = getString(R.string.key_settings_general)
		val appearanceKey = getString(R.string.key_settings_appearance)
		val statisticsKey = getString(R.string.key_settings_statistics)
		val manageNfcTagsKey = getString(R.string.key_settings_manage_nfc_tags)
		val aboutKey = getString(R.string.key_settings_about)
		val supportKey = getString(R.string.key_settings_support)
		val importExportKey = getString(R.string.key_settings_import_export)

		// Check the preference key
		when (preferenceKey)
		{
			// General
			generalKey ->
			{
				fragment = NacGeneralSettingFragment()
				title = getString(R.string.title_setting_general)
			}

			// Appearance
			appearanceKey ->
			{
				fragment = NacAppearanceSettingFragment()
				title = getString(R.string.title_setting_appearance)
			}

			// Statistics
			statisticsKey ->
			{
				fragment = NacStatisticsSettingFragment()
				title = getString(R.string.title_setting_statistics)
			}

			// Manage NFC tags
			manageNfcTagsKey ->
			{
				fragment = NacNfcTagSettingFragment()
				title = getString(R.string.title_setting_manage_nfc_tags)
			}

			// About
			aboutKey ->
			{
				fragment = NacAboutSettingFragment()
				title = getString(R.string.title_setting_about)
			}

			// Import/export
			importExportKey ->
			{
				// Create the dialog and import/export manager
				val dialog = NacImportExportDialog()

				// Set the import listener
				dialog.onImportListener = NacImportExportDialog.OnImportListener {

					// Launch the file chooser
					importManager.launch()

				}

				// Set the export listener
				dialog.onExportListener = NacImportExportDialog.OnExportListener {

					// Launch the file chooser
					exportManager.launch(this)

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
		parentFragmentManager.beginTransaction()
			.replace(android.R.id.content, fragment)
			.addToBackStack(title)
			.commit()

		// Default return
		return super.onPreferenceTreeClick(preference)
	}

	/**
	 * Called after the view is created.
	 */
	override fun onViewCreated(view: View, savedInstanceState: Bundle?)
	{
		// Super
		super.onViewCreated(view, savedInstanceState)

		// Check if API < 35, then edge-to-edge is not enforced and do not need to do
		// anything
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM)
		{
			return
		}

		// TODO: Can maybe customize this more when going up to API 36, but for now opting out
		//// Setup edge to edge for the recyclerview
		//listView.setupEdgeToEdge { insets ->

		//	// Save the top margin value for the recyclerview
		//	(activity as NacMainSettingActivity).rvTopMargin = insets.top

		//}

		//// Get the top margin value that was saved for the recyclerview
		//val rvTopMargin = (activity as NacMainSettingActivity).rvTopMargin

		//// When the main settings fragment is navigated back to, the above edge to edge
		//// setup will not work. In this case, the margin needs to be set directly,
		//// instead of being set in a window insets listener
		//if (rvTopMargin > 0)
		//{
		//	listView.updateLayoutParams<ViewGroup.MarginLayoutParams> {
		//		topMargin = rvTopMargin
		//	}
		//}
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

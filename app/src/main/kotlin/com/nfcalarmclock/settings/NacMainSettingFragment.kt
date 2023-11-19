package com.nfcalarmclock.settings

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.TransitionDrawable
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.preference.Preference
import com.nfcalarmclock.R
import com.nfcalarmclock.statistics.NacStatisticsSettingFragment
import com.nfcalarmclock.support.NacSupportSetting
import com.nfcalarmclock.util.NacUtility.quickToast
import kotlinx.coroutines.launch

/**
 * Main setting fragment.
 */
class NacMainSettingFragment
	: NacGenericSettingFragment()
{

	/**
	 * Setup the Support preference icon.
	 */
	@Suppress("deprecation")
	private fun animateSupportIcon()
	{
		// Get the preference
		val preference = findPreference<Preference>(getString(R.string.support_setting_key))

		// Create the icons
		val whiteDrawable = createIconDrawable(R.mipmap.favorite)
		val redDrawable = createIconDrawable(R.mipmap.favorite)

		// Create the transition icon
		val transitionDrawable = TransitionDrawable(arrayOf(whiteDrawable, redDrawable))

		// Get the color based on the version
		val color = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
		{
			requireContext().getColor(R.color.red)
		}
		else
		{
			resources.getColor(R.color.red)
		}

		// Color the icon that will be transitioned to
		redDrawable.setTint(color)

		// Set the icon
		preference!!.icon = transitionDrawable

		// Start the transition
		transitionDrawable.startTransition(1000)
	}

	/**
	 * Get the icon drawable.
	 *
	 * @return The icon drawable.
	 */
	private fun createIconDrawable(id: Int): BitmapDrawable
	{
		// Get the size that the drawable should be
		val size = resources.getDimension(R.dimen.isz_large).toInt()

		// Create the bitmap
		val bitmap = BitmapFactory.decodeResource(resources, id)

		// Scale the bitmap to the size
		val scaled = Bitmap.createScaledBitmap(bitmap, size, size, true)

		// Create the drawable from the scaled bitmap
		return BitmapDrawable(resources, scaled)
	}

	/**
	 * Called when the preferences are created.
	 */
	override fun onCreatePreferences(savedInstanceState: Bundle?,
		rootKey: String?)
	{
		// Inflate the XML file and add the hierarchy to the current preference
		addPreferencesFromResource(R.xml.main_preferences)

		// Setup the icons for each preference
		setupAppearanceIcon()
		setupGeneralIcon()
		setupStatisticsIcon()
		setupAboutIcon()
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
	 * Setup the About preference icon.
	 */
	private fun setupAboutIcon()
	{
		val preference = findPreference<Preference>(getString(R.string.about_setting_key))
		val drawable = createIconDrawable(R.mipmap.about)

		preference!!.icon = drawable
	}

	/**
	 * Setup the Appearance preference icon.
	 */
	private fun setupAppearanceIcon()
	{
		val preference = findPreference<Preference>(getString(R.string.appearance_setting_key))
		val drawable = createIconDrawable(R.mipmap.palette)

		preference!!.icon = drawable
	}

	/**
	 * Setup the General preference icon.
	 */
	private fun setupGeneralIcon()
	{
		val preference = findPreference<Preference>(getString(R.string.general_setting_key))
		val drawable = createIconDrawable(R.mipmap.settings)

		preference!!.icon = drawable
	}

	/**
	 * Setup the Statistics preference icon.
	 */
	private fun setupStatisticsIcon()
	{
		val preference = findPreference<Preference>(getString(R.string.stats_setting_key))
		val drawable = createIconDrawable(R.mipmap.analytics)

		preference!!.icon = drawable
	}

	/**
	 * Setup the Support preference icon.
	 */
	@Suppress("deprecation")
	private fun setupSupportIcon()
	{
		// Get the preference
		val preference = findPreference<Preference>(getString(R.string.support_setting_key))

		// Create the icon
		val drawable = createIconDrawable(R.mipmap.favorite)

		// Check if the user has shown their support
		if (sharedPreferences!!.wasAppSupported)
		{
			// Get the color based on the version
			val color = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
			{
				requireContext().getColor(R.color.red)
			}
			else
			{
				resources.getColor(R.color.red)
			}

			// Change the color of the heart to show that the user has shown their
			// support
			drawable.setTint(color)
		}

		// Set the icon
		preference!!.icon = drawable
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